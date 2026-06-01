package first.servantry.common.item;

import first.servantry.api.servant.TargetCache;
import first.servantry.common.projectile.ZenithProjectile;
import first.servantry.register.AttachmentRegister;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Zenith extends SwordItem {

    public Zenith(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (usedHand == InteractionHand.MAIN_HAND) {
            if (!level.isClientSide()) {
                DamageSource damageSource = player.damageSources().playerAttack(player);
                Vec3 lookAngle = player.getLookAngle();
                Vec3 startPos = player.getBoundingBox().getCenter().add(lookAngle.scale(-1));
                RandomSource random = player.getRandom();
                for (int i = 0; i < 1; i++) {
                    LivingEntity chase = null;
                    List<LivingEntity> livingEntityList = collectTargets(player);
                    if (random.nextBoolean()) {
                        chase = livingEntityList.stream().findAny().orElse(null);
                    }
                    Vec3 endPos;
                    if (chase != null) {
                        endPos = chase.getBoundingBox().getCenter();
                    } else {
                        endPos = computeEndPos(player);
                    }
                    ZenithProjectile projectile = new ZenithProjectile(damageSource, startPos, endPos);
                    projectile.setOwner(player);
                    if (chase != null) {
                        projectile.setChaseEnd(chase);
                    }
                    projectile.lerp = random.nextFloat() * 0.05f + 0.05f;
                    projectile.setCurrentPathNode(projectile.getEllipseSlashNode(projectile.lerp));
                    projectile.join(player);
                }
            }
            return InteractionResultHolder.success(player.getItemInHand(usedHand));
        }
        return super.use(level, player, usedHand);
    }

    /**
     * 收集玩家视线前方8格内距离视线向量不足8格的敌人，以及玩家周围4格的敌人。
     */
    private List<LivingEntity> collectTargets(Player player) {
        List<LivingEntity> result = new ArrayList<>();
        TargetCache cache = player.getData(AttachmentRegister.TargetCache);

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDir = player.getLookAngle().normalize();
        Vec3 lookEnd = eyePos.add(lookDir.scale(8));

        for (LivingEntity entity : cache.getEntities()) {
            if (entity == player || !entity.isAlive()) continue;

            Vec3 entityPos = entity.getBoundingBox().getCenter();
            double distToPlayer = entityPos.distanceTo(eyePos);

            // 玩家周围4格
            if (distToPlayer <= 4) {
                result.add(entity);
                continue;
            }

            // 视线前方：到视线向量的距离不足8格，且在视线前方
            double distToLine = distanceToLineSegment(eyePos, lookEnd, entityPos);
            if (distToLine <= 8 && entityPos.subtract(eyePos).dot(lookDir) > 0) {
                result.add(entity);
            }
        }
        return result;
    }

    /**
     * 点到线段的最短距离
     */
    private double distanceToLineSegment(Vec3 lineStart, Vec3 lineEnd, Vec3 point) {
        Vec3 line = lineEnd.subtract(lineStart);
        double lineLenSq = line.lengthSqr();
        if (lineLenSq < 1e-5) return point.distanceTo(lineStart);

        double t = Math.max(0, Math.min(1, point.subtract(lineStart).dot(line) / lineLenSq));
        Vec3 projection = lineStart.add(line.scale(t));
        return point.distanceTo(projection);
    }

    /**
     * 计算终点位置：视线命中实体 > 视线命中方块 > 视线方向20格
     */
    private Vec3 computeEndPos(Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDir = player.getLookAngle();
        double reach = 10;

        // 优先检测视线命中的实体
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, eyePos, eyePos.add(lookDir.scale(reach)), player.getBoundingBox().inflate(reach), e -> e instanceof LivingEntity && e.isAlive() && e != player, reach * reach);
        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity living) {
            return living.getBoundingBox().getCenter();
        }

        // 其次检测视线命中的方块
        BlockHitResult blockHit = player.level().clip(new ClipContext(eyePos, eyePos.add(lookDir.scale(reach)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            return blockHit.getLocation();
        }

        // 最后回退到视线方向20格
        return eyePos.add(lookDir.scale(reach));
    }
}
