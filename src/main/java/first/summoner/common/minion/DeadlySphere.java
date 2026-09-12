package first.summoner.common.minion;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.entity.SyncFieldDispatcher;
import first.lyra.common.minion.MinionGoalSelector;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.minion.goal.MomentumMinionIdleGoal;
import first.summoner.common.minion.goal.deadlysphere.DeadlySphereAttackGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 致命球 - 冲刺攻击型仆从，AI与魔焰眼一致。
 */
public class DeadlySphere extends MomentumMinion implements IEntityCollision<DeadlySphere>, IBlockCollision<DeadlySphere> {

    private static final StreamCodec<RegistryFriendlyByteBuf, Appearance> APPEARANCE_STREAM_CODEC = StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(Appearance.class));

    private int trailTimer = 0;
    private Appearance appearance = Appearance.FIRE;
    private int preTimer = 0;

    public DeadlySphere() {
        super(SummonerAttachmentEntityRegister.DEADLY_SPHERE);
        setDrag(0.75f);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ByteBufCodecs.INT, this::getTrailTimer, this::setTrailTimer);
        fields.field(APPEARANCE_STREAM_CODEC, this::getAppearance, value -> appearance = value);
    }

    @Override
    public void registerGoals(MinionGoalSelector goalSelector) {
        goalSelector.addGoal(0, new DeadlySphereAttackGoal(this));
        goalSelector.addGoal(1, new MomentumMinionIdleGoal(this, 6, 0.01f, 64, false));
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public boolean canCollideAttack() {
        return isTarget(getTarget());
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            LivingEntity living = hit.entity();
            boolean applied = InvincibleData.attack(living)
                    .attacker(getUuid())
                    .damageSource(getDamageSource())
                    .damageAmount(getDamage())
                    .invincibleTime(4)
                    .apply();
            if (applied) {
                if (appearance == Appearance.ICE) {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100));
                }
                if (appearance == Appearance.FIRE) {
                    if (living.getRemainingFireTicks() < 100) {
                        living.setRemainingFireTicks(100);
                    }
                }
                if (appearance == Appearance.LIGHT) {
                    living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100));
                }
            }
        }
    }

    @Override
    public boolean canCollideWithBlocks() {
        return !isExecutingPath();
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setVelocity(IBlockCollision.bounceVelocity(getVelocity(), context, 0.98, 0.01));
    }

    @Override
    public void tick() {
        if (!getLevel().isClientSide()) {
            if (trailTimer > 0) {
                trailTimer--;
            }
            if (--preTimer == 0) {
                appearance = appearance.next();
            }
            if (getGoalSelector().getCurrentGoal() instanceof DeadlySphereAttackGoal && getVelocity().length() > 0.45) {
                emitAppearanceParticles();
            }
            setDesiredRotation(currentPathNode.yaw() + 8, currentPathNode.pitch() + 8, currentPathNode.roll() + 8);
        }
        super.tick();
    }

    public int getSearchDistance() {
        return 32;
    }

    public int getTrailTimer() {
        return trailTimer;
    }

    public void setTrailTimer(int trailTimer) {
        this.trailTimer = trailTimer;
    }

    public Appearance getAppearance() {
        return appearance;
    }

    public void nextAppearance() {
        preTimer = 20;
    }

    private void emitAppearanceParticles() {
        float yaw = currentPathNode.yaw() * ((float) Math.PI / 180F);
        float pitch = currentPathNode.pitch() * ((float) Math.PI / 180F);
        float roll = currentPathNode.roll() * ((float) Math.PI / 180F);
        int color = appearance.getColor();
        int edgeColor = appearance.getEdgeColor();
        Vec3 pos = getPos();

        for (int i = 0; i < 8; i++) {
            float dx = ((i & 1) == 0 ? -1 : 1);
            float dy = ((i & 2) == 0 ? -1 : 1);
            float dz = ((i & 4) == 0 ? -1 : 1);
            Vec3 dir = new Vec3(dx, dy, dz).normalize();
            dir = dir.yRot(yaw).xRot(pitch);
            if (roll != 0) dir = dir.zRot(roll);

            ParticleHelper.create(getLevel())
                    .generic(GenericParticleBuilder.create()
                            .centerColor(color)
                            .edgeColor(edgeColor)
                            .lifetime(10)
                            .lifetimeRandom(10)
                            .spin(0.1f)
                            .spinRandom(0.3F)
                            .friction(0.7F)
                            .scale(0.02f)
                            .scaleRandom(0.002f)
                    )
                    .pos(pos)
                    .velocity(dir)
                    .spread(0.01)
                    .speed(0.5)
                    .emit();
        }
    }

    public enum Appearance {
        ICE(0x6fe8ff, 0xb3f0ff),
        FIRE(0xff4422, 0xffaa44),
        LIGHT(0xffffaa, 0xffffff);

        private final int color;
        private final int edgeColor;

        Appearance(int color, int edgeColor) {
            this.color = color;
            this.edgeColor = edgeColor;
        }

        public int getColor() {
            return color;
        }

        public int getEdgeColor() {
            return edgeColor;
        }

        public Appearance next() {
            return Appearance.values()[(this.ordinal() + 1) % Appearance.values().length];
        }
    }
}
