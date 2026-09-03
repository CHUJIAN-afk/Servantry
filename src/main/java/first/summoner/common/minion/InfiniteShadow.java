package first.summoner.common.minion;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.minion.Minion;
import first.summoner.register.SummonerAttachmentEntityRegister;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.UsernameCache;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.List;
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
        if (!itemStack.isEmpty() && player != null) {
            fakePlayer.setPos(getPos());
            for (HitContext hit : hitContexts) {
                LivingEntity living = hit.entity();
                if (this.hitTargets.add(living)) {
                    InvincibleData.get(living)
                            .recordHit(owner.getUUID(), 100);
                    int invulnerableTime = living.invulnerableTime;
                    living.invulnerableTime = 0;
                    if (!itemStack.is(Items.TNT)) {
                        player.resetAttackStrengthTicker();
                        player.attack(living);
                    } else {
                        Vec3 point = hit.hitPoint();
                        Level level = owner.level();
                        level.explode(owner, getDamageSource(), null, point.x(), point.y(), point.z(), 1, false, Level.ExplosionInteraction.TNT);
                    }
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
    public void onRemove() {
        if (fakePlayer != null) {
            fakePlayer.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public InfiniteShadowFakePlayer getFakePlayer() {
        Player player = getOwner();
        if (player != null && !player.level().isClientSide()) {
            Level level = player.level();
            if (fakePlayer != null && !fakePlayer.isAlive()) {
                fakePlayer = null;
            }
            if (fakePlayer == null) {
                fakePlayer = new InfiniteShadowFakePlayer((ServerLevel) level, this);
                fakePlayer.setItemSlot(EquipmentSlot.MAINHAND, itemStack.copy());
                Multimap<Holder<Attribute>, AttributeModifier> modifiers = ArrayListMultimap.create();
                for (ItemAttributeModifiers.Entry entry : itemStack.getAttributeModifiers().modifiers()) {
                    modifiers.put(entry.attribute(), entry.modifier());
                }
                fakePlayer.getAttributes().addTransientAttributeModifiers(modifiers);
            }
        }
        return fakePlayer;
    }

    @Override
    public AttachmentEntityType<? extends Minion> getType() {
        return SummonerAttachmentEntityRegister.INFINITE_SHADOW.get();
    }

    public static class InfiniteShadowFakePlayer extends FakePlayer {

        private final InfiniteShadow infiniteShadow;

        public InfiniteShadowFakePlayer(ServerLevel level, InfiniteShadow infiniteShadow) {
            super(level, buildProfile(infiniteShadow));
            this.infiniteShadow = infiniteShadow;
        }

        @Override
        public float getAttackStrengthScale(float adjustTicks) {
            return 0.85F;
        }

        public InfiniteShadow getInfiniteShadow() {
            return infiniteShadow;
        }

        // 26.2: GameProfile 为 final,无法继承伪装名字,直接按 owner 信息构造
        private static GameProfile buildProfile(InfiniteShadow infiniteShadow) {
            UUID id = infiniteShadow.getUuid();
            String name = "infinite_shadow_fake_player";
            Player owner = infiniteShadow.getOwner();
            if (owner != null) {
                id = owner.getUUID();
                String lastKnownUsername = UsernameCache.getLastKnownUsername(owner.getUUID());
                if (lastKnownUsername != null) {
                    name = lastKnownUsername;
                }
            }
            return new GameProfile(id, name);
        }
    }
}
