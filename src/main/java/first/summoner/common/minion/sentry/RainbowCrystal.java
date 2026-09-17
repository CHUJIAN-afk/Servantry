package first.summoner.common.minion.sentry;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.minion.Minion;
import first.lyra.utils.EasingCurve;
import first.summoner.common.projectile.MiniRainbowCrystal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class RainbowCrystal extends Minion {

    private int cooldown = 0;

    public RainbowCrystal(){
        super(SummonerAttachmentEntityRegister.RAINBOW_CRYSTAL);
    }

    @Override
    public boolean isAlive() {
        return super.isAlive() && getPos().distanceToSqr(owner.position()) <= 128 * 128;
    }

    @Override
    public void tick() {
        super.tick();

        if (--cooldown < 0 && getTarget() != null) {
            cooldown = getRandom().nextInt(6, 8);
            fire(getTarget());
        }
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    public void fire(LivingEntity target) {
        for (int i = 0; i < 3; i++) {
            Vec3 center = target.getBoundingBox().getCenter();
            Vec3 endPos = center.offsetRandom(getRandom(), 4);
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
            MiniRainbowCrystal projectile = new MiniRainbowCrystal();
            projectile.setOwner(owner);
            projectile.init(startPathNode);
            projectile.copyAttributes(this);
            projectile.setPath(list);
            LyraHelper.get(owner).add(projectile);
        }
    }

        public void dimensionChange() {
        setRemove();
    }
}
