package first.servantry.common.sentryServant;

import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.servant.MomentumServant;
import first.servantry.common.projectile.Rain;
import first.servantry.register.ServantryAttachmentEntityRegister;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Cloud extends MomentumServant implements IBlockCollision<Cloud> {

    private int cooldown = 4;

    public Cloud() {
        super();
        setDrag(0.8f);
    }

    @Override
    public int getSearchDistance() {
        return 0;
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            if (--cooldown <= 0) {
                RandomSource random = owner.getRandom();
                cooldown = random.nextInt(2, 4);
                Vec3 start = getPos().offsetRandom(random, 0.25f);
                Rain rain = new Rain(getDamageSource(), start, new Vec3(0, -0.5, 0));
                rain.join(owner);
            }
        }
        super.tick();
    }

    @Override
    public void dimensionChange() {
        setRemove();
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.CLOUD.get();
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.25, -0.1, -0.25, 0.25, 0.1, 0.25);
    }
}
