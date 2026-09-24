package first.summoner.common.projectile;

import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
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
        setGravity(-0.01f);
        setMaxTickCount(40);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.03, -0.03, -0.03, 0.03, 0.03, 0.03);
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setCurrentPathNode(currentPathNode.modifyPos(context.position()));
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
                for (HitContext hitContext : hitContexts) {
                LivingEntity living = hitContext.entity();
                if (hitTargets.add(living)) {
                    attack(living, getDamage(), 20);
                }
                if (hitTargets.size() >= maxPierceCount) {
                    setRemove();
                    break;
                }
            }
    }
}
