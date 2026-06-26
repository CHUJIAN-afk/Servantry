package first.servantry.common.servant;

import first.servantry.api.ServantryHelper;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.servant.goal.stardustDragon.StardustDragonFollowGoal;
import first.servantry.common.servant.goal.stardustDragon.StardustDragonIdleGoal;
import first.servantry.common.servant.goal.voidEater.VoidEaterAttackGoal;
import first.servantry.register.AttachmentEntityRegister;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoidEater extends StardustDragon {

    public VoidEater(){
        super();
        setDrag(0.75f);
        setRotationSpeed(0);
    }

    @Override
    public void tick() {
        Vec3 velocity = getVelocity();
        setSpiralPhase(getSpiralPhase() + 0.2f * (float) velocity.length());
        Vec3 dir = velocity.normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        float pitch = (float) Math.toDegrees(Math.atan2(-dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)));
        setDesiredRotation(yaw, pitch, getRoll());
        super.tick();
    }

    @Override
    protected void emitParticle() {

    }

    @Override
    public void onRemove() {
        if (isHead()) {
            List<VoidEater> dragons = ServantryHelper.get(owner).getEntityData().get(EntityData.Type.Servant, VoidEater.class, true);
            for (VoidEater dragon : dragons) {
                dragon.setRemove();
            }
        }
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new VoidEaterAttackGoal(this));
        goalSelector.addGoal(1, new StardustDragonIdleGoal(this));
        goalSelector.addGoal(2, new StardustDragonFollowGoal(this));
    }

    @Override
    public @Nullable StardustDragon getHead() {
        List<VoidEater> dragons = ServantryHelper.get(owner).getEntityData().get(EntityData.Type.Servant, VoidEater.class, true);
        if (!dragons.isEmpty()) {
            return dragons.getFirst();
        }
        return null;
    }

    @Override
    public @Nullable VoidEater getPrecedingSegment() {
        if (!isHead()) {
            List<VoidEater> dragons = ServantryHelper.get(owner).getEntityData().get(EntityData.Type.Servant, VoidEater.class, true);
            int index = getSegmentIndex() - 1;
            if (index >= 0 && index < dragons.size()) {
                return dragons.get(index);
            }
        }
        return null;
    }

    @Override
    public @Nullable VoidEater getNextSegment() {
        if (getSegmentIndex() < getTotalSegments() - 1) {
            List<VoidEater> dragons = ServantryHelper.get(owner).getEntityData().get(EntityData.Type.Servant, VoidEater.class, true);
            int index = getSegmentIndex() + 1;
            if (index >= 0 && index < dragons.size()) {
                return dragons.get(index);
            }
        }
        return null;
    }

    @Override
    public AttachmentEntityType<? extends MomentumServant> getType() {
        return AttachmentEntityRegister.VoidEater.get();
    }
}
