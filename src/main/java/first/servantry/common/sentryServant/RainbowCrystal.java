package first.servantry.common.sentryServant;

import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.utils.EasingCurve;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class RainbowCrystal extends Servant {

    private int cooldown = 0;

    public RainbowCrystal(){

    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            if (getPos().distanceToSqr(owner.position()) > 128 * 128) {
                setRemove();
            }
            if (--cooldown < 0 && isTarget(getTarget())) {
                cooldown = owner.getRandom().nextInt(6, 8);
                fire(getTarget());
            }
        }
        super.tick();
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    public void fire(LivingEntity target) {
        for (int i = 0; i < 3; i++) {
            Vec3 center = target.getBoundingBox().getCenter();
            Vec3 endPos = center.offsetRandom(owner.getRandom(), 4);
            Vec3 direction = endPos.subtract(getPos()).normalize();
            float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
            float pitch = (float) Math.toDegrees(Math.asin(-direction.y));
            PathNode startPathNode = new PathNode(getPos(), yaw, pitch, 0);
            PathNode endPathNode = new PathNode(endPos, yaw, pitch, 0);
            List<PathNode> list = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                float progress = (float) j / 9;
                list.add(startPathNode.lerp(endPathNode, EasingCurve.EASE_IN_OUT_CUBIC.apply(progress)));
            }
            first.servantry.common.projectile.RainbowCrystal projectile = new first.servantry.common.projectile.RainbowCrystal(getDamageSource(), startPathNode);
            projectile.setPath(list);
            projectile.join(owner);
        }
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.RainbowCrystal.get();
    }

    @Override
    public void dimensionChange() {
        setRemove();
    }
}
