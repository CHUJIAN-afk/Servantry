# 附录 B：弹幕 / 激光 / 结界 / 特效实体 精确参数

> 由 x-spec 从反编译源码逐字段提取，所有数值标注 文件:行号。单位同上。
> 附录 C 里有「反编译死代码清单」，移植时注意：某些常量声明后未被引用（如 SpiralLaser.innerRotation、mult 恒 0 使正弦抖动失效）。

# Astrageldon（末世星史莱姆）特效实体参数表

> 数据来源：`src\CatalystMod.NPCs.Boss.Astrageldon\*`、`src\CatalystMod.Projectiles.Enemy\*`、`src\CatalystMod.Projectiles.Magic\*`、`src\CatalystMod.Projectiles.Typeless\*`（反编译 C# 源码，逐行核对）。
> 所有 `文件名:行号` 均为该反编译文件中的真实行号。

## 单位与约定

| 项 | 约定 |
|---|---|
| 长度 | 源码全部为**像素**；`1 tile = 16 px`。本文凡标「tile」处均已换算 |
| 速度 | **像素 / 刻**（px/tick）；`1 px/tick ≈ 3.75 tile/s` |
| 时间 | **刻**（tick），`60 刻 = 1 秒` |
| 角度 | 源码里 `MathHelper.ToRadians(x)` 的参数是**度**，`XX.ToRotation()` 返回**弧度**；本文标注 `°` 或 `rad` |
| 颜色 | `Color(R,G,B,A)`；`* 系数` 为 premultiplied alpha 乘算 |
| 主色 | 「星际腐蚀紫」= `Color(112, 51, 166)`（`src\CatalystMod.NPCs.Boss.Astrageldon\Astrageldon.cs:104`，`src\CatalystMod\CatalystMod.cs:105` 为其别名）；金黄 = `Color(255, 233, 2)`；品红 = `Color(220, 95, 210)` |

## 实体总览

| 类 | 类型 | 作用 | 由谁生成 |
|---|---|---|---|
| `Astrageldon` | `ModNPC` | Boss 本体 / 攻击调度 | 自然生成 / 召唤物 |
| `AstrageldonLaser` | `ModProjectile`（敌对） | 金/紫双色追踪小激光（带前摇虚线） | `AstrageldonStar`、`AstrageldonStar3` |
| `SpiralLaser` | `ModProjectile`（敌对） | 螺旋双光束，长 21 s | `Astrageldon.AI` phase 11（`Astrageldon.cs:2305`） |
| `StarLaser` | `ModProjectile`（敌对） | 正弦波状长激光（90 tile） | `AstrageldonStar3`（`AstrageldonStar3.cs:174`） |
| `NebulaPlatform` | 普通类（非 Projectile） | 隐藏平台 / 下坠轨迹条 | `Astrageldon.AI`（`Astrageldon.cs:2710`） |
| `NebulaResidue` | `ModProjectile`（敌对） | 冲刺/喷吐残留的能量碎块（解体碎片） | `Astrageldon.cs:2200/2840/2854`、`NebulaResidueHB.OnKill` |
| `NebulaResidueHB` | `ModProjectile`（敌对） | 带 4 颗子碎块的大碎块 | `Astrageldon.cs:2612` |
| `DashTelegraph` | 普通类（非 Projectile） | 冲刺「伪装」指示条 + 位移曲线落点 | `Astrageldon.AddDash` (`Astrageldon.cs:3207`) |
| `AstrageldonHaloManager` | 普通类（非 Projectile） | 身后 50 段拖尾光晕 | `Astrageldon.PreAI` (`Astrageldon.cs:876`) |
| `AstraRock` | `ModProjectile`（敌对） | 环绕/坠落陨石 | `SummonRocks`/`GroundRocks`/phase 5 |
| `AstraBlade` | `ModProjectile`（敌对，破防） | 地面连锁地刺刀刃 | `GroundBlades` (`Astrageldon.cs:3042`) |
| `HostileAsteroid` | `ModProjectile`（敌对） | 绝望期环形大陨石（死亡炸 5 颗星） | `Astrageldon.cs:1762` |
| `AstrageldonStar` | `ModProjectile`（敌对） | 环绕召唤星，70 刻后射出 3 道激光 | `SummonStars` (`Astrageldon.cs:3021`) |
| `AstrageldonStar2` | `ModProjectile`（敌对） | 螺旋加速彗星（带前摇虚线） | 6 处，见正文 |
| `AstrageldonStar3` | `ModProjectile`（敌对） | 四向星，70 刻后射出 4 道激光 | `Astrageldon.cs:2101/2860` |
| `AstrageldonStar4` | `ModProjectile`（敌对） | 追踪星弹（一次性伤害） | `Astrageldon.cs:2532` |
| `NebulaSphere` | `ModProjectile`（敌对） | 第二阶段球形结界（16×2 段） | `SummonBorder` (`Astrageldon.cs:3052`) |
| `NebulaHitbox` | `ModProjectile`（友方，0 伤） | 死亡内爆的纯视觉判定球 | `Astrageldon.cs:1788` |

---

