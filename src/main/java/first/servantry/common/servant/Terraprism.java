package first.servantry.common.servant;

import first.servantry.api.PathNode;
import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.servant.goal.TerraprismAttackGoal;
import first.servantry.common.servant.goal.TerraprismIdleGoal;
import first.servantry.register.ServantRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Terraprism extends Servant implements ICollideAttack<Terraprism> {

    public boolean idle = true;
    public boolean attacking = false;
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
        goalSelector.addGoal(1, new TerraprismIdleGoal(this));
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
    public AABB getHitbox() {
        return new AABB(-0.1, -0.04, -0.75, 0.1, 0.04, 0.25);
    }

    @Override
    public void onCollisionAttack(Set<LivingEntity> hitTargets) {
        if (isExecutingPath()) {
            for (LivingEntity target : hitTargets) {
                if (this.hitTargets.add(target)) {
                    InvincibleData.servantAttack(target, this, -1, getDamageSource(), getDamage(), InvincibleData.Type.PARTIAL);
                }
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
            if (attacking) {
                trailTimer = 12;
                idleBlend = Math.max(0.0f, idleBlend - 0.25f);
            } else {
                trailTimer--;
                idleBlend = Math.min(1.0f, idleBlend + 0.1f);
            }
        }
        super.tick();
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(idle);
        buf.writeBoolean(attacking);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        this.idle = buf.readBoolean();
        this.attacking = buf.readBoolean();
    }

    public PathNode getInterpolatedIdleState(float partialTick) {
        float playerYaw = Mth.rotLerp(partialTick, owner.yBodyRotO, owner.yBodyRot);
        float rad = (float) Math.toRadians(-playerYaw + 180);
        float backX = (float) Math.sin(rad);
        float backZ = (float) Math.cos(rad);
        float rightX = (float) Math.cos(rad);
        float rightZ = (float) -Math.sin(rad);
        int order = getOrder();
        double localZ = 0.5 + order * 0.12;
        double floatSpeed = 0.08 + order * 0.01;
        double floatAngle = (owner.tickCount + partialTick) * floatSpeed + order * 1.33;
        double px = Mth.lerp(partialTick, owner.xo, owner.getX());
        double py = Mth.lerp(partialTick, owner.yo, owner.getY());
        double pz = Mth.lerp(partialTick, owner.zo, owner.getZ());
        Vec3 playerPos = new Vec3(px, py, pz);
        Vec3 targetPos = playerPos.add(localZ * backX + Math.cos(floatAngle) * 0.075 * rightX, owner.getBbHeight() * 0.6 + Math.sin(floatAngle) * 0.075, localZ * backZ + Math.cos(floatAngle) * 0.075 * rightZ);
        return new PathNode(targetPos, playerYaw - 90, 75 - order * 5f, 100);
    }

    /**
     * 获取当前速度向量
     */
    public Vec3 getCurrentVelocity(Vec3 currentPos) {
        LinkedList<PathNode> history = getHistoryNodes();
        if (history.size() > 1) {
            Vec3 rawVel = currentPos.subtract(history.get(1).pos());
            if (rawVel.lengthSqr() > 1e-5) return rawVel.normalize();
        }
        return Vec3.directionFromRotation(getPitch(), getYaw()).normalize();
    }

    /**
     * 获取当前法线向量（基于旋转）
     */
    public Vec3 getCurrentNormal() {
        Quaternionf q = new Quaternionf().rotateY((float) Math.toRadians(-getYaw()))
                .rotateX((float) Math.toRadians(getPitch()))
                .rotateZ((float) Math.toRadians(getRoll()));
        Vector3f upV = new Vector3f(0, 1, 0).rotate(q);
        return new Vec3(upV.x(), upV.y(), upV.z()).normalize();
    }

    /**
     * 计算贝塞尔曲线上的点
     */
    public Vec3 calculateBezierPoint(Vec3 P0, Vec3 P1, Vec3 P2, Vec3 P3, float t) {
        float mt = 1.0f - t;
        return P0.scale(mt * mt * mt)
                .add(P1.scale(3 * mt * mt * t))
                .add(P2.scale(3 * mt * t * t))
                .add(P3.scale(t * t * t));
    }

    /**
     * 计算椭圆上的点
     */
    public Vec3 calculateEllipsePoint(Vec3 center, Vec3 major, Vec3 minor, float theta) {
        return center.add(major.scale(Math.cos(theta))).add(minor.scale(Math.sin(theta)));
    }

    /**
     * 计算椭圆切线方向
     */
    public Vec3 calculateEllipseTangent(Vec3 major, Vec3 minor, float theta) {
        return major.scale(-Math.sin(theta)).add(minor.scale(Math.cos(theta))).normalize();
    }

    @Override
    public ServantType<? extends Servant> getServantType() {
        return ServantRegister.TerraPrism.get();
    }

}