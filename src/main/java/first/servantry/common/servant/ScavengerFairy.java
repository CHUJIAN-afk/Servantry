package first.servantry.common.servant;

import first.servantry.api.PathNode;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentEntityRegister;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class ScavengerFairy extends MomentumServant {

    private static final double SEARCH_RADIUS = 10.0;
    private static final double DELIVERY_DISTANCE = 1.5;
    private static final double MAX_SPEED = 0.32;
    private static final double ACCELERATION = 0.09;

    private ItemEntity targetItem;
    private int retargetCooldown;
    private int soundCooldown;

    public ScavengerFairy() {
        setDrag(0.86f);
        setRotationSpeed(8.0f);
    }

    @Override
    public void tick() {
        if (!getOwner().level().isClientSide()) {
            tickDelivery();
        }
        super.tick();
    }

    @Override
    public int getSlotCost() {
        return 0;
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
        Vec3 look = owner.getLookAngle().multiply(1, 0, 1);
        if (look.lengthSqr() < 1.0E-4) {
            look = new Vec3(0, 0, 1);
        }
        look = look.normalize();
        Vec3 right = new Vec3(-look.z, 0, look.x);
        float bob = Mth.sin((owner.tickCount + partialTick) * 0.18f) * 0.08f;
        Vec3 pos = new Vec3(px, py, pz)
                .add(right.scale(0.85))
                .add(look.scale(-0.65))
                .add(0, owner.getBbHeight() * 0.75 + bob, 0);
        return new PathNode(pos, owner.getYRot(), 0, 0);
    }

    private void tickDelivery() {
        if (retargetCooldown > 0) {
            retargetCooldown--;
        }
        if (soundCooldown > 0) {
            soundCooldown--;
        }
        if (!isValidTarget(targetItem)) {
            targetItem = null;
        }
        if (targetItem == null && retargetCooldown <= 0) {
            targetItem = findNearestItem();
            retargetCooldown = 5;
        }
        if (targetItem == null) {
            moveToward(getInterpolatedIdleState(1.0f).pos(), 0.045);
            return;
        }
        Vec3 targetPos = targetItem.getBoundingBox().getCenter();
        if (getPos().distanceTo(targetPos) <= DELIVERY_DISTANCE) {
            deliver(targetItem);
            targetItem = null;
            retargetCooldown = 2;
            return;
        }
        moveToward(targetPos, ACCELERATION);
        lookAtPos(targetPos);
    }

    private ItemEntity findNearestItem() {
        Player owner = getOwner();
        Level level = owner.level();
        AABB area = owner.getBoundingBox().inflate(SEARCH_RADIUS);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area, this::isValidTarget);
        return items.stream()
                .min(Comparator.comparingDouble(item -> item.distanceToSqr(getPos())))
                .orElse(null);
    }

    private boolean isValidTarget(ItemEntity item) {
        if (item == null || !item.isAlive() || item.getItem().isEmpty()) {
            return false;
        }
        if (item.hasPickUpDelay()) {
            return false;
        }
        return item.distanceToSqr(getOwner()) <= SEARCH_RADIUS * SEARCH_RADIUS;
    }

    private void deliver(ItemEntity item) {
        Player owner = getOwner();
        Vec3 pos = owner.position();
        item.setDeltaMovement(Vec3.ZERO);
        item.teleportTo(pos.x, pos.y + 0.1, pos.z);
        if (soundCooldown <= 0) {
            owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.ITEM_PICKUP, owner.getSoundSource(), 0.25f, 1.6f);
            soundCooldown = 6;
        }
    }

    private void moveToward(Vec3 targetPos, double acceleration) {
        Vec3 offset = targetPos.subtract(getPos());
        if (offset.lengthSqr() < 1.0E-4) {
            setVelocity(Vec3.ZERO);
            return;
        }
        Vec3 velocity = getVelocity().add(offset.normalize().scale(acceleration));
        if (velocity.length() > MAX_SPEED) {
            velocity = velocity.normalize().scale(MAX_SPEED);
        }
        setVelocity(velocity);
    }
}
