---
name: terraria-port
description: 将 Terraria 召唤武器/仆从/射弹移植到 Servantry 模组。输入 Terraria Wiki 页面内容，自动生成开发计划并逐步实现。
triggers:
  - terraria
  - port
  - 移植
  - 泰拉
---

# Terraria → Servantry 移植 Skill

将 Terraria 召唤武器/仆从/射弹移植到 Servantry 模组的标准化工作流。

## 使用方式

用户提供 Terraria Wiki 页面内容（或描述），Skill 引导完成从分析到实现的完整流程。

## 项目核心信息

| 项目 | 值 |
|------|-----|
| 模组 ID | `servantry` |
| 包名 | `first.servantry` |
| 平台 | NeoForge 1.21.1 |
| JDK | 21 |
| 依赖 | Curios (饰品), GeckoLib (动画), JEI (可选) |
| 资源加载 | 数据生成器 (`src/generated/resources/`) + 手动资源 (`src/main/resources/`) |
| 作者 | FirstSight |

## 架构总览

Servantry 使用 **附件实体 (AttachmentEntity)** 系统——不基于原版 Entity 的轻量级实体系统，运行在玩家 Data Attachment 之上。所有仆从、射弹、哨兵都是 AttachmentEntity 子类，由 `EntityData` 附件按 `Type` 枚举分组管理。

### 继承层次

```
AttachmentEntity (api/entity/AttachmentEntity.java) — 抽象基类
├── Servant (api/servant/Servant.java)                — AI 驱动的仆从
│   └── MomentumServant (api/servant/MomentumServant.java) — 动量物理仆从
│       ├── DeadlySphere, StardustCell, StardustDragon, VoidEater
│       ├── Terraprism (直接继承 Servant，非 MomentumServant)
│       ├── Twins, EnchantedThrowingKnives, EtherealStellarCore
│       ├── OreScout, ScavengerFairy, ChlorophyteCrystal, InfiniteShadow, Sharknado
│       └── 哨兵仆从（均继承 MomentumServant，注册到 SentryServant 槽位）
│           ├── Ballista, Cloud, MoonPortal, PulseTurret
│           └── RainbowCrystal, SuperPeashooter
│
└── Projectile (api/projectile/Projectile.java)        — 射弹
    ├── AttachingProjectile (api/projectile/AttachingProjectile.java) — 黏着射弹
    │   ├── MiniStardustCell (星细胞射弹)
    │   └── CrossbowBolt
    └── 直接继承 Projectile
        ├── Laser, CustomLaser, DemonFlame, GodFlame, SharkDragon
        ├── Zenith, BlitzBall, Rain, DestructionBullet
        └── ChlorophyteCrystal/RainbowCrystal/ShatteredStellarCore (射弹版)
```

### 注册表与分组系统

- **`AttachmentEntityType<T>`** — 自定义注册表 (`servantry:attachment_entity_types`)，record 仅含 `Supplier<T> factory`
- 注册位置：`register/ServantryAttachmentEntityRegister.java`
- 自定义注册表：`api/register/ServantryRegistries.java`

`EntityData` 按 `Type` 枚举分组：

| Type | 说明 | 栏位上限属性 |
|------|------|-------------|
| `Servant` | 普通仆从 | `ServantryAttributeRegister.ServantMaxCount` |
| `SentryServant` | 哨兵仆从 | `ServantryAttributeRegister.SentryServantMaxCount` |
| `ExtraServant` | 额外仆从 | 无（`canSummon` 返回 0） |
| `Projectile` | 射弹 | 无栏位限制 |

> `ExtraServant` 和 `Projectile` 的 `canSummon()` 始终返回 false。射弹通过 `projectile.join(owner)` 添加，内部直接调用 `entityData.add(EntityData.Type.Projectile, this)` 绕过检查。

## 核心转换规则

### 数值转换

| Terraria 属性 | 转换规则 | 示例 |
|--------------|---------|------|
| 伤害 | ÷10 | 7 → 0.7f, 50 → 5.0f |
| 击退 | ÷10 | 4 → 0.4f, 2 → 0.2f |
| 使用时间 | 不映射，Servantry 统一 4 tick 冷却 | — |
| 射弹速度 | 接触型仆从不映射；远程射弹映射为 maxSpeed（÷10） | 10 → setMaxSpeed(1.0f) |
| 搜索距离 | 项目惯例 32 格（Terraria 50图格→32格） | — |
| 稀有度 | White→COMMON, Blue→UNCOMMON, Green/LightRed→RARE, Cyan/LightPurple→EPIC | — |
| 无敌帧 | ÷4 向上取整，使用 PARTIAL 模式 | 15 → ceil(15/4)=4, 10 → ceil(10/4)=3, 20 → 5 |
| 冷却/持续时间 | ÷3 向上取整（Terraria 60tick/s → MC 20tick/s） | 45tick→ceil(45/3)=15, 35tick→ceil(35/3)=12, 600tick→200 |

### 行为映射

