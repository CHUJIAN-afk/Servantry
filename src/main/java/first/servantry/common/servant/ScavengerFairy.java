package first.servantry.common.servant;

import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.servant.goal.scavengerFairy.ScavengerFairyCollectItemGoal;
import first.servantry.common.servant.goal.scavengerFairy.ScavengerFairyIdleGoal;
import first.servantry.register.AttachmentEntityRegister;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public class ScavengerFairy extends Servant {

    private Entity targetEntity;

    public ScavengerFairy() {
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new ScavengerFairyCollectItemGoal(this));
        goalSelector.addGoal(1, new ScavengerFairyIdleGoal(this));
    }

    @Override
    public void tick() {
        if (!getOwner().level().isClientSide()) {
            if (targetEntity == null) {
                setTargetEntity(findNearestTargetEntity());
            }
        }
        super.tick();
    }

    @Override
    public float getDamage() {
        return 0.0f;
    }

    @Override
    public float getKnockback() {
        return 0.0f;
    }

    @Override
    public AttachmentEntityType<? extends Servant> getType() {
        return AttachmentEntityRegister.ScavengerFairy.get();
    }

    @Override
    public LivingEntity searchTarget() {
        return null;
    }

    @Override
    public boolean isTarget(LivingEntity target) {
        return false;
    }

    public PathNode getInterpolatedIdleState(float partialTick) {
        Player owner = getOwner();
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
        float bob = Mth.sin((owner.tickCount + partialTick) * 0.16f) * 0.035f;
        Vec3 pos = new Vec3(px, py, pz)
                .add(rightX * 0.35 + backX * 0.12, owner.getBbHeight() + 0.45 + bob, rightZ * 0.35 + backZ * 0.12);
        return new PathNode(pos, playerYaw, 0, 0);
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
    }

    public Entity findNearestNewTargetEntity() {
        Player owner = getOwner();
        return owner.level().getEntitiesOfClass(Entity.class, owner.getBoundingBox().inflate(10.0)).stream()
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
        Player owner = getOwner();
        return owner.level().getEntitiesOfClass(Entity.class, owner.getBoundingBox().inflate(10.0)).stream()
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
        Player owner = getOwner();
        Vec3 pos = owner.position();
        entity.setDeltaMovement(Vec3.ZERO);
        entity.teleportTo(pos.x, pos.y + 0.1, pos.z);
    }
}
