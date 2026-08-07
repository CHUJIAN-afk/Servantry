# Servantry 接入 Lyra 重构计划(REFACTOR_PLAN)

> 创建:2026-08-07
> 目标:删除 Servantry 的 `first.servantry.api` 包,改为依赖独立库 mod **Lyra**(`first.lyra.*`,mavenLocal 坐标 `first:lyra:1.0.0`),行为等价、无冗余代码。
> 依据:`D:\IDEA\Lyra\MIGRATION_STATUS.md`(Lyra 侧六轮重构完成,编译通过)。

## 用户确认的决策

1. **本地依赖方式**:mavenLocal(Lyra `publishToMavenLocal` → Servantry `implementation "first:lyra:1.0.0"`)
2. **饰品系统落点**:`CurioItem`/`CurioItemBuilder` 搬到 `first.servantry.common.item` / `first.servantry.common.builder`,`CuriosUtil` 留 utils
3. **语言文件**:Servantry 跑 runData **不生成 Lyra 的库语言文件**(库键由 Lyra 自己的 runData 生成);用 Lyra itemBuild 的宿主物品键如何输出 → **数据生成通用方案**,最后尝试,做不到再询问
4. 工作原则:歧义先提问 / 不做要求之外扩展 / 谨慎新增方法体

## 阶段 0:发布 Lyra 到 mavenLocal

- [ ] 在 `D:\IDEA\Lyra` 跑 `gradlew publishToMavenLocal`(JBR 21),验证 `~/.m2/repository/first/lyra/1.0.0/` 产物存在
- 注意:Lyra 的 mods.toml 模板在 `src/main/templates`,发布 jar 需含生成后的 META-INF

## 阶段 1:构建配置

- [ ] `build.gradle` dependencies 加 `implementation "first:lyra:1.0.0"`(mavenLocal 仓库已配置)
- [ ] `src/main/templates/META-INF/neoforge.mods.toml` 加 `[[dependencies."servantry"]]` modId="lyra" type="required"

## 阶段 2:删除与 Lyra 重复的类(12 个 + api 包)

| 删除项 | Lyra 替代 |
|---|---|
| `register/ServantryAttachmentRegister` | `LyraAttachmentRegister` |
| `register/ServantryAttributeRegister` | `LyraAttributeRegister` |
| `register/ServantryDamageRegister` | `LyraDamageRegister` |
| `register/ServantryParticleRegister` | `LyraParticleRegister` |
| `api/register/ServantryRegistries` | `LyraRegistries` |
| `network/BatchedParticlesPayload` | `Lyra common.network.BatchedParticlesPayload` |
| `network/BatchedDamageInfoPayload` | `Lyra common.network.BatchedDamageInfoPayload` |
| `config/ClientConfig` | `Lyra client.config.ClientConfig` |
| `utils/AttributeUtils` | `Lyra utils.AttributeUtils` |
| `utils/EasingCurve` | `Lyra utils.EasingCurve` |
| `utils/GroundPathHelper` | `Lyra utils.GroundPathHelper` |
| `mixin/` 8 个(CombatRulesMixin、CreativeModeTabMixin、CreativeModeInventoryScreenMixin、EntityRendererMixin、LevelRendererAccessor、LevelRendererMixin、LivingEntityMixin、PlayerMixin) | Lyra 已有 8 个同名 mixin |
| **整个 `api/` 包(66 文件)** | 除饰品外全部由 Lyra 覆盖 |

删除后引用这些类的主代码(约 50 个文件)改为 import Lyra 对应类。

## 阶段 3:饰品系统搬出 api 包(用户确认 → common 包)

- [ ] `api/item/CurioItem.java` → `common/item/CurioItem.java`(包 `first.servantry.common.item`)
- [ ] `api/builder/CurioItemBuilder.java` → `common/builder/CurioItemBuilder.java`
- [ ] `utils/CuriosUtil.java` 留 utils,import 改向
- [ ] 引用方(register/ServantryCurioRegister、common/Event 等)改 import

## 阶段 4:126 个引用文件全局替换

- [ ] 生成 api 类 → Lyra 类的完整映射表(按 Lyra 实际文件树核对,含改名项):
  - `api.common.attachment.EntityData` → `common.attachment.AttachmentEntityData`
  - `api.common.attachment.BatchedParticlesData` → `common.attachment.ParticlesData`
  - `api.ServantryHelper` → `api.LyraHelper`
  - `api.register.ServantryRegistries` → `register.LyraRegistries`
  - 其余:子包前缀替换 + 类名保持
