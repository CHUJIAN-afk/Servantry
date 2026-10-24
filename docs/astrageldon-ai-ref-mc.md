# 附录 C：Summoner 工程实现约定 + 表现层素材清单

> 由 mc-notes 读取 `D:\IDEA\Summoner` 现有代码与解包资源后整理（快照时间见文末说明）。前半是 MC 侧注册/渲染/同步的现状，后半是泰拉侧的文本、贴图、音效、BGM 清单。

# 末世星史莱姆的 Minecraft 侧实现约定

依据 `D:\IDEA\Summoner\docs\terraprism-ai.md` 的行文体例整理：分节、以陈述句直给数值与路径、代码片段只留关键行、不写客套话，末尾交代验证范围。工程为 NeoForge 1.21.1（`neo_version=21.1.238`）、Lyra `1.21.1.16`、GeckoLib `4.8.4`、Curios `9.5.1`。任务一记录代码约定，任务二记录解包出的表现层素材。

工程里已经有一份 Astrageldon 的空壳：实体类、实体注册、渲染器、`Phase` 枚举都建了，但 `registerGoals`、`registerControllers` 为空，渲染器没有调用 `super.render`，属性只在 `Event` 里挂上了。下文把"现状"和"可复用约定"分开写，凡是空壳的地方都点明。读取期间工作区仍有并发改动（`Astrageldon.tick` 新增 `removeAllEffects()`、删除空的 `push` 覆写），现状描述以本次读取到的版本为准。

## 一、实体注册

Minecraft 实体走标准 `DeferredRegister`，一个 `EntityType` 一个 `DeferredHolder`，全部集中在 `src\main\java\first\summoner\register\SummonerEntityRegister.java`：

```java
private static final DeferredRegister<EntityType<?>> Register = DeferredRegister.create(Registries.ENTITY_TYPE, Summoner.MODID);

public static final DeferredHolder<EntityType<?>, EntityType<Astrageldon>> ASTRAGELDON =
        Register.register("astrageldon", location -> EntityType.Builder.of(Astrageldon::new, MobCategory.MONSTER)
                .sized(3F, 3F)          // 3 格宽 × 3 格高（原版凋灵 0.9 × 3.5）
                .clientTrackingRange(128)
                .updateInterval(1)      // 每 tick 同步一次位置
                .build(location.getPath())
        );
```

`updateInterval(1)` 与 `clientTrackingRange(128)` 是 Boss 的硬要求：`EntityType.Builder` 的默认值是 3 tick 与 5 格，高频位移与长距离索敌都会露馅。`build(location.getPath())` 用注册名当数据修复用的 id，不要写别的字符串。

注册在主类 `Summoner` 构造里统一挂到 mod 事件总线，顺序无关但必须有一行 `SummonerEntityRegister.register(eventBus)`。同父目录下的 `SummonerRegistries` 演示了另一条路：Lyra 自建注册表用 `RegistryBuilder`（`new RegistryBuilder<>(KEY).sync(true).create()` + `NewRegistryEvent`），仆从与弹幕用的是 `LyraRegistries.ATTACHMENT_ENTITY_TYPES`，**不是** vanilla `ENTITY_TYPE`。两套体系不能混用，见第七节。

渲染器注册必须与实体注册分离。`SummonerAttachmentEntityRegister` 的类注释写得很明确：

> 渲染器引用客户端类，绝不能在本类中直接引用，否则服务端加载本类时会被 RuntimeDistCleaner 拦截（Attempted to load class ... for invalid dist DEDICATED_SERVER）导致崩溃。

所以 `SummonerEntityRenderRegister` 用 `@EventBusSubscriber(modid = Summoner.MODID)`（不区分端，因为 `EntityRenderersEvent.RegisterRenderers` 只在客户端触发），而 Lyra 那套用 `@EventBusSubscriber(value = Dist.CLIENT)` + `FMLClientSetupEvent`。

## 二、Boss 与敌对实体

没有 Boss 基类。`Astrageldon extends Monster`，直接继承原版怪物，只额外实现 `GeoAnimatable` 与 Lyra 的 `IPathEntity`：

```java
public class Astrageldon extends Monster implements GeoAnimatable, IPathEntity {
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private Phase phase = Phase.Phase11;
```

属性用静态方法返回 `AttributeSupplier.Builder`，在 `common\Event.java` 里挂 `EntityAttributeCreationEvent`。这是一对必须同时存在的两段，缺任一段进游戏就是崩服：

```java
// Astrageldon.createAttributes()，数值直接照抄泰拉侧
return Monster.createMonsterAttributes()
        .add(Attributes.MAX_HEALTH, 800.0)
        .add(Attributes.MOVEMENT_SPEED, 0.25)
        .add(Attributes.ATTACK_DAMAGE, 15.0)
        .add(Attributes.ARMOR, 10.0)
        .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
        .add(Attributes.FOLLOW_RANGE, 128.0);

// Event.onEntityAttributeCreation
event.put(SummonerEntityRegister.ASTRAGELDON.get(), Astrageldon.createAttributes().build());
```

**Goal 体系基本没用起来。** `registerGoals()` 是空方法，索敌写在 `tick()` 里：

```java
@Override
protected void registerGoals() {
}

@Override
public void tick() {
    super.tick();
    if (!level().isClientSide()) {
        Vec3 vec3 = getDeltaMovement();
        setDeltaMovement(new Vec3(vec3.x() * 0.92, vec3.y(), vec3.z() * 0.92)); // 水平阻尼
        if (getTarget() == null) {
            Player nearestPlayer = level().getNearestPlayer(this, 128);
            ...
        }
    }
    tickPath();  // 位置由 Lyra 路径系统接管
}
```

全工程没有任何 `Goal` 子类服务于怪物实体：仆从的目标在 `AttachmentEntityGoal` 里，类型参数是 `AttachmentEntity`，套不到 `Monster` 上。所以 Boss 的动作应该自己写成显式状态机（`Phase` 枚举 + 每阶段 tick 逻辑），而不是硬塞 vanilla Goal；vanilla `Goal` 只适合"走近然后打"这类兜底。

