# 末世星史莱姆的三维 AI（MC 复刻指导）

依据 `CatalystMod v1.1.5` 的反编译源码 `src\CatalystMod.NPCs.Boss.Astrageldon\Astrageldon.cs`（3340 行）与同目录 14 个配套类，把「末世星史莱姆 = Astrageldon」的整套战斗行为换算到 NeoForge 1.21.1 / Summoner 工程里的一比一三维复刻。

**比例尺按你定的：泰拉瑞亚 2 格 = 我的世界 1 格。** 也就是 1 MC 格 = 32 泰拉像素。

同类文档参照 [terraprism-ai.md](terraprism-ai.md) 的粒度；本文更长，因为 Boss 有 16 个攻击状态而不是 2 个。

---

## 一、时间与比例尺

| 量 | 泰拉侧 | 换算 | MC 侧 |
|---|---|---|---|
| 长度 | 1 tile = 16 px | ÷ 32 | 1 格 |
| 距离 | `d` px | `d / 32` | 格 |
| 速度 | `v` px/刻（60 刻/秒） | `v × 1.875` | 格/秒 |
| 速度（按 MC tick） | `v` px/刻 | `v × 0.09375` = `3v/32` | 格/MC tick |
| 加速度 | `a` px/刻² | `a × 112.5` | 格/秒² |
| 计时器 | `t` 刻 | `t / 3` | MC tick（显示用） |

**建议不要做计时器除法。** 内部保持泰拉单位与 60 刻/秒：每个 MC tick 跑 **3 个子步**，所有 `attackTimer`、`slamTimer`、速度、重力、碰撞都按源码原值跑；只有「读给 MC 用」的时候才 ÷32。这样 50 刻的砸地预警仍然是 50 个泰拉帧（16.67 MC tick），时长与源码逐帧一致，也不会因为四舍五入把 0.93 阻尼、1.04 加速这类逐帧系数改坏。

渲染帧率 20 Hz 而物理解算 60 Hz，所以实体位置要保留最近 3 个子步的位置做插值（与 `terraprism-ai.md` 里「一个逻辑终点 + 5 个渲染关键帧」的思路同源，这里只需要 3 个）。

### 常用换算速查

| 泰拉 | MC |
|---|---|
| 174 × 118 px（本体碰撞箱） | 5.4375 × 3.6875 格 |
| 170 × 170 px（星云形态） | 5.3125 × 5.3125 格 |
| 72 px（星云形态接触半径） | 2.25 格 |
| 1000 px（二阶段初始竞技场半径） | 31.25 格 |
| 600 px（收缩后半径） | 18.75 格 |
| 1200 px（出场高度 / 升空高度） | 37.5 格 |
| 140 px（环绕星轨道半径） | 4.375 格 |
| 40 → 720 px（岩石环绕半径） | 1.25 → 22.5 格 |
| 1440 px（StarLaser 长度） | 45 格 |
| 1296 px（SpiralLaser 长度） | 40.5 格 |
| 15.5 px/刻（登场地冲刺） | 29.1 格/秒 |
| 0.5 px/刻²（默认重力） | 56.25 格/秒² |

注意 MC 原版重力约 0.08 格/tick² ≈ 32 格/秒²，只有泰拉侧 0.5 px/刻² 的 57%——所以**不要用原版物理**，自己按 `velocity.Y += gravity` 逐子步积分（源码里 `if (velocity.Y < gravity * 20) velocity.Y += gravity`）。

---

## 二、本体数值与生命流程

### 2.1 基础数值（`SetDefaults`，386-409）

| 项 | 值 | 说明 |
|---|---|---|
| 碰撞箱 | 174 × 118 px | 5.4375 × 3.6875 格；星云形态切 170 × 170 |
| `lifeMax` | 105000 | 见下方难度换算 |
| 接触伤害 | 160 | 引擎按难度再乘 |
| 防御 | 40 | |
| 击退抗性 | 0（完全免疫） | MC 侧 `KNOCKBACK_RESISTANCE = 1.0` |
| 重力 / 穿墙 | `noGravity = true`、`noTileCollide = true` | 但一阶段会自己贴地（见 4.1） |
| 免疫 | Debuff 137、`GlacialState`、`TemporalSadness`、`MiracleBlight` | MC 侧 `addEffect` 返回 false，并在 `tick` 里 `removeAllEffects()`（工程当前版本已有） |
| 动画 | 7 帧，每帧 3 刻 | 21 刻/循环 = 7 MC tick |
| 拖尾 | 12 段 `oldPos` | |

### 2.2 难度换算（`ApplyDifficultyAndPlayerScaling`，411-429）

```text
damage  = damage * 220/160/2           // 经典 110，专家 220（×2 由引擎给）
lifeMax = balance * lifeMax * 156000/105000   // 专家
lifeMax = balance * lifeMax * 200000/105000   // 复仇（另 damage ×1.12）
damage *= 1.12                          // 死亡模式
lifeMax /= 2                            // 抵消引擎的专家 ×2
```

用类里自带的常量交叉校验过，**最终战斗血量**：

| 难度 | 一阶段 | 二阶段（`SecondPhaseHealthIncrease`，3300-3319） |
|---|---|---|
| 经典 | 78000（同一公式推算） | 145000 |
| 专家 | **156000** | 217500 |
| 复仇 | 200000 | 336000（+ 计时减伤，见 6.5） |

专家多人：二阶段 `lifeMax += lifeMax/2 × (玩家数-1)`。经典模式数值是同一公式的自然结果（作者以专家/复仇为基准调参），复刻时建议**以专家为准**，经典做一个 0.5 倍系数。

### 2.3 生命流程（三段落）

```
第一阶段 156000 ──血量归零──> 过场(7 秒) ──> 第二阶段 217500 ──血量归零──> 绝望期(生命锁 1) ──> 自爆收场
```

1. **一阶段血量归零**：`PreKill`（3234-3243）直接拦下击杀（只播「击杀失败」公告），`HitEffect`（3265-3298）里把 `secondPhaseTransition = true`、`lifeMax` 换成二阶段值、`life = 1`、`dontTakeDamage = true`。
2. **过场**（`attackPhase == 5`，1671-1727，总长 420 刻 = 7 秒）：
   - 0-240 刻：血条被强制「回充」`life = min(lifeMax/180 × attackTimer, lifeMax)`，速度每帧 `×= 1 - 0.65×(t/240)`，同时 `velocity.Y -= 0.5`（缓慢升空）；
   - 240 刻：播 `AstrageldonPhase2Charge`；
   - 240-360 刻：`velocity *= 0.3`，位置做 `sin(12°)` 抖动；
   - 360 刻：震屏 30、播 `AstrageldonPhase2Begin`、8 颗 `AstraRock` 向玩家方向炸出；
   - 420 刻：`SwapPhase(6)`，`life = lifeMax`。