| Terraria 行为 | Servantry 实现 |
|--------------|---------------|
| 接触伤害仆从 | MomentumServant + ICollideAttack |
| 远程射弹仆从 | MomentumServant + 在 AttackGoal 中创建 Projectile 子类 |
| 黏着射弹 | AttachingProjectile |
| 固定哨兵 | MomentumServant + .sentryServant() + SentryServant 槽位 |
| 多体节仆从 | 多个 MomentumServant 实例 + segmentIndex/totalSegments |
| 局部无敌帧 | InvincibleData.PARTIAL（按 UUID 隔离，天然等价） |
| 全局无敌帧 | InvincibleData.GLOBAL（调用 .global()） |
| 视线检测 | 使用 TargetCache.isVisibility() 默认行为 |
| 返回玩家 | IdleGoal 中 teleportTo() 或 applyForce() |
| 鸟巢/装饰渲染 | modelModify() 中额外渲染模型 |
| 盘旋/悬停 | getWanderPos() 计算悬停位置，applyForce 飞向 |
| 施加减益 | 射弹 .effect(new MobEffectInstance(...)) 或仆从 onCollisionAttack 中添加 |
| 无朝向仆从 | `setDesiredRotation(currentPathNode.yaw() + 2, currentPathNode.pitch() + 2, currentPathNode.roll() + 2)` 在 tick() 中缓慢自旋，适用于不需要面向移动/目标方向的仆从 |
| 无俯仰角仆从 | 重写 `lookAtDirection(Vec3)` 只设置 yaw，pitch 固定为 0：`setDesiredRotation(targetYaw, 0, getRoll())` |

### 复用模式

**1. getWanderPos 复用（盘旋/悬停/待机）**

`MomentumServant.getWanderPos(lastWanderPos, targetPos, distance, height)` 自动处理：
- 2.5% 概率刷新位置
- 超出 distance 时刷新
- height 保证最低高度

适用场景：
- IdleGoal 待机徘徊：`getWanderPos(wanderPos, ownerPos, 4, 1)`
- AttackGoal 悬停攻击：`getWanderPos(hoverPos, targetCenter, 3, 3)`
- 任何"在目标周围某距离处徘徊"的行为

**2. 碰撞攻击模式（onCollisionAttack）**

```java
// 单目标命中（射弹/单次碰撞）
@Override
public void onCollisionAttack(List<HitContext> hitContexts) {
    HitContext hit = hitContexts.getFirst();
    InvincibleData.attack(hit.entity())
            .attacker(getUuid())
            .damageSource(source)
            .damageAmount(getDamage())
            .invincibleTime({invincibleTime})
            .effect(new MobEffectInstance(MobEffects.POISON, 80 + getOwner().getRandom().nextInt(61)))  // 可选
            .apply();
    setRemove();  // 射弹命中后移除
}

// 多目标命中（仆从碰撞）
@Override
public void onCollisionAttack(List<HitContext> hitContexts) {
    for (HitContext hit : hitContexts) {
        InvincibleData.attack(hit.entity())
                .attacker(getUuid())
                .damageSource(getDamageSource())
                .damageAmount(getDamage())
                .invincibleTime({invincibleTime})
                .apply();
    }
}
```

**3. 射弹发射模式（仆从远程攻击）**

`setDamageSource(ServantDamageSource)` 已内联复制 damage/knockback/armorPierce（从 servant 提取），无需手动 set。
```java
// 在仆从类中
public void shootAtTarget(LivingEntity target) {
    Vec3 startPos = getPos();
    Vec3 direction = target.getBoundingBox().getCenter().subtract(startPos).normalize();
    // setDamageSource(getDamageSource()) 内部自动复制 damage/knockback/armorPierce
    YourProjectile projectile = new YourProjectile(getDamageSource(), startPos, direction);
    projectile.join(owner);
    applyForce(direction.scale(-0.3));  // 后坐力
}
```

**4. Curio 配饰强化仆从模式**

蜂巢背包模式：仆从在计算属性时检查玩家装备。
```java
// 仆从类中
public int getStingerCooldown() {
    if (CuriosUtil.isEquipped(owner, ServantryCurioRegister.HivePack.get())) {
        return 12;  // 强化值
    }
    return 15;  // 默认值
}
```

## 工作流程

### Phase 1: 解析 Wiki

从用户提供的 Wiki 内容中提取：

1. **基础属性**：伤害、击退、使用时间、射弹速度、稀有度
2. **仆从行为**：攻击方式（接触/远程）、移动方式（飞行/地面/悬浮）、待机行为
3. **特殊机制**：无敌帧类型与数值、视线检测、多召唤、特殊效果
4. **获取方式**：掉落/制作/宝箱

### Phase 2: 确认决策

向用户提问确认以下决策（使用 AskUserQuestion）：

**必问项：**
1. 基类选择（MomentumServant / Servant / Projectile）
2. 渲染风格（纯代码 / GeckoLib 模型 / 拖尾+简单模型 / 占位模型）
3. 获取方式（秘银砧配方 / 暂不需要 / 其他）
4. 特殊行为是否保留（视线检测、特殊击退方向等）
5. **AI 行为逐条确认** — 攻击逻辑、待机逻辑、返回逻辑、特殊机制，必须与用户目标效果一致后再开始实现
6. **朝向模式** — 仆从是否需要面向目标/移动方向？无朝向仆从使用 `setDesiredRotation(currentPathNode.yaw()+2, pitch+2, roll+2)` 缓慢自旋

**条件问项（根据仆从类型）：**
- 哨兵类：确认 SentryServant 槽位
- 远程类：确认射弹类型（Projectile / AttachingProjectile）
- 多体节类：确认体节管理方式
- 有鸟巢/装饰：确认渲染方式

### Phase 3: 生成开发计划

写入 `.claude/plans/<name>-plan.md`，包含：

1. 原版数据映射表（含转换规则）
2. 行为设计描述
3. 新建/修改文件清单
4. 分步实现细节

### Phase 4: 实现

按计划逐步创建文件，遵循 agent.md 中的架构规范：

