package first.servantry.common.sentryServant;

import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.servant.MomentumServant;
import first.servantry.common.projectile.BlitzBall;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.register.SoundRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 超级电能机枪豌豆射手哨兵。
 * <p>
 * 索敌逻辑与 {@link Ballista} 一致；开火发射雷电球（{@link BlitzBall}）群。
 * 落地时清空向下积累的加速度，避免重力下坠堆积。
 * </p>
 */
public class SuperPeashooter extends MomentumServant implements IBlockCollision<SuperPeashooter> {

    private int level = 1;
    private int aiming = 0;
    private int cooldown = 0;
    /** 当前射击任务，运行中非 null，结束后自动置 null。射击期间不推进冷却 */
    private ShootTask task = null;

    public SuperPeashooter() {
        setGravity(-0.05f);
        setRotationSpeed(18f);
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            if (getPos().distanceToSqr(owner.position()) > 128 * 128) {
                setRemove();
            }
            if (isTargetChange()) {
                aiming = 0;
            }
            if (task == null) {
                cooldown--;
            }
            LivingEntity target = getTarget();
            if (isTarget(target)) {
                aiming++;
                AABB box = target.getBoundingBox();
                lookAtPos(box.getCenter());
                if (task != null) {
                    if (task.tick(this)) {
                        Vec3 pos = getPos();
                        Level level = owner.level();
                        level.playSound(null, pos.x(), pos.y(), pos.z(), SoundRegister.BallistaShot.get(), owner.getSoundSource());
                    } else {
                        task = null;
                    }
                } else if (cooldown < 0 && aiming > 10) {
                    RandomSource random = owner.getRandom();
                    cooldown = 20 + random.nextInt(2);
                    if (random.nextInt(10) == 0) {
                        task = new ShootTask(225, 60, true);
                    } else {
                        task = new ShootTask(7, 7, false);
                    }
                }
            }
        }
        super.tick();
    }

    // 落地清零向下加速度，防止重力持续堆积
    @Override
    public void onBlockCollision(CollisionContext context) {
        if (context.bottomSupported()) {
            Vec3 v = getVelocity();
            setVelocity(new Vec3(v.x(), 0, v.z()));
        }
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeInt(level);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        level = buf.readInt();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return AttachmentEntityRegister.SuperPeashooter.get();
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
    }

    /**
     * 射击任务：在 totalTicks 内把 totalShots 发雷电球尽量均分到每个 tick 发射。
     * <p>
     * 用累积误差法（Bresenham 式）分配每 tick 发射数：每 tick 累加 totalShots，
     * 每当累积量达到 totalTicks 就发射一发并扣除 totalTicks，保证总发射数精确且分布均匀。
     * 任务完成后返回 false，由宿主置引用为 null。
     * </p>
     */
    public static class ShootTask {

        private final int totalShots;
        private final int totalTicks;
        private final boolean scatter;
        private int elapsedTicks = 0;
        private int firedShots = 0;

        public ShootTask(int totalShots, int totalTicks, boolean scatter) {
            this.totalShots = Math.max(0, totalShots);
            this.totalTicks = Math.max(1, totalTicks);
            this.scatter = scatter;
        }

        /**
         * 每 tick 推进一次，发射本 tick 应发雷电球。
         *
         * @param shooter 宿主哨兵
         * @return true=进行中，false=已完成（宿主应置引用为 null）
         */
        public boolean tick(SuperPeashooter shooter) {
            if (firedShots >= totalShots) {
                return false;
            }
            // 累积误差均分：本 tick 应发射数 = floor((firedShots + 1) * totalShots / totalTicks) - firedShots
            // 但为支持每 tick 多发（totalShots >> totalTicks），用 while 循环扣除
            int targetFired = (int) (((long) (elapsedTicks + 1) * totalShots) / totalTicks);
            int toFire = Math.max(0, targetFired - firedShots);

            Vec3 direction = shooter.getLookAngle();
            Vec3 pos = shooter.getPos();
            for (int i = 0; i < toFire && firedShots < totalShots; i++) {
                Vec3 dir = scatter ? scatterDirection(direction, shooter) : direction;
                // 沿朝向错位起点，避免同 tick 多发重叠
                Vec3 start = pos.add(dir.scale(i * 0.25));
                BlitzBall projectile = new BlitzBall(shooter.getDamageSource(), start, dir);
                projectile.copyDamageData(shooter);
                projectile.setVelocity(dir.scale(0.5));
                projectile.join(shooter.owner);
                firedShots++;
            }
            elapsedTicks++;
            return elapsedTicks < totalTicks && firedShots < totalShots; // 完成
        }

        // 散射方向：在朝向基础上叠加小幅随机偏转
        private static Vec3 scatterDirection(Vec3 direction, SuperPeashooter shooter) {
            RandomSource random = shooter.owner.getRandom();
            double spread = 0.15;
            Vec3 perp = new Vec3(-direction.z, 0, direction.x);
            if (perp.lengthSqr() < 1e-6) {
                perp = new Vec3(0, 1, 0);
            } else {
                perp = perp.normalize();
            }
            Vec3 up = direction.cross(perp).normalize();
            double offA = (random.nextDouble() * 2 - 1) * spread;
            double offB = (random.nextDouble() * 2 - 1) * spread;
            return direction.add(perp.scale(offA)).add(up.scale(offB)).normalize();
        }
    }
}
