package first.summoner.common.minion.minion;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import first.lyra.common.attachment.ImmunityData;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.List;

public class InfiniteShadow extends Terraprism {

    private ItemStack itemStack = ItemStack.EMPTY;
    private InfiniteShadowFakePlayer fakePlayer = null;

    public InfiniteShadow() {
        super(SummonerAttachmentEntityRegister.INFINITE_SHADOW);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ItemStack.STREAM_CODEC, this::getItemStack, this::setItemStack);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        InfiniteShadowFakePlayer player = getFakePlayer();
        if (!itemStack.isEmpty() && player != null) {
            fakePlayer.setPos(getPos());
            for (HitContext hit : hitContexts) {
                LivingEntity living = hit.entity();
                if (this.hitTargets.add(living)) {
                    ImmunityData.get(living).record(owner.getUUID(), 100);
                    int invulnerableTime = living.invulnerableTime;
                    living.invulnerableTime = 0;
                    if (!itemStack.is(Items.TNT)) {
                        player.resetAttackStrengthTicker();
                        player.attack(living);
                    } else {
                        Vec3 point = hit.hitPoint();
                        Level level = getLevel();
                        level.explode(owner, getDamageSource(), null, point.x(), point.y(), point.z(), 1, false, Level.ExplosionInteraction.TNT);
                    }
                    living.invulnerableTime = invulnerableTime;
                }
            }
        }
    }

    @Override
    public void onRemove() {
        if (fakePlayer != null) {
            fakePlayer.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public InfiniteShadowFakePlayer getFakePlayer() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            if (fakePlayer != null && !fakePlayer.isAlive()) {
                fakePlayer = null;
            }
            if (fakePlayer == null) {
                fakePlayer = new InfiniteShadowFakePlayer(serverLevel, this);
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

    public static class InfiniteShadowFakePlayer extends FakePlayer {

        private final InfiniteShadow infiniteShadow;

        public InfiniteShadowFakePlayer(ServerLevel level, InfiniteShadow infiniteShadow) {
            super(level, new GameProfile(infiniteShadow.getUuid(), "infinite_shadow_fake_player"));
            this.infiniteShadow = infiniteShadow;
        }

        @Override
        public float getAttackStrengthScale(float adjustTicks) {
            return 1;
        }

        public InfiniteShadow getInfiniteShadow() {
            return infiniteShadow;
        }
    }
}