1. 仆从实体类 → `common/servant/`
2. AI Goal 类 → `common/servant/goal/<name>/`
3. 射弹类（可选）→ `common/projectile/`
4. 注册实体类型 → `ServantryAttachmentEntityRegister.java`
5. 注册武器 → `ServantryServantWeaponRegister.java`
6. 渲染器 → `client/attachmentEntityRenderer/`
7. 模型注册（如需）→ `ServantryModelRegister.java`
8. 渲染器注册 → `client/ClientEvent.java`
9. **AI 复查** — 对照 Phase 2 确认的行为逐条验证代码实现，确保无偏差

## 代码模板

### MomentumServant 模板

```java
package first.servantry.common.servant;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.*;
import first.servantry.api.entity.ICollideAttack.HitContext;
import first.servantry.api.entity.IBlockCollision.CollisionContext;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.servant.goal.{name}.{Name}AttackGoal;
import first.servantry.common.servant.goal.{name}.{Name}IdleGoal;
import first.servantry.register.ServantryAttachmentEntityRegister;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class {Name} extends MomentumServant
        implements ICollideAttack<{Name}>, IBlockCollision<{Name}> {

    public {Name}() {
        super();
        setDrag({drag});
        setGravity({gravity});
        setRotationSpeed({rotSpeed});
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new {Name}AttackGoal(this));
        goalSelector.addGoal(2, new {Name}IdleGoal(this));
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public boolean canCollideWithBlocks() {
        return !isExecutingPath();
    }

    @Override
    public boolean canCollideAttack() {
        return isTarget(getTarget());
    }

    @Override
    public boolean isValidCollisionTarget({Name} entity, LivingEntity target) {
        return isTarget(target);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            InvincibleData.attack(hit.entity())
                    .attacker(getUuid())
                    .damageSource(getDamageSource())
                    .damageAmount(getDamage())
                    .invincibleTime({invincibleTime})  // ceil(Terraria帧/4)
                    .apply();
        }
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setVelocity(IBlockCollision.bounceVelocity(getVelocity(), context, 0.98, 0.01));
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public AttachmentEntityType<? extends MomentumServant> getType() {
        return ServantryAttachmentEntityRegister.{Name}.get();
    }
}
```

### AttackGoal 模板

```java
package first.servantry.common.servant.goal.{name};

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.{Name};
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class {Name}AttackGoal extends ServantGoal<{Name}> {

    private int cooldown = 0;

    public {Name}AttackGoal({Name} servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        servant.lookAtPos(targetPos);

        if (--cooldown <= 0) {
            cooldown = {attackCooldown};
            Vec3 direction = targetPos.subtract(servant.getPos()).normalize();
            servant.applyForce(direction.scale({chargeForce}));
        }
    }
}
```

### IdleGoal 模板

```java
package first.servantry.common.servant.goal.{name};

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.{Name};
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class {Name}IdleGoal extends ServantGoal<{Name}> {

    private Vec3 wanderPos = Vec3.ZERO;

    public {Name}IdleGoal({Name} servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return !servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        Player owner = servant.getOwner();
        Vec3 ownerPos = owner.getBoundingBox().getCenter();
        wanderPos = servant.getWanderPos(wanderPos, ownerPos, {wanderRadius}, {wanderHeight});
        double distance = servant.getPos().distanceTo(wanderPos);
        servant.applyForce(wanderPos.subtract(servant.getPos()).normalize()
                .scale(Math.min(distance * {approachFactor}, {maxApproachForce})));
        if (servant.getPos().distanceToSqr(ownerPos) > {returnDist} * {returnDist}) {
            servant.teleportTo(ownerPos);
        }
    }
}
```

### Projectile 模板

```java
package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.*;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.ServantryAttachmentEntityRegister;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class {Name}Projectile extends Projectile
        implements ICollideAttack<{Name}Projectile>, IBlockCollision<{Name}Projectile> {

    public {Name}Projectile() {
        super();
    }

    public {Name}Projectile(DamageSource damageSource, Vec3 startPos, Vec3 direction) {
        super(startPos, direction);
        setDamageSource(damageSource);
        setDrag({drag});
        setMaxSpeed({maxSpeed});
        setMaxLife({maxLife});
        setGravity({gravity});
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        DamageSource source = getDamageSource();
        if (source != null) {
            for (HitContext hit : hitContexts) {
                InvincibleData.attack(hit.entity())
                        .attacker(getUuid())
                        .damageSource(source)
                        .damageAmount(getDamage())
                        .invincibleTime({invincibleTime})  // ceil(Terraria帧/4)
                        .apply();
            }
        }
        setRemove();
    }

    @Override
    public boolean isValidCollisionTarget({Name}Projectile entity, LivingEntity target) {
        if (entity.getDamageSource() instanceof ServantDamageSource sds) {
            return sds.getServant().isTarget(target);
        }
        return ICollideAttack.super.isValidCollisionTarget(entity, target);
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return getHitbox();
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        currentPathNode = new PathNode(context.position(),
                currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll());
        setRemove();
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.{Name}Projectile.get();
    }
}
```

### 武器注册模板

