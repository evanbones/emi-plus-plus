package com.evandev.remi.feature.creativemodetab.gui.itemtab;

import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.feature.creativemodetab.gui.CreativeModeTabGui;
import com.evandev.remi.integration.emi.ScreenManager;
import com.google.common.collect.ImmutableList;
import dev.emi.emi.config.SidebarTheme;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemTabNavigationBar extends AbstractContainerWidget {
    private final ItemTabManager tabManager;
    private final boolean isVertical;
    private final boolean isRightSide;
    public List<ItemTabButton> tabButtons = new ArrayList<>();
    public List<ItemTab> visibleTabs = new ArrayList<>();
    private GridLayout layout = new GridLayout();

    public ItemTabNavigationBar(ItemTabManager tabManager, boolean isVertical, boolean isRightSide) {
        super(0, 0, 0, 0, Component.empty());
        this.tabManager = tabManager;
        this.isVertical = isVertical;
        this.isRightSide = isRightSide;
    }

    public void pos(int x, int y) {
        this.setX(x);
        this.setY(y);
        arrangeElements();
    }

    public void setTabs(List<ItemTab> tabs) {
        this.visibleTabs = new ArrayList<>(tabs);
        if (tabs.isEmpty()) {
            this.tabButtons = ImmutableList.of();
            this.layout = new GridLayout();
            arrangeElements();
            return;
        }
        GridLayout newLayout = new GridLayout();
        newLayout.defaultCellSetting().padding(0);
        EmiScreenManager.SidebarPanel panel = ScreenManager.getTargetCreativeTabPanel();
        SidebarTheme panelTheme = panel != null ? panel.theme : SidebarTheme.TRANSPARENT;
        ImmutableList.Builder<ItemTabButton> buttonBuilder = ImmutableList.builder();

        int leftoverHeight = height % tabs.size();
        int leftoverWidth = width % tabs.size();

        for (int i = 0; i < tabs.size(); i++) {
            ItemTab tab = tabs.get(i);
            ItemTabButton.ButtonStyle buttonStyle = !isVertical ? ItemTabButton.ButtonStyle.TOP
                    : isRightSide ? ItemTabButton.ButtonStyle.RIGHT : ItemTabButton.ButtonStyle.LEFT;
            int w = isVertical ? ReliableEmiConfig.verticalTabsWidth + (panel != null ? panel.theme.horizontalPadding : 0) : ReliableEmiConfig.horizontalTabsWidth;
            int h = isVertical ? ReliableEmiConfig.verticalTabsHeight : ReliableEmiConfig.horizontalTabsHeight + (panel != null ? panel.theme.verticalPadding : 0);

            if (ReliableEmiConfig.tabAlignment == CreativeModeTabGui.TabAlignment.STRETCH) {
                if (isVertical) {
                    h = height / tabs.size();
                    if (leftoverHeight > 0) {
                        h += 1;
                        leftoverHeight--;
                    }
                } else {
                    w = width / tabs.size();
                    if (leftoverWidth > 0) {
                        w += 1;
                        leftoverWidth--;
                    }
                }
            }

            ItemTabButton.TabPosition tabPosition = ItemTabButton.TabPosition.MIDDLE;

            // Soooo many conditions. This could be nicer but I'm tired and it works!
            if (!CreativeModeTabGui.showScrollButtons || panelTheme != SidebarTheme.VANILLA) {
                if (i == 0 && (panelTheme != SidebarTheme.VANILLA || ReliableEmiConfig.tabAlignment == CreativeModeTabGui.TabAlignment.STRETCH || ReliableEmiConfig.tabAlignment == CreativeModeTabGui.TabAlignment.START)) {
                    tabPosition = ItemTabButton.TabPosition.FIRST;
                }
                if (i == tabs.size() - 1 && (panelTheme != SidebarTheme.VANILLA || ReliableEmiConfig.tabAlignment == CreativeModeTabGui.TabAlignment.STRETCH || ReliableEmiConfig.tabAlignment == CreativeModeTabGui.TabAlignment.END)) {
                    tabPosition = ItemTabButton.TabPosition.LAST;
                }
            }

            ItemTabButton button = new ItemTabButton(tabManager, tab, w, h, buttonStyle, tabPosition);
            buttonBuilder.add(button);
            if (isVertical) newLayout.addChild(button, i, 0);
            else newLayout.addChild(button, 0, i);
        }
        this.tabButtons = buttonBuilder.build();
        this.layout = newLayout;
        arrangeElements();
    }

    public void arrangeElements() {
        EmiScreenManager.SidebarPanel panel = ScreenManager.getTargetCreativeTabPanel();

        layout.setX(getX());
        layout.setY(getY());
        layout.arrangeElements();
        if (isVertical) {
            width = ReliableEmiConfig.verticalTabsWidth + (panel != null ? panel.theme.horizontalPadding : 0);
        } else {
            height = ReliableEmiConfig.horizontalTabsHeight + (panel != null ? panel.theme.verticalPadding : 0);
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.tabButtons;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics raw, int mouseX, int mouseY, float partialTick) {
        if (EmiScreenManager.isDisabled()) return;
        tabButtons.forEach(b -> b.render(raw, mouseX, mouseY, partialTick));
    }

    @Override
    public void setFocused(@Nullable GuiEventListener child) {
        super.setFocused(child);
        if (child instanceof TabButton tb) {
            tabManager.setCurrentTab(tb.tab(), false);
        }
    }

    public void setFocusedChild(GuiEventListener child) {
        setFocused(child);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (TabButton child : tabButtons) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                setFocused(child);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }
}
