package first.servantry.common.projectile;

import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.entity.PathNode;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.ServantryAttachmentEntityRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.BiConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class CustomLaser extends Projectile implements ICollideAttack<CustomLaser> {

    private int color = 0;
    private float alpha = 1;
    private AABB hitbox = new AABB(0, 0, 0, 0, 0, 0);
    private Consumer<CustomLaser> tickConsumer = null;
    private BiConsumer<CustomLaser, List<HitContext>> hitConsumer = null;

    public CustomLaser() {
        super();
    }

    public CustomLaser(DamageSource damageSource, PathNode pathNode, int color) {
        super(pathNode.pos(), Vec3.directionFromRotation(pathNode.pitch(), pathNode.yaw()));
        this.color = color;
        setDamageSource(damageSource);
        setDrag(1);
        setMaxSpeed(0);
        setMaxLife(-1);
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getAlpha() {
        return alpha;
    }

    @Override
    protected void tickPhysics() {

    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            if (tickConsumer != null) {
                tickConsumer.accept(this);
            }
        }
        super.tick();
    }

    public void setTickConsumer(Consumer<CustomLaser> tickConsumer) {
        this.tickConsumer = tickConsumer;
    }

    public void setHitConsumer(BiConsumer<CustomLaser, List<HitContext>> hitConsumer) {
        this.hitConsumer = hitConsumer;
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeInt(color);
        buf.writeFloat(alpha);
        buf.writeVec3(hitbox.getMinPosition());
        buf.writeVec3(hitbox.getMaxPosition());
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        color = buf.readInt();
        alpha = buf.readFloat();
        hitbox = new AABB(buf.readVec3(), buf.readVec3());
    }

    public void setHitbox(Vec3 start, Vec3 end, float radius) {
        this.hitbox = new AABB(-radius, -radius, 0, radius, radius, start.distanceTo(end));
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.CustomLaserProjectile.get();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return hitbox;
    }

    @Override
    public boolean isValidCollisionTarget(CustomLaser entity, LivingEntity target) {
        if (damageSource instanceof ServantDamageSource servantDamageSource) {
            Servant servant = servantDamageSource.getServant();
            return servant.isTarget(target);
        }
        return ICollideAttack.super.isValidCollisionTarget(entity, target);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        if (hitConsumer != null) {
            hitConsumer.accept(this, hitContexts);
        }
    }
}
