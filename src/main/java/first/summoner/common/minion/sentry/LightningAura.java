package first.summoner.common.minion.sentry;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachment.TargetCache;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.entity.SyncFieldDispatcher;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LightningAura extends MomentumMinion implements IBlockCollision<LightningAura> {

    private int level = 1;
    private int cooldown = 0;

    public LightningAura() {
        super(SummonerAttachmentEntityRegister.LIGHTNING_AURA);
        setGravity(-0.05f);
        setRotationSpeed(18f);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ByteBufCodecs.INT, this::getSentryLevel, this::setSentryLevel);
    }

    @Override
    public int getSearchDistance() {
        return 0;
    }

    @Override
    public void tick() {
        if (!getLevel().isClientSide()) {
            if (owner != null && getPos().distanceToSqr(owner.position()) > 128 * 128) {
                setRemove();
            }
            spawnParticles();
            cooldown--;
            if (cooldown < 0) {
                cooldown = 10;
                attack();
            }
        }
        super.tick();
    }

    /**
     * 对 4 格半径内的所有敌人造成面板攻击力 × 等级倍率的召唤伤害。
     * 击退/护甲穿透由 MinionDamageSource 统一应用（击退 0.25、穿透 1000）。
     */
    private void attack() {
        TargetCache targetCache = LyraHelper.get(getLevel()).getTargetCache();
        List<LivingEntity> entities = targetCache.getEntitiesInRadius(getPos(), 6, null);
        for (LivingEntity entity : entities) {
            InvincibleData.attack(entity)
                    .damageSource(getDamageSource())
                    .damageAmount(getDamage())
                    .apply();
        }
    }

    /**
     * 每 tick 在半径 4 格的球面上生成 10 个位置不同的闪电色粒子，无速度，约 0.5 秒消散。
     */
    private void spawnParticles() {
        RandomSource random = getRandom();
        Vec3 center = getPos();
        for (int i = 0; i < 10; i++) {
            Vec3 dir = new Vec3(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).normalize();
            Vec3 pos = center.add(dir.scale(6));
            ParticleHelper.create(getLevel())
                    .generic(GenericParticleBuilder.create()
                            .centerColor(0xcfeaff)
                            .edgeColor(0x5aa7ff)
                            .lifetime(10)
                            .lifetimeRandom(4)
                            .friction(0.9f)
                            .scale(0.03f)
                            .scaleRandom(0.01f)
                    )
                    .pos(pos)
                    .count(0)
                    .velocity(0, 0, 0)
                    .emit();
        }
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        if (context.bottomSupported()) {
            Vec3 v = getVelocity();
            setVelocity(new Vec3(v.x(), 0, v.z()));
        }
    }

        public void dimensionChange() {
        setRemove();
    }

    public int getSentryLevel() {
        return level;
    }

    public void setSentryLevel(int level) {
        this.level = level;
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
    }
}
