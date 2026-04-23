# Servantry 模组 - 开发文档

## 项目简介

Servantry 是一个 Minecraft NeoForge 模组，实现仆从(Servant)和射弹(Projectile)战斗系统。玩家可召唤自动战斗的仆从，仆从可发射射弹攻击敌人。

---

## 核心架构

### 附件实体模式

采用 **玩家附件(Attachment)存储** 而非传统实体：
- 仆从/射弹数据存储在玩家 Attachment 中
- 服务端执行逻辑，客户端接收同步数据渲染
- 适合大量小型战斗单位，避免实体注册开销

### 类继承体系

```
AttachmentEntity (附件实体基类)
├── Servant (仆从基类 - AI驱动)
│   └── MomentumServant (动量仆从 - 物理系统)
└── Projectile (射弹基类 - 动量物理)
```

### 两大核心系统

| 系统 | 基类 | 特性 |
|------|------|------|
| 仆从 | `MomentumServant` | AI目标选择、自动战斗、碰撞攻击 |
| 射弹 | `Projectile` | 动量物理、状态管理、可黏贴目标 |

---

## 包结构

```
api/
  entity/      - AttachmentEntity, ICollideAttack
  servant/      - Servant, MomentumServant, PathNode, PlannedPath
  projectile/  - Projectile, ProjectileState
  ai/          - ServantGoal, ServantGoalSelector
  client/      - 渲染器接口、RenderContext
  register/    - ServantType, ProjectileType

common/
  servant/     - StardustCell, Terraprism 等实现
  projectile/  - StardustProjectile 等实现
  attachment/  - ServantData, ProjectileData 数据附件
  weapon/      - IServantWeapon 武器接口

client/renderer/ - 渲染器实现
register/         - 注册类
```

---

## 关键文件速查

| 文件 | 路径 | 作用 |
|------|------|------|
| AttachmentEntity | `api/entity/AttachmentEntity.java` | 附件实体基类 |
| ICollideAttack | `api/entity/ICollideAttack.java` | 碰撞攻击接口 |
| Servant | `api/servant/Servant.java` | 仆从抽象基类 |
| MomentumServant | `api/servant/MomentumServant.java` | 动量仆从基类 |
| Projectile | `api/projectile/Projectile.java` | 射弹抽象基类 |
| ServantGoal | `api/ai/ServantGoal.java` | AI目标抽象类 |
| ServantGoalSelector | `api/ai/ServantGoalSelector.java` | AI目标选择器 |
| PathNode | `api/PathNode.java` | 路径节点(位置+旋转) |
| RenderContext | `api/client/RenderContext.java` | 渲染上下文配置 |
| IServantWeapon | `api/weapon/IServantWeapon.java` | 仆从武器接口 |

---

# 创建自定义仆从

## 完整示例

### 步骤 1: 创建仆从类

继承 `MomentumServant` 并实现必要方法：

```java
public class MyServant extends MomentumServant {

    /** 拖尾计时器，>0 时渲染拖尾 */
    public int trailTimer = 0;

    public MyServant() {
        super();
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        // 优先级数字越小，优先级越高
        goalSelector.addGoal(0, new MyTeleportGoal(this));  // 最高优先级
        goalSelector.addGoal(1, new MyAttackGoal(this));
        goalSelector.addGoal(2, new MyIdleGoal(this));       // 最低优先级
    }

    /**
     * 每tick行为逻辑（仅服务端执行）。
     * 子类在此实现核心行为，物理更新由基类自动处理。
     */
    @Override
    public void tickBehavior() {
        LivingEntity target = getTarget();
        if (target != null && target.isAlive()) {
            // 使用视角调度器：朝向目标
            lookAt(target.getEyePosition());

            // 使用动量调度器：向目标移动
            moveToward(target.position(), 0.1);

            // 触发拖尾渲染
            trailTimer = getTrailDuration();
        }
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeInt(trailTimer);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        trailTimer = buf.readInt();
    }

    @Override
    public float getDamage() { return 8f; }

    @Override
    public float getKnockback() { return 0.3f; }

    @Override
    public ServantType<? extends MomentumServant> getServantType() {
        return ServantRegister.MyServant.get();
    }
}
```

### 步骤 2: 创建 AI Goal

```java
public class MyAttackGoal extends ServantGoal<MyServant> {

    public MyAttackGoal(MyServant servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        // 有有效目标时激活
        return servant.getTarget() != null && servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        if (target == null || !target.isAlive()) return;

        // 目标在攻击范围内
        if (servant.getPos().distanceTo(target.position()) < 2.0) {
            // 造成伤害
            target.hurt(servant.getDamageSource(), servant.getDamage());
        }
    }
}
```

