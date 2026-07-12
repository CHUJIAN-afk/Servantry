package first.servantry.common.servant.goal.twins;

import first.servantry.api.common.particle.GenericParticleBuilder;
import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.projectile.DemonFlameProjectile;
import first.servantry.common.servant.Twins;
import first.servantry.utils.ParticleHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class TwinsCursedFlameAttackGoal extends ServantGoal<Twins> {

    private Vec3 wanderPos = Vec3.ZERO;
    private int cooldown = 0;
    private int emit = 0;
    private boolean emited = false;

    public TwinsCursedFlameAttackGoal(Twins twins) {
        super(twins);
    }

    @Override
    public boolean canUse() {
        return servant.isFlameEye() && servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 toTarget = targetPos.subtract(servant.getPos());
        Player owner = servant.getOwner();
        servant.lookAtPos(targetPos);
        if (!emited) {
            if (--cooldown <= 0) {
                emit += 5;
                if (emit > 40) {
                    emited = true;
                } else {
                    cooldown = 12 + owner.getRandom().nextInt(-2, 2);
                    Vec3 direction = toTarget.offsetRandom(owner.getRandom(), (float) target.getBoundingBox().getSize()).normalize();
                    servant.applyForce(direction.scale(2));
                    servant.setTrailTimer(10);
                }
            }
        } else {
            if (--emit > 0) {
                wanderPos = servant.getWanderPos(wanderPos, targetPos, 4, 1);
                double distance = servant.getPos().distanceTo(wanderPos);
                servant.applyForce(wanderPos.subtract(servant.getPos()).normalize().scale(Math.min(distance * 0.04, 0.4)));
                DemonFlameProjectile demonFlameProjectile = new DemonFlameProjectile(servant.getDamageSource(), servant.getPos(), targetPos.subtract(servant.getPos()).normalize());
                demonFlameProjectile.copyDamageData(servant);
                demonFlameProjectile.join(owner);
                ParticleHelper.create(owner.level())
                        .generic(GenericParticleBuilder.create()
                                .color(0x24d509)
                                .edgeColor(0x1FF109)
                                .colorRandom(0.2f, 0f, 0.2f)
                                .lifetime(5)
                                .lifetimeRandom(15)
                                .spin(0.4f)
                                .spinRandom(0.1F)
                                .friction(0.85F)
                                .scale(0.035f)
                                .scaleRandom(0.005f)
                        )
                        .pos(servant.getPos())
                        .offset(0.025)
                        .velocity(targetPos.subtract(servant.getPos()))
                        .count(4)
                        .speed(1.25)
                        .spread(0.15)
                        .emit();
            } else {
                emited = false;
            }
        }
    }

}