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

    private int cooldown;
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
        Vec3 targetPos = minion.getTarget().getBoundingBox().getCenter();
        Vec3 direction = minion.getPos().offsetRandom(minion.getRandom(), 1.0F).subtract(targetPos);
        offset = direction.lengthSqr() < 1.0E-6 ? Vec3.ZERO : direction.normalize().scale(4);
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 toHover = targetPos.add(offset).subtract(minion.getPos());
        if (toHover.lengthSqr() > 0.25) {
            minion.addVelocity(toHover.normalize().scale(0.04));
        } else if (minion.getVelocity().lengthSqr() < 0.01) {
            start();
        }
        minion.lookAtPos(targetPos);
        if (--cooldown <= 0) {
            cooldown = 20 + minion.getRandom().nextIntBetweenInclusive(0, 6);
            Vec3 direction = targetPos.subtract(minion.getPos()).normalize();
            ImpFireball fireball = new ImpFireball();
            fireball.setOwner(minion.getOwner());
            fireball.init(new PathNode(minion.getPos(), direction));
            fireball.copyAttributes(minion);
            fireball.setVelocity(direction);
            LyraHelper.get(minion.getOwner()).add(fireball);
            minion.attackTime = minion.getTickCount();
            Playable.play(SummonerSoundRegister.ImpFire, minion.getLevel(), minion.getPos(), minion.getSoundSource());
        }
    }
}
