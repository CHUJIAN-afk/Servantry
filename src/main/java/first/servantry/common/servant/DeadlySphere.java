package first.servantry.common.servant;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.servant.goal.deadlysphere.DeadlySphereAttackGoal;
import first.servantry.common.servant.goal.deadlysphere.DeadlySphereIdleGoal;
import first.servantry.register.AttachmentEntityRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 致命球 - 冲刺攻击型仆从，AI与魔焰眼一致。
 */
public class DeadlySphere extends MomentumServant implements ICollideAttack<DeadlySphere>, IBlockCollision<DeadlySphere> {

    private int trailTimer = 0;
    private Appearance appearance = Appearance.FIRE;

    public DeadlySphere() {
        super();
        setDrag(0.75f);
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new DeadlySphereAttackGoal(this));
        goalSelector.addGoal(2, new DeadlySphereIdleGoal(this));
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public boolean canCollideAttack() {
        return isTarget(getTarget());
    }

    @Override
    public boolean isValidCollisionTarget(DeadlySphere entity, LivingEntity target) {
        return isTarget(target);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            InvincibleData.criteriaAttack(hit.entity(), getUuid(), 4, getDamageSource(), getDamage(), InvincibleData.Type.PARTIAL);
        }
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setVelocity(IBlockCollision.bounceVelocity(getVelocity(), context, 0.95, 0.01));
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            if (trailTimer > 0) {
                trailTimer--;
            }
            setDesiredRotation(currentPathNode.yaw() + 8, currentPathNode.pitch() + 8, currentPathNode.roll() + 8);
        }
        super.tick();
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeInt(trailTimer);
        buf.writeEnum(appearance);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        trailTimer = buf.readInt();
        appearance = buf.readEnum(Appearance.class);
    }

    @Override
    public float getDamage() {
        return 5.5f;
    }

    @Override
    public float getKnockback() {
        return 0.1f;
    }

    @Override
    public AttachmentEntityType<? extends MomentumServant> getType() {
        return AttachmentEntityRegister.DeadlySphere.get();
    }

    public int getTrailTimer() {
        return trailTimer;
    }

    public void setTrailTimer(int trailTimer) {
        this.trailTimer = trailTimer;
    }

    public Appearance getAppearance() {
        return appearance;
    }

    public void setAppearance(Appearance appearance) {
        this.appearance = appearance;
    }

    public enum Appearance {
        ICE,
        FIRE,
        LIGHT;

        public Appearance next() {
            return Appearance.values()[(this.ordinal() + 1) % Appearance.values().length];
        }
    }
}
