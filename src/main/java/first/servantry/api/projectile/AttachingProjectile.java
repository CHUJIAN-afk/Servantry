package first.servantry.api.projectile;

import first.servantry.api.PathNode;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;

/**
 * 黏着射弹抽象基类，可附着在指定位置。
 * <p>
 * 通过设置黏着位置（绝对坐标），射弹会固定在该位置。
 * </p>
 */
public abstract class AttachingProjectile extends Projectile {

    /**
     * 黏着位置（绝对坐标），null 表示未黏着
     */
    protected Vec3 attachedPosition;

    // ===================== 构造方法 =====================

    public AttachingProjectile() {
        super();
    }

    public AttachingProjectile(Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
    }

    // ===================== 状态管理 =====================

    /**
     * 是否处于黏着状态
     */
    public boolean isAttached() {
        return attachedPosition != null;
    }

    /**
     * 黏着到指定位置
     */
    public void attachTo(Vec3 position) {
        this.attachedPosition = position;
        this.velocity = Vec3.ZERO;
        this.trailTimer = 0;
    }

    /**
     * 解除黏着
     */
    public void detach() {
        this.attachedPosition = null;
    }

    // ===================== 物理更新 =====================

    @Override
    protected void tickPhysics() {
        if (isAttached()) {
            // 保持黏着位置
            setPath(Collections.singletonList(new PathNode(attachedPosition, getYaw(), getPitch(), getRoll() + getSpinSpeed())));
        } else {
            super.tickPhysics();
        }
    }

}
