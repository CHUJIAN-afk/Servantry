# Confluence 1.20.1 → Summoner 1.21.1 召唤物迁移指南

来源 A：`D:\IDEA\confluence-1.20.1\ConfluenceOtherworld\src\main\java\org\confluence\mod\`
目标 B：`D:\IDEA\Summoner\src\main\java\first\summoner\`
框架：Lyra（`D:\IDEA\Lyra`，已 publishToMavenLocal）

## 用户已确认的决策

1. 6 个新召唤物（小鸟/铁傀儡/史莱姆/小雪怪/蜘蛛/吸血鬼青蛙）**连法杖一起迁移**。
2. 6 个已存在召唤物（致命球/眼球激光塔/黄蜂/小鬼/禁戒遗迹/血蝙蝠）**只迁 AI/模型/动画，保留 Summoner 现有数值**。
3. 血蝙蝠：类名与注册 id 都对齐 confluence → `SanguineBat` / `sanguine_bat`（原 `BloodBat` / `blood_bat`）。
4. 小鸟**不迁**鸟巢玩家渲染层（BirdNestLayer）。
5. Lyra 缺的 API → **往 Lyra 补**并重新 publishToMavenLocal。
6. 眼球激光塔：**保留 Summoner 现有的激光攻击模式**，另把 confluence 的双部件（base/head）模型与 `ICarryMinion` 扛肩一起迁。
7. 冲突时**一律以 confluence 为准**（含小鬼阵型待机、禁戒遗迹同数量增伤、血蝙蝠绕圈待机改回 confluence 写法）。
8. 新法杖**缺的贴图复用现有资源，战利品表先不加**。

## 资源路径约定（已按 Lyra geo 约定复制完毕）

`AbstractAttachmentEntityGeoRenderer` 基础路径 `summoner:entity/summon/<name>` 解析为：
- `assets/summoner/geo/entity/summon/<name>.geo.json`
- `assets/summoner/textures/entity/summon/<name>.png`
- `assets/summoner/animations/entity/summon/<name>.animation.json`

已复制的 base name：
`deadly_sphere_spikes|_flames|_blade`、`eye_laser_turret_base`、`eye_laser_turret_head`、
`finch_baby`、`hornet_baby`、`summon_imp`、`iron_golem_32`、`ruin_relic`、`sanguine_bat`、
`slime_baby`、`summon_snow_flinx`、`spider`（+ `spider/jumper.png`、`spider/dangerous.png`）、`vampire_frog`

另：`textures/entity/model/stinger.png` 已复制为 `lyra_model/json/projectile/hornet_stinger/stinger.png`（黄蜂毒刺自绘两面片用）。
法杖贴图已复制到 `assets/summoner/textures/item/`：`finch_staff`、`finch_staff_empty`、`iron_golem_staff`、`slime_staff`、`snow_flinx_staff`。
**confluence 缺**：`vampire_frog_staff.png`、`spider_staff.png` → 复用现有贴图。

## 框架 API 映射（A → B）

| A | B |
|---|---|
| `RegistryObject<? extends AttachmentEntityType<?>>` 构造参数 / `type.get()` | `Holder<AttachmentEntityType<?>>` / `type.value()` |
| `void attack(LivingEntity, float, int)` | `boolean attack(LivingEntity, float, int)`（返回是否命中） |
| `AttachmentEntityDamageSource getDamageSource()` | `DamageSource getDamageSource()` |
| `Immunity` / `confluence$getImmunityType()` / `immunityDuration` | **已移除**；`Immunity.Type.STATIC` 的等价做法是 `ImmunityData`，或直接删掉该覆写 |
| `ModPrefix prefix` / `getPrefix()` / `setPrefix()` | **已移除**，全部删除相关调用 |
| `MinionSlotType.STREAM_CODEC` | `LyraStreamCodecs.MINION_SLOT_TYPE` |
| `LibStreamCodecUtils.VEC_3` | `LyraStreamCodecs.VEC_3` |
| `Mth.clamp` | `Math.clamp` |
| `list.get(0)` / `add(0,x)` / `remove(size-1)` | `getFirst()` / `addFirst` / `removeLast` |
| `Confluence.asResource(...)` | `Summoner.rl(...)`（**会 toLowerCase**）或 `ResourceLocation.fromNamespaceAndPath` |
| `SummonerAttachmentTypes.X` | `LyraAttachmentRegister.X` |
| `SummonerHelper.get(p)` | `LyraHelper.get(p)` |
| `LibDamageTypes.SUMMONER` | `LyraDamageRegister.Summon` |
| `ConfluenceMagicLib.MINION_CAPACITY` / `SENTRY_CAPACITY` | `LyraAttributeRegister.MinionMaxCount` / `SentryMaxCount` |
| `TCUtils.hasType(owner, TCItems.HIVE$PACK)` | `CuriosUtil.isEquipped(owner, SummonerCurioRegister.HivePack.get())` |
| GeckoLib `software.bernie.geckolib.core.*` | `software.bernie.geckolib.*`（**去掉 `core.` 段**）；`core.object.Color` → `util.Color` |
| `AttachmentEntityDamageSource.getArmorPenetration/setArmorPenetration` | `getArmorPierce/setArmorPierce` |
| `WhipMarkTracker` / `getMarkTarget()` / `isSummonMarkTarget(x)` | `SummonMarkTracker` / `getTarget()` / `isTarget(x)` |
| `ModEffects.ACID_VENOM` 等 confluence 专属内容 | 见"待补"一节 |

### 关键陷阱

- **不要在 minion 的 attack 路径里再加召唤标记增伤**。B 已把该逻辑放进 `first.lyra.mixin.LivingEntityMixin` 的 `hurt` 包装里（SummonDamage 倍率 + 0.85~1.15 随机 + `getDamageModifier`），重复加会双重生效。
- A 的 20 个实体类型注册在 confluence 里；**Lyra 完全没有实体类型注册表**。B 侧注册点：
  `first.summoner.register.SummonerAttachmentEntityRegister`（`DeferredRegister.create(LyraRegistries.ATTACHMENT_ENTITY_TYPES, MODID)`）。
  渲染器注册点：`first.summoner.register.SummonerAttachmentEntityRenderRegister`。
- 渲染器 `AbstractAttachmentEntityGeoRenderer` 在 B 里**不再继承** `AbstractAttachmentEntityRenderer`，且 `createContext` 是 6 参 `GeoRenderContext` 形式：
  `protected GeoRenderContext createContext(T entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, PathNode visualNode)`
  构造器传 base path：`super(Summoner.rl("entity/summon/xxx"))`，或 `super(ctx -> ...)` 动态解析。

## 待补进 Lyra 的内容

- `ICarryMinion<T extends Minion>`（接口：`isOnCarry`/`setOnCarry`/`getCarryPathNode(float)`/`onCarry()`/`canCarry()`/`getMinion()`/`tryCarry()`）
  以及 `AttachmentEntity.tickCurrentPathNode()` 里被删掉的 carry 钩子 + `setHasCarryMinion(true)`（`AttachmentEntityData` 侧访问器仍在）。

## 12 个召唤物清单与落点

| 中文 | 类名（迁移后） | 基类 | goal 目录 | 渲染器 base path |
|---|---|---|---|---|
| 致命球 | `DeadlySphere`（改造） | MomentumMinion | `goal/deadlysphere` | 动态：`deadly_sphere_{spikes\|flames\|blade}` |
| 眼球激光塔 | `EyeLaserTurret`（改造，保留激光） | MomentumMinion | 新增 2 goal | 双部件 `eye_laser_turret_base` + `_head` |
| 小鸟 | `Finch`（新建） | MomentumMinion | `goal/finch` | `finch_baby` |
| 黄蜂 | `Hornet`（改造） | MomentumMinion | `goal/hornet` | `hornet_baby` |
| 小鬼 | `Imp`（改造） | MomentumMinion | `goal/imp` | `summon_imp` |
| 铁傀儡 | `IronGolem`（新建） | GroundMinion | `goal/ironGolem` | `iron_golem_32` |
| 禁戒遗迹 | `RuinRelic`（改造） | MomentumMinion | `goal/ruinRelic`（现 `ruinSigil`） | `ruin_relic` |
| 血蝙蝠 | `SanguineBat`（重命名自 BloodBat） | Minion | `goal/sanguineBat` | `sanguine_bat` |
| 史莱姆 | `Slime`（新建） | GroundMinion | `goal/slime` | `slime_baby` |
| 小雪怪 | `SnowFlinx`（新建） | GroundMinion | `goal/snowFlinx` | `summon_snow_flinx` |
| 蜘蛛 | `Spider`（新建） | GroundMinion | `goal/spider` | `spider`（+ 变体贴图） |
| 吸血鬼青蛙 | `VampireFrog`（新建） | GroundMinion | `goal/vampireFrog` | `vampire_frog` |

投射物：黄蜂 `HornetStinger`（自绘两面片）、小鬼 `ImpFireball`（**不渲染，只粒子**）、禁戒遗迹 `ForbiddenOrb`、眼球塔 `EyeFireball`（confluence 有但按"保留激光"不迁）。

## 动画

B 目前**全项目没有动画层**。迁移 = 把 A 各 minion 的 `registerControllers(AnimatableManager.ControllerRegistrar)` 补进对应类，含 `attackTime` 同步字段：
- 致命球：`"deadly_sphere"` → `form==2 ? "misc.idle" : "attack.strike"`
- 黄蜂：`"hornet"` → `isAttackAnimationActive()`（8 tick）`"attack.cast"` : `"misc.idle"`
- 小鬼：`"imp"` → 18 tick 窗口 `"attack.cast"` : `"misc.idle"`
- 血蝙蝠：`"sanguine_bat"` → `"fly"`
- 其余（小鸟/铁傀儡/史莱姆/小雪怪/蜘蛛/吸血鬼青蛙/禁戒遗迹）动画文件存在但需按各自 clip 名接入。
