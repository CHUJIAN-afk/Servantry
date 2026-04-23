# Servantry 模组 - Claude Code 使用指南

## 项目简介

Servantry 是一个 Minecraft NeoForge 模组，实现仆从(Servant)和射弹(Projectile)战斗系统。玩家可召唤自动战斗的仆从，仆从可发射射弹攻击敌人。

## 核心概念

### 数据存储模式

采用 **玩家附件(Attachment)存储** 而非传统实体：
- 仆从/射弹数据存储在玩家 Attachment 中
- 服务端 tick 逻辑，客户端数据同步渲染
- 适合大量小型战斗单位

### 两大核心系统

| 系统 | 基类 | 说明 |
|------|------|------|
| 仆从 | `Servant` → `MomentumServant` | 自动战斗的单位，有AI目标系统 |
| 射弹 | `Projectile` | 飞行攻击物，可追踪或黏贴目标 |

---

## 快速参考

### 包结构

```
api/
  servant/     - Servant, MomentumServant, PathNode, PlannedPath
  projectile/  - Projectile, ProjectileState
  ai/          - ServantGoal, ServantGoalSelector
  client/      - 渲染器接口 (IServantRenderer, IProjectileRenderer等)
  register/    - ServantType, ProjectileType, Registries

common/
  servent/     - StardustCell, Terraprism 等仆从实现
  projectile/  - StardustProjectile 等射弹实现
  attachment/  - ServantData, ProjectileData 数据附件

client/renderer/ - 渲染器实现
register/        - 注册类
```

### 关键文件速查

| 文件 | 路径 | 作用 |
|------|------|------|
| Servant | `api/servant/Servant.java` | 仆从抽象基类 |
| MomentumServant | `api/servant/MomentumServant.java` | 带物理的仆从基类 |
| Projectile | `api/projectile/Projectile.java` | 射弹抽象基类 |
| ServantGoal | `api/ai/ServantGoal.java` | AI目标抽象类 |
| ServantGoalSelector | `api/ai/ServantGoalSelector.java` | AI目标选择器 |
| PathNode | `api/servant/PathNode.java` | 路径节点(位置+旋转) |
| ServantData | `common/attachment/ServantData.java` | 仆从数据附件 |
| ProjectileData | `common/attachment/ProjectileData.java` | 射弹数据附件 |

---

## 创建新仆从

### 步骤 1: 创建仆从类

继承 `MomentumServant`:

```java
public class MyServant extends MomentumServant {
    public int trailTimer = 0;  // 拖尾计时器

    public MyServant() {
        super();
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new MyTeleportGoal(this));  // 最高优先级
        goalSelector.addGoal(1, new MyAttackGoal(this));
        goalSelector.addGoal(2, new MyIdleGoal(this));
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
    public ServantType<? extends MomentumServant> getType() {
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
        return servant.getTarget() != null && servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        if (target == null) return;

        // 移动逻辑
        Vec3 toTarget = target.position().subtract(servant.getPos());
        servant.applyForce(toTarget.normalize().scale(0.1));

        // 朝向目标
        servant.setDesiredRotation(
            (float) Math.toDegrees(Math.atan2(-toTarget.x, toTarget.z)),
            0, 0
        );
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
public class MyServantRenderer implements IServantRenderer<MyServant>, IServantConeTrailRenderer {

    @Override
    public void render(MyServant servant, PoseStack poseStack, MultiBufferSource buffer,
                       float partialTick, int packedLight, PathNode renderNode) {
        // 渲染仆从本体
        poseStack.mulPose(Axis.YP.rotationDegrees(renderNode.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(renderNode.pitch()));
        // ... 绘制模型
    }

    @Override
    public int getTrailTimer(Servant servant) {
        return ((MyServant) servant).trailTimer;
    }

    @Override
    public int getTrailColorRGB(float progress) {
        return 0x00FF00;  // 绿色拖尾
    }
}
```

### 步骤 5: 注册渲染器

客户端初始化时:
```java
ServantRenderDispatcher.register(ServantRegister.MyServant.get(), new MyServantRenderer());
```

---

