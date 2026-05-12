package first.servantry.common.servant.goal.stardustCell;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.StardustCell;
import first.servantry.register.AttachmentRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 星尘细胞攻击Goal。
 * <p>
 * 追踪目标并在射击冷却结束时发射星细胞射弹。
 * </p>
 */
public class StardustCellAttackGoal extends ServantGoal<StardustCell> {

    public StardustCellAttackGoal(StardustCell servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        LivingEntity target = servant.getTarget();
        return target != null && servant.isTarget(target);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = servant.getTarget();
        return target != null && servant.isTarget(target);
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        if (target == null || !target.isAlive()) return;

        Player owner = servant.getOwner();
        int order = owner.getData(AttachmentRegister.EntityData).getOrder(servant);
        Vec3 anchor = servant.getHaloAnchorPos(owner, target, order);

        Vec3 toAnchor = anchor.subtract(servant.getPos());
        double dist = toAnchor.length();
        if (dist > 0.05) {
            double force = Math.min(dist * 0.08, 0.4);
            servant.applyForce(toAnchor.normalize().scale(force));
        }

        // 动态摩擦与限速
        float friction = dist < 1.5 ? 0.55f : 0.85f;
        float maxSpd = (float) Math.min(1.8, dist * 0.8 + 0.05);
        Vec3 vel = servant.getVelocity();
        if (vel.lengthSqr() > maxSpd * maxSpd) vel = vel.normalize().scale(maxSpd);
        servant.setVelocity(vel.scale(friction));
        // 射击冷却
        if (servant.getShootCooldown() <= 0) {
            servant.shootAtTarget(target);
            servant.setShootCooldown(12 + owner.getRandom().nextInt(4));
        }
    }

    @Override
    public void stop() {
        servant.setTarget(null);
    }
}