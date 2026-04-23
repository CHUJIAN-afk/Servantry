# Servantry 模组 - AI Agent 技术文档

## 项目概述

Servantry 是一个 Minecraft NeoForge 模组，实现了"仆从"(Servant) 和"射弹"(Projectile) 系统。玩家可以召唤仆从自动战斗，仆从可以发射射弹攻击敌人。

## 核心架构

### 1. 数据存储模式

本模组采用 **玩家附件(Attachment)存储模式**，而非传统实体模式：
- 仆从/射弹数据存储在玩家的 `Attachment` 中
- 服务端执行 tick 逻辑，客户端通过数据同步驱动渲染
- 避免了实体同步的复杂性，适合大量小型战斗单位

### 2. 包结构

```
first.servantry/
├── api/                    # 核心 API 层
│   ├── servant/           # 仆从系统
│   ├── projectile/        # 射弹系统
│   ├── ai/                # AI 目标系统
│   ├── client/            # 客户端渲染接口
│   ├── register/          # 类型注册
│   └── event/             # 事件系统
├── common/                 # 具体实现
│   ├── servent/           # 仆从实现
│   ├── projectile/        # 射弹实现
│   ├── attachment/        # 数据附件
│   └── event/             # 事件处理
├── client/                 # 客户端专用
│   └── renderer/          # 渲染器实现
└── register/               # 注册类
```

---

## 仆从系统 (Servant System)

### 类层次

```
Servant (抽象基类)
    └── MomentumServant (动量物理仆从)
            └── StardustCell, Terraprism 等具体实现
```

### Servant 核心类

**文件**: `api/servant/Servant.java`

**职责**:
- 管理仆从的位置、朝向、路径
- 维护历史节点队列用于拖尾渲染
- 驱动 AI 目标选择器

**关键字段**:
| 字段 | 类型 | 说明 |
|------|------|------|
| `uuid` | UUID | 唯一标识符 |
| `owner` | Player | 所有者玩家 |
| `currentPathNode` | PathNode | 当前位置节点 |
| `historyNodes` | LinkedList<PathNode> | 历史轨迹节点 |
| `goalSelector` | ServantGoalSelector | AI 目标选择器 |
| `target` | LivingEntity | 当前攻击目标 |

**抽象方法** (子类必须实现):
```java
void registerGoals(ServantGoalSelector goalSelector);  // 注册 AI 目标
void writeAdditional(RegistryFriendlyByteBuf buf);     // 写入自定义同步数据
void readAdditional(RegistryFriendlyByteBuf buf);      // 读取自定义同步数据
float getDamage();                                      // 获取伤害值
float getKnockback();                                   // 获取击退力度
ServantType<? extends Servant> getType();              // 获取注册类型
```

### MomentumServant 物理仆从

**文件**: `api/servant/MomentumServant.java`

**特性**:
- 维护速度向量 `velocity`，支持惯性运动
- 视角平滑转向（独立于运动方向）
- 提供瞬移、冲量等便捷方法

**关键参数**:
| 参数 | 默认值 | 说明 |
|------|--------|------|
| `drag` | 0.95f | 阻力系数（每tick速度衰减） |
| `maxSpeed` | 0.5f | 最大速度 |
| `rotationSpeed` | 10.0f | 转向速度（度/tick） |

### PathNode 路径节点

**文件**: `api/servant/PathNode.java`

```java
public record PathNode(Vec3 pos, float yaw, float pitch, float roll) {
    PathNode lerp(PathNode to, float partialTick);  // 线性插值
}
```

### PlannedPath 计划路径

**文件**: `api/servant/PlannedPath.java`

管理一组路径节点的执行：
- `advance()` - 获取下一个节点并推进进度
- `isFinished()` - 检查路径是否完成
- `updateNodes()` - 动态修正轨迹

---

## 射弹系统 (Projectile System)

### Projectile 核心类

**文件**: `api/projectile/Projectile.java`

