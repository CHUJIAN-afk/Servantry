package first.servantry.register;

import first.servantry.Servantry;
import first.servantry.api.ActiveMarker;
import first.servantry.api.ServantryUtil;
import first.servantry.api.register.MarkerType;
import first.servantry.api.servant.Servant;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MarkerRegister {

    private static final DeferredRegister<MarkerType> Register = DeferredRegister.create(first.servantry.api.register.Registries.MARKER_TYPES, Servantry.MODID);

    public static final DeferredHolder<MarkerType, MarkerType> COBWEB_MARK = Register.register("cobweb", () -> new MarkerType(0.6f, 0, 40));
    public static final DeferredHolder<MarkerType, MarkerType> SLIME_MARK = Register.register("slime_whip", () -> new MarkerType(0.6f, 0, 40));
    public static final DeferredHolder<MarkerType, MarkerType> LEATHER_MARK = Register.register("leather_whip", () -> new MarkerType(0.8f, 0, 40));
    public static final DeferredHolder<MarkerType, MarkerType> SOULSCOURGE_MARK = Register.register("soulscourge", () -> new MarkerType(1f, 0, 40));
    public static final DeferredHolder<MarkerType, MarkerType> VASCULASH_MARK = Register.register("vasculash", () -> new MarkerType(1f, 0, 40));
    public static final DeferredHolder<MarkerType, MarkerType> STARCRASH_MARK = Register.register("starcrash", () -> new MarkerType(0.4f, 0, 40) {

        @Override
        public void onServantHit(LivingEntity target, Servant servant, Player owner, ActiveMarker activeMarker, float damageAmount) {
            if (!activeMarker.isHited()) {
                activeMarker.setHited(true);
                Level level = target.level();
                if (!level.isClientSide()) {
                    SmallFireball fireball = new SmallFireball(EntityType.SMALL_FIREBALL, level);
                    Vec3 pos = new Vec3(target.getRandomX(4), target.getY() + 3, target.getRandomZ(4));
                    fireball.setPos(pos);
                    fireball.setDeltaMovement(target.getHitbox().getCenter().subtract(pos).normalize());
                    ServantryUtil.addEntity(level, fireball);
                    ServantryUtil.playSound(level, fireball.position(), SoundEvents.GHAST_SHOOT, fireball.getSoundSource());
                }
            }
        }

    });

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }

}