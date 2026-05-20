package first.servantry.client.tooltip;

import first.servantry.utils.RenderUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ScabbardTooltipComponent(ItemStack itemStack) implements ClientTooltipComponent, TooltipComponent {

    @Override
    public int getWidth(@NotNull Font font) {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void renderImage(@NotNull Font font, int tooltipX, int tooltipY, @NotNull GuiGraphics guiGraphics) {
        int dominantColor = RenderUtil.getDominantColor(itemStack);
        tooltipX -= 1;
        tooltipY -= 39;
        TooltipRenderUtil.renderTooltipBackground(
                guiGraphics,
                tooltipX,
                tooltipY,
                18,
                18,
                0,
                -267386864,
                -267386864,
                0xFF000000 | dominantColor,
                0xFF000000 | dominantColor
        );
        guiGraphics.renderItem(itemStack, tooltipX + 1, tooltipY + 1);
    }
}