```java
public static final DeferredItem<ServantWeaponItemBuilder<{Name}>.ServantWeaponItemItem> {WeaponName} =
        ServantryItemRegisterBuilder.build(SERVANT_WEAPON, "{weapon_id}", () -> new ServantWeaponItemBuilder<>(ServantryAttachmentEntityRegister.{Name})
                        .damage({damage}f)            // Terraria伤害 ÷ 10
                        .knockback({knockback}f)      // Terraria击退 ÷ 10
                        .armorPierce({armorPierce}f)  // 可选
                        .sound(ServantrySoundRegister.UseServantWeapon)
                        .summon((weapon, player) -> {
                            {Name} servant = weapon.createServant(player);
                            ServantryHelper helper = ServantryHelper.get(player);
                            if (helper.canSummon(EntityData.Type.{SlotType}, 1)) {
                                servant.init(new PathNode(
                                        player.getBoundingBox().getCenter().offsetRandom(player.getRandom(), 2),
                                        0, 0, 0));
                                helper.add(EntityData.Type.{SlotType}, servant);
                            }
                        })
                        .properties(properties -> properties.rarity(Rarity.{Rarity}))
                        .build())
                // .recipe(...) — 可选
                .itemLanguage("{EnName}", "{ZhName}")
                .servantLanguage(ServantryAttachmentEntityRegister.{Name}, "{ServantEnName}", "{ServantZhName}")
                // 不要添加 itemLanguageTooltip 描述"召唤xxx为你而战"
                // 架构已通过 IServantWeaponItem.getTooltips() 自动生成召唤提示
                .itemModel(ServantryItemRegisterBuilder::handheldItem)
                .itemTag(ServantryItemTagsRegister.ServantWeapon)
                .build();
```

### 渲染器模板（AbstractAttachmentEntityRenderer）

```java
package first.servantry.client.attachmentEntityRenderer.servant;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.api.client.render.AbstractAttachmentEntityRenderer;
import first.servantry.api.client.render.ModelRenderer;
import first.servantry.api.client.render.RenderContext;
import first.servantry.api.client.render.renderConfig.ModelConfig;
import first.servantry.api.entity.PathNode;
import first.servantry.common.servant.{Name};
import first.servantry.register.ServantryModelRegister;
import net.minecraft.client.renderer.MultiBufferSource;

public class {Name}Renderer extends AbstractAttachmentEntityRenderer<{Name}> {

    @Override
    protected RenderContext<{Name}> createContext({Name} entity) {
        return RenderContext.<{Name}>builder()
                // .trail(...) — 可选拖尾
                .model(new ModelConfig<{Name}>()
                        .scale({scale}f)
                        .translateOffset({tx}f, {ty}f, {tz}f)
                        .rotationOffset({yawOff}, {pitchOff}, {rollOff})
                        .alphaDistanceFactor({alphaDist}f)
                )
                .build();
    }

    @Override
    protected void render({Name} entity, PoseStack poseStack,
                          MultiBufferSource bufferSource, PathNode visualNode,
                          RenderContext<{Name}> context, float partialTick) {
        ModelRenderer.renderModel(ServantryModelRegister.{MODEL}, poseStack, bufferSource);
    }
}
```

## 注意事项

1. **伤害/击退必须 ÷10** — Terraria 数值体系与 Minecraft 不同
2. **无敌帧 ÷4 向上取整** — Terraria 15帧 → Servantry ceil(15/4)=4, Terraria 10帧 → ceil(10/4)=3
3. **无参构造器必须存在** — 网络反序列化需要 `getType().factory().get()`
4. **init() 在 add() 之前** — 先初始化位置再添加到 EntityData
5. **射弹用 join()** — `projectile.join(owner)` 而非 `helper.add()`
6. **碰撞盒是局部坐标** — 相对于实体中心，不是世界坐标
7. **writeAdditional/readAdditional** — 自定义数据必须同步
8. **getInterpolatedIdleState** — 不是基类方法，各仆从自行实现
9. **栏位系统** — Servant/SentryServant 有上限，Projectile 无限制
10. **isExecutingPath()** — MomentumServant 排除 "physics" 路径
11. **渲染注册在 client/ClientEvent.java** — 不是 api 包的 ClientEvent
12. **不要添加 itemLanguageTooltip** — "召唤xxx为你而战"提示由 IServantWeaponItem.getTooltips() 自动生成，无需手动注册
13. **AI 逻辑必须反复确认** — 召唤物的攻击/待机/返回/特殊行为必须与目标效果一致，实现前向用户逐条确认关键行为细节，实现后对照复查
14. **Tick 换算** — Terraria 60tick/s，MC 20tick/s。冷却/持续时间用 `ceil(terrariaTick/3)`，无敌帧用 `ceil(terrariaFrames/4)`
15. **非召唤物逻辑不做** — 蜂巢背包等配饰对非召唤武器（蜜蜂枪/养蜂人等）的强化效果不在本 Skill 范围内，只实现与仆从/射弹直接相关的部分
16. **不要用 new 跳过注册** — 使用 `getType().factory().get()` 或 `weapon.createServant(player)` 通过工厂创建，然后通过 `helper.add()` 或 `projectile.join()` 添加到玩家数据
17. **TargetCache** — 碰撞检测和目标搜索都依赖 `TargetCache` 附件，它缓存玩家周围的 LivingEntity 列表
18. **维度切换** — Servant 会重置位置到玩家中心，Projectile 会直接 `setRemove()`
19. **栏位溢出** — EntityData.tick() 中自动检查 Servant/SentryServant 栏位，超出上限时移除最早的仆从

## 开发步骤详解

### 第一步：创建仆从实体类

**路径：** `src/main/java/first/servantry/common/servant/YourServant.java`

#### 基类选择

| 场景 | 基类 | 说明 |
|------|------|------|
| 需要动量物理（冲撞/飞行/弹跳） | `MomentumServant` | 提供 velocity/drag/gravity/applyForce/lookAtPos 等 |
| 不需要物理（路径驱动/悬浮跟随） | `Servant` | 如 Terraprism 直接继承 Servant |
| 哨兵（固定/追踪炮台） | `MomentumServant` + 注册到 `SentryServant` 槽位 | 在武器注册时调用 `.sentryServant()` |

#### 关键细节

