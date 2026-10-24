# 附录 A：召唤物 / 小怪 / 地面弹幕 精确参数

> 由 minions-spec 从反编译源码逐字段提取，所有数值标注 文件:行号。单位：1 tile = 16 px，速度 px/刻（60 刻/秒）。
> 换算到 MC：px ÷ 32 = 格；px/刻 × 0.09375 = 格/MC tick。

# Astrageldon（末世星史莱姆）召唤物 / 小怪 参数表

- 源码根：`C:\Users\l1518\Desktop\CatalystMod_Decompiled\src`（反编译 C#）
- 精读范围：`src\CatalystMod.NPCs.Boss.Astrageldon\` 下 6 个类 + `src\CatalystMod.NPCs\DamageTables.cs`
- 所有数值均取自源码字面量，未做任何"约等于"处理；来源标注 `文件名:行号`

## 单位与读表约定

| 项 | 约定 |
|---|---|
| 长度 | 1 tile = 16 px；表中 `width/height` 均为 px（括号内给出 tile 折算） |
| 速度 | px / 游戏刻（tick），60 tick = 1 s。1 px/tick = 60 px/s = 3.75 tile/s |
| 加速度 / 重力 | px / tick² |
| 计时器 | 单位 tick；`ai[0]~ai[3]` 为 NPC/Projectile 的 `ai` 数组下标 |
| `extraUpdates = n` | 该投射物每 tick 更新 n+1 次，**实际每 tick 位移 = 速度 × (n+1)**；表中凡涉及该字段均在备注写明 |
| 角度 | 源码多用 `MathHelper.ToRadians(deg)`；本表直接写"度/刻" |
| 派生伤害 | 父体传入的 `damage` 多为 `Astrageldon.GetBaseDamage() / 2`，见文末附录 |

## 总览

| 类 | 类型 | lifeMax | 接触伤害 | 防御 | 尺寸 (px) | 一句话定位 |
|---|---|---|---|---|---|---|
| `NovaSlime` | ModNPC | 600 | 80 | 30 | 64 × 40 | 跳砸召唤的跳跳史莱姆，10.5 px/刻冲向玩家 |
| `NovaSlimer` | ModNPC | 1200 | 120 | 30 | 52 × 44 | 螺旋收缩后高速冲撞，自行裂解成 1 根史莱姆矛 |
| `AstraRock` | ModProjectile | — | 父体传入 | — | 32 × 36 | 绕 Boss 公转（半径 40→680）或抛落的星辉岩石 |
| `HostileAsteroid` | ModProjectile | — | 父体传入 | — | 75 × 75（命中箱 120 × 120） | 绝望阶段从边界轨道砸下的敌对小行星，死亡炸成 5 颗星 |
| `AstraBlade` | ModProjectile | — | 父体传入 | — | 20 × 58 | 地面升起并横向铺开的刀柱链（每侧 32 px 间隔） |
| `SlimerSpear` | ModProjectile | — | 60（硬编码） | — | 48 × 48 | 不可见的高速长矛，命中叠 `InterstellarCorruption` |

> 说明：任务清单里的"星辉岩石 / 敌对小行星 / 星刃 / 史莱姆长矛"四个在源码中**不是 NPC，而是 `ModProjectile`**（`class ... : ModProjectile`），因此没有 `lifeMax/defense/aiStyle` 等 NPC 字段；本节按投射物字段（`width/height/timeLeft/penetrate/extraUpdates/...`）如实列出，并给出父体传入的伤害公式。

---

## 1. NovaSlime（新星史莱姆）

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\NovaSlime.cs`（397 行），`class NovaSlime : ModNPC`

### 1.1 SetDefaults / SetStaticDefaults 字段

| 字段 | 值 | 单位 / 说明 | 来源 |
|---|---|---|---|
| `npc.aiStyle` | `0` | 无实际作用（`AI()` 已完全重写） | NovaSlime.cs:82 |
| `npc.lifeMax` | `600` | HP | NovaSlime.cs:83 |
| `npc.damage` | `80` | 接触伤害 | NovaSlime.cs:84 |
| `npc.defense` | `30` | 防御 | NovaSlime.cs:85 |
| `npc.knockBackResist` | `0f` | 完全免疫击退 | NovaSlime.cs:86 |
| `npc.width` | `64` | px = 4 tile | NovaSlime.cs:87 |
| `npc.height` | `40` | px = 2.5 tile | NovaSlime.cs:88 |
| `npc.boss` | `false` | | NovaSlime.cs:89 |
| `npc.lavaImmune` | `false` | | NovaSlime.cs:90 |
| `npc.noTileCollide` | `true` | 但 `AI()` 里手工做切片碰撞 | NovaSlime.cs:91 |
| `npc.noGravity` | `true` | 重力由 `AI()` 自己加 | NovaSlime.cs:92 |
| `npc.netAlways` | `true` | | NovaSlime.cs:93 |
| `npc.netUpdate` | `true` | | NovaSlime.cs:94 |
| `npc.HitSound` | `SoundID.NPCHit1` | | NovaSlime.cs:95 |
| `npc.DeathSound` | `SoundID.NPCDeath1` | | NovaSlime.cs:96 |
| `npc.Calamity().VulnerableToHeat` | `true` | Calamity 抗性标记：怕火 | NovaSlime.cs:97 |
| `npc.Calamity().VulnerableToSickness` | `false` | | NovaSlime.cs:98 |
| `SpawnModBiomes` | `[AstralInfectionBiome]` | 仅星辉感染生物群系 | NovaSlime.cs:99 |
| `Main.npcFrameCount[type]` | `4` | 4 帧 | NovaSlime.cs:27 |
| `NPCID.Sets.TrailCacheLength` | `8` | 拖尾缓存 8 段 | NovaSlime.cs:28 |
| `NPCID.Sets.TrailingMode` | `1` | 记录位置 + 旋转 | NovaSlime.cs:29 |
| **未设置（取默认）** | `value = 0`、`scale = 1f`、`friendly = false`、`buffImmune = 空`、`dontTakeDamage = false`、`timeLeft` 默认 | 源码 SetDefaults 中确实没有这些赋值 | NovaSlime.cs:80-100 |
| `const slamDelay` | `50` | **已声明但全文件未被引用**（实际用字面量 50） | NovaSlime.cs:19 |

### 1.2 AI() 完整行为拆解

入口前处理（`PreAI`，NovaSlime.cs:231-251）：

| 动作 | 数值 | 来源 |
|---|---|---|
| `slamTimer` 正向倒计 | `slamTimer > 0` → 每刻 `-1` | :233-236 |
| `slamTimer` 负向快进 | `-60 < slamTimer < -1` → 每刻 `-2` | :237-240 |
| 砂尘 | 每 10 刻 → 1 颗 `AstraDust`，alpha 20，`noGravity`，向外 0.5~1 px/刻 + 自身速度 0.1 | :241-249 |

`AI()`（NovaSlime.cs:253-387）：局部变量 `tileCollide = true`、`gravity = 0.5f`（:255-256）

