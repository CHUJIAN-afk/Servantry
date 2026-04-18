package first.servantry.common.servent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import first.servantry.api.ai.ActionController;
import first.servantry.api.ai.ServantAction;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.IConeTrailRenderer;
import first.servantry.api.servant.IMomentumControlled;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ItemRegister;
import first.servantry.register.ServantRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StardustCell extends Servant implements IConeTrailRenderer, IMomentumControlled {

    public int trailTimer = 0;
    public LivingEntity currentTarget = null;

    // 接口要求的动量状态
    private Vec3 velocity = Vec3.ZERO;

    private float renderYaw = 0f;
    private float renderPitch = 0f;
    private float renderRoll = 0f;
    private float renderYawO = 0f;
    private float renderPitchO = 0f;
    private float renderRollO = 0f;

    public StardustCell(PathNode node) {
        super(node);
        this.ai = new ActionController<>(this, new ActionCellIdle(this));
    }

    @SuppressWarnings("unchecked")
    public ActionController<StardustCell> getAi() { return (ActionController<StardustCell>) ai; }

    @Override public Vec3 getVelocity() { return this.velocity; }

    @Override public void setVelocity(Vec3 velocity) { this.velocity = velocity; }

    @Override
    public float getBaseDamage() { return 6f; }

    @Override
    public float getBaseKnockback() { return 0.2f; }

    @Override
    public void tick() {
        super.tick();
        Player owner = getOwner();
        if (owner != null) {
            if (owner.level().isClientSide()) clientTick(owner);
            else serverTickAI(owner);
        }
    }

    private void clientTick(Player owner) {
        String actionId = getAi().getCurrentAction().getId();
        if ("teleport".equals(actionId)) {
            trailTimer = 15;
        } else if (trailTimer > 0) {
            trailTimer--;
        }

        renderYawO = renderYaw;
        renderPitchO = renderPitch;
        renderRollO = renderRoll;

        renderYaw += 12f;
        renderPitch += 8f;
        renderRoll += 15f;
    }

    private void serverTickAI(Player owner) {
        LivingEntity newTarget = verifyTarget(owner);

        if (newTarget != null) {
            double distSqr = newTarget.distanceToSqr(this.getPos());
            if (distSqr >= 28.75 * 28.75 && distSqr <= 53.75 * 53.75) {
                if (!(getAi().getCurrentAction() instanceof ActionCellTeleport)) {
                    getAi().forceAction(new ActionCellTeleport(this), newTarget);
                }
            } else if (currentTarget != null && newTarget.getId() != currentTarget.getId()) {
                getAi().forceAction(new ActionCellAttack(this), newTarget);
            }
        }

        this.currentTarget = newTarget;
        getAi().tick(newTarget);
    }

    private LivingEntity verifyTarget(Player owner) {
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        List<LivingEntity> potentialTargets = data.getNearbyTargets(owner, this, 64.0, currentTarget == null);
        if (potentialTargets.isEmpty()) return null;
        potentialTargets.sort(Comparator.comparingDouble(e -> e.distanceToSqr(owner)));
        return potentialTargets.getFirst();
    }

    // 【核心修复】：基于节点消费的射击逻辑
    @Override
    public void onPathNodeConsumed(PathNode node) {
        super.onPathNodeConsumed(node);
        if ("fire".equals(node.feature()) && currentTarget != null) {
            fireCellProjectile(currentTarget);
        }
    }

    public void fireCellProjectile(LivingEntity target) {
        Player owner = getOwner();
        if (owner == null || target == null) return;
        Vec3 targetCenter = target.position().add(0, target.getBbHeight() / 2.0, 0);
        Vec3 shootDir = targetCenter.subtract(this.getPos()).normalize();

        Arrow arrow = new Arrow(EntityType.ARROW, owner.level());
        arrow.setPos(this.getPos());
        arrow.setOwner(owner);
        arrow.shoot(shootDir.x, shootDir.y, shootDir.z, 2.5f, 1.0f);
        owner.level().addFreshEntity(arrow);

        // 动量接口内建支持
        this.applyForce(shootDir.scale(-1.0));
    }

    /**
     * 【核心光环算法】：计算头顶的圆环阵列，随时间缓慢旋转
     */
    public Vec3 getHaloAnchorPos(Player owner, LivingEntity target, int order) {
        int cellsPerTier = 10; // 每层5个，形成好看的五芒星站位
        int tier = order / cellsPerTier;

        double radius = 3.5 + tier * 1.5; // 阶梯状半径变化
        double baseAngle = (order % cellsPerTier) * (Math.PI * 2.0 / cellsPerTier);

        // 缓慢而优雅的自然旋转
        double slowRotation = owner.tickCount * 0.025;
        double angle = baseAngle + slowRotation;

        Vec3 targetCenter = target.position().add(0, target.getBbHeight() + 2.5 + tier * 0.8, 0);

        return targetCenter.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
    }

    @Override
    public ServantAction<?> createAction(String id) {
        return switch (id) {
            case "attack" -> new ActionCellAttack(this);
            case "teleport" -> new ActionCellTeleport(this);
            default -> new ActionCellIdle(this);
        };
    }

    // ================== Action 动作库 ==================

    public static class ActionCellIdle extends ServantAction<StardustCell> {
        private Vec3 wanderOffset = Vec3.ZERO;

        public ActionCellIdle(StardustCell servant) { super(servant); }
        @Override public String getId() { return "idle"; }

        @Override
        public void tick(LivingEntity target) {
            Player owner = servant.getOwner();
            if (target != null) {
                servant.getAi().forceAction(new ActionCellAttack(servant), target);
                return;
            }

            if (wanderOffset.equals(Vec3.ZERO) || owner.tickCount % 60 == 0) {
                wanderOffset = new Vec3(
                        (owner.getRandom().nextDouble() - 0.5) * 12,
                        owner.getRandom().nextDouble() * 3 + 1.5,
                        (owner.getRandom().nextDouble() - 0.5) * 12
                );
            }

            Vec3 targetPos = owner.position().add(wanderOffset);
            Vec3 dir = targetPos.subtract(servant.getPos());
            double dist = dir.length();
            if (dist > 1e-4) dir = dir.normalize();

            double speedBias = dist > 12.0 ? 0.25 : 0.06;
            servant.applyForce(dir.scale(speedBias));
            servant.tickMomentum(servant, 0.82f, dist > 12.0 ? 1.6f : 0.45f);
        }
    }

    public static class ActionCellAttack extends ServantAction<StardustCell> {
        private int shootTimer = 12;

        public ActionCellAttack(StardustCell servant) { super(servant); }
        @Override public String getId() { return "attack"; }
        @Override public boolean isAttack() { return true; }

        @Override
        public void tick(LivingEntity target) {
            if (target == null) {
                servant.getAi().forceAction(new ActionCellIdle(servant), null);
                return;
            }

            Player owner = servant.getOwner();
            ServantData data = owner.getData(AttachmentRegister.ServantData);

            int order = data.getOrder(servant);
            Vec3 anchorPos = servant.getHaloAnchorPos(owner, target, order);

            Vec3 toAnchor = anchorPos.subtract(servant.getPos());
            double distToAnchor = toAnchor.length();

            if (distToAnchor > 0.1) {
                servant.applyForce(toAnchor.normalize().scale(distToAnchor * 0.12));
            }
            float friction = distToAnchor < 0.4 ? 0.4f : 0.85f;
            servant.tickMomentum(servant, friction, 1.8f);

            shootTimer--;
            if (shootTimer <= 0) {
                servant.fireCellProjectile(target);
                shootTimer = 12 + owner.getRandom().nextInt(4);
            }
        }

        @Override public boolean isFinished() { return false; }
    }

    public static class ActionCellTeleport extends ServantAction<StardustCell> {
        private Vec3 startPos;
        private Vec3 targetPos;
        private int ticks = 0;
        private final int duration = 4;

        public ActionCellTeleport(StardustCell servant) { super(servant); }
        @Override public String getId() { return "teleport"; }
        @Override public boolean isAttack() { return true; }

        @Override
        public void onStart(LivingEntity target) {
            if (target == null) return;
            this.startPos = servant.getPos();

            Player owner = servant.getOwner();
            ServantData data = owner.getData(AttachmentRegister.ServantData);

            // 预测抵达时的光环位置
            this.targetPos = servant.getHaloAnchorPos(owner, target, data.getOrder(servant));
        }

        @Override
        public void tick(LivingEntity target) {
            ticks++;
            if (ticks <= duration) {
                float t = (float) ticks / duration;
                float ease = 1.0f - (float) Math.pow(1.0f - t, 3);
                Vec3 currentPos = this.startPos.lerp(this.targetPos, ease);
                // 【核心修复】：将最后一次冲刺的节点打上 "fire" 标签！
                String featureTag = (ticks == duration) ? "fire" : "";
                servant.setPath(Collections.singletonList(new PathNode(featureTag, currentPos, servant.getYaw(), servant.getPitch(), servant.getRoll())));
            } else {
                servant.getAi().forceAction(new ActionCellAttack(servant), target);
            }
        }
        @Override public boolean isFinished() { return false; }
    }

    // ============== 渲染逻辑 ==============

    @Override
    public PathNode getVisualRenderNode(Servant servant, float partialTick, PathNode rawRenderNode) {
        float y = Mth.lerp(partialTick, renderYawO, renderYaw);
        float p = Mth.lerp(partialTick, renderPitchO, renderPitch);
        float r = Mth.lerp(partialTick, renderRollO, renderRoll);
        return new PathNode(rawRenderNode.feature(), rawRenderNode.pos(), y, p, r);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode) {
        PathNode visualNode = getVisualRenderNode(this, partialTick, renderNode);
        poseStack.pushPose();
        Vec3 offset = visualNode.pos().subtract(renderNode.pos());
        poseStack.translate(offset.x, offset.y, offset.z);
        poseStack.mulPose(Axis.YN.rotationDegrees(visualNode.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(visualNode.pitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(visualNode.roll()));
        poseStack.scale(0.5f, 0.5f, 0.5f);

        Player owner = getOwner();
        if(owner != null) {
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    ItemRegister.StardustCell.get().getDefaultInstance(),
                    ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, owner.level(), 0
            );
        }
        poseStack.popPose();
    }

    @Override public int getTrailTimer() { return trailTimer; }
    @Override public float getTrailMaxRadius() { return 0.5f; }
    @Override public int getTrailColorRGB(float progress) { return 0x8AE0FF; }
    @Override public int getTrailResolution() { return 12; }
    @Override public float getTrailFadeOut(float progress) { return (float) Math.pow(Math.max(0.0f, 1.0f - progress), 2.0); }

    @Override
    public void drawTrailVertices(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Servant servant, PathNode visualRenderNode, List<TrailNode> smoothNodes) {
        if (trailTimer > 0 && !smoothNodes.isEmpty()) {
            IConeTrailRenderer.super.drawTrailVertices(poseStack, bufferSource, partialTick, servant, visualRenderNode, smoothNodes);
        }
    }

    @Override
    public ServantType<? extends Servant> getType() { return ServantRegister.StardustCell.get(); }
}