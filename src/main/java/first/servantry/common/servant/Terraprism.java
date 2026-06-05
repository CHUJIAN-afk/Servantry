package first.servantry.common.servant;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.entity.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.servant.goal.terraprism.TerraprismAttackGoal;
import first.servantry.common.servant.goal.terraprism.TerraprismIdleGoal;
import first.servantry.common.servant.goal.terraprism.TerraprismPrepGoal;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.register.AttachmentRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Terraprism extends Servant implements ICollideAttack<Terraprism> {

    public boolean attacking = false;
    private boolean blend = false;
    public int trailTimer = 0;
    public float idleBlend = 0f;
    public float idleBlendO = 0f;
    public final Set<LivingEntity> hitTargets = new HashSet<>();

    public Terraprism() {
        super();
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new TerraprismAttackGoal(this));
        goalSelector.addGoal(1, new TerraprismPrepGoal(this));
        goalSelector.addGoal(2, new TerraprismIdleGoal(this));
    }

    @Override
    public float getDamage() {
        return 9F;
    }

    @Override
    public float getKnockback() {
        return 0.1f;
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.15, -0.06, -0.35, 0.15, 0.06, 1);
    }

    @Override
    public boolean canCollideAttack() {
        return isTarget(getTarget());
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            if (this.hitTargets.add(hit.entity())) {
                InvincibleData.criteriaAttack(hit.entity(), getUuid(), -1, getDamageSource(), getDamage(), InvincibleData.Type.PARTIAL);
            }
        }
    }

    @Override
    public boolean isValidCollisionTarget(Terraprism entity, LivingEntity target) {
        return isTarget(target);
    }

    @Override
    public int getTargetDistance() {
        return 18;
    }

    @Override
    public boolean requireLineOfSight() {
        return !attacking;
    }

    @Override
    public void tick() {
        if (owner.level().isClientSide()) {
            idleBlendO = idleBlend;
            if (blend) {
                trailTimer = Math.min(trailTimer + 1, 12);
                idleBlend = Math.max(0.0f, idleBlend - 0.25f);
            } else {
                trailTimer = Math.max(trailTimer - 1, 0);
                idleBlend = Math.min(1.0f, idleBlend + 0.1f);
            }
        }
        super.tick();
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(attacking);
        buf.writeBoolean(!(getGoalSelector().getCurrentGoal() instanceof TerraprismIdleGoal));
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        this.attacking = buf.readBoolean();
        this.blend = buf.readBoolean();
    }

    public PathNode getInterpolatedIdleState(float partialTick) {
        float bodyYaw = Mth.rotLerp(partialTick, owner.yBodyRotO, owner.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, owner.yHeadRotO, owner.yHeadRot);
        float playerYaw = Mth.wrapDegrees(bodyYaw + Mth.wrapDegrees(headYaw - bodyYaw) * 0.5f);
        float rad = (float) Math.toRadians(-playerYaw + 180);
        float backX = (float) Math.sin(rad);
        float backZ = (float) Math.cos(rad);
        float rightX = (float) Math.cos(rad);
        float rightZ = (float) -Math.sin(rad);
        int order = 0;
        for (AttachmentEntity entity : getOwner().getData(AttachmentRegister.EntityData).getEntities()) {
            if (entity instanceof Terraprism terraprism) {
                if (terraprism == this) {
                    break;
                }
                order++;
            }
        }
        double localZ = 0.75 + order * 0.12;
        double floatSpeed = 0.08 + order * 0.01;
        double floatAngle = (owner.tickCount + partialTick) * floatSpeed + order * 1.33;
        Vec3 playerPos = owner.getPosition(partialTick);
        Vec3 targetPos = playerPos.add(localZ * backX + Math.cos(floatAngle) * 0.075 * rightX, owner.getBbHeight() * 0.6 + Math.sin(floatAngle) * 0.075, localZ * backZ + Math.cos(floatAngle) * 0.075 * rightZ);
        return new PathNode(targetPos, playerYaw - 90, 75 - order * 5f, 100);
    }

    /**
     * 获取当前速度向量
     */
    public Vec3 getCurrentVelocity() {
        Vec3 currentPos = getPos();
        ArrayList<PathNode> history = getHistoryNodes();
        if (history.size() > 1) {
            Vec3 rawVel = currentPos.subtract(history.getFirst().pos());
            if (rawVel.lengthSqr() > 1e-5) {
                return rawVel.normalize();
            }
        }
        return Vec3.directionFromRotation(getPitch(), getYaw()).normalize();
    }

    /**
     * 计算贝塞尔曲线上的点（De Casteljau算法，支持任意数量控制点）
     */
    public Vec3 calculateBezierPoint(float delta, Vec3... P) {
        if (P.length == 0) return Vec3.ZERO;
        if (P.length == 1) return P[0];
        Vec3[] pts = P.clone();
        for (int k = P.length - 1; k > 0; k--) {
            for (int i = 0; i < k; i++) {
                pts[i] = pts[i].lerp(pts[i + 1], delta);
            }
        }
        return pts[0];
    }

    @Override
    public AttachmentEntityType<? extends Servant> getType() {
        return AttachmentEntityRegister.TerraPrism.get();
    }
}