3. **二阶段血量归零**：`HitEffect` 里 `desperation = true`、`life = 1`、`dontTakeDamage = true` → 进入 `attackPhase 15` 绝望期（见第六章）。

### 2.4 接触判定与减伤

- 常规形态用碰撞箱接触。
- **星云形态**（`NebulaForm = true`，二阶段大量使用）改成半径判定：以本体中心为圆心 **72 px（2.25 格）** 的球，取玩家碰撞箱最近点距离比较（`CanHitPlayer`，356-371）。MC 侧对应 `distance(closestPointOnAABB) < 2.25`。
- 减伤（`ModifyHitByProjectile`，334-349）：近战一律 ×0.8；`attackPhase == 11 || 14` 时魔法 ×0.55、近战 ×0.9、其他 ×0.75、潜行 ×0.65（`ModifyHitByItem` 也是 ×0.8）。
- 命中玩家附加 `ArmorCrunch` 240 刻（`OnHitPlayer`，3224）。

---

## 三、状态机总览

### 3.1 16 个攻击 ID ↔ 工程里已有的 `Phase` 枚举

`Astrageldon.AttackID`（45-79）与 `common\entity\Astrageldon.java` 的 `Phase` 是一一对应的，只差一个 `Phase26`（源码本来就没有 6 这个二阶段 ID 的空位）：

| AttackID | 名称 | 阶段 | 工程枚举 |
|---|---|---|---|
| -1 | Idle / 登场 | P1 | `Phase11` |
| 0 | JumpSlam | P1 | `Phase12` |
| 1 | ScatterStars | P1 | `Phase13` |
| 2 | SlimeRain | P1 | `Phase14` |
| 3 | RevengeanceJumpSlam | P1（复仇限定） | `Phase15` |
| 4 | OrbitingRocks | P1 | `Phase16` |
| 5 | Phase2Transition | 过场 | `Phase17` |
| 6 | SummonSlimersAndAscend | P2 | `Phase21` |
| 7 | AscendWithDashes | P2 | `Phase22` |
| 8 | Phase2StarSlam | P2 | `Phase23` |
| 9 | CrossStarDash | P2 | `Phase24` |
| 10 | ShrinkArenaAndStarShotgun | P2 | `Phase25` |
| 11 | SpiralLaser | P2 | `Phase27` |
| 12 | SlimerSlam | P2 | `Phase28` |
| 13 | DashAndMoveArenaHorizontally | P2 | `Phase29` |
| 14 | RapidlyFireStarsThenShotgunNebulaScatterShots | P2 | `Phase210` |
| 15 | DesperationPhase | 绝望 | `Phase211` |

### 3.2 状态转移图

```
                 ┌──────────────── 一阶段循环 ────────────────┐
  Phase11(登场) → Phase12(3 次跳砸) → Phase13(散射星) → Phase14(史莱姆雨)
       ↑                                        │
       └──────── Phase16(环绕岩石) ← Phase12(3 次跳砸) ←┘
                        （复仇难度：Phase14 → Phase15 → Phase16）

  血量归零 → Phase17(过场 7 秒) → Phase21 → Phase22 → Phase23 → Phase24 → Phase25 → Phase27
                                    ↑                                            │
                                    └──────── totalPhase2Cycles++ ←──────────────┤
                                                                                 ↓
                                                        Phase28 → Phase29 → Phase210 → …
  二阶段血量归零 → Phase211(绝望，直到自爆)
```

细节：
- `Phase13` 结束时按 `attackTimer3` 决定去向：`SwapPhase(0)` 且把 `attackTimer3` 置 -1 → 走 `Phase12` 后进入 `Phase16`；否则回 `Phase12` 再进 `Phase13`。
- `Phase16` 结束用 `SwapPhase(-1)` 重置回登场态（但不会重播登场演出）。
- 一阶段每招都从 `SwapPhase(x)` 给的负计时器开始（见 3.3），负值阶段是「站地蓄力 / 前摇」。

### 3.3 `SwapPhase` 初始计时器表（2892-2971）

| 目标状态 | `attackTimer` 初值 | 换算 |
|---|---|---|
| -1 / 登场 | -60 | -1.0 s |
| 0 JumpSlam | -40 | -0.67 s |
| 1 ScatterStars | -70 | -1.17 s |
| 2 SlimeRain | -90 | -1.5 s |
| 3 RevJumpSlam | -60 | -1.0 s |
| 4 OrbitingRocks | -70 | -1.17 s |
| 5 过场 | 0 | — |
| 6 / 7 | -120 | -2 s |
| 8 | -60 | -1 s |
| 9 | -110 | -1.83 s |
| 10 | -60（另 `attackTimer2 = attackTimer3 = 90`） | — |
| 11 | 0 | — |
| 12 | -50 | -0.83 s |
| 13 / 14 | -120 | -2 s |

**除了 6**，所有初值都会再乘 `SpeedUp()`：血量越低前摇越短。

### 3.4 `SpeedUp()`（2869-2890）

```text
SpeedUp = clamp(life / lifeMax, min, 1)
  经典/专家 min = 0.4；复仇 0.25；死亡 0.1；getGoodWorld(传奇种子) 血量比例再 ×0.2
二阶段恒为 1（不再加速）
```

它在两处起作用：招式前摇（`SwapPhase` 乘它）和跳砸间隔（`attackTimer = 14 × (1 - SpeedUp())`）。

### 3.5 每帧全局处理（AI 开头与结尾）

| 处理 | 数值 | 行号 |
|---|---|---|
| 水平阻尼 | `velocity.X *= 0.93`，`abs(vx) < 0.1` 归零 | 929-933 |
| 朝向 | 本体在玩家左侧时 `direction = -1`，右侧 `= 1` | 934-945 |
| 重力 | `if (!NebulaForm && velocity.Y < gravity*20) velocity.Y += gravity` | 2638-2643 |
| 普通形态旋转 | `rotation = velocity.X × -0.01` | 2644 |
| 星云形态旋转 | `abs(vx) < 0.5` → `rotation += 12° × direction`，否则 `rotation += velocity.X × 0.05` | 2669-2676 |
| 落地挤压 | `landingRecoil` 由落地速度 ×0.02 起算，逐刻 `×0.965 - 0.01`，上限 0.36 | 886-894 / 2735 |
| 离场 | 玩家死亡或距离 > 6400 px（200 格）累计 360 刻 → 消失 | 452-470 |
| 环境压制 | 停雨/停史莱姆雨/停沙尘暴/停派对 | 811-830 |
| 光照 | `AddLight(Center, (131,62,196)/255)` | 2744 |

---

## 四、一阶段逐招（源码 923-1647）

### 4.1 AttackID -1：登场 / 落地循环（1132-1172）

