package first.summoner.common.minion.sentry;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.common.sound.Playable;
import first.summoner.common.projectile.CrossbowBolt;
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
    public boolean isAlive() {
        return super.isAlive() && getPos().distanceToSqr(owner.position()) <= 128 * 128;
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = getTarget();
        cooldown--;
        if (isTargetChange()) {
            aiming = 0;
        }
        if (target != null) {
            lookAtPos(target.getBoundingBox().getCenter());
            aiming++;
            if (cooldown < 0 && aiming > 10) {
                cooldown = 53;
                if (owner.hasEffect(SummonerMobEffectRegister.BallistaPanicked)) {
                    cooldown -= 13;
                }
                fire();
            }
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
        projectile.setOwner(owner);
        projectile.init(new PathNode(start, direction));
        projectile.copyAttributes(this);
        projectile.setVelocity(direction.scale(2));
        LyraHelper.get(owner).add(projectile);
        Playable.play(SummonerSoundRegister.BallistaShot, getLevel(), start, getSoundSource());
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
    }
}
