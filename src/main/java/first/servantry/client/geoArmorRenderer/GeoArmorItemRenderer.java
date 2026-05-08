package first.servantry.client.geoArmorRenderer;

import first.servantry.Servantry;
import first.servantry.common.item.GeoArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class GeoArmorItemRenderer extends GeoArmorRenderer<GeoArmorItem> {

    private final ArmorItem.Type type;

    public GeoArmorItemRenderer(String path, ArmorItem.Type armorType) {
        super(new DefaultedItemGeoModel<GeoArmorItem>(
                        Servantry.rl(String.format("armor/%s", path) + (armorType == ArmorItem.Type.LEGGINGS ? "_leggings" : ""))
                ).withAltTexture(
                        Servantry.rl(String.format("armor/%s", path))
                )
        );
        type = armorType;
    }

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        if (type == ArmorItem.Type.LEGGINGS) {
            setAllVisible(true);
        } else {
            super.applyBoneVisibilityBySlot(currentSlot);
        }
    }

}