- 入场：`scale = 1.25`、播 `DoSlamAttack(无音效)`、`attackTimer = -120`。
- 站在地面时 `attackTimer` 每帧 +1；当 `|attackTimer| % 6 ≤ 2` 且在负区间时做挤压动画（`addedStretch += 0.08`）。
- **`attackTimer == 20` 时起跳**（从 -120 数到 20，即落地后在地面待机 140 帧 ≈ 2.33 秒）：
  ```text
  Speed = 15.5
  rot   = atan2(NPC.Center.Y - 玩家中心.Y, NPC.Center.X - 玩家中心.X)
  velocity.X = -cos(rot) × 15.5 × 1.25
  velocity.Y = -sin(rot) × 15.5 × 0.75 - 14
  ```
- 上升期 `tileCollide = false`；水平每帧 `×1.04` 持续加速。
- 落地：`attackTimer2++`，`attackTimer = -67 × SpeedUp()`（重新蓄力）。
- `attackTimer2 ≥ 2` → `SwapPhase(0)`。
- **落地当帧**（`slamTimer == 0` 且 `velocity.Y == 0`）触发地刺：**40 根** `AstraBlade`（专家 +10、死亡 +5），并震屏。

### 4.2 AttackID 0：三连跳砸 JumpSlam（1173-1227）

每轮：
- 地面蓄力到 `attackTimer == 16` 起跳：
  ```text
  Speed = 14.5 + attackTimer2 × 4      // 第 1/2/3 次：14.5 / 18.5 / 22.5
  velocity.X = -cos(rot) × Speed × 1.25
  velocity.Y = -sin(rot) × Speed × 0.75 - 9 - attackTimer2 × 3
  ```
- 飞行中 `velocity.X *= 1.05`；`attackTimer == 17` 时 `DoSlamAttack()`（起 50 刻砸地预警）。
- 落地：`attackTimer2++`，`attackTimer = 14 × (1 - SpeedUp())`（满血 16 帧的短蓄力）；继续 `velocity.Y *= 0.985`、`gravity -= 0.5` 直到 70 帧。
- 三次后：`attackTimer3 == -1` → `SwapPhase(4)`；否则 `SwapPhase(1)`。
- 每次落地：**地刺 10 根**（+5 复仇 +5 死亡）+ `attackTimer2 × 10` 根，即 10 / 20 / 30 根。

> `DoSlamAttack` 只是把 `slamTimer` 置为 50（二阶段 35）并播音效；`slamTimer > 0` 每帧 -1，归零那帧若已着地就出落地效果，没着地则 `gravity += 2.5` 加速下坠（1215-1226 的 1125-1128 分支）。

### 4.3 AttackID 1：散射星 ScatterStars（1228-1347）

1. 前摇 70 帧后跳向 **玩家正上方 360 px（11.25 格）**：`Speed = 16.5 + attackTimer2 × 4`，X ×1.25、Y ×0.75。
2. 进入悬停：绕目标点做半径 240 px 的椭圆摆动（`circular = (0,240).RotatedBy(attackTimer3 × 3°)`，Y 压到 0.1 再叠 `sin(4°×t) × 20 + 8`），并持续喷尘（`DustExplosion(1, flyingMult)`）。
3. **每 100 帧放一次星**：`SummonStars(6 或 8 + 复仇 2 + 死亡 2 + (4×(1-SpeedUp)+0.5))`；第 100×k + 70 帧播 `Item105`（音高 -0.45）。
4. 第 40-100 帧之间速度被 `×0.9` 并随正弦抽帧（形成"悬停抖动"）。
5. 4 轮（`attackTimer2 ≥ 4`）后目标点再抬高 280 px（8.75 格），`DoSlamAttack()` 俯冲；`attackTimer2 ≥ 54` 且着地 → `SwapPhase(2)`。

### 4.4 AttackID 2：史莱姆雨 SlimeRain（1348-1463）

1. **三次垂直跳**（`attackTimer == 9` 触发，每轮落地后 `attackTimer` 重置为 8）：
   ```text
   第 1 次 velocity.Y += -5
   第 2 次 velocity.Y += -12.5
   第 3 次 velocity.Y += -20
   ```
   每次落地出地刺 15 根（+5 复仇、死亡 +10）+ `attackTimer2 × 10` 根。
2. 第三次落地后升空：`attackTimer > 60` 起以 `speed 32`、`veloScale = 0.25 + 0.75×flyingMult` 追到 **玩家上方 1200 px（37.5 格）**，期间 `dontTakeDamage = true`（1381）。
3. `attackTimer == 240` → `DoSlamAttack()`；重力回正 `gravity += 0.25`；着地 → 复仇走 `SwapPhase(3)`，否则 `SwapPhase(0)` 并把 `attackTimer3 = -1`（让 Phase12 结束后转 Phase16）。
4. **着地那帧**（`slamTimer == 0`，1015-1049）：地刺 15 根 + 向上喷 **NovaSlime**：`amt = 5`（专家 +1、死亡 +1）+ `attackTimer2/2`，横向排布 `175 × i × 交替方向`，x 偏移 `rand(-25,26)`，从玩家上方 700~840 px 落下。

### 4.5 AttackID 3：复仇跳砸 RevengeanceJumpSlam（1468-1551）

- 开场（`attackTimer == 0`）先 `SummonStars(8 + 死亡 2 + 4×(1-SpeedUp))`。
- 前摇 20 帧后起跳：`Speed = 14.5 + attackTimer2 × 5`，X ×1.125、Y ×0.5，再 `-10 - attackTimer2×2`。
- 落地：`attackTimer2++`，`attackTimer = 16 × (1-SpeedUp())`；满血条件下落地即再放 8 颗环绕星（1507-1516）。
- `attackTimer == 31` → `DoSlamAttack()`；90 帧内 `gravity -= 0.45`，>40 帧后整体 `velocity *= 0.9775`。
- 三次后 `SwapPhase(4)`。

### 4.6 AttackID 4：环绕岩石 OrbitingRocks（1552-1645）

1. 前摇 70 帧后飞向玩家上方 360 px，进入环绕（`dist = 380 + 60×sin(2.5°×t)`，即绕玩家 380±60 px 公转）。
2. **`attackTimer == 60`**：`SummonRocks(20)` —— 40 颗 `AstraRock` 以 360°/20 均布开始公转（半径 40 → 720 px、角速度 `secondCounter × 2°`），并播 `AstrageldonSpinRocksStart`。
3. 环绕持续到 `attackTimer == 540`（9 秒）→ 播 `AstrageldonSpinRocksEnd`；随后 `DoSlamAttack()`，着地 → `SwapPhase(-1)`。
4. 期间每帧 `speed5 += 距离 × 0.005` 让环绕半径缓慢外扩。

### 4.7 一阶段的"临场追加"

- 每次落地后（非 Phase15/非二阶段）按血量阈值补落石：血量低于 `lifeMax × 30%`（专家 40%、复仇 50%、死亡 75%、传奇种子 90%）→ `GroundRocks(1 + 专家 1)`（1095-1123）。
- 非战斗状态下的 `DisableAnnoyingEvents` 会把天气全部关掉，复刻时同样要在开战时清天气。

