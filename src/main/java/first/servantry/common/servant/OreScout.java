package first.servantry.common.servant;

import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.register.ServantryAttachmentEntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OreScout extends Servant {

    private final List<BlockPos> highlightedOres = new ArrayList<>();
    private int scanCooldown = 0;

    @Override
    public void tick() {
        if (!getOwner().level().isClientSide()) {
            setPath(Collections.singletonList(getInterpolatedIdleState(1.0f)));
        } else {
            if (--scanCooldown <= 0) {
                scanCooldown = 5;
                scanNearbyOres(16);
            }
        }
        super.tick();
    }

    @Override
    public AttachmentEntityType<? extends Servant> getType() {
        return ServantryAttachmentEntityRegister.OreScout.get();
    }

    @Override
    public int getSearchDistance() {
        return 0;
    }

    @Override
    public boolean isTarget(LivingEntity target) {
        return false;
    }

    public List<BlockPos> getHighlightedOres() {
        return Collections.unmodifiableList(highlightedOres);
    }

    public PathNode getInterpolatedIdleState(float partialTick) {
        var owner = getOwner();
        double px = Mth.lerp(partialTick, owner.xo, owner.getX());
        double py = Mth.lerp(partialTick, owner.yo, owner.getY());
        double pz = Mth.lerp(partialTick, owner.zo, owner.getZ());
        float bob = Mth.sin((owner.tickCount + partialTick) * 0.12f) * 0.05f;
        Vec3 targetPos = new Vec3(px, py, pz).add(0, owner.getBbHeight() + 1.05f + bob, 0);
        return new PathNode(targetPos, 0, 0, 0);
    }

    @SuppressWarnings("SameParameterValue")
    private void scanNearbyOres(int radius) {
        highlightedOres.clear();
        BlockPos center = BlockPos.containing(getPos());
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= radius * radius) {
                        cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                        BlockState blockState = getOwner().level().getBlockState(cursor);
                        if (!blockState.isAir() && isOre(blockState)) {
                            highlightedOres.add(cursor.immutable());
                        }
                    }
                }
            }
        }
    }

    private boolean isOre(BlockState state) {
        return state.getTags().map(TagKey::location).anyMatch(this::isOreTag);
    }

    private boolean isOreTag(ResourceLocation location) {
        String path = location.getPath();
        return path.equals("ores")
                || path.startsWith("ores/")
                || path.endsWith("/ores")
                || path.contains("/ores/")
                || path.endsWith("_ores")
                || path.endsWith("_ore");
    }
}
