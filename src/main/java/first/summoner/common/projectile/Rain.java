package first.summoner.common.projectile;

import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Rain extends Projectile implements IBlockCollision<Rain>, IEntityCollision<Rain> {

    public Rain(){
        super(SummonerAttachmentEntityRegister.RAIN);
        setMaxTickCount(400);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.03, -0.03, -0.03, 0.03, 0.03, 0.03);
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setRemove();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.03, -0.03, -0.5, 0.03, 0.03, 0);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        for (HitContext hitContext : hitContexts) {
            LivingEntity living = hitContext.entity();
            attack(living, getDamage(), 20);
            living.setRemainingFireTicks(0);
        }
    }
}