## 1. Astrageldon（本体）

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\Astrageldon.cs`

### 1.1 SetDefaults 基础属性

| 字段 | 值 | 行号 |
|---|---|---|
| `aiStyle` | `-1`（全手写） | 388 |
| `lifeMax` | `105000` | 389 |
| `damage` | `160` | 390 |
| `defense` | `40` | 391 |
| `knockBackResist` | `0` | 392 |
| `width / height` | `174 / 118`（NebulaForm 期间改为 `170 / 170`） | 393-394 / 2762-2770 |
| `value` | `Item.buyPrice(1)` | 395 |
| `boss` | `true` | 396 |
| `lavaImmune` | `true` | 397 |
| `noGravity` | `true` | 398 |
| `netAlways` | `true` | 399 |
| `noTileCollide` | `true` | 401 |
| `scale` | `1`（首帧 `runOnce` 时置 `1.25`） | 406 / 958 |
| `canBreakPlayerDefense` | `true`（Calamity 扩展） | 407 |
| `dontTakeDamage` | 默认 false，多处动态置 true：过场 `1138`、`1731`、`2651`、`2666`、`2776`、`1813`、`3284`、`3292` | — |
| `frameCount` | `7` 帧，`frameCounter` 每 3 刻切换一帧 | 375 / 433-443 |

### 1.2 难度缩放（`ApplyDifficultyAndPlayerScaling`，411-429）

| 项 | 公式 | 行号 |
|---|---|---|
| damage | `damage * 220/160/2` → 普通难度落到 `110` | 413 |
| damage（复仇） | `×1.12`；复仇再叠加死亡 `×1.12` | 417 / 426 |
| lifeMax（非复仇） | `balance * lifeMax * 156000/105000`，随后 `/= 2`（→ 约 `78000 * balance`） | 421-428 |
| lifeMax（复仇） | `balance * lifeMax * 200000/105000`，随后 `/= 2`（→ `100000 * balance`） | 416 / 428 |

### 1.3 攻击状态机（`AttackID`，45-79）

| id | 名称 | id | 名称 |
|---|---|---|---|
| -1 | Idle | 7 | AscendWithDashes |
| 0 | JumpSlam | 8 | Phase2StarSlam |
| 1 | ScatterStars | 9 | CrossStarDash |
| 2 | SlimeRain | 10 | ShrinkArenaAndStarShotgun |
| 3 | RevengeanceJumpSlam | 11 | SpiralLaser |
| 4 | OrbitingRocks | 12 | SlimerSlam |
| 5 | Phase2Transition | 13 | DashAndMoveArenaHorizontally |
| 6 | SummonSlimersAndAscend | 14 | RapidlyFireStarsThenShotgunNebulaScatterShots |
| 15 | DesperationPhase | | |

`SwapPhase` 会同时把 `ai[1]` 置为该 phase 的**负前摇值**，并整体乘 `SpeedUp()`（`Astrageldon.cs:2892-2971`）；`SpeedUp()` 在血量比例上做 `clamp(ratio, 0.4/0.25/0.1, 1)`（`2869-2890`）。

| phase | `attackTimer` 初值（刻） | 行号 |
|---|---|---|
| -1 | -60 | 2899 |
| 0 | -40 | 2903 |
| 1 | -70 | 2907 |
| 2 | -90 | 2911 |
| 3 | -60 | 2915 |
| 4 | -70 | 2919 |
| 5 | 0 | 2923 |
| 6 / 7 | -120 | 2927 / 2931 |
| 8 | -60 | 2935 |
| 9 | -110 | 2939 |
| 10 | -60（`ai[2]=ai[3]=90`） | 2943-2945 |
| 11 | 0 | 2949 |
| 12 | -50 | 2953 |
| 13 / 14 | -120 | 2957 / 2961 |

### 1.4 生命周期 / 判定

| 项 | 数值 | 行号 |
|---|---|---|
| 脱战判定 | 玩家死亡或距离 > `6400 px`（400 tile）时 `despawn++`；`despawn >= 360` 刻后 `active=false` | 452-470 |
| 贴身伤害 | 接触伤害走 NPC 默认判定；`OnHitPlayer` 额外施加 `ArmorCrunch` 240 刻 | 3224-3227 |
| 半血 → 二阶段 | `HitEffect` 中 `life<=0 && !secondPhase` → `secondPhaseTransition=true` + `dontTakeDamage` | 3276-3285 |
| 二阶段血量提升 | `SecondPhaseHealthIncrease()`：复仇 `336000` | 3300-3304 |
| 三阶段（绝望） | `!desperation` 时置 `desperation=true; life=1; dontTakeDamage=true` | 3286-3293 |
| 绝望期自尽 | `targetOrbitalRange` 缩到 `-136` 后第 10 刻 `SimpleStrikeNPC(10000000)` | 1775-1783 |
| 强制不死 | `desperation && attackTimer<90` 时每刻把 `life` 锁回 1 | 3294-3297 |
| 重力 | 默认 `0.5`；NebulaForm 时不施重力；`velocity.Y` 上限 `gravity*20` | 953 / 2640-2643 |
| 水平阻尼 | 每刻 `velocity.X *= 0.93`，`abs(vX)<0.1` 归零 | 929-933 |
| 旋转（史莱姆态） | `rotation = velocity.X * -0.01` | 2644 |
| 旋转（星云态） | `abs(vX)<0.5` 时 `rotation += ToRadians(12 * direction)`，否则 `rotation += vX * 0.05` | 2669-2676 |

### 1.5 绘制要点（`PreDraw`，537-609）

| 项 | 数值 | 行号 |
|---|---|---|
| 挤压拉伸 | `stretch = abs(velocity.Y) * 0.015 + addedStretch`，上限 `0.5`；scale 为 `(1-stretch, 1+stretch)*NPC.scale` | 565-571 |
| 残影 | `CalamityClientConfig.Afterimages` 开关；12 段 oldPos，`color = GetAlpha * (12-i)/12 * 0.5` | 572-574 / 631-650 |
| 史莱姆体 | 主贴图 `Astrageldon`（7 帧），叠加 `AstrageldonRocks` 的两层（`initiateSecondPhaseVisual < 360` 时显示），origin 为 `(w/2, h+4)` | 588-593 |
| NebulaForm | 不画本体，改 `DrawNebula()`：贴图 `AstrageldonNebula`，两层（`*0.5` scale×1.05 / `*1.0` scale×0.9），颜色 `alpha = Lerp(drawColor, Black, (1000-targetOrbitalRange)/1000)`（仅 phase 15） | 595-599 / 485-500 |
| 光环 | 二阶段每帧 `ManageHalo()` | 583 |
| 冲刺/坠落指示 | `DrawSlamTelegraph()`（`slamTimer != -1`）、`UpdateTelegraphs(draw:true)` | 559-562 / 578 |
| 光照 | `Lighting.AddLight(Center, (131,62,196)/255)` | 2744 |

`DrawSlamTelegraph`（686-749）：`delay = 二阶段?35:50`；`percent = 1-|slamTimer|/delay`；贴图 `DashIndicator`，横向 36 层（每层 2 px 偏移），竖直方向 `i ∈ [-num, 50)`，`alphaMult` 每层 `+0.008`（起点 `0.01`）；命中方块(`isTile`)处跳过绘制（671-684）。

### 1.6 关键攻击的运动参数（与生成物相关）

| phase | 关键数值 | 行号 |
|---|---|---|
| 起手 | `attackTimer=-120, attackPhase=-1` | 960-961 |
| 0 JumpSlam | 起跳 `Speed2 = 14.5 + attackTimer2*4`；`vX = cos(rot)*Speed2*-1.25`，`vY = sin(rot)*Speed2*-0.75 - 9 - attackTimer2*3`；`vX *= 1.05`/刻 | 1186-1193 |
| 1 ScatterStars | 悬停 `speed3=16`，追踪目标点 `Player.Center+(0,-360)`；`veloScale = 0.175+0.825*flyingMult`，`flyingMult = clamp(timer/30,0,1)`；每 **100 刻** 一轮 `SummonStars`（`amt5 = 专家8/普通6 + 复仇2 + 死亡2 + (4*(1-SpeedUp())+0.5)`） | 1230-1307 |
| 2 SlimeRain | 上升悬浮 `timer<240`、`240` 刻落砸、`405` 刻后转 phase 0/3 | 1359-1405 |
| 3 RevengeanceJumpSlam | `timer==0` 先 `SummonStars(amt6)`；`Speed4 = 14.5+timer2*5`，`vX *= 1.045` | 1468-1499 |
| 4 OrbitingRocks | 绕玩家半径 `380 + 60*sin`，`sin = sin(ToRadians(timer*2.5))`；`timer==60` 时 `SummonRocks(20)`；持续 540 刻 | 1585-1596 |
| 6→7 | `SummonSlimer(4/专家6)`（`timer==-30`），随后 `SwapPhase(7)` | 1826-1871 |
| 7 AscendWithDashes | `dashSpeed=1`；`attackTimer2<16 && timer%15==0` 时加一条 dash；`AddDash(start,end,dashSpeed)`；`ResolveAll()` 完成 → phase 8 | 1879-1909 |
| 8 Phase2StarSlam | 砸地时 `amt2 = 6/专家8`，`speed2 = -(2+0.3*cycles)` 上限 4 → 6~8 颗 `AstrageldonStar2` 放射 | 1054-1072 |
| 9 CrossStarDash | `dashSpeed=1.34`；`timer<50` 时 `vX += dir*sin(ToRadians(timer*9))*2.4`；`50~110` 时 `vX += dir*4.5`；110 刻瞬移到 `borderCenter - 归一*(targetOrbitalRange+240)`；循环 140 刻，`timer%140==80` 时 `AddDash(center, center+toPlayer*960, 1.34)`；`timer%3==0` 且 `timer%140<12` 时生成 `AstrageldonStar3`；`timer%140>=110` 用 `ResolveAll(ref savedVelocity, 1, 3f)` | 2029-2124 |
| 10 ShrinkArenaAndStarShotgun | 每 30 刻（`-5*cycles`，下限 10）生成 `NebulaResidue`（`timer2<=120`）；`timer2==150` 生成 `8/6` 对 `AstrageldonStar2`；`timer>=540` 后 `attackTimer3` 固定在 90° | 2184-2227 |
| 11 SpiralLaser | `timer==150` 生成 8 道 `SpiralLaser`；`timer>=210` 后本体自转 `-10° * clamp((timer-210)/180, 0.2, 2)`；每 `240-30*cycles`（下限 120）刻放射 8 颗 `AstrageldonStar2`（`timer 340~1270`）；`timer>=1450` 转 phase 12 | 2296-2347 |
| 12 SlimerSlam | `SummonSlimer(min(amt3,12))`，`amt3 = 6+max(cycles-1,0) +专家2 +死亡1` | 1083-1092 |
| 13 DashAndMoveArenaHorizontally | 同 phase 7 但起终点沿 X 移动（每条 dash 后 `attackTimer3 += 200; borderCenter.X -= 200*dir`） | 2449-2491 |
| 14 RapidlyFireStars… | 前段（`attackTimer3<=1`）：`speed13 = 10-2*cycles`（下限 3），每 `speed13` 刻 1 发 `AstrageldonStar4`，`total = 15+5*cycles`（上限 50）；后段：`amount = 3+cycles`（上限 6），`totalTime = 220+40*amount`，每 `totalTime/amount` 刻 5(+复仇1) 发 `NebulaResidueHB`；`timer==-61` 时 `amt13 = 4/专家6 + cycles`（上限 12）放射 `AstrageldonStar2` | 2493-2635 |
| 15 Desperation | 缩圈：`targetOrbitalRange -= 0.4 + attackTimer2*0.02`；从 `borderCenter+(0,-(range+200))` 环带生成 `HostileAsteroid`，间隔 `24+2n` 刻 | 1733-1800 |
| 绝望收尾 | `targetOrbitalRange` 到 `-136` 后 8 颗 `NebulaHitbox`（伤害 0） | 1784-1790 |

### 1.7 阶段过场（`secondPhaseTransition` 分支，1648-1727）

| 项 | 数值 | 行号 |
|---|---|---|
| 边界收缩 | `borderOrbitalRange = Lerp(borderOrbitalRange, targetOrbitalRange, 0.08)` | 1653 |
| 抖动 | `delay = 8 + (1-Smult)*12`，`Smult = clamp(initiateSecondPhaseVisual/240, 0, 1)` | 1655-1665 |
| 缓慢漂浮 | `timer>0` 段：`gravity = -0.1`，`velocity *= 0.3`，`position.Y += 3*sin(ToRadians((timer-120)*12))` | 1686-1701 |
| 转二阶段 | `timer==360` → 8 颗 `AstraRock`；`timer>=420` → `SwapPhase(6); life = lifeMax` | 1702-1725 |

---

## 2. AstrageldonLaser

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\AstrageldonLaser.cs`（注意：虽然行为是「敌方弹幕」，但类定义在 Boss 目录下）

### 2.1 SetDefaults

| 字段 | 值 | 行号 |
|---|---|---|
| `width / height` | `12 / 12` | 28-29 |
| `damage` | 继承生成者传入值（Boss 侧为 `GetBaseDamage()/2`） | — |
| `knockBack` | 继承（生成处传 Boss 的 `knockBack`，实际 0） | — |
| `penetrate` | `-1`（无限穿透） | 27 |
| `timeLeft` | `90`（1.5 s） | 31 |
| `friendly / hostile` | `false / true` | 22 / 25 |
| `tileCollide` | `false` | 23 |
| `ignoreWater` | `true` | 24 |
| `aiStyle` | `-1` | 26 |
| `alpha` | `255`（每刻 `-20` → 约 13 刻淡入） | 30 / 90-97 |
| 帧 | `Main.projFrames = 3`，`frameCounter % 6` 循环 | 17 / 84-88 |
| `scale` | 每刻强制 `1.3` | 83 |

### 2.2 AI 运动（80-115）

| 项 | 数值 | 行号 |
|---|---|---|
| 朝向 | `rotation = velocity.ToRotation()` | 89 |
| 加速度 | `velocity += normalize(velocity) * 0.7`（纯前向加速，`0.7 px/刻²`） | 98-99 |
| 初速 | 由生成者给 `5 px/刻` | 见 2.5 |
| 90 刻总位移 | `5t + 0.35t²` → 约 `3285 px ≈ 205 tile` | 推算 |
| 尾焰尘 | 每刻掷骰 2 次，各 50% 概率生成 `MonoDust2`，位置 `Center + velocity*(i*0.5)`（i=0,1） | 100-114 |

### 2.3 伤害

| 项 | 数值 | 行号 |
|---|---|---|
| 判定框 | 覆写为固定 `12×12` 居中（忽略 `scale`） | 34-38 |
| 命中附加 | `AstralBlight` **240 刻** | 75-78 |
| 伤害节奏 | 未自定义 cooldown，由玩家受伤无敌帧决定；弹幕本身每刻检测 | — |

### 2.4 绘制 / 前摇

| 项 | 数值 | 行号 |
|---|---|---|
| 前摇虚线贴图 | `CatalystMod/NPCs/Boss/Astrageldon/DashIndicator` | 42 |
| 前摇段数 | `i = 1..9`，每段端点 `Center + velocity * i * 5` | 44-47 |
| 前摇 alpha | `counter/90 - (i-1)/10`（越远越淡，0→1） | 46 |
| 虚线尺寸 | `Rectangle(0,0,2,length)`，2 px 宽 | 49 |
| 虚线颜色 | `Color(255, 219, 79) * alphaMult` | 49 |
| 本体 alpha | `GetAlpha = White * (255-alpha)/255` | 54-57 |
| 死亡尘 | 20 颗 `MonoDust2`，`scale 1.5`，`alpha 100`，色 `(255,233,2,100)`，沿 `oldVelocity` 方向 `+0.4` | 59-73 |

### 2.5 生成来源

| 生成者 | 数量 / 参数 | 行号 |
|---|---|---|
| `AstrageldonStar`（星体） | 3 发，`i=-1..1`，`spread=4°`（`getGoodWorld` 时 `8°`），速度 `5` | `AstrageldonStar.cs:185-196` |
| `AstrageldonStar3` | 4 发，`i=0..3`，`velocity = (2,0).RotatedBy(ToRadians(i*90)+ai[0]) * 5` | `AstrageldonStar3.cs:170-175` |

---

