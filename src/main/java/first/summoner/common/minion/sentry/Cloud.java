package first.summoner.common.minion.sentry;



import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.minion.MomentumMinion;
import first.summoner.common.projectile.Rain;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Cloud extends MomentumMinion implements IBlockCollision<Cloud> {

    private int cooldown = 4;

    public Cloud() {
        super(SummonerAttachmentEntityRegister.CLOUD);
        setGravity(0);
    }

    @Override
    public int getSearchDistance() {
        return 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (--cooldown <= 0) {
            RandomSource random = getRandom();
            cooldown = random.nextInt(2, 4);
            Vec3 start = getPos().add(0, -0.25, 0).offsetRandom(random, 0.25f);
            Rain rain = new Rain();
            rain.setOwner(owner);
            rain.init(new PathNode(start, 0, 90, 0));
            rain.copyAttributes(this);
            rain.setVelocity(new Vec3(0, -0.5, 0));
            LyraHelper.get(owner).add(rain);
        }
    }

    public void dimensionChange() {
        setRemove();
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.25, -0.1, -0.25, 0.25, 0.1, 0.25);
    }
}
