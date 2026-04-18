package first.servantry.common.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.projectile.AdvancedProjectile;
import first.servantry.api.projectile.IProjectileCollider;
import first.servantry.api.projectile.IProjectileConeTrail;
import first.servantry.api.projectile.IProjectileMomentum;
import first.servantry.api.register.ProjectileType;
import first.servantry.api.servant.PathNode;
import first.servantry.common.servent.StardustCell;
import first.servantry.register.AdvancedProjectileRegister;
import first.servantry.register.ItemRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class StardustLaser extends AdvancedProjectile implements IProjectileMomentum, IProjectileCollider, IProjectileConeTrail {

    private Vec3 velocity = Vec3.ZERO;
    private StardustCell stardustCell = null;
    private LivingEntity target;
    private int targetId = -1;

    // 【新增】消散相关的状态与计时器
    private boolean isFadingOut = false;
    private int fadeTimer = 0;
    private static final int MAX_FADE_TIME = 15; // 消散动画持续时间（Tick）

    public StardustLaser(Level level, Player owner, PathNode startNode) {
        super(level, owner, startNode);
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
        this.targetId = target != null ? target.getId() : -1;
    }

    public StardustCell getStardustCell() {
        return stardustCell;
    }

    public void setStardustCell(StardustCell stardustCell) {
        this.stardustCell = stardustCell;
    }

    // 【新增】获取当前消散进度（0.0 到 1.0）
    public float getDeathProgress() {
        if (!isFadingOut) return 0.0f;
        return (float) fadeTimer / MAX_FADE_TIME;
    }

    // 【核心拦截】阻断原生销毁，进入渐隐状态
    @Override
    public void discard() {
        if (!isFadingOut && !isRemoved()) {
            this.isFadingOut = true;
            this.setVelocity(this.getVelocity().scale(0.5));
            // 延长最大寿命，防止在消散期间被 super.tick() 强行移除
            this.setMaxAge(this.getTickCount() + MAX_FADE_TIME + 1);
        } else if (isFadingOut && fadeTimer >= MAX_FADE_TIME) {
            super.discard(); // 真正移除实体
        }
    }

    @Override
    public void tick() {
        // 【新增】如果是消散状态，只处理动画逻辑
        if (isFadingOut) {
            fadeTimer++;
            if (fadeTimer >= MAX_FADE_TIME) {
                super.discard();
                return;
            }
            // 必须调用 super.tick，让历史节点 (historyNodes) 继续更新，使轨迹平滑收缩
            super.tick();
            return;
        }

        super.tick();
        if (!getLevel().isClientSide()) {
            if (stardustCell != null && stardustCell.currentTarget == target) {
                Vec3 targetPos = target.getBoundingBox().getCenter();
                Vec3 dir = targetPos.subtract(this.getPos());
                if (dir.lengthSqr() > 1e-4) {
                    this.applyForce(dir.normalize());
                }
            } else {
                discard(); // 失去目标时触发渐隐
            }
        }
    }

    @Override
    public Vec3 getVelocity() { return velocity; }

    @Override
    public void setVelocity(Vec3 vel) { this.velocity = vel; }

    @Override
    public float getMaxSpeed() { return 20f; }

    @Override
    public float getFriction() { return 0; }

    @Override
    public ProjectileType<? extends AdvancedProjectile> getType() {
        return AdvancedProjectileRegister.StardustLaser.get();
    }

    @Override
    public boolean onHitBlock(AdvancedProjectile proj, BlockHitResult hit) {
        if (isFadingOut) return false;
        return false;
    }

    @Override
    public boolean onHitEntity(AdvancedProjectile proj, EntityHitResult hit) {
        if (isFadingOut) return false; // 消散中不产生多次判定
        Entity entity = hit.getEntity();
        if (entity instanceof LivingEntity living) {
            Player owner = proj.getOwner();
            if (owner != null && stardustCell != null) {
                int invulnerableTime = target.invulnerableTime;
                target.invulnerableTime = 0;
                living.hurt(stardustCell.getDamageSource(), stardustCell.getBaseDamage());
                target.invulnerableTime = invulnerableTime;
            }
        }
        return true;
    }

    @Override
    public AABB getHitbox() {
        // 消散状态去除碰撞体积
        if (isFadingOut) return new AABB(0, 0, 0, 0, 0, 0);
        return new AABB(-0.05, -0.05, -0.05, 0.05, 0.05, 0.05);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode) {
        Level level = getLevel();
        poseStack.pushPose();

        // 【修改】核心模型也会随着消散进度逐渐缩小至消失
        float scale = 0.1f * Math.max(0.0f, 1.0f - getDeathProgress());
        poseStack.scale(scale, scale, scale);

        if (level != null && scale > 0.01f) {
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    ItemRegister.StardustCell.get().getDefaultInstance(),
                    ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, level, 0
            );
        }
        poseStack.popPose();
    }

    @Override
    public int getTrailTimer() { return 15; }

    @Override
    public float getTrailMaxRadius() { return 0.1f; }

    @Override
    public int getTrailColorRGB(float progress) { return 0x0066FF; }

    @Override
    public int getTrailResolution() { return 4; }

    @Override
    protected void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.targetId);
        buf.writeBoolean(this.isFadingOut);
        buf.writeInt(this.fadeTimer);
    }

    @Override
    protected void readAdditional(RegistryFriendlyByteBuf buf) {
        this.targetId = buf.readInt();
        this.isFadingOut = buf.readBoolean();
        this.fadeTimer = buf.readInt();
    }
}