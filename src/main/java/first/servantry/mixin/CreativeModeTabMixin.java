package first.servantry.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import first.servantry.register.ServantryCreativeTabRegister;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@Mixin(CreativeModeTab.class)
public class CreativeModeTabMixin {

    @Shadow
    private Collection<ItemStack> displayItems;

    @Shadow
    private Set<ItemStack> displayItemsSearchTab;

    @WrapMethod(method = "buildContents")
    private void simulated$buildContents(CreativeModeTab.ItemDisplayParameters parameters, Operation<Void> original) {
        CreativeModeTab tab = (CreativeModeTab) (Object) this;
        if (tab == ServantryCreativeTabRegister.Tab.get()) {
            List<ItemStack> displayItems = new LinkedList<>();
            Set<ItemStack> searchItems = new LinkedHashSet<>();
            ServantryCreativeTabRegister.processItems(displayItems::add, searchItems::add);
            this.displayItems = displayItems;
            this.displayItemsSearchTab = searchItems;
            return;
        }
        original.call(parameters);
    }
}
