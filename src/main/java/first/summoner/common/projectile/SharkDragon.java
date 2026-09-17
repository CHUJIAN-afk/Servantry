package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

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
        DamageSource source = getDamageSource();
        UUID uuid = getUuid();
        InvincibleData.attack(target)
                .attacker(uuid)
                .damageSource(source)
                .damageAmount(getDamage())
                .apply();
        currentPathNode = currentPathNode.modifyPos(hit.hitPoint().add(getVelocity().scale(0.5)));
        setRemove();
    }
}
