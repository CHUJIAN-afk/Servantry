package first.summoner.common.entity;

import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.PlannedPath;
import first.lyra.common.attachmentEntity.RenderKeyframe;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.entity.IPathEntity;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.entity.astrageldon.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class Astrageldon extends Monster implements GeoAnimatable, IPathEntity {

    private static final float ARENA_EDGE = 156f / 32;
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private Phase phase = Phase.Phase11;
    private int attackTimer = -120;
    private int attackTimer2;
    private float attackTimer3;
    private int slamTimer = 50;
    private int despawn;
    private boolean pathMovement;
    private boolean damageImmune;
    private boolean terrainCollision = true;
    private boolean secondPhase;
    private Vec3 arenaCenter = Vec3.ZERO;
    private float arenaRadius = 150;
    private float previousArenaRadius = 150;
    private float targetArenaRadius = 50;
    private Vec3 velocity = Vec3.ZERO;

    public Astrageldon(EntityType<Astrageldon> entityType, Level level) {
        super(entityType, level);
        setNoGravity(false);
        this.noPhysics = false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 27.5)
                .add(Attributes.ARMOR, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 200.0);
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
        damageImmune = false;
        attackTimer = (int) ((switch (phase) {
            case Phase11 -> -60;
            case Phase12 -> -40;
            case Phase13 -> -70;
            case Phase14 -> -90;
            case Phase15 -> -60;
            case Phase16 -> -70;
            case Phase21, Phase22 -> -120;
            default -> 0;
        }) * speedUp());
        attackTimer2 = 0;
        attackTimer3 = 0;
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        IPathEntity.super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.INT, () -> phase.ordinal(), value -> phase = Phase.values()[value]);
        fields.field(LyraStreamCodecs.INT, () -> attackTimer, value -> attackTimer = value);
        fields.field(LyraStreamCodecs.INT, () -> attackTimer2, value -> attackTimer2 = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> attackTimer3, value -> attackTimer3 = value);
        fields.field(LyraStreamCodecs.INT, () -> slamTimer, value -> slamTimer = value);
        fields.field(LyraStreamCodecs.INT, () -> despawn, value -> despawn = value);
        fields.field(LyraStreamCodecs.BOOL, () -> pathMovement, value -> {
            pathMovement = value;
            setNoGravity(value);
        });
        fields.field(LyraStreamCodecs.VEC_3, () -> velocity, value -> velocity = value);
        fields.field(LyraStreamCodecs.BOOL, () -> damageImmune, value -> damageImmune = value);
        fields.field(LyraStreamCodecs.BOOL, () -> secondPhase, value -> secondPhase = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> arenaCenter, value -> arenaCenter = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> arenaRadius, value -> arenaRadius = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> previousArenaRadius, value -> previousArenaRadius = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> targetArenaRadius, value -> targetArenaRadius = value);
    }

    @Override
    protected void registerGoals() {

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && isAlive()) {
            removeAllEffects();
            if (getTarget() == null || !getTarget().isAlive() || getTarget().isSpectator() || distanceToSqr(getTarget()) > 40000) {
                setTarget(level().getNearestPlayer(this, 200));
            }
            if (getTarget() instanceof Player player && player.isAlive() && !player.isSpectator() && distanceToSqr(player) <= 40000) {
                tickCombat(player);
            } else {
                despawn += 3;
                if (despawn >= 360) {
                    discard();
                }
            }
        }
        tickPath();
    }

    private void tickCombat(Player player) {
        previousArenaRadius = arenaRadius;
        PathNode from = currentNode();
        List<PathNode> nodes = new ArrayList<>(3);
        boolean airborne = false;
        for (int i = 0; i < 3; i++) {
            if (phase.ordinal() <= Phase.Phase21.ordinal()) {
                substep(player);
            } else {
                velocity = Vec3.ZERO;
            }
            if (phase.ordinal() >= Phase.Phase17.ordinal()) {
                arenaRadius = Mth.lerp(0.08f, arenaRadius, targetArenaRadius);
            }
            airborne |= !onGround();
            nodes.add(currentNode());
        }
        pathMovement = !onGround();
        setNoGravity(pathMovement);
        setDeltaMovement(velocity.scale(3.0 / 32));
        if (secondPhase) {
            constrainArena();
        }
        if (airborne) {
            List<RenderKeyframe> frames = List.of(new RenderKeyframe(1f / 3, nodes.get(0)), new RenderKeyframe(2f / 3, nodes.get(1)));
            setCurrentPathNode(from);
            setPlannedPath(new PlannedPath("astrageldon", List.of(nodes.get(2))).render(0, frames));
        } else {
            setPlannedPath(null);
        }
    }

    private PathNode currentNode() {
        return new PathNode(position(), getYRot(), getXRot(), getRoll());
    }

    private void substep(Player player) {
        terrainCollision = true;
        if (slamTimer > 0) {
            slamTimer--;
        } else if (slamTimer < 0 && slamTimer > -60) {
            slamTimer -= 2;
        }
        velocity = velocity.multiply(0.93, 1, 0.93);
        if (velocity.horizontalDistance() < 0.1) {
            velocity = new Vec3(0, velocity.y, 0);
        }
        double gravity = 0.5;
        if (slamTimer == 0 && phase.ordinal() <= Phase.Phase16.ordinal()) {
            if (velocity.y == 0) {
                groundBlades(switch (phase) {
                    case Phase11 -> 55;
                    case Phase13, Phase16 -> 40;
                    case Phase14 -> 30 + attackTimer2 * 10;
                    default -> 20 + attackTimer2 * 10;
                });
                if (phase == Phase.Phase14 && attackTimer2 < 3) {
                    summonSlimeRain();
                }
                if (phase != Phase.Phase15 && getHealth() < getMaxHealth() * 0.75f) {
                    groundRocks(player);
                }
                slamTimer = -1;
            } else {
                gravity += 2.5;
            }
        }
        if (phase == Phase.Phase11) {
            tickIdle(player);
        }
        if (phase == Phase.Phase12) {
            gravity = tickJumpSlam(player, gravity);
        }
        if (phase == Phase.Phase13) {
            gravity = tickScatterStars(player, gravity);
        }
        if (phase == Phase.Phase14) {
            gravity = tickSlimeRain(player, gravity);
        }
        if (phase == Phase.Phase15) {
            gravity = tickRevengeanceJumpSlam(player, gravity);
        }
        if (phase == Phase.Phase16) {
            gravity = tickOrbitingRocks(player, gravity);
        }
        if (phase == Phase.Phase17) {
            gravity = tickTransition(player);
        }
        if (phase == Phase.Phase21) {
            gravity = tickSummonAndAscend(player);
        }
        if (velocity.y > -gravity * 20) {
            velocity = velocity.add(0, -gravity, 0);
        }
        moveSubstep(player);
    }

    private void tickIdle(Player player) {
        if (velocity.y == 0 || attackTimer > 0) {
            attackTimer++;
        }
        if (attackTimer == 20) {
            attackTimer++;
            jumpAt(player, 15.5, 1.25, 0.75, 14);
        }
        velocity = velocity.multiply(1.04, 1, 1.04);
        if (attackTimer >= 20 && velocity.y == 0) {
            attackTimer2++;
            attackTimer = (int) (-67 * speedUp());
        }
        if (attackTimer2 >= 2) {
            setPhase(Phase.Phase12);
        }
    }

    private double tickJumpSlam(Player player, double gravity) {
        if (velocity.y == 0 || attackTimer > 0) {
            attackTimer++;
        }
        if (attackTimer == 16) {
            attackTimer++;
            jumpAt(player, 14.5 + attackTimer2 * 4, 1.25, 0.75, 9 + attackTimer2 * 3);
        }
        if (attackTimer >= 16) {
            velocity = velocity.multiply(1.05, 1, 1.05);
            if (velocity.y == 0) {
                attackTimer = (int) (14 * (1 - speedUp()));
                attackTimer2++;
            }
            if (attackTimer >= 17) {
                if (attackTimer == 17) {
                    slamTimer = 50;
                }
                if (attackTimer < 70) {
                    gravity -= 0.5;
                }
                velocity = velocity.multiply(1, 0.985, 1);
            }
        }
        if (attackTimer2 >= 3) {
            setPhase(attackTimer3 == -1 ? Phase.Phase16 : Phase.Phase13);
        }
        return gravity;
    }

    private void jumpAt(Player player, double speed, double horizontalScale, double verticalScale, double lift) {
        Vec3 direction = player.getBoundingBox().getCenter().subtract(getBoundingBox().getCenter()).normalize();
        velocity = new Vec3(direction.x * speed * horizontalScale, direction.y * speed * verticalScale + lift, direction.z * speed * horizontalScale);
        setYRot(PathNode.yawFromDirection(direction));
        setOnGround(false);
        setNoGravity(true);
    }

    private double tickScatterStars(Player player, double gravity) {
        double speed = 16;
        Vec3 target = player.getBoundingBox().getCenter().add(0, 360.0 / 32, 0);
        float flying = Mth.clamp(attackTimer / 30f, 0, 1);
        if (attackTimer == 0) {
            attackTimer++;
            double jumpSpeed = 16.5 + attackTimer2 * 4;
            velocity = target.subtract(getBoundingBox().getCenter()).normalize().multiply(-jumpSpeed * 1.25, -jumpSpeed * 0.75, -jumpSpeed * 1.25);
        } else if (attackTimer < 0) {
            attackTimer++;
        } else if (attackTimer2 < 4) {
            attackTimer++;
            double angle = Math.toRadians(attackTimer3 * 3);
            double sway = Math.sin(Math.toRadians(attackTimer * 4)) * 20 + 8;
            target = target.add(-Math.sin(angle) * 240 / 32, -(Math.cos(angle) * 24 + sway) / 32, Math.cos(angle) * 240 / 32);
            if (attackTimer >= 100) {
                int progress = attackTimer % 100;
                if (progress == 0) {
                    summonSphericalStars(scatterStarCount());
                }
                if (progress >= 40) {
                    double sin = Math.sin(Math.toRadians((progress - 40) * 3));
                    velocity = velocity.scale(0.9);
                    speed = Math.max(speed * (1 - sin), 0);
                    attackTimer3 += (float) (1 - sin);
                } else {
                    attackTimer3++;
                }
                if (progress == 99) {
                    attackTimer2++;
                }
            } else {
                attackTimer3++;
            }
        } else {
            target = target.add(0, 280.0 / 32, 0);
            if (attackTimer2 == 4) {
                slamTimer = 50;
            }
            attackTimer2++;
            if (attackTimer2 < 54) {
                gravity -= 0.5;
            }
            if (velocity.y == 0 && slamTimer < 0) {
                setPhase(Phase.Phase14);
            }
        }
        if (attackTimer >= 0 && attackTimer2 < 30) {
            gravity *= 0.5;
            flyTowards(target, 0.175 + 0.825 * flying, speed, 0);
        } else if (attackTimer2 >= 30) {
            velocity = velocity.add(0, 0.05, 0);
        }
        return gravity;
    }

    private double tickSlimeRain(Player player, double gravity) {
        if (attackTimer2 >= 3) {
            attackTimer++;
            if (attackTimer >= 40) {
                if (attackTimer < 405) {
                    gravity -= 0.5;
                }
                if (attackTimer > 60) {
                    if (attackTimer < 240) {
                        float flying = Math.min((attackTimer - 60) / 60f, 1);
                        flyTowards(player.getBoundingBox().getCenter().add(0, 1200.0 / 32, 0), 0.25 + 0.75 * flying, 32, 0);
                        damageImmune = true;
                    } else {
                        if (attackTimer < 285) {
                            velocity = velocity.scale(0.9);
                        }
                        if (attackTimer == 240) {
                            slamTimer = 50;
                        }
                        gravity += 0.25;
                        if (velocity.y == 0 && slamTimer < 0) {
                            setPhase(Phase.Phase15);
                        }
                    }
                } else {
                    velocity = velocity.add(0, 8, 0);
                }
            }
        } else {
            if (velocity.y == 0 || attackTimer > 0) {
                attackTimer++;
            }
            if (attackTimer == 9) {
                attackTimer++;
                velocity = velocity.add(0, 5 + attackTimer2 * 7.5, 0);
            }
            if (attackTimer >= 10) {
                velocity = velocity.multiply(0.95, 1, 0.95);
                if (velocity.y == 0) {
                    attackTimer2++;
                    attackTimer = attackTimer2 >= 3 ? 20 : 8;
                }
                if (attackTimer >= 10) {
                    if (attackTimer == 10) {
                        slamTimer = 50;
                    }
                    if (attackTimer < 50) {
                        gravity -= 0.5;
                    }
                    velocity = velocity.multiply(1, 0.985, 1);
                }
            }
        }
        return gravity;
    }

    private double tickRevengeanceJumpSlam(Player player, double gravity) {
        if (attackTimer == 0) {
            summonSphericalStars(scatterStarCount());
        }
        if (velocity.y == 0 || attackTimer > 0) {
            attackTimer++;
        }
        if (attackTimer == 20) {
            attackTimer++;
            jumpAt(player, 14.5 + attackTimer2 * 5, 1.125, 0.5, 10 + attackTimer2 * 2);
        }
        if (attackTimer >= 20) {
            velocity = velocity.multiply(1.045, 1, 1.045);
            if (velocity.y == 0) {
                attackTimer2++;
                attackTimer = (int) (16 * (1 - speedUp()));
                if (attackTimer != 0 && attackTimer2 < 3) {
                    summonSphericalStars(scatterStarCount());
                }
            }
            if (attackTimer >= 21) {
                if (attackTimer == 31) {
                    slamTimer = 50;
                }
                if (attackTimer < 90) {
                    gravity -= 0.45;
                    velocity = attackTimer > 40 ? velocity.scale(0.9775) : velocity.multiply(1, 0.9875, 1);
                }
            }
        }
        if (attackTimer2 >= 3) {
            setPhase(Phase.Phase16);
        }
        return gravity;
    }

    private double tickOrbitingRocks(Player player, double gravity) {
        Vec3 center = getBoundingBox().getCenter();
        Vec3 playerCenter = player.getBoundingBox().getCenter();
        Vec3 target = playerCenter.add(0, 360.0 / 32, 0);
        float flying = Mth.clamp(attackTimer / 30f, 0, 1);
        if (attackTimer == 0) {
            attackTimer++;
            double speed = 16.5 + attackTimer2 * 4;
            velocity = target.subtract(center).normalize().multiply(-speed * 1.25, -speed * 0.75, -speed * 1.25);
        } else if (attackTimer < 0) {
            attackTimer++;
        } else if (attackTimer < 60) {
            attackTimer++;
            gravity -= 0.25;
        } else if (attackTimer2 < 4) {
            terrainCollision = false;
            double sin = Math.sin(Math.toRadians(attackTimer * 2.5));
            double radius = (380 + 60 * sin) / 32;
            target = playerCenter.add(center.subtract(playerCenter).normalize().scale(radius)).add(0, -1.5 * sin / 32, 0);
            if (attackTimer == 60) {
                summonRocks(20);
            }
            attackTimer++;
            if (attackTimer >= 540) {
                attackTimer2++;
            }
        } else {
            target = playerCenter.add(0, 600.0 / 32, 0);
            if (attackTimer2 == 4) {
                slamTimer = 50;
            }
            attackTimer2++;
            if (attackTimer2 < 54) {
                gravity -= 0.5;
            }
            if (velocity.y == 0 && slamTimer < 0) {
                setPhase(Phase.Phase11);
            }
        }
        if (attackTimer >= 0 && attackTimer2 < 30) {
            gravity *= 0.5;
            flyTowards(target, 0.175 + 0.825 * flying, 12, 0.005);
        } else if (attackTimer2 >= 30) {
            velocity = velocity.add(0, 0.05, 0);
        }
        return gravity;
    }

    private void flyTowards(Vec3 target, double blend, double speed, double distanceScale) {
        Vec3 difference = target.subtract(getBoundingBox().getCenter()).scale(32);
        speed = Math.min(speed, difference.length()) + difference.length() * distanceScale;
        velocity = velocity.scale(1 - blend * blend).add(difference.normalize().scale(blend * speed));
    }

    private void summonSphericalStars(int count) {
        double rotation = random.nextDouble() * Math.PI * 2;
        // 等面积纬度配合黄金角，避免球面极点堆积。
        for (int i = 0; i < count; i++) {
            double y = 1 - 2 * (i + 0.5) / count;
            double radius = Math.sqrt(1 - y * y);
            double angle = rotation + i * Math.PI * (3 - Math.sqrt(5));
            AstrageldonStar.spawn(this, new Vec3(Math.cos(angle) * radius, y, Math.sin(angle) * radius));
        }
    }

    private int scatterStarCount() {
        return (12 + (int) (4 * (1 - speedUp()) + 0.5f)) * 4;
    }

    private void summonRocks(int count) {
        for (int plane = 0; plane < 2; plane++) {
            for (int i = 0; i < count; i++) {
                AstraRock.spawnOrbiting(this, i * 360f / count, 1, plane == 1);
                float radius = 0.9f * (float) Math.sqrt((i + 0.5) / count);
                float angle = (float) Math.toDegrees(i * Math.PI * (3 - Math.sqrt(5)));
                AstraRock.spawnOrbiting(this, angle, radius, plane == 1);
            }
        }
    }

    private double tickTransition(Player player) {
        terrainCollision = false;
        damageImmune = true;
        slamTimer = -60;
        attackTimer++;
        setHealth(Math.min((int) (getMaxHealth() / 180f * attackTimer), getMaxHealth()));
        if (attackTimer <= 240) {
            velocity = velocity.add(0, 0.5, 0).scale(1 - 0.65 * attackTimer / 240);
        } else {
            velocity = velocity.scale(0.3);
            setPos(position().add(0, -3 * Math.sin(Math.toRadians((attackTimer - 120) * 12)) / 32, 0));
            if (attackTimer == 360) {
                for (int i = 0; i < 8; i++) {
                    double y = 1 - 2 * (i + 0.5) / 8;
                    double radius = Math.sqrt(1 - y * y);
                    double angle = i * Math.PI * (3 - Math.sqrt(5));
                    Vec3 direction = new Vec3(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
                    AstraRock.spawn(this, getBoundingBox().getCenter().add(direction.scale(3)), player.getBoundingBox().getCenter(), direction.scale(7.2));
                }
            }
            if (attackTimer >= 420) {
                setPhase(Phase.Phase21);
                setHealth(getMaxHealth());
            }
        }
        return 0;
    }

    private double tickSummonAndAscend(Player player) {
        terrainCollision = false;
        double gravity = attackTimer > 0 ? -0.1 : 0.3;
        if (attackTimer == -30) {
            for (int i = -6; i <= 6; i++) {
                if (i != 0) {
                    NovaSlimer.spawn(this, i * 60f);
                }
            }
            velocity = velocity.add(0, 10, 0);
        }
        attackTimer++;
        targetArenaRadius = 1000f / 32;
        secondPhase = true;
        if (attackTimer >= 30) {
            if (attackTimer > 50) {
                if (attackTimer < 120) {
                    float flying = Math.min((attackTimer - 50) / 50f, 1);
                    flyTowards(player.getBoundingBox().getCenter().add(0, 1600.0 / 32, 0), 0.25 + 0.75 * flying, 30, 0);
                } else {
                    setPhase(Phase.Phase22);
                }
            } else {
                velocity = velocity.add(0, 8, 0);
            }
        }
        return gravity;
    }

    private void constrainArena() {
        damageImmune = getBoundingBox().getCenter().distanceTo(arenaCenter) > targetArenaRadius + 200f / 32;
        double limit = arenaRadius + 200.0 / 32;
        for (ServerPlayer player : ((ServerLevel) level()).players()) {
            if (player.isAlive() && !player.isSpectator()) {
                Vec3 offset = player.getBoundingBox().getCenter().subtract(arenaCenter);
                double distance = offset.length();
                if (distance > getArenaBoundaryRadius() && distance < limit + 2) {
                    if (player.hurt(AstrageldonCombat.source(this, this), 16.4f)) {
                        AstrageldonCombat.interstellarCorruption(player);
                    }
                }
                if (distance > limit && distance < limit + 2250.0 / 32) {
                    Vec3 normal = offset.normalize();
                    Vec3 position = player.position().subtract(normal.scale(distance - limit));
                    player.connection.teleport(position.x, position.y, position.z, player.getYRot(), player.getXRot());
                    Vec3 motion = player.getDeltaMovement();
                    player.setDeltaMovement(motion.subtract(normal.scale(Math.max(motion.dot(normal), 0))));
                    player.hurtMarked = true;
                }
            }
        }
    }

    public boolean isBeyondArena(Vec3 center) {
        return secondPhase && center.distanceTo(arenaCenter) > getArenaBoundaryRadius();
    }

    public Vec3 getArenaCenter() {
        return arenaCenter;
    }

    public float getArenaRadius() {
        return arenaRadius;
    }

    public float getArenaBoundaryRadius() {
        return arenaRadius + ARENA_EDGE;
    }

    public float getRenderArenaRadius(float partialTick) {
        return Mth.lerp(partialTick, previousArenaRadius, arenaRadius) + ARENA_EDGE;
    }

    public boolean hasArena() {
        return secondPhase && isAlive();
    }

    private void summonSlimeRain() {
        int count = 7 + attackTimer2 / 2;
        double rotation = random.nextDouble() * Math.PI * 2;
        for (int i = 0; i < count; i++) {
            double radius = 10 * Math.sqrt((i + 0.5) / count);
            double angle = rotation + i * Math.PI * (3 - Math.sqrt(5));
            Vec3 offset = new Vec3(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            NovaSlime.spawn(this, offset, (700 + random.nextFloat() * 140) / 32.0);
        }
    }

    private void moveSubstep(Player player) {
        Vec3 movement = velocity.scale(1.0 / 160);
        boolean collide = terrainCollision && velocity.y <= 0 && player.getY() + 2 > getY();
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
            AstrageldonCombat.hurtPlayers(this, this, (float) getAttributeValue(Attributes.ATTACK_DAMAGE), previous, AstrageldonCombat::armorCrunch);
        }
        setOnGround(grounded);
    }

    private float speedUp() {
        if (phase.ordinal() >= Phase.Phase17.ordinal()) {
            return 1;
        }
        return Mth.clamp(getHealth() / getMaxHealth(), 0.1f, 1f);
    }

    @Override
    public void die(@NotNull DamageSource source) {
        if (!secondPhase) {
            long players = level().players().stream().filter(player -> player.isAlive() && !player.isSpectator()).count();
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(33600 * (1 + 0.5 * (Math.max(players, 1) - 1)));
            setHealth(1);
            arenaCenter = getBoundingBox().getCenter();
            targetArenaRadius = 4000f / 32;
            arenaRadius = 150;
            previousArenaRadius = arenaRadius;
            setPhase(Phase.Phase17);
            damageImmune = true;
            slamTimer = -60;
            setNoGravity(true);
            setOnGround(false);
            setPlannedPath(null);
        } else {
            super.die(source);
        }
    }

    private void groundBlades(int length) {
        for (int i = 0; i < 8; i++) {
            Vec3 outward = Vec3.directionFromRotation(0, getYRot() + i * 45);
            Vec3 center = position().add(outward.scale(getBbWidth() / 2 + 0.5)).add(0, -0.75, 0);
            AstraBlade.spawn(this, center, outward, length + 1);
        }
    }

    private void groundRocks(Player player) {
        Vec3 direction = Vec3.directionFromRotation(0, getYRot());
        for (int i = -2; i <= 2; i++) {
            if (i != 0) {
                Vec3 center = position().add(direction.scale(1.5 * i + getBbWidth() / 2 * Math.signum(i))).add(0, -0.75, 0);
                AstraRock.spawn(this, center, player.getBoundingBox().getCenter(), new Vec3(0, 12.5 - Math.abs(i) * 1.5, 0));
            }
        }
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        // 位移与重力由 60 Hz 子步积分，避免原版物理再推进一次。
        setDeltaMovement(Vec3.ZERO);
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
    public boolean hurt(@NotNull DamageSource source, float amount) {
        float multiplier = source.getDirectEntity() == source.getEntity() && source.getEntity() instanceof LivingEntity ? 0.8f : 1f;
        return !damageImmune && super.hurt(source, amount * 0.9f * multiplier);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        CompoundTag state = new CompoundTag();
        state.putInt("Phase", phase.ordinal());
        state.putInt("AttackTimer", attackTimer);
        state.putInt("AttackTimer2", attackTimer2);
        state.putFloat("AttackTimer3", attackTimer3);
        state.putInt("SlamTimer", slamTimer);
        state.putInt("Despawn", despawn);
        state.putBoolean("PathMovement", pathMovement);
        state.putBoolean("DamageImmune", damageImmune);
        state.putBoolean("SecondPhase", secondPhase);
        state.putDouble("ArenaX", arenaCenter.x);
        state.putDouble("ArenaY", arenaCenter.y);
        state.putDouble("ArenaZ", arenaCenter.z);
        state.putFloat("ArenaRadius", arenaRadius);
        state.putFloat("TargetArenaRadius", targetArenaRadius);
        state.putDouble("VelocityX", velocity.x);
        state.putDouble("VelocityY", velocity.y);
        state.putDouble("VelocityZ", velocity.z);
        tag.put("Astrageldon", state);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Astrageldon")) {
            CompoundTag state = tag.getCompound("Astrageldon");
            phase = Phase.values()[state.getInt("Phase")];
            attackTimer = state.getInt("AttackTimer");
            attackTimer2 = state.getInt("AttackTimer2");
            attackTimer3 = state.getFloat("AttackTimer3");
            slamTimer = state.getInt("SlamTimer");
            despawn = state.getInt("Despawn");
            pathMovement = state.getBoolean("PathMovement");
            damageImmune = state.getBoolean("DamageImmune");
            secondPhase = state.getBoolean("SecondPhase");
            arenaCenter = new Vec3(state.getDouble("ArenaX"), state.getDouble("ArenaY"), state.getDouble("ArenaZ"));
            arenaRadius = state.contains("ArenaRadius") ? state.getFloat("ArenaRadius") : 150;
            previousArenaRadius = arenaRadius;
            targetArenaRadius = state.contains("TargetArenaRadius") ? state.getFloat("TargetArenaRadius") : 50;
            velocity = new Vec3(state.getDouble("VelocityX"), state.getDouble("VelocityY"), state.getDouble("VelocityZ"));
            setNoGravity(pathMovement);
            if (phase.ordinal() <= Phase.Phase16.ordinal()) {
                getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
                setHealth(Math.min(getHealth(), 200));
            }
        }
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean addEffect(@NotNull MobEffectInstance effectInstance, @Nullable Entity entity) {
        return false;
    }

    @Override
    public void forceAddEffect(@NotNull MobEffectInstance instance, @Nullable Entity entity) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public double getTick(Object o) {
        return tickCount;
    }

    public enum Phase {
        Phase11,
        Phase12,
        Phase13,
        Phase14,
        Phase15,
        Phase16,
        Phase17,
        Phase21,
        Phase22,
        Phase23,
        Phase24,
        Phase25,
        Phase27,
        Phase28,
        Phase29,
        Phase210,
        Phase211,
    }
}
