package first.servantry.common.servant.goal.voidEater;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.VoidEater;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 星尘龙攻击目标。
 */
public class VoidEaterAttackGoal extends ServantGoal<VoidEater> {

    public AttackMode mode = AttackMode.GOD_DASH;
    public int combat = 0;

    public VoidEaterAttackGoal(VoidEater servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isHead() && servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        servant.spiralToward(targetPos, 0.25);
    }

    public enum AttackMode {
        GOD_DASH, HOLY_INCINERATION, COSMIC_MAELSTROM
    }
}