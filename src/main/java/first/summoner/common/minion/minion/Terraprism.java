package first.summoner.common.minion.minion;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachment.TargetCache;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.entity.PathNode;
import first.lyra.common.entity.SyncFieldDispatcher;
import first.lyra.common.minion.Minion;
import first.lyra.common.minion.MinionGoalSelector;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.minion.minion.goal.terraprism.TerraprismAttackGoal;
import first.summoner.common.minion.minion.goal.terraprism.TerraprismIdleGoal;
import first.summoner.common.minion.minion.goal.terraprism.TerraprismPrepGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Terraprism extends Minion implements IEntityCollision<Terraprism> {

    public boolean attacking = false;
    public final Set<LivingEntity> hitTargets = new HashSet<>();

    public int trailTimer = 0;
    public float lastIdleBlend = 0f;
    public float idleBlend = 0f;

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.INT, () -> trailTimer, value -> trailTimer = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> lastIdleBlend, value -> lastIdleBlend = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> idleBlend, value -> idleBlend = value);
    }

    public Terraprism() {
        super(SummonerAttachmentEntityRegister.TERRA_PRISM);
    }

    protected Terraprism(Holder<AttachmentEntityType<?>> type) {
        super(type);
    }

    @Override
    public void registerGoals(MinionGoalSelector goalSelector) {
        goalSelector.addGoal(0, new TerraprismAttackGoal(this));
        goalSelector.addGoal(1, new TerraprismPrepGoal(this));
        goalSelector.addGoal(2, new TerraprismIdleGoal(this));
    }

    @Override
    public LivingEntity searchTarget() {
        int distance = this.getSearchDistance();
        if (distance > 0) {
            LyraHelper helper = LyraHelper.get(getLevel());
            TargetCache targetCache = helper.getTargetCache();
            List<LivingEntity> targets = targetCache.getEntitiesInRadius(this.getPos(), targetCache.getSummonSearchRange(this.getOwner(), (float) distance), (living) -> (attacking || targetCache.isVisibility(this, living)) && this.isTarget(living));
            if (!targets.isEmpty()) {
                return targetCache.getNewTarget(this, targets, 8, false);
            }
        }
        return null;
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.15, -0.06, -0.5, 0.15, 0.06, 1.05);
    }

    @Override
    public boolean canCollideAttack() {
        return isTarget(getTarget());
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            if (this.hitTargets.add(hit.entity())) {
                InvincibleData.attack(hit.entity())
                        .attacker(getUuid())
                        .damageSource(getDamageSource())
                        .damageAmount(getDamage())
                        .apply();
            }
        }
    }

    @Override
    public void tick() {
        if (!getLevel().isClientSide()) {
            lastIdleBlend = idleBlend;
            if (attacking) {
                trailTimer = Math.min(trailTimer + 1, 12);
            } else {
                trailTimer = 0;
            }
            if (getGoalSelector().getCurrentGoal() instanceof TerraprismIdleGoal) {
                idleBlend = Math.min(1.0f, idleBlend + 0.1f);
            } else {
                idleBlend = Math.max(0.0f, idleBlend - 0.25f);
            }
        }
        super.tick();
    }

    public int getColor(float partialTick) {
        int order = getSameOrder();
        int total = Math.max(1, getSameSize());
        int animationTick = owner != null ? owner.tickCount : tickCount;
        float hueShift = ((float) order / total + (animationTick + partialTick) * 0.015f) % 1.0f;
        float breathFactor = 0.5f + 0.5f * Mth.sin(hueShift * Mth.TWO_PI);
        return Mth.hsvToRgb(hueShift, 0.75f - 0.35f * breathFactor, 1.0f);
    }

    @Override
    public PathNode getRenderNode(float partialTick) {
        PathNode renderNode = super.getRenderNode(partialTick);
        return renderNode.lerp(getInterpolatedIdleState(partialTick), Mth.lerp(partialTick, lastIdleBlend, idleBlend));
    }

    @Override
    public long getSameHash() {
        return Objects.hash(Terraprism.class) * 43L;
    }

    public PathNode getInterpolatedIdleState(float partialTick) {
        PathNode node = getCurrentPathNode();
        if (owner != null) {
            float bodyYaw = Mth.rotLerp(partialTick, owner.yBodyRotO, owner.yBodyRot);
            float headYaw = Mth.rotLerp(partialTick, owner.yHeadRotO, owner.yHeadRot);
            float playerYaw = Mth.wrapDegrees(bodyYaw + Mth.wrapDegrees(headYaw - bodyYaw) * 0.5f);
            float rad = (float) Math.toRadians(-playerYaw + 180);
            float backX = (float) Math.sin(rad);
            float backZ = (float) Math.cos(rad);
            float rightX = (float) Math.cos(rad);
            float rightZ = (float) -Math.sin(rad);
            int order = getSameOrder();
            double localZ = 0.75 + order * 0.12;
            double floatSpeed = 0.08 + order * 0.01;
            double floatAngle = (owner.tickCount + partialTick) * floatSpeed + order * 1.33;
            Vec3 playerPos = owner.getPosition(partialTick);
            Vec3 targetPos = playerPos.add(localZ * backX + Math.cos(floatAngle) * 0.075 * rightX, owner.getBbHeight() * 0.6 + Math.sin(floatAngle) * 0.075, localZ * backZ + Math.cos(floatAngle) * 0.075 * rightZ);
            node = new PathNode(targetPos, playerYaw - 90, 75 - order * 5f, 100);
        }
        return node;
    }
}
