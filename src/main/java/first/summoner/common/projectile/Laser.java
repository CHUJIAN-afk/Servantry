package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.entity.ICollideAttack;
import first.lyra.common.entity.PathNode;
import first.lyra.common.minion.Minion;
import first.lyra.common.minion.MinionDamageSource;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * 激光射弹 - 高速直线飞行的红色激光束。
 */
public class Laser extends Projectile implements ICollideAttack<Laser>, IBlockCollision<Laser> {

    public Laser() {
        super();
    }

    public Laser(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(1);
        setMaxSpeed(4);
        setMaxLife(34);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        HitContext hit = hitContexts.getFirst();
        LivingEntity target = hit.entity();
        DamageSource source = getDamageSource();
        if (source != null) {
            UUID uuid = null;
            if (source instanceof MinionDamageSource minionDamageSource && minionDamageSource.getMinion() instanceof Minion minion) {
                uuid = minion.getUuid();
            }
            InvincibleData.attack(target)
                    .attacker(uuid)
                    .damageSource(source)
                    .damageAmount(getDamage())
                    .apply();
            target.setRemainingFireTicks(Math.min(target.getRemainingFireTicks() + 20, 120));
        }
        currentPathNode = new PathNode(hit.hitPoint(), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
        setRemove();
    }

    @Override
    public boolean isValidCollisionTarget(Laser entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof MinionDamageSource minionDamageSource) {
            Minion minion = minionDamageSource.getMinion();
            return minion.isTarget(target);
        }
        return ICollideAttack.super.isValidCollisionTarget(entity, target);
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.03, -0.03, -1, 0.03, 0.03, 0);
    }

    @Override
    public AttachmentEntityType<? extends Projectile> getType() {
        return SummonerAttachmentEntityRegister.LASER.get();
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.03, -0.03, -0.03, 0.03, 0.03, 0.03);
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        currentPathNode = new PathNode(context.position(), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
        setRemove();
    }
}