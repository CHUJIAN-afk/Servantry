package first.servantry.common.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.projectile.AdvancedProjectile;
import first.servantry.api.projectile.IProjectileCollider;
import first.servantry.api.projectile.IProjectileConeTrail;
import first.servantry.api.projectile.IProjectileMomentum;
import first.servantry.api.register.ProjectileType;
import first.servantry.api.servant.PathNode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class StardustLaser extends AdvancedProjectile implements IProjectileMomentum, IProjectileCollider, IProjectileConeTrail {

    private Vec3 velocity = Vec3.ZERO; // 接口要求提供变量存储

    public StardustLaser(Level level, LivingEntity owner, PathNode startNode) {
        super(level, owner, startNode);
    }

    @Override public Vec3 getVelocity() { return velocity; }
    @Override public void setVelocity(Vec3 vel) { this.velocity = vel; }

    @Override
    public ProjectileType<? extends AdvancedProjectile> getType() {
        return null;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode renderNode) {

    }

    @Override
    public void tick() {
        super.tick(); // 基类消化队列
        
        if (getFutureNodes().isEmpty()) {
            // 没有固定剧本时，动量接管飞行
            processMomentum(this, 0.99f, 3.0f);
        }
        
        // 触发碰撞射线检测
        processCollision(this); 
    }

    @Override
    public void renderInternal(float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
        super.renderInternal(partialTick, poseStack, bufferSource); // 基类画模型
        // 触发圆锥拖尾渲染
        processTrailRender(poseStack, bufferSource, partialTick, this, getHistoryNodes().getFirst());
    }

    // 实现碰撞接口的反馈
    @Override public boolean onHitBlock(AdvancedProjectile proj, BlockHitResult hit) { return true; /* 销毁 */ }
    @Override public boolean onHitEntity(AdvancedProjectile proj, EntityHitResult hit) { return true; /* 销毁 */ }
}