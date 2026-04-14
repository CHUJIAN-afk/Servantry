package first.servantry.common.attachment;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.ActiveMarker;
import first.servantry.api.OBB;
import first.servantry.api.item.IWhipWeapon;
import first.servantry.api.register.MarkerType;
import first.servantry.register.DamageRegister;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WhipData implements AttachmentSyncHandler<WhipData> {

    // ==========================================
    // 【参数微调区】
    // ==========================================
    public static final float HANDLE_OFFSET_X = -0.35f;
    public static final float HANDLE_OFFSET_Y = -0.5f;
    public static final float HANDLE_OFFSET_Z = 0.4f;
    public static final float HANDLE_LEN = 0.35f;
    public static final float HANDLE_SWING_AMPLITUDE = 0f; // 手柄鞭甩的幅度 (角度)
    public static final float MIN_LENGTH_COEF = 0.3f;

    public static final float CURL_MAGNITUDE = 10f * (float)Math.PI;
    public static final int TOTAL_COLLISION_SAMPLES = 60;

    public static final double WHIP_BASE_THICKNESS = 0.15;
    public static final float TIP_SCALE_MAX = 3.0f;

    // UV 锁定配置
    public static final float TEXTURE_WIDTH = 64.0f;
    public static final float HANDLE_PIXELS = 13.0f;
    public static final float TIP_PIXELS = 13.0f;
    // ==========================================

    private float progress = 0;
    private float lastProgress = 0;
    private boolean isAttacking = false;
    private int attackSlot = -1;

    // 【新增】：用于保存每次挥击的随机平面角度
    private float swingAngle = 0;

    private final Set<Integer> hitTargets = new HashSet<>();
    private int lastHitEntityId = -1;

    private int markedEntityId = -1;
    private ActiveMarker activeMarker = null;

    public float getProgress() { return progress; }
    public boolean isAttacking() { return isAttacking; }
    public int getMarkedEntityId() { return markedEntityId; }
    public ActiveMarker getActiveMarker() { return activeMarker; }

    public boolean isMarkTarget(LivingEntity target) {
        return activeMarker != null && target.getId() == markedEntityId;
    }

    public void startAttack(Player player) {
        if (!this.isAttacking) {
            this.isAttacking = true;
            this.progress = 0;
            this.lastProgress = 0;
            this.hitTargets.clear();
            this.lastHitEntityId = -1;
            this.attackSlot = player.getInventory().selected;
            this.swingAngle = player.getRandom().nextFloat() * 2.0f * (float)Math.PI;

            ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (itemStack.getItem() instanceof IWhipWeapon whipWeapon) {
                if (!player.level().isClientSide()) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), whipWeapon.getSwingSound(), player.getSoundSource(), 1, 1);
                }
            }
        }
    }

    public void tick(Player player) {
        if (this.activeMarker != null) {
            this.activeMarker.tick();
            if (this.activeMarker.isExpired()) {
                this.activeMarker = null;
                this.markedEntityId = -1;
            }
        }

        if (!isAttacking) return;

        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (!(itemStack.getItem() instanceof IWhipWeapon whipWeapon) || player.getInventory().selected != this.attackSlot) {
            this.isAttacking = false;
            this.progress = 0;
            this.lastProgress = 0;
            this.hitTargets.clear();
            this.lastHitEntityId = -1;
            return;
        }

        this.lastProgress = this.progress;
        this.progress += 1.0f / whipWeapon.getUseTime();
        player.resetAttackStrengthTicker();
        player.swinging = false;

        if (this.lastProgress < 0.5f && this.progress >= 0.5f) {
            if (!player.level().isClientSide()) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), whipWeapon.getTipHitSound(), player.getSoundSource(), 1, 1);
            }
        }

        boolean finished = false;
        if (this.progress >= 1.0f) {
            this.progress = 1.0f;
            player.resetAttackStrengthTicker();
            finished = true;
        }

        if (!player.level().isClientSide()) {
            performSegmentedCollision(player, whipWeapon);
            if (finished && this.lastHitEntityId != -1) {
                if (player.level().getEntity(lastHitEntityId) instanceof LivingEntity lastTarget) {
                    whipWeapon.onLastTargetHit(player, lastTarget);
                    this.markedEntityId = lastTarget.getId();
                    MarkerType type = whipWeapon.getBoundMarker();
                    this.activeMarker = new ActiveMarker(type, type.getDurationTicks());
                }
            }
        }

        if (finished) {
            this.isAttacking = false;
            this.progress = 0;
            this.lastProgress = 0;
            this.hitTargets.clear();
        }
    }

    private void performSegmentedCollision(Player player, IWhipWeapon whipWeapon) {
        int startSample = (int) Math.floor(this.lastProgress * TOTAL_COLLISION_SAMPLES);
        int endSample = (int) Math.floor(this.progress * TOTAL_COLLISION_SAMPLES);

        boolean penetrate = whipWeapon.canPenetrateBlocks();
        float damageFalloff = whipWeapon.getDamageFalloff();
        float baseDamage = whipWeapon.getDamage();

        for (int step = startSample + 1; step <= endSample; step++) {
            float t = (float) step / TOTAL_COLLISION_SAMPLES;
            if (t > 1.0f) t = 1.0f;
            List<Vec3> points = getWhipPoints(player, t, whipWeapon, 1.0f);

            if (points.size() < 2) continue;
            int numSegments = points.size() - 1;

            for (int i = 0; i < numSegments; i++) {
                Vec3 p1 = points.get(i);
                Vec3 p2 = points.get(i + 1);

                if (!penetrate) {
                    BlockHitResult hitResult = player.level().clip(new ClipContext(p1, p2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                    if (hitResult.getType() == HitResult.Type.BLOCK) break;
                }

                Vec3 dir = p2.subtract(p1);
                double len = dir.length();
                if (len < 1e-4) continue;

                float scaleMod = 1.0f + ((float)i / numSegments) * (TIP_SCALE_MAX - 1.0f);
                double currentThickness = WHIP_BASE_THICKNESS * scaleMod;

                Vec3 normDir = dir.normalize();
                float yaw = (float) Math.toDegrees(Math.atan2(-normDir.x, normDir.z));
                float pitch = (float) Math.toDegrees(Math.asin(-normDir.y));
                Vec3 center = p1.add(dir.scale(0.5));
                Vec3 size = new Vec3(currentThickness, currentThickness, len);

                OBB obb = new OBB(center, size, yaw, pitch, 0);
                AABB broadBox = obb.getBoundingBox();

                List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, broadBox, e -> e != player && e.isAlive());
                for (LivingEntity target : targets) {
                    if (obb.intersects(target.getBoundingBox())) {
                        if (hitTargets.add(target.getId())) {
                            float falloffMultiplier = (float) Math.pow(1 - damageFalloff, hitTargets.size() - 1);
                            float finalDamage = baseDamage * falloffMultiplier;

                            target.invulnerableTime = 0;
                            Registry<DamageType> damageTypes = player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
                            target.hurt(new DamageSource(damageTypes.getHolderOrThrow(DamageRegister.Servant), player), finalDamage);

                            whipWeapon.onHitEntity(player, target);
                            this.lastHitEntityId = target.getId();
                        }
                    }
                }
            }
        }
    }

    private List<Vec3> getWhipPoints(Player player, float rawP, IWhipWeapon whipWeapon, float partialTick) {
        Vec3 eyePos = player.getEyePosition(partialTick);
        float viewYaw = player.getViewYRot(partialTick);
        float viewPitch = player.getViewXRot(partialTick);
        double totalLength = whipWeapon.getLength();

        boolean isRightHand = player.getMainArm() == HumanoidArm.RIGHT;
        float shoulderX = isRightHand ? HANDLE_OFFSET_X : -HANDLE_OFFSET_X;
        Vec3 shoulderOffset = new Vec3(shoulderX, HANDLE_OFFSET_Y, HANDLE_OFFSET_Z)
                .xRot((float) Math.toRadians(-viewPitch))
                .yRot((float) Math.toRadians(-viewYaw));
        Vec3 pivotPos = eyePos.add(shoulderOffset);

        float whipExt;
        float curlPhi;

        if (rawP < 0.5f) {
            float t = rawP * 2.0f;
            float easeT = (float)Math.pow(t, 0.6);
            whipExt = MIN_LENGTH_COEF + (1.0f - MIN_LENGTH_COEF) * easeT;
            curlPhi = CURL_MAGNITUDE * (1.0f - easeT);
        } else {
            float t = (rawP - 0.5f) * 2.0f;
            float easeT = (float)Math.pow(t, 1.2);
            whipExt = 1.0f - (1.0f - MIN_LENGTH_COEF) * easeT;
            curlPhi = -CURL_MAGNITUDE * easeT;
        }

        Vec3 wristStartDir = Vec3.directionFromRotation(viewPitch - HANDLE_SWING_AMPLITUDE, viewYaw + (isRightHand ? HANDLE_SWING_AMPLITUDE : -HANDLE_SWING_AMPLITUDE)).normalize();

        Vec3 eyeLook = Vec3.directionFromRotation(viewPitch, viewYaw);
        Vec3 camRight = Vec3.directionFromRotation(0, viewYaw + 90f).normalize();
        Vec3 camUp = camRight.cross(eyeLook).normalize();

        Vec3 aimTarget = eyePos.add(eyeLook.scale(totalLength));
        Vec3 whipForward = aimTarget.subtract(pivotPos).normalize();
        Vec3 armDir = wristStartDir.lerp(whipForward, whipExt).normalize();

        Vec3 baseRight = whipForward.cross(camUp).normalize();
        if (baseRight.lengthSqr() < 1e-5) baseRight = camRight;
        Vec3 baseUp = baseRight.cross(whipForward).normalize();

        // 【直接使用缓存的全局平切角，彻底杜绝闪烁】
        Vec3 curlPlaneUp = baseUp.scale(Math.cos(this.swingAngle)).add(baseRight.scale(Math.sin(this.swingAngle))).normalize();

        int segments = Math.max(12, (int) (totalLength * 5));
        List<Vec3> points = new ArrayList<>(segments + 1);

        points.add(pivotPos);
        Vec3 handleEnd = pivotPos.add(armDir.scale(HANDLE_LEN));
        points.add(handleEnd);

        int remainSegs = segments - 1;
        double remainingMaxLen = totalLength - HANDLE_LEN;
        double currentRemainingLen = remainingMaxLen * whipExt;
        if (currentRemainingLen < 0.1) currentRemainingLen = 0.1;

        double ds = currentRemainingLen / remainSegs;
        Vec3 currentPos = handleEnd;

        for (int i = 1; i <= remainSegs; i++) {
            float t = (float) i / remainSegs;
            Vec3 tangent = armDir.lerp(whipForward, t).normalize();

            float angle = curlPhi * t * t;
            double dx = Math.cos(angle) * ds;
            double dy = Math.sin(angle) * ds;

            currentPos = currentPos.add(tangent.scale(dx)).add(curlPlaneUp.scale(dy));
            points.add(currentPos);
        }
        return points;
    }

    public Vec3 getTipPosition(Player player, float partialTick) {
        if (!isAttacking) return player.getEyePosition(partialTick);
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(itemStack.getItem() instanceof IWhipWeapon whipWeapon)) return player.getEyePosition(partialTick);

        float currentP = Mth.lerp(partialTick, lastProgress, progress);
        List<Vec3> points = getWhipPoints(player, currentP, whipWeapon, partialTick);
        return points.isEmpty() ? player.getEyePosition(partialTick) : points.getLast();
    }

    public void renderWhip(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource, Player player, float partialTick) {
        if (!this.isAttacking) return;
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(itemStack.getItem() instanceof IWhipWeapon whipWeapon)) return;

        float rawP = Mth.lerp(partialTick, this.lastProgress, this.progress);
        List<Vec3> points = getWhipPoints(player, rawP, whipWeapon, partialTick);
        if (points.size() < 2) return;

        float viewYaw = player.getViewYRot(partialTick);
        float viewPitch = player.getViewXRot(partialTick);
        Vec3 eyeLook = Vec3.directionFromRotation(viewPitch, viewYaw);
        Vec3 camRight = Vec3.directionFromRotation(0, viewYaw + 90f).normalize();
        Vec3 camUp = camRight.cross(eyeLook).normalize();

        Vec3 cameraPos = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        com.mojang.blaze3d.vertex.VertexConsumer consumer = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(whipWeapon.getTexture()));

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        int light = LevelRenderer.getLightColor(player.level(), player.blockPosition());
        int numSegments = points.size() - 1;

        // 【核心修改】：计算最大伸展时物理分段的绝对长度
        double maxSegLen = (whipWeapon.getLength() - HANDLE_LEN) / (numSegments - 1);

        for (int i = 1; i < numSegments - 1; i++) {
            // 将 maxSegLen 传给渲染器
            renderWhipSegment(poseStack, consumer, points, i, numSegments, cameraPos, light, false, camRight, camUp, maxSegLen);
        }
        renderWhipSegment(poseStack, consumer, points, 0, numSegments, cameraPos, light, true, camRight, camUp, maxSegLen);
        renderWhipSegment(poseStack, consumer, points, numSegments - 1, numSegments, cameraPos, light, true, camRight, camUp, maxSegLen);

        poseStack.popPose();

        if (rawP > 0.05f) {
            Vec3 currentTip = points.getLast();
            List<Vec3> prevPoints = getWhipPoints(player, rawP - 0.05f, whipWeapon, partialTick);
            Vec3 prevTip = prevPoints.getLast();
            Vec3 movementVector = currentTip.subtract(prevTip).normalize();
            whipWeapon.onTipRender(player, currentTip, movementVector);
        }
    }

    private void renderWhipSegment(PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer consumer, List<Vec3> points, int i, int numSegments, Vec3 cameraPos, int light, boolean isExtremity, Vec3 camRight, Vec3 camUp, double maxSegLen) {
        Vec3 p1 = points.get(i);
        Vec3 p2 = points.get(i + 1);
        Vec3 center = p1.add(p2).scale(0.5);

        if (isExtremity) {
            Vec3 toCamera = cameraPos.subtract(center).normalize().scale(0.015);
            p1 = p1.add(toCamera);
            p2 = p2.add(toCamera);
            center = center.add(toCamera);
        }

        Vec3 dir = p2.subtract(p1);
        double segLen = dir.length();
        if (segLen < 1e-4) return;
        Vec3 tangent = dir.normalize();

        Vec3 right = tangent.cross(camUp).normalize();
        if (right.lengthSqr() < 1e-5) right = camRight;
        Vec3 realUp = right.cross(tangent).normalize();

        float twist = i * (float)Math.PI / 4.0f;
        Vec3 renderRight = right.scale(Math.cos(twist)).add(realUp.scale(Math.sin(twist)));
        Vec3 renderUp = realUp.scale(Math.cos(twist)).subtract(right.scale(Math.sin(twist)));

        float scaleMod = 1.0f + ((float)i / numSegments) * (TIP_SCALE_MAX - 1.0f);
        double currentThickness = WHIP_BASE_THICKNESS * scaleMod;

        // ==============================================================
        // 【核心修改：固定渲染尺寸，以重叠代替拉伸】
        // ==============================================================
        double drawLen;
        if (i == 0) {
            drawLen = HANDLE_LEN; // 手柄：渲染尺寸绝对锁死为物理长度，绝不拉伸
        } else if (i == numSegments - 1) {
            drawLen = maxSegLen; // 鞭梢：渲染尺寸锁死为最大伸缩段
        } else {
            drawLen = maxSegLen * 1.4; // 鞭身：保持最大长度的 1.4 倍，收缩时这块渲染会大量相互重叠！
        }

        Vec3 drawP1 = center.subtract(tangent.scale(drawLen / 2));
        Vec3 drawP2 = center.add(tangent.scale(drawLen / 2));

        float u0, u1;
        if (i == 0) {
            u0 = 0.0f; u1 = HANDLE_PIXELS / TEXTURE_WIDTH;
        } else if (i == numSegments - 1) {
            u0 = (TEXTURE_WIDTH - TIP_PIXELS) / TEXTURE_WIDTH; u1 = 1.0f;
        } else {
            u0 = HANDLE_PIXELS / TEXTURE_WIDTH; u1 = (TEXTURE_WIDTH - TIP_PIXELS) / TEXTURE_WIDTH;
        }

        Vec3 sideVec = renderRight.scale(currentThickness / 2);
        drawWhipQuad(poseStack, consumer, drawP1.add(sideVec), drawP2.add(sideVec), drawP2.subtract(sideVec), drawP1.subtract(sideVec), u0, u1, light);

        Vec3 upVec = renderUp.scale(currentThickness / 2);
        drawWhipQuad(poseStack, consumer, drawP1.add(upVec), drawP2.add(upVec), drawP2.subtract(upVec), drawP1.subtract(upVec), u0, u1, light);
    }

    private void drawWhipQuad(com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer consumer, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, float u0, float u1, int light) {
        PoseStack.Pose pose = poseStack.last();
        org.joml.Matrix4f matrix = pose.pose();
        consumer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).setColor(255, 255, 255, 255).setUv(u0, 0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).setColor(255, 255, 255, 255).setUv(u1, 0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, (float) v3.x, (float) v3.y, (float) v3.z).setColor(255, 255, 255, 255).setUv(u1, 1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, (float) v4.x, (float) v4.y, (float) v4.z).setColor(255, 255, 255, 255).setUv(u0, 1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
    }

    public void renderDebug(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource, Player player, float partialTick) {
        if (!this.isAttacking) return;
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(itemStack.getItem() instanceof IWhipWeapon whipWeapon)) return;

        float currentP = Mth.lerp(partialTick, this.lastProgress, this.progress);
        List<Vec3> points = getWhipPoints(player, currentP, whipWeapon, partialTick);
        if (points.size() < 2) return;

        Vec3 cameraPos = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        com.mojang.blaze3d.vertex.VertexConsumer consumer = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.lines());
        int numSegments = points.size() - 1;

        for (int i = 0; i < numSegments; i++) {
            Vec3 p1 = points.get(i);
            Vec3 p2 = points.get(i + 1);
            Vec3 dir = p2.subtract(p1);
            double len = dir.length();
            if (len < 1e-4) continue;

            float scaleMod = 1.0f + ((float)i / numSegments) * (TIP_SCALE_MAX - 1.0f);
            double currentThickness = WHIP_BASE_THICKNESS * scaleMod;

            Vec3 normDir = dir.normalize();
            float yaw = (float) Math.toDegrees(Math.atan2(-normDir.x, normDir.z));
            float pitch = (float) Math.toDegrees(Math.asin(-normDir.y));
            Vec3 center = p1.add(dir.scale(0.5));

            poseStack.pushPose();
            poseStack.translate(center.x - cameraPos.x, center.y - cameraPos.y, center.z - cameraPos.z);
            poseStack.mulPose(com.mojang.math.Axis.YN.rotationDegrees(yaw));
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(pitch));

            AABB localBox = new AABB(-currentThickness / 2.0, -currentThickness / 2.0, -len / 2.0, currentThickness / 2.0, currentThickness / 2.0, len / 2.0);
            net.minecraft.client.renderer.LevelRenderer.renderLineBox(poseStack, consumer, localBox, 1.0F, 0.5F, 0.0F, 1.0F);
            poseStack.popPose();
        }
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf, WhipData data, boolean isSelf) {
        buf.writeBoolean(data.isAttacking);
        if (data.isAttacking) {
            buf.writeFloat(data.progress);
            buf.writeFloat(data.lastProgress);
            buf.writeInt(data.attackSlot);
            buf.writeFloat(data.swingAngle); // 序列化平面角度
            buf.writeInt(data.lastHitEntityId);
        }

        buf.writeInt(data.markedEntityId);
        buf.writeBoolean(data.activeMarker != null);
        if (data.activeMarker != null) {
            data.activeMarker.write(buf);
        }
    }

    @Override
    public @Nullable WhipData read(@NotNull IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable WhipData oldData) {
        WhipData data = oldData != null ? oldData : new WhipData();

        data.isAttacking = buf.readBoolean();
        if (data.isAttacking) {
            data.progress = buf.readFloat();
            data.lastProgress = buf.readFloat();
            data.attackSlot = buf.readInt();
            data.swingAngle = buf.readFloat(); // 反序列化平面角度
            data.lastHitEntityId = buf.readInt();
        } else {
            data.progress = 0;
            data.lastProgress = 0;
            data.attackSlot = -1;
            data.swingAngle = 0;
            data.lastHitEntityId = -1;
        }

        data.markedEntityId = buf.readInt();
        if (buf.readBoolean()) {
            data.activeMarker = ActiveMarker.read(buf);
        } else {
            data.activeMarker = null;
        }

        return data;
    }

}