# 外星法杖 (Xeno Staff) 开发计划

## 原版数据映射

| 属性 | Terraria 原值 | 转换规则 | Servantry 值 |
|------|-------------|---------|-------------|
| 伤害 | 36 | ÷10 | 3.6f |
| 击退 | 2 | ÷10 | 0.2f |
| 使用时间 | 36 | 不映射 | 统一4tick冷却 |
| 射弹速度 | 10 | 不适用 | 激光瞬间命中 |
| 稀有度 | Yellow (8) | — | EPIC |
| 搜索距离 | 125图格 | 项目惯例 | 32格 |
| 攻击冷却 | 0.4秒=24嘀嗒 | ceil(24/3) | 8 tick |
| 传送距离 | 18.75~50图格 | ÷3.2近似 | 6~16格 |
| 无敌帧 | — | — | 4 tick (PARTIAL) |

## 行为设计

### UFO 仆从
- **基类**: MomentumServant（无 ICollideAttack，纯远程攻击）
- **朝向**: 无朝向自旋 — `setDesiredRotation(currentPathNode.yaw()+2, currentPathNode.pitch()+2, currentPathNode.roll()+2)` 缓慢自旋
- **物理**: drag=0.75, gravity=0, rotationSpeed=10
- **待机**: 在玩家周围徘徊悬浮（MomentumServantIdleGoal）
- **攻击**: 悬浮在目标正上方约3格处，每8tick发射一次激光（直接InvincibleData.attack + 激光粒子渲染）
- **瞬移**: 目标在6~16格范围时瞬移到目标上方（teleportTo）
- **返回**: 距玩家超过48格时teleportTo回玩家身边

### 激光攻击
- **实现方式**: 不创建Projectile实体，直接在AttackGoal中对目标造成伤害
- **伤害**: InvincibleData.attack()，invincibleTime=4，PARTIAL模式
- **视觉**: 使用LaserRendererHelper在UFO和目标之间渲染蓝色激光线，持续约3tick
- **粒子**: 发射时在UFO位置产生蓝色粒子效果

## 新建/修改文件清单

### 新建文件
1. `common/servant/UFO.java` — UFO仆从实体类
2. `common/servant/goal/ufo/UFOAttackGoal.java` — UFO攻击AI
3. `common/servant/goal/ufo/UFOIdleGoal.java` — UFO待机AI（复用MomentumServantIdleGoal，不需要单独文件）
4. `client/attachmentEntityRenderer/servant/UFORenderer.java` — UFO渲染器

### 修改文件
1. `register/ServantryAttachmentEntityRegister.java` — 注册UFO实体类型
2. `register/ServantryServantWeaponRegister.java` — 注册外星法杖武器
3. `client/ClientEvent.java` — 注册UFO渲染器

## 分步实现

### Step 1: UFO.java
- MomentumServant子类，实现IBlockCollision
- 无朝向自旋：重写tick()中设置setDesiredRotation自旋
- 无俯仰角：重写lookAtDirection只设置yaw
- shootLaserAt(target)方法：直接InvincibleData.attack + 记录激光目标位置供渲染
- getLaserCooldown() = 8

### Step 2: UFOAttackGoal.java
- canUse: 有目标时
- tick: 悬浮在目标上方3格，冷却到0时调用servant.shootLaserAt()
- 瞬移逻辑：目标在6~16格时teleportTo目标上方

### Step 3: 注册
- AttachmentEntityRegister: register("ufo", UFO::new)
- ServantWeaponRegister: damage=3.6f, knockback=0.2f, rarity=EPIC

### Step 4: UFORenderer
- 占位模型(TEST) + ConeTrail拖尾
- 激光渲染：检查servant的laserTarget，用LaserRendererHelper渲染蓝色激光

### Step 5: ClientEvent注册渲染器
