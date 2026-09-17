package first.summoner.common.minion.sentry;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.PathNode;
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
        super(SummonerAttachmentEntityRegister.BARNACLE);
        setGravity(0);
    }

    @Override
    public int getSearchDistance() {
        return SEARCH_DISTANCE;
    }

    @Override
    public boolean isAlive() {
        if (!super.isAlive()) {
            return false;
        }
        Vec3 pos = getPos();
        Vec3 ownerPos = owner.position();
        double horizontalDistanceSqr = (pos.x - ownerPos.x) * (pos.x - ownerPos.x) + (pos.z - ownerPos.z) * (pos.z - ownerPos.z);
        return pos.y - ownerPos.y <= 64 && horizontalDistanceSqr <= 128 * 128;
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 pos = getPos();
        for (int i = 1; i <= 15; i++) {
            BlockPos supportPos = BlockPos.containing(pos.add(0, -i, 0));
            if (getLevel().getBlockState(supportPos).isCollisionShapeFullBlock(getLevel(), supportPos)) {
                addVelocity(new Vec3(0, (float) (0.04 * (1.0 - i / 15.0)), 0));
                break;
            }
            if (i == 15) {
                addVelocity(new Vec3(0, 0.04f, 0));
            }
        }
        Vec3 velocity = getVelocity();
        if (Math.abs(velocity.y) < 0.002) {
            setVelocity(new Vec3(velocity.x, 0, velocity.z));
        }
        if (--cooldown <= 0 && getTarget() != null) {
            cooldown = ATTACK_COOLDOWN;
            LivingEntity target = getTarget();
            Vec3 targetCenter = target.getBoundingBox().getCenter();
            Vec3 direction = targetCenter.subtract(getPos()).normalize();
            PoisonSaliva saliva = new PoisonSaliva();
            saliva.setOwner(owner);
            saliva.init(new PathNode(getPos(), direction));
            saliva.copyAttributes(this);
            saliva.setVelocity(direction.scale(2));
            LyraHelper.get(owner).add(saliva);
            // 向前喷射绿色唾液粒子（粒子量/速度/阻力均高于激光眼）
            ParticleHelper.create(getLevel())
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
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.25, -0.25, -0.25, 0.25, 0.25, 0.25);
    }
}