- `getDamageSource()` 返回 `ServantDamageSource`，内含 `servant` 引用，射弹可从中获取仆从的目标判定
- `InvincibleData.attack()` 有两种无敌帧模式：`PARTIAL`（默认，按 UUID 隔离）和 `GLOBAL`（调用 `.global()` 切换）
- `isTarget()` 判定逻辑：Enemy 接口 → Targeting 指向玩家 → 互相攻击过

### 第二步：创建 AI 目标类

**路径：** `src/main/java/first/servantry/common/servant/goal/yourservant/`

#### ServantGoal 生命周期

| 方法 | 调用时机 |
|------|---------|
| `canUse()` | 每 tick 检查，决定是否启动 |
| `canContinueToUse()` | 默认委托 `canUse()`，可重写 |
| `start()` | 目标开始执行时调用一次 |
| `tick()` | 每帧执行 |
| `stop()` | 目标停止时调用一次 |
| `isInterruptable()` | 默认 true，低优先级目标能否被高优先级打断 |

#### ServantGoalSelector 优先级规则

数值越小优先级越高。同一 tick 内，只有最高优先级的 `canUse()=true` 的 Goal 会执行。当前 Goal 可被更低数值（更高优先级）的 Goal 打断（前提是 `isInterruptable()=true`）。

### 第三步：创建射弹类（可选）

参见 Projectile 模板。关键行为：

- `tick()` 中自动执行 `tickPhysics()`（应用 drag/gravity、限速、更新朝向、推进位置）
- 超过 `maxLife` 或距 owner 超过 `getMaxDistance()`（默认 128 格）自动 `setRemove()`
- `getHistoryNodesSize()` 默认 8（比 Servant 的 16 小，适合短拖尾）
- `join(Player)` 内部调用 `ServantryHelper.get(owner).add(EntityData.Type.Projectile, this)`

#### AttachingProjectile 子类（黏着射弹）

如需黏着射弹（如 MiniStardustCell），继承 `AttachingProjectile`：
- `attachTo(Vec3 position)` — 黏着到指定位置
- `setAttachedTarget(LivingEntity)` — 设置跟随目标
- `isAttached()` — 是否处于黏着状态
- 黏着时 `tickPhysics()` 不执行飞行物理，而是跟随目标移动

### 第四步：注册附件实体类型

**文件：** `register/ServantryAttachmentEntityRegister.java`

```java
public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<YourServant>> YourServant =
        register("your_servant", YourServant::new);

public static final DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<YourProjectile>> YourProjectile =
        register("your_projectile", YourProjectile::new);
```

注册方法签名：
```java
private static <T extends AttachmentEntity> DeferredHolder<AttachmentEntityType<?>, AttachmentEntityType<T>>
    register(String name, Supplier<T> supplier)
```
- `name` 是注册表键名（小写下划线），会变成 `servantry:your_servant`
- `supplier` 是工厂方法引用，用于 `AttachmentEntityType.factory().get()` 创建实例

### 第五步：创建召唤武器物品

**文件：** `register/ServantryServantWeaponRegister.java`

关键配置方法：

| 方法 | 说明 |
|------|------|
| `.damage(float)` | 仆从攻击伤害值，通过 `createServant()` 写入 `servant.setDamage()` |
| `.knockback(float)` | 仆从击退力度 |
| `.armorPierce(float)` | 护甲穿透 |
| `.sentryServant()` | 标记为哨兵（使用 `SentryServant` 槽位） |
| `.sound(Supplier<SoundEvent>)` | 召唤音效 |
| `.summon(BiConsumer)` | 自定义召唤逻辑，不设置则使用默认 |
| `.properties(Consumer<Item.Properties>)` | 物品属性，`stacksTo(1)` 已由 build() 自动设置 |
| `.onRemove(Consumer<Player>)` | 移除回调，默认调用 `IServantWeaponItem.remove()` |

`createServant(player)` 做了什么：
```java
T servant = getType().factory().get();  // 通过工厂创建
servant.setOwner(player);
servant.setDamage(getDamage());
servant.setKnockback(getKnockback());
servant.setArmorPierce(getArmorPierce());
return servant;
```

默认 summon 逻辑（未调用 `.summon()` 时）：
```java
T servant = weapon.createServant(player);
ServantryHelper helper = ServantryHelper.get(player);
if (helper.canSummon(EntityData.Type.Servant, 1)) {
    Vec3 pos = player.getBoundingBox().getCenter().offsetRandom(player.getRandom(), 2);
    servant.init(new PathNode(pos, 0, 0, 0));
    helper.add(EntityData.Type.Servant, servant);
}
```

武器交互行为（`ServantWeaponItemBuilder.handler`）：
- 右键（主手）→ 召唤仆从
- Shift + 右键 → 移除所有同类型仆从
- 4 tick 冷却
- 自动播放音效

`getInterpolatedIdleState` 模式：许多仆从使用 `servant.init(servant.getInterpolatedIdleState(1.0f))` 而非手动计算位置。**不是基类方法**，各仆从子类自行定义，用于计算跟随玩家时的理想悬浮位置。

### 第六步：创建渲染器

见渲染器模板。注册在 `client/ClientEvent.java` 的 `register` 方法中（`@SubscribeEvent` 监听 `EntityRenderersEvent.RegisterRenderers`）。

> 注意：渲染器注册在 `client/ClientEvent.java`（非 `api/client/ClientEvent.java`）。

