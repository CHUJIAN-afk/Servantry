package first.servantry.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class Marker {

    private final ResourceLocation type; // 标记类型
    private final float extraDamage;           // 额外伤害
    private int remainingTicks;          // 剩余持续时间 (单位：Tick)
    private final float critRate;              // 额外暴击率 (0.0 ~ 1.0)

    /**
     * @param type           标记的唯一标识类型
     * @param extraDamage    该标记提供的额外基础伤害
     * @param durationTicks  持续时间
     * @param critRate       该标记提供的额外暴击率
     */
    public Marker(ResourceLocation type, float extraDamage, int durationTicks, float critRate) {
        this.type = type;
        this.extraDamage = extraDamage;
        this.remainingTicks = durationTicks;
        this.critRate = critRate;
    }

    /**
     * 每 tick 执行的方法，用于更新标记状态
     */
    public void tick() {
        if (this.remainingTicks > 0) {
            this.remainingTicks--;
        }
    }

    /**
     * 检查标记是否已经过期
     */
    public boolean isExpired() {
        return this.remainingTicks <= 0;
    }

    // --- Getter & Setter ---

    public ResourceLocation getType() {
        return type;
    }

    public float getExtraDamage() {
        return extraDamage;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public void setRemainingTicks(int remainingTicks) {
        this.remainingTicks = remainingTicks;
    }

    public float getCritRate() {
        return critRate;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(this.type);
        buf.writeFloat(this.extraDamage);
        buf.writeInt(this.remainingTicks);
        buf.writeFloat(this.critRate);
    }

    public static Marker read(RegistryFriendlyByteBuf buf) {
        return new Marker(
                buf.readResourceLocation(),
                buf.readFloat(),
                buf.readInt(),
                buf.readFloat()
        );
    }

}