- [ ] 126 个文件批量替换 `first.servantry.api.` → `first.lyra.` 对应映射
- [ ] `ServantryAttachmentRegister` → `LyraAttachmentRegister` 等引用替换
- [ ] `ServantryDamageRegister` → `LyraDamageRegister`(Servant.java、InvincibleData 等)
- [ ] `ServantryItemRegisterBuilder` → `LyraItemRegisterBuilder`(API 差异:TabGroup → Section 特征标签)
- [ ] `TabGroup` → `Section`(仅创造 Tab 相关)

## 阶段 5:行为等价修复(4 个关键点)

- [x] **DamageSourceMixin**:已整体迁移到 Lyra(用户操作),`servantry$isCritical` → `lyra$isCritical`;Servantry 侧文件已删,servantry.mixins.json 只留 `DamageSourcesMixin`;Lyra 重新 publishToMavenLocal
- [ ] **无限阴影武器**:`ServantryServantWeaponRegister` 中 InfiniteShadow 武器覆写 `IServantWeaponItem.getSummonTooltip()` 恢复剑鞘 tooltip(ScabbardContainer 数据组件保留在主 mod)
- [ ] **创造 Tab**:`ServantryCreativeTabRegister` 保留 Tab 注册;`TabBuilder`/`sortedTabGroup` 分组逻辑改用 Lyra `CreativeTabDispatcher.registerTab()` + `Section` 特征标签自动归类;物品注册改 `Properties.tag(Section 标签)`;横幅 `AnimInfo` → Lyra `AnimBanner` 适配
- [ ] **语言键**:`item.servantry.tooltip.*` → `item.lyra.*`(Lyra 已生成库键);Servantry 物品键维持 `ServantryLanguageGenerateRegister` 输出;**不遍历 LyraLanguageRegister**(用户决策)

## 阶段 6:编译验证

- [ ] `gradlew compileJava`(JBR 21)迭代修复 → BUILD SUCCESSFUL
- [ ] `gradlew runData` 验证 Servantry 语言文件正常生成
- [ ] 运行游戏验证行为等价(可选,用户决定)

## 阶段 7:数据生成通用方案 ✅(已完成)

- [x] **物品注册**:98 处 `ServantryItemRegisterBuilder.build(Section, ...)` → `LyraItemRegisterBuilder.build(ITEMS, ...)` + `.itemTag(Section.tag())`(用户确认直接改 98 处);`ServantryItemRegisterBuilder` 已删除;`ITEMS` 提取到 `ServantryItemRegister`
- [x] **Lyra provider 加 modid 参数**(用户确认改 Lyra):`LyraItemModelProvider`/`LyraItemTagsProvider`/`LyraBlockTagsProvider` 新增带 modid 构造器,默认 `Lyra.MODID` 不变;Lyra 重新 publishToMavenLocal
- [x] **主 mod datagen**:`DataGeneratorEvent` 改用 Lyra provider(传 Servantry.MODID);删除主 mod 的 `ServantryRecipeProvider`/`ServantryItemTagsProvider`/`ServantryItemModelProvider`/`ServantryBlockTagsProvider`
- [x] **语言**:`ServantryLanguageProvider` 遍历 `ServantryLanguageGenerateRegister`(主 mod 静态键)+ `LyraLanguageRegister`(过滤含 servantry 的键:物品键 + 套装奖励键);库键(`item.lyra.*`)由 Lyra 自己输出
- [x] **验证**:clean + compileJava + runData BUILD SUCCESSFUL;产物 203 文件(模型 98 个到 servantry 命名空间、5 个 Section 标签、lang 207 键)

## 涉及文件规模

- 修改:约 130 个 Java 文件 + build.gradle + neoforge.mods.toml + 语言 provider
- 删除:66(api 包)+ 12(重复类)= 78 个文件

## 风险

- mixin:删除 Servantry 8 个 mixin 后行为由 Lyra 接管(已核对 Lyra mixin 覆盖相同功能)
- 存档:附件/属性 ID `servantry:` → `lyra:`,旧存档不兼容(Lyra 侧已拍板)
- 编译命令:必须 JBR 21(PATH 的 Java 25 会导致 Gradle 8.8 崩溃)

## 编译命令

```powershell
.\gradlew.bat compileJava "-Dorg.gradle.java.home=C:\Users\l1518\AppData\Local\Programs\IntelliJ IDEA Ultimate 2025.2.5\jbr" --console=plain
```
