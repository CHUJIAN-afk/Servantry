package first.summoner.common.item;

import first.lyra.api.LyraHelper;
import first.summoner.common.master.DoomsdayStar;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        DoomsdayStar doomsdayStar = new DoomsdayStar();
        doomsdayStar.setPos(new Vec3(
                spawnPos.getX() + 0.5D,
                spawnPos.getY() + level.getMaxBuildHeight(),
                spawnPos.getZ() + 0.5D
        ));
        doomsdayStar.setPathRotation(player.getYRot(), 0.0F, 0.0F);
        doomsdayStar.setTarget(context.getPlayer());
        LyraHelper.get(serverLevel).add(doomsdayStar);
        return InteractionResult.SUCCESS;
    }

    private static void removeAll(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            LyraHelper.get(level).remove(SummonerAttachmentEntityRegister.DOOMSDAY_STAR.get());
        }
    }
}
