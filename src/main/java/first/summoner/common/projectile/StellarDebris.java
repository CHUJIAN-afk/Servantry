package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachmentEntity.IEntityCollision;
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

public class StellarDebris extends Projectile implements IEntityCollision<StellarDebris> {

    private DoomsdayStar source;
    private double orbitRadiusX;
    private double orbitRadiusY;
    private double orbitAngle;
    private double orbitSpeed;
    private Vec3 ringAxisA = new Vec3(1.0D, 0.0D, 0.0D);
    private Vec3 ringAxisB = new Vec3(0.0D, 0.0D, 1.0D);

    public StellarDebris() {
        super(SummonerAttachmentEntityRegister.STELLAR_DEBRIS);
        setDrag(1.0F);
        setGravity(0.0F);
        setMaxTickCount(-1);
    }

    public void orbit(DoomsdayStar source, double orbitRadiusX, double orbitRadiusY, double orbitAngle, double orbitSpeed) {
        this.source = source;
        this.orbitRadiusX = orbitRadiusX;
        this.orbitRadiusY = orbitRadiusY;
        this.orbitAngle = orbitAngle;
        this.orbitSpeed = orbitSpeed;
    }

    public void setRingFrame(Vec3 ringAxisA, Vec3 ringAxisB) {
        this.ringAxisA = ringAxisA;
        this.ringAxisB = ringAxisB;
    }

    @Override
    public boolean isAlive() {
        return super.isAlive() && (source == null || !source.isRemove());
    }

    @Override
    public void tick() {
        super.tick();
        if (!getLevel().isClientSide()) {
            orbitAngle += orbitSpeed;
            Vec3 center = source.getPos();
            Vec3 orbitOffset = ringAxisA.scale(Math.cos(orbitAngle) * orbitRadiusX)
                    .add(ringAxisB.scale(Math.sin(orbitAngle) * orbitRadiusY));
            setPos(new Vec3(
                    center.x + orbitOffset.x,
                    center.y + orbitOffset.y,
                    center.z + orbitOffset.z
            ));
            setVelocity(Vec3.ZERO);
        }
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.25D, -0.25D, -0.25D, 0.25D, 0.25D, 0.25D);
    }

    @Override
    public boolean isValidCollisionTarget(StellarDebris entity, LivingEntity target) {
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
