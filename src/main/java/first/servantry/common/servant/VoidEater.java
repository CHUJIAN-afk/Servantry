package first.servantry.common.servant;

import first.servantry.api.servant.ai.ServantGoalSelector;

public class VoidEater extends StardustDragon {

    public VoidEater(){

    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        super.registerGoals(goalSelector);
    }
}