---

## 五、二阶段逐招（1648-2637）

二阶段的主循环变量是 **`totalPhase2Cycles`**（从 0 开始，每完成一轮 8/14 或 10 组合就 +1），它同时影响：招式内的弹速、数量、减伤、以及 `Phase27`/`Phase210` 的循环次数。上限通常 2-3（`getGoodWorld` 强制 ≥2）。

### 5.1 竞技场：星云结界（`SummonBorder`，3047-3054 + `NebulaSphere`）

- 二阶段开始（`attackPhase == 6` 里 `secondPhase = true` 时）生成一个 `NebulaSphere`：中心 = `borderCenter`，半径 = `targetOrbitalRange`。
- 本体每帧把 `borderOrbitalRange` 向 `targetOrbitalRange` 插值（`lerp 0.08`）；`borderCenter` 默认跟随本体，但会随招式被推走（`Phase25` 每帧 `+= direction × 3 + (0,-1.2)`）。
- 半径变化：`Phase21` 起 1000 px（31.25 格）→ `Phase25` 插值到 **600 px**（18.75 格）→ `Phase24` 期间每帧 `-1` → 绝望期 `Phase211` 从 1000 一路收到 **-136**（穿零）完成收束。
- **越界处理**（2680-2720）：非星云形态时若 `|Center - borderCenter| > targetOrbitalRange + 40`，把本体拉回并把外向速度分量抵消；星云形态在 `> targetOrbitalRange + 400` 时拉回；距离边缘 200 px 内 `dontTakeDamage = true`。
- 结界本身：内圈排斥半径 `distance + 200`，环带判定 `[distance + 156 + 64×(1-scaleMult), distance + 2250 - 156]`，16×2 段每 tick 转 0.33°（`NebulaSphere.cs`）。**复刻时只需要「半径 + 推力 + 视觉环带宽」，不要照搬 2250 px 的视觉厚度当判定。**

### 5.2 AttackID 6：召唤裂星史莱姆并升空（1817-1878）

```text
attackTimer = -120 → 每帧 +1
gravity = (attackTimer > 0) ? -0.1 : 0.3      // 负重力 = 升空
attackTimer == -30：播 Item62、DustExplosion(2)、SummonSlimer(4 / 专家 6)、velocity.Y -= 10
attackTimer > 0   ：secondPhase = true、SummonBorder()
attackTimer 50-120：向玩家上方 1600 px 飞行（speed 30）
attackTimer == 120：SwapPhase(7)
```

`SummonSlimer(amt)`（2991-3005）：`i = -amt..amt` 跳过 0，按 `360 × i / amt` 角度均布，即 **2×amt 只** `NovaSlimer`。

### 5.3 AttackID 7：带冲刺的升空（1879-1921）

- 每帧 `velocity *= 0.9`，`gravity = 0`，`attackTimer++`。
- `attackTimer > 0` 且 `attackTimer2 < 16`、`attackTimer % 15 == 0`：**每 15 帧加一条冲刺**（共 16 组，左右交替）：
  ```text
  difference = attackTimer2 % 2 × 2 - 1           // ±1
  vector = borderCenter + (0, targetOrbitalRange - 160×(attackTimer2+1) + attackTimer3)
  start  = vector + (targetOrbitalRange + 160, 80×difference)
  end    = vector + (-targetOrbitalRange - 160, -80×difference)
  AddDash(start, end, dashSpeed = 1)
  ```
  每 8 条之后 `borderCenter.Y -= 200`、`attackTimer3 += 200`（结界整体上移）。
- 冲刺数量过半后每帧 `ResolveAll()`：**把本体吸附到最近一条冲刺轨迹上**，沿 `Lerp(start, end, pow(|counter/40|, 1.15))` 移动（见 5.12 的 Resolve 机制）。全部走完 → `SwapPhase(8)`。
- 未过半时 `NebulaForm = true`（半透明星云态，接触改用 2.25 格半径判定）。

### 5.4 AttackID 8：二阶段星砸（1922-2028）

- `attackTimer < 20`：先回到「星云形态」流程 —— 先 `attackTimer3` 升到 70 帧再追击玩家上方 `350 - 90×cos(t°)`，`attackTimer3 == 140` 时落地并恢复普通形态。
- 之后三次跳砸（`attackTimer == 36` 起跳，`Speed = 15.5 + attackTimer2×4`，X ×1.1、Y ×1.0，再 `-5 - attackTimer2×2`），`attackTimer == 37` 时 `DoSlamAttack()`。
- 每次落地 `attackTimer2++`，第四次后 `SwapPhase(9)`。
- **落地弹幕**（1050-1073）：6 或 8 颗 `AstageldonStar2` 以 `speed = min(2 + 0.3×cycles, 4)` 全向均布炸出，`ai[1] = ±1` 控制旋向。

### 5.5 AttackID 9：交叉星冲（2029-2124）

- 先 `attackTimer == -30` 起跳（`velocity.Y -= 10`），`attackTimer >= 0` 切星云形态。
- 星云形态每帧：`attackTimer 20-50` 速度 `×0.9825` 并叠 `sin(9°t) × 2.4` 水平摆；`50-110` 每帧水平 `+= direction × 4.5`；`110` 帧把位置强制摆到结界外圈 `targetOrbitalRange + 240`。
- 之后进入 140 帧一个小循环：
  - `timer % 140 == 80`：`velocity *= 0.1`，并加一条**指向玩家预测位置 960 px 外**的冲刺；
  - `timer % 140 < 80`：每帧朝玩家加速 `(60 - timer%140)/60 × 2`，且 `timer % 3 == 0` 时发射 `AstrageldonStar3`（伤害 ×0.8，`ai[0] = 速度角 + 45°`）；
  - `timer % 140 ≥ 110`：`ResolveAll` 吸附到冲刺轨迹并回弹；
  - 期间 `targetOrbitalRange -= 1`（结界持续收缩）。
- `attackTimer > 962` → `SwapPhase(10)`，`attackTimer = -90`。

### 5.6 AttackID 10：收缩结界 + 星霰弹（2125-2241）

1. `targetOrbitalRange` 向 600 px 插值（`lerp 0.03`/帧）。
2. 本体沿结界内圈逆时针巡游：`degrees = lerp(旧角, 目标角, min(timer/40,1)) + sin(6°t)×20`，每 180 帧换一次目标角（180 帧的末帧取「玩家相对中心的方向」，540 帧后固定 90°）。
3. 巡游区间（`timer = attackTimer % 180`）：
   - `timer % fireCooldown == 0 && timer ≤ 120`：朝玩家喷 `NebulaResidue`（`fireCooldown = max(30 - cycles×5, 10)`）；
   - `timer == 150`：播 `Item105`、`DustExplosion(3)`，放 **6 或 8 对** `AstrageldonStar2`（±0.33 旋向）做扇形散射；
   - `attackTimer ≥ 720` 时把 `sin` 项清零；
   - `attackTimer ≥ 800`：`cycles == 0 → SwapPhase(12)`，否则 `SwapPhase(11)`。
