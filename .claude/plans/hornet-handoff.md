# 交接文档：黄蜂召唤物移植（进行中）

## 任务背景

将 Terraria **黄蜂法杖 (Hornet Staff)** + **蜂巢背包 (Hive Pack)** 移植到 Servantry 模组。使用 `terraria-port` Skill（`.claude/skills/terraria-port.md`）。

## 已确认决策

### 黄蜂法杖 (Hornet Staff)
- **基类**：MomentumServant + IBlockCollision（黄蜂无 ICollideAttack，靠毒刺射弹造成伤害）
- **伤害**：12 ÷10 = 1.2f
- **击退**：2 ÷10 = 0.2f
- **稀有度**：Orange(3) → RARE
- **攻击行为**：在目标上空，抵达一定水平距离时悬停（getWanderPos），不主动贴近目标；无论是否抵达悬停位置，只要毒刺冷却完成就发射
- **开火冷却**：Terraria 0.75秒=45tick → ÷3 向上取整 = **15 MC tick**；装备蜂巢背包 → 35tick → **12 MC tick**
- **搜索距离**：32（项目惯例，Terraria 125图格）
- **返回距离**：48格瞬移返回

### 毒刺射弹 (HornetStinger)
- **基类**：Projectile + ICollideAttack + IBlockCollision
- **不可穿透**，命中即 setRemove()
- **中毒**：100% 施加原版 MobEffects.POISON，4-7秒 = 80-140 tick
- **无敌帧**：Terraria 10帧 ÷4 向上取整 = **3**
- `onCollisionAttack` 用 `hitContexts.getFirst()` 单目标命中

### 蜂巢背包 (Hive Pack)
- **类型**：Curios 配饰
- **稀有度**：Expert → EPIC
- **效果**：只强化黄蜂法杖开火频率（非召唤武器逻辑不做——蜜蜂枪/养蜂人等不实现）
- **实现方式**：Hornet.getStingerCooldown() 中用 `CuriosUtil.isEquipped(owner, ServantryCurioRegister.HivePack.get())` 检查

## 关键 API 行为（重要）

- **`setDamageSource(ServantDamageSource)`** 已内联复制 damage/knockback/armorPierce（从 ServantDamageSource.getServant() 提取）。**射弹创建后无需手动 setDamage/setKnockback/setArmorPierce**。`copyDamageData` 已从基类移除。
- **`getWanderPos(lastWanderPos, targetPos, distance, height)`**：2.5%概率刷新、超出distance刷新、height保证最低高度。Idle/Attack 悬停都用它。

## 已创建文件

```
src/main/java/first/servantry/
├── common/
│   ├── servant/Hornet.java                          [已创建]
│   ├── servant/goal/hornet/HornetAttackGoal.java    [已创建, 用getWanderPos]
│   ├── servant/goal/hornet/HornetIdleGoal.java      [已创建, 用getWanderPos]
│   └── projectile/HornetStinger.java               [已创建, 单目标+中毒]
├── client/
│   ├── attachmentEntityRenderer/servant/HornetRenderer.java         [已创建, 占位stardust_cell]
│   └── attachmentEntityRenderer/projectile/HornetStingerRenderer.java [已创建, 占位]
```

## 已修改文件

- `register/ServantryAttachmentEntityRegister.java` — +Hornet(仆从), +HornetStinger(射弹) 注册
- `register/ServantryServantWeaponRegister.java` — +HornetStaff 武器（damage1.2f, knockback0.2f, RARE, 无 itemLanguageTooltip）
- `register/ServantryCurioRegister.java` — +HivePack 配饰（EPIC）
- `register/ServantryModelRegister.java` — +HORNET, +HORNET_STINGER 模型占位
- `client/ClientEvent.java` — +Hornet/HornetStinger 渲染器注册

## 编译状态

`gradlew compileJava` 通过（已验证）。所有文件编译无误。

## 用户最后指令（待执行）

> "压缩上下文...替换到新对话中完成接下来的任务"

**之前未完成的任务**：用户要求"移除 agent.md，合并两个文档内容"。我已开始将 agent.md 的架构总览/项目信息合并进 skill，但**未完成全部合并**，且**未删除 agent.md**。

## 待完成任务（按优先级）

1. **完成 skill 合并**：将 agent.md 剩余内容（开发步骤详解、核心 API 参考表、渲染系统、额外工具、完整文件清单）合并进 `.claude/skills/terraria-port.md`
2. **删除 agent.md**：合并完成后删除 `D:\IDEA\Servantry\agent.md`
3. 验证 skill 完整性

## Skill 协定要点（已写入 `.claude/skills/terraria-port.md`）

### 数值转换规则
- 伤害/击退：÷10
- 无敌帧：÷4 向上取整
- 冷却/持续时间：÷3 向上取整（Terraria 60tick/s → MC 20tick/s）
- 搜索距离：项目惯例 32 格
- 稀有度：White→COMMON, Blue→UNCOMMON, Green/LightRed→RARE, Cyan/LightPurple→EPIC

### 复用模式（4种）
1. getWanderPos 复用（盘旋/悬停/待机）
2. 碰撞攻击模式（单目标 vs 多目标）
3. 射弹发射模式（setDamageSource 自动复制伤害数据）
4. Curio 配饰强化仆从模式

### 工作流程（4 Phase）
1. 解析 Wiki
2. 确认决策（必问5项，含 AI 行为逐条确认）
3. 生成开发计划 → `.claude/plans/<name>-plan.md`
4. 实现（9步，含 AI 复查）

### 代码模板（6个）
MomentumServant / AttackGoal / IdleGoal / Projectile / 武器注册 / 渲染器

### 关键注意事项（15条，核心几条）
- 无参构造器必须存在（网络反序列化）
- init() 在 add() 之前
- 射弹用 join() 不是 helper.add()
- 碰撞盒是局部坐标
- 不要添加 itemLanguageTooltip（架构 IServantWeaponItem.getTooltips() 自动生成）
- AI 逻辑必须反复确认
- 非召唤物逻辑不做

## 持久化记忆（已写入）

`C:\Users\l1518\.claude\projects\D--IDEA-Servantry\memory\`:
- `terraria-value-scaling.md` — 伤害/击退÷10，无敌帧÷4向上取整，搜索距离32格
- `terraria-tick-conversion.md` — 时间÷3向上取整
- `neoforge-1-21-1-eventbus.md` — @EventBusSubscriber 不需指定 bus
- `MEMORY.md` — 索引

## 项目路径

- 工作目录：`D:\IDEA\Servantry`
- Skill：`.claude/skills/terraria-port.md`
- agent.md（待删除）：`D:\IDEA\Servantry\agent.md`
- 计划文件：`.claude\plans\finch-staff-plan.md`（雀杖，已完成实现）

## 下一步行动

继续执行未完成的 skill 合并任务：将 agent.md 剩余内容（核心 API 参考、渲染系统、额外工具等）合并进 `.claude/skills/terraria-port.md`，然后删除 agent.md。
