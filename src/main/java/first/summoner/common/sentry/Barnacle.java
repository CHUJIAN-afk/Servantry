package first.summoner.common.sentry;

import first.lyra.common.entity.AttachmentEntity;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.projectile.PoisonSaliva;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 藤壶哨兵 - 以正浮力向上漂浮，攻击时喷吐毒唾液。
 * <p>
 * 行为：
 * - 正下方 15 格内有实心方块时，通过动态调整浮力实现阻尼上升
 * - 无实心方块或顶部接触方块时原地飘浮
 * - 距离玩家过远（高度差 64 格或水平 128 格）时消失
 * - 每 0.83 秒向敌人发射毒唾液
 */
public class Barnacle extends MomentumMinion implements IBlockCollision<Barnacle> {

    private static final int ATTACK_COOLDOWN = 17;
    private static final int SEARCH_DISTANCE = 32;

    private int cooldown = ATTACK_COOLDOWN;

    public Barnacle() {
        super();
        setDrag(0.9f);
        setGravity(0);
        setRotationSpeed(0);
    }

    @Override
    public int getSearchDistance() {
        return SEARCH_DISTANCE;
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            Vec3 pos = getPos();
            for (int i = 1; i <= 15; i++) {
                if (owner.level().getBlockState(BlockPos.containing(pos.add(0, -i, 0))).isSolid()) {
                    applyForce(new Vec3(0, (float) (0.04 * (1.0 - i / 15.0)), 0));
                    break;
                }
                if (i == 15) {
                    applyForce(new Vec3(0, 0.04f, 0));
                }
            }
            Vec3 velocity = getVelocity();
            if (Math.abs(velocity.y) < 0.002) {
                setVelocity(new Vec3(velocity.x, 0, velocity.z));
            }
            Vec3 ownerPos = owner.position();
            if (pos.y - ownerPos.y > 64 || Math.sqrt((pos.x - ownerPos.x) * (pos.x - ownerPos.x) + (pos.z - ownerPos.z) * (pos.z - ownerPos.z)) > 128) {
                setRemove();
            }
        }
        if (--cooldown <= 0 && isTarget(getTarget())) {
            cooldown = ATTACK_COOLDOWN;
            LivingEntity target = getTarget();
            Vec3 targetCenter = target.getBoundingBox().getCenter();
            Vec3 direction = targetCenter.subtract(getPos()).normalize();
            PoisonSaliva saliva = new PoisonSaliva(getDamageSource(), getPos(), direction.scale(2));
            saliva.join(owner);
            // 向前喷射绿色唾液粒子（粒子量/速度/阻力均高于激光眼）
            ParticleHelper.create(owner.level())
                    .generic(GenericParticleBuilder.create()
                            .centerColor(0x59ff3a)
                            .edgeColor(0x1ecf0a)
                            .lifetime(8)
                            .lifetimeRandom(6)
                            .spin(0.15f)
                            .spinRandom(0.1F)
                            .friction(0.75F)
                            .scale(0.03f)
                            .scaleRandom(0.01f)
                    )
                    .pos(getPos())
                    .offset(0.15)
                    .velocity(direction)
                    .count(16)
                    .speed(1.2)
                    .spread(0.25)
                    .emit();
        }
        super.tick();
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.25, -0.25, -0.25, 0.25, 0.25, 0.25);
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        // 顶部接触方块时停止移动（落地同理）
        if (context.collisionY()) {
            Vec3 velocity = getVelocity();
            setVelocity(new Vec3(velocity.x, 0, velocity.z));
        }
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return SummonerAttachmentEntityRegister.BARNACLE.get();
    }
}