### 步骤 3: 注册类型

在 `ServantRegister.java`:

```java
public static final DeferredHolder<ServantType<?>, ServantType<MyServant>> MyServant =
    SERVANT_TYPES.register("my_servant", () -> new ServantType<>(MyServant::new));
```

### 步骤 4: 创建渲染器

```java
public class MyServantRenderer implements IServantRenderer<MyServant> {

    @Override
    public void renderEntity(MyServant servant, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight,
                             PathNode renderNode) {
        // 应用旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(renderNode.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(renderNode.pitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(renderNode.roll()));

        // 渲染模型（示例：立方体）
        VertexConsumer consumer = buffer.getBuffer(RenderType.entitySolid(TEXTURE));
        // ... 绘制顶点
    }

    @Override
    public RenderContext getRenderContext(MyServant servant) {
        // 返回拖尾配置
        return RenderContext.cone()
            .color(progress -> 0x00FF00)           // 绿色
            .fadeOut(progress -> (1f - progress) * (1f - progress))
            .maxRadius(0.3f)
            .historyLength(8)
            .build();
    }

    @Override
    public int getTrailTimer(MyServant servant) {
        return servant.trailTimer;
    }
}
```

### 步骤 5: 注册渲染器

客户端初始化时：

```java
ServantRenderDispatcher.register(ServantRegister.MyServant.get(), new MyServantRenderer());
```

---

# 创建自定义射弹

## 完整示例

### 步骤 1: 创建射弹类

```java
public class MyProjectile extends Projectile {

    public MyProjectile() {
        super();
    }

    public MyProjectile(UUID ownerUuid, Vec3 startPos, LivingEntity target) {
        super(startPos, target);
        setOwnerUuid(ownerUuid);
    }

    @Override
    public void tickBehavior(Player owner) {
        switch (getState()) {
            case FLYING -> tickFlying(owner);
            case ATTACHED -> updateAttachedPosition();
            case DEAD -> markForRemoval();
        }
    }

    private void tickFlying(Player owner) {
        LivingEntity target = getCachedTarget();

        // 目标无效时移除
        if (target == null || !target.isAlive()) {
            markForRemoval();
            return;
        }

        // 追踪目标
        Vec3 dir = target.position().subtract(getPos()).normalize();
        applyForce(dir.scale(0.15));

        // 命中检测
        if (getPos().distanceTo(target.position()) < getHitRadius()) {
            onHit(owner, target);
        }

        // 触发拖尾
        setTrailTimer(getTrailDuration());
    }

    private void onHit(Player owner, LivingEntity target) {
        // 造成伤害
        target.hurt(owner.damageSources().playerAttack(owner), getDamage());

        // 可选：黏贴到目标身上
        attachTo(target);

        // 或直接移除
        // markForRemoval();
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) { }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) { }

    @Override
    public float getDamage() { return 5f; }

    @Override
    public ProjectileType<? extends Projectile> getProjectileType() {
        return ProjectileRegister.MyProjectile.get();
    }
}
```

### 步骤 2: 注册类型

在 `ProjectileRegister.java`:

```java
public static final DeferredHolder<ProjectileType<?>, ProjectileType<MyProjectile>> MyProjectile =
    PROJECTILE_TYPES.register("my_projectile", () -> new ProjectileType<>(MyProjectile::new));
```

### 步骤 3: 创建渲染器

```java
public class MyProjectileRenderer implements IProjectileRenderer<MyProjectile> {

    @Override
    public void renderEntity(MyProjectile projectile, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight,
                             PathNode renderNode) {
        // 渲染射弹本体
        poseStack.mulPose(Axis.YP.rotationDegrees(renderNode.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(renderNode.pitch()));
        // ... 绘制模型
    }

    @Override
    public RenderContext getRenderContext(MyProjectile projectile) {
        return RenderContext.cone()
            .color(progress -> 0xFF0000)
            .maxRadius(0.15f)
            .historyLength(6)
            .build();
    }

    @Override
    public int getTrailTimer(MyProjectile projectile) {
        return projectile.getTrailTimer();
    }
}
```

### 步骤 4: 注册渲染器

```java
ProjectileRenderDispatcher.register(ProjectileRegister.MyProjectile.get(), new MyProjectileRenderer());
```

---

# 碰撞攻击系统

## 概述

