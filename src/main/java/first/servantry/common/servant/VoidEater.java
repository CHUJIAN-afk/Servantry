package first.servantry.common.servant;

import first.servantry.api.ServantryHelper;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.servant.goal.stardustDragon.StardustDragonFollowGoal;
import first.servantry.common.servant.goal.stardustDragon.StardustDragonIdleGoal;
import first.servantry.common.servant.goal.voidEater.VoidEaterAttackGoal;
import first.servantry.register.ServantryAttachmentEntityRegister;
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
        super.tick();
    }

    @Override
    protected void emitParticle() {

    }

    @Override
    public void onRemove() {
        if (isHead()) {
            List<VoidEater> voidEaters = ServantryHelper.get(owner)
                    .getEntityData()
                    .get(EntityData.Type.Servant, ServantryAttachmentEntityRegister.VOID_EATER.get());
            for (VoidEater voidEater : voidEaters) {
                voidEater.setRemove();
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
        List<VoidEater> voidEaters = ServantryHelper.get(owner)
                .getEntityData()
                .get(EntityData.Type.Servant, ServantryAttachmentEntityRegister.VOID_EATER.get());
        if (!voidEaters.isEmpty()) {
            return voidEaters.getFirst();
        }
        return null;
    }

    @Override
    public @Nullable VoidEater getPrecedingSegment() {
        if (!isHead()) {
            List<VoidEater> voidEaters = ServantryHelper.get(owner)
                    .getEntityData()
                    .get(EntityData.Type.Servant, ServantryAttachmentEntityRegister.VOID_EATER.get());
            int index = getSegmentIndex() - 1;
            if (index >= 0 && index < voidEaters.size()) {
                return voidEaters.get(index);
            }
        }
        return null;
    }

    @Override
    public @Nullable VoidEater getNextSegment() {
        if (getSegmentIndex() < getTotalSegments() - 1) {
            List<VoidEater> voidEaters = ServantryHelper.get(owner)
                    .getEntityData()
                    .get(EntityData.Type.Servant, ServantryAttachmentEntityRegister.VOID_EATER.get());
            int index = getSegmentIndex() + 1;
            if (index >= 0 && index < voidEaters.size()) {
                return voidEaters.get(index);
            }
        }
        return null;
    }

    @Override
    public AttachmentEntityType<? extends MomentumServant> getType() {
        return ServantryAttachmentEntityRegister.VOID_EATER.get();
    }
}
