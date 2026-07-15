package first.servantry.api.client.dynamicLight;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import first.servantry.mixin.LevelRendererAccessor;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 动态光照调度器。
 */
public class DynamicLightDispatcher {

    private static final double MAX_RADIUS = 7.75;
    private static final Map<Vec3, Integer> LightSources = new HashMap<>();
    private static final Set<Long> LastUpdateSectionSet = new HashSet<>();
    private static volatile Map<Vec3, Integer> SnapshotLightSources = new HashMap<>();

    public static void addLightSources(Map<Vec3, Integer> lightSources) {
        LightSources.putAll(lightSources);
    }

    public static void addLightSources(Vec3 pos, int light) {
        LightSources.put(pos, light);
    }

    public static void update(LevelRendererAccessor levelRenderer) {
        Set<Long> updateSectionSet = new HashSet<>(LastUpdateSectionSet);
        LastUpdateSectionSet.clear();
        LightSources.forEach((lightPos, luminance) -> {
            SectionPos sectionPos = SectionPos.of(lightPos);
            updateSectionSet.add(sectionPos.asLong());
            Direction dirX = (Mth.floor(lightPos.x) & 15) >= 8 ? Direction.EAST : Direction.WEST;
            Direction dirY = (Mth.floor(lightPos.y) & 15) >= 8 ? Direction.UP : Direction.DOWN;
            Direction dirZ = (Mth.floor(lightPos.z) & 15) >= 8 ? Direction.SOUTH : Direction.NORTH;
            int cx = sectionPos.getX(), cy = sectionPos.getY(), cz = sectionPos.getZ();
            for (int i = 0; i < 7; i++) {
                switch (i % 4) {
                    case 0 -> cx += dirX.getStepX();
                    case 1 -> cz += dirZ.getStepZ();
                    case 2 -> cx -= dirX.getStepX();
                    case 3 -> { cz -= dirZ.getStepZ(); cy += dirY.getStepY(); }
                }
                updateSectionSet.add(SectionPos.asLong(cx, cy, cz));
            }
        });
        SnapshotLightSources = new HashMap<>(LightSources);
        LightSources.clear();
        updateSectionSet.forEach(key -> levelRenderer.callSetSectionDirty(SectionPos.x(key), SectionPos.y(key), SectionPos.z(key), false));
        LastUpdateSectionSet.addAll(updateSectionSet);
    }

    public static int getDynamicLight(BlockAndTintGetter level, BlockState state, BlockPos blockPos, Operation<Integer> original) {
        Map<Vec3, Integer> lights = SnapshotLightSources;
        int originalLight = original.call(level, state, blockPos);
        if (!lights.isEmpty() && !level.getBlockState(blockPos).isSolidRender(level, blockPos)) {
            double maxLight = 0;
            for (Map.Entry<Vec3, Integer> entry : lights.entrySet()) {
                Vec3 pos = entry.getKey();
                int luminance = entry.getValue();
                double dx = blockPos.getX() - pos.x + 0.5;
                double dy = blockPos.getY() - pos.y + 0.5;
                double dz = blockPos.getZ() - pos.z + 0.5;
                double distSq = dx * dx + dy * dy + dz * dz;
                if (distSq <= MAX_RADIUS * MAX_RADIUS) {
                    double contribution = luminance - Math.sqrt(distSq) / MAX_RADIUS * 15.0;
                    if (contribution > maxLight) {
                        maxLight = contribution;
                    }
                }
            }
            if (maxLight > 0) {
                int blockLevel = LightTexture.block(originalLight);
                if (maxLight > blockLevel) {
                    int luminance = (int) (maxLight * 16.0);
                    originalLight &= 0xfff00000;
                    originalLight |= luminance & 0x000fffff;
                }
            }
        }
        return originalLight;
    }
}
