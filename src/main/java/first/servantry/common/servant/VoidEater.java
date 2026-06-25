package first.servantry.common.servant;

import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.servant.goal.stardustDragon.StardustDragonFollowGoal;
import first.servantry.common.servant.goal.stardustDragon.StardustDragonIdleGoal;
import first.servantry.common.servant.goal.voidEater.VoidEaterAttackGoal;

public class VoidEater extends StardustDragon {

    public VoidEater(){

    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new VoidEaterAttackGoal(this));
        goalSelector.addGoal(1, new StardustDragonIdleGoal(this));
        goalSelector.addGoal(2, new StardustDragonFollowGoal(this));
    }
}
