package first.summoner.common.minion.goal.scavengerFairy;

import first.lyra.common.entity.PathNode;
import first.lyra.common.minion.MinionGoal;
import first.summoner.common.minion.ScavengerFairy;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ScavengerFairyCollectItemGoal extends MinionGoal<ScavengerFairy> {

    public ScavengerFairyCollectItemGoal(ScavengerFairy minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        Entity targetEntity = minion.getTargetEntity();
        if (targetEntity != null) {
            if (targetEntity instanceof ItemEntity itemEntity) {
                Inventory inventory = minion.getOwner().getInventory();
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack item = inventory.getItem(i);
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
        Entity entity = minion.getTargetEntity();
        if (entity.distanceToSqr(minion.getPos()) <= 1) {
            if (entity instanceof ItemEntity) {
                minion.deliver(entity);
            }
            if (entity instanceof ExperienceOrb experienceOrb) {
                Player owner = minion.getOwner();
                owner.takeXpDelay = 0;
                experienceOrb.playerTouch(owner);
            }
            minion.setTargetEntity(minion.findNearestNewTargetEntity());
            return;
        }
        if (!minion.isExecutingPath()) {
            PathNode start = minion.getCurrentPathNode();
            Vec3 targetPos = entity.getBoundingBox().getCenter();
            Vec3 direction = targetPos.subtract(start.pos()).normalize();
            float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
            float targetPitch = (float) Math.toDegrees(Math.asin(-direction.y));
            PathNode end = new PathNode(targetPos, targetYaw, targetPitch, start.roll());
            List<PathNode> path = new ArrayList<>();
            int tick = 6;
            for (int i = 0; i < tick; i++) {
                float partialTick = (float) i / tick;
                path.add(start.lerp(end, partialTick));
            }
            minion.setPath(path);
        }
    }
}
