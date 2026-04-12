package first.servantry.common.item;

import first.servantry.api.PathNode;
import first.servantry.api.item.IServantWeapon;
import first.servantry.api.register.ServantType;
import first.servantry.common.attachment.ServantData;
import first.servantry.common.servent.Terraprism;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ServantRegister;
import first.servantry.register.SoundRegister;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.List;

public class TerraPrismItem extends Item implements IServantWeapon<Terraprism> {

    public TerraPrismItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC));
    }

    @Override
    public ServantType<Terraprism> getType() {
        return ServantRegister.TerraPrism.get();
    }

    @Override
    public float getDamage() {
        return 5;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return SoundRegister.UseTerraprism.get();
    }

    @Override
    public void summon(Terraprism servant) {
        Player owner = servant.getOwner();
        ServantData data = owner.getData(AttachmentRegister.ServantData);
        PathNode node = servant.getIdleState(owner, data.getOrder(servant), data.getSameSize(servant));
        servant.setPath(List.of(node));
    }

}
