package first.servantry.common.servant.goal.deadlysphere;

import first.lyra.common.servant.ServantGoal;
import first.servantry.common.servant.DeadlySphere;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 致命球冲刺攻击Goal，AI与魔焰眼完全一致。
 */
public class DeadlySphereAttackGoal extends ServantGoal<DeadlySphere> {

    private int cooldown;
    private int combat = 0;

    public DeadlySphereAttackGoal(DeadlySphere deadlySphere) {
        super(deadlySphere);
    }

    @Override
    public boolean canUse() {
        return servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 toTarget = targetPos.subtract(servant.getPos());
        Player owner = servant.getOwner();
        Vec3 direction = toTarget.offsetRandom(owner.getRandom(), (float) target.getBoundingBox().getSize()).normalize();
        if (--cooldown <= 0) {
            if (++combat == 3) {
                combat = 0;
                cooldown = 20;
                servant.nextAppearance();
            } else {
                cooldown = 8 + owner.getRandom().nextInt(-2, 2);
            }
            servant.applyForce(direction.scale(3));
            servant.setTrailTimer(10);
        }
    }
}
