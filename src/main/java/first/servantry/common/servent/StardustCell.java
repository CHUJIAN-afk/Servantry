package first.servantry.common.servent;

import first.servantry.api.ai.ServantGoalSelector;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.MomentumServant;
import first.servantry.common.servent.goal.StardustCellAttackGoal;
import first.servantry.common.servent.goal.StardustCellIdleGoal;
import first.servantry.common.servent.goal.StardustCellTeleportGoal;
import first.servantry.register.ServantRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * 星尘细胞仆从 - 追踪敌人并发射星细胞射弹。
 * <p>
 * 旋转逻辑由服务端控制并同步给客户端。
 * 瞬移后发射星细胞射弹而非直接造成伤害。
 * </p>
 */
public class StardustCell extends MomentumServant {

    // ===================== 渲染状态（服务端控制） =====================
    private float renderYaw = 0f, renderPitch = 0f, renderRoll = 0f;
    private float renderYawO = 0f, renderPitchO = 0f, renderRollO = 0f;

    // ===================== 客户端表现状态 =====================
    public int trailTimer = 0;

    // ===================== 攻击冷却 =====================
    private int shootCooldown = 0;

    // ===================== 瞬移相关 =====================
    private int teleportTimer = 0;
    private Vec3 teleportStart, teleportTarget;

    public StardustCell() {
        super();
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new StardustCellTeleportGoal(this));
        goalSelector.addGoal(1, new StardustCellAttackGoal(this));
        goalSelector.addGoal(2, new StardustCellIdleGoal(this));
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeInt(trailTimer);
        buf.writeFloat(renderYaw);
        buf.writeFloat(renderPitch);
        buf.writeFloat(renderRoll);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        trailTimer = buf.readInt();
        renderYawO = renderYaw;
        renderPitchO = renderPitch;
        renderRollO = renderRoll;
        renderYaw = buf.readFloat();
        renderPitch = buf.readFloat();
        renderRoll = buf.readFloat();
    }

    // ===================== 服务端 tick =====================
    @Override
    public void tick() {
        super.tick();
        Player owner = getOwner();

        if (!owner.level().isClientSide()) {
            // 服务端控制旋转
            renderYawO = renderYaw;
            renderPitchO = renderPitch;
            renderRollO = renderRoll;
            renderYaw += 2f;
            renderPitch += 2f;
            renderRoll += 2f;

            if (shootCooldown > 0) {
                shootCooldown--;
            }
            if (trailTimer > 0) {
                trailTimer--;
            }
        }
    }

    // ===================== 光环锚点计算 =====================
    public Vec3 getHaloAnchorPos(Player owner, LivingEntity target, int order) {
        long seed = target.getId() * 31337L + order * 1021L;
        Random rand = new Random(seed);
        double baseTheta = rand.nextDouble() * Math.PI * 2.0;
        double phi = Math.acos(1.0 - rand.nextDouble() * 1.4);
        double radius = 3.5 + rand.nextDouble() * 2.0 + (order * 0.15);
        double rotationSpeed = (rand.nextDouble() * 0.02 + 0.01) * (rand.nextBoolean() ? 1 : -1);
        double currentTheta = baseTheta + owner.tickCount * rotationSpeed;

        double offsetX = radius * Math.sin(phi) * Math.cos(currentTheta);
        double offsetY = radius * Math.cos(phi) + Math.sin(owner.tickCount * 0.05 + rand.nextDouble() * Math.PI) * 0.5;
        double offsetZ = radius * Math.sin(phi) * Math.sin(currentTheta);

        Vec3 targetCenter = target.position().add(0, target.getBbHeight() / 2.0, 0);
        return targetCenter.add(offsetX, offsetY, offsetZ);
    }

    // ===================== 渲染数据访问 =====================
    public float getRenderYaw(float partialTick) {
        return Mth.lerp(partialTick, renderYawO, renderYaw);
    }

    public float getRenderPitch(float partialTick) {
        return Mth.lerp(partialTick, renderPitchO, renderPitch);
    }

    public float getRenderRoll(float partialTick) {
        return Mth.lerp(partialTick, renderRollO, renderRoll);
    }

    // ===================== 属性 =====================
    @Override
    public float getDamage() {
        return 6f;
    }

    @Override
    public float getKnockback() {
        return 0.2f;
    }

    @Override
    public ServantType<? extends first.servantry.api.servant.Servant> getType() {
        return ServantRegister.StardustCell.get();
    }

    // ===================== 访问器 =====================
    public int getShootCooldown() {
        return shootCooldown;
    }

    public void setShootCooldown(int shootCooldown) {
        this.shootCooldown = shootCooldown;
    }

    public int getTeleportTimer() {
        return teleportTimer;
    }

    public void setTeleportTimer(int teleportTimer) {
        this.teleportTimer = teleportTimer;
    }

    public Vec3 getTeleportStart() {
        return teleportStart;
    }

    public void setTeleportStart(Vec3 teleportStart) {
        this.teleportStart = teleportStart;
    }

    public Vec3 getTeleportTarget() {
        return teleportTarget;
    }

    public void setTeleportTarget(Vec3 teleportTarget) {
        this.teleportTarget = teleportTarget;
    }

    public void setTrailTimer(int trailTimer) {
        this.trailTimer = trailTimer;
    }
}