| 阶段 | 进入条件 | 计时器 / 初值 | 关键数值 | 行为 | 来源 |
|---|---|---|---|---|---|
| **A. 空降待机态** | `npc.ai[3] == -1`（父体 `NewNPC` 传入 ai3 = -1） | `ai[0]` 逐刻 `+1`，起点 `-Main.rand.Next(40)` ∈ [-39, 0] | `gravity = 0`（`ai[0] < 155` 期间）；`dontTakeDamage = true` | 目标点 = `(Player.Center.X + ai[1], Player.Center.Y - ai[2])`；`lerp = clamp(ai[0]/240, 0.075, 0.325)`；`Center.X` 用 `lerp*0.5`、`Center.Y` 用 `lerp` 向目标插值（悬停下降） | :338-352 |
| **B. 落地预警开始** | `ai[0] == 160` | `slamTimer = 50` | — | 开始画垂直冲击预警（50 刻 = 0.833 s） | :353-356 |
| **C. 转战斗态** | `ai[0] >= 180` | `ai[0] = -60`、`ai[3] = 0` | — | 空降结束，`dontTakeDamage = false` | :357-361 |
| **D. 战斗态·计时** | `ai[3] != -1` | 触地或 `ai[0] > 0` 时 `ai[0] += 1` | — | 站着时：`-70 <= ai[0] < 0` → `frameCounter += 2`；否则若 `ai[0] > -20`（即 `ai[0] >= 0`）→ `frameCounter += 4` | :276-293 |
| **E. 战斗态·下落加速** | `velocity.Y != 0` | — | `velocity.X *= 1.06`（每刻） | 水平加速 | :294-297 |
| **F. 起跳（弹向玩家）** | `(int)ai[0] == 20` | `ai[0] += 1` | `Speed = 10.5` | 无目标：`timeLeft ≤ 30` 且 `velocity.Y = -40`；有目标：`rot = atan2(玩家中心 - 自身中心)`，`velocity.X = -cos(rot) * 10.5`，`velocity.Y = -sin(rot) * 10.5 * 0.7 - 14` | :298-317 |
| **G. 落地反弹/重置** | `ai[0] >= 20` 且 `velocity.Y == 0` | `ai[0] = -100 - Main.rand.Next(21)`（`Next(21)` ∈ [0,20]）→ `ai[0] ∈ [-120, -100]` | 每轮随机 21 档 | 进入下一轮待机（该随机是**唯一的 AI 随机数**） | :318-331 |
| **H. 上升期穿墙** | `ai[0] >= 20` 且 `velocity.Y < 0` | — | — | `tileCollide = false` | :332-335 |
| **I. 砸地** | `slamTimer == 0`（由 50 逐刻 -1 到达） | — | 触地：`slamTimer = -2`；未触地：`gravity = 0.5 + 2.5 = 3.0` | 触地瞬间 `DustExplosion()` + `SoundID.Item62`（`Volume * 0.8`、`Pitch + 0.3`）；随后 `slamTimer` 每刻 -2 到 -60 停 | :258-275 |
| **J. 重力施加** | 每刻 | — | `gravity = 0.5`（砸地未触地时 3.0） | `if (velocity.Y < gravity * 20f(=10 或 60)) velocity.Y += gravity` | :363-366 |
| **K. 旋转** | 每刻 | — | `rotation = velocity.X * -0.01`（rad） | 按水平速度倾斜 | :367 |
| **L. 自定义碰撞** | `tileCollide == true` **且** `Player.position.Y < npc.position.Y + npc.height`（玩家在 NPC 底部之上时才做） | 5 段切片（`i = 0..4`） | `extraY = i * velocity.Y / 5`，命中时 `position.Y += extraY * 2` | 用 `Collision.TileCollision` 分段检测，避免高速穿透 | :370-386 |

**空降态参数来源**：父体调用 `NPC.NewNPC(..., NovaSlime, 0, -Main.rand.Next(40), xOffset + 目标X, Main.rand.NextFloat(700, 840), -1f)`（Astrageldon.cs:1045），即 `ai[0] ∈ [-39,0]`、`ai[1] = xOffset + 175*i*方向`、`ai[2] = 700~840`（距玩家上方的高度）、`ai[3] = -1`。

### 1.3 贴图动画 / 绘制缩放

| 项 | 数值 | 来源 |
|---|---|---|
| 帧数 | 4（`Main.npcFrameCount = 4`） | :27 |
| 空中帧 | `velocity.Y != 0` 时固定 `frame.Y = frameHeight`（第 2 帧，不循环） | :217-221 |
| 地面帧 | `frameCounter += 1.0`，`>= 5.0` 进 1 帧并 `% (frameHeight * 4)` | :222-228 |
| 拖尾 | 8 段，第 i 段缩放 `1 - 0.02*i`，alpha `180 * 渐进 * 0.5` | :28, :66-78 |
| **挤压/拉伸缩放** | `stretch = \|velocity.Y * 0.005\|`，上限 `0.2`；最终 `scale = (1 - stretch, 1 + stretch) * npc.scale` | :50-56 |
| 绘制原点 | `(npc.width / 2, npc.height)` —— 底部中心对齐，脚底贴地 | :61-62 |
| 叠加层 | 同名 `_Glow` 贴图整体再画一遍（纯白） | :49, :62 |
| 冲击预警贴图 | `CatalystMod/NPCs/Boss/Astrageldon/DashIndicator`，颜色 `(131, 62, 196)`；`percent = 1 - \|slamTimer\|/50`，半程（`half = 25`）后 `percent2` 反转 | :102-164 |

### 1.4 产出的弹幕 / 子 NPC

| 产出 | 类型 | 数量 | 备注 | 来源 |
|---|---|---|---|---|
| 弹幕 | 无 | 0 | 该 NPC 不发射任何投射物 | 全文件无 `Projectile.NewProjectile` |
| 子 NPC | 无 | 0 | | 全文件无 `NPC.NewNPC` |
| 粒子 | `AstraDust` | 每 10 刻 1 颗 | 移动时持续 | :241-249 |
| 粒子 | Dust ID 242 + `AstraDust` | 死亡/砸地各 30 颗（`NextBool(4)` 时追加 30 颗 AstraDust） | `DustExplosion()` | :180-209 |

### 1.5 难度 / 玩家数分支

| 项 | 规则 | 来源 |
|---|---|---|
| `ApplyDifficultyAndPlayerScaling` | `lifeMax = (int)(lifeMax * 0.6f * balance)`，随后调基类 | :166-170 |
| 生成数量（父体） | `amt = 5`；专家 `+1`；死亡模式 `+1`；再 `+= (int)attackTimer2 / 2`；仅 `attackTimer2 < 3` 且非客户端 | Astrageldon.cs:1030-1047 |
| 生成位置 | `x = xOffset + 175 * i * 交替方向`，`xOffset = rand(-25, 26)`，垂直上方 700~840 px | Astrageldon.cs:1040-1045 |
| `PreKill` | Boss 存活 → 返回 `false`（**不掉落、不进入死亡流程**）；Boss 不在 → 返回 `WorldDefeats.downedAstrageldon` | :389-396 |
| 战利品 | `AstraJelly` × 1~2，仅当场上**没有** Astrageldon 时（`DropHelper.If`） | :37-40 |
| 自然刷出 | 星辉感染 + `WorldDefeats.downedAstrageldon` 后，地表权重 `0.15` | CatalystNPC.cs:367-370 |

---