渲染调度流程：
1. `RenderLevelStageEvent` 触发 → `AttachmentEntityRenderDispatcher.render()`
2. 遍历所有玩家的 `EntityData.getRenderCache()`
3. 对每个实体：平移到渲染位置 → 调用 `IAttachmentEntityRenderer.render()`
4. `AbstractAttachmentEntityRenderer` 内部：先渲染拖尾 → 再渲染本体

### 第七步：在仆从攻击目标中发射射弹

```java
// 在仆从类中
public void shootAtTarget(LivingEntity target) {
    Vec3 startPos = getPos().add(0, 0.5, 0);
    Vec3 direction = target.getBoundingBox().getCenter().subtract(startPos).normalize();
    YourProjectile projectile = new YourProjectile(getDamageSource(), startPos, direction);
    projectile.join(owner);  // 加入到玩家的 Projectile 数据
    applyForce(direction.scale(-0.3));  // 后坐力
}
```

射弹发射关键点：
- `getDamageSource()` 返回 `ServantDamageSource`，射弹的 `isValidCollisionTarget` 可通过 `sds.getServant().isTarget()` 判定目标
- `setDamageSource(ServantDamageSource)` 内联复制 damage/knockback/armorPierce，无需手动 set
- `join(Player)` 将射弹添加到 `EntityData.Type.Projectile` 分组
- 射弹不需要 `init()`——`Projectile(Vec3, Vec3)` 构造器已自动调用 `init()`

### 第八步：可选 — 配置数据生成

- **物品模型**：数据生成器自动处理 `ServantryItemRegisterBuilder::handheldItem` 或 `basicModel`
- **语言文件**：`itemLanguage(en, zh)` → `item.servantry.xxx`，`servantLanguage(holder, en, zh)` → `servant.servantry.xxx`
- **配方**：通过 `recipe()` 链式调用生成秘银砧配方
- **物品标签**：通过 `itemTag()` 链式调用添加到标签

## 核心 API 参考

### AttachmentEntity 基类

| 方法/字段 | 说明 |
|-----------|------|
| `tick()` | 每 tick 调用：方块碰撞 → 碰撞攻击 → 历史轨迹更新 → 路径推进 |
| `getRenderNode(partialTick)` | 获取插值后的渲染节点（historyNodes[1] → currentPathNode 线性插值） |
| `getPos()` / `getYaw()` / `getPitch()` / `getRoll()` | 当前位置/旋转（来自 currentPathNode） |
| `setPath(List<PathNode>)` | 设置路径节点列表，创建默认 PlannedPath |
| `setPlannedPath(PlannedPath)` | 设置计划路径 |
| `setRemove()` | 标记移除（EntityData 在 tick 后自动清理） |
| `isRemove()` | 是否已标记移除 |
| `init(PathNode)` | 初始化位置和历史队列（清空历史，设当前节点） |
| `getHistoryNodes()` | 历史轨迹节点队列（用于拖尾渲染和碰撞检测） |
| `getHistoryNodesSize()` | 历史节点最大数（Servant 默认 16，Projectile 默认 8） |
| `writeBase(buf)` / `readBase(buf)` | 网络同步基础数据（位置+旋转） |
| `writeAdditional(buf)` / `readAdditional(buf)` | 子类附加数据同步（空实现，需重写） |
| `calculateBezierPoint(delta, P...)` | De Casteljau 贝塞尔曲线计算 |
| `getLookAngle()` | 视线方向（基于 yaw/pitch） |
| `getCurrentVelocity()` | 当前速度方向（基于历史位置差值） |
| `getCurrentNormal()` | 当前法线方向（基于旋转矩阵） |
| `getEulerNode(pos, direction, normal)` | 从方向+法线计算 PathNode（含 roll） |
| `onRemove()` | 移除时回调（空实现，可重写做粒子效果等） |
| `dimensionChange()` | 维度切换时回调（Servant 重置位置，Projectile 标记移除） |

### Servant 基类

| 方法/字段 | 说明 |
|-----------|------|
| `registerGoals(goalSelector)` | 注册 AI 目标（空实现，需重写） |
| `searchTarget()` | 搜索目标（基于 TargetCache + 距离 + 可见性） |
| `isTarget(LivingEntity)` | 判断是否为有效目标（Enemy / Targeting指向玩家 / 互相攻击过） |
| `getSearchDistance()` | 搜索距离（抽象方法，必须实现） |
| `getSlotCost()` / `setSlotCost(int)` | 栏位占用（默认 1，多体节仆从的后续体节设为 0） |
| `getDamageSource()` | 构造 ServantDamageSource（DamageType=Servant, causingEntity=owner, 内含 servant 引用） |
| `getOrder()` | 在同 AttachmentEntityType 分组中的未移除顺序 |
| `getSameSize()` | 同 AttachmentEntityType 分组中未移除数量 |
| `getTarget()` / `setTarget(LivingEntity)` | 当前目标 |
| `isTargetChange()` | 目标是否在本 tick 发生变化 |
| `getGoalSelector()` | AI 目标选择器 |
| `tick()` | 服务端：更新目标 → goalSelector.tick() → super.tick() |

### MomentumServant 基类（继承 Servant）

