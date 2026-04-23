package first.servantry.api.servant;

import first.servantry.api.PathNode;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.EntityType;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.DamageRegister;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;

/**
 * 仆从实体抽象基类，代表由玩家拥有、AI驱动、自主行动的战斗单位。
 * <p>
 * 仆从通过 {@link ServantGoalSelector} 管理行为优先级，支持路径移动、目标搜索与攻击。
 * 可实现 {@link ICollideAttack} 接口获得沿轨迹扫掠的碰撞攻击能力。
 * </p>
 *
 * <h3>架构模式</h3>
 * <ul>
 *   <li><b>服务端</b>：执行AI目标选择、路径推进、碰撞检测</li>
 *   <li><b>客户端</b>：接收网络同步数据，渲染拖尾和模型</li>
 * </ul>
 *
 * <h3>子类需实现的方法</h3>
 * <ul>
 *   <li>{@link #registerGoals(ServantGoalSelector)} - 注册AI目标</li>
 *   <li>{@link #getDamage()} / {@link #getKnockback()} - 攻击属性</li>
 *   <li>{@link #getServantType()} - 返回注册类型</li>
 *   <li>{@link #writeAdditional(RegistryFriendlyByteBuf)} / {@link #readAdditional(RegistryFriendlyByteBuf)} - 自定义数据同步</li>
 * </ul>
 *
 * @see ICollideAttack
 * @see ServantGoalSelector
 */
public abstract class Servant extends AttachmentEntity {

    // ===================== AI 系统 =====================

    /** AI目标选择器，管理所有行为的优先级和执行 */
    private final ServantGoalSelector goalSelector = new ServantGoalSelector();

    /** 当前攻击目标 */
    private LivingEntity target = null;

    /** 目标变化标记，用于触发响应逻辑 */
    private boolean targetChange = false;

    // ===================== 构造方法 =====================

    /**
     * 构造仆从，初始化AI目标选择器。
     */
    public Servant() {
        super();
        registerGoals(goalSelector);
    }

    // ===================== 抽象方法 =====================

    /**
     * 注册AI目标。
     * <p>
     * 子类应在此方法内通过 {@link ServantGoalSelector#addGoal(int, first.servantry.api.servant.ai.ServantGoal)} 添加行为。
     * 优先级数字越小，优先级越高。
     * </p>
     *
     * @param goalSelector 目标选择器实例
     */
    public abstract void registerGoals(ServantGoalSelector goalSelector);

    /**
     * 获取攻击造成的击退力度。
     *
     * @return 击退系数
     */
    public abstract float getKnockback();

    /**
     * 返回仆从的注册类型。
     *
     * @return 仆从类型
     */
    public abstract ServantType<? extends Servant> getServantType();

    // ===================== AttachmentEntity 实现 =====================