`ICollideAttack` 接口为附件实体提供沿运动轨迹的碰撞攻击能力。仆从移动时会自动检测扫掠路径上的目标。

## 实现碰撞攻击

```java
public class MyServant extends MomentumServant implements ICollideAttack {

    @Override
    public AABB getHitbox() {
        // 返回局部坐标系下的碰撞箱
        return new AABB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
    }

    @Override
    public void onCollisionAttack(Set<LivingEntity> targets) {
        // 对碰撞到的所有目标造成伤害
        for (LivingEntity target : targets) {
            target.hurt(getDamageSource(), getDamage());
        }
    }

    @Override
    public boolean isValidCollisionTarget(LivingEntity entity) {
        // 过滤有效目标（默认实现检查敌对生物）
        return isTarget(entity);
    }
}
```

## 碰撞检测原理

1. 使用 OBB（有向包围盒）表示实体碰撞箱
2. 沿运动轨迹进行扫掠检测
3. 自动去重，避免同一目标被多次判定

---

# 渲染系统

## RenderContext 渲染上下文

`RenderContext` 用于配置拖尾渲染参数，支持链式调用：

```java
RenderContext context = RenderContext.cone()          // 创建锥形拖尾配置
    .color(progress -> {                               // 颜色（按进度变化）
        int r = (int) (255 * (1 - progress));
        int g = (int) (255 * progress);
        return (r << 16) | (g << 8);
    })
    .fadeOut(progress -> (1f - progress) * (1f - progress))  // 淡出曲线
    .alphaBoost(progress -> 0.5f + progress * 0.5f)   // 透明度增强
    .brightnessBoost(progress -> 100 + (int)(progress * 50))  // 亮度增强
    .maxRadius(0.3f)                                    // 最大半径
    .historyLength(8)                                   // 历史节点数
    .segmentsPerNode(4)                                 // 每节点插值段数
    .resolution(8)                                      // 多边形边数
    .build();
```

### 拖尾类型

| 类型 | 方法 | 说明 |
|------|------|------|
| 无拖尾 | `RenderContext.none()` | 不渲染拖尾 |
| 锥形拖尾 | `RenderContext.cone()` | 圆锥形拖尾，适合射弹 |
| 带状拖尾 | `RenderContext.ribbon()` | 扁平带状拖尾，适合刀剑 |

### 渲染器接口

```java
public interface IServantRenderer<T extends Servant> {
    // 渲染实体本体（必须实现）
    void renderEntity(T servant, PoseStack poseStack, MultiBufferSource buffer,
                      int packedLight, PathNode renderNode);

    // 获取渲染上下文（可选，默认无拖尾）
    default RenderContext getRenderContext(T servant) {
        return RenderContext.none();
    }

    // 获取拖尾计时器（可选）
    default int getTrailTimer(T servant) {
        return 0;
    }
}
```

---

# 仆从武器系统

## IServantWeapon 接口

用于创建可召唤仆从的武器物品：

```java
public class MyWeaponItem extends Item implements IServantWeapon<MyServant> {

    // 获取武器对应的仆从类型
    @Override
    public ServantType<MyServant> getType() {
        return ServantRegister.MyServant.get();
    }

    // 创建临时仆从实例（用于预览等）
    @Override
    public MyServant getDummyServant() {
        return new MyServant();
    }

    // 召唤仆从时的回调
    @Override
    public void summon(Player player, MyServant servant) {
        // 设置初始位置（在玩家前方）
        Vec3 lookDir = player.getLookAngle();
        Vec3 spawnPos = player.position().add(lookDir.scale(2));
        servant.teleportTo(spawnPos);
    }

    // 移除仆从时的回调
    @Override
    public void remove(Player player, MyServant servant) {
        // 清理资源
    }
}
```

## 使用 Builder 创建武器

```java
public static final Item MY_WEAPON = new Item(new Item.Properties())
    .createWeaponBuilder(ServantRegister.MyServant.get())
    .onSummon((player, servant) -> {
        servant.teleportTo(player.position().add(0, 1, 0));
    })
    .onRemove((player, servant) -> {
        // 播放消失音效等
    })
    .build();
```

---

# API 参考

## AttachmentEntity 基类方法

