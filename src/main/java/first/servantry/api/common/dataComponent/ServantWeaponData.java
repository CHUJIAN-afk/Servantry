package first.servantry.api.common.dataComponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 剑鞘容器 - 存储剑鞘中的物品。
 */
public record ServantWeaponData(float damage, float knockback, float armor_pierce) {

    public static final Codec<ServantWeaponData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.FLOAT.fieldOf("damage").forGetter(ServantWeaponData::damage),
                    Codec.FLOAT.fieldOf("knockback").forGetter(ServantWeaponData::knockback),
                    Codec.FLOAT.fieldOf("armor_pierce").forGetter(ServantWeaponData::armor_pierce)
            ).apply(instance, ServantWeaponData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ServantWeaponData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ServantWeaponData(float damage1, float knockback1, float armor_pierce1)) {
            return damage1 == damage && knockback1 == knockback && armor_pierce1 == armor_pierce;
        }
        return false;
    }
}
