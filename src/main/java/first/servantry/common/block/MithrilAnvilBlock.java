package first.servantry.common.block;

import com.mojang.serialization.MapCodec;
import first.servantry.client.screen.MithrilAnvilGui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class MithrilAnvilBlock extends FallingBlock {

    public static final VoxelShape SOUTH_SHAPE = Shapes.or(box(4, 0, 2, 12, 2, 14), box(5, 2, 4, 11, 3, 12), box(6, 3, 6, 10, 6, 11), box(4, 6, 3, 12, 7, 13), box(4, 7, 2.5, 12, 7.5, 13.5), box(4, 7.5, 1, 12, 8, 15), box(3, 8, 0, 13, 10, 16));
    public static final VoxelShape NORTH_SHAPE = Shapes.or(box(4, 0, 2, 12, 2, 14), box(5, 2, 4, 11, 3, 12), box(6, 3, 5, 10, 6, 10), box(4, 6, 3, 12, 7, 13), box(4, 7, 2.5, 12, 7.5, 13.5), box(4, 7.5, 1, 12, 8, 15), box(3, 8, 0, 13, 10, 16));
    public static final VoxelShape EAST_SHAPE = Shapes.or(box(2, 0, 4, 14, 2, 12), box(4, 2, 5, 12, 3, 11), box(6, 3, 6, 11, 6, 10), box(3, 6, 4, 13, 7, 12), box(2.5, 7, 4, 13.5, 7.5, 12), box(1, 7.5, 4, 15, 8, 12), box(0, 8, 3, 16, 10, 13));
    public static final VoxelShape WEST_SHAPE = Shapes.or(box(2, 0, 4, 14, 2, 12), box(4, 2, 5, 12, 3, 11), box(5, 3, 6, 10, 6, 10), box(3, 6, 4, 13, 7, 12), box(2.5, 7, 4, 13.5, 7.5, 12), box(1, 7.5, 4, 15, 8, 12), box(0, 8, 3, 16, 10, 13));
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final MapCodec<MithrilAnvilBlock> CODEC = simpleCodec(MithrilAnvilBlock::new);

    public MithrilAnvilBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public @NotNull MapCodec<MithrilAnvilBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getClockWise());
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> Shapes.block();
        };
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!level.isClientSide()) {
            player.openMenu(getMenuProvider(state, level, pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @NotNull
    @Override
    public MenuProvider getMenuProvider(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) -> new MithrilAnvilGui.MithrilAnvilMenu(id, inventory), Component.translatable("container.servantry.mithril_anvil"));
    }

    @Override
    public void falling(@NotNull FallingBlockEntity entity) {
        entity.setHurtsEntities(2.0F, 40);
    }

    @Override
    public void onLand(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull BlockState replaceState, @NotNull FallingBlockEntity entity) {
        super.onLand(level, pos, state, replaceState, entity);
        level.levelEvent(1031, pos, 0); // SOUND_ANVIL_LAND
    }

    @Override
    public void onBrokenAfterFall(@NotNull Level level, @NotNull BlockPos pos, @NotNull FallingBlockEntity entity) {
        level.levelEvent(1029, pos, 0); // SOUND_ANVIL_BROKEN
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType type) {
        return false;
    }

    @Override
    public @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
