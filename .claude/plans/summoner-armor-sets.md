# 注册四个召唤师套装计划

## 转换规则

- 防御力：泰拉值÷3，向上取整
- 鞋子防御：胸甲防御÷2，向上取整
- 护腿属性：原版护腿属性×2/3给新护腿，×1/3给新鞋子
- 鞭子属性：全部转为移动速度（四个套装均无鞭子属性，无需处理）
- 召唤栏：保持原版位置
- 稀有度：叶绿=UNCOMMON, 阴森/提基=RARE, 星尘=EPIC
- 修复材料：全部Ingredient.EMPTY（不可修复，叶绿锭暂不注册）
- 装备音效：叶绿=IRON, 阴森=LEATHER, 提基=LEATHER, 星尘=IRON
- 附魔能力：叶绿=10, 阴森=10, 提基=15, 星尘=25

---

## 1. 叶绿盔甲 (Chlorophyte Armor - 召唤师变体)

### MC换算结果

| 部位       | 防御 | 属性              |
|----------|----|-----------------|
| 头盔       | 2  | +1召唤栏, +10%仆从伤害 |
| 胸甲       | 3  | +6%仆从伤害         |
| 护腿       | 2  | +4%仆从伤害         |
| 鞋子       | 2  | +2%仆从伤害         |
| **套装奖励** |    | +1召唤栏, +12%仆从伤害 |

### 注册ID

- ArmorMaterial: `chlorophyte`, 防御{2,3,2,2}, 附魔10, 音效IRON, 修复EMPTY
- 物品: `chlorophyte_helmet/helmet/chestplate/leggings/boots`
- 翻译: 叶绿面具/叶绿板甲/叶绿护胫/叶绿战靴, 英: Chlorophyte Mask/Chlorophyte Breastplate/Chlorophyte
  Leggings/Chlorophyte Boots

---

## 2. 阴森盔甲 (Spooky Armor)

### MC换算结果

| 部位       | 防御 | 属性              |
|----------|----|-----------------|
| 头盔       | 2  | +11%仆从伤害        |
| 胸甲       | 4  | +11%仆从伤害        |
| 护腿       | 3  | +8%仆从伤害, +1召唤栏  |
| 鞋子       | 2  | +4%仆从伤害         |
| **套装奖励** |    | +1召唤栏, +25%仆从伤害 |

### 注册ID

- ArmorMaterial: `spooky`, 防御{2,4,3,2}, 附魔10, 音效LEATHER, 修复EMPTY
- 物品: `spooky_helmet/chestplate/leggings/boots`
- 翻译: 阴森头盔/阴森胸甲/阴森护腿/阴森战靴, 英: Spooky Helmet/Spooky Chestplate/Spooky Leggings/Spooky Boots

---

## 3. 提基盔甲 (Tiki Armor)

### MC换算结果

| 部位       | 防御 | 属性              |
|----------|----|-----------------|
| 头盔       | 2  | +1召唤栏, +10%仆从伤害 |
| 胴甲       | 3  | +10%仆从伤害        |
| 护腿       | 2  | +7%仆从伤害         |
| 鞋子       | 2  | +3%仆从伤害         |
| **套装奖励** |    | +1召唤栏           |

### 注册ID

- ArmorMaterial: `tiki`, 防御{2,3,2,2}, 附魔15, 音效LEATHER, 修复EMPTY
- 物品: `tiki_helmet/chestplate/leggings/boots`
- 翻译: 提基面具/提基胸甲/提基护腿/提基战靴, 英: Tiki Mask/Tiki Chestplate/Tiki Leggings/Tiki Boots

---

## 4. 星尘盔甲 (Stardust Armor)

### MC换算结果

| 部位       | 防御 | 属性              |
|----------|----|-----------------|
| 头盔       | 3  | +1召唤栏, +16%仆从伤害 |
| 胴甲       | 5  | +22%仆从伤害        |
| 护腿       | 4  | +15%仆从伤害        |
| 鞋子       | 3  | +7%仆从伤害         |
| **套装奖励** |    | +1召唤栏, +22%仆从伤害 |

### 注册ID

- ArmorMaterial: `stardust`, 防御{3,5,4,3}, 附魔25, 音效IRON, 修复EMPTY
- 物品: `stardust_helmet/chestplate/leggings/boots`
- 翻译: 星尘头盔/星尘板甲/星尘护腿/星尘战靴, 英: Stardust Helmet/Stardust Chestplate/Stardust Leggings/Stardust Boots

---

## 实施步骤

### 步骤1: ArmorMaterialRegister.java

在 `ValhallaKnightArmorMaterial` 之后添加4个ArmorMaterial注册

### 步骤2: ItemRegister.java

在英灵殿骑士套装之后添加16个物品注册（4×4件），顺序：叶绿→阴森→提基→星尘

### 步骤3: ArmorSetRegister.java

在 `ValhallaKnight` 之后添加4个套装注册

### 步骤4: ServantryLanguageProvider.java

添加16个物品的中英文翻译

### 涉及文件

- `src/main/java/first/servantry/register/ArmorMaterialRegister.java`
- `src/main/java/first/servantry/register/ItemRegister.java`
- `src/main/java/first/servantry/register/ArmorSetRegister.java`
- `src/main/java/first/servantry/dadageneeator/provider/ServantryLanguageProvider.java`