**状态枚举**:
```java
public enum ProjectileState {
    FLYING,     // 飞行状态：追踪目标、渲染拖尾
    ATTACHED,   // 黏贴状态：附着在目标身上
    DEAD        // 死亡状态：等待被移除
}
```

**关键特性**:
- 内置动量物理系统（与 MomentumServant 类似）
- 支持目标追踪
- 支持黏贴到目标身上（如附着的投射物）
- 历史节点队列用于拖尾渲染

**抽象方法**:
```java
void tickBehavior(Player owner);                                          // 射弹行为逻辑
void writeAdditional(RegistryFriendlyByteBuf buf);                       // 自定义同步数据
void readAdditional(RegistryFriendlyByteBuf buf);                        // 读取同步数据
float getDamage();                                                        // 伤害值
ProjectileType<? extends Projectile> getType();                          // 注册类型
```

**可重写方法**:
```java
int getHistoryNodesSize()  { return 8; }     // 历史节点队列大小
float getHitRadius()       { return 0.5f; }  // 命中检测半径
double getMaxDistance()    { return 128.0; } // 最大飞行距离
float getSpinSpeed()       { return 5f; }    // 自转速度
int getTrailDuration()     { return 15; }    // 拖尾持续时间
```

---

## AI 目标系统

### ServantGoal 抽象目标

**文件**: `api/ai/ServantGoal.java`

```java
public abstract class ServantGoal<T extends Servant> {
    protected final T servant;

    public abstract boolean canUse();           // 是否可以开始
    public boolean canContinueToUse();          // 是否可以继续
    public boolean isInterruptable();           // 是否可被中断
    public void start() {}                      // 开始时调用
    public void tick() {}                       // 每tick调用
    public void stop() {}                       // 结束时调用
}
```

### ServantGoalSelector 目标选择器

**文件**: `api/ai/ServantGoalSelector.java`

**特性**:
- 使用 `TreeSet` 按优先级管理目标
- 优先级数字越小，优先级越高
- 高优先级目标可以中断低优先级目标

**使用示例**:
```java
@Override
public void registerGoals(ServantGoalSelector goalSelector) {
    goalSelector.addGoal(0, new TeleportGoal(this));    // 最高优先级
    goalSelector.addGoal(1, new AttackGoal(this));
    goalSelector.addGoal(2, new IdleGoal(this));        // 最低优先级
}
```

---

## 渲染系统

### 渲染调度器

**ServantRenderDispatcher**: `api/client/ServantRenderDispatcher.java`
- 管理所有仆从渲染器
- 在客户端渲染事件中调用

**ProjectileRenderDispatcher**: `api/client/ProjectileRenderDispatcher.java`
- 管理所有射弹渲染器
- 支持拖尾渲染接口

### 渲染器接口

**IServantRenderer**: 仆从渲染器接口
```java
void render(T servant, PoseStack poseStack, MultiBufferSource bufferSource,
            float partialTick, int packedLight, PathNode renderNode);
```

**IProjectileRenderer**: 射弹渲染器接口
```java
void render(T projectile, PoseStack poseStack, MultiBufferSource bufferSource,
            float partialTick, int packedLight, PathNode renderNode);
```

### 拖尾渲染接口

**IServantConeTrailRenderer**: 仆从圆锥形拖尾
**IServantRibbonTrailRenderer**: 仆从飘带形拖尾
**IProjectileConeTrailRenderer**: 射弹圆锥形拖尾

**拖尾渲染流程**:
1. 检查拖尾计时器
2. 获取历史节点并构建插值数组
3. Catmull-Rom 样条插值生成平滑节点
4. 构建多边形截面并绘制四边形带
5. 绘制半球形头部封闭拖尾

---

## 数据附件

### ServantData

**文件**: `common/attachment/ServantData.java`

**功能**:
- 存储玩家的所有仆从
- 实现 `AttachmentSyncHandler` 支持网络同步
- 提供目标搜索缓存

