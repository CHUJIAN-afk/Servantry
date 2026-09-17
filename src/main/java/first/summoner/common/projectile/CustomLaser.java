package first.summoner.common.projectile;


import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.SummonerStreamCodecs;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.BiConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class CustomLaser extends Projectile implements IEntityCollision<CustomLaser> {

    private int color = 0;
    private float alpha = 1;
    private AABB hitbox = new AABB(0, 0, 0, 0, 0, 0);
    private Consumer<CustomLaser> tickConsumer = null;
    private BiConsumer<CustomLaser, List<HitContext>> hitConsumer = null;

    public CustomLaser() {
        super(SummonerAttachmentEntityRegister.CUSTOM_LASER);
        setPhysics(false);
        setMaxTickCount(-1);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ByteBufCodecs.INT, this::getColor, this::setColor);
        fields.field(ByteBufCodecs.FLOAT, this::getAlpha, this::setAlpha);
        fields.field(SummonerStreamCodecs.AABB, this::getHitbox, this::setHitbox);
    }

    public int getLife() {
        return getTickCount();
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getAlpha() {
        return alpha;
    }

    @Override
    public void tick() {
        super.tick();

        if (tickConsumer != null) {
            tickConsumer.accept(this);
        }
    }

    public void setTickConsumer(Consumer<CustomLaser> tickConsumer) {
        this.tickConsumer = tickConsumer;
    }

    public void setHitConsumer(BiConsumer<CustomLaser, List<HitContext>> hitConsumer) {
        this.hitConsumer = hitConsumer;
    }

    public void setHitbox(AABB hitbox) {
        this.hitbox = hitbox;
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
    public @NotNull AABB getHitbox() {
        return hitbox;
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        if (hitConsumer != null) {
            hitConsumer.accept(this, hitContexts);
        }
    }
}