| 方法/字段 | 说明 |
|-----------|------|
| `setDrag(float)` | 空气阻力 [0,1]，1=无阻力。每 tick: `velocity = velocity.add(0, gravity, 0).scale(drag)` |
| `setGravity(float)` | 重力加速度（格/tick²） |
| `setRotationSpeed(float)` | 旋转插值速度（度/tick），0=瞬转 |
| `applyForce(Vec3)` | 施加力（累加到 velocity） |
| `applyForce(Vec3 targetPos, float force)` | 向目标方向施加力 |
| `setVelocity(Vec3)` | 设置速度 |
| `getVelocity()` | 获取当前速度向量 |
| `teleportTo(Vec3)` | 瞬移（4 tick 线性插值路径，velocity 清零） |
| `lookAtPos(Vec3)` | 看向指定位置（设置 desiredYaw/desiredPitch） |
| `lookAtDirection(Vec3)` | 看向指定方向 |
| `setDesiredRotation(yaw, pitch, roll)` | 设置期望朝向（rotationSpeed 控制插值速度） |
| `getWanderPos(lastWanderPos, targetPos, distance, height)` | 徘徊位置计算（2.5% 概率刷新，超出距离刷新） |
| `isExecutingPath()` | 重写：排除 "physics" 标识的路径 |

**物理更新流程（`tickPhysics`）：**
```
velocity = velocity.add(0, gravity, 0).scale(drag)
newPos = getPos().add(velocity)
setPath(new PlannedPath("physics", [PathNode(newPos, desiredYaw, desiredPitch, desiredRoll)]))
```

**朝向更新流程（`tickOrientation`）：**
```
deltaYaw = wrapDegrees(desiredYaw - currentYaw)
desiredYaw = currentYaw + clamp(deltaYaw, -rotationSpeed, rotationSpeed)
// pitch/roll 同理
```

### Projectile 基类

| 方法/字段 | 说明 |
|-----------|------|
| `setDrag(float)` | 空气阻力 [0,1]，默认 1.0（无阻力） |
| `setGravity(float)` | 重力，默认 0 |
| `setMaxSpeed(float)` | 最大速度（格/tick），默认 2.0 |
| `setMaxLife(int)` | 最大存活 ticks，默认 200，0=无限 |
| `setVelocity(Vec3)` | 设置速度（同时更新朝向） |
| `applyForce(Vec3)` | 施加力 |
| `setDamage/setKnockback/setArmorPierce` | 内联复制伤害数据到射弹（原 `copyDamageData` 已移除） |
| `join(Player owner)` | 加入到玩家的 Projectile 数据 |
| `setDamageSource(DamageSource)` | 设置伤害来源 |
| `getSpinSpeed()` | 自旋速度（度/tick），默认 0 |
| `getTrailDuration()` | 拖尾持续时间，默认 15 |
| `getMaxDistance()` | 最大飞行距离，默认 128.0 |
| `getHistoryNodesSize()` | 默认 8 |
| `onRemove()` | 移除回调（空实现，可重写做爆炸粒子等） |

**Projectile 物理更新流程（`tickPhysics`）：**
```
velocity = velocity.scale(drag).add(0, gravity, 0)
if (speed > maxSpeed) velocity = velocity.scale(maxSpeed / speed)
updateRotationFromVelocity()  // 根据速度方向更新 yaw/pitch
newPos = getPos().add(velocity)
setPath([PathNode(newPos, yaw, pitch, roll + getSpinSpeed())])
```

### 碰撞系统

| 接口 | 说明 |
|------|------|
| `ICollideAttack<T>` | 碰撞攻击（对实体），基于历史轨迹的贝塞尔曲线扫掠检测 |
| `IBlockCollision<T>` | 方块碰撞，使用原版 `Entity.collideBoundingBox` 算法 |

**ICollideAttack 实现：**
```java
// 必须实现：
@NotNull AABB getHitbox();           // 碰撞盒（局部坐标，相对于实体中心）
void onCollisionAttack(List<HitContext> hitContexts);  // 碰撞回调

// 可选重写：
default boolean canCollideAttack()   // 是否启用碰撞攻击（默认 true）
default boolean isValidCollisionTarget(T entity, LivingEntity target)  // 目标过滤（默认排除 owner）
default boolean renderHitbox()       // 是否渲染调试碰撞箱（默认 true）
```

碰撞检测算法：使用上一tick、上上tick、当前位置构建二次贝塞尔曲线，沿曲线采样 OBB（有向包围盒），与 TargetCache 中的实体做精确碰撞检测。碰撞结果按距离排序。

**IBlockCollision 实现：**
```java
// 必须实现：
@NotNull AABB getBlockCollisionBox();  // 方块碰撞盒（局部坐标）

// 可选重写：
default boolean canCollideWithBlocks()  // 是否启用碰撞检测（默认 true）
default void onBlockCollision(CollisionContext context)  // 碰撞回调
```

**CollisionContext 字段：**

| 字段 | 说明 |
|------|------|
| `position` | 碰撞修正后位置 |
| `collisionX/Y/Z` | 对应轴是否被碰撞截断 |
| `bottomSupported` | 是否被底部方块支撑（落地） |

**静态工具方法：**
- `IBlockCollision.bounceVelocity(velocity, context, damping, threshold)` — 弹跳
- `IBlockCollision.clearVelocity(velocity, context)` — 清零碰撞轴速度

### 伤害系统

```java
InvincibleData.attack(living)
    .attacker(getUuid())           // UUID（用于 PARTIAL 无敌帧追踪）
    .damageSource(getDamageSource())  // 伤害来源
    .damageAmount(getDamage())     // 伤害值
    .invincibleTime(4)             // 无敌帧 (ticks)
    .global()                      // 可选：切换为 GLOBAL 无敌帧模式
    .effect(new MobEffectInstance(...))  // 可选：附加效果（仅在 hurt 成功时添加）
    .apply();                      // 应用伤害，返回 boolean
```

**无敌帧模式：**

| 模式 | 说明 |
|------|------|
| `PARTIAL`（默认） | 按 UUID 隔离：同一 UUID 在 invincibleTime 内不能再次伤害，不同 UUID 互不影响 |
| `GLOBAL`（调用 `.global()`） | 全局无敌：invincibleTime 内任何攻击都不能伤害目标 |