## 3. SpiralLaser

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\SpiralLaser.cs`

### 3.1 SetDefaults

| 字段 | 值 | 行号 |
|---|---|---|
| `width / height` | `8 / 8`（仅占位，判定用自定义线段） | 42-43 |
| `timeLeft` | `1260`（21 s） | 44 |
| `penetrate` | `-1` | 45 |
| `hostile / friendly` | `true / false` | 46-47 |
| `tileCollide / ignoreWater` | `false / true` | 48-49 |
| `alpha` | `100` | 50 |
| `ShouldUpdatePosition` | `false`（位置全部手写） | 53-56 |
| 常量 | `innerRotation = 135f`、`outerRotation = 270f`（**均未被使用**，实际用字面量 `135f` / `0.6f`） | 23 / 25 |

### 3.2 运动（112-175）

| 项 | 数值 | 行号 |
|---|---|---|
| 归属判定 | `ai[1]` 指向的 NPC 必须是 `Astrageldon` 且 `parent.ai[0] != 15`，否则 `Kill()` | 118-145 |
| 旋转方向 | `direction = ai[0] % 2 * 2 - 1`（±1） | 118 |
| 起始角 | `ogRotation = 生成时 velocity.ToRotation()`（只取一次） | 124-127 |
| 环绕半径 | `Center = parent.Center + normalize(velocity) * 80`（**80 px = 5 tile**） | 140 |
| 前摇（0-180 刻） | 内圈角 `nextRotation = -67.5° * clamp((counter-90)/90, 0, 1)`；`velocity = (1,0).RotatedBy(ogRotation + ToRadians(lastDegreesInner*dir))` | 129-139 |
| 外圈角（0-180 刻） | `degrees = 135° * clamp((counter-90)/90, 0, 1)`（`Laser()` 的弯曲总角度） | 148-156 |
| 光束采样 | `i = 0..180` 共 **181 点**，步长 `7.2 px` → 折线长约 **1296 px ≈ 81 tile** | 206-213 |
| 波长/抖动 | `sin = sin(ToRadians(ai[0]*10 + i*3))`，振幅 `6 * mult`，但 `mult` 恒为 0 → **实际无抖动** | 208-212 |
| 激活后（>180 刻） | `rotateCounter++`（上限 90），`mult3 = rotateCounter/90`；内圈角每刻 `-= 0.6 * mult3` → **0.6°/刻（36°/s），转一圈 600 刻**；外圈角固定为 `lastDegrees` | 159-167 |
| 自转判据 | `active` 首次为 true 时播放音效 `AstrageldonSpiralBeam` | 168-172 |
| 光照 | `AddLight(Center, 0.8*(255-alpha)/255)` | 174 |

### 3.3 伤害（182-202）

| 项 | 数值 | 行号 |
|---|---|---|
| 生效条件 | 仅 `active == true`（即 **前 180 刻（3 s）零伤害**） | 184 |
| 收束 | `timeLeft < 30` 时 `hostile = false` | 114-117 |
| 判定方式 | 遍历全部 `trailPos`，每点一个 `width × width` 方块 | 192-199 |
| 判定宽度 | `width = 8 + 14 * degrade`，`degrade = clamp(timeLeft/240, 0, 1)` → **22 px → 8 px** 随剩余时间收细 | 186-191 |
| 命中附加 | `AstralBlight` 240 刻 | 177-180 |

### 3.4 绘制（58-110 / 228-232）

| 阶段 | 内容 | 行号 |
|---|---|---|
| 前摇（`!active`） | 沿 `trailPos` 的虚线，贴图 `DashIndicator`，`Rectangle(0,0,length,2)`（2 px），`alphaMult = counter/90 - (i-1)/count`（≤1），颜色 `(255,233,2)` | 60-78 |
| 激活后 | 用本体贴图逐段填充：`degrade = clamp(timeLeft/240,0,1)`；`oscillation = 0.25*cos(ToRadians((j-counter)*10))`；`scale = projectile.scale + (255-alpha)/255`；横向拉伸 `(1, (1+oscillation)*degrade) * scale`，最终再 `*1.5`；颜色 `(255,233,2,0) * (255-alpha)/255` | 85-108 |

### 3.5 生成来源

| 生成者 | 参数 | 行号 |
|---|---|---|
| `Astrageldon.AI` phase 11，`attackTimer==150` | 8 道，`n=0..7`，`velocity = (0,1).RotatedBy(ToRadians(n*45)) * 2`（只用作方向）；`ai[0] = totalPhase2Cycles`（决定旋向），`ai[1] = npc.whoAmI` | `Astrageldon.cs:2296-2306` |

> 8 束各偏 45° 起始角，配合 `direction = cycles%2*2-1` 产生正反交替螺旋。

---

## 4. StarLaser

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\StarLaser.cs`

### 4.1 SetDefaults

| 字段 | 值 | 行号 |
|---|---|---|
| `width / height` | `8 / 8` | 27-28 |
| `timeLeft` | `40`（0.67 s） | 29 |
| `penetrate` | `-1` | 30 |
| `hostile / friendly` | `true / false` | 31-32 |
| `tileCollide / ignoreWater` | `false / true` | 33-34 |
| `alpha` | `100` | 35 |
| 常量 | `MaxLength = 120`、`degradePoint = 50` | 16 / 18 |
| `ShouldUpdatePosition` | `false` | 38-41 |

### 4.2 曲线（`Laser()`，72-124）

| 项 | 数值 | 行号 |
|---|---|---|
| 采样点 | `i = 0..120` 共 **121 点** | 75 |
| 步长 | `unit = (12,0).RotatedBy(velocity.ToRotation())` → 每点前进 **12 px** → 总长 **1440 px ≈ 90 tile** | 83 |
| 横向正弦 | `sin = sin(ToRadians(ai[0]*15 + i*3))`，振幅 `6 * mult`，`mult = clamp(i/30, 0, 1)` → 前端 30 点内振幅线性长到 **6 px** | 77-84 |
| 相位推进 | 每刻 `ai[0] += 1` → 波形沿光束以 `15°/刻` 相位速度流动 | 60 |
| 收尾 | `i > 70` 后绘制宽度 `size -= (i-70)/50` → 尾端收细 | 104-107 |

### 4.3 生命周期 / 伤害

| 项 | 数值 | 行号 |
|---|---|---|
| 透明度 | `alpha += 4`/刻（从 100 起） | 51 |
| 关伤阈值 | `alpha > 140` → `damage = 0`（`alpha` 从 100 每刻 +4，故**约第 11 刻起无伤害**） | 52-55 |
| 消失 | `alpha > 250` → `Kill()`（约第 38 刻） | 56-59 |
| 判定方式 | `Colliding` 直接调用 `Laser(targetHitbox)`；**每 3 个采样点**生成一个 `8×8` 判定框（`i%3==0`） | 67-70 / 85-94 |
| 伤害节奏 | 未自定义 cooldown，受玩家无敌帧限制 | — |
| 命中附加 | `OnHitPlayer` **为空**（不加 debuff） | 63-65 |

### 4.4 绘制（98-110 / 126-130）

| 项 | 数值 | 行号 |
|---|---|---|
| 贴图 | 本体 `TextureAssets.Projectile[Type]`（逐点平铺） | 102 |
| 宽度曲线 | `size = 0.6 + 0.8 * (timeLeft/40)`；`i>70` 再减 `(i-70)/50` | 103-107 |
| 缩放 | `new Vector2(size, 2f)`（纵向恒定 2 倍厚度） | 108 |
| 旋转 | `velocity.ToRotation() + π/2` | 108 |
| 颜色 | `(255,233,2) * (255-alpha)/255` | 74 |
| 前摇尘 | `runOnce` 首帧沿光束生成尘（`i>10 && i%5==0`，`MonoDust2`，`scale 1.5`） | 45-49 / 111-121 |

### 4.5 生成来源

| 生成者 | 参数 | 行号 |
|---|---|---|
| `AstrageldonStar3.PreAI`（`counter>=70`，条件是 `parent.type == Astrageldon` 或 `CalamityWorld.death`；否则改用 `AstrageldonLaser`） | 4 发，`velocity = (2,0).RotatedBy(ToRadians(i*90) + ai[0]) * 5` | `AstrageldonStar3.cs:166-175` |

---

## 5. NebulaPlatform

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\NebulaPlatform.cs`（**普通类，非 ModProjectile；持有纹理、位置、淡入计数**）

### 5.1 字段

| 字段 | 值 | 行号 |
|---|---|---|
| `delay`（常量） | `48` | 13 |
| `counter` 初值 | `12` | 17 / 28 |
| `Size` | `28`（构造时赋值，源码中未被使用） | 27 |
| `active` | `true` | 19 |
| `hasReachedTop` | `false` | 21 |
| 贴图 | `CatalystMod/NPCs/Boss/Astrageldon/NebulaPlatform`（`ImmediateLoad`） | 29 |

### 5.2 更新逻辑（33-57）

| 条件 | 行为 | 行号 |
|---|---|---|
| `platformY != position.Y && hasReachedTop`（Boss 已离开该高度） | `counter--`（每刻 1）；到 0 时 `active = false` → 淡出并移除 | 35-45 |
| 否则 | `position.X = newX`（跟随 Boss 水平位置）；`counter += 5`，上限 48 | 47-55 |
| 淡入耗时 | `(48-12)/5 = 8` 刻（约 0.13 s） | 推算 |
| 淡出耗时 | 48 刻（0.8 s） | 41-44 |

### 5.3 绘制（59-65）

| 项 | 数值 | 行号 |
|---|---|---|
| 颜色 | `InterstellarCorruptionBorderColorReverse` = `Color(112,51,166)`，`A = 0` | 61-62 |
| alpha | `counter / 48` | 63 |
| origin | `(texture.Width/2, 0)` → **以顶端为锚点向下延伸**的柱状光带 | 64 |
| 缩放 | `1`（不缩放） | 64 |

### 5.4 生成 / 归属

| 项 | 说明 | 行号 |
|---|---|---|
| 创建 | Boss 二阶段下落且满足 `invisiblePlatformHeight` 判据时创建；`nebulaPlatforms` 中同高度只留一个 | `Astrageldon.cs:2702-2711` |
| 高度赋值 | `invisiblePlatformHeight = NPC.position.Y + NPC.height` | `Astrageldon.cs:2706` |
| 判定副作用 | 命中平台后 `NPC.velocity.Y = 0`、`canContinue = false`（即“隐形平台”） | `Astrageldon.cs:2712-2713` |
| 更新 / 移除 / 绘制 | `UpdateTelegraphs`：`telegraph2.Update(invisiblePlatformHeight, NPC.Center.X)` / `!active` 移除 / `Draw()` | `Astrageldon.cs:793-808`（update 806、remove 800-803、draw 795-799） |
| 仅在二阶段绘制 | `PreDraw` 中 `if (secondPhase) UpdateTelegraphs(draw:true)` | `Astrageldon.cs:576-578` |

---

## 6. NebulaResidue

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\NebulaResidue.cs`（冲刺残留的「解体碎块」）

### 6.1 SetDefaults