## 创建新射弹

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
            case ATTACHED -> tickAttached(owner);
        }
    }

    private void tickFlying(Player owner) {
        LivingEntity target = getCachedTarget();
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

        setTrailTimer(getTrailDuration());
    }

    private void onHit(Player owner, LivingEntity target) {
        // 造成伤害
        target.hurt(owner.damageSources().playerAttack(owner), getDamage());
        markForRemoval();
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) { }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) { }

    @Override
    public float getDamage() { return 5f; }

    @Override
    public ProjectileType<? extends Projectile> getType() {
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
public class MyProjectileRenderer implements IProjectileRenderer<MyProjectile>, IProjectileConeTrailRenderer {

    @Override
    public void render(MyProjectile projectile, PoseStack poseStack, MultiBufferSource buffer,
                       float partialTick, int packedLight, PathNode renderNode) {
        // 渲染射弹本体
    }

    @Override
    public int getTrailTimer(Projectile projectile) {
        return projectile.getTrailTimer();
    }
}
```

### 步骤 4: 注册渲染器

```java
ProjectileRenderDispatcher.register(ProjectileRegister.MyProjectile.get(), new MyProjectileRenderer());
```

---

## 常用 API

### Servant 常用方法

```java
// 位置与朝向
Vec3 getPos();
float getYaw(), getPitch(), getRoll();
void setDesiredRotation(float yaw, float pitch, float roll);

// 路径
void setPath(List<PathNode> nodes);
PathNode getRenderNode(float partialTick);
LinkedList<PathNode> getHistoryNodes();

// 目标
LivingEntity getTarget();
void setTarget(LivingEntity target);
boolean isTarget(LivingEntity entity);
LivingEntity searchTarget();

// 伤害
ServantDamageSource getDamageSource();
```

### MomentumServant 常用方法

```java
// 物理控制
Vec3 getVelocity();
void setVelocity(Vec3 velocity);
void applyForce(Vec3 force);      // 施加力
void applyImpulse(Vec3 impulse);  // 施加冲量
void teleportTo(Vec3 targetPos);  // 瞬移

// 参数调整
void setDrag(float drag);         // 阻力系数
void setMaxSpeed(float speed);    // 最大速度
void setRotationSpeed(float speed); // 转向速度
```

### Projectile 常用方法

```java
// 状态
ProjectileState getState();
void markForRemoval();
void attachTo(LivingEntity target);  // 黏贴到目标

// 物理
void applyForce(Vec3 force);
void setDesiredRotation(float yaw, float pitch, float roll);

// 可重写配置
float getHitRadius();      // 命中半径
double getMaxDistance();   // 最大距离
int getTrailDuration();    // 拖尾时长
```

---

## 拖尾渲染配置

实现拖尾接口时可重写的方法:

```java
// 基础配置
int getTrailHistoryLength()  { return 4; }    // 历史节点数
int getTrailSegmentsPerNode() { return 4; }   // 插值分段数
float getTrailMaxRadius()    { return 0.2f; } // 最大半径
int getTrailResolution()     { return 6; }    // 多边形边数

// 颜色与淡出
int getTrailColorRGB(float progress) { return 0xFF0000; }  // RGB颜色
float getTrailFadeOut(float progress) {
    return (float) Math.pow(1.0f - progress, 1.5);  // 淡出曲线
}
```

---

## 调试技巧

### 查看碰撞箱

开启 F3+B 调试渲染，或使用:
```java
if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
    LevelRenderer.renderLineBox(poseStack, consumer, hitbox, 1.0F, 0.0F, 0.0F, 1.0F);
}
```

### 检查数据同步

在 `writeAdditional` / `readAdditional` 中添加日志:
```java
@Override
public void writeAdditional(RegistryFriendlyByteBuf buf) {
    buf.writeInt(trailTimer);
    // System.out.println("Sync trailTimer: " + trailTimer);
}
```

### 目标搜索调试

```java
LivingEntity target = servant.searchTarget();
if (target != null) {
    System.out.println("Found target: " + target.getName().getString());
}
```

---

## 示例参考

| 实现 | 文件 | 特性 |
|------|------|------|
| StardustCell | `common/servent/StardustCell.java` | 瞬移攻击、发射射弹 |
| StardustProjectile | `common/projectile/StardustProjectile.java` | 追踪、黏贴、寄生效果 |
| Terraprism | `common/servent/Terraprism.java` | 另一种仆从实现 |

---

## 注意事项

1. **服务端/客户端分离**: tick 逻辑只在服务端执行，客户端只渲染
2. **并发安全**: 使用 `markForRemoval()` 而非直接移除，避免并发修改
3. **网络同步**: 所有自定义数据必须在 `writeAdditional` / `readAdditional` 中同步
4. **优先级**: Goal 优先级数字越小，优先级越高
5. **插值渲染**: 使用 `getRenderNode(partialTick)` 获取平滑渲染位置