4. 结界中心每帧 `+= direction × 3 + (0, -1.2)`（整体漂移）。

### 5.7 AttackID 11：螺旋激光（2242-2355）

```text
第一阶段（星云形态，attackTimer < 110）
  向 borderCenter 上方 (targetOrbitalRange - 64) 收敛，speed ≈ 4 + 距离×0.002
  attackTimer == 90：velocity *= 0.1，DoSlamAttack()，NebulaForm = false

第二阶段（落地）
  attackTimer 90-140：贴地（gravity -= 0.4），着地后 attackTimer++
  attackTimer == 150：播 AstrageldonSpiralBeamCharge，生成 8 道 SpiralLaser（每 45° 一道，半径 80 px 环绕）
  attackTimer ≥ 210：开始自转，rotation += -10° × clamp((t-210)/180, 0.2, 2)
  每 speedMod 帧（= max(240 - cycles×30, 120)）且 340 < t < 1270：
      播 Item105、DustExplosion(2)、放 8 颗 AstrageldonStar2（±1.2 旋向）
  attackTimer == 1450 → SwapPhase(12)
  额外：若本体脚下 480 px 处无可见线，本体与 borderCenter 同时每帧上移 1.5 px
```

`SpiralLaser` 本身：前 180 刻（3 秒）零伤害，181 点 × 7.2 px ≈ 40.5 格；激活后内圈以 **0.6°/刻** 反转、判定宽 8→22 px 随剩余时间收细，寿命 1260 刻（21 秒）。

### 5.8 AttackID 12：裂星史莱姆跳砸（2356-2448）

- 前半与 AttackID 8 相同（星云态归位 → 落地）。
- `attackTimer ≥ 20` 后：`gravity = 0.2`，`attackTimer2` 每帧 +1；`attackTimer2 > 36` → `NebulaForm = true` 且清除隐形平台；`attackTimer2 < 32` 或 `50-60` 之间每帧 `velocity.Y -= 4`（上升气流）。
- `attackTimer2 > 120` → `SwapPhase(13)`。
- **落地时**（`slamTimer == 0`，1079-1093）：`SummonSlimer(min(6 + max(cycles-1,0) + 专家 2 + 死亡 1, 12))`，即最多 24 只。

### 5.9 AttackID 13：横向搬移结界的冲刺（2449-2492）

与 AttackID 7 同构，只是把冲刺改成横向布置，结界沿 X 移动：

```text
direction3 = totalPhase2Cycles % 2 × 2 - 1
vector = borderCenter + (targetOrbitalRange - 160×(t2+1) + t3, 0) × direction3
start  = vector + (80×diff, targetOrbitalRange + 160) × direction3
end    = vector + (80×-diff, -targetOrbitalRange - 160) × direction3
每 8 条后 borderCenter.X -= 200 × direction3，attackTimer3 += 200
ResolveAll() 走完 → SwapPhase(14)
```

冲刺途中（`lerp ∈ [0.2, 0.8]`）每帧吐 2 颗 `NebulaResidue`（伤害 ×1.4）。

### 5.10 AttackID 14：速射星 + 星霰弹循环（2493-2635）

两段式，`attackTimer3` 计外层循环：

| 段 | 条件 | 行为 |
|---|---|---|
| A 速射 | `attackTimer3 ≤ 1` | 每 `speed13 = max(10 - cycles×2, 3)` 帧发一颗 `AstageldonStar4`；总数 `total = min(15 + cycles×5, 50)`；每次发射 `velocity -= 玩家方向 × 8/(1+cycles×0.5)`（后坐） |
| A 结束 | `attackTimer2 ≥ total` | `cycles == 0 && t3 ≥ 1` → `SwapPhase(8)` 且 `cycles++`；否则重置计时、`attackTimer = -60/t3==1 或 -120` |
| B 霰弹 | `attackTimer3 > 1` | 每 `totalTime/amount` 帧放一轮 `NebulaResidueHB`；`amount = min(3 + cycles, 6)`、`totalTime = 220 + 40×amount`；每轮 5(+复仇 1) 颗 `NebulaResidueHB`，初速 `player→boss 反向 × 23`、散布 `±(30+2n)°`、附加随机圆 6×(1+0.15n) |
| B 起始 | `attackTimer == -61` | 播落地音、全向 8~12 对 `AstrageldonStar2`（±1 旋向） |
| B 结束 | `attackTimer2 ≥ amount` | `t3 ≥ 3` → `SwapPhase(8)` 且 `cycles++`，否则 `attackTimer = -120`、`t3++` |

### 5.11 AttackID 15：绝望期（1733-1814）

```text
进入：doDamage = false、NebulaForm = true、dontTakeDamage = true、CanHaveBossHealthBar = false
      播 AstrageldonDeathImplosion、DustExplosion、DustExplosion(3)
      targetOrbitalRange = 1000（从当前值开始收）
每帧：Center 向 borderCenter 插值 0.1
      attackTimer > 90 后：targetOrbitalRange -= 0.4 + attackTimer2 × 0.02
      每隔 (24 + attackTimer2×2) 帧，从结界外圈随机角度砸下 HostileAsteroid：
          orbitalRange = (0, -(range+200)).RotatedBy(random(60°) × 旋向)
          velocity = 归一化 × -4 + (0,10) + 随机圆(4, 2.5)
      范围 < 330 时 attackTimer2 += 0.2 加速
      targetOrbitalRange ≤ -136：attackTimer3++，> 10 后自爆
自爆：SimpleStrikeNPC(10,000,000) + 8 颗 NebulaHitbox(0 伤，纯视觉)
屏幕：AstrageldonSky.Darkness 每帧 +0.00076（到 1 全黑）
旋转：rotation += sqrt(|attackTimer|) × direction（越转越快）
ResolveAll(1, 5) 持续吸附冲刺轨迹
```

绝望期大约 **340 帧收束 + 10 帧引爆 ≈ 6 秒**，期间血条消失、玩家不能再造成有效伤害。

### 5.12 冲刺"伪装"机制：`DashTelegraph` + `ResolveAll`（497-557 / 2791-2867）

这是二阶段最特殊的部分：Boss 会**先在半空画出一条 40 刻的预警线，然后本体瞬移到该线起点、沿线上做加速滑行**，滑行时还会反弹、吐残留、留拖尾。

