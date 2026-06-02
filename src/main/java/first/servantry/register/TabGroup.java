package first.servantry.register;

import first.servantry.client.creativeTab.AnimInfo;
import net.minecraft.resources.ResourceLocation;

public record TabGroup(int order, ResourceLocation texture, AnimInfo animInfo) {
}
