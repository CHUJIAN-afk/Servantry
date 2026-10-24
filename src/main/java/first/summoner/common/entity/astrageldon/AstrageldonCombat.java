package first.summoner.common.entity.astrageldon;

import first.summoner.Summoner;
import first.summoner.register.SummonerMobEffectRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class AstrageldonCombat {

    public static final float PROJECTILE_DAMAGE = 13.7f;
    public static final float STAR_DAMAGE = 10.2f;
    private static final ResourceKey<DamageType> DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, Summoner.rl("astrageldon"));

    public static void hurtPlayers(Entity direct, @Nullable Entity owner, float damage, AABB previous, Consumer<Player> onHit) {
        AABB current = direct.getBoundingBox();
        Vec3 from = previous.getCenter();
        Vec3 to = current.getCenter();
        for (Player player : direct.level().getEntitiesOfClass(Player.class, previous.minmax(current), player -> player.isAlive() && !player.isSpectator())) {
            AABB hitbox = player.getBoundingBox().inflate(direct.getBbWidth() / 2, direct.getBbHeight() / 2, direct.getBbWidth() / 2);
            if (hitbox.contains(from) || hitbox.clip(from, to).isPresent()) {
                DamageSource source = new DamageSource(direct.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DAMAGE), direct, owner);
                if (player.hurt(source, damage)) {
                    onHit.accept(player);
                }
            }
        }
    }

    public static void armorCrunch(Player player) {
        player.addEffect(new MobEffectInstance(SummonerMobEffectRegister.ArmorCrunch, 80));
    }

    public static void astralBlight(Player player) {
        player.addEffect(new MobEffectInstance(SummonerMobEffectRegister.AstralBlight, 80));
    }
}
