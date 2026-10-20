package first.summoner.common.minion.minion;

import first.lyra.common.attachment.TargetCache;
import first.lyra.common.attachmentEntity.*;
import first.lyra.common.minion.Minion;
import first.lyra.utils.EasingCurve;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.minion.minion.goal.sanguineBat.SanguineBatAttackGoal;
import first.summoner.common.minion.minion.goal.sanguineBat.SanguineBatIdleGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SanguineBat extends Minion implements IEntityCollision<SanguineBat> {

    private static final EasingCurve ATTACK_CURVE = EasingCurve.bezier()
            .control(0.1f).control(0.2f).control(0.3f).control(0.4f).control(0.45f)
            .control(0.475f).control(0.5f).control(0.5f).control(0.525f).control(0.575f)
            .control(0.6f).control(0.7f).control(0.8f).control(0.9f).control(1f).build();

    public boolean prep = false;
    public long attackStart = -1;
    public Vec3 attackPoint = Vec3.ZERO;
    public final Set<LivingEntity> hitTargets = new HashSet<>();

    public SanguineBat() {
        super(SummonerAttachmentEntityRegister.SANGUINE_BAT);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.VAR_LONG, () -> attackStart, value -> attackStart = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> attackPoint, value -> attackPoint = value);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new SanguineBatAttackGoal(this));
        goalSelector.addGoal(1, new SanguineBatIdleGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "sanguine_bat", 0, state -> state.setAndContinue(RawAnimation.begin().thenLoop("fly"))));
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public LivingEntity searchTarget() {
        if (isAttacking()) {
            return getTarget();
        } else {
            if (prep) {
                int distance = this.getSearchDistance();
                if (distance > 0 && this.owner != null) {
                    TargetCache targetCache = this.getTargetCache();
                    List<LivingEntity> targets = targetCache.getEntitiesInRadius(this.owner.getBoundingBox().getCenter(), distance, (living) -> targetCache.isVisibility(this.owner, living) && targetCache.isTarget(living));
                    if (!targets.isEmpty()) {
                        return targetCache.getNewTarget(this, targets, 6, false);
                    }
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (isAttacking() && getTarget() != null) {
            attackPoint = getTarget().getBoundingBox().getCenter();
        }
        setCurrentPathNode(getRenderNode(0));
    }

    @Override
    public void onLevelChange() {
        super.onLevelChange();
        attackStart = -1;
        prep = false;
        setTarget(null);
        hitTargets.clear();
    }

    public boolean isAttacking() {
        return attackStart >= 0;
    }

    public float getProgress(float partialTick) {
        return ATTACK_CURVE.apply(Mth.clamp((getLevel().getGameTime() - attackStart + partialTick) / 22, 0f, 1f));
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.15, -0.15, -0.15, 0.15, 0.15, 0.15);
    }

    @Override
    public boolean canCollideAttack() {
        return isAttacking();
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            if (hitTargets.add(hit.entity())) {
                attack(hit.entity(), getDamage(), 4);
            }
        }
    }

    public PathNode getIdleNode(float partialTick) {
        int total = Math.max(1, getSameSize());
        float angle = getOrder() * Mth.TWO_PI / total;
        Vec3 ownerPos = getOwner().getPosition(partialTick);
        double radius = total > 1 ? 0.8f : 0;
        Vec3 targetPos = ownerPos.add(Math.cos(angle) * radius, getOwner().getBbHeight() + 0.5, Math.sin(angle) * radius);
        return new PathNode(targetPos, Mth.rotLerp(partialTick, getOwner().yHeadRotO, getOwner().yHeadRot), 0, 0);
    }

    @Override
    public PathNode getRenderNode(float partialTick) {
        float positionTick = getLevel().isClientSide() ? partialTick : 1;
        PathNode node = getIdleNode(positionTick);
        if (isAttacking()) {
            Vec3 targetPos = attackPoint;
            LivingEntity target = getTarget();
            if (target != null && !target.isRemoved() && target.level() == getLevel()) {
                targetPos = target.getPosition(positionTick).add(0, target.getBbHeight() * 0.5, 0);
            }
            RandomSource random = RandomSource.create(getUuid().getLeastSignificantBits() ^ attackStart);
            Vec3 normal = Ellipse.randomPlaneNormal(random, targetPos, node.pos());
            float curvature = 0.1f + random.nextFloat() * 0.4f + Math.min(0.6f, 6 / (float) (targetPos.distanceTo(node.pos()) + 1));
            Ellipse ellipse = new Ellipse(targetPos, node.pos(), normal, curvature);
            float progress = getProgress(partialTick);
            Vec3 point = ellipse.getPoint(progress);
            Vec3 tangent = ellipse.getTangent(progress);
            node = new PathNode(point, (float) Math.toDegrees(Math.atan2(-tangent.x, tangent.z)), (float) Math.toDegrees(Math.asin(-tangent.y)), 0);
        }
        return node;
    }
}
