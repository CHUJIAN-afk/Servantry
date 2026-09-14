package first.summoner.common.projectile;


import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.register.SummonerCurioRegister;
import first.summoner.utils.CuriosUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 黄蜂毒刺射弹 - 不可穿透，命中后施加中毒效果。
 */
public class HornetStinger extends Projectile implements IEntityCollision<HornetStinger>, IBlockCollision<HornetStinger> {

    public HornetStinger() {
        super(SummonerAttachmentEntityRegister.HORNET_STINGER);
        setMaxTickCount(200);
        setDrag(1.0f);
        setGravity(0);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        LivingEntity living = hitContexts.getFirst().entity();
        attack(living, getDamage(), 0);
        living.addEffect(new MobEffectInstance(MobEffects.POISON, 80 + getRandom().nextInt(60), owner != null && CuriosUtil.isEquipped(owner, SummonerCurioRegister.HivePack.get()) ? 1 : 0));
        setRemove();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.1, -0.1, -0.1, 0.1, 0.1, 0.1);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return getHitbox();
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setRemove();
    }
}
