package first.servantry.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static first.servantry.api.ParticleUtils.getRandom;

public class ServantryUtil {
    /**
     * 从攻击者位置向目标位置发射射弹
     *
     * @param projectile 射弹实体
     * @param attacker   攻击者实体
     * @param target     目标实体
     * @param distance   起始位置与攻击者实体的随机距离范围系数
     * @param speed      射弹速度系数
     */
    public static void shootTargetFromTarget(Projectile projectile, LivingEntity attacker, LivingEntity target, double distance, double speed) {
        double size = target.getBoundingBox().getSize() * distance;
        double x = getRandomWithError(target.getX(), size);
        double y = getRandomWithError(target.getY(), size);
        double z = getRandomWithError(target.getZ(), size);
        Vec3 pos = new Vec3(x, y, z);
        projectile.setPos(pos);
        Vec3 toTarget = target.getHitbox().getCenter().subtract(pos).normalize().scale(speed);
        projectile.setOwner(attacker);
        projectile.setDeltaMovement(toTarget);
        addEntity(attacker.level(), projectile);
    }

    /**
     * 从攻击者位置向目标位置发射射弹
     *
     * @param projectile 射弹实体
     * @param attacker   攻击者实体
     * @param target     目标实体
     * @param distance   起始位置与攻击者实体的随机距离范围系数
     * @param speed      射弹速度系数
     */
    public static void shootTargetFromAttaker(Projectile projectile, LivingEntity attacker, LivingEntity target, double distance, double speed) {
        double size = attacker.getBoundingBox().getSize() * distance;
        double x = getRandomWithError(attacker.getX(), size);
        double y = getRandomWithError(attacker.getY(), size);
        double z = getRandomWithError(attacker.getZ(), size);
        Vec3 pos = new Vec3(x, y, z);
        projectile.setPos(pos);
        Vec3 toTarget = target.getHitbox().getCenter().subtract(pos).normalize().scale(speed);
        projectile.setOwner(attacker);
        projectile.setDeltaMovement(toTarget);
        addEntity(attacker.level(), projectile);
    }

    /**
     * 添加实体到维度
     *
     * @param level  维度
     * @param entity 实体
     */
    public static void addEntity(Level level, Entity entity) {
        MinecraftServer server = level.getServer();
        if (server != null && entity.getId() > 0) {
            if (entity instanceof AbstractArrow abstractArrow) {
                abstractArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            server.execute(() -> level.addFreshEntity(entity));
        }
    }

    /**
     * 从基础值中添加随机误差
     *
     * @param baseValue  基础值
     * @param errorRange 误差范围
     * @return 带有误差的随机值
     */
    public static double getRandomWithError(double baseValue, double errorRange) {
        return baseValue + (getRandom().nextFloat(-1, 1) * errorRange);
    }

    /**
     * 播放音效
     *
     * @param level       维度
     * @param center      音效中心位置
     * @param soundEvent  音效事件
     * @param soundSource 音效源
     */
    public static void playSound(Level level, Vec3 center, SoundEvent soundEvent, SoundSource soundSource) {
        level.playSound(null, center.x(), center.y(), center.z(), soundEvent, soundSource, 1.0f, getRandom().nextFloat(0.4f, 0.8f));
    }

}
