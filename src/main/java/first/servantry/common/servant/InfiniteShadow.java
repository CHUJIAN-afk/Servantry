package first.servantry.common.servant;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentEntityRegister;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.UsernameCache;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class InfiniteShadow extends Terraprism {

    private ItemStack itemStack = ItemStack.EMPTY;
    private InfiniteShadowFakePlayer fakePlayer = null;

    public InfiniteShadow() {
        super();
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public boolean isTarget(LivingEntity target) {
        return super.isTarget(target);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        InfiniteShadowFakePlayer player = getFakePlayer();
        if (player != null) {
            for (HitContext hit : hitContexts) {
                LivingEntity living = hit.entity();
                if (this.hitTargets.add(living)) {
                    int invulnerableTime = living.invulnerableTime;
                    living.invulnerableTime = 0;
                    player.attack(living);
                    living.invulnerableTime = invulnerableTime;
                }
            }
        }
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        super.readAdditional(buf);
        this.itemStack = ItemStack.STREAM_CODEC.decode(buf);
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        super.writeAdditional(buf);

        ItemStack.STREAM_CODEC.encode(buf, this.itemStack);
    }

    @Override
    public float getKnockback() {
        return 0;
    }

    @Override
    public float getDamage() {
        return 0;
    }

    @Override
    public void onRemove() {
        if (fakePlayer != null) {
            fakePlayer.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public InfiniteShadowFakePlayer getFakePlayer() {
        Player player = getOwner();
        if (player != null) {
            Level level = player.level();
            if (fakePlayer == null && !level.isClientSide()) {
                fakePlayer = new InfiniteShadowFakePlayer((ServerLevel) level, this);
            }
        }
        if (fakePlayer != null) {
            fakePlayer.setItemSlot(EquipmentSlot.MAINHAND, itemStack.copy());
            ItemAttributeModifiers attributeModifiers = itemStack.getAttributeModifiers();
            List<ItemAttributeModifiers.Entry> modifiers = attributeModifiers.modifiers();
            Multimap<Holder<Attribute>, AttributeModifier> multimap = HashMultimap.create();
            for (ItemAttributeModifiers.Entry modifier : modifiers) {
                multimap.put(modifier.attribute(), modifier.modifier());
            }
            fakePlayer.getAttributes().addTransientAttributeModifiers(multimap);
            fakePlayer.setPos(getPos());
        }
        return fakePlayer;
    }

    @Override
    public AttachmentEntityType<? extends Servant> getType() {
        return AttachmentEntityRegister.InfiniteShadow.get();
    }

    public static class InfiniteShadowFakePlayer extends FakePlayer {

        private final InfiniteShadow infiniteShadow;

        public InfiniteShadowFakePlayer(ServerLevel level, InfiniteShadow infiniteShadow) {
            super(level, new InfiniteShadowGameProfile(infiniteShadow.getUuid(), "infinite_shadow_fake_player", infiniteShadow.getOwner()));
            this.infiniteShadow = infiniteShadow;
        }

        public InfiniteShadow getInfiniteShadow() {
            return infiniteShadow;
        }

        public static class InfiniteShadowGameProfile extends GameProfile {

            private final Player owner;

            public InfiniteShadowGameProfile(UUID id, String name, Player owner) {
                super(id, name);
                this.owner = owner;
            }

            public UUID getId() {
                return this.owner == null ? super.getId() : this.owner.getUUID();
            }

            public String getName() {
                if (this.owner == null) {
                    return super.getName();
                } else {
                    String lastKnownUsername = UsernameCache.getLastKnownUsername(this.owner.getUUID());
                    return lastKnownUsername == null ? super.getName() : lastKnownUsername;
                }
            }

            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                } else if (!(o instanceof GameProfile otherProfile)) {
                    return false;
                } else {
                    return Objects.equals(this.getId(), otherProfile.getId()) && Objects.equals(this.getName(), otherProfile.getName());
                }
            }

            public int hashCode() {
                UUID id = this.getId();
                String name = this.getName();
                int result = id == null ? 0 : id.hashCode();
                result = 31 * result + (name == null ? 0 : name.hashCode());
                return result;
            }
        }

    }
}
