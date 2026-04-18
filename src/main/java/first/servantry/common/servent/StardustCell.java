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
import first.servantry.common.attachment.LevelProjectileData;
import first.servantry.common.attachment.ServantData;
import first.servantry.common.projectile.StardustLaser;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ItemRegister;
import first.servantry.register.ServantRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class StardustCell extends Servant implements IConeTrailRenderer, IMomentumControlled {

    public int trailTimer = 0;
    public LivingEntity currentTarget = null;

    // 接口要求的动量状态
    private Vec3 velocity = Vec3.ZERO;
    private float maxSpeed = 1;
    private float friction = 0.85f;

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
    public void setFriction(float friction) {
        this.friction = friction;
    }
    @Override
    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
    @Override
    public float getMaxSpeed() {
        return maxSpeed ;
    }

    @Override public float getFriction() { return friction; }

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

        renderYaw += 2f;
        renderPitch += 2f;
        renderRoll += 2f;
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
        int order = data.getOrder(this);
        potentialTargets.sort(Comparator.comparingDouble(e -> {
            double distSqr = e.distanceToSqr(getPos());
            double score = distSqr;
            if (e.distanceToSqr(owner) < 36.0) score -= 10000.0;
            if (e.getId() == targetId) score -= 1000.0;
            int hashBias = (e.getId() * 31 + order * 17) % 5;
            score += hashBias * 40.0;
            return score;
        }));
        return potentialTargets.getFirst();
    }

    @Override
    public void onPathNodeConsumed(PathNode node) {
        super.onPathNodeConsumed(node);
        if ("fire".equals(node.feature()) && currentTarget != null && currentTarget.isAlive()) {
            fireCellProjectile(currentTarget);
        }
    }

    public void fireCellProjectile(LivingEntity target) {
        Player owner = getOwner();
        if (owner == null || target == null) return;

        Vec3 startPos = this.getPos();
        Vec3 targetCenter = target.getBoundingBox().getCenter();
        Vec3 baseShootDir = targetCenter.subtract(startPos);

        if (baseShootDir.lengthSqr() > 1e-4) {
            baseShootDir = baseShootDir.normalize();
        } else {
            baseShootDir = new Vec3(0, -1, 0);
        }

        // 计算直指目标的基准 Yaw 和 Pitch
        float baseYaw = (float) (Math.atan2(-baseShootDir.x, baseShootDir.z) * (180D / Math.PI));
        double horiz = Math.sqrt(baseShootDir.x * baseShootDir.x + baseShootDir.z * baseShootDir.z);
        float basePitch = (float) (Math.atan2(-baseShootDir.y, horiz) * (180D / Math.PI));

        // 随机发射 1 到 3 枚
        int projectileCount = 1 + owner.getRandom().nextInt(3);

        for (int i = 0; i < projectileCount; i++) {
            float scatterYaw = baseYaw + (owner.getRandom().nextFloat() - 0.5f) * 40f;
            float scatterPitch = basePitch + (owner.getRandom().nextFloat() - 0.5f) * 40f;

            // 将叠加了随机偏移的 Yaw 和 Pitch 重新转回三维方向向量
            float f = (float) Math.cos(-scatterYaw * ((float) Math.PI / 180F) - (float) Math.PI);
            float f1 = (float) Math.sin(-scatterYaw * ((float) Math.PI / 180F) - (float) Math.PI);
            float f2 = (float) -Math.cos(-scatterPitch * ((float) Math.PI / 180F));
            float f3 = (float) Math.sin(-scatterPitch * ((float) Math.PI / 180F));
            Vec3 finalShootDir = new Vec3(f1 * f2, f3, f * f2).normalize();

            // 依据新角度生成轨迹起点
            PathNode startNode = new PathNode("", startPos, scatterYaw, scatterPitch, this.getRoll());
            StardustLaser laser = new StardustLaser(owner.level(), owner, startNode);
            laser.setStardustCell(this);
            laser.setTarget(target);

            // 赋予偏转后的初始动量，射弹飞出后会被 tick() 里的追尾逻辑慢慢拉回目标
            laser.setVelocity(finalShootDir.scale(1.0));

            LevelProjectileData data = owner.level().getData(AttachmentRegister.LevelProjectileData);
            data.addProjectile(laser);
        }

        // 细胞自身的后坐力依然基于最开始直指目标的基准方向 baseShootDir
        // 这样能保证细胞在视觉上的后退受力显得稳定且符合物理直觉
        this.applyForce(baseShootDir.scale(-1));
    }

    /**
     * 计算目标周围的有机蜂群/星云驻点
     */
    public Vec3 getHaloAnchorPos(Player owner, LivingEntity target, int order) {
        // 利用目标 ID 和细胞次序生成一个固定的随机种子
        // 这样可以保证同一个细胞打同一个怪时，它的相对位置是稳定的，不会乱闪
        long seed = target.getId() * 31337L + order * 1021L;
        Random rand = new Random(seed);

        // 生成随机的球面坐标
        double baseTheta = rand.nextDouble() * Math.PI * 2.0; // 水平环绕角度 (0 ~ 360度)

        // 限制高度角 (Phi)，让细胞主要分布在目标的上半球及周围，避免它们钻进地里
        // Math.acos 配合 1.0 ~ -0.4 的范围，使其分布在顶部到略低于腰部的位置
        double phi = Math.acos(1.0 - rand.nextDouble() * 1.4);

        // 随机基础半径，并且随着细胞数量 (order) 的增加稍微向外扩散，防止重叠
        double radius = 3.5 + rand.nextDouble() * 2.0 + (order * 0.15);

        // 为每个细胞分配独一无二的缓慢公转速度和方向（顺时针或逆时针）
        double rotationSpeed = (rand.nextDouble() * 0.02 + 0.01) * (rand.nextBoolean() ? 1 : -1);
        double currentTheta = baseTheta + owner.tickCount * rotationSpeed;

        // 球面坐标转笛卡尔坐标 (x, y, z)
        double offsetX = radius * Math.sin(phi) * Math.cos(currentTheta);
        double offsetY = radius * Math.cos(phi);
        double offsetZ = radius * Math.sin(phi) * Math.sin(currentTheta);

        // 增加一点独有相位的上下“呼吸”浮动感 (振幅 0.5 格)
        offsetY += Math.sin(owner.tickCount * 0.05 + rand.nextDouble() * Math.PI) * 0.5;

        // 以目标的中心偏上作为基准原点
        Vec3 targetCenter = target.position().add(0, target.getBbHeight() / 2.0, 0);

        return targetCenter.add(offsetX, offsetY, offsetZ);
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
            if (wanderOffset.equals(Vec3.ZERO) || owner.getRandom().nextDouble() < 0.025 || wanderOffset.distanceToSqr(servant.getPos()) < 4) {
                wanderOffset = new Vec3((owner.getRandom().nextDouble() - 0.5) * 8, owner.getRandom().nextDouble() * 3 + 2, (owner.getRandom().nextDouble() - 0.5) * 8);
            }
            Vec3 targetPos = owner.position().add(wanderOffset);
            Vec3 dir = targetPos.subtract(servant.getPos());
            double dist = dir.length();
            if (dist > 1e-4) dir = dir.normalize();

            double pullForce = Math.min(dist * 0.05, 0.3);
            servant.applyForce(dir.scale(pullForce));

            if (servant instanceof IMomentumControlled iMomentumControlled) {
                float maxSpeed = (float) Math.min(1.3, 0.1 + dist * 0.125);
                float friction = dist < 1.0 ? 0.6f : 0.82f;
                iMomentumControlled.setMaxSpeed(maxSpeed);
                iMomentumControlled.setFriction(friction);
            }
        }

    }

    public static class ActionCellAttack extends ServantAction<StardustCell> {
        private int shootTimer = 12;

        public ActionCellAttack(StardustCell servant) { super(servant); }
        @Override public String getId() { return "attack"; }
        @Override public boolean isAttack() { return true; }

        @Override
        public void tick(LivingEntity target) {
            if (target == null || !target.isAlive()) {
                servant.getAi().forceAction(new ActionCellIdle(servant), null);
                return;
            }

            Player owner = servant.getOwner();
            ServantData data = owner.getData(AttachmentRegister.ServantData);

            int order = data.getOrder(servant);
            Vec3 anchorPos = servant.getHaloAnchorPos(owner, target, order);

            Vec3 toAnchor = anchorPos.subtract(servant.getPos());
            double distToAnchor = toAnchor.length();

            // 【核心修复：临界阻尼防抖科技】
            // 1. 设置拉力上限，防止离得太远时积攒恐怖的加速度
            double pullForce = Math.min(distToAnchor * 0.08, 0.4);
            if (distToAnchor > 0.05) {
                servant.applyForce(toAnchor.normalize().scale(pullForce));
            }

            // 2. 磁悬浮式刹车：越靠近锚点，摩擦力越大（数值越小），且最高速度被强制压缩
            float friction = distToAnchor < 1.5 ? 0.55f : 0.85f;
            float maxSpeed = (float) Math.min(1.8, distToAnchor * 0.8 + 0.05);

            // 执行动量更新
            Vec3 vel = servant.getVelocity();
            if (vel.lengthSqr() > maxSpeed * maxSpeed) vel = vel.normalize().scale(maxSpeed);
            Vec3 nextPos = servant.getPos().add(vel);
            servant.setVelocity(vel.scale(friction));

            shootTimer--;
            if (shootTimer <= 0) {
                // 打上射击特征，在下一刻被消费时触发开火
                servant.setPath(Collections.singletonList(new PathNode("fire", nextPos, servant.getYaw(), servant.getPitch(), servant.getRoll())));
                shootTimer = 12 + owner.getRandom().nextInt(4);
            } else {
                servant.setPath(Collections.singletonList(new PathNode("", nextPos, servant.getYaw(), servant.getPitch(), servant.getRoll())));
            }
        }
        @Override public boolean isFinished() { return false; }
    }

    public static class ActionCellTeleport extends ServantAction<StardustCell> {
        private Vec3 startPos;
        private Vec3 targetPos;
        private int ticks = 0;
        private final int duration = 6;

        public ActionCellTeleport(StardustCell servant) { super(servant); }
        @Override public String getId() { return "teleport"; }
        @Override public boolean isAttack() { return true; }

        @Override
        public void onStart(LivingEntity target) {
            if (target == null) return;
            this.startPos = servant.getPos();

            Player owner = servant.getOwner();
            ServantData data = owner.getData(AttachmentRegister.ServantData);

            // 直接预测抵达时的光环位置
            this.targetPos = target.getBoundingBox().getCenter();
        }

        @Override
        public void tick(LivingEntity target) {
            ticks++;
            if (ticks <= duration) {
                float t = (float) ticks / duration;
                float ease = 1.0f - (float) Math.pow(1.0f - t, 3);
                Vec3 currentPos = this.startPos.lerp(this.targetPos, ease);
                servant.setPath(Collections.singletonList(new PathNode("", currentPos, servant.getYaw(), servant.getPitch(), servant.getRoll())));
            } else {
                if (target != null) {
                    int invulnerableTime = target.invulnerableTime;
                    target.invulnerableTime = 0;
                    target.hurt(servant.getDamageSource(), servant.getBaseDamage());
                    target.invulnerableTime = invulnerableTime;
                }
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
    @Override public float getTrailMaxRadius() { return 0.2f; }
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