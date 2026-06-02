package first.servantry.client.screen;

import first.servantry.common.recipe.MithrilAnvilRecipe;
import first.servantry.network.MithrilAnvilPlaceRecipePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MithrilAnvilRecipePanel {

    private static final ResourceLocation RECIPE_BOOK_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/recipe_book.png");
    private static final WidgetSprites FILTER_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled"),
            ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled"),
            ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled_highlighted"),
            ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled_highlighted")
    );
    private static final WidgetSprites PAGE_FORWARD_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/page_forward"),
            ResourceLocation.withDefaultNamespace("recipe_book/page_forward_highlighted")
    );
    private static final WidgetSprites PAGE_BACKWARD_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/page_backward"),
            ResourceLocation.withDefaultNamespace("recipe_book/page_backward_highlighted")
    );

    private static final int PANEL_WIDTH = 147;
    private static final int PANEL_HEIGHT = 166;
    private static final int COLS = 5;
    private static final int ROWS = 4;
    private static final int ITEMS_PER_PAGE = COLS * ROWS;
    private static final int BUTTON_SIZE = 25;
    private static final int GAP = 2;

    private final MithrilAnvilRecipeButton[] recipeButtons = new MithrilAnvilRecipeButton[ITEMS_PER_PAGE];
    private Minecraft minecraft;
    private MithrilAnvilGui.MithrilAnvilMenu menu;
    private int panelX, panelY;
    private EditBox searchBox;
    private StateSwitchingButton filterButton;
    private StateSwitchingButton forwardButton, backButton;
    private List<RecipeHolder<MithrilAnvilRecipe>> allRecipes = new ArrayList<>();
    private List<RecipeHolder<MithrilAnvilRecipe>> filteredRecipes = new ArrayList<>();
    private int currentPage;
    private int totalPages;
    private int timesInventoryChanged;
    private String lastSearch = "";

    public void init(Minecraft minecraft, Player player, int screenWidth, int screenHeight, MithrilAnvilGui.MithrilAnvilMenu menu) {
        this.minecraft = minecraft;
        this.menu = menu;
        this.timesInventoryChanged = player.getInventory().getTimesChanged();

        // 配方面板位置：计算 leftPos 后反推 panelX
        // leftPos = panelX + PANEL_WIDTH + GAP，居中后两边留白相等
        int imageWidth = 170;
        int totalWidth = PANEL_WIDTH + GAP + imageWidth;
        int leftPos = (screenWidth - totalWidth) / 2 + PANEL_WIDTH + GAP;
        this.panelX = leftPos - PANEL_WIDTH - GAP;
        this.panelY = (screenHeight - PANEL_HEIGHT) / 2;

        // 搜索框
        this.searchBox = new EditBox(minecraft.font, panelX + 25, panelY + 13, 81, 14, Component.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(0xFFFFFF);
        this.searchBox.setHint(Component.translatable("gui.recipebook.search_hint"));

        // 可合成切换按钮
        this.filterButton = new StateSwitchingButton(panelX + 110, panelY + 12, 26, 16, false) {
            @Override
            public boolean isStateTriggered() {
                CompoundTag tag = player.getPersistentData();
                isStateTriggered = tag.getBoolean("mithril_anvil_recipe_panel");
                return isStateTriggered;
            }

            @Override
            public void setStateTriggered(boolean triggered) {
                CompoundTag tag = player.getPersistentData();
                tag.putBoolean("mithril_anvil_recipe_panel", !tag.getBoolean("mithril_anvil_recipe_panel"));
            }
        };
        this.filterButton.initTextureValues(FILTER_SPRITES);
        updateFilterTooltip();

        // 配方按钮
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int col = i % COLS;
            int row = i / COLS;
            recipeButtons[i] = new MithrilAnvilRecipeButton();
            recipeButtons[i].setPosition(panelX + 11 + col * BUTTON_SIZE, panelY + 31 + row * BUTTON_SIZE);
        }

        // 翻页按钮
        this.forwardButton = new StateSwitchingButton(panelX + 93, panelY + 137, 12, 17, false);
        this.forwardButton.initTextureValues(PAGE_FORWARD_SPRITES);
        this.backButton = new StateSwitchingButton(panelX + 38, panelY + 137, 12, 17, false);
        this.backButton.initTextureValues(PAGE_BACKWARD_SPRITES);

        // 加载配方
        loadRecipes();
    }

    @SuppressWarnings("unchecked")
    private void loadRecipes() {
        allRecipes = new ArrayList<>();
        if (minecraft.level != null) {
            for (RecipeHolder<?> holder : minecraft.level.getRecipeManager().getRecipes()) {
                if (holder.value() instanceof MithrilAnvilRecipe) {
                    allRecipes.add((RecipeHolder<MithrilAnvilRecipe>) holder);
                }
            }
        }
        updateFilteredRecipes();
    }

    public void tick() {
        if (minecraft.player != null && timesInventoryChanged != minecraft.player.getInventory().getTimesChanged()) {
            timesInventoryChanged = minecraft.player.getInventory().getTimesChanged();
            refreshCraftability();
            // 刷新输出槽显示（材料不足时可能变为空）
            menu.updateResult();
        }
    }

    private void updateFilteredRecipes() {
        String search = searchBox != null ? searchBox.getValue().toLowerCase(Locale.ROOT) : "";
        filteredRecipes = new ArrayList<>();

        for (RecipeHolder<MithrilAnvilRecipe> holder : allRecipes) {
            // 搜索过滤
            if (!search.isEmpty()) {
                ItemStack result = holder.value().getResultItem(minecraft.level.registryAccess());
                String displayName = result.getHoverName().getString().toLowerCase(Locale.ROOT);
                if (!displayName.contains(search)) continue;
            }

            // 可合成过滤
            if (filterButton.isStateTriggered()) {
                if (!canCraft(holder.value())) continue;
            }

            filteredRecipes.add(holder);
        }

        totalPages = Math.max(1, (int) Math.ceil((double) filteredRecipes.size() / ITEMS_PER_PAGE));
        if (currentPage >= totalPages) currentPage = Math.max(0, totalPages - 1);
        refreshPageButtons();
    }

    private void refreshCraftability() {
        if (filterButton.isStateTriggered()) {
            updateFilteredRecipes();
        } else {
            refreshPageButtons();
        }
    }

    private void refreshPageButtons() {
        int startIndex = currentPage * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int recipeIndex = startIndex + i;
            if (recipeIndex < filteredRecipes.size()) {
                RecipeHolder<MithrilAnvilRecipe> holder = filteredRecipes.get(recipeIndex);
                boolean craftable = canCraft(holder.value());
                recipeButtons[i].init(holder, craftable);
                recipeButtons[i].visible = true;
            } else {
                recipeButtons[i].visible = false;
            }
        }

        forwardButton.active = currentPage < totalPages - 1;
        forwardButton.setStateTriggered(currentPage < totalPages - 1);
        backButton.active = currentPage > 0;
        backButton.setStateTriggered(currentPage > 0);
    }

    private boolean canCraft(MithrilAnvilRecipe recipe) {
        if (minecraft.player == null) return false;
        return recipe.canCraft(minecraft.player);
    }

    private void updateFilterTooltip() {
        Tooltip tooltip = filterButton.isStateTriggered() ? Tooltip.create(Component.translatable("gui.recipebook.toggleRecipes.craftable")) : Tooltip.create(Component.translatable("gui.recipebook.toggleRecipes.all"));
        filterButton.setTooltip(tooltip);
    }

    public int getScreenOffset(int screenWidth, int imageWidth) {
        return panelX + PANEL_WIDTH + GAP;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 面板背景
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 100);
        graphics.blit(RECIPE_BOOK_LOCATION, panelX, panelY, 1, 1, PANEL_WIDTH, PANEL_HEIGHT);

        // 搜索框
        searchBox.render(graphics, mouseX, mouseY, partialTick);

        // 可合成切换按钮
        filterButton.render(graphics, mouseX, mouseY, partialTick);

        // 配方按钮
        for (MithrilAnvilRecipeButton button : recipeButtons) {
            if (button.visible) {
                button.render(graphics, mouseX, mouseY, partialTick);
            }
        }

        // 翻页箭头
        if (currentPage > 0) backButton.render(graphics, mouseX, mouseY, partialTick);
        if (currentPage < totalPages - 1) forwardButton.render(graphics, mouseX, mouseY, partialTick);

        // 页码
        if (totalPages > 1) {
            String pageText = (currentPage + 1) + "/" + totalPages;
            graphics.drawString(minecraft.font, pageText, panelX + 73 - minecraft.font.width(pageText) / 2, panelY + 141, 0xFFFFFF, false);
        }

        graphics.pose().popPose();
    }

    public void renderTooltip(GuiGraphics graphics, int leftPos, int topPos, int mouseX, int mouseY) {
        // 配方按钮提示
        for (MithrilAnvilRecipeButton button : recipeButtons) {
            if (button.visible && button.isHovered() && button.getRecipe() != null) {
                ItemStack result = button.getRecipe().value().getResultItem(minecraft.level.registryAccess());
                graphics.renderTooltip(minecraft.font, result, mouseX, mouseY);
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 搜索框
        if (searchBox.mouseClicked(mouseX, mouseY, button)) {
            searchBox.setFocused(true);
            return true;
        }
        searchBox.setFocused(false);

        // 可合成切换按钮
        if (filterButton.mouseClicked(mouseX, mouseY, button)) {
            filterButton.setStateTriggered(!filterButton.isStateTriggered());
            updateFilterTooltip();
            updateFilteredRecipes();
            return true;
        }

        // 配方按钮
        for (MithrilAnvilRecipeButton recipeBtn : recipeButtons) {
            if (recipeBtn.visible && recipeBtn.mouseClicked(mouseX, mouseY, button)) {
                RecipeHolder<MithrilAnvilRecipe> holder = recipeBtn.getRecipe();
                if (holder != null) {
                    selectRecipe(holder, Screen.hasShiftDown());
                }
                return true;
            }
        }

        // 翻页
        if (currentPage > 0 && backButton.mouseClicked(mouseX, mouseY, button)) {
            currentPage--;
            refreshPageButtons();
            return true;
        }
        if (currentPage < totalPages - 1 && forwardButton.mouseClicked(mouseX, mouseY, button)) {
            currentPage++;
            refreshPageButtons();
            return true;
        }

        return false;
    }

    private void selectRecipe(RecipeHolder<MithrilAnvilRecipe> holder, boolean craftAll) {
        // 设置客户端选中配方（填充输入槽显示物品）
        menu.setSelectedRecipe(holder);
        // 发送C2S包设置服务端选中配方
        PacketDistributor.sendToServer(new MithrilAnvilPlaceRecipePayload(menu.containerId, holder.id(), craftAll));
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            checkSearchUpdate();
            return true;
        }
        return searchBox.isFocused() && searchBox.isVisible() && keyCode != 256;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBox.charTyped(codePoint, modifiers)) {
            checkSearchUpdate();
            return true;
        }
        return false;
    }

    private void checkSearchUpdate() {
        String search = searchBox.getValue().toLowerCase(Locale.ROOT);
        if (!search.equals(lastSearch)) {
            lastSearch = search;
            currentPage = 0;
            updateFilteredRecipes();
        }
    }

    public boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int imageWidth, int imageHeight) {
        boolean outsideCrafting = mouseX < guiLeft || mouseY < guiTop
                || mouseX >= guiLeft + imageWidth || mouseY >= guiTop + imageHeight;
        boolean insidePanel = mouseX >= panelX && mouseX < panelX + PANEL_WIDTH
                && mouseY >= panelY && mouseY < panelY + PANEL_HEIGHT;
        return outsideCrafting && !insidePanel;
    }
}