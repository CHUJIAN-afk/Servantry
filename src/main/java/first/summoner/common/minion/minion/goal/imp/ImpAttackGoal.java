package first.summoner.common.minion.minion.goal.imp;


import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.sound.Playable;
import first.summoner.common.minion.minion.Imp;
import first.summoner.common.projectile.ImpFireball;
import first.summoner.register.SummonerSoundRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ImpAttackGoal extends AttachmentEntityGoal<Imp> {

    private int cooldown = 0;
    private Vec3 offset = Vec3.ZERO;

    public ImpAttackGoal(Imp minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() != null;
    }

    @Override
    public void start() {
        LivingEntity target = minion.getTarget();
        Vec3 minionPos = minion.getPos().offsetRandom(minion.getRandom(), 1f);
        Vec3 targetPos = target.getBoundingBox().getCenter();
        offset = minionPos.subtract(targetPos).normalize().scale(4);
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        Vec3 minionPos = minion.getPos();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 wanderPos = targetPos.add(offset);
        if (minionPos.distanceTo(wanderPos) > 0.5) {
            minion.addVelocity(wanderPos.subtract(minionPos).normalize().scale(0.04));
        } else {
            if (minion.getVelocity().length() < 0.1) {
                start();
            }
        }
        minion.lookAtPos(targetPos);

        if (--cooldown <= 0) {
            cooldown = 20 + minion.getRandom().nextIntBetweenInclusive(0, 6);

            Vec3 direction = targetPos.subtract(minionPos).normalize();
            ImpFireball fireball = new ImpFireball();
            fireball.setOwner(minion.getOwner());
            fireball.init(new PathNode(minionPos, direction));
            fireball.copyAttributes(minion);
            fireball.setVelocity(direction);
            LyraHelper.get(minion.getOwner()).add(fireball);
            Playable.play(SummonerSoundRegister.ImpFire, minion.getLevel(), minionPos, minion.getSoundSource());
        }
    }
}