## 2. NovaSlimer（裂星史莱姆）

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\NovaSlimer.cs`（360 行），`class NovaSlimer : ModNPC`

### 2.1 SetDefaults / SetStaticDefaults 字段

| 字段 | 值 | 单位 / 说明 | 来源 |
|---|---|---|---|
| `npc.aiStyle` | `-1` | 纯自定义 AI | NovaSlimer.cs:49 |
| `npc.lifeMax` | `1200` | HP | :50 |
| `npc.damage` | `120` | 接触伤害 | :51 |
| `npc.defense` | `30` | 防御 | :52 |
| `npc.knockBackResist` | `0f` | | :53 |
| `npc.width` | `52` | px = 3.25 tile | :54 |
| `npc.height` | `44` | px = 2.75 tile | :55 |
| `npc.value` | `0f` | 无金钱掉落 | :56 |
| `npc.lavaImmune` | `true` | | :57 |
| `npc.netAlways` | `true` | | :58 |
| `npc.HitSound` / `DeathSound` | `NPCHit1` / `NPCDeath1` | | :59-60 |
| `npc.noTileCollide` | `true` | | :61 |
| `npc.noGravity` | `true` | | :62 |
| `npc.dontTakeDamage` | `true` | 生成即无敌（但仍可被 `PreKill` 拦死） | :63 |
| `SpawnModBiomes` | `[AstralInfectionBiome]` | | :64 |
| `NPCID.Sets.TrailCacheLength` | `6` | 拖尾 6 段 | :34 |
| `NPCID.Sets.TrailingMode` | `0` | 仅位置 | :35 |
| 图鉴绘制偏移 | `Direction = -1`（朝左） | | :36-39 |
| **未设置（取默认）** | `boss = false`、`friendly = false`、`scale = 1f`、`buffImmune = 空` | | :47-65 |
| `const TelegraphDelay` | `40` | **已声明但未被引用**（代码用字面量 `40f`） | :18 |

### 2.2 AI() 完整行为拆解

每刻固定前置（NovaSlimer.cs:158-193）：

| 动作 | 数值 | 来源 |
|---|---|---|
| 水平阻尼 | `velocity.X *= 0.93`；`\|vx\| < 0.1` 直接归零 | :158-162 |
| 目标 | 无有效目标 → `TargetClosest(false)` | :163-166 |
| 砂尘 | 每 10 刻 1 颗 `AstraDust` | :185-193 |
| `counter` | `ai[0] >= 0` → `counter++`；`ai[0] < 0` → `counter--` | :195-205 |
| `delay` | `\|ai[0]\|`（= 父体传入的角度，度） | :195 |
| `direction` | `ai[0] >= 0 ? 1 : -1` | :196-201 |

| 阶段 | 进入条件 | 计时器 / 初值 | 关键数值 | 行为 | 来源 |
|---|---|---|---|---|---|
| **S0. Boss 消失→撤离** | `Astrageldon` 不存在 **或** `Astrageldon.desperation == true` | `timeLeft` 压到 `≤ 100` | `velocity.X *= 0.99`；`velocity.Y -= 0.2`；`rotation += 24° * sign(ai[0])` | 竖直上飘逃逸，`_leaving = true`，`CanHitPlayer` 返回 false | :167-183 |
| **S1. 螺旋收缩（靠近玩家）** | `npc.ai[1] >= 0`（生成时 `ai[1] = 0`） | `\|counter\| > delay` 时 `ai[1] += 1` | 半径 `r = 320 + 1.1 * delay - \|counter\|`（px，从 `320 + 1.1*delay` 收缩到 `320`）；角度 `ai[0] + counter * 1.5 + direction * 22.5`（度）；速度 `v = 18 + 0.005 * 距离`（px/刻，上限=距离） | 每刻 `velocity *= 0.3` 后叠加指向环上目标点的速度；`rotation += 36° * direction` | :242-261 |
| **S2. 预警 + 追位（视觉冲击线）** | `npc.ai[1] >= 40` | `ai[1]` 每刻 `+1`；`savePos2` 在进入该状态的首帧锁定为自身 `Center`；`savePos` 在 `ai[1] < 80` 时持续取 `Player.Center` | 摆幅 `120 px`；`sin(弧度(ai[1]-40) * 4)`；速度 `v = 18 + 0.005 * 距离`（px/刻，上限=距离） | 目标点 = `savePos2` + 沿 `(Center - savePos)` 方向的 `sin(...) * 120` px 偏移；每刻向该点收拢；`rotation += 12° * direction`；`ai[1] >= 40` 起 `CanHitPlayer = true`（可碰撞） | :206-241<br>:141-148 |
| **S3. 突进（冲向玩家最后记录位置）** | `ai[1] >= 85` | `ai[1] = -1`，`savePos2 = 0` | `velocity = (Center - savePos) 归一化 * -12` → 指向 `savePos`（玩家）方向，12 px/刻 | 记 `acceleration = velocity`；播放 `SoundID.Item71` | :224-231 |
| **S4. 加速滑行** | `ai[1] < 0`（每刻 `-1`） | `ai[1] < -8` 时引爆（共 8 刻 ≈ 0.13 s） | `velocity = acceleration`；`acceleration += normalize(acceleration) * 0.5`（沿原方向 +0.5 px/刻²，末速 ≈ 12 + 4 = 16 px/刻）；`rotation += 24° * direction` | 直线加速冲向玩家 | :262-278 |
| **S5. 自毁→生成矛** | `ai[1] < -8` | — | — | `DustExplosion()`；`npc.active = false`；服务器端生成 **1 根 `SlimerSpear`**，速度 = `velocity` 归一化 `* 14`（14 px/刻），伤害 `60`，击退 `0` | :263-272 |

> 整个生命周期不含 `Main.rand.NextBool` 概率分支；唯一随机是 `S3` 之前的敌方 `desperation` 判定（外部状态）。

### 2.3 贴图动画 / 绘制缩放

| 项 | 数值 | 来源 |
|---|---|---|
| 帧动画 | **无**（单帧贴图）；`FindFrame` 只处理图鉴图标旋转：`24° * direction * GlobalTimeWrappedHourly * 60` | :281-287 |
| 拖尾 | 6 段（`TrailCacheLength = 6`），仅 `ai[1] >= 40` 时绘制，颜色 `(255,255,255,160)`，每段 alpha 递减，缩放 `npc.scale * 0.9` | :289-307 |
| 本体发光层 | `_Glow` 独立绘制，`scale = npc.scale * 1.05`，偏移 `(0, gfxOffY + 4)`，**不跟随旋转**（rotation 传 0） | :309-313 |
| 冲击预警线条 | 贴图 `DashIndicator`，`timer = ai[1] - 45`（>40 截断），`percent = 1 - \|timer\|/40`，半程 `half = 20` 翻转；长度 `dist = 40 + 40*(1 - percent)` 段；横向铺开 `fromCenter = 8 * percent + 12`；颜色 `(131, 62, 196, 0)`，绘制角 `旋转 + 1.57`（rad） | :67-139 |
| `PostDraw` 额外点 | 未撤离时每帧画预警 | :314-318 |

### 2.4 产出的弹幕 / 子 NPC

| 产出 | 类型 | 数量 | 伤害 / 速度 | 来源 |
|---|---|---|---|---|
| 弹幕 | `SlimerSpear` | **1**（自毁时） | 伤害 `60`、击退 `0`、初速 = 自毁时速度方向 × `14` px/刻 | :270 |
| 粒子 | `AstraDust` / Dust 242 | 每 10 刻 1 颗；自毁 30 颗（1/4 概率追加 AstraDust） | 纯视觉 | :185-193, :333-359 |

### 2.5 难度 / 玩家数分支

| 项 | 规则 | 来源 |
|---|---|---|
| `ApplyDifficultyAndPlayerScaling` | `lifeMax = (int)(lifeMax * 0.6f * balance)`，随后调基类 | :150-154 |
| `PreKill` | **恒返回 `false`** —— 不能被打死，只能靠自身 `active = false` 退场（因此不掉落） | :320-323 |
| 生成数量（父体 `SummonSlimer(amt)`） | `for i = -amt..amt`，跳过 0 → **2 × amt 只**，按 `360 * i / amt` 度均匀分布 | Astrageldon.cs:2991-3005 |
| 第 1 处调用 | `amt3 = 6 + max(totalPhase2Cycles - 1, 0)`；专家 `+2`；死亡 `+1`；再 `Math.Min(amt3, 12)` → 最多 24 只 | Astrageldon.cs:1083-1092 |
| 第 2 处调用（二阶段开场） | `amt8 = 4`；专家 `6` → 8 / 12 只 | Astrageldon.cs:1826-1837 |
| 生成条件 | `attackTimer2 < 3` 且非客户端 | Astrageldon.cs:2993 |

---

## 3. AstraRock（星辉岩石）

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\AstraRock.cs`（286 行），**`class AstraRock : ModProjectile`**

