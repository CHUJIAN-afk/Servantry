package first.servantry.api.item;

import first.servantry.api.PathNode;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.Servant;
import first.servantry.common.attachment.ServantData;
import first.servantry.register.AttachmentRegister;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

/**
 * 为物品实现此接口，在右键时召唤仆从，潜行右键时移除最早召唤的该类型的一个仆从
 * 同时将会在物品提示中显示类型和仆从栏数量
 */
public interface IServantWeapon<T extends Servant> {

    static <T extends Servant> void handleSummon(Player player, IServantWeapon<T> weapon) {
        T servant = weapon.getType().factory().apply(PathNode.Empty);
        servant.setOwner(player);
        ServantData data = player.getData(AttachmentRegister.ServantData);
        if (data.summon(player, servant)) {
            weapon.summon(servant);
        }
    }

    /**
     * 获取武器的仆从类型
     */
    ServantType<T> getType();

    /**
     * 成功召唤仆从时
     * @param servant 新加入召唤栏的仆从
     */
    void summon(T servant);

    /**
     * 获得使用音效
     */
    default SoundEvent getSoundEvent() {
        return null;
    }

    /**
     * 潜行右键试图移除仆从时
     */
    default void remove(Player player) {
        player.getData(AttachmentRegister.ServantData).remove(getType());
    }

}
