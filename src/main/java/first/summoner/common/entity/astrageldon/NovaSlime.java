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
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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

public class NovaSlime extends Monster implements IPathEntity {

    private UUID bossId;
    private Vec3 offset = Vec3.ZERO;
    private Vec3 velocity = Vec3.ZERO;
    private int timer;
    private int slamTimer = -1;
    private int despawn;
    private boolean descending = true;
    private boolean damageImmune = true;

    public NovaSlime(EntityType<? extends NovaSlime> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 72)
                .add(Attributes.ATTACK_DAMAGE, 16)
                .add(Attributes.ARMOR, 30)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.FOLLOW_RANGE, 200);
    }

    public static void spawn(Astrageldon boss, Vec3 offset, double height) {
        NovaSlime slime = new NovaSlime(SummonerEntityRegister.NOVA_SLIME.get(), boss.level());
        slime.bossId = boss.getUUID();
        slime.offset = offset.add(0, height, 0);
        slime.timer = -boss.getRandom().nextInt(40);
        slime.setPos(boss.getBoundingBox().getCenter().add(0, -slime.getBbHeight() / 2, 0));
        slime.setTarget(boss.getTarget());
        boss.level().addFreshEntity(slime);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        IPathEntity.super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.OPTIONAL_UUID, () -> Optional.ofNullable(bossId), value -> bossId = value.orElse(null));
        fields.field(LyraStreamCodecs.VEC_3, () -> offset, value -> offset = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> velocity, value -> velocity = value);
        fields.field(LyraStreamCodecs.INT, () -> timer, value -> timer = value);
        fields.field(LyraStreamCodecs.INT, () -> slamTimer, value -> slamTimer = value);
        fields.field(LyraStreamCodecs.INT, () -> despawn, value -> despawn = value);
        fields.field(LyraStreamCodecs.BOOL, () -> descending, value -> descending = value);
        fields.field(LyraStreamCodecs.BOOL, () -> damageImmune, value -> damageImmune = value);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && isAlive()) {
            if (getTarget() == null || !getTarget().isAlive() || getTarget().isSpectator() || distanceToSqr(getTarget()) > 40000) {
                setTarget(level().getNearestPlayer(this, 200));
            }
            PathNode from = node();
            List<RenderKeyframe> frames = new ArrayList<>(2);
            for (int i = 0; i < 3 && !isRemoved(); i++) {
                if (getTarget() instanceof Player player && player.isAlive() && !player.isSpectator()) {
                    substep(player);
                } else {
                    despawn++;
                    velocity = velocity.lerp(new Vec3(0, 40, 0), 0.1);
                    setPos(position().add(velocity.scale(1.0 / 32)));
                    if (despawn >= 30) {
                        discard();
                    }
                }
                if (i < 2) {
                    frames.add(new RenderKeyframe((i + 1) / 3f, node()));
                }
            }
            if (!isRemoved()) {
                PathNode to = node();
                setCurrentPathNode(from);
                setPlannedPath(new PlannedPath("nova_slime", List.of(to)).render(0, frames));
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

    private void substep(Player player) {
        if (slamTimer > 0) {
            slamTimer--;
        } else if (slamTimer < -1 && slamTimer > -60) {
            slamTimer -= 2;
        }
        double gravity = 0.5;
        if (slamTimer == 0) {
            if (velocity.y == 0) {
                slamTimer = -2;
            } else {
                gravity += 2.5;
            }
        }
        if (descending) {
            damageImmune = true;
            if (timer < 155) {
                gravity = 0;
            }
            timer++;
            if (timer < 155) {
                Vec3 center = getBoundingBox().getCenter();
                Vec3 target = player.getBoundingBox().getCenter().add(offset);
                float blend = Mth.clamp(timer / 240f, 0.075f, 0.325f);
                setPos(center.add(target.subtract(center).multiply(blend * 0.5, blend, blend * 0.5)).add(0, -getBbHeight() / 2, 0));
            }
            if (timer == 160) {
                slamTimer = 50;
            }
            if (timer >= 180) {
                timer = -60;
                descending = false;
            }
        } else {
            damageImmune = false;
            if (velocity.y == 0 || timer > 0) {
                timer++;
            }
            if (velocity.y != 0) {
                velocity = velocity.multiply(1.06, 1, 1.06);
            } else if (timer == 20) {
                timer++;
                Vec3 direction = player.getBoundingBox().getCenter().subtract(getBoundingBox().getCenter()).normalize();
                velocity = direction.multiply(10.5, 10.5 * 0.7, 10.5).add(0, 14, 0);
                setYRot(PathNode.yawFromDirection(direction));
            }
            if (timer >= 20 && velocity.y == 0) {
                timer = -100 - random.nextInt(21);
            }
        }
        if (velocity.y > -gravity * 20) {
            velocity = velocity.add(0, -gravity, 0);
        }
        moveSubstep(player);
    }

    private void moveSubstep(Player player) {
        Vec3 movement = velocity.scale(1.0 / 160);
        boolean collide = velocity.y <= 0 && player.getBoundingBox().maxY > getY();
        boolean grounded = false;
        for (int i = 0; i < 5; i++) {
            var previous = getBoundingBox();
            Vec3 resolved = collide ? Entity.collideBoundingBox(this, movement, previous, level(), List.of()) : movement;
            setPos(position().add(resolved));
            if (resolved.x != movement.x) {
                velocity = new Vec3(0, velocity.y, velocity.z);
            }
            if (resolved.z != movement.z) {
                velocity = new Vec3(velocity.x, velocity.y, 0);
            }
            if (resolved.y != movement.y && movement.y < 0) {
                velocity = new Vec3(velocity.x, 0, velocity.z);
                grounded = true;
            }
            AstrageldonCombat.hurtPlayers(this, this, (float) getAttributeValue(Attributes.ATTACK_DAMAGE), previous, AstrageldonCombat::astralBlight);
        }
        setOnGround(grounded);
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return !damageImmune && super.hurt(source, amount);
    }

    @Override
    public void die(@NotNull DamageSource source) {
        if (bossId != null && level() instanceof ServerLevel server && server.getEntity(bossId) instanceof Astrageldon boss && boss.isAlive()) {
            setHealth(1);
        } else {
            super.die(source);
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, @NotNull DamageSource source) {
        return false;
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
        tag.putDouble("OffsetX", offset.x);
        tag.putDouble("OffsetY", offset.y);
        tag.putDouble("OffsetZ", offset.z);
        tag.putDouble("VelocityX", velocity.x);
        tag.putDouble("VelocityY", velocity.y);
        tag.putDouble("VelocityZ", velocity.z);
        tag.putInt("Timer", timer);
        tag.putInt("SlamTimer", slamTimer);
        tag.putInt("Despawn", despawn);
        tag.putBoolean("Descending", descending);
        tag.putBoolean("DamageImmune", damageImmune);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Boss")) {
            bossId = tag.getUUID("Boss");
        }
        offset = new Vec3(tag.getDouble("OffsetX"), tag.getDouble("OffsetY"), tag.getDouble("OffsetZ"));
        velocity = new Vec3(tag.getDouble("VelocityX"), tag.getDouble("VelocityY"), tag.getDouble("VelocityZ"));
        timer = tag.getInt("Timer");
        slamTimer = tag.getInt("SlamTimer");
        despawn = tag.getInt("Despawn");
        descending = tag.getBoolean("Descending");
        damageImmune = tag.getBoolean("DamageImmune");
    }
}
