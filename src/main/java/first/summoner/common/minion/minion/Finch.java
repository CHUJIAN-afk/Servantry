package first.summoner.common.minion.minion;

import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.minion.minion.goal.finch.FinchAttackGoal;
import first.summoner.common.minion.minion.goal.finch.FinchIdleGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.List;

/**
 * 小鸟：绕主人编队悬停待机，发现目标后贴到目标附近盘旋并靠身体碰撞啄击。
 * <p>
 * {@code idleBlend} 为待机姿态混合权重，渲染节点在物理节点与待机节点之间插值。
 */
public class Finch extends MomentumMinion implements IEntityCollision<Finch> {

    public float idleBlend;
    public float idleBlendO;

    public Finch() {
        super(SummonerAttachmentEntityRegister.FINCH);
        setGravity(0);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.FLOAT, () -> idleBlendO, value -> idleBlendO = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> idleBlend, value -> idleBlend = value);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new FinchAttackGoal(this));
        goalSelector.addGoal(1, new FinchIdleGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "finch", 2, state -> {
            if (idleBlend == 1) {
                state.setControllerSpeed(0.05f);
            } else {
                state.setControllerSpeed(1);
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("move.fly"));
        }));
    }

    @Override
    public int getSearchDistance() {
        return 16;
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.1, -0.1, -0.15, 0.1, 0.1, 0.15);
    }

    @Override
    public boolean canCollideAttack() {
        return getTarget() != null;
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            attack(hit.entity(), getDamage(), 2);
        }
    }

    @Override
    public void tick() {
        super.tick();
        idleBlendO = idleBlend;
        if (getTarget() != null) {
            idleBlend = Math.max(0, idleBlend - 0.05F);
        } else {
            double distance = getPos().distanceTo(getInterpolatedIdleState(1).pos());
            if (distance < 0.3) {
                idleBlend = Math.min(1, idleBlend + 0.05F);
            } else if (idleBlend != 1) {
                idleBlend = Math.max(0, idleBlend - 0.1F);
            }
        }
    }

    @Override
    public PathNode getRenderNode(float partialTick) {
        PathNode base = super.getRenderNode(partialTick);
        PathNode idle = getInterpolatedIdleState(partialTick);
        return base.lerp(idle, Mth.lerp(partialTick, idleBlendO, idleBlend));
    }

    /**
     * 待机位置：绕主人身体朝向四等分排布，奇数层整体错开 45°，越高层越高。
     */
    public PathNode getInterpolatedIdleState(float partialTick) {
        float bodyYaw = Mth.rotLerp(partialTick, owner.yBodyRotO, owner.yBodyRot);
        int order = getOrder();
        int layer = order / 4;
        int index = order % 4;
        double angle = index * (360.0 / 4) + (layer & 1) * (180.0 / 4);
        Vec3 offset = Vec3.directionFromRotation(0, bodyYaw + (float) angle).scale(0.2);
        Vec3 position = owner.getPosition(partialTick)
                .add(offset.x, owner.getBbHeight() * 0.83 + 0.78 + layer * 0.18, offset.z);
        return new PathNode(position, bodyYaw, 0, 0);
    }
}