阶段枚举已经按泰拉侧的攻击编号铺好了，17 个状态，`ordinal() < 7` 用来切一阶段与二阶段：

```java
public enum Phase {
    Phase11, Phase12, Phase13, Phase14, Phase15, Phase16, Phase17,   // Phase17 = 1.5 阶段
    Phase21, Phase22, Phase23, Phase24, Phase25, Phase27, Phase28,
    Phase29, Phase210, Phase211,
}
```

几处刻意的空实现，含义要记住：

- `addEffect(...)` 返回 `false`、`forceAddEffect(...)` 空体，`tick()` 里每 tick 调一次 `removeAllEffects()` —— Boss 既拒收状态效果又主动清除，不用自己维护免疫表。
- `KNOCKBACK_RESISTANCE = 1.0` —— 不被推动（早先还有一个空的 `push(...)` 覆写，已被删除，靠属性就够）。
- `shouldRender(...)` 恒 `true` —— 不做视锥剔除，巨型 Boss 的包围盒容易误剔。
- `setNoGravity(false)`、`noPhysics = false` —— 保留重力与方块碰撞（虽然有路径接管位移，仍要留碰撞用于落地判定）。
- `IPathEntity.tickPath()` 在 tick 末尾调用，位移与朝向由此驱动；渲染侧用 `EntityPathRender.translate/rotate` 从同一份路径取插值位姿，服务端与客户端不各算一套。

## 三、弹幕与投射物

射弹全部继承 Lyra 的 `Projectile`，注册在 `SummonerAttachmentEntityRegister` 里、类型是 `AttachmentEntityType`，并显式声明两类碰撞接口。`common\projectile\Laser.java` 是最好的最小样本：

```java
public class Laser extends Projectile implements IEntityCollision<Laser>, IBlockCollision<Laser> {

    public Laser() {
        super(SummonerAttachmentEntityRegister.LASER);
        setDrag(1);              // 无空气阻力
        setGravity(0);
        setMaxTickCount(100);    // 5 秒后自毁
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        HitContext hit = hitContexts.getFirst();
        LivingEntity target = hit.entity();
        attack(target, getDamage(), 0);          // 第三参数是护甲穿透
        target.setRemainingFireTicks(Math.min(target.getRemainingFireTicks() + 20, 120));
        currentPathNode = currentPathNode.modifyPos(hit.hitPoint());  // 停在命中点再移除
        setRemove();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.03, -0.03, -1, 0.03, 0.03, 0);  // 局部 -Z 方向一格的细长线段
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.03, -0.03, -0.03, 0.03, 0.03, 0.03);
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setRemove();
    }
}
```

要点：`getHitbox()` 是**局部空间**的判定形状，随路径位姿一起旋转；激光这种要拉成线段，火球这种用立方体。命中后把 `currentPathNode` 移到命中点再 `setRemove()`，否则拖尾/爆点会停在上一帧的位置。穿透弹幕在 `onCollisionAttack` 里自己计 `hitCount`（`FrostBolt` 穿透 3 次，每次 `setDamage(getDamage() * 0.85f)`）。

生成弹幕的统一写法（`goal\imp\ImpAttackGoal.java`）：

```java
ImpFireball fireball = new ImpFireball();
fireball.setOwner(minion.getOwner());
fireball.init(new PathNode(minion.getPos(), direction));  // 入口：起点 + 单位方向
fireball.copyAttributes(minion);                          // 继承主人的伤害/暴击等属性
fireball.setVelocity(direction);
LyraHelper.get(minion.getOwner()).add(fireball);          // 归到玩家名下的附件实体组
```

`LyraHelper.get(owner).add(...)` 是关键：附件实体不属于世界，挂在玩家身上随玩家卸载/切维度一起走，Boss 的弹幕用同一套只需把 owner 换成 Boss 的 `AttachmentEntityGroup`（或者按需要新建），生命周期要自己管。

## 四、客户端渲染

目录名就是 `client\entityRenerer\`（拼写错误但保留），里面只有 `AstrageldonRenderer`。Lyra 附件实体的渲染器在另一个目录 `client\attachmentEntityRenderer\{minion,projectile}\`，两套不要混。

现有渲染器如下，逐行看都有问题：

```java
public class AstrageldonRenderer extends GeoEntityRenderer<Astrageldon> {

    public AstrageldonRenderer(EntityRendererProvider.Context context) {
        super(context, SummonerEntityRegister.ASTRAGELDON.get());
    }

    @Override
    public void render(@NotNull Astrageldon entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        EntityPathRender.translate(entity, poseStack, partialTick);
        EntityPathRender.rotate(entity, poseStack, partialTick);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(...) { return true; }
}
```

两个坑：一是 `render` 里**没有调用 `super.render(...)`**，所以等于什么都不画；正确形态是把 `EntityPathRender` 的两句包在 `super.render(...)` 外层（路径位姿要作用到模型整体），例如 `pushPose → EntityPathRender.translate/rotate → super.render(...) → popPose`，而不是自己开关一次 PoseStack 就完事。二是 GeckoLib 的 `GeoEntityRenderer` 需要同名 `GeoModel`，而工程里没有任何 `GeoModel` 子类（全局 grep `GeoModel` 无结果），所以要么补 `GeoModel`，要么改用 Lyra 的渲染栈。

GeckoLib 与 Lyra 的分工实测如下：

- GeckoLib 在仆从侧只用来**驱动动画状态**：`AnimatableManager.ControllerRegistrar` + `AnimationController` + `RawAnimation`（`Imp`、`Spider`、`Hornet`、`VampireFrog` 等 12 个类）。`AttachmentEntity` 基类已 `implements GeoAnimatable`（解包 `Lyra-1.21.1.16.jar` 的 `first/lyra/common/attachmentEntity/AttachmentEntity.class` 常量池里同时含 `GeoAnimatable` 与 `AnimatableManager`），所以子类不用再声明接口。
- 模型加载与绘制交给 Lyra：`AbstractAttachmentEntityGeoRenderer<T>`，构造参数是一个 `ResourceLocation` 模型 id。

```java
public class ImpRenderer extends AbstractAttachmentEntityGeoRenderer<Imp> {
    public ImpRenderer() {
        super(Summoner.rl("entity/summon/summon_imp"));   // geo 模型 id
    }

