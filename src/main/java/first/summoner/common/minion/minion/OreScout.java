package first.summoner.common.minion.minion;

import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.minion.Minion;
import first.lyra.utils.ParticleHelper;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.Optional;

public class OreScout extends Minion {

    private BlockPos oreTarget = null;
    private float miningProgress = 0;
    private int searchRadius = 1;

    public OreScout() {
        super(SummonerAttachmentEntityRegister.ORE_SCOUT);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ByteBufCodecs.optional(BlockPos.STREAM_CODEC), () -> Optional.ofNullable(oreTarget), pos -> oreTarget = pos.orElse(null));
    }

    @Override
    public void tick() {
        super.tick();
        Level level = getLevel();

        setPath(Collections.singletonList(getRenderNode(1.0f)));
        if (oreTarget == null) {
            oreTarget = getNearestOrePos(searchRadius);
            searchRadius = searchRadius >= 16 ? 1 : searchRadius + 1;
        }
        if (oreTarget != null) {
            BlockState state = level.getBlockState(oreTarget);
            float destroySpeed = state.getDestroySpeed(level, oreTarget);
            if (destroySpeed < 0 || !isOre(state)) {
                oreTarget = null;
                miningProgress = 0;
            } else {
                miningProgress += 1f / Math.max(1f, destroySpeed * 20f);
                ParticleHelper.create(level)
                        .type(new DustParticleOptions(new Vector3f(1f, 0f, 0f), 1f))
                        .pos(Vec3.atCenterOf(oreTarget))
                        .velocity(0, 0.2, 0)
                        .count(4)
                        .speed(0.15)
                        .offset(0.5)
                        .count(10)
                        .emit();
                if (miningProgress >= 1f) {
                    level.destroyBlock(oreTarget, true, owner);
                    oreTarget = null;
                    miningProgress = 0;
                }
            }
        }
    }

    public BlockPos getOreTarget() {
        return oreTarget;
    }

    @Override
    public int getSearchDistance() {
        return 0;
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
        return state.is(Blocks.NETHER_QUARTZ_ORE) || state.is(Blocks.ANCIENT_DEBRIS);
    }
}
