package first.servantry.api.common.attachment;

import first.servantry.api.damageInfo.DamageInfo;
import first.servantry.network.DamageInfoPayload;
import first.servantry.register.AttachmentRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 伤害数字累积附件（挂载于 {@link net.minecraft.world.level.Level}）。
 * <p>
 * 服务端：单 tick 内通过链式 {@link DamageInfoBuilder} 累积伤害记录，
 * tick 末由 {@link first.servantry.common.event.Event#tick} 一次性取出并打包为
 * {@link DamageInfoPayload} 下发，随后清空。
 * </p>
 * <p>
 * 客户端：接收 {@link DamageInfoPayload} 后将 Entry 转为 {@link DamageInfo} 渲染数据，
 * 由客户端 tick 驱动生命周期衰减。
 * </p>
 */
public class DamageInfoData {

    /** 服务端：累积 Entry 待发包 */
    private final List<DamageInfoPayload.Entry> pendingEntries = new ArrayList<>();
    /**
     * 客户端：持有活跃 DamageInfo 渲染列表
     */
    private final Map<ResourceLocation, List<DamageInfo>> activeInfos = new HashMap<>();

    public DamageInfoData() {}

    // ===================== 服务端 =====================

    /** 开启一次链式伤害数字构建 */
    public static DamageInfoBuilder add(net.minecraft.world.level.Level level) {
        return new DamageInfoBuilder(level);
    }

    /** 累积一条伤害记录 */
    public void addEntry(DamageInfoPayload.Entry entry) {
        pendingEntries.add(entry);
    }

    /** 取出并清空当前累积的所有伤害记录 */
    public List<DamageInfoPayload.Entry> drain() {
        List<DamageInfoPayload.Entry> snapshot = new ArrayList<>(pendingEntries);
        pendingEntries.clear();
        return snapshot;
    }

    /** 当前累积伤害记录数 */
    public int size() {
        return pendingEntries.size();
    }

    // ===================== 客户端 =====================

    /**
     * 接收网络包中的伤害记录，转为渲染数据。
     * <p>
     * 若伤害类型未在 JSON 中定义且无 default，该条目将被跳过。
     * </p>
     */
    public void receive(List<DamageInfoPayload.Entry> entries) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            for (DamageInfoPayload.Entry entry : entries) {
                DamageInfo info = entry.toDamageInfo();
                if (info != null) {
                    Vec3 pos = info.getRenderPos(0);
                    Vec3 eyePosition = player.getEyePosition(0);
                    if (pos.distanceToSqr(eyePosition) < 64 * 64) {
                        activeInfos.computeIfAbsent(info.getTexture(), key -> new ArrayList<>())
                                .add(info);
                    }
                }
            }
        }
    }

    /** 客户端 tick：驱动 DamageInfo 生命周期衰减，移除已过期的 */
    public void tick() {
        activeInfos.values()
                .removeIf(infoList -> {
                    infoList.removeIf(DamageInfo::tick);
                    return infoList.isEmpty();
                });
    }

    /** 获取当前活跃的渲染数据列表 */
    public Map<ResourceLocation, List<DamageInfo>> getActiveInfos() {
        return activeInfos;
    }

    // ===================== 链式构建器 =====================

    /**
     * 链式伤害数字构建器：必填 damageType/damageAmount/pos，可省略其余（有默认值）。
     * <p>
     * 调用 {@link #emit()} 将记录写入 Level 附件，由 tick 末统一批发包。
     * 渲染参数（贴图、颜色、尺寸等）由客户端根据 damageType 从 JSON 样式表查询，
     * 不通过网络同步。
     * </p>
     */
    public static final class DamageInfoBuilder {
        private final Level level;
        private String damageType = "default";
        private float damageAmount;
        private double x, y, z;
        private double vx, vy, vz;
        private boolean critical = false;

        DamageInfoBuilder(Level level) {
            this.level = level;
        }

        public DamageInfoBuilder damageType(String damageType) {
            this.damageType = damageType;
            return this;
        }

        public DamageInfoBuilder damageAmount(float amount) {
            this.damageAmount = amount;
            return this;
        }

        public DamageInfoBuilder pos(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public DamageInfoBuilder pos(Vec3 pos) {
            return pos(pos.x, pos.y, pos.z);
        }

        public DamageInfoBuilder velocity(double vx, double vy, double vz) {
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            return this;
        }

        public DamageInfoBuilder velocity(Vec3 velocity) {
            return velocity(velocity.x, velocity.y, velocity.z);
        }

        public DamageInfoBuilder critical(boolean critical) {
            this.critical = critical;
            return this;
        }

        /**
         * 将记录写入 Level 附件，客户端调用无效
         */
        public void emit() {
            if (!level.isClientSide()) {
                level.getData(AttachmentRegister.DamageInfoData)
                        .addEntry(new DamageInfoPayload.Entry(damageType, damageAmount, x, y, z, vx, vy, vz, critical));
            }
        }
    }
}