| 字段 | 值 | 行号 |
|---|---|---|
| `width / height` | `34 / 34` | 23-24 |
| `timeLeft` | `60`（可被 `ai[0]` 覆盖） | 26 |
| `penetrate` | `-1` | 28 |
| `hostile / friendly` | `true / false` | 27 / 25 |
| `tileCollide / hide` | `false / false` | 30-31 |
| `netImportant` | `true` | 29 |
| 拖尾 | `TrailingMode = 0`，`TrailCacheLength = 8` | 17-18 |

### 6.2 AI（52-64）

| 项 | 数值 | 行号 |
|---|---|---|
| 生命周期覆盖 | `runOnce`：`ai[0] > 60` 时 `timeLeft = (int)ai[0]` | 54-61 |
| 加速 | `velocity += normalize(velocity) * 0.175`（`0.175 px/刻²`，纯前向） | 62 |
| 自转 | `rotation += velocity.X * 0.006` | 63 |
| 无重力 / 无追踪 | — | — |

### 6.3 伤害

| 项 | 数值 | 行号 |
|---|---|---|
| 判定 | 默认矩形 `34×34`（未覆写） | — |
| 伤害节奏 | 未自定义 cooldown，受玩家无敌帧限制 | — |
| 死亡尘 | 15 颗 `MonoDust2`，半径速度 `rand(0.4, 5)`，`scale ×3`，色 `(112,51,166)` | 34-50 |

### 6.4 绘制（66-83）

| 项 | 数值 | 行号 |
|---|---|---|
| 拖尾 | 8 段，`scale = 1 - 0.02*i`；`color = GetAlpha(White,A=180) * (8-i)/8 * 0.5` | 72-78 |
| 主体两层 | 后半帧（`y = H/2`）用「腐蚀紫」描边 + 前半帧用 `White`；origin `(W/2, H/4)` | 79-81 |

### 6.5 生成来源

| 生成者 | 数量 / 速度 | 行号 |
|---|---|---|
| `Astrageldon.AI` phase 10 喷吐（`timer2 % fireCooldown == 0`，`fireCooldown = 30-5*cycles`，下限 10；仅 `timer2<=120`） | 1 颗/次，位置 `Center + toPlayer*80`，速度 `toPlayer*2`（**2 px/刻**），`ai[0]=200`（寿命 200 刻） | `Astrageldon.cs:2184-2201` |
| `ResolveAll` 冲刺中 phase 7（`lerp 0.2~0.8`） | 每刻 2 颗，位置 `Center + (-0.33·vx·i, -0.33·vy·i) + rand圆形(80,80)`，速度 `randCircular(4,3) - (vx*0.015, 3.5 或专家 5.5)` | `Astrageldon.cs:2830-2842` |
| `ResolveAll` 冲刺中 phase 13 | 同上，但竖直分量换成 `(5.5/3.5)*direction2` | `Astrageldon.cs:2843-2856` |
| `NebulaResidueHB.OnKill` | 4 颗，`ai[1]+i*90` 十字方向，位置 `Center + 单位向量*5`，速度 `(0,4).RotatedBy(...)`；`ai[0]=180`（寿命 180 刻） | `NebulaResidueHB.cs:52-59` |

---

## 7. NebulaResidueHB

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\NebulaResidueHB.cs`（大碎块，死亡裂成 4 颗小碎块）

### 7.1 SetDefaults

| 字段 | 值 | 行号 |
|---|---|---|
| `width / height` | `34 / 34` | 27-28 |
| `timeLeft` | `60`（实际由 `ai[0]` 覆盖） | 30 |
| `penetrate` | `-1` | 32 |
| `hostile / friendly` | `true / false` | 31 / 29 |
| `tileCollide` | `false` | 34 |
| `netImportant` | `true` | 33 |
| 拖尾 | `TrailCacheLength = 8` | 21-22 |
| 私有字段 | `slowDown = 5f` | 17 |

### 7.2 AI（62-91）

| 项 | 数值 | 行号 |
|---|---|---|
| 寿命 | `runOnce`：`ai[0] > 60` → `timeLeft = (int)ai[0]`；否则 `ai[0] = timeLeft`（记住总寿命供绘制用） | 64-76 |
| 缩放 | `scale = 1` 固定 | 74 |
| 自旋角 | `ai[1] += slowDown * sign(ai[1])`（每刻 5°，**初始 ±360 内随机、方向同号** → 自旋加速） | 88 |
| 自旋衰减 | `slowDown *= 0.99`（每刻） | 89 |
| 速度阻尼 | `velocity *= 0.99` | 90 |
| 尘 | 每刻 1 颗 `MonoDust2`，`scale ×1.8*scale`，色 `(112,51,166)` | 77-87 |

### 7.3 伤害 / 死亡散射

| 项 | 数值 | 行号 |
|---|---|---|
| 判定框 | 覆写为 `(34*scale)²` 居中 | 38-42 |
| 死亡音效 | `SoundID.Item91`，`Volume ×1.1`，`Pitch +0.5` | 46-51 |
| 死亡散射 | 4 颗 `NebulaResidue`，方向 `ai[1] + i*90`，位置 `Center + 单位向量*5`，速度模 4，`ai[0]=180` | 52-59 |

### 7.4 绘制（93-120）

| 项 | 数值 | 行号 |
|---|---|---|
| 拖尾 | 8 段，`scale = 1-0.02*i`，alpha `*0.5` | 100-106 |
| 主体两层 | 同 `NebulaResidue` | 107-109 |
| 4 颗子碎块（预告位置） | 半径 `24 * (1 - timeLeft/ai[0])`（**从中心长到 24 px**），角度 `ai[1] + j*90`，`scale ×0.7` | 110-118 |

### 7.5 生成来源

| 生成者 | 数量 / 速度 | 行号 |
|---|---|---|
| `Astrageldon.AI` phase 14 后半段（`attackTimer % (totalTime/amount) == 0`） | `amountP = 5`（复仇 +1），每颗：`velocity = (toPlayer*23).RotatedBy(ToRadians(rand(-30-2n, 30+2n))) + rand圆形(6,6)*(1+0.15n)`，再 **`*0.5`**（→ 约 11.5 px/刻）；位置 `Center + toPlayer*24`；`ai[0] = totalTime/amount/4 + rand(20,40)`，`ai[1] = rand(-360,360)` | `Astrageldon.cs:2598-2613` |

---

## 8. DashTelegraph

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\DashTelegraph.cs`（**普通类：冲刺「伪装」指示条 + 本体的位移插值路径**）

### 8.1 字段

| 字段 | 值 | 行号 |
|---|---|---|
| `delay`（常量） | `40`（0.67 s） | 22 |
| `counter` 初值 | `40` | 63 |
| `resolveSpeed` | `2`（可被 `Resolve()` 覆盖：常用 `4`、`3`、`5`） | 20 / 54-58 |
| `initializeSpeed` | `1`（构造传入，phase 7 = `1`、phase 9 = `1.34`、phase 13 = `1`） | 28 / 60-67 |
| `resolving` / `resolveFinish` | `false / false` | 24-26 |
| 贴图 | `CatalystMod/NPCs/Boss/Astrageldon/DashIndicator2` | 62 |

### 8.2 状态机（30-52）

| 状态 | 行为 | 行号 |
|---|---|---|
| 预警（`resolving=false`） | `counter -= initializeSpeed`（每刻 1 或 1.34），到 0 停 → **40 刻 / 约 30 刻** | 44-51 |
| 冲刺（`resolving=true`） | `counter -= resolveSpeed`（每刻 3/4/5），到 `-40` 时 `resolveFinish = true` → **10~13 刻** | 32-43 |
| 冲刺位移 | Boss 本体做 `Center = Lerp(start, end, pow(abs(counter/-40), 1.15))`（见 8.5） | 2817-2826 |

### 8.3 绘制（69-142）

| 项 | 数值 | 行号 |
|---|---|---|
| `percent` | `1 - abs(counter)/40`（0→1 推进） | 71 |
| `half` | `20`（中段门限） | 72 |
| `percent2` | `clamp(1-(abs(counter)-20)/20, 0, 1)`；`counter<0` 时直接用 `percent` | 73-78 |
| 颜色 | `Color(131, 62, 196, 0)`（**紫**，非金黄） | 83 |
| 条带层数 | `i = -10..10`（21 条），`vOffset = 归一方向 * texture.Height * i`，`i>0` 时再加整条长度 | 88-111 |
| 中线 | `i == 0` 时 `yScale = dist + 1`（`dist = 长度/texture.Height`），即一条拉长的实体中线 | 102-106 |
| 横向厚度 | 每侧 `k = 0..31`，偏移 `k*2 px`，`alphaMult` 从 `0.01` 每层 `+0.008`（≈0.01→0.266） | 112-123 |
| 两端羽化 | 第二组循环用 `percent` 收紧范围（`fromCenter*2*percent`） | 124-140 |
| alpha 权重 | `alphaMult * percent2 * (baseAlpha/10)` | 121 |

### 8.4 更新 / 绘制调用点

| 项 | 行号 |
|---|---|
| 添加：`Astrageldon.AddDash(start, end, initSpeed)` → `nebulaDashes.Add(new DashTelegraph(...))`（服务端同步 `previousDashPostion`，客户端在 `PreAI` 重建） | `3207-3222` / `907-914` |
| 更新 / 移除：`UpdateTelegraphs`（`resolveFinish` 时移除） | `775-792` |
| 绘制：`UpdateTelegraphs(draw:true)` | `780-783`（调用处 `578`） |
| phase 7 生成参数 | 每 15 刻一条（`attackTimer2<16`）：`start = borderCenter + (0, targetOrbitalRange-160*amt + attackTimer3) + (targetOrbitalRange+160, 80*±1)`，`end` 镜像（`1887-1903`） |
| phase 9 生成参数 | `AddDash(NPC.Center, NPC.Center + toPlayer*960, 1.34)`，循环 140 刻，在 `timer%140==80` 时（`2088-2093`） |
| phase 13 生成参数 | 同 phase 7 但 `start/end` 沿 X 轴镜像（`1000+` 行程，`2455-2474`） |
| 冲刺期间的判定 | `lerp ∈ [0.2, 0.8]` 时才喷 `NebulaResidue` / `AstrageldonStar3`（`2830`, `2843`, `2857`） |

### 8.5 ResolveAll 位移（2797-2867）

| 项 | 数值 | 行号 |
|---|---|---|
| 触发 | 首次调用只置 `resolveDash = true; resolveSpeed = 参数`，并播 `Item91`（`Vol ×1.4, Pitch -0.5`） | 2799-2816 |
| 插值 | `lerp = abs(counter/-40)`；`resolveDirection == -1` 时取 `1-lerp`；再 `pow(lerp, 1.15)` | 2819-2824 |
| 位置 | `NPC.Center = Lerp(startPosition, endPosition, lerp)` | 2825-2826 |
| 旋转 | `NPC.rotation -= velocityX * 0.04` | 2862 |
| 绝望期特殊 | `attackPhase == 15` 时**不做插值**，改用 `ResolveAll(1, 5f)`（速度 5） | 1799 / 2817 |