### 3.1 SetDefaults / SetStaticDefaults 字段（投射物字段）

| 字段 | 值 | 单位 / 说明 | 来源 |
|---|---|---|---|
| `Projectile.width` | `32` | px = 2 tile | AstraRock.cs:39 |
| `Projectile.height` | `36` | px = 2.25 tile | :40 |
| `Projectile.friendly` | `false` | | :41 |
| `Projectile.timeLeft` | `960` | 16 s | :42 |
| `Projectile.hostile` | `true` | | :43 |
| `Projectile.alpha` | `255` | 生成时全透明，靠 AI 淡入 | :44 |
| `Projectile.penetrate` | `-1` | 无限穿透（不因命中消失） | :45 |
| `Projectile.netImportant` | `true` | | :46 |
| `Projectile.hide` | `true` | 自定义绘制（由 Boss 或自身画） | :47 |
| `Projectile.tileCollide` | `false` | 独立飞行 45 刻后由 AI 打开 | :48 |
| `Projectile.extraUpdates` | `1`（AI 首帧内改 `0`） | 挂 Boss 时每刻 2 次更新，脱落后每刻 1 次 | :49, :200 |
| `Projectile.scale` | `0.5` 起 → 上限 `scaleMax`（`1.25f`，`knockBack == -2` 时 `1.0f`） | 每更新步 `+0.05 / (extraUpdates + 1)` | :24, :132-146 |
| `ProjectileID.Sets.TrailCacheLength` | `8` | 拖尾 8 段 | :32 |
| `ProjectileID.Sets.TrailingMode` | `0` | | :33 |
| `Main.projFrames` | `10` | 10 帧，生成时随机 1 帧后不再变化 | :34, :131 |
| `ModifyDamageHitbox` | `32 × 32` 方形；`knockBack == -2` 时 `20 × 20` | 命中箱小于贴图（贴图 32×36） | :52-61 |
| **`knockBack` 被复用为模式标记** | `0` = 挂 Boss 公转；`-1` = 独立抛落（Boss 用）；`-2` = 小型变体（`MetanovaSlimeRocks` 用） | 不是真正的击退值 | Astrageldon.cs:2986 / :1717 / MetanovaSlimeRocks.cs:101 |
| 亮度 | 每刻 `Lighting.AddLight(center, (255-alpha)*0.9/255, (255-alpha)*0.1/255, (255-alpha)*0.3/255)` → 橙红光照 | — | :202 |

### 3.2 AI 行为拆解

`PreAI`（AstraRock.cs:126-196）+ `AI()`（:198-232）：

| 模式 | 进入条件 | 计时器 / 初值 | 关键数值 | 行为 | 来源 |
|---|---|---|---|---|---|
| **通用·初始化** | `runOnce` | — | `frame = Main.rand.Next(10)`；`scale = 0.5`；`scaleMax = 1.25`（`knockBack == -2` → `1.0`） | 随机帧 + 起始缩放 | :129-138 |
| **通用·缩放** | 每更新步 | — | `scale += 0.05 / (extraUpdates + 1)`，封顶 `scaleMax` | 生长动画 | :139-146 |
| **通用·透明度** | 每更新步 | — | `timeLeft < 51` → `alpha += 5`（淡出）；否则 `alpha > 0` → `alpha -= 5`（淡入） | 淡入 / 末段淡出 | :147-158 |
| **A. 挂 Boss 公转** | `ai[1] >= 0` **且** `knockBack >= 0` | `counter` 每更新步 `+1`；`secondCounter` 每更新步 `+1/(extraUpdates+1)`（= 每刻 +1） | 半径 `40 + bonusLength`，`bonusLength = secondCounter * 4.4`，**上限 680 px**；角度 = `ai[0] + secondCounter * 2`（度/刻）；再整体旋转 `90 + secondCounter * 0.75`（度） | 椭圆化：`ai[0] < 0` 时 `rotate.Y *= 0.15 * cos(secondCounter/45 * π)`，否则 `rotate.X *= 0.15 * cos(...)`；最终 `Center = Boss.Center + rotate + (0, -16)`；`rotate.Y > 0` 时记 `drawInFront = true` | :159-190 |
| **A'. 父体失效** | 父体不是 Astrageldon 或已消失 | — | — | `ai[1] = -1` 并 `Kill()` | :191-193 |
| **B. 独立飞行（抛物）** | `ai[1] < 0` 或 `knockBack < 0` → `PreAI` 返回 `true`，进 `AI()` | `counter` 每刻 `+6`（首帧把 `extraUpdates` 置 0） | `circular = (3 + counter*0.01)` 沿 `counter` 度旋转，取其 `Y` 分量作为漂移量；`counter < 270` 期间 `velocity *= 0.9875`；`counter >= 270`（约 45 刻 = 0.75 s） | 前 45 刻：向目标点反方向缓慢漂移（`Center -= toTheLocation`），`rotation = 方向 + bonusRotation`，`bonusRotation` 从 `270` 每刻 `-0.3` 后 `*0.98` 衰减到 0 | :198-220 |
| **C. 抛射** | `counter >= 270` | 一次性（`runOnce2`） | `velocity = toTheLocation 归一化 * -24 + Main.rand.NextVector2Circular(5, 5)`（24 px/刻 + 半径 5 的随机散布）；此后 `velocity.Y += 0.02`（px/刻²） | `tileCollide = true`；`rotation = velocity.ToRotation()` | :221-231 |
| **附·绘制回溯** | `knockBack < 0` | — | `DrawTrail` 8 段，Glow 缩放 `scale * (0.3 + 0.4 * min(counter/120, 1))` × `(scale*0.75 + 0.25)` | 独立模式才自绘 | :100-119 |
| `OnKill` | 死亡 | — | 8 颗（`knockBack < 0` 时 16 颗）`AstralRocksDust`，`1/5` 追加 Dust 242，`1/5` 追加 `AstraDust` | 仅粒子；独立模式播放 `AstralRockImpact1~4` | :238-285 |

### 3.3 贴图动画 / 绘制缩放

