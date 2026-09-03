package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.AttachmentEntity;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.minion.MinionDamageSource;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.register.SummonerCurioRegister;
import first.summoner.utils.CuriosUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 黄蜂毒刺射弹 - 不可穿透，命中后施加中毒效果。
 */
public class HornetStinger extends Projectile implements IEntityCollision<HornetStinger>, IBlockCollision<HornetStinger> {

    public HornetStinger() {
        super();
    }

    public HornetStinger(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag(1.0f);
        setMaxSpeed(1.2f);
        setMaxLife(200);
        setGravity(0);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source != null) {
            HitContext hit = hitContexts.getFirst();
            boolean equipped = CuriosUtil.isEquipped(owner, SummonerCurioRegister.HivePack.get());
            RandomSource random = getOwner().getRandom();
            InvincibleData.attack(hit.entity())
                    .attacker(getUuid())
                    .damageSource(source)
                    .damageAmount(getDamage())
                    .effect(new MobEffectInstance(MobEffects.POISON, 80 + random.nextInt(60), equipped ? 1 : 0))
                    .apply();
        }
        setRemove();
    }

    @Override
    public boolean isValidCollisionTarget(HornetStinger entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof MinionDamageSource sds) {
            return sds.getMinion().isTarget(target);
        }
        return IEntityCollision.super.isValidCollisionTarget(entity, target);
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

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return SummonerAttachmentEntityRegister.HORNET_STINGER.get();
    }
}
