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

/**
 * 电能激光射弹 - 高速直线飞行的蓝色激光束。
 * 继承Laser的物理特性，颜色改为蓝色，无着火效果。
 */
public class ElectricLaser extends Projectile implements IEntityCollision<ElectricLaser>, IBlockCollision<ElectricLaser> {

    public ElectricLaser() {
        super(SummonerAttachmentEntityRegister.ELECTRIC_LASER);
        setDrag(1);
        setGravity(0);
        setMaxTickCount(34);
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
        currentPathNode = currentPathNode.modifyPos(hit.hitPoint());
        setRemove();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.03, -0.03, -1, 0.03, 0.03, 0);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.03, -0.03, -0.03, 0.03, 0.03, 0.03);
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setRemove();
    }
}
