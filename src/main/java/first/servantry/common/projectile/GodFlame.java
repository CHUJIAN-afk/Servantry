package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.ServantryAttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 噬神火球 - 紫色高速火球射弹，造成20tick无敌帧。
 */
public class GodFlame extends Projectile implements ICollideAttack<GodFlame> {

    public GodFlame() {
        super();
    }

    public GodFlame(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(1);
        setMaxSpeed(4);
        setMaxLife(34);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source != null) {
            for (HitContext hitContext : hitContexts) {
                InvincibleData.attack(hitContext.entity())
                        .attacker(getUuid())
                        .damageSource(source)
                        .damageAmount(getDamage())
                        .invincibleTime(20)
                        .apply();
            }
        }
    }

    @Override
    public boolean isValidCollisionTarget(GodFlame entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            return servant.isTarget(target);
        }
        return false;
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.1, -0.1, -2, 0.1, 0.1, 0);
    }

    @Override
    public AttachmentEntityType<? extends Projectile> getType() {
        return ServantryAttachmentEntityRegister.GOD_FLAME.get();
    }

    @Override
    public int getTrailDuration() {
        return 10;
    }
}
