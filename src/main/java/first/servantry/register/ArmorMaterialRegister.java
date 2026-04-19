package first.servantry.register;

import first.servantry.Servantry;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class ArmorMaterialRegister {

    private static final DeferredRegister<ArmorMaterial> Register = DeferredRegister.create(Registries.ARMOR_MATERIAL, Servantry.MODID);

    public static final Holder<ArmorMaterial> HallowedArmorMaterial = Register.register("hallowed", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 1);      // 鞋子 1点护甲
                map.put(ArmorItem.Type.LEGGINGS, 2);   // 护腿 2点护甲
                map.put(ArmorItem.Type.CHESTPLATE, 3); // 胸甲 3点护甲
                map.put(ArmorItem.Type.HELMET, 1);     // 头部 1点护甲 (总计 7 点)
            }),
            35, // 极高的附魔能力 (金头盔是25，35属于极强)
            SoundEvents.ARMOR_EQUIP_LEATHER, // 穿戴音效
            () -> Ingredient.of(Items.GOLD_INGOT), // 皮革修复
            List.of(new ArmorMaterial.Layer(Servantry.rl("hallowed"))), // 对应贴图路径 textures/models/armor/hallowed_layer_1.png 和 hallowed_layer_2.png
            0.0F, // 韧性
            0.0F  // 击退抗性
    ));

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}
