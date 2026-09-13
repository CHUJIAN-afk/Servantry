package first.summoner.common.master.goal.doomsdayStar;

import first.lyra.common.entity.AttachmentEntityDamageSource;
import first.lyra.common.entity.PathNode;
import first.lyra.register.LyraDamageRegister;
import first.summoner.common.master.DoomsdayStar;
import first.summoner.common.projectile.StarlightShockwave;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public class DoomsdayStarSummonGoal extends DoomsdayStarGoal {

    private boolean landed;
    private int jumpDelay;

    public DoomsdayStarSummonGoal(DoomsdayStar.GoalState goalState, DoomsdayStar doomsdayStar) {
        super(goalState, doomsdayStar);
    }

    @Override
    public void start() {
        landed = false;
        jumpDelay = 40;
        doomsdayStar.setOnGround(false);
        doomsdayStar.setSummonBeamVisible(true);
    }

    @Override
    public void tick() {
        if (!doomsdayStar.level().isClientSide()) {
            if (!landed) {
                doomsdayStar.setVelocity(new Vec3(0, Math.max(doomsdayStar.getVelocity().y() - 0.075, -10), 0));
                if (doomsdayStar.onGround()) {
                    landed = true;
                    doomsdayStar.setSummonBeamVisible(false);
                    Vec3 origin = doomsdayStar.getBoundingBox().getCenter();
                    float damage = (float) doomsdayStar.getAttributeValue(Attributes.ATTACK_DAMAGE);

                    for (int i = 0; i < 36; i++) {
                        double angle = Math.TAU * i / 36;
                        Vec3 direction = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
                        StarlightShockwave shockwave = new StarlightShockwave();
                        shockwave.setCurrentPathNode(new PathNode(origin, (float) Math.toDegrees(Math.atan2(-direction.x, direction.z)), 0.0F, 0.0F));
                        shockwave.setVelocity(direction.scale(0.4));
                        shockwave.setDamage(damage);
                        shockwave.setDamageSourceSupplier((level, player) -> new AttachmentEntityDamageSource(LyraDamageRegister.getDamageTypeHolder(LyraDamageRegister.Summon, level), doomsdayStar, doomsdayStar, shockwave.getPos(), shockwave));
                        shockwave.join(doomsdayStar.level());
                    }
                }
            } else if (--jumpDelay <= 0) {
                doomsdayStar.setGoalState(DoomsdayStar.GoalState.JUMP);
            }
        }
    }

    @Override
    public void stop() {
        doomsdayStar.setSummonBeamVisible(false);
    }
}