| 项 | 数值 | 来源 |
|---|---|---|
| 帧 | `Main.projFrames = 10`；`frame = rand(10)`（生成时定死），绘制用 `Rectangle(0, frame * height, texture.Width, height)` | :34, :77, :131 |
| 本体缩放 | `0.5 → 1.25`（+0.05/更新步）；`knockBack == -2` 时为 `1.0` | :132-146 |
| 光晕 | `Glow` 贴图，缩放 `scale * (0.3 + 0.4 * min(counter/120, 1))`，颜色 `(131, 62, 196)` | :87 |
| 描边 | `AstraRockOutline` 沿 4 个方向（`i*90` 度）各画 1 次，颜色 `(255, 233, 2)`，alpha 为 `alphaMult²` | :88-92 |
| 拖尾 | 8 段 Glow，颜色 `(131,62,196) * 0.95`，逐段 `scale * (0.3+0.4*scaling) * (0.75*scale + 0.25)` | :100-119 |
| 层级 | `DrawBehind → behindNPCs.Add(index)`（画在 NPC 之后/瓦片前） | :121-124 |
| 挂 Boss 时的绘制 | 由 Boss 调 `AstraRock.Draw(...)`（`Astrageldon.cs:605`），`drawInFront` 决定前后层 | Astrageldon.cs:545-548, :605 |

### 3.4 产出的弹幕 / 子 NPC

| 产出 | 类型 | 数量 | 来源 |
|---|---|---|---|
| 弹幕 | 无 | 0 | 全文件无 `Projectile.NewProjectile` |
| 子 NPC | 无 | 0 | |
| 粒子 | `AstralRocksDust` / Dust 242 / `AstraDust` + 光 | 死亡 8 或 16 颗 | :238-285 |

### 3.5 难度 / 生成数量（无 `ApplyDifficultyAndPlayerScaling`，投射物无此钩子）

| 生成点 | 数量 | 模式 | 伤害 | 来源 |
|---|---|---|---|---|
| `SummonRocks(amt)` | `2 × amt`（`i = -amt..amt` 跳过 0，`ai[0] = 360*i/amt` 度） | `knockBack = 0`（公转） | `(int)(damage2 * 0.75)`，`damage2 = GetBaseDamage()/2` | Astrageldon.cs:2973-2989 |
| `SummonRocks(20)` 调用（旋转岩石攻击） | **40 颗** | 公转 | 同上 | Astrageldon.cs:1593 |
| `GroundRocks(amt = 2)` | `2 × amt`，出生点 `x = 48*i + width/2*sign(i)`，初速 `(0, -12.5 + \|i\| * 1.5)` | `knockBack = -1`（抛落） | `damage2` | Astrageldon.cs:3056-3071 |
| `GroundRocks` 调用 | `amt4 = 1`；专家 `2` → **2 / 4 颗**；触发阈值：血量 ≤ `lifeMax * scatterRockThreshhold`（普通 0.3 / 专家 0.4 / 复仇 0.5 / 死亡 0.75 / GetGoodWorld 0.9） | 抛落 | 同上 | Astrageldon.cs:1097-1122 |
| 二阶段开场爆发 | **8 颗**，初速 `24 * 0.3 = 7.2` px/刻 环状外抛 | `knockBack = -1` | `damage2` | Astrageldon.cs:1714-1718 |

---

## 4. HostileAsteroid（敌对小行星）

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\HostileAsteroid.cs`（214 行），**`class HostileAsteroid : ModProjectile`**

### 4.1 SetDefaults / SetStaticDefaults 字段

| 字段 | 值 | 单位 / 说明 | 来源 |
|---|---|---|---|
| `Projectile.width` | `75` | px = 4.6875 tile | HostileAsteroid.cs:26 |
| `Projectile.height` | `75` | px = 4.6875 tile | :27 |
| `Projectile.friendly` | `false` | | :28 |
| `Projectile.hostile` | `true` | | :29 |
| `Projectile.aiStyle` | `-1` | 纯自定义 | :30 |
| `Projectile.tileCollide` | `false` | 但 `TileCollideStyle.fallThrough = false` + `OnTileCollide` 回写速度 | :31, :113-123 |
| `Projectile.timeLeft` | `600` | 10 s | :32 |
| `Projectile.penetrate` | `-1` | 无限穿透 | :33 |
| `Projectile.extraUpdates` | `2` | 每刻 3 次更新 → **实际位移 = 速度 × 3** | :34 |
| `Projectile.scale` | `1.1` | | :35 |
| `ModifyDamageHitbox` | `Utils.CenteredRectangle(Center, (120, 120))` | 命中箱 120×120 px，远大于 75×75 贴图 | :189-192 |
| `ProjectileID.Sets.TrailCacheLength` | `28` | 拖尾 28 段 | :20 |
| `ProjectileID.Sets.TrailingMode` | `0` | | :21 |
| 无 `buffImmune` / 无 `lifeMax` | — | 投射物不存在这些字段 | — |

### 4.2 AI 行为拆解

| 阶段 | 进入条件 | 计时器 / 初值 | 关键数值 | 行为 | 来源 |
|---|---|---|---|---|---|
| **首次生成演出** | `runOnce` | — | 音效 `Item/AstralsEndUse`（Volume 1、Pitch -0.6）；与本地玩家距离 < 3000 px 时 `ShakeIntensity += 4`、`ShakeMult = 0.92` | 屏震入场 | :40-52 |
| **A. 边界存活判定** | `Projectile.damage < 9000` 且 `ai[0] >= 0`（`ai[0]` = Boss `whoAmI`） | 每帧 | 半径 `range = Boss.targetOrbitalRange + 200` | 若 `Center.Y > borderCenter.Y` **且** 与 `borderCenter` 距离 `> range` → `Kill()`；父体不是 Astrageldon → `Kill()` | :53-70 |
| **B. 自旋** | 每更新步 | — | `rotation += velocity.Length() * 0.0075`（rad） | 速度越快转越快 | :76 |
| **C. 尾焰** | 每更新步 | — | 3~4 颗 Dust：Dust 6（`scale 0.3~2.5`）、Dust 6（`scale 0.75~1.5`，法向 ×3）、Dust 6（同，反法向 ×3）；`NextBool(3)` 追加 Dust 31；`NextBool(3)` 从 `oldPos[2]` 追加 Dust 31 | 纯视觉 | :77-110 |
| **D. 瓦片** | 碰到瓦片 | — | `fallThrough = false`；`OnTileCollide` 把 `velocity = oldVelocity`（不减速、不消失） | 穿地时保持速度 | :113-123 |
| **E. 死亡** | 任意方式消失 | — | 5 颗 `AstrageldonStar2`，扇形 `i * 160/5 - 80` 度（即 -80/-48/-16/16/48 度），速度 `3` px/刻，伤害 = 自身伤害，`ai[0]` 传给子体 | 服务器端生成；本地端 `< 3000 px` 时 `ShakeIntensity += 6`；音效 `Item105`(Vol×1.2, Pitch -0.3) + `Item/AstralsEndKill`(Pitch -1) | :125-187 |

> 生成速度（父体，Astrageldon.cs:1757-1762）：`orbitalRange = (0, -(targetOrbitalRange + 200))` 沿 `rand(60 度) * 交替方向` 旋转；`velocity = normalize(orbitalRange) * -4 + (0, 10) + NextVector2Circular(4, 2.5)`，再除以 `(4 + attackTimer2 * 0.05)`；生成位置 `borderCenter + orbitalRange`；`extraUpdates = 2` → 每刻实际位移 ×3。

### 4.3 贴图动画 / 绘制缩放

| 项 | 数值 | 来源 |
|---|---|---|
| 帧动画 | **无**（未设 `Main.projFrames`，单帧贴图） | 全文件 |
| 本体 | 1 次正常绘制 + 1 次 `drawColor * 0.25`、缩放 `scale * 1.1`（= 1.21）的重影 | :200-201 |
| 拖尾 | `Glow` 贴图，28 段，缩放 `scale * 3 = 3.3` 逐段线性衰减，颜色 `(75, 20, 10, 10) * progress` | :204-210 |
| 速度方向光晕 | 额外 1 次，位置偏移 `velocity * 5`，缩放 `scale * 3 * 0.65 = 2.145`，颜色 `(125, 70, 40, 25)` | :211 |
| 旋转 | `rotation` 由 `velocity.Length() * 0.0075` 累加（见 4.2-B） | :76 |

### 4.4 产出的弹幕 / 子 NPC

| 产出 | 类型 | 数量 | 伤害 / 速度 | 来源 |
|---|---|---|---|---|
| 弹幕 | `AstrageldonStar2`（`CatalystMod.Projectiles.Enemy`） | **5** | 伤害 = 自身 `damage`；速度 `3` px/刻，扇形 ±80 度；继承 `ai[0] = Boss whoAmI` | HostileAsteroid.cs:128-136 |
| 子体关键参数（供参考） | `AstrageldonStar2`：36×36 px、`timeLeft 180`、`penetrate -1`、`TrailCacheLength 50`、速度每步 `×1.018` 并按 `ai[1] * 1.5` 度旋转（`ai[1] = 0` 时为直线加速） | | | AstrageldonStar2.cs:44-56, :79-89 |
| 粒子 | Dust 6 / Dust 31 / 270 / `AstralRocksDust` / 烟雾 Gore | 死亡时循环 `width + height = 150` 次 | | HostileAsteroid.cs:158-186 |

### 4.5 难度 / 生成数量（投射物无 `ApplyDifficultyAndPlayerScaling`）

| 项 | 规则 | 来源 |
|---|---|---|
| 生成数量 | **不固定**：每 `90 + attackTimer2 * 2` 刻的间隔从边界轨道生成 1 颗；每次生成 `attackTimer2++`，同时 `targetOrbitalRange -= 0.4 + attackTimer2 * 0.02`，收缩到 `-136` 后停止（绝望阶段收尾） | Astrageldon.cs:1750-1776 |
| 伤害 | `damage3 = GetBaseDamage() / 2` | Astrageldon.cs:1761 |
| 彩蛋变体 | `CatalystPlayer.cs:462` 以伤害 `9750` 生成一颗（`damage >= 9000` → 跳过边界存活判定，不随 Boss 消失） | CatalystPlayer.cs:462, HostileAsteroid.cs:53 |

---

## 5. AstraBlade（星刃）

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\AstraBlade.cs`（109 行），**`class AstraBlade : ModProjectile`**