---

## 9. AstrageldonHaloManager

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\AstrageldonHaloManager.cs`（普通类，Boss 身后光晕拖尾）

### 9.1 字段

| 字段 | 值 | 行号 |
|---|---|---|
| `trailPos` | `Vector2[50]`（50 段历史） | 17 |
| `scale` | `1`（由 Boss 每刻写入 `clamp(initiateSecondPhaseVisual/180, 0.2, 1)`） | 19 / `Astrageldon.cs:504-509` |
| `nebula` | 对应 Boss 的 `NebulaForm` | 21 / 510 |
| 贴图 | `CatalystMod/NPCs/Boss/Astrageldon/AstrageldonHalo` | 89 |

### 9.2 采样（`Astrageldon.ManageHalo`，502-535）

| 项 | 数值 | 行号 |
|---|---|---|
| 每刻采样次数 | 史莱姆态 `1`，`NebulaForm` 时 `3` | 507-514 |
| 轨道半径 | `64 * clamp(initiateSecondPhaseVisual/180, 0.05, 1)` | 518 |
| 角速度 | `8° * haloCounter`（`haloCounter` 每样本 +1） | 517-518 |
| 史莱姆态压扁 | `circular.Y *= 0.3; circular.Y -= 96` | 519-523 |
| 星云态 | `circular *= 1.5` | 526 |
| 朝向倾斜 | `rotate = clamp((NPC.Center.X - Player.Center.X) * 0.02, -15, 15)` 度 | 528-530 |

### 9.3 绘制（48-85）

| 项 | 数值 | 行号 |
|---|---|---|
| 段缩放 | `scale = this.scale * (0.2 + 0.8*(50-k)/50)`（尾端 0.2×，头端 1.0×） | 58 |
| 史莱姆态额外压扁 | `mult = 0.7 - 0.3 * trailPos[k].Y / 20` | 64-68 |
| 分段密度 | 每段填充 `length / (2.5*scale)` 个贴图 | 72-73 |
| 颜色 | `Color(100,100,100,0) * (50-k)/50` | 75 |
| 抖动 | 每个 sprite 额外 `randCircular(1,1)` 偏移 | 79 |
| 重叠绘制 | 每个位置画 **3 次** | 77-80 |
| 提前退出 | 任何 `trailPos[k] == Zero` 时立即 return（即初始化期不画） | 60-63 |

### 9.4 其他

| 项 | 说明 | 行号 |
|---|---|---|
| 数据搬运 | `cataloguePos()`：整体后移一格，最前面写入当前位置 | 23-32 |
| 死代码 | `checkPos()` 计算了 `iterator` 但从不使用（空逻辑） | 34-46 |
| 创建 | `Astrageldon.PreAI`：`haloManager == null` 时 `new AstrageldonHaloManager(NPC.Center, Vector2.Zero)` | `Astrageldon.cs:874-877` |
| 调用 | 二阶段 `PreDraw` → `ManageHalo()` | `Astrageldon.cs:583` |

---

## 10. AstraRock

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\AstraRock.cs`

### 10.1 SetDefaults

| 字段 | 值 | 行号 |
|---|---|---|
| `width / height` | `32 / 36` | 39-40 |
| `timeLeft` | `960`（16 s） | 42 |
| `penetrate` | `-1` | 45 |
| `hostile / friendly` | `true / false` | 43 / 41 |
| `alpha` | `255`（每刻 `∓5` 淡入淡出） | 44 / 147-158 |
| `tileCollide` | `false`（第 270 刻后置 `true`） | 48 / 223 |
| `hide` | `true`（由 Boss 的 `PreDraw` 手动分前后绘制） | 47 |
| `netImportant` | `true` | 46 |
| `extraUpdates` | `1`（`AI()` 首帧后强制 `0`） | 49 / 200 |
| 帧 | `Main.projFrames = 10`，帧随机 | 34 / 131 |
| 拖尾 | `TrailCacheLength = 8`，`TrailingMode = 0` | 32-33 |
| 私有 | `scaleMax = 1.25`（`knockBack == -2f` 时 `1.0`），`bonusRotation = 270f` | 24 / 133-136 |

### 10.2 两种模式（由 `knockBack` 区分）

| 模式 | 判据 | 行为 | 行号 |
|---|---|---|---|
| 环绕型 | `ai[1] >= 0 && knockBack >= 0` | 绕 Boss 公转，半径随时间增长 | 159-194 |
| 坠击型 | `knockBack < 0`（`-1` 或 `-2`） | 飞向 `(ai[0], ai[1])` 目标点，270 刻后反弹上抛 | 198-232 |

### 10.3 环绕型运动（159-194）

| 项 | 数值 | 行号 |
|---|---|---|
| 半径 | `R = 40 + min(secondCounter*4.4, 680)` px（**40 → 720 px，即 2.5 → 45 tile**） | 167-172 |
| 角速度 | `ai[0] + secondCounter*2`（度，每 `secondCounter+1` 转 2°） | 172 |
| 摆动 | `cos = cos(secondCounter/45 * π)`，周期 90 tick；`ai[0] < 0` 时压 `Y *= 0.15*cos`，否则压 `X *= 0.15*cos` | 165、177-184 |
| 附加旋转 | 再 `RotatedBy(ToRadians(90 + secondCounter*0.75))` | 185-186 |
| 中心偏移 | `Center = parent.Center + (0, -16)` | 187 |
| 绘制分层 | `rotate.Y > 0` 时 `drawInFront = true`（画在 Boss 前） | 173-176 |
| 缩放 | 每刻 `scale += 0.05/(extraUpdates+1)`，上限 `scaleMax` | 139-146 |
| 旋转 | `rotation = (oldPos[0].X - position.X) * 0.05` | 161 |

### 10.4 坠击型运动（198-232）

| 项 | 数值 | 行号 |
|---|---|---|
| 计时 | `counter` 每刻 `+6`（`AI`） | 201 |
| 归位 | `toTheLocation = normalize((ai0,ai1) - Center) * circular.Y`，其中 `circular = (3 + counter*0.01, 0).RotatedBy(ToRadians(counter))` | 203-205 |
| 前 270 刻 | `velocity *= 0.9875`；`Center -= toTheLocation`；`rotation = toTheLocation.ToRotation() + bonusRotation`；`bonusRotation = (bonusRotation-0.3)*0.98`（270° 收敛到 0） | 206-220 |
| 270 刻后 | `tileCollide = true`；首帧 `velocity = normalize(toTheLocation)*-24 + randCircular(5,5)`；`velocity.Y += 0.02`（重力）；`rotation = velocity.ToRotation()` | 221-231 |
| 光照 | `AddLight(Center, (0.9,0.1,0.3)*(255-alpha)/255)` | 202 |

### 10.5 伤害 / 绘制

| 项 | 数值 | 行号 |
|---|---|---|
| 判定框 | 覆写 `32×32`（`knockBack == -2` 时 `20×20`） | 52-61 |
| 透明度曲线 | `timeLeft < 51` 时 `alpha += 5`，否则 `alpha -= 5`，下限 0 | 147-158 |
| 主贴图 | `AstraRock`，origin `(W/2, height/2)` | 74-77 |
| 描边 | `AstraRockOutline`，四向各 `(1,0).RotatedBy(ToRadians(i*90))` 偏移 1 px，`color = (255,233,2) * alphaMult²` | 88-92 |
| 外发光 | `Assets/Glow`，色 `(131,62,196)`，scale `(0.3 + clamp(counter/120,0,1)*0.4)*scale` | 80-87 |
| 拖尾 | 8 段 `Assets/Glow`，`color (131,62,196)*0.95*progress`，scale `*(0.3+0.4*scaling)*(0.75*progress+0.25)` | 100-119 |
| 死亡尘 | 8（坠击型 16）颗，环形半径 40 px，`xMult = 0.5`，色 `AstralRocksDust` | 238-285 |

### 10.6 生成来源

| 生成者 | 参数 | 行号 |
|---|---|---|
| `SummonRocks(amt)`（phase 4，`amt=20` → `i=-20..20` 去 0，共 40 颗） | 位置 `Center`，`velocity = Zero`，`ai[0] = 360*i/amt`（度），`ai[1] = whoAmI + 0.5`，`knockBack = 0`，伤害 `GetBaseDamage()/2*0.75` | `Astrageldon.cs:2973-2989`；调用处 `1593` |
| 二阶段过场（`attackTimer == 360`） | 8 颗，`circular2 = (24,0).RotatedBy(ToRadians(k*45))`，位置 `Center + circular2*4`，`velocity = circular2*0.3`（7.2 px/刻），`knockBack = -1`，`ai[0]=Player.Center.X, ai[1]=Player.Center.Y` | `Astrageldon.cs:1711-1719` |
| `GroundRocks(amt)`（砸地时） | `i=-amt..amt` 去 0；位置 `Center + (48i + w/2*sign(i), h/2+24)`，`velocity = (0, -12.5 + abs(i)*1.5)`，`knockBack = -1`，`ai[0]=Player.X, ai[1]=Player.Y` | `Astrageldon.cs:3056-3071`；调用 `1121` |

`amt` 随难度：普通 `1`、专家 `2`（`1116-1121`）。

---

