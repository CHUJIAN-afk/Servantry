package first.servantry.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.*;
import first.lyra.common.projectile.Projectile;
import first.lyra.common.servant.Servant;
import first.lyra.common.servant.ServantDamageSource;
import first.servantry.register.ServantryAttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrossbowBolt extends Projectile implements IBlockCollision<CrossbowBolt>, ICollideAttack<CrossbowBolt> {

    private int maxPierceCount = 3;
    public final Set<LivingEntity> hitTargets = new HashSet<>();

    public CrossbowBolt() {
    }

    public CrossbowBolt(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(0.97f);
        setMaxSpeed(4);
        setMaxLife(40);
        setGravity(-0.01f);
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.CROSSBOW_BOLT.get();
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.03, -0.03, -0.03, 0.03, 0.03, 0.03);
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setCurrentPathNode(new PathNode(context.position(), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll()));
        setRemove();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.1, -0.1, -2, 0.1, 0.1, 0);
    }

    @Override
    public boolean isValidCollisionTarget(CrossbowBolt entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            return servant.isTarget(target);
        }
        return false;
    }

    public void setMaxPierceCount(int maxPierceCount) {
        this.maxPierceCount = maxPierceCount;
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        if (damageSource != null) {
            for (HitContext hitContext : hitContexts) {
                LivingEntity living = hitContext.entity();
                if (hitTargets.add(living)) {
                    InvincibleData.attack(living)
                            .attacker(uuid)
                            .damageSource(damageSource)
                            .damageAmount(getDamage())
                            .invincibleTime(20)
                            .apply();
                }
                if (hitTargets.size() >= maxPierceCount) {
                    setRemove();
                    break;
                }
            }
        }
    }
}
