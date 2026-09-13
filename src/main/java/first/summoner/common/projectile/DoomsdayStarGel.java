package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.AttachmentEntityDamageSource;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.entity.PathNode;
import first.lyra.common.projectile.Projectile;
import first.lyra.register.LyraDamageRegister;
import first.summoner.common.master.DoomsdayStar;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DoomsdayStarGel extends Projectile implements IEntityCollision<DoomsdayStarGel> {

    private static final int LAUNCH_DELAY = 5;
    private static final double SPRAY_SPEED = 0.12D;
    private static final double BOOST_SPEED = 0.45D;

    private LivingEntity target;
    private int launchDelay = LAUNCH_DELAY;

    public DoomsdayStarGel() {
        super(SummonerAttachmentEntityRegister.DOOMSDAY_STAR_GEL);
        setDrag(1.0F);
        setGravity(0.0F);
        setMaxLife(100);
    }

    public static void fire(DoomsdayStar doomsdayStar, LivingEntity target) {
        Vec3 origin = doomsdayStar.getAbsoluteHitbox().getCenter();
        int count = doomsdayStar.getRandom().nextInt(6, 11);
        for (int i = 0; i < count; i++) {
            Vec3 direction = Vec3.ZERO.offsetRandom(doomsdayStar.getRandom(), 1);
            DoomsdayStarGel gel = new DoomsdayStarGel();
            gel.setCurrentPathNode(new PathNode(
                    origin,
                    (float) Math.toDegrees(Math.atan2(-direction.x, direction.z)),
                    (float) Math.toDegrees(Math.asin(-direction.y)),
                    0.0F
            ));
            gel.setVelocity(direction.normalize().scale(SPRAY_SPEED));
            gel.setTarget(target);
            gel.setDamage(doomsdayStar.getAttackDamage());
            gel.setDamageSourceSupplier((level, player) -> new AttachmentEntityDamageSource(
                    LyraDamageRegister.getDamageTypeHolder(LyraDamageRegister.Summon, level),
                    doomsdayStar.getHurtEntity(),
                    doomsdayStar.getHurtEntity(),
                    gel.getPos(),
                    gel
            ));
            gel.join(doomsdayStar.getLevel());
        }
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
    }

    @Override
    public void tick() {
        if (!getLevel().isClientSide() && launchDelay > 0) {
            launchDelay--;
            if (launchDelay == 0 && target != null && target.isAlive()) {
                applyForce(target.getBoundingBox().getCenter().subtract(getPos()).normalize().scale(BOOST_SPEED));
                target = null;
            }
        }
        super.tick();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.25D, -0.25D, -0.25D, 0.25D, 0.25D, 0.25D);
    }

    @Override
    public boolean isValidCollisionTarget(DoomsdayStarGel entity, LivingEntity target) {
        return target instanceof Player player && player.isAlive() && !player.isSpectator();
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        for (HitContext hitContext : hitContexts) {
            InvincibleData.attack(hitContext.entity())
                    .attacker(getUuid())
                    .damageSource(source)
                    .damageAmount(getDamage())
                    .invincibleTime(10)
                    .apply();
        }
    }
}