## 11. AstraBlade

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\AstraBlade.cs`（地面连锁地刺，**破防**）

### 11.1 SetDefaults

| 字段 | 值 | 行号 |
|---|---|---|
| `width / height` | `20 / 58` | 44-45 |
| `timeLeft` | `100` | 46 |
| `penetrate` | `-1` | 47 |
| `hostile / friendly` | `true / false` | 49-48 |
| `tileCollide / ignoreWater` | `false / false` | 50-51 |
| `alpha` | `255`（每刻 `±8`） | 52 / 60-75 |
| `hide` | `true` | 53 |
| `extraUpdates` | `1` | 54 |
| **破防** | `Calamity().DealsDefenseDamage = true` | 55 |

### 11.2 AI（58-108）

| 项 | 数值 | 行号 |
|---|---|---|
| 淡入 / 淡出 | 出生 `alpha = 255`（全透明）；`timeLeft >= 50` 时每刻 `alpha -= 8`（约 32 刻后完全显现）；`timeLeft < 50` 时每刻 `alpha += 8` | 60-74 |
| 关伤阈值 | `alpha > 100` → `hostile = false`（淡出后 12 刻失效） | 66-69 |
| 竖直摆动 | `sin = sin(ToRadians(ai[0]*1.8)) * -64`；`sin > 0` 时 `×1.2` → 摆幅 **-64 ~ +76.8 px** | 76-80 |
| 摆动周期 | `ai[0]` 每刻 +1，`1.8°/刻` → 周期 **200 刻（3.33 s）** | 87 |
| 位置 | `Center = (Center.X, oldcenter.Y + sin)`（只上下动） | 86 |
| 连锁生成 | `ai[0] >= 10 && abs(ai[1]) > 1` 时：在 `oldcenter + (32*dir, 0)` 生成下一根，`velocity = (0,2)`，`ai[0]=0`，`ai[1]` 递减/递增直到 0 → **每 10 刻（0.167 s）向两侧延伸一根，间距 32 px = 2 tile** | 88-107 |
| 初始朝向 | `ai[0]==0` 时 `initialDirection = ai[1] > 0 ? 1 : -1`，`oldcenter = Center` | 81-85 |

### 11.3 绘制（23-40）

| 项 | 数值 | 行号 |
|---|---|---|
| 贴图 | 本体（宽 `W`，高 `H`） | 25 |
| 纵向切片 | `for i = 0; i < H + 30; i += 2`，每片 `Rectangle(0, min(i, H-1), W, 2)` | 27-36 |
| alpha 曲线 | 每片 `alpha += 2`；越过贴图高度后每片额外 `+13`（尾端快速隐去） | 29-35 |
| 水平翻转 | `initialDirection != 1` 时 `FlipHorizontally` | 37 |

### 11.4 生成来源

`GroundBlades(length)`：从 Boss 中心两侧各 1 根，`ai[1] = length * ±1`，位置 `Center + ((16 + NPC.width/2)*±1, NPC.height/2 + 24)`（`Astrageldon.cs:3026-3045`，生成在 `3042`）。各攻击的 `length`：

| 攻击 | `length`（→ 每侧根数） | 行号 |
|---|---|---|
| 落地（phase -1） | `40`（专家 +10，死亡 +5） | 976-985 |
| phase 0 / 3 | `10`（复仇 +5、死亡 +5）`+ attackTimer2*10` | 989-999 |
| phase 1 / 4 | `30`（复仇 +5、死亡 +5） | 1003-1013 |
| phase 2 | `15`（复仇 +5、死亡 +10）`+ attackTimer2*10` | 1017-1027 |

---

## 12. HostileAsteroid

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\HostileAsteroid.cs`

### 12.1 SetDefaults

| 字段 | 值 | 行号 |
|---|---|---|
| `width / height` | `75 / 75` | 26-27 |
| `timeLeft` | `600`（10 s） | 32 |
| `penetrate` | `-1` | 33 |
| `hostile / friendly` | `true / false` | 29-28 |
| `tileCollide` | `false` | 31 |
| `aiStyle` | `-1` | 30 |
| `extraUpdates` | `2`（逻辑每刻跑 3 次） | 34 |
| `scale` | `1.1` | 35 |
| 拖尾 | `TrailCacheLength = 28`，`TrailingMode = 0` | 20-21 |

### 12.2 AI（74-111）

| 项 | 数值 | 行号 |
|---|---|---|
| 自转 | `rotation += velocity.Length() * 0.0075` | 76 |
| 尘环半径 | `sqrt(75²+75²) * scale / 2 ≈ 58.3 px`（3.6 tile） | 78-80 |
| 每刻尘量 | 3 颗火焰尘（id 6）+ 概率 1/3 追加 1 颗（id 31），另 1/3 概率在 `oldPos[2]` 再补 1 颗 | 82-110 |
| 出界清理 | `Center.Y > borderCenter.Y` 且 `dist > targetOrbitalRange + 200` 时 `Kill()`（跟随绝望期缩圈） | 53-65 |
| 初始音效 | `AstralsEndUse`（`Vol 1.0, Pitch -0.6`），玩家距 < 3000 px 时 `ShakeIntensity += 4; ShakeMult = 0.92` | 40-51 |

### 12.3 伤害 / 死亡

| 项 | 数值 | 行号 |
|---|---|---|
| 判定框 | 覆写为 **`120×120`** 居中（比贴图大 60%） | 189-192 |
| `TileCollideStyle` | `fallThrough = false`（可踩在物块上不穿透） | 113-117 |
| 死亡散射 | 5 颗 `AstrageldonStar2`，方向 `-normalize(velocity)` 左右各展开 `±80°`（`maxSpread=160/5`），速度 **3 px/刻**，`ai[0]` 继承（Boss `whoAmI`） | 127-135 |
| 死亡震屏 | 3000 px 内 `ShakeIntensity += 6` | 141-144 |
| 死亡尘 | `width+height = 150` 次，速度 `-velocity*1.5 * rand(0.6,1.2)`，含 `AstralRocksDust`、`Gore`（1/40） | 148-186 |

### 12.4 绘制（194-213）

| 项 | 数值 | 行号 |
|---|---|---|
| 主体 | 本体贴图，`scale` | 200 |
| 重影 | 同贴图 `*0.25` alpha，`scale*1.1` | 201 |
| 拖尾 | 28 段 `Assets/Glow`，色 `Color(75,20,10,10) * progress`，scale `3*projectile.scale*progress` | 204-210 |
| 前向辉光 | `position + velocity*5` 处，色 `(125,70,40,25)`，scale `glowScale*0.65` | 211 |

### 12.5 生成来源

`Astrageldon.AI` 绝望期（`attackPhase==15`）：`attackTimer >= 24 + attackTimer2*2 + 90` 且 `targetOrbitalRange > 330`（`Astrageldon.cs:1750-1766`）

| 项 | 数值 | 行号 |
|---|---|---|
| 环形起点 | `orbitalRange = (0, -(targetOrbitalRange+200)).RotatedBy(ToRadians(rand(0..60) * (attackTimer2%2*2-1)))` | 1757 |
| 位置 | `borderCenter + orbitalRange` | 1762 |
| 速度 | `(normalize(orbitalRange)*-4 + (0,10) + randCircular(4, 2.5)) / (4 + attackTimer2*0.05)` | 1758-1762 |
| 伤害 | `GetBaseDamage()/2` | 1761 |
| 节奏 | 每次生成后 `attackTimer2++` 且 `attackTimer = 90`，下发的门槛是 `attackTimer >= 24 + 2*attackTimer2 + 90` → **间隔 = 24 + 2n 刻（首轮 24 刻，之后每轮 +2 刻）** | 1755-1765 |

---

## 13. AstrageldonStar

文件：`src\CatalystMod.Projectiles.Enemy\AstrageldonStar.cs`（环绕召唤星，**70 刻后射出 3 道 `AstrageldonLaser`**）

### 13.1 SetDefaults

| 字段 | 值 | 行号 |
|---|---|---|
| `width / height` | `42 / 42` | 39-38 |
| `timeLeft` | `360`（正常会在第 70 刻被 `Kill`） | 41 |
| `penetrate` | `-1` | 44 |
| `hostile / friendly` | `true / false` | 42 / 40 |
| `alpha` | `255` | 43 |
| `tileCollide` | `false` | 46 |
| `hide` | `true`（画在 NPC 之后） | 47 |
| `netImportant` | `true` | 45 |
| `scale` | `1.1` → `runOnce` 时置 `0.2`，每刻 `+0.05` 至 `0.7` | 48 / 137-147 |
| 绘制顺序 | `behindNPCs.Add(index)` | 128-131 |

### 13.2 AI 轨道（133-209）

| 项 | 数值 | 行号 |
|---|---|---|
| 轨道半径 | `R = 140 + secondCounter * 0.5` px（**140 px ≈ 8.75 tile，随时间缓慢增大**） | 177 |
| 角度 | `ToRadians(ai[0] + secondCounter * 10)`（`ai[0]` 为初始相位，`secondCounter*10` **10°/单位**） | 177 |
| 半径增量 | `increment = (40-counter)/70`；`counter>40` 时再 `*= 1-(counter-40)/30` | 171-175 |
| 中心 | `Center = parent.Center + rotate + (0,-16)` | 179 |
| 自旋 | `rotation = max(0, (50-counter) * 14.4)` 度（**从 720° 快速减到 0**，720/14.4 = 50 刻） | 148-153；`178` 又叠加 `rotate.ToRotation()` |
| 收缩 | `timeLeft < 28` → `alpha += 9`；否则 `alpha -= 10` | 154-165 |
| 终止 | `counter == 70`（1.17 s）时生成激光并 `Kill()`；父 NPC 不存在也 `Kill()` | 181-208 |

### 13.3 生成物（185-197）

| 项 | 数值 | 行号 |
|---|---|---|
| 数量 | 3（`i = -1..1`） | 192 |
| 展开角 | `spread = 4°`；`Main.getGoodWorld` 时 `8°` | 185-189 |
| 方向 | `rotate.SafeNormalize * ToRadians(i*spread)` | 194 |
| 速度 | `* 5`（5 px/刻） | 194 |
| 类型 | `AstrageldonLaser` | 195 |
| 伤害 | 继承本体 `damage`（Boss 传 `GetBaseDamage()/2*0.75`） | — |

### 13.4 绘制（51-126）

| 元素 | 参数 | 行号 |
|---|---|---|
| `spotlight` 四层 | `*0.2*intensity`（scale `0.5*scale*intensity`）、`*0.1`（scale `0.8*…`）、两条 `*0.35` 十字（`(1.1,0.2)` 压扁，分别 0° / 90°） | 75-84 |
| 辉光 `Glow` | `timeLeft > 240` 时显示，`scaling = min(counter/120, 1.25)`，scale `projectile.scale*(0.5+scaling)`，色 `(255,215,60)` | 114-124 |
| 主体 | 本体贴图 ×1；再 4 个 `alpha=0`、`*0.6` 的子图围绕 `(0,1.2).RotatedBy(rotation)` 每 90° 一次 | 101-113 |
| 拖尾（Primitive） | 紫色宽 `20*(1-p)`，黄色宽 `20*0.72*(1-p)`；色函数见 51-73；shader `CalamityMod:TrailStreak`，`RenderTrail` 采样 16 | 51-96 |

### 13.5 生成来源

`SummonStars(amt)`（`Astrageldon.cs:3007-3024`）：`amt` 颗均分 360°，`ai[0] = (rand360 + i*360/amt) % 360`，`ai[1] = whoAmI + 0.5`，伤害 `GetBaseDamage()/2*0.75`，`velocity = Zero`。

| 调用处 | `amt` | 行号 |
|---|---|---|
| phase 1（每 100 刻） | 专家 8 / 普通 6 +（复仇 2）（死亡 2）+ `(4*(1-SpeedUp())+0.5)` | `Astrageldon.cs:1267-1277` |
| phase 3（`timer==0`） | 8 + 死亡 2 + 同项 | `1472-1478` |
| phase 3（每次落地） | 8 + 死亡 2 + 同项 | `1509-1515` |

---

## 14. AstrageldonStar2

文件：`src\CatalystMod.Projectiles.Enemy\AstrageldonStar2.cs`（螺旋加速彗星，带前置虚线预测）

### 14.1 SetDefaults