- `DashTelegraph`：`delay = 40`；前 40 刻画线（每刻 alpha +1/40）；随后进入「冲刺」40 刻：`counter` 从 -40 走到 0，位置 = `Lerp(start, end, pow(|counter/40|, 1.15))`（每刻 -1 即 3~5 px 的位移曲线）。
- `ResolveAll(resolveSpeed)`：找最近一条未解析的冲刺线，进入 `resolving`；之后每帧把 `NPC.Center` 直接设到线上的 `Lerp(..., pow(|c/40|,1.15))`，并把本帧位移差当作 `outwardVelocity` 缓存。
- 冲刺过程中按当前招式吐残留（AttackID 7 吐 2 颗 `NebulaResidue`、AttackID 9 每 2 帧吐星、AttackID 13 吐 2 颗横向残留），并在 `lerp ∈ [0.2, 0.8]` 之间才生效。
- MC 侧不需要 40 刻的"线"，但**必须保留 40 刻的预警时长与相同的 Lerp 曲线**，否则冲刺的手感与原版不一致；预警线用 `ParticleHelper` 每 2 格一个紫色粒子即可。

---

## 六、弹幕与召唤物速查

> 完整字段表（含每个 `SetDefaults`、判定、绘制参数、生成处行号）见附录文件 `astrageldon-ai-ref-projectiles.md` 与 `astrageldon-ai-ref-minions.md`。

伤害统一以 `D = GetBaseDamage()/2`（专家 110）为基数。

| 实体 | 尺寸 px | 寿命 | 速度 / 运动 | 伤害 | 生成处 |
|---|---|---|---|---|---|
| `AstrageldonStar`（环绕星） | 42 | 360 | 绕本体 140 px 公转（`counter ≤ 70`），70 帧后射出 3 道激光（±4°、5 px/刻） | 0.75 D | `SummonStars` |
| `AstrageldonStar2`（螺旋彗星） | 36 | 180 | 每子步 `×1.018`、转 ±1.5°，4 子步/刻 → 180 步后约 ×24.5 速 | D | 6 处（5.4/5.6/5.10） |
| `AstrageldonStar3`（四向星） | 42 | 360 | 原地自旋，70 帧后射 4 道激光；**只有前 3 帧能造成伤害** | 0.8 D | 5.5 / 5.12 |
| `AstrageldonStar4`（追踪星） | 36 | 180 | 追踪系数 `0.5×(180-timeLeft)/180×(1+0.2×cycles)`，命中即消失 | D | 5.10 |
| `AstrageldonLaser` | 12×12 | 90 | 5 px/刻 + 0.7 px/刻² 前向加速，累计约 102 格 | 继承 | 星/星3 死亡时 |
| `StarLaser`（正弦长激光） | 8×8 占位 | 40 | 121 点 × 12 px = 45 格，正弦振幅 6 px；**第 11 帧起无伤** | 继承 | Star3 |
| `SpiralLaser`（螺旋双光） | 8×8 占位 | 1260 | 绕本体 80 px，前 180 刻零伤，之后 0.6°/刻反旋 | 继承 | 5.7 |
| `AstraRock`（岩石） | 32×36 | 960 | 环绕型：半径 40→720、`+2°/帧`；坠落型：`counter +6`、270 帧后 `-24` 上抛 | 0.75 D / D | 5.6 / 4.6 / 过场 |
| `AstraBlade`（地刺） | 20×58 | 100 | 从地面升起后横向铺开，每 10 帧向外一根、间距 32 px（1 格）；破防 | D | 所有落地 |
| `HostileAsteroid`（陨石） | 75（判定 120） | 600 | `extraUpdates 2`，从结界外圈砸下；死亡喷 5 颗 Star2 | D | 5.11 |
| `NebulaResidue` | 34 | 60（可到 200） | 0.175 px/刻² 加速，8 段拖尾 | D（冲刺时 1.4 D） | 5.5/5.10/5.12 |
| `NebulaResidueHB` | 34 | 依 `ai[0]` | 自旋 5°/帧并衰减，死亡裂 4 颗小碎块 | D | 5.10 |
| `NebulaSphere`（结界） | — | — | 半径 = `targetOrbitalRange`，16×2 段 0.33°/帧 | 1.2 D | 5.1 |
| `NebulaHitbox` | 240×240 | 20 | 纯视觉，0 伤害 | 0 | 5.11 自爆 |
| `NovaSlime`（新星史莱姆） | 64×40 | — | 600 HP / 接触 80（SetDefaults 原值，专家再 ×2，实际血量还会被 `×0.6×balance` 缩放）；被召唤时从 700~840 px 高空落下，之后 `10.5 px/刻` 弹向玩家 | 接触 80 | 4.4 落地 |
| `NovaSlimer`（裂星史莱姆） | 52×44 | — | 1200 HP / 接触 120（原值，专家 ×2）；螺旋收缩 → 反向冲刺 → 自毁成 1 根 `SlimerSpear`（60 伤、14 px/刻） | 接触 120 | `SummonSlimer` |
| `SlimerSpear` | 48×48 | — | 不可见长矛，命中叠 `InterstellarCorruption` | 60 | NovaSlimer 死亡 |

MC 侧落地方式建议：
- 有明确矩形判定、需要撞地形/撞玩家的（星、岩石、地刺、陨石、碎块）→ 走**普通 `EntityType`**（像原版火球），自管 tick 与 AABB，不依赖 owner；
- 激光类（StarLaser / SpiralLaser）→ 用「一段折线的连续判定」，每 tick 对折线做 `AABB.clip` 或逐点球检测，**不要生成几十个实体**；
- 结界与预警线 → 纯客户端视觉 + 服务端只存参数（半径、中心、角度），不发实体。

---

## 七、表现层映射

| 泰拉侧 | 数据 | MC 侧做法 |
|---|---|---|
| 音乐切换 | P1 `AstrageldonPhase1`；`attackPhase ≥ 6` 或 `attackPhase 5` 时切 `AstrageldonPhase2`（918-921） | `MusicManager.play(...)`，工程里 `doomsday_star_phase_1/2` 已配好 `stream: true` |
| 暗屏 | `AstrageldonSky.Darkness`：Phase 7/13/14 每帧 +1/180（上限 0.5），其余 -1/180（3321-3334）；绝望期 +0.00076/帧到 1 | 客户端设置雾/天空亮度或叠加一层深紫渐变 |
| 震屏 | 登场落地、过场 360 刻：`ShakeIntensity += 30`、`ShakeMult = 0.95` | 原生 `level.playLocalSound` + 相机抖动手写，或客户端屏幕震动 |
| 本体动画 | 7 帧 / 3 刻一帧；拖尾 12 段；光环 50 段（`AstrageldonHaloManager`） | GeckoLib 动画：用 7 个关键帧的 idle 循环 + 拖尾用粒子带 |
| 星云形态 | 换整套渲染（metaball 球体 + `DrawNebula` 拖尾），碰撞箱切 170×170 | 换成第二个 GeoModel（发光球体 + 内部粒子），或同一模型换材质/骨骼缩放 |
| 落地挤压 | `addedStretch` 累加 0.08/帧，`scale = (1-stretch, 1+stretch)` | GeckoLib 的 squash 关键帧，或 PoseStack 缩放 |
| 预警线 | `DashIndicator` 贴图 2 px 宽虚线，40 刻由淡到浓 | `ParticleHelper` 沿路径每 2 格一颗紫色粒子，alpha 随时间线性上升 |
| 地刺/岩石 | 贴图 + `_Glow` 叠加层 | 模型发光层（`packedLight(FULL_BRIGHT)`） |
| 音效 | 见附录音效清单（出场 / 4 种弹跳 / 死亡 / 过场 / 光束 / 旋转岩石） | `SummonerSoundRegister` 新增条目，BGM 与音效分离 |