**ServantDamageSource：** 继承 `DamageSource`，额外持有 `Servant servant` 引用。射弹可通过 `source instanceof ServantDamageSource sds → sds.getServant()` 获取仆从实例。

## 渲染系统详解

### RenderContext 配置

```java
RenderContext.<YourServant>builder()
    .trail(new ConeTrailConfig<YourServant>()     // 拖尾配置
        .timer(10)                                 // entity.trailTimer > 此值时显示拖尾
        .colorRGB(0xFF4422)                        // 颜色
        .historyLength(5)                          // 使用历史节点数
        .maxRadius(0.2f)                           // 最大半径
        .minRadiusRatio(0.75f)                     // 最小半径比例
        .resolution(4)                             // 圆周细分
        .fadeOut(progress -> (1 - progress) * (1 - progress))  // 淡出函数
    )
    .model(new ModelConfig<YourServant>()          // 模型配置
        .scale(0.5f)                               // 缩放
        .translateOffset(-0.5f, -0.25f, -0.5f)    // 平移
        .rotationOffset(180, 0, 0)                 // yaw/pitch/roll 旋转偏移（度）
        .alphaDistanceFactor(1.0f)                 // 第一人称透明度距离因子
    )
    .build();
```

### 拖尾类型

| 类 | 说明 |
|------|------|
| `ConeTrailConfig` | 锥体拖尾（如 DeadlySphere） |
| `RibbonTrailConfig` | 带状拖尾 |
| `DropletTrailConfig` | 水滴拖尾 |
| `TrailConfig` | 基类（自定义继承） |

### 已有渲染器示例

| 渲染器 | 渲染方式 |
|--------|---------|
| `DeadlySphereRenderer` | 纯代码 ConeTrail（无实体模型） |
| `LaserProjectileRenderer` | 使用 `LaserRenderer` 工具类渲染激光 |
| `TerraprismRenderer` | 使用 GeoAttachmentModel（GeckoLib） |
| `StardustCellRenderer` | 使用 GeoAttachmentModel |
| `BlitzBallRenderer` | 使用 SphereRenderer 渲染球体 |
| `MiniStardustProjectileRenderer` | 使用 SphereRenderer |

### 第一人称透明度

`AbstractAttachmentEntityRenderer` 自动处理——当玩家第一人称视角时，距眼睛 0.5×alphaDistanceFactor 以内完全透明，4.0×alphaDistanceFactor 以外完全可见，中间线性插值（最低 0.102）。

### 方式 B：直接实现 IAttachmentEntityRenderer

```java
public class YourServantRenderer implements IAttachmentEntityRenderer<YourServant> {
    @Override
    public void render(YourServant entity, PoseStack poseStack, MultiBufferSource bufferSource,
                       float partialTick, int packedLight, PathNode renderNode) {
        // 完全自定义渲染
    }
}
```

> `IAttachmentEntityRenderer` 是 `@FunctionalInterface`，甚至可以用 lambda。

## 额外工具

| 工具 | 说明 |
|------|------|
| `ParticleHelper` | 粒子系统辅助（`ParticleHelper.create(level).generic(builder).pos().velocity().emit()`） |
| `GenericParticleBuilder` | 通用粒子构建器（centerColor/edgeColor/lifetime/spin/friction/scale 等） |
| `DynamicLightDispatcher` | 动态光照调度 |
| `SphereRenderer` | 球体渲染工具 |
| `LaserRenderer` | 激光渲染工具 |
| `LightningRenderer` | 闪电渲染工具 |
| `GeoSideloader` / `GeoAttachmentModel` | GeckoLib 动画模型加载 |

## 完整文件清单（新建召唤武器所需）

```
src/main/java/first/servantry/
├── api/
│   ├── entity/AttachmentEntity.java                     [基类 — 无需修改]
│   ├── entity/AttachmentEntityType.java                 [record — 无需修改]
│   ├── entity/ICollideAttack.java                       [接口 — 无需修改]
│   ├── entity/IBlockCollision.java                      [接口 — 无需修改]
│   ├── projectile/Projectile.java                       [基类 — 无需修改]
│   ├── projectile/AttachingProjectile.java              [基类 — 无需修改]
│   ├── servant/Servant.java                             [基类 — 无需修改]
│   ├── servant/MomentumServant.java                     [基类 — 无需修改]
│   ├── servant/ai/ServantGoal.java                      [基类 — 无需修改]
│   ├── servant/ai/ServantGoalSelector.java              [选择器 — 无需修改]
│   ├── builder/ServantWeaponItemBuilder.java            [构建器 — 无需修改]
│   ├── item/IServantWeaponItem.java                     [接口 — 无需修改]
│   ├── client/render/AbstractAttachmentEntityRenderer.java [渲染基类 — 无需修改]
│   └── client/render/AttachmentEntityRenderDispatcher.java [调度器 — 无需修改]
├── common/
│   ├── servant/YourServant.java                         [新建]
│   ├── servant/goal/yourservant/
│   │   ├── YourServantAttackGoal.java                   [新建]
│   │   └── YourServantIdleGoal.java                     [新建]
│   └── projectile/YourProjectile.java                   [新建（可选）]
├── register/
│   ├── ServantryAttachmentEntityRegister.java           [修改：注册实体类型]
│   └── ServantryServantWeaponRegister.java              [修改：注册武器物品]
└── client/
    ├── attachmentEntityRenderer/servant/
    │   └── YourServantRenderer.java                     [新建]
    └── ClientEvent.java                                 [修改：注册渲染器]
```
