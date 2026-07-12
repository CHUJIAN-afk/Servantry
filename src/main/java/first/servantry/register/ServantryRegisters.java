package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.dataGenerator.provider.ServantryRecipeProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ServantryRegisters {

    private static final ServantryRegisters ServantryRegisters = new ServantryRegisters();
    private final DeferredRegister.Items Register = DeferredRegister.createItems(Servantry.MODID);
    private final LinkedHashMap<TabGroup, List<DeferredItem<Item>>> map = new LinkedHashMap<>();
    private final List<ServantryRecipeProvider.RecipeGenerate> RecipeGenerate = new ArrayList<>();
    private DeferredItem<Item> pending;

    public static ServantryRegisters getInstance() {
        return ServantryRegisters;
    }

    public ServantryRegisters recipe(Consumer<RecipeOutput> outputConsumer) {
        RecipeGenerate.add(outputConsumer::accept);
        return this;
    }

    public ServantryRegisters language(String en, String zh) {
        if (pending != null) {
            ResourceLocation id = pending.getId();
            return language("item." + id.getNamespace() + "." + id.getPath(), en, zh);
        }
        return this;
    }

    public ServantryRegisters blockLanguage(String en, String zh) {
        if (pending != null) {
            ResourceLocation id = pending.getId();
            return language("block." + id.getNamespace() + "." + id.getPath(), en, zh);
        }
        return this;
    }

    public ServantryRegisters tooltip(int index, String en, String zh) {
        if (pending != null) {
            ResourceLocation id = pending.getId();
            return language("item." + id.getNamespace() + "." + id.getPath() + ".tooltip." + index, en, zh);
        }
        return this;
    }

    public ServantryRegisters servant(DeferredHolder<AttachmentEntityType<?>, ?> holder, String en, String zh) {
        ResourceLocation servantId = holder.getId();
        return language("servant." + servantId.getNamespace() + "." + servantId.getPath(), en, zh);
    }

    public ServantryRegisters language(String key, String en, String zh) {
        return this;
    }

    public ServantryRegisters register(TabGroup group, String name, Supplier<Item> sup) {
        DeferredItem<Item> register = Register.register(name, sup);
        map.computeIfAbsent(group, k -> new ArrayList<>()).add(register);
        this.pending = register;
        return this;
    }

    public ServantryRegisters register(TabGroup group, String name) {
        return register(group, name, () -> new Item(new Item.Properties()));
    }

    public DeferredItem<Item> build() {
        return pending;
    }

    public List<TabGroup> sortedEntries() {
        List<TabGroup> sortedKeys = new ArrayList<>(map.keySet());
        sortedKeys.sort(Comparator.comparingInt(TabGroup::order));
        return sortedKeys;
    }

    public LinkedHashMap<TabGroup, List<DeferredItem<Item>>> getMap() {
        return map;
    }

    public List<ServantryRecipeProvider.RecipeGenerate> getRecipeGenerate() {
        return RecipeGenerate;
    }

    public void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
