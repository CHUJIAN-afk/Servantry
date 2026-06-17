package first.servantry.mixin;

import first.servantry.register.CreativeTabRegister;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)

public class CreativeModeInventoryScreenMixin {

    @Shadow
    private static CreativeModeTab selectedTab;

    @Shadow
    private float scrollOffs;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/EffectRenderingInventoryScreen;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void simulated$render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick, final CallbackInfo ci) {
        if (selectedTab == CreativeTabRegister.Tab.get()) {
            CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;
            CreativeTabRegister.renderBanners(screen, guiGraphics, mouseX, mouseY, scrollOffs);
        }
    }
}