**metaball 渲染（星云形态）不可能 1:1**：泰拉侧用 `NebulaManager` 做多层场渲染，MC 侧没有等价的屏幕空间场。替代方案：发光球体模型 + 内部随机粒子 + 边缘加法混合光晕，观感接近即可；判定不要跟着视觉走，仍然用 170×170 px 换算的 5.31 格立方体 + 72 px 球半径。

---

## 八、工程落地

### 8.1 现状（已核对）

| 项 | 位置 | 状态 |
|---|---|---|
| 实体类 | `common\entity\Astrageldon.java` | 骨架在，`Phase` 17 项枚举已按上表铺好；`tick()` 里只有水平阻尼 + 索敌，`registerControllers` / `registerGoals` 空 |
| 注册 | `register\SummonerEntityRegister.java` | `astrageldon` 已注册，`sized(3,3)`、`updateInterval(1)`、`clientTrackingRange(128)` |
| 属性 | `common\Event.java` + `createAttributes()` | 占位值（HP 800 / 攻击 15 / 护甲 10），要按 2.2 换成 156000 / 220 / 40 |
| 渲染 | `client\entityRenerer\AstrageldonRenderer.java` | 有，但 `render()` 没调 `super.render(...)`，等于不画；且工程里没有任何 `GeoModel` 子类 |
| 音乐 | `SummonerSoundRegister` + `sounds.json` | `doomsday_star_phase_1/2` 已预留；实体注册名 `astrageldon` 与语言键 `entity.summoner.doomsday_star` 不一致，需统一 |
| Boss 血条 | — | 全工程零实现 |
| 弹幕 | `common\projectile` + Lyra `AttachmentEntity` | 那套绑定 owner 玩家，Boss 弹幕**不要**复用 |

### 8.2 字段与同步

```java
// 一一对应 npc.ai[0..3]
private Phase phase = Phase.Phase11;      // ai[0]
private float attackTimer;                // ai[1]（60 Hz 帧）
private float attackTimer2;               // ai[2]
private float attackTimer3;               // ai[3]

// 转换用的状态
private boolean secondPhase, secondPhaseTransition, desperation, nebulaForm, doDamage;
private float borderOrbitalRange = 4800f, targetOrbitalRange = 1600f;  // px
private double borderX, borderZ;          // borderCenter（Y 轴替换：泰拉 Y 向下 → MC Y 向上，见 8.3）
private float invisiblePlatformY = -1f;   // 隐形平台（相对高度）
private float dashSpeed, resolveSpeed;
private boolean resolveDash;
private int totalPhase2Cycles;
private float slamTimer = -1f;
private int despawn;
```

同步：全部走 Lyra 的 `registerSyncFields`，**不要**新建 payload。服务端写、客户端只读；`borderCenter`、`targetOrbitalRange`、`phase`、三个 timer、`despawn` 必同步，`slamTimer` 与动画相关的 `addedStretch` 可只同步计算用的基准，客户端自行插值。

### 8.3 坐标系与运动

泰拉 Y 轴向下、MC Y 轴向上。**位置只做一次镜像**：`y_mc = worldHeight - y_terraria` 之类的全局映射没有意义，实际操作是「泰拉算式里的 `+Y` 在 MC 里写成 `-Y`」：

```text
// 泰拉：velocity.Y = -sin(rot) * Speed * 0.75 - 14       // 负值 = 向上（泰拉向上是负）
// MC  ：setDeltaMovement(vx, +sin(rot) * Speed * 0.75 + 14 / 32, vz)   // 正值 = 向上
```

角度也要注意：泰拉 `atan2(target.Y - self.Y, target.X - self.X)` 在 Y 翻转后，`sin` 分量的符号要同时翻。

**每 tick 三次子步**，位置用手写积分而不是原版物理：

```java
@Override
public void tick() {
    super.tick();
    for (int i = 0; i < 3; i++) { substep(); }   // 60 Hz
    flushRenderHistory();                        // 供客户端插值
}

private void substep() {
    // 1. 状态机（对应 AttackID）
    switch (phase) { case Phase11: tickIdle(); break; ... }
    // 2. 重力：if (!nebulaForm && vyInYetAnotherUpIsPositive() < gravity*20) vy += gravity / 32.0;
    // 3. 碰撞：groundCollision();      // 见 8.4
    // 4. 边界：arenaClamp();           // 见 8.5
    // 5. 计时器 tickTimers();          // slamTimer 的 -1 / -2 规则
}
```

### 8.4 碰撞（泰拉 `Collision.TileCollision` 的替代）

泰拉侧所有落地都靠 `Collision.TileCollision` + **5 段切片**防穿透（2721-2738）：

```text
for i in 0..4:
    extraY = i * velocity.Y / 5
    把 velocity 交给「位移 (0, extraY) 后的 AABB」做一次地形检测
    任一子步被挡住且 vy > 2 → position.Y += extraY * 2；landingRecoil = vy * 0.02；break
```

MC 侧等价实现：把本子步位移拆成 3-5 段，每段用 `level().clip` 或 `BlockCollision` 判一次，命中即落地（**不要用 `move(MoverType.SELF, ...)` 让原版接管**，否则速度会被原版物理改写）。落地当帧要做三件事：`landingRecoil`、`slamTimer` 归零判定、以及对应的落地弹幕/地刺。

**隐形平台**（二阶段，2702-2719）：当本体向下穿过 `targetOrbitalRange` 下沿时，记录 `invisiblePlatformY = position.Y + height`，把 `velocity.Y` 归零，并生成一条 `NebulaPlatform` 视觉条。对玩家不生效（不是方块），只服务 Boss 自己的「空中落脚」。MC 侧用一个只会拦截 Boss 的虚拟地面高度即可。

### 8.5 竞技场

- 数据：`center + radius`，服务端唯一权威，`radius` 通过 `borderOrbitalRange` 向 `targetOrbitalRange` 插值。
- 每子步：若 `dist > radius + 40`（普通形态）或 `radius + 400`（星云形态）→ 拉回并把外向速度分量抵消（2691-2701 的公式，直接照抄向量投影）。
- 距离边缘 200 px 内免疫（`dontTakeDamage`）。
- 视觉：`NebulaSphere` 用一层半透明球形壳（`RenderType.translucent` + 自定义 shader 或两层缩放环），带宽按 `radius ± (156 + 2250×系数)` 换算，别把 2250 px 当判定。

