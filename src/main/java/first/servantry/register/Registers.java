package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.dadageneeator.provider.ServantryRecipeProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Registers {

    private final DeferredRegister.Items Register;
    private final LinkedHashMap<TabGroup, List<DeferredItem<Item>>> map = new LinkedHashMap<>();
    private final List<ServantryRecipeProvider.Generate> RecipeGenerate = new ArrayList<>();
    private DeferredItem<Item> pending;

    public Registers(String modid) {
        this.Register = DeferredRegister.createItems(modid);
    }

    public static Registers create() {
        return new Registers(Servantry.MODID);
    }

    public Registers recipe(Consumer<RecipeOutput> outputConsumer) {
        RecipeGenerate.add(outputConsumer::accept);
        return this;
    }

    public Registers register(TabGroup group, String name, Supplier<Item> sup) {
        DeferredItem<Item> register = Register.register(name, sup);
        map.computeIfAbsent(group, k -> new ArrayList<>()).add(register);
        this.pending = register;
        return this;
    }

    public Registers register(TabGroup group, String name) {
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

    public List<ServantryRecipeProvider.Generate> getRecipeGenerate() {
        return RecipeGenerate;
    }

    public void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}
