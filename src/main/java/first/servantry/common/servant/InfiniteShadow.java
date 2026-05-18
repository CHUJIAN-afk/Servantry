package first.servantry.common.servant;

import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentEntityRegister;

public class InfiniteShadow extends Terraprism {

    public InfiniteShadow() {
        super();
    }

    @Override
    public AttachmentEntityType<? extends Servant> getType() {
        return AttachmentEntityRegister.InfiniteShadow.get();
    }
}