    @Override
    protected RenderContext<Imp> createContext(Imp imp, PathNode visualNode, float partialTick, int packedLight) {
        return super.createContext(imp, visualNode, partialTick, packedLight)
                .packedLight(LightTexture.FULL_BRIGHT)     // 常亮，自带发光感
                .model(new ModelContext().scale(0.8F).translateOffset(0, -1.15F, 0).rotationOffset(180, 0, 0));
    }
}
```

Boss 如果沿用 GeckoLib 路线，`ModelContext` 这套缩放/偏移/翻正（`rotationOffset(180, 0, 0)` 是 Blockbench 坐标 Y 轴朝下导致的）同样可以直接抄。

资源路径约定（`assets\summoner\` 下）：

| 用途 | 路径 | 现状 |
| --- | --- | --- |
| GeckoLib geo 模型 | `geo\entity\summon\*.geo.json` | 只有 19 个仆从/哨戒，无 Boss |
| GeckoLib 动画 | `animations\entity\summon\*.animation.json` | 同上 |
| geo 模型贴图 | `textures\entity\summon\*.png` | 同上 |
| Lyra 独立 JSON 模型 | `lyra_model\json\<folder>\<id>\<id>.json` + 同名 `.png` | `minion` / `projectile` 两个子目录 |
| 独立模型贴图 | `lyra_model\textures\*.png` | `imp_fireball` / `meteor` / `petal` |

独立模型必须在 `SummonerModelRegister` 里登记，否则 `ModelEvent.RegisterAdditional` 不会加载，而且 `build.gradle` 会做一次目录镜像（原生模型烘焙只认 `models/`）：

```groovy
tasks.named('processResources', ProcessResources).configure {
    from("src/main/resources/assets/${mod_id}/lyra_model/json") {
        include '**/*.json'
        into "assets/${mod_id}/models/lyra_model/json"
    }
    from("src/main/resources/assets/${mod_id}/lyra_model/json") {
        include '**/*.png'
        into "assets/${mod_id}/textures/lyra_model/json"
    }
}
```

结论：Astrageldon 现在**没有任何模型资源**，geo/anim/texture 三样都要新建。贴图尺寸在任务二给出（`Astrageldon.rawimg` 574908 字节，同目录还有独立的 Halo/Laser/Rocks 图层，说明泰拉侧是分层叠画的，MC 侧要么合成到一张贴图，要么做成多部件 GeoModel）。

## 五、音效、粒子、Boss 血条

音效用 `DeferredRegister<SoundEvent>` + `SoundEvent.createVariableRangeEvent`，名字必须与 `sounds.json` 的键一致：

```java
private static DeferredHolder<SoundEvent, SoundEvent> create(String name) {
    return Register.register(name, () -> SoundEvent.createVariableRangeEvent(Summoner.rl(name)));
}

public static final DeferredHolder<SoundEvent, SoundEvent> DoomsdayStarPhase1 = create("doomsday_star_phase_1");
public static final DeferredHolder<SoundEvent, SoundEvent> DoomsdayStarPhase2 = create("doomsday_star_phase_2");
```

`entity.summoner.doomsday_star`（末世星）与这两条音乐已经在工程里预留好了，只是实体注册名用的是 `astrageldon`，两者对不上，落地时需要统一（要么加 `astrageldon` 的 translations，要么把实体改名）。BGM 在 `sounds.json` 里必须带 `"stream": true`，两分钟以上的 ogg 不流式会卡加载：

```json
"doomsday_star_phase_1": { "sounds": [ { "name": "summoner:doomsday_star_phase_1", "stream": true } ] }
```

播放 BGM 不用 `level.playSound`，用 `client\MusicManager.java`。它是一个自管的循环 `AbstractSoundInstance`，`play(sound)` 把 `time` 置 10 tick（留一个淡出余量），`stop` 置 0，每 tick 检查是否还在响、不响就重放；另外通过 `SoundManagerAccessor` / `SoundEngineAccessor` 两个 mixin 把同通道（`SoundSource.MUSIC`）的原版音乐全部停掉，保证 Boss 战不被背景乐干扰。`Astrageldon.tick()` 的客户端分支里已经写好了调用点，但整段被注释掉了，等模型和音乐资源到位后取消注释即可。

普通音效走 `Playable.play(holder, level, pos, soundSource)`（见 `ImpAttackGoal`），按位置衰减播放，服务端调用即可。

粒子统一走 `utils\ParticleHelper.java`，链式配置，服务端不直接发包而是累积到 `ParticlesData` 附件里由 tick 末批量下发：

```java
ParticleHelper.create(level)
        .generic(GenericParticleBuilder.create()
                .centerColor(0xe1c316).edgeColor(0xff7506)
                .lifetime(6).lifetimeRandom(4)
                .spin(0.25f).spinRandom(0.25F)
                .scale(0.015f).scaleRandom(0.015f))
        .pos(getPos())
        .offset(0.2)
        .emit();
```

`count <= 0` 表示单发精确速度，`count > 0` 表示多粒子随机散射（`speed`/`spread` 生效）。颜色是 `0xRRGGBB` 双色渐变，很适合做星辉的青紫发光。

**Boss 血条：工程里零实现。** 全局 grep `BossBar` / `ServerBossEvent` / `BossEvent` 均无命中。要自己做，参照原版凋灵的写法（下面是建议形态，不是仓库现有代码）：

```java
private final ServerBossEvent bossEvent =
        new ServerBossEvent(Component.translatable("entity.summoner.astrageldon"),
                            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

@Override
public void startSeenByPlayer(ServerPlayer player) { super.startSeenByPlayer(player); bossEvent.addPlayer(player); }

@Override
public void stopSeenByPlayer(ServerPlayer player) { super.stopSeenByPlayer(player); bossEvent.removePlayer(player); }

@Override
public void tick() {
    super.tick();
    bossEvent.setProgress(getHealth() / getMaxHealth());
}
```

`Mob` 已经有 `startSeenByPlayer` / `stopSeenByPlayer` 钩子，不用另发网络包。二阶段换色/换样式直接改 `bossEvent.setColor(...)`，切换阶段时 `setName(...)` 换成"凋亡辉星"之类的阶段名。泰拉侧的血条是自绘贴图（可任意渐变、分段），MC 侧 `BossEvent.BossBarColor` 只有 7 种枚举，只能靠 `setProgress` 与换色表达，做不到泰拉的样式；真要做成自定义血条得走 `RenderGuiLayerEvent` 自绘，超出原版 BossBar 的能力。

## 六、网络同步

工程里只有**一个**自定义包：`network\GildedWandPacket.java`，`playToServer` 方向、`HandlerThread.MAIN`、`StreamCodec.composite` 组合字段：

```java
public record GildedWandPacket(int id, InteractionHand hand) implements CustomPacketPayload {
    public static final Type<GildedWandPacket> TYPE = new Type<>(Summoner.rl("gilded_wand"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GildedWandPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, GildedWandPacket::id,
            StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(InteractionHand.class)), GildedWandPacket::hand,
            GildedWandPacket::new);
}

// SummonerNetworkPacketRegister
event.registrar(Summoner.MODID).executesOn(HandlerThread.MAIN)
     .playToServer(GildedWandPacket.TYPE, GildedWandPacket.STREAM_CODEC, GildedWandPacket::handleServer);
```

**实体状态不走这套。** AI 状态字段统一在实体的 `registerSyncFields(SyncFieldDispatcher)` 里声明，由 Lyra 负责服务端写、客户端读：

```java
// Terraprism
@Override
protected void registerSyncFields(SyncFieldDispatcher fields) {
    super.registerSyncFields(fields);
    fields.field(LyraStreamCodecs.INT,   () -> trailTimer,    value -> trailTimer = value);
    fields.field(LyraStreamCodecs.INT,   () -> attackTimer,   value -> attackTimer = value);
    fields.field(LyraStreamCodecs.FLOAT, () -> lastIdleBlend, value -> lastIdleBlend = value);
    fields.field(LyraStreamCodecs.FLOAT, () -> idleBlend,     value -> idleBlend = value);
    fields.field(LyraStreamCodecs.BOOL,  () -> damageActive,  value -> damageActive = value);
}
```

一对 getter/setter lambda 就是双向通道：服务端改字段，客户端自动收到；客户端只读不写。新增状态（阶段号、技能计时器、蓄力进度、激光角度）就加一行 `fields.field(...)`，不要新建 payload。`LyraStreamCodecs` 提供 INT/FLOAT/BOOL/VEC_3/PATH_NODE 等原语，`SummonerStreamCodecs` 在其上补了 `ELLIPSE`、`PATH_NODE_LIST`、`SET_INT`、`AABB`。

高精度运动不用逐 tick 同步位置，而是同步**渲染关键帧**：`PlannedPath` 里塞 `RenderKeyframe(time, PathNode)`，通过 `PATH_NODE_LIST` 一次性下发，客户端 `EntityPathRender` / `getRenderNode(partialTick)` 在关键帧之间插值。`Terraprism.move(...)` 是标准用法：

```java
List<RenderKeyframe> frames = times.stream().map(t -> new RenderKeyframe(t, sampler.apply(t))).toList();
PlannedPath path = new PlannedPath(identifier, List.of(sampler.apply(1f))).render(0, frames);
path.run(0, () -> { damageActive = damaging; if (resetHits) hitTargets.clear(); });  // 先结算上一段碰撞
setPlannedPath(path);
```

`path.run(...)` 的回调在路径推进时执行，命中集合就在回调里清空——这样"同一动作重复命中"这类问题在同步层就解决了，不依赖客户端。

一句话规则：**玩家输入与物品交互 → 新增 `CustomPacketPayload`；实体 AI 状态与位姿 → `registerSyncFields` + `PlannedPath`。**

## 七、召唤物／仆从体系与 Boss 的差异

两套实体体系彼此不相通，这是本工程最容易踩的地方：

| | 仆从 / 弹幕（`common\minion`、`common\projectile`） | Boss（`common\entity`） |
| --- | --- | --- |
| 注册表 | `LyraRegistries.ATTACHMENT_ENTITY_TYPES`（自建 `RegistryBuilder`） | `Registries.ENTITY_TYPE`（原版） |
| 注册类 | `SummonerAttachmentEntityRegister` | `SummonerEntityRegister` |
| 基类 | `AttachmentEntity` → `Minion` → `MomentumMinion` / `GroundMinion`；`Projectile` | `Monster`（+ `GeoAnimatable`、`IPathEntity`） |
| 归属 | 绑定 owner 玩家，随玩家走 | 独立存在于世界，走原版生成/存档 |
| 目标 | `TargetCache` + `SummonMark`，`getSearchDistance()` 给半径 | `setTarget(...)`，`getNearestPlayer(this, 128)` |
| Goal | `AttachmentEntityGoal<T>` + `AttachmentEntityGoalSelector.addGoal(priority, goal)` | vanilla `GoalSelector`（当前未使用，逻辑写在 `tick`） |
| 伤害 | `attack(entity, damage, armorPierce)`，伤害由 `copyAttributes(owner)` 继承 | 原版 `doHurtTarget` / `hurt` |
| 生命值 | 无独立 `MAX_HEALTH`，靠 owner 的属性 | `AttributeSupplier` 里显式给 |
| 碰撞 | `IEntityCollision` + `IBlockCollision` + 局部空间 `getHitbox()` | 原版 `Entity` 碰撞 + `EntityType.sized()` |
| 渲染 | `AbstractAttachmentEntityGeoRenderer` / `SimpleRenderer` | GeckoLib `GeoEntityRenderer` |

`AttachmentEntity` 同时是 `Minion` 和 `Projectile` 的基类，并且已经 `implements GeoAnimatable`——这就是为什么 12 个仆从只写 `registerControllers` 就能播动画，却不需要实现 `GeoAnimatable`。仆从侧的 `registerGoals` 名字被 Lyra 改成了 `registerGoals(AttachmentEntityGoalSelector)`，签名不同，别指望把仆从的 Goal 直接搬到 Boss 上（类型参数卡在 `AttachmentEntity`）。

攻击判定的写法也不一样。仆从是"碰撞即攻击"：`canCollideAttack()` 返回是否开启判定，`onCollisionAttack(List<HitContext>)` 一次性拿到本 tick 所有命中，自己去重（`hitTargets.add(hit.entity())` 返回 `true` 才算新命中）。Boss 侧如果要做冲刺撞击伤害，可以照抄这个模式，只是判定循环要自己写。

可复用到 Boss 的部分：`PathNode` / `Ellipse` / `PlannedPath` / `RenderKeyframe` 这一整套运动与采样工具、`ParticleHelper`、`Playable` 音效、`registerSyncFields` 的字段同步思路、`EntityPathRender` 的位姿应用。不可复用的是技能选择与归属逻辑（owner、召唤标记、数量上限）。

验证范围为只读源码与资源清单，未执行 `gradlew compileJava`，未启动游戏；`D:\IDEA\Summoner` 下的文件未做任何修改。

---

# 任务二：表现层素材清单（泰拉瑞亚侧）

来源：`C:\Users\l1518\Desktop\CatalystMod_Decompiled\Extracted`（CatalystMod 解包）。全部条目按"MC 侧要不要用"筛过一遍，只留 Astrageldon / 末世星史莱姆 / 新星史莱姆 / 裂星史莱姆 / 星辉（Astral、Blight、Metanova）相关。

## 八、文本条目

### 8.1 名称与显示名

| hjson 键 | zh-Hans | en-US |
| --- | --- | --- |
| `NPCs.Astrageldon.DisplayName` | 末世星史莱姆 | Astrageldon |
| `NPCs.NovaSlime.DisplayName` | 新星史莱姆 | Nova Slime |
| `NPCs.NovaSlimer.DisplayName` | 裂星史莱姆 | Nova Slimer |
| `NPCs.AscendedAstralSlime.DisplayName` | 星极史莱姆 | Ascended Astral Slime |
| `NPCs.MetanovaSlime.DisplayName` | 辉恒星史莱姆 | Metanova Slime |
| `NPCs.ArmoredAstralSlime.DisplayName` | 装甲幻星史莱姆 | Armored Astral Slime |
| `NPCs.AstrageldonSlimer.DisplayName` | 辉星史莱姆 | Astrageldon Slimer |
| `NPCs.AstrageldonSlime.DisplayName` | 末星史莱姆 | Astrageldon Slime |
| `Biomes.AstrageldonBiome.DisplayName` | 星末瘟疫 | Astrageldon Biome |
| `AstrageldonMainMenu` | 末世星史莱姆风格 | Astrageldon Style |

MC 侧需要注意：`AstrageldonSlimer`（辉星史莱姆）与 `AstrageldonSlime`（末星史莱姆）是两个不同条目，中文只差一个字，别合并。

### 8.2 Bestiary 图鉴描述

| 键 | zh-Hans |
| --- | --- |
| `Bestiary.Astrageldon` | 拥有神之力量、穿越在维度之间的史莱姆。 作为星辉凋亡的前导，它在追求质量的过程中吸收了它原先的整个宇宙。 |
| `Bestiary.NovaSlime` | 从末世星史莱姆身上分离出来的一小块碎片。 新星史莱姆本身拥有巨大的能量，但它的质量不足以维持自身的稳定。 |
| `Bestiary.NovaSlimer` | 一个高密度的星辉凝胶球，嵌入耐用且锋利、足以撕裂维度的锋刃。 当它们被高速发射时，现实之构都会被其撕裂。 |
| `Bestiary.AscendedAstralSlime` | 来自另一个宇宙的由纯能量构成的星凝胶体。 它们的核心是一块带有共振辉星珠的外星石头。 |
| `Bestiary.MetanovaSlime` | 末世星史莱姆的寂灭使星辉洞穴中充满了闪闪发光的外星岩石。 这些史莱姆吸收了这些石头的小碎片，构建出可操纵的、轻巧耐用的装甲。 |

英文原文（同一行号顺序，用作翻译对照）：Astrageldon 为 “An interdimensional slime infused with God-like power. Empowered by the blighted astral, it grew to absorb its entire host universe.”

### 8.3 Boss Checklist 与状态提示

| hjson 键 | zh-Hans | en-US |
| --- | --- | --- |
| `NPCs.Astrageldon.BossChecklistIntegration.SpawnInfo` | 在星辉瘟疫生物群落使用 `[i:CatalystMod/AstralCommunicator]` 召唤 | Spawned with an `[i:CatalystMod/AstralCommunicator]` in the Astral Infection |
| `...BossChecklistIntegration.DespawnMessage` | `[c/db5ed2:来自平行宇宙的骇宇巨物继续着它吞噬万物的征途。]` | `[c/db5ed2:The multiversal terror returns to its consumption.]` |
| `TitleIcon.Astrageldon.1` | 你无法挑战末世星史莱姆 | Astrageldon is unfightable |
| `TitleIcon.Astrageldon.3` | 你可以挑战末世星史莱姆 | Astrageldon is fightable |
| `TitleIcon.Astrageldon.4` | 你已击败末世星史莱姆 | Astrageldon is defeated |

DespawnMessage 里的 `[c/db5ed2:...]` 是泰拉的颜色标记（`#db5ed2` 品红），MC 侧对应 `ChatFormatting.LIGHT_PURPLE` 或 `Component.withColor(0xdb5ed2)`；`[i:...]` 是内联物品图标，MC 侧没有等价语法，要么去掉、要么用区域名代替。

注意：**没有专门列"攻击提示"的条目**。泰拉侧的引导全靠 `SpawnInfo`、`DespawnMessage`、`TitleIcon`、`AstrageldonAntiCheat` 这几处，攻击模式本身不写在文本里（玩家靠实战学习）。如果 MC 侧要做阶段提示，需要新写文案。

### 8.4 公告与系统消息

| hjson 键 | zh-Hans |
| --- | --- |
| `Announcement.AstrageldonLocked` | `星辉发讯器核心破碎……` / `末世星之奇点遗世而去……`（两行） |
| `Announcement.AstrageldonKilledImproperly` | 你触发了一个不常见的bug……重新召唤这个Boss并击败它，获取它的战利品吧！ |
| `Announcement.HasAwakened` | `{0}已苏醒！`（`{0}` 填 Boss 名） |
| `Announcement.AstralTreasures` | 星辉洞穴中的宝藏发出了淡淡星光！ |
| `WorldGen.AstralSpread` | 扩散星辉瘟疫…… |
| `WorldGen.AstralTreasures` | 生成辉恒星矿…… |
| `AstrageldonAntiCheat` | `默认你是无意的` / `你都这么玩了，别找我们吐槽平衡性问题了` / `打无伤也别这么召唤`（三行，召唤作弊时的提示） |
| `DropCondition.Flawless` | 无伤击败时掉落 |
| `DeathMessage.InterstellarCorruption` | 被空间裂缝吞噬了……（死亡消息后缀） |

### 8.5 物品与提示

| hjson 键 | zh-Hans | 说明 |
| --- | --- | --- |
| `Items.AstralCommunicator.DisplayName` | 星辉发讯器 | 召唤物，对应 MC 侧的召唤道具 |
| `Items.AstralCommunicator.Tooltip` | 在星辉瘟疫使用，召唤末世星史莱姆 / `[c/870808:若你在尚未击败末世星史莱姆时就杀死了月球领主，本物品将失效]` | 两行，第二行是暗红警告 |
| `ItemTooltipExtra0.AstralCommunicator` | 右键使用以召唤末世星史莱姆的第一阶段 | |
| `ItemTooltipExtra1.AstralCommunicator` | （英文）The core has been destroyed by energy released from the death of a lunar deity... | 中文该键缺失，只有英文 |
| `Items.AstrageldonBag.DisplayName` | 宝藏胶囊（末世星史莱姆） | 战利品袋 |
| `Items.AstrageldonMask.DisplayName` | 末世星史莱姆面具 | |
| `Items.AstrageldonTrophy.DisplayName` | 末世星史莱姆纪念章 | |
| `Items.AstrageldonRelic.DisplayName` | 末世星史莱姆圣物 | 专家/大师饰品 |
| `Items.AstrageldonPhase1MusicBox` | 音乐盒（末世星史莱姆第一阶段）+ Tooltip《宙极之恶》，由 DM DOKURO 创作 | |
| `Items.AstrageldonPhase2MusicBox` | 音乐盒（末世星史莱姆第二阶段）+ Tooltip《天河荒溃》，由 Heart Plus Up! 创作 | |
| `Items.AstrageldonTitleMusicBox` | 音乐盒（末世星史莱姆游戏主页）+ Tooltip《来自地狱的天堂之礼》，由 DM DOKURO 创作 | |
| `Items.InterstellarCorruptionDye.DisplayName` | 星辉浊流染料 | 掉落染料 |
| `Items.MetanovaOre` / `MetanovaBar` | 辉恒星矿 / 辉恒锭 | 战后矿物 |
| `Items.NovaMonolith.DisplayName` | 新星天柱 | 装饰柱 |
| `Items.LoreAstrageldon.DisplayName` | 末世星史莱姆 | 传说书，`Lore` 字段是 8 行长文（见下） |

`Items.Lore.LoreAstrageldon.Lore`（zh-Hans，8 行，可作为 MC 侧图鉴/书的正文）：

> 这样的存在让人无法理解。 嘉登曾在他对星辉瘟疫的调查中报告过这种异常的力量。
> 纵使才华横溢，他也无法洞彻其独特的星辉凝胶本质。
> 他确实发现该实体已被更为强大的星辉瘟疫菌株污染。
> 它的绝对密度无法估量，远远超出了理性可认知的范围。 不仅如此，它奇怪的粘性无论如何都解释不通。
> 如果它真的是个史莱姆的话，那么它具有的吞噬万物的能力将成为它无尽野心的推手。 正是这种瘟疫的理想宿主。
> 它的饥饿感是无法满足的。 无法控制，野性难驯。 自然秩序的完美体现。
> 光是这实体拥有的这种潜力，也会令贪婪的史莱姆之神羡慕不已吧。
> 噬界之力不复存在，我们只能猜测这惊人之举背后的恐怖威胁。

### 8.6 弹幕与召唤物显示名

MC 侧做弹幕/召唤物时需要给它们起名，泰拉侧现成的对应如下：

| hjson 键 | zh-Hans | en-US | 判读 |
| --- | --- | --- | --- |
| `Projectiles.AstrageldonStar.DisplayName`（含 `2/3/4` 三个变体） | 凋亡辉星 | Blighted Star | Boss 弹幕星体，4 个变体名相同 |
| `Projectiles.AstrageldonLaser.DisplayName` | 凋亡光束 | Blighted Beam | 激光 |
| `Projectiles.AstrageldonFlamethrower.DisplayName` | 凋亡之焱 | Blighted Flames | 火焰喷射 |
| `Projectiles.SlimerSpear.DisplayName` | 新星长矛 | Nova Spear | 裂星史莱姆的突刺 |
| `Projectiles.NovaBeam.DisplayName` | 新星激光 | — | 二阶段光束 |
| `Projectiles.NovaFlame.DisplayName` | 新星火焰 | — | |
| `Projectiles.NovaGlobe.DisplayName` | 超新星炸弹 | — | 与 `AstragelgunProj` 同名 |
| `Projectiles.NebulaSphere.DisplayName` | 超新星云雾 | Hypernova Smog | 星云球 |
| `Projectiles.NebulaResidue.DisplayName` / `NebulaResidueHB` | （无中文） | Blighted Residue | 星云残渣，HB 为判定盒版本 |
| `Projectiles.InterstellarVolutionSlimer.DisplayName` | 裂星史莱姆 | — | 武器弹幕召唤出的仆从 |
| `Projectiles.InterstellarVolutionSlimerBullet.DisplayName` | 裂星史莱姆激光 | — | |
| `Projectiles.AstrageldonPet.DisplayName` | 史莱姆星灵 | Astrageldon Pet | 宠物 |
| `Buffs.AstralpodBuff` | 星辉水母 / 星辉水母会保护你 | Astralpod / The astralpod will protect you | 召唤物 buff |
| `Buffs.AstralBlight` | 星辉凋亡 | Astral Blight | Boss 施加的核心减益 |
| `Buffs.AstrageldonPetBuff` | （无中文） | — | 宠物 buff |

### 8.7 音乐显示名

| hjson 键 | zh-Hans | en-US | 作曲 |
| --- | --- | --- | --- |
| `MusicDisplay.SongNames.AstrageldonTitle` | 来自地狱的天堂之礼 | Heaven's Hell-Sent Gift | DM DOKURO |
| `MusicDisplay.SongNames.AstrageldonPhase1` | 宙极之恶 | Cosmic Disgust | DM DOKURO |
| `MusicDisplay.SongNames.AstrageldonPhase2` | 天河荒溃 | Galactic Destruction | Heart Plus Up! |

另有两个作者键：`MusicDisplay.Authors.DMDOKURO`（由 DM DOKURO 创作）、`MusicDisplay.Authors.HeartPlusUp`（由 Heart Plus Up! 创作）。

## 九、贴图清单（`NPCs\Boss\Astrageldon\`）

共 24 个 `.rawimg`，按用途分组。`.rawimg` 是泰拉的自定义容器（含尺寸/帧信息，不是裸 PNG），所以只给文件名与字节数。

### 9.1 Boss 本体与光环

| 文件 | 字节 | 判读 |
| --- | --- | --- |
| `Astrageldon.rawimg` | 574908 | 本体主贴图，全目录最大，含多帧（弹跳压缩/伸展） |
| `AstrageldonHalo.rawimg` | 412 | 光晕，字节极少，说明是纯色/径向渐变的简单遮罩 |
| `AstrageldonNebula.rawimg` | 39996 | 星云层，本体外层的旋转云气 |
| `AstrageldonRocks.rawimg` | 164268 | 环绕岩石群贴图（第二大的图，多块岩石一张图） |
| `NebulaPlatform.rawimg` | 74412 | 星云平台/落脚面 |
| `NebulaResidue.rawimg` / `NebulaResidueHB.rawimg` | 9260 / 9260 | 星云残渣；`HB` 是同尺寸的判定盒图层（hitbox 可视化/命中遮罩） |

### 9.2 激光与光束

| 文件 | 字节 | 判读 |
| --- | --- | --- |
| `AstrageldonLaser.rawimg` | 4620 | 主激光束 |
| `SpiralLaser.rawimg` | 588 | 螺旋激光，极小说明是细长条带 |
| `StarLaser.rawimg` | 588 | 星形激光，与 Spiral 同尺寸，同一套 UV 布局 |
| `AstraBlade.rawimg` | 4652 | 星刃（挥击特效/近战判定表现） |

### 9.3 岩石与小行星

| 文件 | 字节 | 判读 |
| --- | --- | --- |
| `AstraRock.rawimg` | 46092 | 单块星岩 |
| `AstraRockOutline.rawimg` | 46092 | 同尺寸描边层，与上一张叠画做外发光轮廓 |
| `HostileAsteroid.rawimg` | 48412 | 敌对小行星（会砸向玩家的那类） |

### 9.4 新星/裂星史莱姆（带辉光双层）

| 文件 | 字节 | 判读 |
| --- | --- | --- |
| `NovaSlime.rawimg` | 40140 | 新星史莱姆本体 |
| `NovaSlime_Glow.rawimg` | 40140 | 辉光层，与本体**字节完全相同**，即同尺寸的加法叠加层 |
| `NovaSlimer.rawimg` | 9164 | 裂星史莱姆本体 |
| `NovaSlimer_Glow.rawimg` | 9164 | 辉光层，同上 |
| `SlimerSpear.rawimg` | 1644 | 新星长矛 |

MC 侧对应做法：本体一张贴图 + `_Glow` 一张贴图，用 `RenderType.entityTranslucentEmissive` 或 GeckoLib 的 `AutoGlowingGeoLayer` 叠一层，不需要自己算 emissive mask。

### 9.5 UI 与图鉴

| 文件 | 字节 | 判读 |
| --- | --- | --- |
| `Astrageldon_BossChecklist.rawimg` | 91276 | Boss Checklist 模组用的展示图（横版立绘） |
| `Astrageldon_Head_Boss.rawimg` | 3756 | 图鉴/Boss 头像，标准头图尺寸 |
| `Astrageldon_Head_Boss_Alt.rawimg` | 3132 | 头像的备用版本（可能是二阶段/不同表情） |
| `DashIndicator.rawimg` | 140 | 冲刺指示器，140 字节说明是纯色小矩形/箭头 |
| `DashIndicator2.rawimg` | 268 | 第二版指示器 |

## 十、音效清单（`Assets\Sounds\`）

根目录 22 个 `.ogg` 全部是 Astrageldon 相关，没有杂项。按战斗环节分组：

### 10.1 出场

| 文件 | 字节 |
| --- | --- |
| `AstrageldonSpawn.ogg` | 234320 |
| `AstrageldonSpawnOLD.ogg` | 18211 |

`OLD` 后缀是作废版本，别用；体积差 13 倍说明旧版是短促的音效、新版是带混响的长出场。

### 10.2 落地 / 撞击 / 弹跳

| 文件 | 字节 |
| --- | --- |
| `AstrageldonBounceLand1.ogg` | 63154 |
| `AstrageldonBounceLand2.ogg` | 64557 |
| `AstrageldonBounceLand3.ogg` | 62997 |
| `AstrageldonBounceLand4.ogg` | 63363 |
| `AstrageldonImpact.ogg` | 162081 |
| `AstrageldonImpactOLD.ogg` | 52494 |
| `AstralRockImpact1.ogg` | 47582 |
| `AstralRockImpact2.ogg` | 48250 |
| `AstralRockImpact3.ogg` | 45832 |
| `AstralRockImpact4.ogg` | 48456 |

`BounceLand` 1–4 是同一动作的四个变体（每个约 63 KB，字节数接近），MC 侧就该做成 `sounds.json` 里的 `sounds` 数组由引擎随机挑一个，不要自己 `switch`。`AstralRockImpact` 1–4 同理，用于陨石/岩石落地。

### 10.3 死亡

| 文件 | 字节 |
| --- | --- |
| `AstrageldonDeath.ogg` | 183695 |
| `AstrageldonDeathImplosion.ogg` | 1374804 |

`DeathImplosion` 1.37 MB、是全场第三大的音频，明显是"内爆/坍缩"的长尾音效，应该作为死亡动画的后半段单独播，而不是和 `Death` 同时起。

### 10.4 二阶段与变身

| 文件 | 字节 |
| --- | --- |
| `AstrageldonPhase2Begin.ogg` | 133842 |
| `AstrageldonPhase2Charge.ogg` | 113277 |
| `AstrageldonTransform2Energy.ogg` | 70829 |
| `AstrageldonTransform2Slime.ogg` | 67926 |

四段构成一个完整的转阶段序列：蓄力（Charge）→ 能量体形态（Transform2Energy）→ 凝胶体形态（Transform2Slime）→ 正式开始（Phase2Begin）。MC 侧按这个顺序在转阶段动画里排时间轴。

### 10.5 光束

| 文件 | 字节 |
| --- | --- |
| `AstrageldonSpiralBeam.ogg` | 1060840 |
| `AstrageldonSpiralBeamCharge.ogg` | 74486 |

两段：蓄力 + 持续光束（1 MB，长循环音）。MC 侧要把 `SpiralBeam` 切成 loop 段，用 `Playable` 或自管 `SoundInstance` 循环，光束结束再停。

### 10.6 旋转岩石

| 文件 | 字节 |
| --- | --- |
| `AstrageldonSpinRocksStart.ogg` | 469767 |
| `AstrageldonSpinRocksEnd.ogg` | 76155 |

起手长（470 KB）收尾短（76 KB），是"岩石升起并开始环绕"和"岩石回落"的对应关系。

### 10.7 关联物品音效（`Assets\Sounds\Item\`）

| 文件 | 字节 | 用途 |
| --- | --- | --- |
| `AstrageldonSpawner.ogg` | 162101 | 召唤器使用 |
| `AstralCommunicatorUseOLD.ogg` | 34922 | 星辉发讯器旧版使用音 |
| `AstragelgunShot.ogg` / `AstragelgunShotOld.ogg` | 16322 / 16322 | 星凝枪射击，两版同字节 |
| `AstralsEndUse.ogg` | 19483 | 星辉之终使用 |
| `AstralsEndKill.ogg` | 63942 | 星辉之终击杀 |
| `Astragelgun\AstragelgunOrbHit.ogg` | 24027 | 凝胶球命中 |
| `Astragelgun\AstragelgunPrimary.ogg` | 58720 | 主射击 |
| `Astragelgun\AstragelgunSecondary.ogg` | 31046 | 副射击 |
| `Astragelgun\AstragelgunShoot.ogg` | 32302 | 通用射击 |

## 十一、Boss 战音乐（`Assets\Sounds\Music\`）

| 路径 | 字节 | 曲名（zh） | 曲名（en） | 作曲 | MC 侧对应 SoundEvent |
| --- | --- | --- | --- | --- | --- |
| `Bosses\AstrageldonPhase1.ogg` | 2162400 | 宙极之恶 | Cosmic Disgust | DM DOKURO | `doomsday_star_phase_1`（已存在，`stream: true`） |
| `Bosses\AstrageldonPhase2.ogg` | 2866279 | 天河荒溃 | Galactic Destruction | Heart Plus Up! | `doomsday_star_phase_2`（已存在，`stream: true`） |
| `Misc\AstrageldonTitle.ogg` | 1247340 | 来自地狱的天堂之礼 | Heaven's Hell-Sent Gift | DM DOKURO | 无，需要新增（仅主菜单用） |

`Music\` 下只有这三首，没有其他 Boss 的曲子。体量：一阶段约 2.06 MB、二阶段约 2.73 MB、标题曲约 1.19 MB，都是分钟级长音频，`sounds.json` 必须写 `stream: true`，否则一次性解码会明显卡顿。

---

## 附：跨两个任务的落地注意点

1. 实体注册名 `astrageldon` 与语言键 `entity.summoner.doomsday_star` 不一致，落地前必须统一，否则血条与图鉴名显示为原始键。
2. Astrageldon 的 geo / anim / texture 资源全部空缺，需要按第九节的分层（本体 / Halo / Nebula / Rocks / Laser）决定合成一张贴图还是拆成多部件 GeoModel。
3. `AstrageldonRenderer.render` 缺 `super.render(...)`，`registerGoals` 与 `registerControllers` 为空，BossBar 完全未实现——这四处是目前最大的缺口。
4. 音效文件名与 MC 侧注册名没有既定映射，建议按第十节的分组直接转小写下划线（`AstrageldonBounceLand1` → `astrageldon_bounce_land_1`），并把 1–4 变体写成同一个 `sounds.json` 数组。