| 字段 | 值 | 行号 |
|---|---|---|
| `width / height` | `36 / 36` | 46-47 |
| `timeLeft` | `180`（3 s） | 49 |
| `penetrate` | `-1` | 52 |
| `hostile / friendly` | `true / false` | 50 / 48 |
| `alpha` | `255`，每刻 `-5` | 51 / 75-78 |
| `tileCollide` | `false` | 54 |
| `hide` | `false` | 55 |
| `netImportant` | `true` | 53 |
| 拖尾 | `TrailingMode = 0`，`TrailCacheLength = 50`，`DrawScreenCheckFluff = 2000` | 39-41 |
| 贴图 | 复用 `AstrageldonStar` 的贴图（`CatalystHelpers.GetPath<AstrageldonStar>()`） | 35 |
| `ShouldUpdatePosition` | `false` | 104-107 |

### 14.2 运动（64-102）

| 项 | 数值 | 行号 |
|---|---|---|
| 速度倍率 | `veloMult = 1.018`（每步 ×1.018，**180 步后 ×24.5**） | 79 |
| 每步转角 | `rotateMod = 1.5f` 度（固定旋向 `Direction1 = ai[1]`） | 80 / 89 |
| 每刻推进 | `runPreAI(4)` 预演 4 步 + `runPreAI(-1)` 实体推进 1 步 → 预测轨迹领先本体 3 步/刻 | 186-187 / 92-97 |
| 预演上限 | `trailPos.Count >= timeLeft` 后停止 | 83-86 |
| 实体中心 | `Center += velocity`（手动），`velocity *= 1.018`，`velocity.RotatedBy(ToRadians(1.5*Direction2))` | 94-96 |
| 缩放 | `scale *= 0.998`/刻 | 200 |
| 长度换算 | 初速 4 + 1.5°/步 → 前 60 步约走出 `4*(1.018^60-1)/0.018 ≈ 500 px`，整生命周期可达数千 px | 推算 |

### 14.3 伤害

| 项 | 数值 | 行号 |
|---|---|---|
| 判定框 | 覆写 `(36*scale)²` 居中 | 58-62 |
| 伤害节奏 | 未自定义 cooldown（受玩家无敌帧限制）；本体每刻检测 | — |
| 无 debuff | 无 `OnHitPlayer` | — |

### 14.4 绘制（109-157）

| 元素 | 参数 | 行号 |
|---|---|---|
| 预测虚线 | 沿 `trailPos` 每段 `Rectangle(0,0,length,2)`（2 px），`alphaMult = 1.1 - 0.5*counter/240 - (i-1)/count`（≤1），色 `(255,233,2)` | 109-131 |
| Primitive 拖尾 | 同 `AstrageldonStar` 的黄/紫函数，`RenderTrail(oldPos, …, 16)` | 140-142 |
| 辉光 | `RenderStarGlowyThing(..., Color(200,200,0), 1f, 1f)` | 143 |
| 主体 | 本体贴图，**画 2 次**，色 `(255,255,255,100) * (255-alpha)/255` | 149-153 |
| 死亡尘 | 20 颗 `MonoDust2` | 159-173 |

### 14.5 生成来源（6 处）

| 位置 | 数量 / 速度 / `ai[1]` | 行号 |
|---|---|---|
| phase 8 砸地 | `amt2 = 6`（专家 8），`speed2 = -(2+0.3*cycles)` ≤4，`ai[1] = attackTimer2%2*2-1` | `Astrageldon.cs:1054-1072` |
| phase 10 / `timer2==150` | 8（专家）或 6，`outWards2 = (0,-2).RotatedBy(ToRadians(m*150/amt)+toPlayer.rotation)`，每方向 **2 颗**（`ai[1]=-0.33 / +0.33`） | `Astrageldon.cs:2219-2225` |
| phase 11（每 240-30c 刻） | `outWards3 = (-4,0).RotatedBy(ToRadians(num2*360/4))`，`ai[1] = num*1.2`（`num=±1`） | `Astrageldon.cs:2331-2340` |
| phase 14 / `timer==-61` | `amt13 = 4/专家6 + cycles` ≤12，`speed14 = -(2+0.4c)` ≤4，`ai[1] = -1 / +1` 各一轮 | `Astrageldon.cs:2574-2595` |
| `HostileAsteroid.OnKill` | 5 颗，速度 3，`ai[0]` 继承 | `HostileAsteroid.cs:131-135` |

---

## 15. AstrageldonStar3

文件：`src\CatalystMod.Projectiles.Enemy\AstrageldonStar3.cs`（四向星，**70 刻后射 4 道激光**）

### 15.1 SetDefaults

| 字段 | 值 | 行号 |
|---|---|---|
| `width / height` | `42 / 42` | 39-40 |
| `timeLeft` | `360` | 42 |
| `penetrate` | `-1` | 45 |
| `hostile / friendly` | `true / false` | 43 / 41 |
| `alpha` | `255`（每刻 `-10`） | 44 / 144-155 |
| `tileCollide / hide` | `false / true` | 47-48 |
| `netImportant` | `true` | 46 |
| 常量 | `EpicMax = 70f` | 20 |
| 私有 | `rotationTrue = 360f` | 22 |
| 绘制顺序 | `behindNPCs` | 103-106 |

### 15.2 运动（118-188）

| 项 | 数值 | 行号 |
|---|---|---|
| 初速阻尼 | `velocity *= 0.9`/刻 | 127 |
| 缩放 | `runOnce` 置 `0.4`，每刻 `+0.02` 至 `1` | 122 / 129-136 |
| 自旋衰减 | `rotationTrue = rotationTrue*0.95 - 2.3`，下限 0（**每刻先 ×0.95 再减 2.3°，快速刹停**） | 137-142 |
| 最终朝向 | `rotation = ToRadians(rotationTrue) + ai[0]` | 143 |
| 透明度 | `timeLeft<28` → `alpha += 9`，否则 `alpha -= 10` | 144-155 |
| 计时 | `counter++` 到 **70** 触发爆发 | 165-166 |
| 父判定 | 父 NPC 必须是 `Astrageldon` 或 `AscendedAstralSlime` | 158-159 |

### 15.3 生成物（166-175）

| 项 | 数值 | 行号 |
|---|---|---|
| 类型 | 父为 Boss **或** `CalamityWorld.death` → `StarLaser`；否则 `AstrageldonLaser` | 170 |
| 数量 | 4（`i=0..3`，90° 均分） | 171-174 |
| 方向/速度 | `velocity = (2,0).RotatedBy(ToRadians(i*90) + ai[0]) * 5` → **5 px/刻** | 173 |
| 伤害 | 继承本体 `damage`（Boss 传 `GetBaseDamage()/2*0.8`） | — |

### 15.4 伤害

| 项 | 数值 | 行号 |
|---|---|---|
| `CanHitPlayer` | **仅 `counter < 3`** 时可以命中（前 3 刻），之后纯装饰 | 108-111 |
| 命中附加 | `AstralBlight` 240 刻 | 113-116 |
| 死亡音效 | `Item91`，`Vol ×1.2`，`Pitch -0.1` | 190-199 |

### 15.5 绘制（51-101）

| 元素 | 参数 | 行号 |
|---|---|---|
| 前摇四芒 | 4 臂 × 19 段，每段步长 `88*i` px，`percent = clamp(counter/40, 0, 1)`，贴图 `DashIndicator`，`Rectangle(0,0,2,length)`，色 `(255,219,79)`，alpha `percent-(i-1)/20` | 53-71 |
| 辉光 | `glowy = 0.6`；`timeLeft>240` 时 `glowy += min(counter/70,1.25)*0.8`；色 `(150,20,150)` | 76-86 |
| 主体 | 本体贴图画 2 次，色 `(255,255,255,100)*(255-alpha)/255`；origin `(W/2, height/2)` | 87-99 |

### 15.6 生成来源

| 位置 | 参数 | 行号 |
|---|---|---|
| phase 9 冲刺中（`timer%3==0 && timer%cycleSpeed<12`） | 位置 `Center + velocity*-0.5`，`velocity = velocity*0.1 + randCircular(1,1)`，`ai[0] = velocity.ToRotation()+π/4`，`ai[1]=whoAmI`，伤害 `damage4*0.8` | `Astrageldon.cs:2098-2102` |
| `ResolveAll` phase 9（`attackTimer%2==0`，`lerp 0.2~0.8`） | 位置 `Center + outwardVelocity*-0.5`，`velocity = outwardVelocity*0.1 + randCircular(1,1)`，`ai[0] = outwardVelocity.ToRotation()+π/4` | `Astrageldon.cs:2857-2861` |

---

## 16. AstrageldonStar4

文件：`src\CatalystMod.Projectiles.Enemy\AstrageldonStar4.cs`（会追踪的星弹，**命中即消失**）

### 16.1 SetDefaults

| 字段 | 值 | 行号 |
|---|---|---|
| `width / height` | `36 / 36` | 31-32 |
| `timeLeft` | `180`（3 s） | 34 |
| `penetrate` | `-1`（但 `OnHitPlayer` 主动 `Kill`，实际单次） | 37 / 138-142 |
| `hostile / friendly` | `true / false` | 35 / 33 |
| `alpha` | `255`，每刻 `-10` | 36 / 126-133 |
| `tileCollide` | `false` | 38 |
| 拖尾 | `TrailingMode = 0`，`TrailCacheLength = 50`，`DrawScreenCheckFluff = 2000` | 24-26 |
| 贴图 | 复用 `AstrageldonStar` | 20 |

### 16.2 追踪（104-125）

| 项 | 数值 | 行号 |
|---|---|---|
| 追踪系数 | `homingCoefficient = 1 + 0.2 * totalPhase2Cycles`（循环越多越准） | 104-113 |
| 加速度 | `velocity += normalize(toPlayer) * (0.5 * (180-timeLeft)/180) * homingCoefficient` → **加速度从 0 线性升到 0.5·k px/刻²** | 122-124 |
| 目标 | `Main.player[ai[1]]` | 119 |
| 父校验 | `ai[0]` 指向的 NPC 必须是 `Astrageldon`，否则 `Kill` | 105-118 |
| 自转 | `rotation = velocity.X * 0.07` | 134 |
| 缩放 | `scale *= 0.999`/刻 | 147 |

### 16.3 伤害 / 绘制

| 项 | 数值 | 行号 |
|---|---|---|
| 判定框 | 覆写 `(36*scale)²` | 41-45 |
| 命中处理 | `Kill()` + `netUpdate = true`（真·单次） | 138-142 |
| 出生尘 | 20 颗，沿 `velocity*i*0.5` 分布，速度 `+normalize(velocity)*4` | 88-103 |
| 死亡尘 | 30 颗 `MonoDust2`，`scale 1.8` | 70-84 |
| 绘制 | Primitive 拖尾 + `RenderStarGlowyThing`（色 `(200,200,0)`）；主体画 2 次（`(255,255,255,100)`） | 52-68 |

### 16.4 生成来源

