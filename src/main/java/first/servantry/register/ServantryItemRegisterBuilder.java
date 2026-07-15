package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.dataGenerator.provider.ServantryItemModelProvider;
import first.servantry.dataGenerator.provider.ServantryItemTagsProvider;
import first.servantry.dataGenerator.provider.ServantryRecipeProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ServantryItemRegisterBuilder<T extends Item> {

    private static final DeferredRegister.Items Register = DeferredRegister.createItems(Servantry.MODID);
    private final DeferredItem<T> register;

    private ServantryItemRegisterBuilder(DeferredItem<T> register) {
        this.register = register;
    }

    public static <T extends Item> ServantryItemRegisterBuilder<T> build(TabGroup group, String name, Supplier<T> supplier) {
        DeferredItem<T> register = Register.register(name, supplier);
        if (group != null) {
            ServantryCreativeTabRegister.TabBuilder.computeIfAbsent(group, key -> new ArrayList<>()).add(register);
        }
        return new ServantryItemRegisterBuilder<>(register);
    }

    public static ServantryItemRegisterBuilder<Item> build(TabGroup group, String name) {
        return build(group, name, () -> new Item(new Item.Properties()));
    }

    public ServantryItemRegisterBuilder<T> recipe(Consumer<RecipeOutput> outputConsumer) {
        if (!FMLLoader.isProduction()) {
            ServantryRecipeProvider.RecipeGenerate.add(outputConsumer);
        }
        return this;
    }

    public ServantryItemRegisterBuilder<T> language(String key, String enDesc, String zhDesc) {
        if (!FMLLoader.isProduction()) {
            ServantryLanguageGenerateRegister.entry(key, enDesc, zhDesc);
        }
        return this;
    }

    public ServantryItemRegisterBuilder<T> itemLanguage(String en, String zh) {
        if (register != null) {
            ResourceLocation id = register.getId();
            return language("item." + id.getNamespace() + "." + id.getPath(), en, zh);
        }
        return this;
    }

    public ServantryItemRegisterBuilder<T> itemLanguageTooltip(int index, String en, String zh) {
        if (register != null) {
            ResourceLocation id = register.getId();
            return language("item." + id.getNamespace() + "." + id.getPath() + ".tooltip." + index, en, zh);
        }
        return this;
    }

    public ServantryItemRegisterBuilder<T> blockLanguage(String en, String zh) {
        if (register != null) {
            ResourceLocation id = register.getId();
            return language("block." + id.getNamespace() + "." + id.getPath(), en, zh);
        }
        return this;
    }

    public ServantryItemRegisterBuilder<T> itemTag(TagKey<Item> tagKey) {
        if (!FMLLoader.isProduction()) {
            ServantryItemTagsProvider.ItemTagsGenerate.computeIfAbsent(tagKey, key -> new ArrayList<>())
                    .add(register);
        }
        return this;
    }

    public ServantryItemRegisterBuilder<T> itemModel(BiConsumer<ResourceLocation, ServantryItemModelProvider> consumer) {
        if (!FMLLoader.isProduction()) {
            ServantryItemModelProvider.ItemModelGenerate.put(register.getId(), consumer);
        }
        return this;
    }

    public static void basicModel(ResourceLocation location, ServantryItemModelProvider provider) {
        provider.basicItem(location);
    }

    public static void handheldItem(ResourceLocation location, ServantryItemModelProvider provider) {
        provider.handheldItem(location);
    }

    public <A extends AttachmentEntity> ServantryItemRegisterBuilder<T> servantLanguage(DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<A>> holder, String en, String zh) {
        ResourceLocation servantId = holder.getId();
        return language("servant." + servantId.getNamespace() + "." + servantId.getPath(), en, zh);
    }

    public DeferredItem<T> build() {
        return register;
    }

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