**关键方法**:
```java
boolean summon(Player player, Servant servant);  // 召唤仆从
void remove(ServantType<?> type);                 // 移除仆从
int getMaxSize(Player player);                    // 获取最大数量
List<LivingEntity> getNearbyTargets(...);         // 获取附近目标
```

### ProjectileData

**文件**: `common/attachment/ProjectileData.java`

**功能**:
- 存储玩家的所有射弹
- 提供并发安全的添加和移除操作
- 支持按状态筛选射弹

---

## 类型注册系统

### ServantType

**文件**: `api/register/ServantType.java`

```java
public record ServantType<T extends Servant>(Supplier<T> factory) {}
```

### ProjectileType

**文件**: `api/register/ProjectileType.java`

```java
public record ProjectileType<T extends Projectile>(Supplier<T> factory) {}
```

### Registries

**文件**: `api/register/Registries.java`

自定义注册表管理：
- `SERVANT_TYPES` - 仆从类型注册表
- `PROJECTILE_TYPES` - 射弹类型注册表

---

## 创建新仆从的步骤

### 1. 创建仆从类

```java
public class MyServant extends MomentumServant {
    public MyServant() {
        super();
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new MyAttackGoal(this));
        goalSelector.addGoal(1, new MyIdleGoal(this));
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) { }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) { }

    @Override
    public float getDamage() { return 10f; }

    @Override
    public float getKnockback() { return 0.5f; }

    @Override
    public ServantType<? extends MomentumServant> getType() {
        return ServantRegister.MyServant.get();
    }
}
```

### 2. 注册仆从类型

```java
public class ServantRegister {
    public static final DeferredHolder<ServantType<?>, ServantType<MyServant>> MyServant =
        SERVANT_TYPES.register("my_servant", () -> new ServantType<>(MyServant::new));
}
```

### 3. 创建渲染器

```java
public class MyServantRenderer implements IServantRenderer<MyServant>, IServantConeTrailRenderer {
    @Override
    public void render(MyServant servant, PoseStack poseStack, MultiBufferSource bufferSource,
                       float partialTick, int packedLight, PathNode renderNode) {
        // 渲染逻辑
    }

    @Override
    public int getTrailTimer(Servant servant) {
        return ((MyServant) servant).trailTimer;
    }
}
```

### 4. 注册渲染器

```java
// 在客户端初始化时
ServantRenderDispatcher.register(ServantRegister.MyServant.get(), new MyServantRenderer());
```

---

## 创建新射弹的步骤

### 1. 创建射弹类

```java
public class MyProjectile extends Projectile {
    @Override
    public void tickBehavior(Player owner) {
        // 行为逻辑
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

### 2. 注册射弹类型

```java
public class ProjectileRegister {
    public static final DeferredHolder<ProjectileType<?>, ProjectileType<MyProjectile>> MyProjectile =
        PROJECTILE_TYPES.register("my_projectile", () -> new ProjectileType<>(MyProjectile::new));
}
```

### 3. 创建并注册渲染器

```java
public class MyProjectileRenderer implements IProjectileRenderer<MyProjectile>, IProjectileConeTrailRenderer {
    // 实现接口方法
}

// 注册
ProjectileRenderDispatcher.register(ProjectileRegister.MyProjectile.get(), new MyProjectileRenderer());
```

---

## 示例实现

### StardustCell (星尘细胞仆从)

**文件**: `common/servent/StardustCell.java`

特性：
- 瞬移攻击
- 发射星细胞射弹
- 玩家攻击联动

### StardustProjectile (星细胞射弹)

**文件**: `common/projectile/StardustProjectile.java`

特性：
- 追踪目标
- 命中后黏贴在目标身上
- 施加细胞寄生效果

---

## 关键设计模式

1. **数据驱动渲染**: 服务端计算，客户端渲染
2. **组件化 AI**: 通过 Goal 组合行为
3. **接口分离**: 渲染器接口与拖尾接口分离
4. **工厂模式**: 通过 Type 记录创建实例
5. **插值渲染**: 使用历史节点实现平滑拖尾
