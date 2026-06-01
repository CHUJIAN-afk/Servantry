package first.servantry.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import first.servantry.Servantry;
import first.servantry.api.mithrilAnvil.MithrilAnvilCraftingRecipe;
import first.servantry.common.menu.MithrilAnvilPlaceRecipeHandler;
import first.servantry.register.MenuRegister;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class MithrilAnvilGui {

    // ==================== 客户端 Screen ====================

    public static class MithrilAnvilScreen extends AbstractContainerScreen<MithrilAnvilMenu> {

        private static final ResourceLocation CRAFTING_TABLE_LOCATION =
                Servantry.rl("textures/gui/crafting_table.png");

        private final MithrilAnvilRecipePanel recipePanel = new MithrilAnvilRecipePanel();

        public MithrilAnvilScreen(MithrilAnvilMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title);
            this.imageWidth = 176;
            this.imageHeight = 166;
            this.titleLabelX = 16;
            this.inventoryLabelX = 8;
            this.inventoryLabelY = 72;
        }

        @Override
        protected void init() {
            super.init();
            recipePanel.init(this.minecraft, this.width, this.height, this.menu);
            this.leftPos = recipePanel.getScreenOffset(this.width, this.imageWidth);
        }

        @Override
        public void containerTick() {
            super.containerTick();
            recipePanel.tick();
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            this.renderBackground(graphics, mouseX, mouseY, partialTick);
            super.render(graphics, mouseX, mouseY, partialTick);
            recipePanel.render(graphics, mouseX, mouseY, partialTick);
            recipePanel.renderGhostRecipe(graphics, this.leftPos, this.topPos, partialTick);
            this.renderTooltip(graphics, mouseX, mouseY);
            recipePanel.renderTooltip(graphics, this.leftPos, this.topPos, mouseX, mouseY);
        }

        @Override
        protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
            int x = this.leftPos;
            int y = (this.height - this.imageHeight) / 2;
            graphics.blit(CRAFTING_TABLE_LOCATION, x, y, 0, 0, this.imageWidth, this.imageHeight);
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate(x + 37, y + 35, -100);
            pose.scale(3, 3, 3);
            graphics.renderFakeItem(Items.ANVIL.getDefaultInstance(), 0, 0);
            pose.popPose();
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return recipePanel.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            return recipePanel.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (recipePanel.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
            return recipePanel.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, imageWidth, imageHeight);
        }

        @Override
        protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
            // 输入槽（1-5）只读，阻止所有交互
            if (slotId >= 1 && slotId <= 5) return;
            // 输出槽（0）仅允许拿取结果
            super.slotClicked(slot, slotId, mouseButton, type);
            recipePanel.slotClicked();
        }
    }

    // ==================== 双端 Menu ====================

    public static class MithrilAnvilMenu extends AbstractContainerMenu {

        static final int INPUT_SLOTS = 5;

        private final ContainerLevelAccess access;
        private final Player player;
        private final TransientCraftingContainer inputContainer;
        private final ResultContainer resultContainer = new ResultContainer();
        private RecipeHolder<MithrilAnvilCraftingRecipe> selectedRecipe;

        public MithrilAnvilMenu(int containerId, Inventory playerInventory) {
            this(containerId, playerInventory, ContainerLevelAccess.NULL);
        }

        public MithrilAnvilMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
            super(MenuRegister.MITHRIL_ANVIL.get(), containerId);
            this.player = playerInventory.player;
            this.access = access;
            this.inputContainer = new TransientCraftingContainer(this, INPUT_SLOTS, 1);

            // 输出槽（索引 0）
            this.addSlot(new MithrilAnvilResultSlot(player, inputContainer, resultContainer, 0, 135, 44));

            // 5个输入槽（索引 1-5）— 只读显示，物品由程序设置
            for (int i = 0; i < INPUT_SLOTS; i++) {
                this.addSlot(new Slot(inputContainer, i, 17 + i * 18, 17) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean mayPickup(@NotNull Player player) {
                        return false;
                    }
                });
            }

            // 玩家背包（索引 6-32）
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
                }
            }

            // 快捷栏（索引 33-41）
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
            }
        }

        @Override
        public void slotsChanged(@NotNull Container container) {
            if (container == inputContainer) {
                updateResult();
            }
        }

        void updateResult() {
            if (selectedRecipe != null) {
                resultContainer.setItem(0, selectedRecipe.value().inner().result().copy());
            } else {
                resultContainer.setItem(0, ItemStack.EMPTY);
            }
        }

        public void setSelectedRecipe(RecipeHolder<MithrilAnvilCraftingRecipe> holder) {
            // 清除旧材料（丢弃，不退回背包）
            inputContainer.clearContent();
            this.selectedRecipe = holder;
            // 用配方的材料物品填充输入槽（供显示和JEI查询）
            if (holder != null) {
                var ingredients = holder.value().inner().ingredients();
                for (int i = 0; i < ingredients.size() && i < INPUT_SLOTS; i++) {
                    ItemStack[] items = ingredients.get(i).ingredient().getItems();
                    if (items.length > 0) {
                        inputContainer.setItem(i, items[0].copyWithCount(ingredients.get(i).count()));
                    }
                }
            }
            updateResult();
        }

        public void clearSelectedRecipe() {
            inputContainer.clearContent();
            this.selectedRecipe = null;
            resultContainer.setItem(0, ItemStack.EMPTY);
        }

        public TransientCraftingContainer getInputContainer() {
            return inputContainer;
        }

        public void placeRecipe(RecipeHolder<MithrilAnvilCraftingRecipe> holder, boolean craftAll, net.minecraft.server.level.ServerPlayer player) {
            MithrilAnvilPlaceRecipeHandler.handleRecipeClick(player, this, holder, craftAll);
        }

        @Override
        public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
            ItemStack itemstack = ItemStack.EMPTY;
            Slot slot = this.slots.get(index);
            if (slot.hasItem()) {
                ItemStack slotStack = slot.getItem();
                itemstack = slotStack.copy();
                if (index == 0) {
                    // 输出槽 → 玩家背包
                    if (!this.moveItemStackTo(slotStack, 6, 42, true)) {
                        return ItemStack.EMPTY;
                    }
                    slot.onQuickCraft(slotStack, itemstack);
                } else if (index >= 1 && index <= 5) {
                    // 输入槽只读
                    return ItemStack.EMPTY;
                } else {
                    // 玩家背包 → 无法移入输入槽（只读）
                    return ItemStack.EMPTY;
                }
                if (slotStack.isEmpty()) {
                    slot.setByPlayer(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
                slot.onTake(player, slotStack);
            }
            return itemstack;
        }

        @Override
        public boolean stillValid(@NotNull Player player) {
            return true;
        }

        @Override
        public void removed(@NotNull Player player) {
            super.removed(player);
            if (!player.level().isClientSide) {
                // 丢弃输入槽内容（不退回背包）
                inputContainer.clearContent();
                resultContainer.clearContent();
            }
        }

        private class MithrilAnvilResultSlot extends ResultSlot {
            MithrilAnvilResultSlot(Player player, CraftingContainer inputContainer, Container resultContainer, int slot, int x, int y) {
                super(player, inputContainer, resultContainer, slot, x, y);
            }

            @Override
            public ItemStack getItem() {
                // 只有可制作时才显示输出物品
                if (selectedRecipe != null && selectedRecipe.value().inner().canCraft(player)) {
                    return super.getItem();
                }
                return ItemStack.EMPTY;
            }

            @Override
            public boolean mayPickup(@NotNull Player player) {
                return selectedRecipe != null && selectedRecipe.value().inner().canCraft(player);
            }

            @Override
            public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
                // 从玩家背包消耗材料
                if (selectedRecipe != null) {
                    selectedRecipe.value().inner().consumeIngredients(player);
                    // 消耗后重新检查可制作性
                    updateResult();
                }
            }

            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        }
    }
}
