package first.summoner.common.minion.minion;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.summoner.common.minion.minion.goal.stardustDragon.StardustDragonFollowGoal;
import first.summoner.common.minion.minion.goal.stardustDragon.StardustDragonIdleGoal;
import first.summoner.common.minion.minion.goal.voidEater.VoidEaterAttackGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoidEater extends StardustDragon {

    public VoidEater(){
        super(SummonerAttachmentEntityRegister.VOID_EATER);
        setDrag(0.75f);
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
            List<VoidEater> voidEaters = LyraHelper.get(owner.level()).getEntityData().get(SummonerAttachmentEntityRegister.VOID_EATER.get());
            for (VoidEater voidEater : voidEaters) {
                voidEater.setRemove();
            }
        }
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new VoidEaterAttackGoal(this));
        goalSelector.addGoal(1, new StardustDragonIdleGoal(this));
        goalSelector.addGoal(2, new StardustDragonFollowGoal(this));
    }

    @Override
    public @Nullable StardustDragon getHead() {
        List<VoidEater> voidEaters = LyraHelper.get(owner.level())
                .getEntityData()
                .get(SummonerAttachmentEntityRegister.VOID_EATER.get());
        if (!voidEaters.isEmpty()) {
            return voidEaters.getFirst();
        }
        return null;
    }

    @Override
    public @Nullable VoidEater getPrecedingSegment() {
        if (!isHead()) {
            List<VoidEater> voidEaters = LyraHelper.get(owner.level())
                    .getEntityData()
                    .get(SummonerAttachmentEntityRegister.VOID_EATER.get());
            int index = getOrder() - 1;
            if (index >= 0 && index < voidEaters.size()) {
                return voidEaters.get(index);
            }
        }
        return null;
    }

    @Override
    public @Nullable VoidEater getNextSegment() {
        if (getOrder() < getSameSize() - 1) {
            List<VoidEater> voidEaters = LyraHelper.get(owner.level())
                    .getEntityData()
                    .get(SummonerAttachmentEntityRegister.VOID_EATER.get());
            int index = getOrder() + 1;
            if (index >= 0 && index < voidEaters.size()) {
                return voidEaters.get(index);
            }
        }
        return null;
    }
}
