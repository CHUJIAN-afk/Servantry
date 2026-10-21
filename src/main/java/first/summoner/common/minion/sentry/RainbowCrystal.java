package first.summoner.common.minion.sentry;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.minion.Minion;
import first.summoner.common.projectile.MiniRainbowCrystal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class RainbowCrystal extends Minion {

    private int cooldown = 0;

    public RainbowCrystal() {
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
            MiniRainbowCrystal projectile = new MiniRainbowCrystal();
            projectile.setOwner(owner);
            projectile.init(new PathNode(endPos, 0, 0, 0));
            projectile.setSourcePosition(getPos());
            projectile.copyAttributes(this);
            LyraHelper.get(owner).add(projectile);
        }
    }

    public void dimensionChange() {
        setRemove();
    }
}
