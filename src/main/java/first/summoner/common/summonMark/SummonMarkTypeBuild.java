package first.summoner.common.summonMark;

import first.lyra.common.minion.MinionDamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;

public class SummonMarkTypeBuild {

    private final ResourceLocation identifier;
    private float additionalDamage = 0;
    private float additionalArmorPierce = 0;
    private float criticalHitRate = 0;
    private BiConsumer<LivingEntity, Player> tickConsumer = null;
    private SummonMarkType.SummonMarkDamageConsumer damageConsumer = null;
    private BiConsumer<LivingEntity, MinionDamageSource> killConsumer = null;

    public SummonMarkTypeBuild(ResourceLocation identifier) {
        this.identifier = identifier;
    }

    /** 设置标记附加伤害。 */
    public SummonMarkTypeBuild damage(float damage) {
        this.additionalDamage = damage;
        return this;
    }

    /** 设置标记附加护甲穿透。 */
    public SummonMarkTypeBuild armorPierce(float armorPierce) {
        this.additionalArmorPierce = armorPierce;
        return this;
    }

    /** 设置标记暴击率（0~1，如 0.2 = 20%）。 */
    public SummonMarkTypeBuild criticalRate(float criticalRate) {
        this.criticalHitRate = criticalRate;
        return this;
    }

    /** 标记持续期间每 tick 回调（target = 被标记实体，owner = 标记所有者）。 */
    public SummonMarkTypeBuild onTick(BiConsumer<LivingEntity, Player> tickConsumer) {
        this.tickConsumer = tickConsumer;
        return this;
    }

    /** 标记目标受击时回调（target = 被标记实体，source = 伤害来源，damage = 本次伤害值）。 */
    public SummonMarkTypeBuild onDamage(SummonMarkType.SummonMarkDamageConsumer damageConsumer) {
        this.damageConsumer = damageConsumer;
        return this;
    }

    /** 标记目标被击杀时回调（target = 被标记实体，source = 伤害来源）。 */
    public SummonMarkTypeBuild onKill(BiConsumer<LivingEntity, MinionDamageSource> killConsumer) {
        this.killConsumer = killConsumer;
        return this;
    }

    /**
     * 构建标记类型（工厂延迟创建独立 SummonMark 实例，含回调注入）。
     */
    public SummonMarkType build() {
        return new SummonMarkType(identifier, additionalDamage, additionalArmorPierce, criticalHitRate, tickConsumer, damageConsumer, killConsumer);
    }
}
