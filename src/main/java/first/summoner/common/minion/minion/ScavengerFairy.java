package first.summoner.common.minion.minion;


import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.minion.Minion;
import first.summoner.common.minion.minion.goal.scavengerFairy.ScavengerFairyCollectItemGoal;
import first.summoner.common.minion.minion.goal.scavengerFairy.ScavengerFairyIdleGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public class ScavengerFairy extends Minion {

    private Entity targetEntity;

    public ScavengerFairy() {
        super(SummonerAttachmentEntityRegister.SCAVENGER_FAIRY);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new ScavengerFairyCollectItemGoal(this));
        goalSelector.addGoal(1, new ScavengerFairyIdleGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (targetEntity == null) {
            setTargetEntity(findNearestTargetEntity());
        }
    }

    @Override
    public int getSearchDistance() {
        return 0;
    }

    public PathNode getInterpolatedIdleState(float partialTick) {
        Player owner = (Player) getOwner();
        PathNode node = getCurrentPathNode();
        double px = Mth.lerp(partialTick, owner.xo, owner.getX());
        double py = Mth.lerp(partialTick, owner.yo, owner.getY());
        double pz = Mth.lerp(partialTick, owner.zo, owner.getZ());
        float bodyYaw = Mth.rotLerp(partialTick, owner.yBodyRotO, owner.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, owner.yHeadRotO, owner.yHeadRot);
        float playerYaw = Mth.wrapDegrees(bodyYaw + Mth.wrapDegrees(headYaw - bodyYaw) * 0.5f);
        float rad = (float) Math.toRadians(-playerYaw + 180);
        float backX = Mth.sin(rad);
        float backZ = Mth.cos(rad);
        float rightX = Mth.cos(rad);
        float rightZ = -Mth.sin(rad);
        float bob = Mth.sin((tickCount + partialTick) * 0.16f) * 0.035f;
        Vec3 pos = new Vec3(px, py, pz).add(rightX * 0.35 + backX * 0.12, owner.getBbHeight() + 0.45 + bob, rightZ * 0.35 + backZ * 0.12);
        node = new PathNode(pos, playerYaw, 0, 0);
        return node;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
    }

    public Entity findNearestNewTargetEntity() {
        Player owner = (Player) getOwner();
        AABB searchBox = owner.getBoundingBox().inflate(10.0);
        return getLevel().getEntitiesOfClass(Entity.class, searchBox).stream()
                .filter(entity -> entity != targetEntity)
                .filter(this::isValidTarget)
                .min(Comparator.comparingDouble(entity -> {
                    double distanceToSqr = entity.distanceToSqr(getPos());
                    if (entity instanceof ExperienceOrb) {
                        distanceToSqr += 100;
                    }
                    return distanceToSqr;
                }))
                .orElse(null);
    }

    public Entity findNearestTargetEntity() {
        Player owner = (Player) getOwner();
        AABB searchBox = owner.getBoundingBox().inflate(10.0);
        return getLevel().getEntitiesOfClass(Entity.class, searchBox).stream()
                .filter(this::isValidTarget)
                .min(Comparator.comparingDouble(entity -> {
                    double distanceToSqr = entity.distanceToSqr(getPos());
                    if (entity instanceof ExperienceOrb) {
                        distanceToSqr += 100;
                    }
                    return distanceToSqr;
                }))
                .orElse(null);
    }

    public boolean isValidTarget(Entity entity) {
        if (entity.isAlive()) {
            if (entity instanceof ItemEntity itemEntity) {
                if (!itemEntity.getItem().isEmpty() && !itemEntity.hasPickUpDelay()) {
                    return true;
                }
            }
            return entity instanceof ExperienceOrb;
        }
        return false;
    }

    public void deliver(Entity entity) {
        Player owner = (Player) getOwner();
        Vec3 pos = owner.position();
        entity.setDeltaMovement(Vec3.ZERO);
        entity.teleportTo(pos.x, pos.y + 0.1, pos.z);
    }
}
