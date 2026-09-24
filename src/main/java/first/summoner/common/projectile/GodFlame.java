package first.summoner.common.projectile;

import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * 噬神火球 - 紫色高速火球射弹，造成20tick无敌帧。
 */
public class GodFlame extends Projectile implements IEntityCollision<GodFlame> {

    public GodFlame() {
        super(SummonerAttachmentEntityRegister.GOD_FLAME);
        setDrag(1);
        setMaxTickCount(34);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
                for (HitContext hitContext : hitContexts) {
                attack(hitContext.entity(), getDamage(), 20);
            }
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.1, -0.1, -2, 0.1, 0.1, 0);
    }

    public int getTrailDuration() {
        return 10;
    }
}
