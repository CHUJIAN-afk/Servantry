package first.servantry.common.servent.goal;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.api.PathNode;
import first.servantry.api.common.attachment.ServantData;
import first.servantry.common.servent.EnchantedThrowingKnives;
import first.servantry.register.AttachmentRegister;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;

/**
 * 附魔飞刀空闲目标。
 * <p>
 * 空闲状态下，飞刀围绕玩家旋转悬浮。
 * 当发现目标时，切换到攻击状态。
 * </p>
 */
public class EnchantedThrowingKnivesIdleGoal extends ServantGoal<EnchantedThrowingKnives> {

    public EnchantedThrowingKnivesIdleGoal(EnchantedThrowingKnives servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        // 当没有目标或不在攻击状态时进入空闲
        return servant.getTarget() == null || !servant.attacking;
    }

    @Override
    public boolean canContinueToUse() {
        return servant.getTarget() == null;
    }

    @Override
    public boolean isInterruptable() {
        return true;
    }

    @Override
    public void start() {
        servant.attacking = false;
    }

    @Override
    public void stop() {
        // 切换到攻击状态时调用
    }

    @Override
    public void tick() {
        Player owner = servant.getOwner();
        if (owner == null) return;

        ServantData data = owner.getData(AttachmentRegister.ServantData);
        PathNode idleNode = servant.getInterpolatedIdleState(owner, data.getOrder(servant), Math.max(1, data.getSameSize(servant)), 1.0f);

        // 平滑过渡到空闲位置
        Vec3 nextPos = servant.getPos().lerp(idleNode.pos(), 0.25f);
        float nextYaw = Mth.rotLerp(0.25f, servant.getYaw(), idleNode.yaw());
        float nextPitch = Mth.rotLerp(0.25f, servant.getPitch(), idleNode.pitch());
        float nextRoll = Mth.rotLerp(0.25f, servant.getRoll(), idleNode.roll());

        servant.setPath(Collections.singletonList(new PathNode(nextPos, nextYaw, nextPitch, nextRoll)));
    }
}