    @Override
    public EntityType<? extends AttachmentEntity> getType() {
        return getServantType();
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            // 服务端：执行AI逻辑
            setTargetChange(false);
            setTarget(searchTarget());
            goalSelector.tick();
            // 路径推进
            if (currentPlannedPath != null && !currentPlannedPath.isFinished()) {
                currentPathNode = currentPlannedPath.advance();
            }
        } else {
            // 客户端：使用同步数据更新位置
            currentPathNode = clientTargetNode;
        }
        super.tick();
    }

    // ===================== 目标搜索 =====================

    /**
     * 在所有者周围搜索有效目标。
     *
     * @return 找到的目标，无则返回 null
     */
    public LivingEntity searchTarget() {
        return TargetSelector.create(this)
                .maxDistance(getTargetDistance())
                .requireLineOfSight(requireLineOfSight())
                .filter(this::isTarget)
                .preferCloseTo(getPos())
                .preferCurrentTarget(getTarget())
                .find();
    }

    /**
     * 获取目标搜索最大距离。
     *
     * @return 最大距离（格）
     */
    public int getTargetDistance() {
        return 64;
    }

    /**
     * 搜索目标时是否要求视线可见。
     *
     * @return 是否要求可见
     */
    public boolean requireLineOfSight() {
        return true;
    }

    /**
     * 判断生物是否为有效攻击目标。
     * <p>
     * 有效条件（满足其一）：
     * <ul>
     *   <li>是敌对生物（{@link Enemy}）</li>
     *   <li>正以所有者为攻击目标</li>
     *   <li>是最后伤害所有者的生物</li>
     * </ul>
     * </p>
     *
     * @param target 待检测生物
     * @return 是否有效
     */
    public boolean isTarget(LivingEntity target) {
        if (target != null && owner != target && target.isAlive()) {
            boolean isEnemy = target instanceof Enemy;
            boolean targetingOwner = target instanceof Targeting t && t.getTarget() == owner;
            boolean hurtOwner = owner.getLastHurtByMob() == target;
            return isEnemy || targetingOwner || hurtOwner;
        }
        return false;
    }

    // ===================== 伤害来源 =====================

    /**
     * 构造仆从专属伤害来源。
     *
     * @return 伤害来源对象
     */
    public ServantDamageSource getDamageSource() {
        Registry<DamageType> damageTypes = owner.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE);
        return new ServantDamageSource(
                damageTypes.getHolderOrThrow(DamageRegister.Servant),
                null,
                owner,
                currentPathNode.pos(),
                this
        );
    }

    // ===================== 排序 =====================

    /**
     * 获取在所有者仆从列表中的顺序索引。
     *
     * @return 顺序索引
     */
    public int getOrder() {
        return getOwner().getData(AttachmentRegister.EntityData).getOrder(this);
    }

    // ===================== 目标访问器 =====================

    /** @return 当前攻击目标 */
    public LivingEntity getTarget() {
        return target;
    }

    /**
     * 设置攻击目标，自动标记变化。
     *
     * @param target 新目标
     */
    public void setTarget(LivingEntity target) {
        if (this.target != target) {
            setTargetChange(true);
        }
        this.target = target;
    }

    /** @return 目标是否变化 */
    public boolean isTargetChange() {
        return targetChange;
    }

    /** 设置目标变化标记 */
    public void setTargetChange(boolean targetChange) {
        this.targetChange = targetChange;
    }

    /** @return AI目标选择器 */
    public ServantGoalSelector getGoalSelector() {
        return goalSelector;
    }

    // ===================== 数学工具 =====================

    /**
     * 球面线性插值两个向量。
     *
     * @param v1 起始向量
     * @param v2 终止向量
     * @param t  插值系数（0~1）
     * @return 插值后的单位向量
     */
    public Vec3 slerpVector(Vec3 v1, Vec3 v2, float t) {
        double dot = Mth.clamp(v1.dot(v2), -1.0, 1.0);
        double theta = Math.acos(dot) * t;
        Vec3 relativeVec = v2.subtract(v1.scale(dot));
        if (relativeVec.lengthSqr() < 1e-5) return v1;
        relativeVec = relativeVec.normalize();
        return v1.scale(Math.cos(theta)).add(relativeVec.scale(Math.sin(theta)));
    }

    /**
     * 根据位置、尖端朝向和叶片法向量计算欧拉角节点。
     *
     * @param pos         位置
     * @param tipDir      尖端方向（局部Z轴）
     * @param bladeNormal 叶片法向量
     * @return 欧拉角节点
     */
    public PathNode getEulerNode(Vec3 pos, Vec3 tipDir, Vec3 bladeNormal) {
        if (tipDir.lengthSqr() < 1e-4) tipDir = new Vec3(0, 0, 1);
        tipDir = tipDir.normalize();

        // 计算偏航和俯仰
        float yaw = (float) (Math.atan2(-tipDir.x, tipDir.z) * (180D / Math.PI));
        double horiz = Math.sqrt(tipDir.x * tipDir.x + tipDir.z * tipDir.z);
        float pitch = (float) (Math.atan2(-tipDir.y, horiz) * (180D / Math.PI));

        // 计算滚转
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