### 5.1 SetDefaults 字段

| 字段 | 值 | 单位 / 说明 | 来源 |
|---|---|---|---|
| `Projectile.width` | `20` | px = 1.25 tile | AstraBlade.cs:44 |
| `Projectile.height` | `58` | px = 3.625 tile | :45 |
| `Projectile.timeLeft` | `100` | 1.667 s | :46 |
| `Projectile.penetrate` | `-1` | 无限穿透 | :47 |
| `Projectile.friendly` | `false` | | :48 |
| `Projectile.hostile` | `true` | | :49 |
| `Projectile.tileCollide` | `false` | | :50 |
| `Projectile.ignoreWater` | `false` | | :51 |
| `Projectile.alpha` | `255` | 全透明起步，AI 淡入 | :52 |
| `Projectile.hide` | `true` | 自定义绘制 | :53 |
| `Projectile.extraUpdates` | `1` | 每刻 2 次更新 → `ai[0]` 与正弦每刻推进 2 | :54 |
| `Projectile.Calamity().DealsDefenseDamage` | `true` | Calamity 机制：命中额外削减玩家防御 | :55 |
| 命中箱 | 未重写 → 即 `20 × 58` | | — |

### 5.2 AI 行为拆解

| 阶段 | 进入条件 | 计时器 / 初值 | 关键数值 | 行为 | 来源 |
|---|---|---|---|---|---|
| **A. 淡入** | `timeLeft >= 50` 且 `alpha > 0` | — | `alpha -= 8`/更新步，clamp 到 `[0, 255]` | 从无到实体 | :71-75 |
| **B. 淡出并失去伤害** | `timeLeft < 50` | — | `alpha += 8`；**`alpha > 100` 后 `hostile = false`**（不再能伤人） | 末段 0~50 刻（约 0.83 s） | :60-70 |
| **C. 垂直正弦摆动** | 每更新步 | `ai[0]` 为相位（初始 0，每更新步 +1） | 振幅 `64 px`，向上半波 `×1.2`（= 76.8 px）；`sin(弧度(ai[0] * 1.8))`，周期 = `200` 个 `ai[0]` 单位（≈100 刻 ≈ 1.67 s） | `Center = (Center.X, oldcenter.Y + sin)` —— **X 完全不动，纯上下浮动** | :76-86 |
| **D. 初始方向记录** | `ai[0] == 0` | — | `initialDirection = ai[1] > 0 ? 1 : -1`；`oldcenter = Center` | 决定 PreDraw 水平翻转 | :81-85 |
| **E. 链式生成下一根** | `ai[0] >= 10` **且** `\|ai[1]\| > 1` | 仅触发 1 次（触发后 `ai[1] = 0`） | 偏移 `32 * direction` px；子体初速 `(0, 2)` px/刻；`ai[1] = \|ai[1]\| - 1`（保留符号） | 沿水平方向每 32 px 长出下一根刀柱，伤害/击退继承 | :88-107 |
| 生成位置（初始两根） | 由父体 | — | `Center + ((16 + Boss.width/2) * i, Boss.height/2 + 24)`，`i = -1 / +1`；初速 `(0, 2)`；`ai[1] = length * i` | | Astrageldon.cs:3040-3043 |

> 链式结构：`GroundBlades(L)` 先 `length = L + 1`，两侧各生成 1 根（`ai[1] = ±(L+1)`），每根再降 1 继续生成，直到 `|ai[1]| = 1` 停止 → **每侧 L+1 根、共 2(L+1) 根**，间距 32 px（`GroundBlades(40)` → 每侧 41 根、跨 40 段 = 1280 px）。

### 5.3 贴图动画 / 绘制缩放

| 项 | 数值 | 来源 |
|---|---|---|
| 帧动画 | **无**（未设 `Main.projFrames`） | 全文件 |
| 特殊画法 | 把贴图**按 2 px 高的横条自下而上平铺** `texture.Height + 30` 次，形成"光柱"；每条纹 alpha 递增，越过贴图高度后 `+13` 加速淡出 | :27-38 |
| 缩放 | `Projectile.scale`（默认 `1f`，未修改） | :37 |
| 翻转 | `initialDirection != 1` → `SpriteEffects.FlipHorizontally` | :37 |
| 层级 | `DrawBehind → behindNPCsAndTiles.Add(index)`（画在瓦片与 NPC 之后） | :18-21 |

### 5.4 产出的弹幕 / 子 NPC

| 产出 | 类型 | 数量 | 伤害 | 来源 |
|---|---|---|---|---|
| 弹幕 | `AstraBlade`（同类型递归 1 个） | 每根最多 **1** 个（`ai[1]` 递减，链终止于 `\|ai[1]\| = 1`） | 继承 `Projectile.damage` 与 `knockBack` | AstraBlade.cs:105 |
| 子 NPC | 无 | 0 | | |

### 5.5 难度 / 生成数量（投射物无 `ApplyDifficultyAndPlayerScaling`；难度只影响数量）

