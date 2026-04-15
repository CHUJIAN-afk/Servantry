package first.servantry.common.servent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import first.servantry.api.PathNode;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.IBlockCollider;
import first.servantry.api.servant.IDamagingOnCollide;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ServantRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class SlimeBaby extends Servant implements IDamagingOnCollide, IBlockCollider {

    public enum SlimeState {
        IDLE_GROUND,    // 原地落地待机
        IDLE_FLY,       // 悬浮待机 (防虚空/悬崖掉落)
        JUMPING,        // 抛物线跳跃中 (不可打断)
        FALLING,        // 悬空坠落中
        RETURN_JUMP,    // 超距跳跃回归
        RETURN_FLY      // 终极兜底飞行
    }

    private SlimeState state = SlimeState.IDLE_GROUND;
    private int hitClearTimer = 0;
    private final Set<Integer> swingHitTargets = new HashSet<>();

    public SlimeBaby(PathNode node) {
        super(node);
    }

    @Override
    public AABB getHitbox() {
        return new AABB(-0.25, 0, -0.25, 0.25, 0.5, 0.25);
    }

    @Override
    public double getBlockCollisionRepulsion() {
        return 0.25;
    }

    @Override
    public boolean shouldCollideWithBlocks(Servant servant) {
        return state != SlimeState.RETURN_FLY && state != SlimeState.IDLE_FLY;
    }

    @Override
    public void collisionAttack(Set<LivingEntity> hitTargets) {
        for (LivingEntity target : hitTargets) {
            if (swingHitTargets.add(target.getId())) {
                int invulnerableTime = target.invulnerableTime;
                target.invulnerableTime = 0;
                target.hurt(getDamageSource(), 0.8f);
                target.invulnerableTime = invulnerableTime;
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        hitClearTimer++;
        if (hitClearTimer >= 4) {
            swingHitTargets.clear();
            hitClearTimer = 0;
        }

        Player owner = getOwner();
        if (owner != null && !owner.level().isClientSide()) {
            serverTickAI(owner);
        }
    }

    private void serverTickAI(Player owner) {
        double distSqrToOwner = this.getPos().distanceToSqr(owner.position());

        // 1. 距离阶段判定
        if (state == SlimeState.RETURN_FLY || state == SlimeState.RETURN_JUMP) {
            if (distSqrToOwner < 16.0) {
                state = SlimeState.IDLE_GROUND;
            }
        } else if (distSqrToOwner > 576.0) { // > 24格：兜底飞行
            state = SlimeState.RETURN_FLY;
            this.setPath(Collections.emptyList());
        } else if (distSqrToOwner > 256.0) { // > 16格：跳跃回归
            if (state != SlimeState.JUMPING && state != SlimeState.FALLING) {
                state = SlimeState.RETURN_JUMP;
            }
        }

        if (this.isExecutingPath()) return;

        // 重力检测
        if (checkAndApplyGravity(owner)) return;

        findNewTarget(16.0, true);
        LivingEntity target = getTarget();

        if (state == SlimeState.RETURN_FLY) {
            generateReturnFly(owner);
            return;
        }

        if (state == SlimeState.RETURN_JUMP) {
            state = SlimeState.JUMPING;
            generateJumpPath(owner.position(), owner);
            return;
        }

        if (target != null) {
            state = SlimeState.JUMPING;

            Vec3 start = this.getPos();
            Vec3 targetPos = target.position();
            Vec3 dir = new Vec3(targetPos.x - start.x, 0, targetPos.z - start.z);
            if (dir.lengthSqr() > 1e-4) dir = dir.normalize();
            else dir = new Vec3(1, 0, 0);

            // 跳向目标身后，并添加左右随机偏移
            Vec3 baseAim = targetPos.add(dir.scale(1.0));
            Vec3 cross = dir.cross(new Vec3(0, 1, 0)).normalize();
            double lateralOffset = (owner.getRandom().nextDouble() - 0.5) * 1.2;
            baseAim = baseAim.add(cross.scale(lateralOffset));

            generateJumpPath(baseAim, owner);

        } else {
            IdlePosInfo idleInfo = calculateIdlePos(owner);

            // 只要距离自己的专属簇拥点超过 1.5 格 (2.25的平方)，就起跳跟上
            if (this.getPos().distanceToSqr(idleInfo.pos) > 2.25) {
                state = SlimeState.JUMPING;
                generateJumpPath(idleInfo.pos, owner);
            } else {
                // 【核心】：一旦到达允许误差范围内，彻底锁死滑动，只进行原地动画
                state = idleInfo.shouldFly ? SlimeState.IDLE_FLY : SlimeState.IDLE_GROUND;

                float currentScale = this.getPitch();
                if (currentScale <= 0.1f) currentScale = 1.0f;
                // Q弹回正
                if (Math.abs(currentScale - 1.0f) > 0.05f) {
                    currentScale = Mth.lerp(0.35f, currentScale, 1.0f);
                } else {
                    currentScale = 1.0f;
                }

                // 传入 this.getPos() 锁死水平坐标，传入 this.getYaw() 保持最后一次眺望方向
                this.setPath(Collections.singletonList(new PathNode("", this.getPos(), this.getYaw(), currentScale, 0)));
            }
        }
    }

    private boolean checkAndApplyGravity(Player owner) {
        if (state == SlimeState.IDLE_FLY || state == SlimeState.RETURN_FLY) return false;

        Vec3 pos = this.getPos();
        BlockHitResult hit = owner.level().clip(new ClipContext(
                pos, pos.add(0, -12, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner
        ));

        if (hit.getType() == HitResult.Type.BLOCK) {
            double groundY = hit.getLocation().y;
            if (pos.y > groundY + 0.1) {
                state = SlimeState.FALLING;
                int fallTicks = Math.max(2, (int) ((pos.y - groundY) * 2));
                List<PathNode> fallNodes = new ArrayList<>();
                for (int i = 1; i <= fallTicks; i++) {
                    float t = (float) i / fallTicks;
                    Vec3 p = pos.lerp(hit.getLocation(), t);
                    float yScale = 1.0f + t * 0.4f; // 下落拉长形变
                    fallNodes.add(new PathNode("", p, this.getYaw(), yScale, 0));
                }
                this.setPath(fallNodes);
                return true;
            }
        } else {
            // 如果下方 12 格都没有方块，为了避免掉出世界，强行转换为悬浮待机状态自保
            state = SlimeState.IDLE_FLY;
            return true;
        }
        return false;
    }

    private void generateJumpPath(Vec3 targetPos, Player owner) {
        Vec3 start = this.getPos();
        Vec3 diff = targetPos.subtract(start);

        double maxJumpDist = 4.5;
        double horizDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        // 防止由于极小距离导致的鬼畜跳跃
        if (horizDist < 0.1) {
            diff = new Vec3(0, diff.y, 0);
        } else if (horizDist > maxJumpDist) {
            double ratio = maxJumpDist / horizDist;
            diff = new Vec3(diff.x * ratio, diff.y * ratio, diff.z * ratio);
        }

        Vec3 end = start.add(diff);
        float startYaw = this.getYaw();
        float endYaw = (float) (Math.atan2(-diff.x, diff.z) * 180 / Math.PI);
        endYaw = startYaw + Mth.wrapDegrees(endYaw - startYaw);

        int ticks = owner.getRandom().nextInt(10, 14);

        List<PathNode> nodes = new ArrayList<>();
        for (int i = 1; i <= ticks; i++) {
            float t = (float) i / ticks;
            double jumpHeightOffset = 4.0 * 1.0 * t * (1.0f - t);
            Vec3 p = start.lerp(end, t).add(0, jumpHeightOffset, 0);
            float yaw = Mth.rotLerp(t, startYaw, endYaw);

            // 正弦波计算起跳压扁和半空拉伸
            float yScale = 0.7f + (float) Math.sin(t * Math.PI) * 0.6f;
            nodes.add(new PathNode("", p, yaw, yScale, 0));
        }
        this.setPath(nodes);
    }

    private void generateReturnFly(Player owner) {
        Vec3 start = this.getPos();
        Vec3 end = owner.position().add(0, owner.getBbHeight() / 2, 0);
        float yaw = (float) (Math.atan2(-(end.x - start.x), end.z - start.z) * 180 / Math.PI);

        List<PathNode> nodes = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            nodes.add(new PathNode("", start.lerp(end, (float) i / 10), yaw, 1.0f, 0));
        }
        this.setPath(nodes);
    }

    /**
     * 计算玩家周围的随机簇拥待机点
     */
    private IdlePosInfo calculateIdlePos(Player owner) {
        // 【核心修改】：利用 UUID 异或生成伪随机种子，让每个史莱姆有固定的相对散布位置
        Random rand = new Random(this.getUuid().hashCode() ^ owner.getUUID().hashCode());

        // 360 度随机方向
        float angle = rand.nextFloat() * (float) (Math.PI * 2);
        // 半径 1.0 到 2.5 格，自然形成簇拥
        float radius = 2.5f + rand.nextFloat() * 2.5f;

        double dx = Math.sin(angle) * radius;
        double dz = Math.cos(angle) * radius;
        Vec3 targetXZ = owner.position().add(dx, 0, dz);

        // 探测合理落脚点 (往下探 6 格)
        BlockHitResult hit = owner.level().clip(new ClipContext(
                targetXZ.add(0, 2, 0), targetXZ.add(0, -6, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner
        ));

        if (hit.getType() == HitResult.Type.BLOCK) {
            return new IdlePosInfo(hit.getLocation(), false);
        }
        // 往下 6 格都没有落脚方块，采取飞行悬浮姿态 (保持玩家身高一半的高度)
        return new IdlePosInfo(targetXZ.add(0, owner.getBbHeight() * 0.5, 0), true);
    }

    private static class IdlePosInfo {
        Vec3 pos;
        boolean shouldFly;
        IdlePosInfo(Vec3 pos, boolean shouldFly) {
            this.pos = pos;
            this.shouldFly = shouldFly;
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YN.rotationDegrees(renderNode.yaw()));

        float yScale = renderNode.pitch();

        float floatOffsetBase = 0.0f;
        float floatAmplitude = 0.15f;
        float floatSpeed = 0.15f;

        Player owner = getOwner();
        float hoverY = 0f;
        // 悬浮待机状态应用上下浮动正弦波
        if (owner != null && state == SlimeState.IDLE_FLY) {
            hoverY = Mth.sin((owner.getData(AttachmentRegister.ServantData).getOrder(this) + owner.tickCount + partialTick) * floatSpeed) * floatAmplitude;
        }

        // 同步平移
        poseStack.translate(0, 0.25f * yScale + floatOffsetBase + hoverY, 0.5f);
        // 执行 Y 轴起跳压扁/拉长渲染
        poseStack.scale(1, yScale, 1);

        if (owner != null) {
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    Blocks.SLIME_BLOCK.asItem().getDefaultInstance(),
                    ItemDisplayContext.FIXED,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    owner.level(),
                    0
            );
        }

        poseStack.popPose();
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(state);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        this.state = buf.readEnum(SlimeState.class);
    }

    @Override
    public ServantType<? extends Servant> getType() {
        return ServantRegister.SlimeBaby.get();
    }
}