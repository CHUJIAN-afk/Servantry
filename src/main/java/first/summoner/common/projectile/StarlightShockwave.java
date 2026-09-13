package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class StarlightShockwave extends Projectile implements IEntityCollision<StarlightShockwave> {

    public StarlightShockwave() {
        super(SummonerAttachmentEntityRegister.STARLIGHT_SHOCKWAVE);
        setDrag(1.0F);
        setGravity(0.0F);
        setMaxLife(200);
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.5D, -0.5D, -0.5D, 0.5D, 0.5D, 0.5D);
    }

    @Override
    public boolean isValidCollisionTarget(StarlightShockwave entity, LivingEntity target) {
        return target instanceof Player player && player.isAlive() && !player.isSpectator();
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source == null) {
            return;
        }

        Entity attacker = source.getEntity();
        UUID attackerUuid = attacker == null ? getUuid() : attacker.getUUID();
        for (HitContext hitContext : hitContexts) {
            InvincibleData.attack(hitContext.entity())
                    .attacker(attackerUuid)
                    .damageSource(source)
                    .damageAmount(getDamage())
                    .invincibleTime(10)
                    .apply();
        }
        setRemove();
    }
}
