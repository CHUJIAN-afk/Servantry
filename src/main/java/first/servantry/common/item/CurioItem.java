package first.servantry.common.item;

import com.google.common.collect.Multimap;
import first.lyra.common.servant.Servant;
import first.lyra.common.servant.ServantDamageSource;
import first.servantry.Servantry;
import first.servantry.common.builder.CurioItemBuilder;
import first.servantry.utils.CuriosUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.apache.commons.lang3.function.TriFunction;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

@EventBusSubscriber(modid = Servantry.MODID)
public class CurioItem extends Item implements ICurioItem {

    private final PreDamageCallback preDamageCallback;
    private final PostDamageCallback postDamageCallback;
    private final TriFunction<SlotContext, ResourceLocation, ItemStack, Multimap<Holder<Attribute>, AttributeModifier>> attributeModifiers;

    public CurioItem(CurioItemBuilder curioItemBuilder) {
        super(curioItemBuilder.properties);
        this.attributeModifiers = curioItemBuilder.attributeModifiers;
        this.preDamageCallback = curioItemBuilder.preDamageCallback;
        this.postDamageCallback = curioItemBuilder.postDamageCallback;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamageEventPre(LivingDamageEvent.Pre event) {
        if (event.getSource() instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            Player owner = servant.getOwner();
            if (!owner.level().isClientSide()) {
                List<CurioItem> curiosItemList = CuriosUtil.getCuriosItemList(owner);
                for (CurioItem curioItem : curiosItemList) {
                    PreDamageCallback callback = curioItem.preDamageCallback;
                    if (callback != null) {
                        float result = callback.apply(servant, owner, event.getEntity(), event.getNewDamage(), servantDamageSource);
                        if (result != event.getNewDamage()) {
                            event.setNewDamage(result);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamageEventPost(LivingDamageEvent.Post event) {
        if (event.getSource() instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            Player owner = servant.getOwner();
            if (!owner.level().isClientSide()) {
                List<CurioItem> curiosItemList = CuriosUtil.getCuriosItemList(owner);
                for (CurioItem curioItem : curiosItemList) {
                    PostDamageCallback callback = curioItem.postDamageCallback;
                    if (callback != null) {
                        callback.accept(servant, owner, event.getEntity(), servantDamageSource);
                    }
                }
            }
        }
    }

    public static CurioItemBuilder builder() {
        return new CurioItemBuilder();
    }

    @FunctionalInterface
    public interface PreDamageCallback {
        float apply(Servant servant, Player player, LivingEntity target, float damage, DamageSource damageSource);
    }

    @FunctionalInterface
    public interface PostDamageCallback {
        void accept(Servant servant, Player owner, LivingEntity target, DamageSource damageSource);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return !CuriosUtil.isEquipped(slotContext.entity(), stack.getItem());
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        return attributeModifiers != null ? attributeModifiers.apply(slotContext, id, stack) : ICurioItem.super.getAttributeModifiers(slotContext, id, stack);
    }
}
