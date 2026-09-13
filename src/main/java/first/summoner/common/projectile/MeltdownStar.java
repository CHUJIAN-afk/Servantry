package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.projectile.Projectile;
import first.summoner.common.master.DoomsdayStar;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MeltdownStar extends Projectile implements IEntityCollision<MeltdownStar> {

    private static final double FULL_SPEED = 0.6D;
    private static final double ORBIT_RADIUS = 2.5D;
    private static final double ORBIT_SPEED = 0.15D;

    private DoomsdayStar source;
    private Vec3 launchDirection = Vec3.ZERO;
    private double orbitAngle;
    private boolean launched;

    public MeltdownStar() {
        super(SummonerAttachmentEntityRegister.MELTDOWN_STAR);
        setDrag(1.0F);
        setGravity(0.0F);
        setMaxLife(100);
    }

    public void orbit(DoomsdayStar source, Vec3 launchDirection) {
        this.source = source;
        this.launchDirection = launchDirection.normalize();
        this.orbitAngle = source.getRandom().nextDouble() * Math.TAU;
        this.launched = false;
    }

    public void launch() {
        launched = true;
        source = null;
        setVelocity(launchDirection.scale(FULL_SPEED));
    }

    @Override
    public void tick() {
        if (!getLevel().isClientSide() && !launched) {
            if (source == null || source.isRemove()) {
                setRemove();
            } else {
                orbitAngle += ORBIT_SPEED;
                Vec3 center = source.getAbsoluteHitbox().getCenter();
                Vec3 offset = launchDirection.scale(ORBIT_RADIUS);
                double horizontalRadius = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
                double angle = Math.atan2(offset.z, offset.x) + orbitAngle;
                setPos(new Vec3(
                        center.x + Math.cos(angle) * horizontalRadius,
                        center.y + offset.y,
                        center.z + Math.sin(angle) * horizontalRadius
                ));
                setVelocity(Vec3.ZERO);
            }
        }
        super.tick();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.25D, -0.25D, -0.25D, 0.25D, 0.25D, 0.25D);
    }

    @Override
    public boolean isValidCollisionTarget(MeltdownStar entity, LivingEntity target) {
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