### 8.6 Boss 血条（两段）

```java
private final ServerBossEvent bossEvent = new ServerBossEvent(
        Component.translatable("entity.summoner.astrageldon"),
        BossBarColor.PURPLE, BossBarOverlay.PROGRESS);
// startSeenByPlayer / stopSeenByPlayer 里 addPlayer / removePlayer
// tick(): 一阶段 progress = hp / 156000；二阶段换成 217500 与 PINK 色
// 过场：progress 从 0 涨到 1（源码就是 life = lifeMax/180 × attackTimer 的"回充"演出）
// 绝望期：setVisible(false)（源码 CanHaveBossHealthBar = false）
```

### 8.7 建议的落地顺序

1. 先做**运动骨架**（8.3 + 8.4 + 8.5）：能跳、能落地、能被结界约束、能跑 `Phase12` 三连跳砸。这一步不涉及任何美术。
2. 再做**一阶段 6 个状态**（`Phase11`-`Phase16`），弹幕用最简单的白色立方体代替，先验证时序与数量。
3. 接入 `Phase17` 过场与二阶段生命重置。
4. 二阶段按 `Phase21`→`Phase211` 顺序补，冲刺（`DashTelegraph`/`ResolveAll`）放在 `Phase22` 一起做，因为它是后面 9/13 的公共设施。
5. 最后补美术、音效、血条、暗屏。

---

## 九、验收清单（逐条可测）

| # | 项 | 期望 |
|---|---|---|
| 1 | 一阶段血量（专家） | 156000；归零后进入 7 秒过场，期间不可击杀 |
| 2 | 过场时长 | 420 刻 = 7.0 秒；360 刻时 8 颗岩石向玩家方向炸出 |
| 3 | 二阶段血量 | 217500；`Phase25` 时结界插值到 600 px |
| 4 | `Phase12` 三连跳砸 | 起跳间隔递减；每次落地地刺 10 / 20 / 30 根 |
| 5 | `Phase13` 散射星 | 每 100 帧一波，共 4 波；每波 6（普通）/ 8（专家）颗环绕星，70 帧后各变 3 道激光 |
| 6 | `Phase14` 史莱姆雨 | 3 次竖直跳；落地放地刺 15 根 + 5~6 只新星史莱姆从上方 700~840 px 落下 |
| 7 | `Phase16` 环绕岩石 | 40 颗岩石公转 9 秒后落下 |
| 8 | `Phase22` 冲刺 | 16 条冲刺线、每条间隔 15 帧，每 8 条结界上移 200 px |
| 9 | `Phase24` 交叉星冲 | 目标半径每帧 -1；`attackTimer` 到 962 转下一招 |
| 10 | `Phase27` 螺旋激光 | 8 道、前 3 秒零伤、寿命 21 秒、0.6°/刻反旋 |
| 11 | `Phase210` 循环 | 速射 15 发（首轮）→ 3 组霰弹；`cycles` 递增后转 `Phase23` |
| 12 | 绝望期 | 生命锁 1、血条消失、约 6 秒收束后自爆，8 颗视觉球 |
| 13 | 接触伤害 | 专家 220；星云形态半径 2.25 格 |
| 14 | 退场 | 玩家死亡/离开 200 格累计 6 秒后消失；血条移除 |

---

## 十、无法 1:1 的部分

1. **地形半分辨率**：泰拉 1 tile = 0.5 格，MC 的地形只能按 32 px 的粗粒度对齐。跳跃落点最高会差半格，砸地高度同理——这是比例尺本身的代价，靠验收时放宽半格容差处理。
2. **metaball / 屏幕滤镜**：星云形态的多层场渲染、`AstrageldonSky` 的紫黑渐变无法完全复刻，用近似方案（第八章）。
3. **Calamity 难度分支**：复仇/死亡/传奇种子会改变数量与速度（源码里到处都是 `CalamityWorld.revenge/death`、`Main.getGoodWorld`）。MC 侧建议映射成 `Difficulty`：和平→跳过、普通→经典系数、困难→专家；复仇专属分支（`Phase15`、`Phase24` 的召唤数量加成）可挂在配置文件上单独开。
4. **`DR_NERD` 计时减伤**：Calamity 的按时长削减伤害机制（`CatalystNPC.TimedDRDivisor/HealthCutting`），MC 没有等价物；不影响 AI 行为，只影响数值，可以不做。
5. **`getGoodWorld`（传奇种子）**：会强制 `totalPhase2Cycles = 2`，属于彩蛋分支，不建议复刻。

---

## 附：源码对照表（移植时按行号找）

| 内容 | `src\CatalystMod.NPCs.Boss.Astrageldon\Astrageldon.cs` |
|---|---|
| AttackID 枚举 | 45-79 |
| 常量（血量/伤害/延迟） | 84-102 |
| `SetDefaults` | 386-409 |
| 难度换算 | 411-429 |
| `FindFrame`（7 帧 / 3 刻） | 431-445 |
| `PreAI`（全场前置） | 832-916 |
| `AI()` 总入口 | 923 |
| 全局阻尼/朝向/重力/旋转 | 929-945, 2638-2689 |
| AttackID -1 | 1132-1172 |
| AttackID 0 | 1173-1227 |
| AttackID 1 | 1228-1347 |
| AttackID 2 | 1348-1463 |
| AttackID 3 | 1468-1551 |
| AttackID 4 | 1552-1645 |
| 二阶段分区 | 1648-1732 |
| 绝望期 15 | 1733-1814 |
| AttackID 6 | 1817-1878 |
| AttackID 7 | 1879-1921 |
| AttackID 8 | 1922-2028 |
| AttackID 9 | 2029-2124 |
| AttackID 10 | 2125-2241 |
| AttackID 11 | 2242-2355 |
| AttackID 12 | 2356-2448 |
| AttackID 13 | 2449-2492 |
| AttackID 14 | 2493-2635 |
| 碰撞/隐形平台/结界约束 | 2678-2744 |
| `ResolveAll`（冲刺吸附） | 2791-2867 |
| `SpeedUp` | 2869-2890 |
| `SwapPhase` | 2892-2971 |
| `SummonRocks/Slimer/Stars` | 2973-3024 |
| `GroundBlades/SummonBorder/GroundRocks` | 3026-3071 |
| `DustExplosion` | 3073-3205 |
| `AddDash` | 3207-3222 |
| 阶段切换 `HitEffect` | 3265-3298 |
| 二阶段血量 `SecondPhaseHealthIncrease` | 3300-3319 |
| 暗屏 `DarkenScreenSecondPhase` | 3321-3334 |

反编译源码位置：`C:\Users\l1518\Desktop\CatalystMod_Decompiled\src\CatalystMod.NPCs.Boss.Astrageldon\`。
