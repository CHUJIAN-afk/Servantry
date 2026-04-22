package first.servantry.common.servent;

import first.servantry.api.ai.ServantGoal;
import first.servantry.api.ai.ServantGoalSelector;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ParticleRegister;
import first.servantry.register.ServantRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class StardustCell extends MomentumServant {

    // 客户端表现状态
    public int trailTimer = 0;
    private float renderYaw = 0f, renderPitch = 0f, renderRoll = 0f;
    private float renderYawO = 0f, renderPitchO = 0f, renderRollO = 0f;

    // 攻击冷却
    private int shootCooldown = 0;

    // 瞬移相关
    private int teleportTimer = 0;
    private Vec3 teleportStart, teleportTarget;

    public StardustCell() {
        super();
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new TeleportGoal(this));
        goalSelector.addGoal(1, new AttackGoal(this));
        goalSelector.addGoal(2, new IdleGoal(this));
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeInt(trailTimer);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        trailTimer = buf.readInt();
    }

    // ---------- 服务端 tick ----------
    @Override
    public void tick() {
        super.tick();
        Player owner = getOwner();
        if (!owner.level().isClientSide()) {
            if (shootCooldown > 0) {
                shootCooldown--;
            }
            if (trailTimer > 0) {
                trailTimer--;
            }
        } else {
            renderYawO = renderYaw;
            renderPitchO = renderPitch;
            renderRollO = renderRoll;
            renderYaw += 2f;
            renderPitch += 2f;
            renderRoll += 2f;
        }
    }

    public Vec3 getHaloAnchorPos(Player owner, LivingEntity target, int order) {
        long seed = target.getId() * 31337L + order * 1021L;
        Random rand = new Random(seed);
        double baseTheta = rand.nextDouble() * Math.PI * 2.0;
        double phi = Math.acos(1.0 - rand.nextDouble() * 1.4);
        double radius = 3.5 + rand.nextDouble() * 2.0 + (order * 0.15);
        double rotationSpeed = (rand.nextDouble() * 0.02 + 0.01) * (rand.nextBoolean() ? 1 : -1);
        double currentTheta = baseTheta + owner.tickCount * rotationSpeed;

        double offsetX = radius * Math.sin(phi) * Math.cos(currentTheta);
        double offsetY = radius * Math.cos(phi) + Math.sin(owner.tickCount * 0.05 + rand.nextDouble() * Math.PI) * 0.5;
        double offsetZ = radius * Math.sin(phi) * Math.sin(currentTheta);

        Vec3 targetCenter = target.position().add(0, target.getBbHeight() / 2.0, 0);
        return targetCenter.add(offsetX, offsetY, offsetZ);
    }

    public void shootAtTarget(LivingEntity target) {

        Vec3 start = getPos();
        Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();

        // 生成箭矢（替代原 StardustLaser）
        Arrow arrow = new Arrow(EntityType.ARROW, getOwner().level());
        arrow.setPos(start);
        arrow.setOwner(getOwner());
        arrow.shoot(direction.x, direction.y, direction.z, 5f, 1.0f);
        arrow.setBaseDamage(getDamage());
        getOwner().level().addFreshEntity(arrow);


            // ================= 【客户端专属】自定义粒子生成 =================
            RandomSource rand = getOwner().getRandom();
            int particleCount = 2 + rand.nextInt(2);

            // 构建正交基准面，用于计算精准的锥形散射
            Vec3 upDir = new Vec3(0, 1, 0);
            if (Math.abs(direction.y) > 0.99) upDir = new Vec3(1, 0, 0);
            Vec3 rightDir = direction.cross(upDir).normalize();
            Vec3 trueUpDir = rightDir.cross(direction).normalize();

            for (int i = 0; i < particleCount; i++) {
                double spreadAngle = (rand.nextDouble() * 35.0) * (Math.PI / 180.0);
                double rollAngle = rand.nextDouble() * Math.PI * 2.0;

                double dx = Math.sin(spreadAngle) * Math.cos(rollAngle);
                double dy = Math.sin(spreadAngle) * Math.sin(rollAngle);
                double dz = Math.cos(spreadAngle);

                Vec3 particleDir = rightDir.scale(dx)
                        .add(trueUpDir.scale(dy))
                        .add(direction.scale(dz))
                        .normalize();

                double speedMultiplier = 0.15 + rand.nextDouble() * 0.15;
                Vec3 particleVel = particleDir.scale(speedMultiplier);

                // 直接在客户端本地添加我们刚注册的自定义粒子
                ((ServerLevel) getOwner().level()).sendParticles(
                        ParticleRegister.StardustScatter.get(),
                        start.x, start.y, start.z,
                        0,             // count=0 启用速度矢量模式
                        particleVel.x,
                        particleVel.y,
                        particleVel.z,
                        1.0            // 速度标量
                );
            }

        // 后坐力
        applyForce(direction.scale(-0.5));
    }

    // ---------- 渲染数据访问 ----------
    public float getRenderYaw(float partialTick) {
        return Mth.lerp(partialTick, renderYawO, renderYaw);
    }

    public float getRenderPitch(float partialTick) {
        return Mth.lerp(partialTick, renderPitchO, renderPitch);
    }

    public float getRenderRoll(float partialTick) {
        return Mth.lerp(partialTick, renderRollO, renderRoll);
    }

    @Override
    public float getDamage() {
        return 6f;
    }

    @Override
    public float getKnockback() { return 0.2f; }

    @Override
    public ServantType<? extends Servant> getType() {
        return ServantRegister.StardustCell.get();
    }

    // ---------- Goal 定义 ----------

    /** 空闲游荡 Goal */
    static class IdleGoal extends ServantGoal<StardustCell> {
        private Vec3 wanderOffset = Vec3.ZERO;

        public IdleGoal(StardustCell servant) { super(servant); }

        @Override
        public boolean canUse() {
            return servant.getTarget() == null && servant.teleportTimer <= 0;
        }

        @Override
        public void tick() {
            Player owner = servant.getOwner();

            if (wanderOffset.equals(Vec3.ZERO) || owner.getRandom().nextDouble() < 0.025 || wanderOffset.distanceToSqr(servant.getPos()) < 1) {
                wanderOffset = new Vec3((owner.getRandom().nextDouble() - 0.5) * 8, owner.getRandom().nextDouble() * 3 + 2, (owner.getRandom().nextDouble() - 0.5) * 8);
            }

            Vec3 targetPos = owner.position().add(wanderOffset);
            Vec3 dir = targetPos.subtract(servant.getPos());
            double dist = dir.length();
            if (dist > 0.05) {
                dir = dir.normalize();
                double force = Math.min(dist * 0.01, 0.1);
                servant.applyForce(dir.scale(force));
            }

            // 缓慢朝向运动方向
            if (servant.getVelocity().lengthSqr() > 0.01) {
                Vec3 vel = servant.getVelocity().normalize();
                servant.desiredYaw = (float) Math.toDegrees(Math.atan2(-vel.x, vel.z));
            }
        }
    }

    /** 攻击 Goal */
    static class AttackGoal extends ServantGoal<StardustCell> {
        public AttackGoal(StardustCell servant) { super(servant); }

        @Override
        public boolean canUse() {
            LivingEntity target = servant.getTarget();
            return target != null && servant.isTarget(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = servant.getTarget();
            return target != null && servant.isTarget(target);
        }

        @Override
        public void tick() {
            LivingEntity target = servant.getTarget();

            Player owner = servant.getOwner();
            int order = owner.getData(AttachmentRegister.ServantData).getOrder(servant);
            Vec3 anchor = servant.getHaloAnchorPos(owner, target, order);

            Vec3 toAnchor = anchor.subtract(servant.getPos());
            double dist = toAnchor.length();
            if (dist > 0.05) {
                double force = Math.min(dist * 0.08, 0.4);
                servant.applyForce(toAnchor.normalize().scale(force));
            }

            // 动态摩擦与限速
            float friction = dist < 1.5 ? 0.55f : 0.85f;
            float maxSpd = (float) Math.min(1.8, dist * 0.8 + 0.05);
            Vec3 vel = servant.getVelocity();
            if (vel.lengthSqr() > maxSpd * maxSpd) vel = vel.normalize().scale(maxSpd);
            servant.setVelocity(vel.scale(friction));

            // 朝向目标
            Vec3 faceDir = target.position().subtract(servant.getPos()).normalize();
            servant.desiredYaw = (float) Math.toDegrees(Math.atan2(-faceDir.x, faceDir.z));

            // 射击冷却
            if (servant.shootCooldown <= 0) {
                servant.shootAtTarget(target);
                servant.shootCooldown = 12 + owner.getRandom().nextInt(4);
            }
        }

        @Override
        public void stop() {
            servant.setTarget(null);
        }
    }

    /** 瞬移 Goal */
    static class TeleportGoal extends ServantGoal<StardustCell> {
        private static final int TELEPORT_DURATION = 6;

        public TeleportGoal(StardustCell servant) { super(servant); }

        @Override
        public boolean canUse() {
            if (servant.teleportTimer > 0) return true; // 正在执行
            LivingEntity target = servant.getTarget();
            if (target == null) return false;

            double distSqr = target.distanceToSqr(servant.getPos());
            boolean shouldTeleport = (distSqr >= 28.75 * 28.75 && distSqr <= 53.75 * 53.75) || distSqr > 128 * 128;
            if (!shouldTeleport) return false;

            // 初始化瞬移
            servant.teleportTimer = TELEPORT_DURATION;
            servant.teleportStart = servant.getPos();
            servant.teleportTarget = target.getBoundingBox().getCenter();
            servant.trailTimer = 15; // 客户端拖尾特效
            servant.setTarget(target);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return servant.teleportTimer > 0;
        }

        @Override
        public void tick() {
            servant.teleportTimer--;
            float progress = 1.0f - (float) servant.teleportTimer / TELEPORT_DURATION;
            float ease = 1.0f - (float) Math.pow(1.0f - progress, 3);
            Vec3 currentPos = servant.teleportStart.lerp(servant.teleportTarget, ease);
            servant.setPath(Collections.singletonList(new PathNode(currentPos, servant.getYaw(), servant.getPitch(), servant.getRoll())));

            if (servant.teleportTimer == 0) {
                LivingEntity target = servant.getTarget();
                if (target != null) {
                    int inv = target.invulnerableTime;
                    target.invulnerableTime = 0;
                    target.hurt(servant.getDamageSource(), servant.getDamage());
                    target.invulnerableTime = inv;
                }
                servant.teleportTimer = 0;
            }
        }

        @Override
        public void stop() {
            servant.teleportTimer = 0;
        }
    }

}