`Astrageldon.AI` phase 14 前段（`attackTimer % speed13 == 0`，`timer3 <= 1`）：

| 项 | 数值 | 行号 |
|---|---|---|
| 节奏 | `speed13 = 10 - 2*cycles`（下限 3 → `cycles` 强制 4）；总量 `total = 15 + 5*cycles`（≤50） | `2509-2520` |
| 位置 | `Center + normalize(toPlayer) * 24` | 2532 |
| 速度（常规） | `normalize(toPlayer) * (8 + cycles)` 再 `RotateBy(rand(-4°, +4°))` + `randCircular(2,2)` | 2532 |
| 速度（散射轮，`attackTimer3 == 1`） | `normalize(toPlayer) * 6.4` 再 `RotateBy(rand(-30°,30°) + 180°)` + `randCircular(2,2)`（**向背后喷**） | 2532 |
| 后坐 | `velocity -= normalize(toPlayer) * 8/(1+cycles*0.5)` | 2534 |
| `ai` | `ai[0] = whoAmI`，`ai[1] = NPC.target` | 2532 |

---

## 17. NebulaSphere（第二阶段球形结界）

文件：`src\CatalystMod.Projectiles.Enemy\NebulaSphere.cs`

### 17.1 SetDefaults

| 字段 | 值 | 行号 |
|---|---|---|
| `width / height` | `64 / 64`（不使用，判定全自定义） | 38-39 |
| `timeLeft` | `1200`（但每刻被强制回 20 → 实际由父 NPC 存活决定） | 43 / 113 |
| `penetrate` | `-1` | 44 |
| `hostile / friendly` | `true / false` | 45 / 40 |
| `tileCollide / ignoreWater` | `false / true` | 42-41 |
| 常量 | `Height = 2250`（结界的「厚度」半径） | 17 |
| 缩放 | `projScale = (5, 3)` | 29 / 92 |
| `DrawScreenCheckFluff` | `4000` | 33 |

### 17.2 AI（89-172）

| 项 | 数值 | 行号 |
|---|---|---|
| 中心跟随 | 首帧 `center = parent.Center`；之后 `center = Lerp(center, geldon.borderCenter, 0.1)` | 103-110 |
| 半径 | `distance = geldon.borderOrbitalRange`（默认 `4800`，随二阶段缩圈从 `1000` 起按 `0.08` 插值） | 111-112 / `Astrageldon.cs:1653` |
| 存活 | `timeLeft = 20` 每刻刷新；父 NPC 消失后 `distance += 10`/刻直到 `2400` 再 `Kill` | 113 / 126-139 |
| 绝望期 | `geldon.desperation && parent.ai[0] == 15` 时 `scaleMult = 0.04 + 0.96*(136+targetOrbitalRange)/1136`；`targetOrbitalRange < 330` 时关伤并标记 `explode` | 115-124 |
| 玩家排斥 | 玩家超出 `distance + 200` 时被拉回球面（径向速度分量清零） | 151-170 |
| 清理 | `KillOtherProjectiles()` 杀掉环带内的 `SlimerSpear` | 73-87 |
| 方向 / 旋转 | `spriteDirection = velocity.X > 0 ? 1 : -1`；`spriteDirection == -1` 时 `rotation += π` | 146-150 |

### 17.3 伤害（58-71）

| 项 | 数值 | 行号 |
|---|---|---|
| 判定 | 距离环带：`dist ∈ (distance + lenience, distance + 2250 - 156)` | 65-67 |
| `lenience` | `156 + 64*(1 - scaleMult)` px | 66 |
| 命中附加 | `CatalystPlayer.IncreaseInterstellarCorruption(target)` | 48-51 |
| 不可伤 NPC | `CanHitNPC → false` | 53-56 |

### 17.4 绘制（182-228）

| 项 | 数值 | 行号 |
|---|---|---|
| 每侧精灵数 | `amt = 16`，双向（`j = -1, +1`），`j == 1` 时跳过第 0 个 → **31 片** | 184-195 |
| 角度 | `ToRadians(counter*0.33 + ai[0] + i*22.5) * j`（每刻转 **0.33°**） | 196 |
| 半径 | `distance + 1125 - tex.Height*0.75*(1 - scaleMult)` | 196 |
| 颜色 | `(112,51,166)` | 186 |
| 缩放 | `projectile.scale * projScale * scaleMult` = `(5,3) * scaleMult` | 199 |
| 星系图层版本 | `DrawGalaxyMappedSprite` 用 `(trueCenter - screenPosition)/2`、`projScale/2` | 205-228 |

### 17.5 生成来源

`SummonBorder()`（`Astrageldon.cs:3047-3054`）：`ai[0] = 0`，`ai[1] = whoAmI + 0.5`，伤害 `GetBaseDamage()/2*1.2`。调用点：phase 6 首次进入二阶段时（`Astrageldon.cs:1842-1846`）。

---

## 18. NebulaHitbox（零伤装饰球）

文件：`src\CatalystMod.Projectiles.Magic\NebulaHitbox.cs`

| 项 | 值/说明 | 行号 |
|---|---|---|
| `width / height` | `240 / 240` | 19-20 |
| `timeLeft` | `20` | 22 |
| `penetrate` | `-1` | 21 |
| `friendly / hostile` | `true / false` | 22-25 |
| `tileCollide` | `false` | 23 |
| `alpha` | `255`（纯粒子，不画贴图） | 26 |
| `DamageType` | `Magic` | 27 |
| 免疫 | `usesLocalNPCImmunity = true`，`localNPCHitCooldown = 30` | 28-29 |
| 每刻粒子 | 22 个 `NebulaFoam`，半径 `16*(1-timeLeft/20)`，`scale rand(1.2, 2.0)`，摩擦 `0.99` | 46-51 |
| 死亡粒子 | 120 个，速度 `rand(12,20)`，`scale rand(1.2, 2.4)`，摩擦 `0.989` | 56-72 |
| 命中 | 清除目标免疫并施加 `AstralBlight` 360 刻 | 74-78 |
| **Boss 侧用法** | 绝望期自杀瞬间生成 8 个，**伤害 0**（纯内爆视觉）；玩家武器 `NebulaSpear` 会生成 1 个带伤害的 | `Astrageldon.cs:1784-1790` |

---

## 附录 A：玩家侧同族弹幕（非 Boss 生成，供参数对照）

| 类 | 关键参数 | 行号 |
|---|---|---|
| `IntergelacticStar`（`Projectiles.Magic`） | `42×42`，`timeLeft 360`，`friendly`，`localNPCHitCooldown 30`，`extraUpdates 1`；轨道半径 `32 + counter*1.5`，角度 `ai[0] + secondCounter*4`，`counter>=70` 后生成 3 发 `IntergelacticLaser`（±10°，速度 5）；伤害经 `Softcap(damage, 100, 0.25)` | `src\CatalystMod.Projectiles.Magic\IntergelacticStar.cs:26-41`、`112-127`、`135-145` |
| `IntergelacticLaser` | `32×12`（判定 12×12）、`timeLeft 30`、`alpha 255` 每刻 `-20`、`scale` 恒 `1.3`、加速度 `+1.0 px/刻²`、`localNPCHitCooldown 10` | 同上目录 `IntergelacticLaser.cs:19-34`、`68-112` |
| `NebulaSpear` | `48×48`、`timeLeft 120`、`extraUpdates 7`、`penetrate 1`、追踪半径 `360/480`、速度 `2/3.6`；6 层腐蚀后炸 5 发 | `src\CatalystMod.Projectiles.Magic\NebulaSpear.cs:23-37`、`137-177` |
| `AstralRocksProj` | `20×20`，`friendly`，`idStaticNPCHitCooldown 6`；最大 8 颗环绕石，半径 `50~120`，帧数 13（随机 8 帧） | `src\CatalystMod.Projectiles.Typeless\AstralRocksProj.cs:53-95` |
| `PlayerNebula` | `96×96`、`timeLeft 120`、`localNPCHitCooldown 20`；用 `AstrageldonNebula` 贴图绘制（与本 Boss 同贴图） | `src\CatalystMod.Projectiles.Typeless\PlayerNebula.cs:24-36`、`79-90` |

## 附录 B：Boss 召唤的小怪（非「弹幕」，但参与同屏演出）

| 类 | 关键 SetDefaults | 生成 | 关联弹幕 |
|---|---|---|---|
| `NovaSlimer` | `aiStyle -1`、`lifeMax 1200`、`damage 120`、`defense 30`、`52×44`、`noTileCollide`、`noGravity`、**`dontTakeDamage = true`** | `SummonSlimer(amt)`：相位 `i*360/amt`（`Astrageldon.cs:2991-3004`） | 喷 `SlimerSpear`（`NovaSlimer.cs:270`，速度 14，伤害 60） |
| `NovaSlime` | `aiStyle 0`、`lifeMax 600`、`damage 80`、`defense 30`、`64×40` | phase 2 落地时 `NPC.NewNPC(... NovaSlime ...)`，X 方向 `175*i` 交替 | — |
| `SlimerSpear` | `48×48`、`timeLeft 120`、`extraUpdates 7`、`penetrate 1`、`alpha 255`、`DamageType = Magic`、`hide = true`、无自绘（`PreDraw` 返回 false，全靠 `NebulaFoam` 粒子） | 由 `NovaSlimer` 发射 | 命中 `IncreaseInterstellarCorruption` |

## 附录 C：反编译还原备注（读源码时容易踩的点）

| 现象 | 位置 | 说明 |
|---|---|---|
| `SpiralLaser.innerRotation / outerRotation` 常量 | `SpiralLaser.cs:23-25` | 声明了但代码里用字面量 `135f` / `135f`，常量未被引用 |
| `SpiralLaser.Laser()` 的 `mult` 恒为 0 | `SpiralLaser.cs:208-212` | 正弦抖动实际被注释掉（`mult = 0f`），光束是纯螺旋线 |
| `AstrageldonHaloManager.checkPos()` | `AstrageldonHaloManager.cs:34-46` | 只累加局部变量 `iterator`，无任何副作用（死代码） |
| `AstrageldonStar2` 的 `borderCenter / orbitalDistance` | `AstrageldonStar2.cs:184-185` | 每刻写入但从未使用（遗留代码） |
| `NebulaPlatform.Size = 28` | `NebulaPlatform.cs:27` | 未被任何绘制/判定代码使用 |
| `Astrageldon.lifeMax /= 2` | `Astrageldon.cs:428` | 基础血量 105000 在难度缩放后统一减半，实际战斗血量约为表中公式值 |
| `StarLaser` 无 debuff | `StarLaser.cs:63-65` | `OnHitPlayer` 是空方法，只有 `AstrageldonLaser` / `SpiralLaser` 上 `AstralBlight` |
| `StarLaser.MaxLength / degradePoint` | `StarLaser.cs:16-18` | 常量声明存在，但曲线用字面量 `120` / `70`（`75`、`104`） |

