package first.summoner.common.minion.sentry;

import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.entity.SyncFieldDispatcher;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.common.sound.Playable;
import first.summoner.common.projectile.CrossbowBolt;
import first.summoner.register.SummonerArmorSetRegister;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.register.SummonerMobEffectRegister;
import first.summoner.register.SummonerSoundRegister;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Ballista extends MomentumMinion implements IBlockCollision<Ballista> {

    private int level = 1;
    private int aiming = 0;
    private int cooldown = 0;

    public Ballista() {
        super(SummonerAttachmentEntityRegister.BALLISTA);
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
        return 32;
    }

    @Override
    public void tick() {
        if (!getLevel().isClientSide()) {
            if (owner != null && getPos().distanceToSqr(owner.position()) > 128 * 128) {
                setRemove();
            }
            LivingEntity target = getTarget();
            cooldown--;
            if (isTargetChange()) {
                aiming = 0;
            }
            if (isTarget(target)) {
                lookAtPos(target.getBoundingBox().getCenter());
                aiming++;
                if (cooldown < 0 && aiming > 10) {
                    cooldown = 53;
                    boolean full = false;
                    boolean hasEffect = false;
                    if (owner != null) {
                        full = SummonerArmorSetRegister.ValhallaKnight.value().full(owner);
                        if (full) {
                            cooldown -= 20;
                        }
                        hasEffect = owner.hasEffect(SummonerMobEffectRegister.BallistaPanicked);
                        if (hasEffect) {
                            cooldown -= 13;
                        }
                        if (full && hasEffect) {
                            cooldown -= 10;
                        }
                    }
                    fire();
                }
            }
        }
        super.tick();
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

    public void fire() {
        Vec3 direction = getLookAngle();
        Vec3 start = getPos();
        CrossbowBolt projectile = new CrossbowBolt();
        projectile.copyDamageSource(this);
        projectile.setPos(start);
        if (owner != null) {
            if (SummonerArmorSetRegister.ValhallaKnight.value().full(owner)) {
                projectile.setMaxPierceCount(6);
                projectile.setDamage(projectile.getDamage() * 1.33f);
                direction = direction.scale(1.33f);
            }
        }
        projectile.setVelocity(direction.scale(2));
        if (owner != null) {
            projectile.join(owner);
            Playable.play(SummonerSoundRegister.BallistaShot, getLevel(), start, owner.getSoundSource());
        } else {
            projectile.join(getLevel());
            Playable.play(SummonerSoundRegister.BallistaShot, getLevel(), start, net.minecraft.sounds.SoundSource.PLAYERS);
        }
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
    }
}