```java
// 位置与朝向
Vec3 getPos();
float getYaw(), getPitch(), getRoll();
PathNode getCurrentPathNode();

// 路径管理
void setPath(List<PathNode> nodes);
void setPlannedPath(PlannedPath path);
boolean isExecutingPath();

// 历史轨迹（拖尾渲染）
LinkedList<PathNode> getHistoryNodes();
PathNode getRenderNode(float partialTick);
int getHistoryNodesSize();

// 网络同步
void writeBase(RegistryFriendlyByteBuf buf);
void readBase(RegistryFriendlyByteBuf buf);
void writeAdditional(RegistryFriendlyByteBuf buf);  // 子类实现
void readAdditional(RegistryFriendlyByteBuf buf);   // 子类实现

// 基础属性
UUID getUuid();
Player getOwner();
float getDamage();  // 子类实现
```

## MomentumServant 调度器方法

### 视角调度器

```java
// 朝向指定位置（平滑过渡）
void lookAt(Vec3 targetPos);

// 朝向指定方向
void lookToward(Vec3 direction);

// 设置期望朝向角度
void setDesiredRotation(float yaw, float pitch, float roll);

// 立即设置朝向（无过渡）
void setRotationImmediate(float yaw, float pitch, float roll);

// 获取期望角度
float getDesiredYaw(), getDesiredPitch(), getDesiredRoll();

// 设置转向速度（度/tick）
void setRotationSpeed(float speed);
```

### 动量调度器

```java
// 向目标位置移动
void moveToward(Vec3 targetPos, double strength);

// 施加力（持续影响）
void applyForce(Vec3 force);

// 施加冲量（瞬时影响）
void applyImpulse(Vec3 impulse);

// 设置速度
void setVelocity(Vec3 velocity);
Vec3 getVelocity();

// 瞬移到目标位置
void teleportTo(Vec3 targetPos);

// 停止移动
void stopMoving();

// 设置阻力系数 [0, 1]
void setDrag(float drag);
float getDrag();
```

## Servant 目标系统

```java
// 目标访问
LivingEntity getTarget();
void setTarget(LivingEntity target);
boolean isTargetChange();

// 目标搜索
LivingEntity searchTarget();
boolean isTarget(LivingEntity entity);

// 可重写配置
int getTargetDistance();           // 搜索距离（默认64）
boolean requireLineOfSight();      // 是否要求可见（默认true）

// 伤害来源
ServantDamageSource getDamageSource();
```

## Projectile 状态管理

```java
// 状态
ProjectileState getState();        // FLYING, ATTACHED, DEAD
void markForRemoval();
boolean isMarkedForRemoval();

// 黏贴
void attachTo(LivingEntity target);
void attachTo(LivingEntity target, Vec3 customOffset);
void updateAttachedPosition();

// 目标
UUID getTargetUuid();
void setTargetUuid(UUID uuid);
LivingEntity getCachedTarget();

// 可重写配置
float getHitRadius();              // 命中半径（默认0.5）
double getMaxDistance();           // 最大距离（默认128）
int getTrailDuration();            // 拖尾时长（默认15）
float getSpinSpeed();              // 自转速度（默认5）
```

---

# 调试技巧

## 查看碰撞箱

开启 F3+B 调试渲染，或在代码中绘制：

```java
if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
    LevelRenderer.renderLineBox(poseStack, consumer, hitbox, 1.0F, 0.0F, 0.0F, 1.0F);
}
```

## 检查数据同步

在序列化方法中添加日志：

```java
@Override
public void writeAdditional(RegistryFriendlyByteBuf buf) {
    buf.writeInt(trailTimer);
    // System.out.println("Sync trailTimer: " + trailTimer);
}
```

## 目标搜索调试

```java
LivingEntity target = servant.searchTarget();
if (target != null) {
    System.out.println("Found target: " + target.getName().getString());
}
```

---

# 示例参考

| 实现 | 文件 | 特性 |
|------|------|------|
| StardustCell | `common/servant/StardustCell.java` | 瞬移攻击、发射射弹、玩家攻击联动 |
| StardustProjectile | `common/projectile/StardustProjectile.java` | 追踪、黏贴、寄生效果 |
| Terraprism | `common/servant/Terraprism.java` | 另一种仆从实现 |

---

# 注意事项

1. **服务端/客户端分离**：行为逻辑只在服务端执行，客户端只渲染
2. **并发安全**：使用 `markForRemoval()` 而非直接移除，避免并发修改异常
3. **网络同步**：所有自定义数据必须在 `writeAdditional` / `readAdditional` 中同步
4. **优先级**：Goal 优先级数字越小，优先级越高
5. **插值渲染**：使用 `getRenderNode(partialTick)` 获取平滑渲染位置
6. **物理更新**：`MomentumServant` 的物理更新在基类自动执行，子类只需调用调度器方法
