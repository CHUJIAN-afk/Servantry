package first.servantry.api.common.attachment;

import first.servantry.api.damageInfo.DamageInfo;
import first.servantry.network.DamageInfoPayload;
import first.servantry.register.AttachmentRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

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
    /** 客户端：持有活跃 DamageInfo 渲染列表 */
    private final List<DamageInfo> activeInfos = new ArrayList<>();

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
     * 接收网络包中的伤害记录，转为渲染数据
     */
    public void receive(List<DamageInfoPayload.Entry> entries) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            for (DamageInfoPayload.Entry entry : entries) {
                DamageInfo info = entry.toDamageInfo();
                Vec3 pos = info.getRenderPos(0);
                Vec3 eyePosition = player.getEyePosition(0);
                if (pos.distanceToSqr(eyePosition) < 64 * 64) {
                    activeInfos.add(entry.toDamageInfo());
                }
            }
        }
    }

    /** 客户端 tick：驱动 DamageInfo 生命周期衰减，移除已过期的 */
    public void tick() {
        activeInfos.removeIf(info -> {
            info.tick();
            return info.isRemove();
        });
    }

    /** 获取当前活跃的渲染数据列表 */
    public List<DamageInfo> getActiveInfos() {
        return activeInfos;
    }

    // ===================== 链式构建器 =====================

    /**
     * 链式伤害数字构建器：必填 damageAmount/pos，可省略其余（有默认值）。
     * <p>
     * 调用 {@link #emit()} 将记录写入 Level 附件，由 tick 末统一批发包。
     * </p>
     */
    public static final class DamageInfoBuilder {
        private final net.minecraft.world.level.Level level;
        private float damageAmount;
        private double x, y, z;
        private double vx, vy, vz;
        private float drag = 0.9f;
        private boolean critical = false;
        private int color = 0xFFFFFF;
        private int endColor = 0xFFFFFF;
        private float roll = 0;

        DamageInfoBuilder(net.minecraft.world.level.Level level) {
            this.level = level;
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

        public DamageInfoBuilder drag(float drag) {
            this.drag = drag;
            return this;
        }

        public DamageInfoBuilder critical(boolean critical) {
            this.critical = critical;
            return this;
        }

        public DamageInfoBuilder color(int color) {
            this.color = color;
            return this;
        }

        public DamageInfoBuilder endColor(int endColor) {
            this.endColor = endColor;
            return this;
        }

        public DamageInfoBuilder roll(int roll) {
            this.roll = roll;
            return this;
        }

        /**
         * 将记录写入 Level 附件，客户端调用无效
         */
        public void emit() {
            if (!level.isClientSide()) {
                level.getData(AttachmentRegister.DamageInfoData)
                        .addEntry(new DamageInfoPayload.Entry(damageAmount, x, y, z, vx, vy, vz, drag, critical, color, endColor,roll));
            }
        }
    }
}
