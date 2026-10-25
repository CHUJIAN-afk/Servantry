package first.summoner.common.entity;

import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.PlannedPath;
import first.lyra.common.attachmentEntity.RenderKeyframe;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.entity.IPathEntity;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.entity.astrageldon.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
    private static final double DASH_SPACING = 4;
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
    private Vec3 previousArenaCenter = Vec3.ZERO;
    private Vec3 targetArenaCenter = Vec3.ZERO;
    private Vec3 frameArenaCenter = Vec3.ZERO;
    private Vec3 previousFrameArenaCenter = Vec3.ZERO;
    private Vec3 previousFrameArenaMotion = Vec3.ZERO;
    private float frameArenaRadius;
    private float previousFrameArenaRadius;
    private float previousFrameRadiusMotion;
    private boolean arenaFrameReady;
    private float arenaRadius = 150;
    private float previousArenaRadius = 150;
    private float targetArenaRadius = 50;
    private Vec3 velocity = Vec3.ZERO;
    private boolean nebulaForm;
    private boolean contactDamage = true;
    private double platformY = Double.NaN;
    private int totalPhase2Cycles;
    private int resolveDirection = 1;
    private Vec3 dashAxis = new Vec3(1, 0, 0);
    private List<AstrageldonDash> dashes = new ArrayList<>();
    private Vec3 savedDashVelocity = Vec3.ZERO;

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
            case Phase11, Phase23, Phase25 -> -60;
            case Phase12 -> -40;
            case Phase13 -> -70;
            case Phase14 -> -90;
            case Phase15 -> -60;
            case Phase16 -> -70;
            case Phase21, Phase22 -> -120;
            case Phase24 -> -110;
            case Phase28 -> -50;
            default -> 0;
        }) * speedUp());
        attackTimer2 = phase == Phase.Phase25 ? 90 : 0;
        attackTimer3 = phase == Phase.Phase25 ? 90 : 0;
        savedDashVelocity = Vec3.ZERO;
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
        fields.field(LyraStreamCodecs.VEC_3, () -> previousArenaCenter, value -> previousArenaCenter = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> targetArenaCenter, value -> targetArenaCenter = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> arenaRadius, value -> arenaRadius = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> previousArenaRadius, value -> previousArenaRadius = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> targetArenaRadius, value -> targetArenaRadius = value);
        fields.field(LyraStreamCodecs.BOOL, () -> nebulaForm, value -> {
            nebulaForm = value;
            refreshDimensions();
        });
        fields.field(LyraStreamCodecs.DOUBLE, () -> platformY, value -> platformY = value);
        fields.field(LyraStreamCodecs.INT, () -> totalPhase2Cycles, value -> totalPhase2Cycles = value);
        fields.field(LyraStreamCodecs.INT, () -> resolveDirection, value -> resolveDirection = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> dashAxis, value -> dashAxis = value);
        fields.field(AstrageldonDash.CODEC.apply(ByteBufCodecs.list()), () -> dashes, value -> dashes = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> savedDashVelocity, value -> savedDashVelocity = value);
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
            previousArenaCenter = arenaCenter;
            previousArenaRadius = arenaRadius;
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
        PathNode from = currentNode();
        List<PathNode> nodes = new ArrayList<>(3);
        boolean airborne = false;
        for (int i = 0; i < 3; i++) {
            if (phase.ordinal() <= Phase.Phase25.ordinal()) {
                substep(player);
            } else {
                velocity = Vec3.ZERO;
            }
            if (phase.ordinal() >= Phase.Phase17.ordinal()) {
                arenaRadius = Mth.lerp(0.08f, arenaRadius, targetArenaRadius);
                arenaCenter = arenaCenter.lerp(targetArenaCenter, 0.08);
            }
            airborne |= !onGround();
            nodes.add(currentNode());
        }
        pathMovement = !onGround();
        setNoGravity(pathMovement);
        setDeltaMovement(velocity.scale(3.0 / 32));
        if (secondPhase) {
            updateArenaImmunity();
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
        contactDamage = true;
        dashes.removeIf(AstrageldonDash::finished);
        dashes.replaceAll(AstrageldonDash::update);
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
        if (slamTimer == 0 && (phase.ordinal() <= Phase.Phase16.ordinal() || phase == Phase.Phase23)) {
            if (velocity.y == 0) {
                if (phase == Phase.Phase23) {
                    summonSpiralStars();
                } else {
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
        if (phase == Phase.Phase22) {
            gravity = tickAscendWithDashes(player);
        }
        if (phase == Phase.Phase23) {
            gravity = tickPhase2StarSlam(player, gravity);
        }
        if (phase == Phase.Phase24) {
            gravity = tickCrossStarDash(player, gravity);
        }
        if (phase == Phase.Phase25) {
            gravity = tickStarShotgun(player);
        }
        if (!nebulaForm && velocity.y > -gravity * 20) {
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
        speed = Math.min(speed + difference.length() * distanceScale, difference.length());
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

    private double tickAscendWithDashes(Player player) {
        terrainCollision = false;
        velocity = velocity.scale(0.9);
        attackTimer++;
        if (attackTimer > 0) {
            if (attackTimer2 < 16 && attackTimer % 15 == 0) {
                if (attackTimer2 == 0) {
                    dashAxis = Vec3.directionFromRotation(0, player.getYRot());
                }
                int difference = attackTimer2 % 2 * 2 - 1;
                Vec3 center = targetArenaCenter.add(0, -targetArenaRadius + (160 * (attackTimer2 + 1) - attackTimer3) / 32, 0);
                Vec3 offset = dashAxis.scale(targetArenaRadius + 160f / 32).add(0, -80.0 / 32 * difference, 0);
                dashes.add(new AstrageldonDash(center.add(offset), center.subtract(offset), 1));
                if (attackTimer2 > 8) {
                    attackTimer3 += 200;
                    targetArenaCenter = targetArenaCenter.add(0, 200.0 / 32, 0);
                }
                attackTimer2++;
            }
            if (attackTimer2 > 8) {
                if (resolveAll(0, 4)) {
                    setPhase(Phase.Phase23);
                }
            } else if (!dashes.isEmpty()) {
                setCenterPosition(dashes.getFirst().start());
                if (attackTimer2 > 1) {
                    setNebulaForm(true);
                }
            }
        }
        return 0;
    }

    private boolean resolveAll(int direction, float speed) {
        boolean finished = dashes.isEmpty();
        if (!finished) {
            AstrageldonDash dash = dashes.getFirst();
            if (!dash.resolving()) {
                if (dash.counter() <= 0) {
                    resolveDirection = direction == 0 ? -resolveDirection : direction;
                    dashes.set(0, dash.resolve(speed));
                    setCenterPosition(resolveDirection < 0 ? dash.end() : dash.start());
                    if (phase == Phase.Phase22) {
                        int count = dashWingCount();
                        for (int i = -count; i <= count; i++) {
                            if (i != 0) {
                                AstrageldonAfterimage.spawn(this, dash, i * DASH_SPACING, resolveDirection);
                            }
                        }
                    }
                }
            } else {
                float progress = dash.progress(resolveDirection);
                Vec3 center = dash.start().lerp(dash.end(), progress);
                Vec3 outward = center.subtract(getBoundingBox().getCenter());
                savedDashVelocity = outward.scale(32);
                Vec3 movement = outward.scale(0.2);
                for (int i = 0; i < 5; i++) {
                    AABB previous = getBoundingBox();
                    setPos(position().add(movement));
                    hurtContact(previous);
                }
                if (phase == Phase.Phase22 && progress >= 0.2f && progress <= 0.8f) {
                    int count = dashWingCount();
                    for (int wing = -count; wing <= count; wing++) {
                        Vec3 position = dash.formationPosition(progress, resolveDirection, wing * DASH_SPACING);
                        for (int i = 1; i <= 2; i++) {
                            Vec3 spawn = position.subtract(outward.scale(0.33 * i)).add(randomSphere(80.0 / 32));
                            Vec3 motion = randomSphere(1).multiply(4, 3, 4).add(-outward.x * 32 * 0.015, 5.5, -outward.z * 32 * 0.015);
                            NebulaResidue.spawn(this, spawn, motion);
                        }
                    }
                }
                if (phase == Phase.Phase24 && progress >= 0.2f && progress <= 0.8f) {
                    spawnCrossStar(center.subtract(outward.scale(0.5)), savedDashVelocity);
                }
            }
        }
        return finished;
    }

    private int dashWingCount() {
        return (int) Math.ceil((getArenaBoundaryRadius() - 72.0 / 32) / DASH_SPACING);
    }

    public @Nullable AstrageldonDash getResolvingDash() {
        return !dashes.isEmpty() && dashes.getFirst().resolving() ? dashes.getFirst() : null;
    }

    private Vec3 randomSphere(double radius) {
        return new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize()
                .scale(Math.cbrt(random.nextDouble()) * radius);
    }

    private double tickPhase2StarSlam(Player player, double gravity) {
        targetArenaRadius = 1000f / 32;
        if (attackTimer < 20) {
            contactDamage = false;
            if (nebulaForm) {
                attackTimer3++;
                if (attackTimer3 >= 70) {
                    float blend = Math.min((attackTimer3 - 70) / 70, 1);
                    double height = (350 - 90 * Math.cos(Math.toRadians(attackTimer3))) / 32;
                    flyTowards(player.getBoundingBox().getCenter().add(0, height, 0), 0.25 + 0.75 * blend, 32, 0.002);
                    if (attackTimer3 >= 140) {
                        startSlam();
                        setNebulaForm(false);
                    }
                } else {
                    Vec3 target = attackTimer3 > 30 ? player.getBoundingBox().getCenter().add(0, 320.0 / 32, 0)
                            : getBoundingBox().getCenter().add(0, 1600.0 / 32, 0);
                    flyTowards(target, 0.25 + 0.75 * Math.min(attackTimer3 / 60, 1), 16, 0);
                }
            } else {
                attackTimer++;
            }
        } else {
            if (velocity.y == 0 || attackTimer > 0) {
                if (attackTimer2 < 1) {
                    attackTimer2++;
                }
                attackTimer++;
            }
            if (attackTimer == 36) {
                attackTimer++;
                jumpAt(player, 15.5 + attackTimer2 * 4, 1.1, 1, 5 + attackTimer2 * 2);
            }
            if (attackTimer >= 36) {
                velocity = velocity.multiply(1.05, 1, 1.05);
                if (velocity.y == 0) {
                    attackTimer = 20;
                    attackTimer2++;
                }
                if (attackTimer >= 37) {
                    if (attackTimer == 37) {
                        startSlam();
                    }
                    if (attackTimer < 70) {
                        gravity -= 0.5;
                    }
                    velocity = velocity.multiply(1, 0.985, 1);
                }
            }
            if (attackTimer2 >= 4) {
                setPhase(Phase.Phase24);
            }
        }
        return gravity;
    }

    private void startSlam() {
        platformY = Double.NaN;
        if (slamTimer < 0) {
            slamTimer = 35;
        }
    }

    private void summonSpiralStars() {
        double speed = 2 + 0.3 * totalPhase2Cycles;
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI / 8;
            AstrageldonStar2.spawn(this, new Vec3(Math.cos(angle), 0, Math.sin(angle)).scale(speed), attackTimer2 % 2 * 2 - 1);
        }
    }

    private double tickCrossStarDash(Player player, double gravity) {
        terrainCollision = false;
        attackTimer++;
        if (!nebulaForm) {
            targetArenaRadius = 1000f / 32;
            if (attackTimer == -109) {
                dashAxis = Vec3.directionFromRotation(0, getYRot());
            }
            attackTimer2 = getBoundingBox().getCenter().subtract(arenaCenter).dot(dashAxis) > 0 ? -1 : 1;
            if (attackTimer == -30) {
                velocity = velocity.add(0, 10, 0);
            } else if (attackTimer >= 0) {
                setNebulaForm(true);
            } else {
                gravity = -0.1;
            }
        } else {
            if (attackTimer > 10 && attackTimer < 20) {
                velocity = velocity.scale(0.9);
            }
            if (attackTimer > 20) {
                if (attackTimer < 50) {
                    velocity = velocity.multiply(1, 0.7, 1).scale(0.9825)
                            .add(dashAxis.scale(attackTimer2 * Math.sin(Math.toRadians(attackTimer * 9)) * 2.4));
                } else if (attackTimer < 110) {
                    velocity = velocity.add(dashAxis.scale(attackTimer2 * 4.5));
                } else if (attackTimer == 110) {
                    Vec3 outward = getBoundingBox().getCenter().subtract(arenaCenter).normalize();
                    setCenterPosition(arenaCenter.add(outward.scale(targetArenaRadius + 240.0 / 32)));
                }
                if (attackTimer > 962) {
                    targetArenaRadius = 1000f / 32;
                    setPhase(Phase.Phase25);
                    attackTimer = -90;
                } else if (attackTimer > 110) {
                    int timer = attackTimer - 110;
                    int progress = timer % 140;
                    if (progress == 80) {
                        velocity = velocity.scale(0.1);
                        Vec3 center = getBoundingBox().getCenter();
                        Vec3 target = player.getBoundingBox().getCenter().add(player.getDeltaMovement().scale(1.0 / 3));
                        dashes.add(new AstrageldonDash(center, center.add(target.subtract(center).normalize().scale(960.0 / 32)), 1.34f));
                    }
                    if (progress < 80) {
                        Vec3 toPlayer = player.getBoundingBox().getCenter().subtract(getBoundingBox().getCenter()).normalize();
                        velocity = velocity.add(toPlayer.scale((60 - progress) / 60.0 * 2));
                        if (timer % 3 == 0 && progress < 12 && attackTimer > 230) {
                            spawnCrossStar(getBoundingBox().getCenter().subtract(velocity.scale(0.5 / 32)), velocity);
                        }
                    }
                    if (progress >= 110) {
                        if (resolveAll(1, 3)) {
                            targetArenaRadius -= 2;
                            velocity = velocity.subtract(savedDashVelocity.scale(1.05));
                            setCenterPosition(getBoundingBox().getCenter().subtract(savedDashVelocity.scale(1.0 / 32)));
                            attackTimer += 139 - progress;
                        } else {
                            velocity = velocity.scale(0.1);
                        }
                    }
                }
            }
        }
        return gravity;
    }

    private void spawnCrossStar(Vec3 center, Vec3 motion) {
        AstrageldonStar3.spawn(this, center, motion.scale(0.1).add(randomSphere(1)), motion, attackTimer3);
        attackTimer3 += 12;
    }

    private double tickStarShotgun(Player player) {
        terrainCollision = false;
        contactDamage = false;
        targetArenaRadius = Mth.lerp(0.03f, targetArenaRadius, 600f / 32);
        if (attackTimer < 0) {
            velocity = velocity.scale(0.9);
            attackTimer++;
        } else {
            if (attackTimer == 0) {
                setCenterPosition(arenaCenter.add(0, targetArenaRadius + 240.0 / 32, 0));
            }
            attackTimer++;
            int timer = attackTimer % 180;
            int orbitTime = Math.min(attackTimer, 720);
            double angle = Math.toRadians(90 + orbitTime * 2);
            Vec3 horizontal = dashAxis.yRot((float) Math.toRadians(orbitTime * 0.25));
            Vec3 radial = horizontal.scale(Math.cos(angle)).add(0, Math.sin(angle), 0);
            Vec3 target = arenaCenter.add(radial.scale((targetArenaRadius - 1) * 0.5));
            flyTowards(target, 0.25 + 0.75 * Math.min(attackTimer / 60.0, 1), 48, 0);
            targetArenaCenter = targetArenaCenter.subtract(radial.scale(3.0 / 32)).add(0, 1.2 / 32, 0);
            if (attackTimer < 720) {
                Vec3 center = getBoundingBox().getCenter();
                Vec3 toPlayer = player.getBoundingBox().getCenter().subtract(center).normalize();
                int cooldown = Math.max(30 - totalPhase2Cycles * 5, 10);
                if (timer % cooldown == 0 && timer <= 120) {
                    NebulaResidue.spawn(this, center.add(toPlayer.scale(80.0 / 32)), toPlayer.scale(2), AstrageldonCombat.PROJECTILE_DAMAGE, 200);
                }
                if (attackTimer % 90 == 60) {
                    double heading = Math.atan2(toPlayer.z, toPlayer.x);
                    for (int i = 0; i < 16; i++) {
                        double shotAngle = heading + Math.toRadians(i * 150.0 / 16 - 90);
                        Vec3 shot = new Vec3(Math.cos(shotAngle), 0, Math.sin(shotAngle)).scale(2);
                        AstrageldonStar2.spawn(this, shot, -0.33f);
                        AstrageldonStar2.spawn(this, shot, 0.33f);
                    }
                }
            } else if (attackTimer >= 800) {
                setPhase(totalPhase2Cycles == 0 ? Phase.Phase28 : Phase.Phase27);
            }
        }
        return 0;
    }

    private void setCenterPosition(Vec3 center) {
        setPos(center.add(0, -getBbHeight() / 2, 0));
    }

    private void setNebulaForm(boolean value) {
        if (nebulaForm != value) {
            Vec3 center = getBoundingBox().getCenter();
            nebulaForm = value;
            refreshDimensions();
            setCenterPosition(center);
        }
    }

    @Override
    protected @NotNull EntityDimensions getDefaultDimensions(@NotNull Pose pose) {
        return nebulaForm ? EntityDimensions.scalable(170F / 32, 170F / 32) : super.getDefaultDimensions(pose);
    }

    private void updateArenaImmunity() {
        damageImmune = getBoundingBox().getCenter().distanceTo(arenaCenter) > targetArenaRadius + 200f / 32;
    }

    public boolean isBeyondArena(Vec3 center) {
        return hasArena() && center.distanceToSqr(arenaCenter) > Mth.square(getArenaBoundaryRadius() + 2);
    }

    public Vec3 getArenaCenter() {
        return arenaCenter;
    }

    public Vec3 getRenderArenaCenter(float partialTick) {
        Vec3 result = previousArenaCenter.lerp(arenaCenter, partialTick);
        if (arenaFrameReady) {
            Vec3 motion = frameArenaCenter.subtract(previousFrameArenaCenter);
            // 延续上一段末速度，避免同步更新在 tick 交界处产生速度折点。
            double blend = partialTick * (1 - partialTick) * (1 - partialTick);
            result = previousFrameArenaCenter.lerp(frameArenaCenter, partialTick).add(previousFrameArenaMotion.subtract(motion).scale(blend));
        }
        return result;
    }

    public void captureArenaFrame() {
        if (hasArena()) {
            previousFrameArenaMotion = arenaFrameReady ? frameArenaCenter.subtract(previousFrameArenaCenter) : Vec3.ZERO;
            previousFrameRadiusMotion = arenaFrameReady ? frameArenaRadius - previousFrameArenaRadius : 0;
            previousFrameArenaCenter = arenaFrameReady ? frameArenaCenter : arenaCenter;
            previousFrameArenaRadius = arenaFrameReady ? frameArenaRadius : arenaRadius;
            frameArenaCenter = previousFrameArenaCenter.lerp(arenaCenter, 0.5);
            frameArenaRadius = Mth.lerp(0.5f, previousFrameArenaRadius, arenaRadius);
            arenaFrameReady = true;
        } else {
            arenaFrameReady = false;
        }
    }

    public double getArenaSyncMargin() {
        double smoothing = arenaFrameReady ? arenaCenter.distanceTo(frameArenaCenter) + Math.abs(arenaRadius - frameArenaRadius) : 0;
        return 2 + arenaCenter.distanceTo(previousArenaCenter) * 3 + Math.abs(arenaRadius - previousArenaRadius) * 3 + smoothing;
    }

    public float getArenaRadius() {
        return arenaRadius;
    }

    public float getArenaBoundaryRadius() {
        return arenaRadius + ARENA_EDGE;
    }

    public float getRenderArenaRadius(float partialTick) {
        float result = Mth.lerp(partialTick, previousArenaRadius, arenaRadius);
        if (arenaFrameReady) {
            float motion = frameArenaRadius - previousFrameArenaRadius;
            float blend = partialTick * (1 - partialTick) * (1 - partialTick);
            result = Mth.lerp(partialTick, previousFrameArenaRadius, frameArenaRadius) + (previousFrameRadiusMotion - motion) * blend;
        }
        return result + ARENA_EDGE;
    }

    public boolean hasArena() {
        return secondPhase && isAlive();
    }

    public boolean usesSecondPhaseMusic() {
        return phase.ordinal() >= Phase.Phase17.ordinal();
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
        boolean grounded = constrainBody(player);
        Vec3 movement = velocity.scale(1.0 / 160);
        boolean collide = terrainCollision && !nebulaForm && (!secondPhase || phase.ordinal() <= Phase.Phase22.ordinal())
                && velocity.y <= 0 && player.getY() + 2 > getY();
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
            hurtContact(previous);
        }
        setOnGround(grounded);
    }

    private boolean constrainBody(Player player) {
        boolean grounded = false;
        if (secondPhase) {
            if (nebulaForm || terrainCollision && phase.ordinal() >= Phase.Phase23.ordinal()) {
                Vec3 offset = getBoundingBox().getCenter().subtract(arenaCenter);
                double radius = targetArenaRadius + (nebulaForm ? 400.0 : 40.0) / 32;
                if (offset.length() > radius) {
                    Vec3 normal = offset.normalize();
                    setCenterPosition(arenaCenter.add(normal.scale(radius)));
                    if (!nebulaForm) {
                        velocity = velocity.subtract(normal.scale(Math.max(velocity.dot(normal), 0)));
                    }
                }
                if (!nebulaForm && (!Double.isNaN(platformY) || velocity.y < 0
                        && (getY() < player.getY() || getY() < arenaCenter.y - arenaRadius + 40.0 / 32))) {
                    // 虚拟落脚面只停止 Boss 下落，不放置方块或影响玩家。
                    if (Double.isNaN(platformY)) {
                        platformY = getY();
                    }
                    velocity = new Vec3(velocity.x, 0, velocity.z);
                    grounded = true;
                }
            } else {
                platformY = Double.NaN;
            }
        }
        return grounded;
    }

    private void hurtContact(AABB previous) {
        if (contactDamage) {
            float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
            if (nebulaForm) {
                AstrageldonCombat.hurtPlayersSphere(this, this, damage, 72.0 / 32, AstrageldonCombat::armorCrunch);
            } else {
                AstrageldonCombat.hurtPlayers(this, this, damage, previous, AstrageldonCombat::armorCrunch);
            }
        }
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
            previousArenaCenter = arenaCenter;
            targetArenaCenter = arenaCenter;
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
        state.putDouble("TargetArenaX", targetArenaCenter.x);
        state.putDouble("TargetArenaY", targetArenaCenter.y);
        state.putDouble("TargetArenaZ", targetArenaCenter.z);
        state.putFloat("ArenaRadius", arenaRadius);
        state.putFloat("TargetArenaRadius", targetArenaRadius);
        state.putDouble("VelocityX", velocity.x);
        state.putDouble("VelocityY", velocity.y);
        state.putDouble("VelocityZ", velocity.z);
        state.putBoolean("NebulaForm", nebulaForm);
        state.putDouble("PlatformY", platformY);
        state.putInt("TotalPhase2Cycles", totalPhase2Cycles);
        state.putInt("ResolveDirection", resolveDirection);
        state.putDouble("DashAxisX", dashAxis.x);
        state.putDouble("DashAxisY", dashAxis.y);
        state.putDouble("DashAxisZ", dashAxis.z);
        state.putDouble("SavedDashVelocityX", savedDashVelocity.x);
        state.putDouble("SavedDashVelocityY", savedDashVelocity.y);
        state.putDouble("SavedDashVelocityZ", savedDashVelocity.z);
        ListTag dashTags = new ListTag();
        dashes.forEach(dash -> dashTags.add(dash.save()));
        state.put("Dashes", dashTags);
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
            previousArenaCenter = arenaCenter;
            targetArenaCenter = state.contains("TargetArenaX")
                    ? new Vec3(state.getDouble("TargetArenaX"), state.getDouble("TargetArenaY"), state.getDouble("TargetArenaZ")) : arenaCenter;
            arenaRadius = state.contains("ArenaRadius") ? state.getFloat("ArenaRadius") : 150;
            previousArenaRadius = arenaRadius;
            targetArenaRadius = state.contains("TargetArenaRadius") ? state.getFloat("TargetArenaRadius") : 50;
            velocity = new Vec3(state.getDouble("VelocityX"), state.getDouble("VelocityY"), state.getDouble("VelocityZ"));
            nebulaForm = state.getBoolean("NebulaForm");
            platformY = state.contains("PlatformY") ? state.getDouble("PlatformY") : Double.NaN;
            totalPhase2Cycles = state.getInt("TotalPhase2Cycles");
            resolveDirection = state.contains("ResolveDirection") ? state.getInt("ResolveDirection") : 1;
            dashAxis = state.contains("DashAxisX") ? new Vec3(state.getDouble("DashAxisX"), state.getDouble("DashAxisY"), state.getDouble("DashAxisZ")) : new Vec3(1, 0, 0);
            savedDashVelocity = new Vec3(state.getDouble("SavedDashVelocityX"), state.getDouble("SavedDashVelocityY"), state.getDouble("SavedDashVelocityZ"));
            dashes.clear();
            ListTag dashTags = state.getList("Dashes", 10);
            for (int i = 0; i < dashTags.size(); i++) {
                dashes.add(AstrageldonDash.load(dashTags.getCompound(i)));
            }
            refreshDimensions();
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
