package first.summoner.common.entity.astrageldon;

import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.entity.Astrageldon;
import first.summoner.register.SummonerEntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AstrageldonAfterimage extends AstrageldonProjectile implements GeoAnimatable {

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private Vec3 start = Vec3.ZERO;
    private Vec3 end = Vec3.ZERO;
    private double wing;
    private int direction;

    public AstrageldonAfterimage(EntityType<? extends AstrageldonAfterimage> type, Level level) {
        super(type, level, 60);
    }

    public static void spawn(Astrageldon boss, AstrageldonDash dash, double wing, int direction) {
        AstrageldonAfterimage image = new AstrageldonAfterimage(SummonerEntityRegister.ASTRAGELDON_AFTERIMAGE.get(), boss.level());
        image.setOwner(boss);
        image.start = dash.start();
        image.end = dash.end();
        image.wing = wing;
        image.direction = direction;
        image.damage = (float) boss.getAttributeValue(Attributes.ATTACK_DAMAGE);
        Vec3 forward = dash.end().subtract(dash.start()).normalize().scale(direction);
        image.setCenterPose(dash.formationPosition(direction < 0 ? 1 : 0, direction, wing), forward);
        image.addToLevel();
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.VEC_3, () -> start, value -> start = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> end, value -> end = value);
        fields.field(LyraStreamCodecs.DOUBLE, () -> wing, value -> wing = value);
        fields.field(LyraStreamCodecs.INT, () -> direction, value -> direction = value);
    }

    @Override
    protected void step() {
        if (getOwner() instanceof Astrageldon boss && boss.isAlive() && boss.getPhase() == Astrageldon.Phase.Phase22) {
            AstrageldonDash dash = boss.getResolvingDash();
            if (dash != null && dash.start().equals(start) && dash.end().equals(end)) {
                float counter = dash.counter() + (2 - age % 3) * dash.resolveSpeed();
                float progress = AstrageldonDash.progress(counter, direction);
                Vec3 target = dash.formationPosition(progress, direction, wing);
                Vec3 movement = target.subtract(getBoundingBox().getCenter()).scale(0.2);
                Vec3 forward = end.subtract(start).normalize().scale(direction);
                for (int i = 0; i < 5; i++) {
                    setCenterPose(getBoundingBox().getCenter().add(movement), forward);
                    AstrageldonCombat.hurtPlayersSphere(this, boss, damage, 72.0 / 32, AstrageldonCombat::armorCrunch);
                }
            } else {
                discard();
            }
        } else {
            discard();
        }
    }

    @Override
    protected boolean canDamage() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public double getTick(Object entity) {
        return tickCount;
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("StartX", start.x);
        tag.putDouble("StartY", start.y);
        tag.putDouble("StartZ", start.z);
        tag.putDouble("EndX", end.x);
        tag.putDouble("EndY", end.y);
        tag.putDouble("EndZ", end.z);
        tag.putDouble("Wing", wing);
        tag.putInt("Direction", direction);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        start = new Vec3(tag.getDouble("StartX"), tag.getDouble("StartY"), tag.getDouble("StartZ"));
        end = new Vec3(tag.getDouble("EndX"), tag.getDouble("EndY"), tag.getDouble("EndZ"));
        wing = tag.getDouble("Wing");
        direction = tag.getInt("Direction");
    }
}
