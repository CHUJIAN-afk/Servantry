package first.servantry.api.servant;

import first.servantry.api.ServantryHelper;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.common.attachment.TargetCache;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.DamageRegister;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.monster.Enemy;

import java.util.List;
import java.util.Map;

/**
 * 仆从实体抽象基类，代表由玩家拥有、AI驱动、自主行动的战斗单位。
 */
public abstract class Servant extends AttachmentEntity {

    // ===================== AI系统 =====================

    private final ServantGoalSelector goalSelector = new ServantGoalSelector();
    private LivingEntity target = null;
    private int slotCost = 1;
    private boolean targetChange = false;

    public Servant() {
        super();
        registerGoals(goalSelector);
    }

    // ===================== 抽象方法 =====================

    /**
     * 注册AI目标
     */
    public void registerGoals(ServantGoalSelector goalSelector) {
    }

    /**
     * 获取占用栏位数
     */
    public int getSlotCost() {
        return slotCost;
    }

    public void setSlotCost(int slotCost) {
        this.slotCost = slotCost;
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

    /**
     * 在所有者周围搜索有效目标
     */
    public LivingEntity searchTarget() {
        ServantryHelper helper = ServantryHelper.get(owner);
        TargetCache targetCache = helper.getTargetCache();
        if (!targetCache.isEmpty()) {
            float searchRange = targetCache.getServantSearchRange(this.getOwner(), 32);
            List<LivingEntity> targets = targetCache.getEntities()
                    .stream()
                    .filter(living -> targetCache.isVisibility(owner, living))
                    .filter(living -> targetCache.isVisibility(this, living))
                    .filter(living -> targetCache.getDistance(owner, living) < searchRange)
                    .filter(living -> isTarget(living))
                    .toList();
            return targetCache.getNewTarget(this, targets, 0, true);
        }
        return null;
    }

    public int getTargetDistance() {
        return 64;
    }

    public boolean requireLineOfSight() {
        return true;
    }

    /**
     * 判断生物是否为有效攻击目标
     */
    public boolean isTarget(LivingEntity target) {
        if (target != null && owner != target && target.isAlive()) {
            if (target instanceof Enemy) {
                return true;
            }
            if (target instanceof Targeting targeting && targeting.getTarget() == owner) {
                return true;
            }
            if (InvincibleData.recentlyAttacked(target, owner.getUUID())) {
                return true;
            }
            if (InvincibleData.recentlyAttacked(owner, target.getUUID())) {
                return true;
            }
            if (InvincibleData.recentlyAttacked(target, this.getUuid())) {
                return true;
            }
        }
        return false;
    }

    // ===================== 伤害来源 =====================

    /** 构造仆从专属伤害来源 */
    public ServantDamageSource getDamageSource() {
        Holder<DamageType> holder = DamageRegister.getDamageTypeHolder(DamageRegister.Servant, owner.level());
        return new ServantDamageSource(holder, null, owner, getCurrentPathNode().pos(), this);
    }

    // ===================== 排序 =====================

    /**
     * 获取目标仆从在其 AttachmentEntityType 分组中的未移除顺序
     */
    public int getOrder() {
        AttachmentEntityType<?> entityType = getType();
        Map<AttachmentEntityType<?>, List<AttachmentEntity>> inner = owner.getData(AttachmentRegister.EntityData).getGroups().get(EntityData.Type.Servant);
        List<AttachmentEntity> list = inner != null ? inner.get(entityType) : null;
        if (list != null) {
            int order = 0;
            for (AttachmentEntity attachmentEntity : list) {
                if (attachmentEntity == this) {
                    return order;
                }
                if (!attachmentEntity.isRemove()) {
                    order++;
                }
            }
        }
        return -1;
    }

    /**
     * 获取目标仆从在其 AttachmentEntityType 分组中的未移除数量
     */
    public int getSameSize() {
        Map<AttachmentEntityType<?>, List<AttachmentEntity>> inner = owner.getData(AttachmentRegister.EntityData).getGroups().get(EntityData.Type.Servant);
        List<AttachmentEntity> list = inner != null ? inner.get(getType()) : null;
        int count = 0;
        if (list != null) {
            for (AttachmentEntity attachmentEntity : list) {
                if (!attachmentEntity.isRemove()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public void writeBase(RegistryFriendlyByteBuf buf) {
        super.writeBase(buf);
        buf.writeInt(slotCost);
    }

    @Override
    public void readBase(RegistryFriendlyByteBuf buf) {
        super.readBase(buf);
        slotCost = buf.readInt();
    }

    // ===================== 目标访问器 =====================

    public LivingEntity getTarget() { return target; }

    public void setTarget(LivingEntity target) {
        if (this.target != null && this.target != target) {
            setTargetChange(true);
        }
        this.target = target;
    }

    public boolean isTargetChange() {
        return targetChange;
    }

    public void setTargetChange(boolean targetChange) {
        this.targetChange = targetChange;
    }

    public ServantGoalSelector getGoalSelector() { return goalSelector; }
}