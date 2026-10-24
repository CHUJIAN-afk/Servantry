package first.summoner.common.entity.astrageldon;

import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.PlannedPath;
import first.lyra.common.attachmentEntity.RenderKeyframe;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.entity.IPathEntity;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.entity.Astrageldon;
import first.summoner.register.SummonerEntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NovaSlimer extends Monster implements IPathEntity {

    private UUID bossId;
    private float angle;
    private int counter;
    private int attackTimer;
    private int despawn;
    private boolean leaving;
    private Vec3 velocity = Vec3.ZERO;
    private Vec3 acceleration = Vec3.ZERO;
    private Vec3 savedTarget = Vec3.ZERO;
    private Vec3 chargeOrigin = Vec3.ZERO;

    public NovaSlimer(EntityType<? extends NovaSlimer> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 144)
                .add(Attributes.ATTACK_DAMAGE, 24)
                .add(Attributes.ARMOR, 30)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.FOLLOW_RANGE, 200);
    }

    public static void spawn(Astrageldon boss, float angle) {
        NovaSlimer slimer = new NovaSlimer(SummonerEntityRegister.NOVA_SLIMER.get(), boss.level());
        slimer.bossId = boss.getUUID();
        slimer.angle = angle;
        slimer.setTarget(boss.getTarget());
        slimer.setPos(boss.getBoundingBox().getCenter().add(0, -slimer.getBbHeight() / 2, 0));
        boss.level().addFreshEntity(slimer);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        IPathEntity.super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.OPTIONAL_UUID, () -> Optional.ofNullable(bossId), value -> bossId = value.orElse(null));
        fields.field(LyraStreamCodecs.FLOAT, () -> angle, value -> angle = value);
        fields.field(LyraStreamCodecs.INT, () -> counter, value -> counter = value);
        fields.field(LyraStreamCodecs.INT, () -> attackTimer, value -> attackTimer = value);
        fields.field(LyraStreamCodecs.INT, () -> despawn, value -> despawn = value);
        fields.field(LyraStreamCodecs.BOOL, () -> leaving, value -> leaving = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> velocity, value -> velocity = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> acceleration, value -> acceleration = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> savedTarget, value -> savedTarget = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> chargeOrigin, value -> chargeOrigin = value);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && isAlive()) {
            if (getTarget() == null || !getTarget().isAlive() || getTarget().isSpectator()) {
                setTarget(level().getNearestPlayer(this, 200));
            }
            PathNode from = node();
            List<RenderKeyframe> frames = new ArrayList<>(2);
            for (int i = 0; i < 3 && !isRemoved(); i++) {
                substep();
                if (i < 2) {
                    frames.add(new RenderKeyframe((i + 1) / 3f, node()));
                }
            }
            if (!isRemoved()) {
                PathNode to = node();
                setCurrentPathNode(from);
                setPlannedPath(new PlannedPath("nova_slimer", List.of(to)).render(0, frames));
                setDeltaMovement(velocity.scale(3.0 / 32));
            }
        }
        if (!isRemoved()) {
            tickPath();
        }
    }

    private PathNode node() {
        return new PathNode(position(), getYRot(), getXRot(), getRoll());
    }

    private void substep() {
        velocity = velocity.multiply(0.93, 1, 0.93);
        if (velocity.horizontalDistance() < 0.1) {
            velocity = new Vec3(0, velocity.y, 0);
        }
        int direction = angle < 0 ? -1 : 1;
        if (bossId != null && ((ServerLevel) level()).getEntity(bossId) instanceof Astrageldon boss && boss.isAlive() && getTarget() instanceof Player player && player.isAlive()) {
            leaving = false;
            counter += direction;
            Vec3 center = getBoundingBox().getCenter();
            if (attackTimer >= 40) {
                if (attackTimer == 40) {
                    chargeOrigin = center;
                }
                Vec3 outward = center.subtract(savedTarget).normalize();
                Vec3 target = chargeOrigin.add(outward.scale(Math.sin(Math.toRadians((attackTimer - 40) * 4)) * 120 / 32));
                velocity = towards(target);
                setRoll(getRoll() + 12 * direction);
                if (attackTimer >= 85) {
                    attackTimer = -1;
                    velocity = outward.scale(-12);
                    acceleration = velocity;
                } else {
                    if (attackTimer < 80) {
                        savedTarget = player.getBoundingBox().getCenter();
                    }
                    attackTimer++;
                }
            } else if (attackTimer >= 0) {
                int delay = Math.abs((int) angle);
                double radius = (320 + delay * 1.1 - Math.abs(counter)) / 32;
                double rotation = Math.toRadians(angle + counter * 1.5 + direction * 22.5);
                Vec3 offset = new Vec3(Math.cos(rotation), -Math.sin(rotation), 0).scale(radius).yRot((float) Math.toRadians(Math.abs(angle)));
                velocity = velocity.scale(0.3).add(towards(player.getBoundingBox().getCenter().add(offset)));
                setRoll(getRoll() + 36 * direction);
                if (Math.abs(counter) > delay) {
                    attackTimer++;
                }
            } else {
                attackTimer--;
                if (attackTimer < -8) {
                    SlimerSpear.spawn(boss, center, velocity);
                    discard();
                } else {
                    velocity = acceleration;
                    acceleration = acceleration.add(acceleration.normalize().scale(0.5));
                    setRoll(getRoll() + 24 * direction);
                }
            }
        } else {
            leaving = true;
            despawn++;
            velocity = velocity.multiply(0.99, 1, 0.99).add(0, 0.2, 0);
            setRoll(getRoll() + 24 * direction);
            if (despawn >= 100) {
                discard();
            }
        }
        if (!isRemoved()) {
            var previous = getBoundingBox();
            setPos(position().add(velocity.scale(1.0 / 32)));
            setYRot(PathNode.yawFromDirection(velocity));
            setXRot(PathNode.pitchFromDirection(velocity));
            if (!leaving && attackTimer >= 40) {
                AstrageldonCombat.hurtPlayers(this, this, (float) getAttributeValue(Attributes.ATTACK_DAMAGE), previous, target -> {
                });
            }
        }
    }

    private Vec3 towards(Vec3 target) {
        Vec3 difference = target.subtract(getBoundingBox().getCenter()).scale(32);
        double distance = difference.length();
        return difference.normalize().scale(Math.min(18 + distance * 0.005, distance));
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return false;
    }

    @Override
    public void die(@NotNull DamageSource source) {
        setHealth(1);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (bossId != null) {
            tag.putUUID("Boss", bossId);
        }
        tag.putFloat("Angle", angle);
        tag.putInt("Counter", counter);
        tag.putInt("AttackTimer", attackTimer);
        tag.putInt("Despawn", despawn);
        tag.putBoolean("Leaving", leaving);
        putVector(tag, "Velocity", velocity);
        putVector(tag, "Acceleration", acceleration);
        putVector(tag, "SavedTarget", savedTarget);
        putVector(tag, "ChargeOrigin", chargeOrigin);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Boss")) {
            bossId = tag.getUUID("Boss");
        }
        angle = tag.getFloat("Angle");
        counter = tag.getInt("Counter");
        attackTimer = tag.getInt("AttackTimer");
        despawn = tag.getInt("Despawn");
        leaving = tag.getBoolean("Leaving");
        velocity = vector(tag, "Velocity");
        acceleration = vector(tag, "Acceleration");
        savedTarget = vector(tag, "SavedTarget");
        chargeOrigin = vector(tag, "ChargeOrigin");
    }

    private static void putVector(CompoundTag tag, String name, Vec3 value) {
        tag.putDouble(name + "X", value.x);
        tag.putDouble(name + "Y", value.y);
        tag.putDouble(name + "Z", value.z);
    }

    private static Vec3 vector(CompoundTag tag, String name) {
        return new Vec3(tag.getDouble(name + "X"), tag.getDouble(name + "Y"), tag.getDouble(name + "Z"));
    }
}
