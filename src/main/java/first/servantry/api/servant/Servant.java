package first.servantry.api.servant;

import first.servantry.api.PathNode;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.DamageRegister;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;

/**
 * 仆从实体抽象基类，代表由玩家拥有、AI驱动、自主行动的战斗单位。
 */
public abstract class Servant extends AttachmentEntity {

    // ===================== AI系统 =====================

    private final ServantGoalSelector goalSelector = new ServantGoalSelector();
    private LivingEntity target = null;
    private boolean targetChange = false;

    public Servant() {
        super();
        registerGoals(goalSelector);
    }

    // ===================== 抽象方法 =====================

    /**
     * 注册AI目标
     */
    public abstract void registerGoals(ServantGoalSelector goalSelector);

    /**
     * 获取占用栏位数
     */
    public int getSlotCost() {
        return 1;
    }

    // ===================== 生命周期 =====================

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            setTargetChange(false);
            setTarget(searchTarget());
            goalSelector.tick();
        }
        super.tick();
    }

    // ===================== 目标搜索 =====================

    /** 在所有者周围搜索有效目标 */
    public LivingEntity searchTarget() {
        return TargetSelector.create(this)
                .maxDistance(getTargetDistance())
                .requireLineOfSight(requireLineOfSight())
                .filter(this::isTarget)
                .preferCloseTo(owner.getBoundingBox().getCenter())
                .preferCurrentTarget(getTarget())
                .find();
    }

    public int getTargetDistance() {
        return 64;
    }

    public boolean requireLineOfSight() {
        return true;
    }

    /** 判断生物是否为有效攻击目标 */
    public boolean isTarget(LivingEntity target) {
        if (target != null && owner != target && target.isAlive()) {
            boolean isEnemy = target instanceof Enemy;
            boolean targetingOwner = target instanceof Targeting t && t.getTarget() == owner;
            boolean hurtOwner = owner.getLastHurtByMob() == target;
            boolean hurtTarget = target.getLastHurtByMob() == owner;
            return isEnemy || targetingOwner || hurtOwner || hurtTarget;
        }
        return false;
    }

    // ===================== 伤害来源 =====================

    /** 构造仆从专属伤害来源 */
    public ServantDamageSource getDamageSource() {
        Registry<DamageType> damageTypes = owner.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        return new ServantDamageSource(
                damageTypes.getHolderOrThrow(DamageRegister.Servant),
                null,
                owner,
                getCurrentPathNode().pos(),
                this
        );
    }

    // ===================== 排序 =====================

    /** 获取在所有者仆从列表中的顺序索引 */
    public int getOrder() {
        return getOwner().getData(AttachmentRegister.EntityData).getOrder(this);
    }

    // ===================== 目标访问器 =====================

    public LivingEntity getTarget() { return target; }

    public void setTarget(LivingEntity target) {
        if (this.target != target) setTargetChange(true);
        this.target = target;
    }

    public boolean isTargetChange() {
        return targetChange;
    }

    public void setTargetChange(boolean targetChange) {
        this.targetChange = targetChange;
    }

    public ServantGoalSelector getGoalSelector() { return goalSelector; }

    // ===================== 数学工具 =====================

    /** 球面线性插值两个向量 */
    public Vec3 slerpVector(Vec3 v1, Vec3 v2, float t) {
        double dot = Mth.clamp(v1.dot(v2), -1.0, 1.0);
        double theta = Math.acos(dot) * t;
        Vec3 relativeVec = v2.subtract(v1.scale(dot));
        if (relativeVec.lengthSqr() < 1e-5) return v1;
        relativeVec = relativeVec.normalize();
        return v1.scale(Math.cos(theta)).add(relativeVec.scale(Math.sin(theta)));
    }

    /** 根据位置、尖端朝向和叶片法向量计算欧拉角节点 */
    public PathNode getEulerNode(Vec3 pos, Vec3 tipDir, Vec3 bladeNormal) {
        if (tipDir.lengthSqr() < 1e-4) tipDir = new Vec3(0, 0, 1);
        tipDir = tipDir.normalize();

        float yaw = (float) (Math.atan2(-tipDir.x, tipDir.z) * (180D / Math.PI));
        double horiz = Math.sqrt(tipDir.x * tipDir.x + tipDir.z * tipDir.z);
        float pitch = (float) (Math.atan2(-tipDir.y, horiz) * (180D / Math.PI));

        Vec3 defaultUp = new Vec3(0, 1, 0)
                .xRot((float) Math.toRadians(pitch))
                .yRot((float) Math.toRadians(yaw));
        Vec3 projNormal = bladeNormal.subtract(tipDir.scale(bladeNormal.dot(tipDir))).normalize();
        if (projNormal.lengthSqr() < 1e-4) projNormal = defaultUp;

        double dot = defaultUp.dot(projNormal);
        Vec3 cross = defaultUp.cross(projNormal);
        float roll = (float) (Math.atan2(cross.dot(tipDir), dot) * (180D / Math.PI));

        return new PathNode(pos, yaw, pitch, roll);
    }
}