| 触发点 | 参数 | 每侧根数 | 总根数 | 来源 |
|---|---|---|---|---|
| 落地（`attackPhase == -1`，开场/循环） | `blades = 40`；专家 `+10`；死亡 `+5` | 41 / 51 / 56 | 82 / 102 / 112 | Astrageldon.cs:976-985 |
| 落地（`attackPhase 0` 或 `3`） | `blades2 = 10`；复仇 `+5`；死亡 `+5`；再 `+ (int)attackTimer2 * 10` | 11~26+ | 22~52+ | Astrageldon.cs:989-999 |
| 落地（`attackPhase 1` 或 `4`） | `blades3 = 30`；复仇 `+5`；死亡 `+5` | 31 / 36 / 41 | 62 / 72 / 82 | Astrageldon.cs:1003-1013 |
| 落地（`attackPhase 2`） | `blades4 = 15`；复仇 `+5`；死亡 `+10`；再 `+ attackTimer2 * 10` | 16~36+ | 32~72+ | Astrageldon.cs:1017-1027 |
| 伤害 | `damage2 = GetBaseDamage() / 2`，击退 `0` | | | Astrageldon.cs:3039-3043 |

---

## 6. SlimerSpear（史莱姆长矛）

文件：`src\CatalystMod.NPCs.Boss.Astrageldon\SlimerSpear.cs`（139 行），**`class SlimerSpear : ModProjectile`**

### 6.1 SetDefaults 字段

| 字段 | 值 | 单位 / 说明 | 来源 |
|---|---|---|---|
| `Projectile.width` | `48` | px = 3 tile | SlimerSpear.cs:20 |
| `Projectile.height` | `48` | px = 3 tile | :21 |
| `Projectile.friendly` | `false` | | :22 |
| `Projectile.timeLeft` | `120` | 2 s | :23 |
| `Projectile.hostile` | `true` | | :24 |
| `Projectile.alpha` | `255` | 全透明（本来就不可见） | :25 |
| `Projectile.penetrate` | `1` | **命中 1 次即消失** | :26 |
| `Projectile.netImportant` | `false` | 不同步 | :27 |
| `Projectile.tileCollide` | `false` | | :28 |
| `Projectile.hide` | `true` | | :29 |
| `Projectile.DamageType` | `DamageClass.Magic` | 伤害类型 = 魔法 | :30 |
| `Projectile.extraUpdates` | `7` | **每刻 8 次更新 → 实际位移 = 速度 × 8** | :31 |
| `const Updates` | `8` | `= extraUpdates + 1` | :12 |
| `ShouldUpdatePosition` | `true` | 正常位移 | :39-42 |
| `PreDraw` | 返回 `false` | **完全不画贴图**，视觉全靠 `NebulaFoam` 粒子 | :44-47 |
| 命中箱 | 未重写 → `48 × 48` | | — |

### 6.2 AI 行为拆解

| 阶段 | 进入条件 | 计时器 / 初值 | 关键数值 | 行为 | 来源 |
|---|---|---|---|---|---|
| **首次生成** | `runOnce` | — | 音效 `SoundID.Item62`（`Volume * 0.7`、`Pitch + 0.5`） | 播放一次 | :49-62 |
| **A. 飞行** | 每更新步 | `counter++`（每刻 +8） | 初速由 NovaSlimer 传入：方向 × `14` px/刻；`extraUpdates = 7` → 实际 **112 px/刻 ≈ 7 tile/刻** | 直线飞行（自身不施加速度/重力） | NovaSlimer.cs:270；SlimerSpear.cs:137 |
| **B. 拖影粒子** | `counter > 0` | 每更新步 | 插值 `i = 0, 0.5`，`NextFloat(1) <= 0.6`（≈60%）时补 1 颗 `NebulaFoam` | 沿 `oldPosition → Center` 插值 | :64-75, :96-99 |
| **C. 环状喷射** | `(int)(counter % 16) == 1` | 每 16 个更新步（= 每 2 刻） | 半径 `rand(8, 10)`，X 压扁 ×0.5；`i = 0..345` 步长 15（24 个方向） | 每个方向各 1 颗 `NebulaFoam` | :100-112 |
| **D. 随机尘** | 每更新步 | `NextBool(3)` / `NextBool(3)` | 前三处独立 1/3 判定：`NebulaFoam`（边缘速度 4）、Dust 242（`scale 2.5`）、Dust 242（`scale 2.25` + 正弦横向摆动 `16 * -direction`） | 纯视觉 | :113-135 |
| **E. 死亡** | `timeLeft` 归零或命中 | — | 36 颗 `NebulaFoam`，速度 `rand(0.5, 8)` 全向 + 自身速度 ×0.5 | 爆炸 | :77-89 |
| **F. 命中玩家** | `OnHitPlayer` | — | — | 调 `CatalystPlayer.IncreaseInterstellarCorruption(target)` | :34-37 |

**命中附加状态（`InterstellarCorruption`）**：
- 首次施加 `2400` 刻（40 s）（CatalystPlayer.cs:589）
- 重复施加时长公式 `(2 + (buffTime - 1) / 1200) * 1200` 刻，且 `delayCorruption = 40` 刻内不会重复刷新（CatalystPlayer.cs:585-599）
- 玩家侧每 `1200` 刻 = 1 级：`lifeRegen -= level`；**`level >= 4`（即 buffTime ≥ 4800）直接 `Player.KillMe` 或 `Hurt(statLifeMax*4 + statDefense*4 + endurance*40)`**（InterstellarCorruption.cs:59-107）
- 实际含义：40 s 内被第 2 根矛命中（2400 → 3600 → 4800）即致死

### 6.3 贴图动画 / 绘制缩放

| 项 | 数值 | 来源 |
|---|---|---|
| 贴图 | **不绘制**（`PreDraw` 返回 `false`） | :44-47 |
| 视觉替身 | `NebulaFoam` 粒子（`ParticlePlayer.foamParticleList1`），缩放 `1.1~1.575`，衰减系数 `0.9775~0.99` | :72-135 |
| 帧动画 | 无 | — |
| 缩放 | 无（不绘制） | — |

### 6.4 产出的弹幕 / 子 NPC

| 产出 | 类型 | 数量 | 来源 |
|---|---|---|---|
| 弹幕 | 无 | 0 | 全文件无 `Projectile.NewProjectile` |
| 子 NPC | 无 | 0 | |
| 粒子 | `NebulaFoam` | 飞行中每 2 刻 24 颗 + 死亡 36 颗 | :100-112, :83-88 |

### 6.5 难度 / 生成数量（投射物无 `ApplyDifficultyAndPlayerScaling`）

| 项 | 规则 | 来源 |
|---|---|---|
| 生成源 | **每个 NovaSlimer 自毁时生成 1 根**；伤害硬编码 `60`、击退 `0`、初速 14 px/刻 | NovaSlimer.cs:263-272 |
| 数量上限 | = 同时自毁的 NovaSlimer 数量（父体最多 24 只 → 最多 24 根） | 见 §2.5 |
| 难度分支 | 无（伤害/速度不随专家/大师变化） | — |

---

## 7. DamageTables.cs —— 与本 Boss / 召唤物相关的伤害规则

文件：`src\CatalystMod.NPCs\DamageTables.cs`（366 行），`class DamageTables : GlobalNPC`

### 7.1 结论先行

