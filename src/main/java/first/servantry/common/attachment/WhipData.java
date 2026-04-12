package first.servantry.common.attachment;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.Marker;
import first.servantry.api.OBB;
import first.servantry.api.item.IWhipWeapon;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.DamageRegister;
import first.servantry.register.SoundRegister;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WhipData implements AttachmentSyncHandler<WhipData> {

    private float progress = 0;
    private float lastProgress = 0;
    private boolean isAttacking = false;

    // 1 为右到左扫，-1 为左到右扫
    private int sweepDirection = 1;

    private final Set<Integer> hitTargets = new HashSet<>();
    private int lastHitEntityId = -1;

    private int markedEntityId = -1;
    private Marker activeMarker = null;

    private static final double WHIP_THICKNESS = 0.25;

    public float getProgress() { return progress; }
    public float getLastProgress() { return lastProgress; }
    public boolean isAttacking() { return isAttacking; }
    public int getMarkedEntityId() { return markedEntityId; }
    public Marker getActiveMarker() { return activeMarker; }

    public void startAttack(Player player) {
        if (!this.isAttacking) {
            this.isAttacking = true;
            this.progress = 0;
            this.lastProgress = 0;
            this.hitTargets.clear();
            this.lastHitEntityId = -1;

            this.sweepDirection = (this.sweepDirection == 1) ? -1 : 1;

            if (!player.level().isClientSide()) {
                ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
                if (itemStack.getItem() instanceof IWhipWeapon) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundRegister.UseWhip.get(), player.getSoundSource(), 1, 1);
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
        if (itemStack.getItem() instanceof IWhipWeapon whipWeapon) {
            IWhipWeapon.WhipProperties props = whipWeapon.getWhipProperties();

            this.lastProgress = this.progress;
            this.progress += 1.0f / props.useTime();
            player.resetAttackStrengthTicker();

            if (this.lastProgress < 0.5f && this.progress >= 0.5f) {
                if (!player.level().isClientSide()) {
                    SoundEvent sound = props.swingSound() != null ? props.swingSound() : SoundRegister.ShakeWhip.get();
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, player.getSoundSource(), 1, 1);
                }
            }

            boolean finished = false;
            if (this.progress >= 1.0f) {
                player.swingTime = 0;
                this.progress = 1.0f;
                finished = true;
            }

            if (!player.level().isClientSide()) {
                performSegmentedCollision(player, whipWeapon, props);
                if (finished && this.lastHitEntityId != -1) {
                    if (player.level().getEntity(lastHitEntityId) instanceof LivingEntity lastTarget) {
                        whipWeapon.onLastTargetHit(player, lastTarget);
                        Marker markerTemplate = props.marker();
                        if (!(lastTarget instanceof Player) && markerTemplate != null) {
                            this.markedEntityId = lastTarget.getId();
                            this.activeMarker = new Marker(markerTemplate.getType(), markerTemplate.getExtraDamage(), markerTemplate.getRemainingTicks(), markerTemplate.getCritRate());
                        }
                    }
                }
            }
            whipWeapon.sweepTip(player, getTipPosition(player, 0));
            if (finished) {
                this.isAttacking = false;
                this.progress = 0;
                this.lastProgress = 0;
            }
        } else {
            this.isAttacking = false;
        }
    }

    private void performSegmentedCollision(Player player, IWhipWeapon whipWeapon, IWhipWeapon.WhipProperties props) {
        int steps = Math.max(1, (int) Math.ceil((progress - lastProgress) / 0.02f));

        for (int step = 1; step <= steps; step++) {
            float t = lastProgress + (progress - lastProgress) * ((float) step / steps);
            List<Vec3> points = getWhipPoints(player, t, props.length(), 1.0f);

            if (points.size() < 2) continue;

            for (int i = 0; i < points.size() - 1; i++) {
                Vec3 p1 = points.get(i);
                Vec3 p2 = points.get(i + 1);

                if (!props.penetrateBlocks()) {
                    BlockHitResult hitResult = player.level().clip(new ClipContext(p1, p2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                    if (hitResult.getType() == HitResult.Type.BLOCK) {
                        break;
                    }
                }

                Vec3 dir = p2.subtract(p1);
                double len = dir.length();
                if (len < 1e-4) continue;

                Vec3 normDir = dir.normalize();
                float yaw = (float) Math.toDegrees(Math.atan2(-normDir.x, normDir.z));
                float pitch = (float) Math.toDegrees(Math.asin(-normDir.y));
                Vec3 center = p1.add(dir.scale(0.5));
                Vec3 size = new Vec3(WHIP_THICKNESS, WHIP_THICKNESS, len);

                OBB obb = new OBB(center, size, yaw, pitch, 0);
                AABB broadBox = obb.getBoundingBox();

                List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, broadBox, e -> e != player && e.isAlive());
                for (LivingEntity target : targets) {
                    if (obb.intersects(target.getBoundingBox())) {
                        if (hitTargets.add(target.getId())) {
                            float falloffMultiplier = (float) Math.pow(1 - props.damageFalloff(), hitTargets.size() - 1);
                            float finalDamage = props.damage() * falloffMultiplier;

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

    public Vec3 getTipPosition(Player player, float partialTick) {
        if (!isAttacking) return player.getEyePosition(partialTick);
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(itemStack.getItem() instanceof IWhipWeapon whipWeapon)) return player.getEyePosition(partialTick);

        float currentP = Mth.lerp(partialTick, lastProgress, progress);
        List<Vec3> points = getWhipPoints(player, currentP, whipWeapon.getWhipProperties().length(), partialTick);
        return points.isEmpty() ? player.getEyePosition(partialTick) : points.getLast();
    }

    private List<Vec3> getWhipPoints(Player player, float rawP, double totalLength, float partialTick) {
        float p = rawP < 0.5f ? 4.0f * rawP * rawP * rawP : 1.0f - (float) Math.pow(-2.0f * rawP + 2.0f, 3.0f) / 2.0f;

        Vec3 eyePos = player.getEyePosition(partialTick);
        float viewYaw = player.getViewYRot(partialTick);
        float viewPitch = player.getViewXRot(partialTick);
        Vec3 eyeLook = Vec3.directionFromRotation(viewPitch, viewYaw);

        boolean isRightHand = player.getMainArm() == HumanoidArm.RIGHT;
        float shoulderX = isRightHand ? -0.35f : 0.35f;
        Vec3 shoulderOffset = new Vec3(shoulderX, -0.2, 0.0)
                .xRot((float) Math.toRadians(-viewPitch))
                .yRot((float) Math.toRadians(-viewYaw));
        Vec3 pivotPos = eyePos.add(shoulderOffset);

        Vec3 aimTarget = eyePos.add(eyeLook.scale(totalLength));
        Vec3 forwardDir = aimTarget.subtract(pivotPos).normalize();

        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 rightDir = forwardDir.cross(worldUp);
        if (rightDir.lengthSqr() < 1e-5) {
            rightDir = Vec3.directionFromRotation(0, viewYaw + 90f).normalize();
        } else {
            rightDir = rightDir.normalize();
        }
        Vec3 upDir = rightDir.cross(forwardDir).normalize();

        float maxTheta = (float) Math.toRadians(70);
        double theta = maxTheta * this.sweepDirection * (1.0 - 2.0 * p);
        Vec3 currentLook = forwardDir.scale(Math.cos(theta)).add(rightDir.scale(Math.sin(theta))).normalize();

        Vec3 startPos = pivotPos.add(currentLook.scale(0.4));
        Vec3 curlDir = upDir.cross(currentLook).normalize();

        int segments = Math.max(8, (int) (totalLength * 4));
        List<Vec3> points = new ArrayList<>(segments + 1);
        points.add(startPos);

        float lengthMod = 1.0f - Math.abs(p - 0.5f) * 2.0f;
        double currentLen = totalLength * lengthMod;
        if (currentLen < 0.1) return points;
        double ds = currentLen / segments;

        float maxPhi = 2.0f * (float) Math.PI;
        float currentPhi = maxPhi * (1.0f - 2.0f * p);

        Vec3 currentPos = startPos;
        for (int i = 0; i < segments; i++) {
            float t = (float) i / segments;
            float angle = -currentPhi * t * t * this.sweepDirection;

            double dx = Math.cos(angle) * ds;
            double dy = Math.sin(angle) * ds;
            currentPos = currentPos.add(currentLook.scale(dx)).add(curlDir.scale(dy));
            points.add(currentPos);
        }
        return points;
    }

    public void renderWhip(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource, Player player, float partialTick) {
        if (!this.isAttacking) return;
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(itemStack.getItem() instanceof IWhipWeapon whipWeapon)) return;

        IWhipWeapon.WhipProperties props = whipWeapon.getWhipProperties();
        float rawP = Mth.lerp(partialTick, this.lastProgress, this.progress);
        List<Vec3> points = getWhipPoints(player, rawP, props.length(), partialTick);
        if (points.size() < 2) return;

        Vec3 eyePos = player.getEyePosition(partialTick);
        float viewYaw = player.getViewYRot(partialTick);
        float viewPitch = player.getViewXRot(partialTick);
        Vec3 eyeLook = Vec3.directionFromRotation(viewPitch, viewYaw);

        Vec3 cameraPos = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        com.mojang.blaze3d.vertex.VertexConsumer consumer = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(props.texture()));

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        int light = LevelRenderer.getLightColor(player.level(), player.blockPosition());
        float thickness = (float) WHIP_THICKNESS * 0.5f;

        for (int i = 0; i < points.size() - 1; i++) {
            float u1 = (float) i / (points.size() - 1);
            float u2 = (float) (i + 1) / (points.size() - 1);
            Vec3 p1 = points.get(i);
            Vec3 p2 = points.get(i + 1);

            Vec3 dir = p2.subtract(p1).normalize();

            Vec3 renderUp = eyeLook.cross(dir).normalize();
            if (renderUp.lengthSqr() < 1e-5) renderUp = new Vec3(0, 1, 0);
            Vec3 renderRight = dir.cross(renderUp).normalize();

            Vec3 sideVec = renderRight.scale(thickness);
            Vec3 upVec = renderUp.scale(thickness);

            drawWhipQuad(poseStack, consumer, p1.add(sideVec), p1.subtract(sideVec), p2.subtract(sideVec), p2.add(sideVec), u1, u2, light);
            drawWhipQuad(poseStack, consumer, p1.add(upVec), p1.subtract(upVec), p2.subtract(upVec), p2.add(upVec), u1, u2, light);
        }
        poseStack.popPose();
    }

    private void drawWhipQuad(com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer consumer, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, float u1, float u2, int light) {
        PoseStack.Pose pose = poseStack.last();
        org.joml.Matrix4f matrix = pose.pose();

        consumer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).setColor(255, 255, 255, 255).setUv(u1, 0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).setColor(255, 255, 255, 255).setUv(u1, 1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, (float) v3.x, (float) v3.y, (float) v3.z).setColor(255, 255, 255, 255).setUv(u2, 1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, (float) v4.x, (float) v4.y, (float) v4.z).setColor(255, 255, 255, 255).setUv(u2, 0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
    }

    public void renderDebug(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource, Player player, float partialTick) {
        if (!this.isAttacking) return;
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(itemStack.getItem() instanceof IWhipWeapon whipWeapon)) return;

        float currentP = Mth.lerp(partialTick, this.lastProgress, this.progress);
        List<Vec3> points = getWhipPoints(player, currentP, whipWeapon.getWhipProperties().length(), partialTick);
        if (points.size() < 2) return;

        Vec3 cameraPos = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        com.mojang.blaze3d.vertex.VertexConsumer consumer = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.lines());

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 p1 = points.get(i);
            Vec3 p2 = points.get(i + 1);
            Vec3 dir = p2.subtract(p1);
            double len = dir.length();
            if (len < 1e-4) continue;

            Vec3 normDir = dir.normalize();
            float yaw = (float) Math.toDegrees(Math.atan2(-normDir.x, normDir.z));
            float pitch = (float) Math.toDegrees(Math.asin(-normDir.y));
            Vec3 center = p1.add(dir.scale(0.5));

            poseStack.pushPose();
            poseStack.translate(center.x - cameraPos.x, center.y - cameraPos.y, center.z - cameraPos.z);
            poseStack.mulPose(com.mojang.math.Axis.YN.rotationDegrees(yaw));
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(pitch));

            AABB localBox = new AABB(-WHIP_THICKNESS / 2.0, -WHIP_THICKNESS / 2.0, -len / 2.0, WHIP_THICKNESS / 2.0, WHIP_THICKNESS / 2.0, len / 2.0);
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
            buf.writeInt(data.sweepDirection);
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
            data.sweepDirection = buf.readInt();
            data.lastHitEntityId = buf.readInt();
        } else {
            data.progress = 0;
            data.lastProgress = 0;
            data.lastHitEntityId = -1;
        }

        data.markedEntityId = buf.readInt();
        if (buf.readBoolean()) {
            data.activeMarker = Marker.read(buf);
        } else {
            data.activeMarker = null;
        }

        return data;
    }
}