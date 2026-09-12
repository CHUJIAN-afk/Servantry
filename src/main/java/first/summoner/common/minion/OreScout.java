package first.summoner.common.minion;

import first.lyra.common.entity.PathNode;
import first.lyra.common.minion.Minion;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;

public class OreScout extends Minion {

    private BlockPos target = null;
    private int searchRadius = 1;

    public OreScout() {
        super(SummonerAttachmentEntityRegister.ORE_SCOUT);
    }

    @Override
    public void tick() {
        Level level = getLevel();
        if (!level.isClientSide()) {
            setPath(Collections.singletonList(getRenderNode(1.0f)));
            if (target == null) {
                target = getNearestOrePos(searchRadius);
                searchRadius = searchRadius >= 16 ? 1 : searchRadius + 1;
            }
            if (target != null) {
                level.destroyBlock(target, true, owner);
                target = null;
            }
        }
        super.tick();
    }

    @Override
    public int getSearchDistance() {
        return 0;
    }

    @Override
    public boolean isTarget(LivingEntity target) {
        return false;
    }

    @Override
    public PathNode getRenderNode(float partialTick) {
        PathNode node = getCurrentPathNode();
        if (owner != null) {
            double px = Mth.lerp(partialTick, owner.xo, owner.getX());
            double py = Mth.lerp(partialTick, owner.yo, owner.getY());
            double pz = Mth.lerp(partialTick, owner.zo, owner.getZ());
            float bob = Mth.sin((tickCount + partialTick) * 0.12f) * 0.05f;
            Vec3 targetPos = new Vec3(px, py, pz).add(0, owner.getBbHeight() + 1.05f + bob, 0);
            node = new PathNode(targetPos, 0, 0, 0);
        }
        return node;
    }

    private BlockPos getNearestOrePos(int radius) {
        Level level = getLevel();
        BlockPos center = BlockPos.containing(getPos());
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int radiusSquared = radius * radius;
        int innerSquared = (radius - 1) * (radius - 1);
        // 仅搜索当前半径的球壳 (inner, radius]，由调用方逐层扩大半径
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int distanceSquared = x * x + y * y + z * z;
                    if (distanceSquared <= radiusSquared && distanceSquared > innerSquared) {
                        cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                        BlockState blockState = level.getBlockState(cursor);
                        if (!blockState.isAir() && isOre(blockState)) {
                            return cursor.immutable();
                        }
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static final TagKey<Block>[] ORE_TAGS = (TagKey<Block>[]) new TagKey[]{BlockTags.COAL_ORES, BlockTags.COPPER_ORES, BlockTags.DIAMOND_ORES, BlockTags.EMERALD_ORES, BlockTags.GOLD_ORES, BlockTags.IRON_ORES, BlockTags.LAPIS_ORES, BlockTags.REDSTONE_ORES};

    private boolean isOre(BlockState state) {
        for (TagKey<Block> tag : ORE_TAGS) {
            if (state.is(tag)) {
                return true;
            }
        }
        return false;
    }
}
