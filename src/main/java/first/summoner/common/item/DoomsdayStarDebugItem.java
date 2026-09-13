package first.summoner.common.item;

import first.summoner.common.master.DoomsdayStar;
import first.summoner.register.SummonerEntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DoomsdayStarDebugItem extends Item {

    public DoomsdayStarDebugItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            removeAll(serverLevel.getServer());
            return InteractionResult.SUCCESS;
        }

        DoomsdayStar doomsdayStar = SummonerEntityRegister.DOOMSDAY_STAR.get().create(serverLevel);
        if (doomsdayStar == null) {
            return InteractionResult.FAIL;
        }

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        doomsdayStar.moveTo(
                spawnPos.getX() + 0.5D,
                spawnPos.getY() + level.getMaxBuildHeight(),
                spawnPos.getZ() + 0.5D,
                player.getYRot(),
                0.0F
        );
        serverLevel.addFreshEntity(doomsdayStar);
        return InteractionResult.SUCCESS;
    }

    private static void removeAll(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (DoomsdayStar doomsdayStar : level.getEntities(SummonerEntityRegister.DOOMSDAY_STAR.get(), entity -> true)) {
                doomsdayStar.discard();
            }
        }
    }
}
