package first.summoner.common.projectile;

import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class SharkDragon extends Projectile implements IEntityCollision<SharkDragon>, IBlockCollision<SharkDragon> {

    public SharkDragon() {
        super(SummonerAttachmentEntityRegister.SHARK_DRAGON);
        setDrag(1);
        setGravity(0);
        setMaxTickCount(100);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setRemove();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.2, -0.2, -0.6, 0.2, 0.2, 0.6);
    }

    @Override
    public boolean isValidCollisionTarget(SharkDragon entity, LivingEntity target) {
        return getTargetCache().isTarget(target);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        HitContext hit = hitContexts.getFirst();
        LivingEntity target = hit.entity();
        attack(target, getDamage(), 0);
        currentPathNode = currentPathNode.modifyPos(hit.hitPoint().add(getVelocity().scale(0.5)));
        setRemove();
    }
}
