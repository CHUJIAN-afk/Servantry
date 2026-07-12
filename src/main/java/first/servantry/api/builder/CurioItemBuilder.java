package first.servantry.api.builder;

import com.google.common.collect.Multimap;
import first.servantry.api.item.CurioItem;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.function.TriFunction;
import top.theillusivec4.curios.api.SlotContext;

import java.util.function.Consumer;

public class CurioItemBuilder {

    public final Item.Properties properties;
    public TriFunction<SlotContext, ResourceLocation, ItemStack, Multimap<Holder<Attribute>, AttributeModifier>> attributeModifiers;
    public CurioItem.PreDamageCallback preDamageCallback;
    public CurioItem.PostDamageCallback postDamageCallback;

    public CurioItemBuilder() {
        this.properties = new Item.Properties().stacksTo(1);
    }

    public CurioItemBuilder attributeModifiers(TriFunction<SlotContext, ResourceLocation, ItemStack, Multimap<Holder<Attribute>, AttributeModifier>> attributeModifiers) {
        this.attributeModifiers = attributeModifiers;
        return this;
    }

    public CurioItemBuilder onPreDamage(CurioItem.PreDamageCallback callback) {
        this.preDamageCallback = callback;
        return this;
    }

    public CurioItemBuilder onPostDamage(CurioItem.PostDamageCallback callback) {
        this.postDamageCallback = callback;
        return this;
    }

    public CurioItemBuilder properties(Consumer<Item.Properties> customizer) {
        customizer.accept(this.properties);
        return this;
    }

    public CurioItem build() {
        return new CurioItem(this);
    }
}
