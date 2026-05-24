package first.servantry.common.servant.goal.scavengerFairy;

import first.servantry.api.PathNode;
import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.ScavengerFairy;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ScavengerFairyCollectItemGoal extends ServantGoal<ScavengerFairy> {

    public ScavengerFairyCollectItemGoal(ScavengerFairy servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        Entity targetEntity = servant.getTargetEntity();
        if (targetEntity != null) {
            if (targetEntity instanceof ItemEntity itemEntity) {
                NonNullList<ItemStack> items = servant.getOwner().getInventory().items;
                for (ItemStack item : items) {
                    if (item.isEmpty()) {
                        return true;
                    }
                    if (item.getCount() < item.getMaxStackSize() && itemEntity.getItem().is(item.getItem())) {
                        return true;
                    }
                }
            }
            return targetEntity instanceof ExperienceOrb;
        }
        return false;
    }

    @Override
    public void tick() {
        Entity entity = servant.getTargetEntity();
        if (entity.distanceToSqr(servant.getPos()) <= 0.5) {

            servant.deliver(entity);
            servant.setTargetEntity(null);
        }
        if (!servant.isExecutingPath()) {
            PathNode start = servant.getCurrentPathNode();
            Vec3 targetPos = entity.getBoundingBox().getCenter();
            Vec3 direction = targetPos.subtract(start.pos()).normalize();
            float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
            float targetPitch = (float) Math.toDegrees(Math.asin(-direction.y));
            PathNode end = new PathNode(targetPos, targetYaw, targetPitch, start.roll());
            List<PathNode> path = new ArrayList<>();
            int tick = 4;
            for (int i = 0; i < tick; i++) {
                float partialTick = (float) i / tick;
                path.add(start.lerp(end, partialTick));
            }
            servant.setPath(path);
        }
    }
}
