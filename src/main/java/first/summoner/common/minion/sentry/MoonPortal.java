package first.summoner.common.minion.sentry;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.minion.Minion;
import first.summoner.common.projectile.MoonPortalLaser;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MoonPortal extends Minion {

    private int cooldown = 0;

    public MoonPortal() {
        super(SummonerAttachmentEntityRegister.MOON_PORTAL);
    }

    @Override
    public boolean isAlive() {
        return super.isAlive() && getPos().distanceToSqr(owner.position()) <= 128 * 128;
    }

    @Override
    public void tick() {
        super.tick();

        if (--cooldown < 0 && getTarget() != null) {
            cooldown = getRandom().nextInt(9, 11);
            fireLaser(getTarget());
        }
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    private void fireLaser(LivingEntity target) {
        Vec3 pos = getPos();
        Vec3 middlePos = target.getBoundingBox().getCenter();
        Vec3 startPos = middlePos.offsetRandom(getRandom(), 3);
        Vec3 endPos = middlePos.add(middlePos.subtract(startPos));

        Vec3 startDirection = pos.subtract(startPos).scale(-1).normalize();
        float startYaw = (float) Math.toDegrees(Math.atan2(-startDirection.x, startDirection.z));
        float startPitch = (float) Math.toDegrees(Math.asin(-startDirection.y));
        PathNode startPathNode = new PathNode(pos, startYaw, startPitch, 0);

        Vec3 middleDirection = pos.subtract(middlePos).scale(-1).normalize();
        float middleYaw = (float) Math.toDegrees(Math.atan2(-middleDirection.x, middleDirection.z));
        float middlePitch = (float) Math.toDegrees(Math.asin(-middleDirection.y));
        PathNode middlePathNode = new PathNode(pos, middleYaw, middlePitch, 0);

        Vec3 endDirection = pos.subtract(endPos).scale(-1).normalize();
        float endYaw = (float) Math.toDegrees(Math.atan2(-endDirection.x, endDirection.z));
        float endPitch = (float) Math.toDegrees(Math.asin(-endDirection.y));
        PathNode endPathNode = new PathNode(pos, endYaw, endPitch, 0);

        List<PathNode> pathNodes = new ArrayList<>(40);
        for (int i = 0; i < 10; i++) {
            float progress = (float) i / 9;
            pathNodes.add(startPathNode.lerp(middlePathNode, progress));
        }
        for (int i = 0; i < 10; i++) {
            float progress = (float) i / 9;
            pathNodes.add(middlePathNode.lerp(endPathNode, progress));
        }

        MoonPortalLaser projectile = new MoonPortalLaser();
        projectile.setOwner(owner);
        projectile.follow(this);
        projectile.init(pathNodes.getFirst());
        projectile.copyAttributes(this);
        projectile.aim(pos, target);
        projectile.setPath(pathNodes);
        LyraHelper.get(owner).add(projectile);
    }

    public void dimensionChange() {
        setRemove();
    }
}
