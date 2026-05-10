package first.servantry.common.servant;

import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.projectile.StardustProjectile;
import first.servantry.common.servant.goal.StardustCellAttackGoal;
import first.servantry.common.servant.goal.StardustCellIdleGoal;
import first.servantry.common.servant.goal.StardustCellTeleportGoal;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ParticleRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * 星尘细胞仆从 - 追踪敌人并发射星细胞射弹。
 * <p>
 * 特性：
 * <ul>
 *   <li>瞬移攻击：瞬移到目标附近发射射弹</li>
 *   <li>玩家攻击联动：玩家攻击时33%概率额外发射射弹</li>
 *   <li>持续旋转：渲染时恒定速度旋转</li>
 * </ul>
 * </p>
 */
public class StardustCell extends MomentumServant implements IBlockCollision<StardustCell> {

    // ===================== 渲染状态 =====================
    private float renderYaw = 0f, renderPitch = 0f, renderRoll = 0f;
    private float renderYawO = 0f, renderPitchO = 0f, renderRollO = 0f;

    /** 拖尾计时器 */
    public int trailTimer = 0;

    /** 基础射击冷却 */
    private int shootCooldown = 0;

    /** 玩家攻击联动冷却 */
    private int extraShootCooldown = 0;

    // ===================== 瞬移状态 =====================
    private int teleportTimer = 0;
    private Vec3 teleportStart, teleportTarget;

    public StardustCell() {
        super();
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setVelocity(IBlockCollision.clearVelocity(getVelocity(), context));
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new StardustCellTeleportGoal(this));
        goalSelector.addGoal(1, new StardustCellAttackGoal(this));
        goalSelector.addGoal(2, new StardustCellIdleGoal(this));
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeInt(trailTimer);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        trailTimer = buf.readInt();
    }

    @Override
    public int getTargetDistance() {
        return 24;
    }

    @Override
    public void tick() {
        if (!getOwner().level().isClientSide()) {
            // 冷却衰减
            if (shootCooldown > 0) shootCooldown--;
            if (extraShootCooldown > 0) extraShootCooldown--;
            if (trailTimer > 0) trailTimer--;
        } else {
            // 客户端：旋转动画
            renderYawO = renderYaw;
            renderPitchO = renderPitch;
            renderRollO = renderRoll;
            renderYaw += 2f;
            renderPitch += 2f;
            renderRoll += 2f;
        }
        super.tick();
    }

    /**
     * 计算光环锚点位置（用于攻击目标周围环绕）。
     */
    public Vec3 getHaloAnchorPos(Player owner, LivingEntity target, int order) {
        long seed = target.getId() * 31337L + order * 1021L;
        Random rand = new Random(seed);
        double baseTheta = rand.nextDouble() * Math.PI * 2;
        double phi = Math.acos(1.0 - rand.nextDouble() * 1.4);
        double radius = 3.5 + rand.nextDouble() * 2.0 + order * 0.15;
        double rotationSpeed = (rand.nextDouble() * 0.02 + 0.01) * (rand.nextBoolean() ? 1 : -1);
        double currentTheta = baseTheta + owner.tickCount * rotationSpeed;

        double offsetX = radius * Math.sin(phi) * Math.cos(currentTheta);
        double offsetY = radius * Math.cos(phi) + Math.sin(owner.tickCount * 0.05 + rand.nextDouble() * Math.PI) * 0.5;
        double offsetZ = radius * Math.sin(phi) * Math.sin(currentTheta);

        Vec3 targetCenter = target.position().add(0, target.getBbHeight() / 2.0, 0);
        return targetCenter.add(offsetX, offsetY, offsetZ);
    }

    public static void spawnShootParticles(ServerLevel level, Vec3 pos, Vec3 direction) {
        RandomSource rand = level.getRandom();
        int particleCount = 2 + rand.nextInt(4);

        for (int i = 0; i < particleCount; i++) {
            double spreadAngle = (rand.nextDouble() - 0.5) * 0.8;
            double rollAngle = rand.nextDouble() * Math.PI * 2.0;
            double speed = 0.15 + rand.nextDouble() * 0.25;

            // 基于前进方向散射
            double vx = direction.x * speed + Math.cos(rollAngle) * spreadAngle * 0.1;
            double vy = direction.y * speed + Math.sin(rollAngle) * spreadAngle * 0.1;
            double vz = direction.z * speed + rand.nextDouble() * 0.1;

            level.sendParticles(
                    ParticleRegister.StardustScatter.get(),
                    pos.x, pos.y, pos.z,
                    0,
                    vx, vy, vz,
                    1.0
            );
        }
    }

    public void shootAtTarget(LivingEntity target) {
        Vec3 start = getPos();
        // 创建并发射星细胞射弹
        StardustProjectile projectile = new StardustProjectile(getDamageSource(), start);
        projectile.setTarget(target);
        projectile.life = 10;
        owner.getData(AttachmentRegister.EntityData).addProjectile(projectile);
        // 后坐力
        Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
        spawnShootParticles((ServerLevel) owner.level(), start, direction);
        applyForce(direction.scale(-0.5));
    }

    // ===================== 渲染数据 =====================

    public float getRenderYaw(float partialTick) { return Mth.lerp(partialTick, renderYawO, renderYaw); }
    public float getRenderPitch(float partialTick) { return Mth.lerp(partialTick, renderPitchO, renderPitch); }
    public float getRenderRoll(float partialTick) { return Mth.lerp(partialTick, renderRollO, renderRoll); }

    // ===================== 属性 =====================

    @Override
    public float getDamage() { return 6f; }

    @Override
    public float getKnockback() { return 0.2f; }

    @Override
    public AttachmentEntityType<? extends MomentumServant> getType() {
        return AttachmentEntityRegister.StardustCell.get();
    }

    // ===================== 访问器 =====================

    public int getShootCooldown() { return shootCooldown; }
    public void setShootCooldown(int cooldown) { this.shootCooldown = cooldown; }

    public int getExtraShootCooldown() { return extraShootCooldown; }
    public void setExtraShootCooldown(int cooldown) { this.extraShootCooldown = cooldown; }

    public int getTeleportTimer() { return teleportTimer; }
    public void setTeleportTimer(int timer) { this.teleportTimer = timer; }

    public Vec3 getTeleportStart() { return teleportStart; }
    public void setTeleportStart(Vec3 pos) { this.teleportStart = pos; }

    public Vec3 getTeleportTarget() { return teleportTarget; }
    public void setTeleportTarget(Vec3 pos) { this.teleportTarget = pos; }

    public void setTrailTimer(int timer) { this.trailTimer = timer; }

}