| 结论 | 依据 |
|---|---|
| **六个召唤物/投射物中，只有 `Astrageldon` 本体登记了减伤表**；`NovaSlime`、`NovaSlimer` 没有任何条目 | 全文件 grep 仅命中 `Astrageldon`（:257） |
| 未登记的 NPC 走 `DamageReductionTable` 默认结构体，`ItemDR / ProjDR / ProjStealthDR` 均为 `null` → 三个查询函数一律返回 `1f`（**无任何减伤/免疫**） | DamageTables.cs:55-115 |
| 因此 `NovaSlime` / `NovaSlimer` 吃全额伤害，且 `CanBeHitByItem` 不会拦截任何物品（`FullItemResistance` 为 false） | :352-359 |
| 全局「时间/血量进度」除算减伤 `ReduceDamage_Reactive` 对**所有** NPC 生效（含小怪），只要 `CatalystNPC.KillTime != 0 && TimedDRDivisor > 0` 且非 BossRush | :305-340 |

### 7.2 Astrageldon 本体减伤表（DamageTables.cs:257-284）

构造签名：`DamageReductionTable(projDR[], projStealthDR[], ItemDR[])`（:117-134），数值为**伤害倍率**（< 1 即减伤，> 1 即易伤）。

**普通投射物减伤（17 条，:259-275）**

| 键 | 倍率 | 备注 |
|---|---|---|
| `181` / `189` / `566` | `0.85` | 原版 `ProjectileID` 硬编码整数（源码未写名称） |
| `617` / `619` / `620` / `618` | `0.75` | 同上 |
| `WitherBolt` | `0.2` | |
| `Hiveling` | `0.2` | |
| `SpikecragSpike` | `0.2` | |
| `LeonidCometBig` | `0.8` | |
| `LeonidCometSmall` | `0.9` | |
| `LeonidProgenitorBombshell` | `0.8` | |
| `LeonidStar` | `0.9` | |
| `DukesDecapitatorProj` | `0.4` | |
| `DukesDecapitatorBubble` | `0.4` | |
| `SubductionFlameburst` | `0.8` | |

**潜行攻击额外减伤（6 条，:276-283）**

| 键 | 倍率 |
|---|---|
| `RegulusRiotProj` | `0.8` |
| `FantasyTalismanStealth` | `0.8` |
| `MalachiteProj` | `0.7` |
| `MalachiteBolt` | `0.7` |
| `MalachiteStealth` | `0.7` |
| `AntumbraShardProjectile` | `0.15` |

**物品减伤（0 条）**：`ItemDR` 传空数组 → 对物品无减伤、无拦截。

### 7.3 相关机制（非小怪专属，但决定小怪实际承伤）

| 机制 | 规则 | 来源 |
|---|---|---|
| `ReduceDamage_Reactive` | 设 `timeLeft = ActiveTime / KillTime`、`healthTime = 1 - life/lifeMax`；若 `healthTime > timeLeft` 则 `SourceDamage /= divisor`（`TimedDRDivisor`），并按 `TimedDRHealthCutting` 线性过渡；潜行攻击把 `KillTime += 1200`，真近战 `KillTime -= 900`；`BossRushActive` 时跳过 | :305-340 |
| `ModifyHitByProjectile` | 顺序：Reactive → 潜行额外表 → 普通表（连乘） | :293-303 |
| `ModifyHitByItem` | Reactive（按真近战算）→ `ItemResistance` | :361-365 |
| `AstrageldonShootData` | 武器级整体削弱表：`ConferenceCall` = `0.9`；`Vortexpopper` = `0.9`，其子投射物 `HyperiusBulletProj` `0.9`、`HyperiusSplit` `0.9`、`BubonicRoundProj` `0.8` | :285-290 |
| `DRParameter` | 支持 `StealthOnly` 标记（`DRParameter(float dr, bool stealthOnly = false)`），本次 Astrageldon 条目并未使用该形式 | :156-177 |

---

## 附录 A：父体伤害链（子体 `damage` 的来源）

| 步骤 | 计算 | 结果 | 来源 |
|---|---|---|---|
| 1. 基础 | `SetDefaults`：`NPC.damage = 160` | 160 | Astrageldon.cs:390 |
| 2. 引擎难度 | 专家模式引擎先 `damage × 2`（`ScaleStats_ByDifficulty`，在 mod 钩子**之前**执行） | 320 | 原版 |
| 3. 标准化 | `damage = damage * 220 / 160 / 2`（整数除法） | **220** | Astrageldon.cs:413 |
| 4. 复仇模式 | `damage = (int)(1.12f * damage)` | **246** | Astrageldon.cs:414-418 |
| 5. 死亡模式 | 再 `(int)(1.12f * damage)` | **275**（复仇+死亡叠加时） | Astrageldon.cs:424-427 |
| 6. 子体接收 | 多数为 `damage2 = NPC.GetBaseDamage() / 2` | **110**（专家）/ 55（经典） | 见各类 4.x/3.5/5.5 小节 |

> **修正说明**：tModLoader 的 `ApplyDifficultyAndPlayerScaling` 在 `NPC.ScaleStats` 末尾调用，此时原版难度倍率（专家伤害 ×2、生命 ×2）已经生效，所以第 2 步的输入是 320 而不是 160；文末的 `/2` 正是用来抵消这次 ×2 的。经典模式没有 ×2 项，同一公式给出 `GetBaseDamage() = 110`、子体 `55`。本表按**专家**给出（与主文档一致），复仇 246 / 死亡叠加 275。

## 附录 B：主要生成关系图

```
Astrageldon (Boss, 专家 156000 HP / 接触 220)
├─ NovaSlime ×(5 + 专家1 + 死亡1 + attackTimer2/2)   ← attackPhase 2 落地           Astrageldon.cs:1028-1048
├─ NovaSlimer ×2×min(6+循环数+难度, 12) / ×2×4~6     ← attackPhase 12 / 二阶段开场   Astrageldon.cs:1092, :1837
│   └─ SlimerSpear ×1（自毁，dmg 60，14 px/刻）                                      NovaSlimer.cs:270
├─ AstraRock ×2×amt（公转，dmg = base/2 × 0.75）     ← SummonRocks(20) → 40 颗        Astrageldon.cs:1593, :2973-2989
├─ AstraRock ×2×amt（抛落，dmg = base/2）            ← GroundRocks / 二阶段爆发×8     Astrageldon.cs:1116-1122, :3056-3071, :1714-1718
├─ AstraBlade ×2×(L+1)（链式，间距 32 px）           ← 每次落地 GroundBlades          Astrageldon.cs:985-1027, :3026-3045
└─ HostileAsteroid ×N（绝望阶段轨道坠落）                                            Astrageldon.cs:1750-1766
    └─ AstrageldonStar2 ×5（dmg = 自身伤害，3 px/刻 扇形）                            HostileAsteroid.cs:128-136
```

## 附录 C：不确定 / 未纳入本次结论的项

| 项 | 状态 |
|---|---|
| `NovaSlime.slamDelay = 50`、`NovaSlimer.TelegraphDelay = 40` | 两个常量**声明后未被任何代码引用**（全仓库 grep 仅命中声明行），实际逻辑使用字面量 |
| 原版 `ProjectileID` 181/189/566/617/618/619/620 的名称 | 源码直接写死整数，未附名称；本表不臆测对应原版弹幕 |
| `GetBaseDamage()` 的难度换算 | 属于 tModLoader API，不在反编译模组源码内；本表只给公式与父体原始数值 |
| 六个类的 `buffImmune` | 均未赋值（默认空表），没有任何 Buff 免疫 |

