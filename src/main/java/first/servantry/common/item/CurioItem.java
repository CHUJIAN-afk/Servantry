package first.servantry.common.item;

import com.google.common.collect.Multimap;
import first.servantry.Servantry;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.utils.CuriosUtil;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
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
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@EventBusSubscriber(modid = Servantry.MODID)
public class CurioItem extends Item implements ICurioItem {

    private final PreDamageCallback preDamageCallback;
    private final PostDamageCallback postDamageCallback;

    protected CurioItem(Builder builder) {
        super(builder.properties);
        this.equipSound = builder.equipSound;
        this.canEquipFromUse = builder.canEquipFromUse;
        this.dropRule = builder.dropRule;
        this.canEquip = builder.canEquip;
        this.canUnequip = builder.canUnequip;
        this.attributeModifiers = builder.attributeModifiers;
        this.canSync = builder.canSync;
        this.writeSyncData = builder.writeSyncData;
        this.readSyncData = builder.readSyncData;
        this.preDamageCallback = builder.preDamageCallback;
        this.postDamageCallback = builder.postDamageCallback;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamageEventPre(LivingDamageEvent.Pre event) {
        if (event.getSource() instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            if (servant != null) {
                Player owner = servant.getOwner();
                if (!owner.level().isClientSide()) {
                    List<CurioItem> curiosItemList = CuriosUtil.getCuriosItemList(owner);
                    for (CurioItem curioItem : curiosItemList) {
                        if (curioItem.preDamageCallback != null) {
                            float result = curioItem.preDamageCallback.apply(servant, owner, event.getEntity(), event.getNewDamage());
                            if (result != event.getNewDamage()) {
                                event.setNewDamage(result);
                            }
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
            if (servant != null) {
                Player owner = servant.getOwner();
                if (!owner.level().isClientSide()) {
                    List<CurioItem> curiosItemList = CuriosUtil.getCuriosItemList(owner);
                    for (CurioItem curioItem : curiosItemList) {
                        if (curioItem.postDamageCallback != null) {
                            curioItem.postDamageCallback.accept(servant, owner, event.getEntity());
                        }
                    }
                }
            }
        }
    }

    private final ICurio.SoundInfo equipSound;
    private final boolean canEquipFromUse;
    private final ICurio.DropRule dropRule;
    private final BiFunction<SlotContext, ItemStack, Boolean> canEquip;
    private final BiFunction<SlotContext, ItemStack, Boolean> canUnequip;
    private final TriFunction<SlotContext, ResourceLocation, ItemStack, Multimap<Holder<Attribute>, AttributeModifier>> attributeModifiers;
    private final BiFunction<SlotContext, ItemStack, Boolean> canSync;
    private final BiFunction<SlotContext, ItemStack, CompoundTag> writeSyncData;
    private final TriConsumer<SlotContext, CompoundTag, ItemStack> readSyncData;

    public static Builder builder() {
        return new Builder();
    }

    @FunctionalInterface
    public interface PreDamageCallback {
        float apply(Servant servant, Player player, LivingEntity target, float damage);
    }

    @FunctionalInterface
    public interface PostDamageCallback {
        void accept(Servant servant, Player owner, LivingEntity target);
    }

    @NotNull
    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return equipSound != null ? equipSound : ICurioItem.super.getEquipSound(slotContext, stack);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return canEquipFromUse;
    }

    @NotNull
    @Override
    public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, boolean recentlyHit, ItemStack stack) {
        return dropRule != null ? dropRule : ICurioItem.super.getDropRule(slotContext, source, recentlyHit, stack);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return canEquip != null ? canEquip.apply(slotContext, stack) : !CuriosUtil.isEquipped(slotContext.entity(), stack.getItem());
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return canUnequip != null ? canUnequip.apply(slotContext, stack) : ICurioItem.super.canUnequip(slotContext, stack);
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        return attributeModifiers != null ? attributeModifiers.apply(slotContext, id, stack) : ICurioItem.super.getAttributeModifiers(slotContext, id, stack);
    }

    @Override
    public boolean canSync(SlotContext slotContext, ItemStack stack) {
        return canSync != null ? canSync.apply(slotContext, stack) : ICurioItem.super.canSync(slotContext, stack);
    }

    @NotNull
    @Override
    public CompoundTag writeSyncData(SlotContext slotContext, ItemStack stack) {
        return writeSyncData != null ? writeSyncData.apply(slotContext, stack) : ICurioItem.super.writeSyncData(slotContext, stack);
    }

    @Override
    public void readSyncData(SlotContext slotContext, CompoundTag compound, ItemStack stack) {
        if (readSyncData != null) {
            readSyncData.accept(slotContext, compound, stack);
        } else {
            ICurioItem.super.readSyncData(slotContext, compound, stack);
        }
    }

    @FunctionalInterface
    public interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }

    public static class Builder {
        private final Properties properties;
        private ICurio.SoundInfo equipSound;
        private boolean canEquipFromUse = false;
        private ICurio.DropRule dropRule;
        private BiFunction<SlotContext, ItemStack, Boolean> canEquip;
        private BiFunction<SlotContext, ItemStack, Boolean> canUnequip;
        private TriFunction<SlotContext, ResourceLocation, ItemStack, Multimap<Holder<Attribute>, AttributeModifier>> attributeModifiers;
        private BiFunction<SlotContext, ItemStack, Boolean> canSync;
        private BiFunction<SlotContext, ItemStack, CompoundTag> writeSyncData;
        private TriConsumer<SlotContext, CompoundTag, ItemStack> readSyncData;
        private PreDamageCallback preDamageCallback;
        private PostDamageCallback postDamageCallback;

        private Builder() {
            this.properties = new Properties().stacksTo(1);
        }

        public Builder equipSound(ICurio.SoundInfo equipSound) {
            this.equipSound = equipSound;
            return this;
        }

        public Builder canEquipFromUse(boolean canEquipFromUse) {
            this.canEquipFromUse = canEquipFromUse;
            return this;
        }

        public Builder dropRule(ICurio.DropRule dropRule) {
            this.dropRule = dropRule;
            return this;
        }

        public Builder canEquip(BiFunction<SlotContext, ItemStack, Boolean> canEquip) {
            this.canEquip = canEquip;
            return this;
        }

        public Builder canUnequip(BiFunction<SlotContext, ItemStack, Boolean> canUnequip) {
            this.canUnequip = canUnequip;
            return this;
        }

        public Builder attributeModifiers(TriFunction<SlotContext, ResourceLocation, ItemStack, Multimap<Holder<Attribute>, AttributeModifier>> attributeModifiers) {
            this.attributeModifiers = attributeModifiers;
            return this;
        }

        public Builder canSync(BiFunction<SlotContext, ItemStack, Boolean> canSync) {
            this.canSync = canSync;
            return this;
        }

        public Builder writeSyncData(BiFunction<SlotContext, ItemStack, CompoundTag> writeSyncData) {
            this.writeSyncData = writeSyncData;
            return this;
        }

        public Builder readSyncData(TriConsumer<SlotContext, CompoundTag, ItemStack> readSyncData) {
            this.readSyncData = readSyncData;
            return this;
        }

        public Builder onPreDamage(PreDamageCallback callback) {
            this.preDamageCallback = callback;
            return this;
        }

        public Builder onPostDamage(PostDamageCallback callback) {
            this.postDamageCallback = callback;
            return this;
        }

        public Builder properties(Consumer<Item.Properties> customizer) {
            customizer.accept(this.properties);
            return this;
        }

        public CurioItem build() {
            return new CurioItem(this);
        }
    }
}
