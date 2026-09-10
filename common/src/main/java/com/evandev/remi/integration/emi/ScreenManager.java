package com.evandev.remi.integration.emi;

import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.feature.creativemodetab.CreativeModeTabManager;
import com.evandev.remi.feature.creativemodetab.gui.CreativeModeTabGui;
import com.evandev.remi.mixin.emi.accessor.EmiScreenManagerAccessor;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ScreenManager {
    public static final int ENTRY_SIZE = 18;
    public static EmiScreenManager.ScreenSpace indexScreenSpace;
    public static Component customIndexTitle;
    private static Component rawCustomIndexTitle;
    private static Screen screen;

    public static boolean isSearching() {
        return indexScreenSpace != null && indexScreenSpace.search
                && !EmiScreenManager.search.getValue().isEmpty();
    }

    public static void onScreenInitialized(Screen s) {
        screen = s;
    }

    public static void onIndexScreenSpaceCreated(EmiScreenManager.ScreenSpace space) {
        indexScreenSpace = space;
        refreshCustomIndexTitle();
        if (screen == null) return;

        if (ReliableEmiConfig.enableCreativeModeTabs) {
            CreativeModeTabGui.initialize(screen);
            CreativeModeTabManager.initialize();
        }
    }

    public static EmiScreenManager.SidebarPanel getTargetCreativeTabPanel() {
        if (!ReliableEmiConfig.enableCreativeModeTabs) return null;
        var panels = EmiScreenManagerAccessor.getPanels();
        if (panels == null || panels.isEmpty()) return null;

        var target = ReliableEmiConfig.creativeTabSidebarTarget;
        for (var p : panels) {
            if (target.matches(p)) {
                return p;
            }
        }

        var searchPanel = EmiScreenManager.getSearchPanel();
        if (searchPanel != null && searchPanel.space != null) {
            return searchPanel;
        }
        if (indexScreenSpace != null) {
            for (var p : panels) {
                if (p != null && p.space == indexScreenSpace) return p;
            }
        }
        return null;
    }

    public static EmiScreenManager.ScreenSpace getActiveCreativeTabScreenSpace() {
        var panel = getTargetCreativeTabPanel();
        return panel != null ? panel.space : null;
    }

    public static boolean onMouseScrolled(double mouseX, double mouseY, double amount) {
        if (ReliableEmiConfig.enableCreativeModeTabs && CreativeModeTabGui.contains(mouseX, mouseY)) {
            return CreativeModeTabGui.onMouseScrolled(amount);
        }
        return false;
    }

    public static void setCustomIndexTitle(Component title) {
        rawCustomIndexTitle = title;
        refreshCustomIndexTitle();
    }

    public static void refreshCustomIndexTitle() {
        if (rawCustomIndexTitle == null) {
            customIndexTitle = null;
            return;
        }
        var font = Minecraft.getInstance().font;
        int spaceWidth = ScreenManager.indexScreenSpace != null ? ScreenManager.indexScreenSpace.tw : 0;
        int maxWidth = spaceWidth * ScreenManager.ENTRY_SIZE - 20;

        ScreenManager.customIndexTitle = (maxWidth > 0 && font.width(rawCustomIndexTitle) > maxWidth)
                ? Component.literal(font.plainSubstrByWidth(rawCustomIndexTitle.getString(), Math.max(1, maxWidth - font.width("..."))) + "...")
                : rawCustomIndexTitle;
    }

    public static void removeCustomIndexTitle(Component component) {
        if (rawCustomIndexTitle == component || customIndexTitle == component) {
            rawCustomIndexTitle = null;
            customIndexTitle = null;
        }
    }
}
