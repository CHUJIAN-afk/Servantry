package first.summoner.common.projectile;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.entity.PathNode;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrossbowBolt extends Projectile implements IBlockCollision<CrossbowBolt>, IEntityCollision<CrossbowBolt> {

    private int maxPierceCount = 3;
    public final Set<LivingEntity> hitTargets = new HashSet<>();

    public CrossbowBolt() {
        super(SummonerAttachmentEntityRegister.CROSSBOW_BOLT);
        setDrag(0.97f);
        setMaxLife(40);
        setGravity(-0.01f);
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

    public void setMaxPierceCount(int maxPierceCount) {
        this.maxPierceCount = maxPierceCount;
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source != null) {
            for (HitContext hitContext : hitContexts) {
                LivingEntity living = hitContext.entity();
                if (hitTargets.add(living)) {
                    InvincibleData.attack(living)
                            .attacker(getUuid())
                            .damageSource(source)
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
