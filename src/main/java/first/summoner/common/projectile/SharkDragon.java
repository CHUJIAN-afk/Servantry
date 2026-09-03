package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.entity.IEntityCollision;
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

public class SharkDragon extends Projectile implements IEntityCollision<SharkDragon>, IBlockCollision<SharkDragon> {

    public SharkDragon() {
        super();
    }

    public SharkDragon(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(1f);
        setGravity(-0.002f);
        setMaxLife(100);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        currentPathNode = new PathNode(context.position(), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
        setRemove();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.2, -0.2, -0.6, 0.2, 0.2, 0.6);
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
        }
        currentPathNode = new PathNode(hit.hitPoint().add(getVelocity().scale(0.5)), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
        setRemove();
    }

    @Override
    public boolean isValidCollisionTarget(SharkDragon entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof MinionDamageSource minionDamageSource) {
            Minion minion = minionDamageSource.getMinion();
            return minion.isTarget(target);
        }
        return false;
    }

    @Override
    public AttachmentEntityType<SharkDragon> getType() {
        return SummonerAttachmentEntityRegister.SHARK_DRAGON.get();
    }
}
