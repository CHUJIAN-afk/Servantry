package first.summoner.common.entity.astrageldon;

import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.PlannedPath;
import first.lyra.common.attachmentEntity.RenderKeyframe;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.entity.IPathEntity;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.common.entity.Astrageldon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AstrageldonProjectile extends Projectile implements IPathEntity {

    protected int age;
    protected int life;
    protected float damage = AstrageldonCombat.PROJECTILE_DAMAGE;

    protected AstrageldonProjectile(EntityType<? extends AstrageldonProjectile> type, Level level, int life) {
        super(type, level);
        this.life = life;
        setNoGravity(true);
        noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
    }

    @Override
    public void registerSyncFields(SyncFieldDispatcher fields) {
        IPathEntity.super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.INT, () -> age, value -> age = value);
        fields.field(LyraStreamCodecs.INT, () -> life, value -> life = value);
        fields.field(LyraStreamCodecs.FLOAT, () -> damage, value -> damage = value);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            checkArena();
            PathNode from = node();
            List<RenderKeyframe> frames = new ArrayList<>();
            int steps = substeps();
            for (int i = 0; i < steps && !isRemoved(); i++) {
                var previous = getBoundingBox();
                step();
                checkArena();
                if (!isRemoved() && canDamage()) {
                    AstrageldonCombat.hurtPlayers(this, getOwner(), damage, previous, this::onHitPlayer);
                }
                age++;
                life--;
                if (life <= 0) {
                    discard();
                }
                if (i + 1 < steps) {
                    frames.add(new RenderKeyframe((float) (i + 1) / steps, node()));
                }
            }
            if (!isRemoved()) {
                PathNode to = node();
                setCurrentPathNode(from);
                setPlannedPath(new PlannedPath("astrageldon_projectile", List.of(to)).render(0, frames));
            }
        }
        if (!isRemoved()) {
            tickPath();
        }
    }

    private PathNode node() {
        return new PathNode(position(), getYRot(), getXRot(), getRoll());
    }

    private void checkArena() {
        if (getOwner() instanceof Astrageldon boss && boss.isBeyondArena(getBoundingBox().getCenter())) {
            discard();
        }
    }

    protected void setCenterPose(Vec3 center, Vec3 direction) {
        setPos(center.add(0, -getBbHeight() / 2, 0));
        setYRot(PathNode.yawFromDirection(direction));
        setXRot(PathNode.pitchFromDirection(direction));
    }

    protected int substeps() {
        return 3;
    }

    protected boolean canDamage() {
        return true;
    }

    protected void onHitPlayer(Player player) {
    }

    protected abstract void step();

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Age", age);
        tag.putInt("Life", life);
        tag.putFloat("Damage", damage);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        age = tag.getInt("Age");
        life = tag.getInt("Life");
        damage = tag.getFloat("Damage");
    }
}
