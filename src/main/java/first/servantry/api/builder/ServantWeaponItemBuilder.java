package first.servantry.api.builder;

import first.servantry.api.ServantryHelper;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.PathNode;
import first.servantry.api.item.IServantWeaponItem;
import first.servantry.api.servant.Servant;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 仆从武器构建器，通过链式配置创建武器物品。
 */
public class ServantWeaponItemBuilder<T extends Servant> {

    public static void handler(PlayerInteractEvent.RightClickItem event) {
        ItemStack itemStack = event.getItemStack();
        Player player = event.getEntity();
        Level level = player.level();
        ItemCooldowns cooldowns = player.getCooldowns();
        if (event.getHand() == InteractionHand.MAIN_HAND && !cooldowns.isOnCooldown(itemStack.getItem()) && itemStack.getItem() instanceof IServantWeaponItem<?> iServantWeaponItem) {
            cooldowns.addCooldown(itemStack.getItem(), 4);
            player.swing(InteractionHand.MAIN_HAND, true);
            if (!level.isClientSide()) {
                if (!player.isShiftKeyDown()) {
                    iServantWeaponItem.summon(player);
                } else {
                    iServantWeaponItem.remove(player);
                }
                SoundEvent soundEvent = iServantWeaponItem.getSoundEvent();
                if (soundEvent != null) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, player.getSoundSource());
                }
            }
        }
    }

    private final Supplier<AttachmentEntityType<T>> typeSupplier;
    private boolean sentryServant = false;
    private float damage = 0;
    private float knockback = 0;
    private float armorPierce = 0;
    private Supplier<SoundEvent> soundEventSupplier = () -> null;
    private BiConsumer<IServantWeaponItem<T>, Player> summonAction = (weapon, player) -> {
        T servant = weapon.createServant(player);
        ServantryHelper servantryHelper = ServantryHelper.get(player);
        if (servantryHelper.canSummon(EntityData.Type.Servant, 1)) {
            AABB box = player.getBoundingBox();
            Vec3 pos = box.getCenter();
            servant.init(new PathNode(pos.offsetRandom(player.getRandom(), 2), 0, 0, 0));
            servantryHelper.add(EntityData.Type.Servant, servant);
        }
    };
    private Consumer<Player> onRemove = null;
    private Consumer<Item.Properties> properties = null;

    public ServantWeaponItemBuilder(@NotNull Supplier<AttachmentEntityType<T>> typeSupplier) {
        this.typeSupplier = typeSupplier;
    }

    /**
     * 设置为哨兵。
     */
    public ServantWeaponItemBuilder<T> sentryServant() {
        this.sentryServant = true;
        return this;
    }

    /**
     * 设置仆从伤害值。
     */
    public ServantWeaponItemBuilder<T> damage(float damage) {
        this.damage = damage;
        return this;
    }

    /**
     * 设置仆从击退力度。
     */
    public ServantWeaponItemBuilder<T> knockback(float knockback) {
        this.knockback = knockback;
        return this;
    }

    /**
     * 设置仆从护甲穿透。
     */
    public ServantWeaponItemBuilder<T> armorPierce(float armorPierce) {
        this.armorPierce = armorPierce;
        return this;
    }

    /**
     * 设置召唤时播放的音效。
     */
    public ServantWeaponItemBuilder<T> sound(Supplier<SoundEvent> soundEventSupplier) {
        this.soundEventSupplier = soundEventSupplier;
        return this;
    }

    /**
     * 完整重写召唤逻辑，weapon 可调用 createServant 构建实例。
     */
    public ServantWeaponItemBuilder<T> summon(BiConsumer<IServantWeaponItem<T>, Player> action) {
        this.summonAction = action;
        return this;
    }

    /**
     * 设置仆从移除回调。
     */
    public ServantWeaponItemBuilder<T> onRemove(Consumer<Player> action) {
        this.onRemove = action;
        return this;
    }

    public ServantWeaponItemBuilder<T> properties(Consumer<Item.Properties> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * 构建武器物品。
     */
    public Item build() {
        Item.Properties proper = new Item.Properties().stacksTo(1);
        if (properties != null) {
            properties.accept(proper);
        }
        return new ServantWeaponItemItem(proper);
    }

    private class ServantWeaponItemItem extends Item implements IServantWeaponItem<T> {

        public ServantWeaponItemItem(Properties p) {
            super(p);
        }

        @Override
        public AttachmentEntityType<T> getType() {
            return typeSupplier.get();
        }

        @Override
        public boolean isSentryServant() {
            return sentryServant;
        }

        @Override
        public float getDamage() {
            return damage;
        }

        @Override
        public float getArmorPierce() {
            return armorPierce;
        }

        @Override
        public float getKnockback() {
            return knockback;
        }

        @Override
        public SoundEvent getSoundEvent() {
            return soundEventSupplier.get();
        }

        @Override
        public void summon(Player player) {
            summonAction.accept(this, player);
        }

        @Override
        public void remove(Player player) {
            if (onRemove != null) {
                onRemove.accept(player);
            } else {
                IServantWeaponItem.super.remove(player);
            }
        }
    }
}
