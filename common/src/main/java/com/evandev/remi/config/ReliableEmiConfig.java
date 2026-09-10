package com.evandev.remi.config;

import com.evandev.ReliableEmi;
import com.evandev.remi.feature.creativemodetab.CreativeModeTabManager;
import com.evandev.remi.feature.creativemodetab.gui.CreativeModeTabGui;
import com.evandev.remi.feature.stackgroup.StackGroupManager;
import com.evandev.remi.feature.workstation.WorkstationSidebarManager;
import com.evandev.remi.integration.emi.StackManager;
import com.evandev.remi.platform.Services;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReliableEmiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static boolean enableCreativeModeTabs = true;
    public static CreativeTabSidebarTarget creativeTabSidebarTarget = CreativeTabSidebarTarget.INDEX;
    public static CreativeTabTheme creativeTabTheme = CreativeTabTheme.SYNCED;
    public static boolean syncSelectedCreativeModeTab = true;
    public static boolean showCreativeTabNameInSearchbar = true;
    public static int maxSidebarTabs = 0;
    public static int verticalTabsWidth = 28;
    public static int verticalTabsHeight = 24;
    public static int horizontalTabsWidth = 22;
    public static int horizontalTabsHeight = 24;
    public static int tabIconSize = 16;
    public static CreativeModeTabGui.TabAlignment tabAlignment = CreativeModeTabGui.TabAlignment.STRETCH;
    public static List<String> disabledCreativeModeTabs = new ArrayList<>(List.of("minecraft:op_blocks"));
    public static Map<String, List<String>> stackGroupItemOrder = new HashMap<>();
    public static boolean enableStackGroups = true;
    public static boolean enableCreateStackGroupButton = true;
    public static List<String> disabledStackGroups = new ArrayList<>();
    public static boolean stackGroupsIndex = true;
    public static boolean stackGroupsCraftables = true;
    public static boolean stackGroupsWorkstation = false;
    public static boolean stackGroupsFavorites = false;
    public static boolean enableCategorizedTagPages = true;
    public static boolean enableEntityTags = true;
    public static boolean enableTagSearchEnhancements = true;
    public static boolean emiOnlyInRecipeBook = false;
    public static boolean emiOnlyInRecipeBookState = false;
    public static boolean disableEmiGlobalConfig = false;
    public static boolean dragCheatToInventory = true;
    public static boolean disablePaginationWrapping = false;
    public static boolean scrollInsteadOfPagination = false;
    public static boolean showTitleInsteadOfPageNumbers = false;
    public static boolean hidePageButtonWhenOnePage = false;
    public static boolean incrementalScrollbarFill = false;
    public static boolean verticalScrollbar = false;

    public static boolean searchWidgetAlignWithPanel = false;
    public static int searchWidgetWidth = 0;
    public static int searchWidgetLeftOffset = 0;
    public static int searchWidgetTopOffset = 0;
    public static int searchWidgetHorizontalPadding = 4;
    public static int searchWidgetVerticalPadding = 2;
    public static int searchWidgetSuggestionTextColor = 0xFF808080;
    public static int searchWidgetTextColor = 0xFFFFFFFF;
    public static boolean searchWidgetUseVanillaTexture = false;

    public static boolean searchById = true;
    public static boolean searchModPrefix = true;
    public static boolean searchTagPrefix = true;
    public static boolean searchTooltipPrefix = true;

    private static boolean loaded = false;

    public static boolean isLoaded() {
        return loaded;
    }

    public static boolean isVerticalScrollbarEnabled() {
        return verticalScrollbar && scrollInsteadOfPagination;
    }

    public static boolean isCreativeTabsEnabled(SidebarType type) {
        if (!enableCreativeModeTabs || type == null) return false;
        var targetPanel = com.evandev.remi.integration.emi.ScreenManager.getTargetCreativeTabPanel();
        return targetPanel != null && targetPanel.getType() == type;
    }

    public static boolean isStackGroupsEnabled(SidebarType type) {
        if (!enableStackGroups || type == null) return false;
        if (type == SidebarType.INDEX) return stackGroupsIndex;
        if (type == SidebarType.CRAFTABLES) return stackGroupsCraftables;
        if (WorkstationSidebarManager.WORKSTATION != null && type == WorkstationSidebarManager.WORKSTATION)
            return stackGroupsWorkstation;
        if (type == SidebarType.FAVORITES) return stackGroupsFavorites;
        return false;
    }

    public static Path getConfigDir() {
        Path remiDir = Services.PLATFORM.getConfigDirectory().resolve(ReliableEmi.MOD_ID);
        if (Files.isDirectory(remiDir) || Files.exists(remiDir)) {
            return remiDir;
        }
        Path emixxDir = Services.PLATFORM.getConfigDirectory().resolve("emixx");
        if (Files.isDirectory(emixxDir) || Files.exists(emixxDir)) {
            return emixxDir;
        }
        return remiDir;
    }

    public static Path getConfigPath() {
        Path dir = getConfigDir();
        Path remiPath = dir.resolve(ReliableEmi.MOD_ID + ".json");
        if (Files.exists(remiPath)) {
            return remiPath;
        }
        Path emixxPath = dir.resolve("emixx.json");
        if (Files.exists(emixxPath)) {
            return emixxPath;
        }
        Path rootEmixxPath = Services.PLATFORM.getConfigDirectory().resolve("emixx.json");
        if (Files.exists(rootEmixxPath)) {
            return rootEmixxPath;
        }
        Path rootRemiPath = Services.PLATFORM.getConfigDirectory().resolve(ReliableEmi.MOD_ID + ".json");
        if (Files.exists(rootRemiPath)) {
            return rootRemiPath;
        }
        return remiPath;
    }

    public static void load() {
        Path path = getConfigPath();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null) {
                    enableCreativeModeTabs = data.enableCreativeModeTabs;
                    if (data.creativeTabSidebarTarget != null) creativeTabSidebarTarget = data.creativeTabSidebarTarget;
                    if (data.creativeTabTheme != null) creativeTabTheme = data.creativeTabTheme;
                    if (data.tabAlignment != null) tabAlignment = data.tabAlignment;
                    verticalTabsWidth = data.verticalTabsWidth;
                    verticalTabsHeight = data.verticalTabsHeight;
                    horizontalTabsWidth = data.horizontalTabsWidth;
                    horizontalTabsHeight = data.horizontalTabsHeight;
                    tabIconSize = data.tabIconSize;
                    syncSelectedCreativeModeTab = data.syncSelectedCreativeModeTab;
                    showCreativeTabNameInSearchbar = data.showCreativeTabNameInSearchbar;
                    maxSidebarTabs = data.maxSidebarTabs;
                    if (data.disabledCreativeModeTabs != null) {
                        disabledCreativeModeTabs = new ArrayList<>(data.disabledCreativeModeTabs);
                    }

                    enableStackGroups = data.enableStackGroups;
                    enableCreateStackGroupButton = data.enableCreateStackGroupButton;
                    stackGroupsIndex = data.stackGroupsIndex;
                    stackGroupsCraftables = data.stackGroupsCraftables;
                    stackGroupsWorkstation = data.stackGroupsWorkstation;
                    stackGroupsFavorites = data.stackGroupsFavorites;

                    if (data.disabledStackGroups != null) {
                        disabledStackGroups = new ArrayList<>(data.disabledStackGroups);
                    }

                    if (data.stackGroupItemOrder != null) {
                        stackGroupItemOrder = new HashMap<>(data.stackGroupItemOrder);
                    }

                    enableCategorizedTagPages = data.enableCategorizedTagPages;
                    enableEntityTags = data.enableEntityTags;
                    enableTagSearchEnhancements = data.enableTagSearchEnhancements;

                    emiOnlyInRecipeBook = data.emiOnlyInRecipeBook;
                    emiOnlyInRecipeBookState = data.emiOnlyInRecipeBookState;
                    disableEmiGlobalConfig = data.disableEmiGlobalConfig;
                    dragCheatToInventory = data.dragCheatToInventory;
                    disablePaginationWrapping = data.disablePaginationWrapping;
                    scrollInsteadOfPagination = data.scrollInsteadOfPagination;
                    showTitleInsteadOfPageNumbers = data.showTitleInsteadOfPageNumbers;
                    hidePageButtonWhenOnePage = data.hidePageButtonWhenOnePage;
                    incrementalScrollbarFill = data.incrementalScrollbarFill;
                    verticalScrollbar = data.verticalScrollbar;

                    searchWidgetAlignWithPanel = data.searchWidgetAlignWithPanel;
                    searchWidgetWidth = data.searchWidgetWidth;
                    searchWidgetLeftOffset = data.searchWidgetLeftOffset;
                    searchWidgetTopOffset = data.searchWidgetTopOffset;
                    searchWidgetHorizontalPadding = data.searchWidgetHorizontalPadding;
                    searchWidgetVerticalPadding = data.searchWidgetVerticalPadding;
                    searchWidgetSuggestionTextColor = data.searchWidgetSuggestionTextColor;
                    searchWidgetTextColor = data.searchWidgetTextColor;
                    searchWidgetUseVanillaTexture = data.searchWidgetUseVanillaTexture;

                    searchById = data.searchById;
                    searchModPrefix = data.searchModPrefix;
                    searchTagPrefix = data.searchTagPrefix;
                    searchTooltipPrefix = data.searchTooltipPrefix;
                }
            } catch (IOException | JsonSyntaxException e) {
                ReliableEmi.LOGGER.error("Failed to load config", e);
            }
        }
        loaded = true;
        save();
    }

    public static void reload() {
        load();
        try {
            CreativeModeTabManager.reload();
        } catch (Throwable t) {
            ReliableEmi.LOGGER.error("Failed to reload creative mode tabs", t);
        }
        try {
            StackGroupManager.reload();
        } catch (Throwable t) {
            ReliableEmi.LOGGER.error("Failed to reload stack groups", t);
        }
        try {
            StackManager.reload();
        } catch (Throwable t) {
            ReliableEmi.LOGGER.error("Failed to reload stack manager", t);
        }
        try {
            EmiScreenManager.recalculate();
        } catch (Throwable t) {
            ReliableEmi.LOGGER.error("Failed to recalculate EMI screen manager", t);
        }
    }

    public static void save() {
        StackManager.invalidateStacks();
        Path path = getConfigPath();
        try {
            Files.createDirectories(path.getParent());
            StackGroupManager.getStackGroupsDir();
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(collectData(), writer);
            }
        } catch (IOException | JsonSyntaxException e) {
            ReliableEmi.LOGGER.error("Failed to save config", e);
        }

        if (disableEmiGlobalConfig) {
            EmiConfig.useGlobalConfig = false;
        }
        EmiConfig.loadConfig();

        var client = Minecraft.getInstance();
        if (client.screen != null && !EmiScreenManager.isDisabled()) {
            EmiScreenManager.addWidgets(client.screen);
        }
        if (EmiSearch.bakedStacks != null) {
            EmiSearch.bake();
            EmiSearch.update();
        }
    }

    private static ConfigData collectData() {
        ConfigData data = new ConfigData();
        data.enableCreativeModeTabs = enableCreativeModeTabs;
        data.creativeTabSidebarTarget = creativeTabSidebarTarget;
        data.creativeTabTheme = creativeTabTheme;
        data.tabAlignment = tabAlignment;
        data.verticalTabsWidth = verticalTabsWidth;
        data.verticalTabsHeight = verticalTabsHeight;
        data.horizontalTabsWidth = horizontalTabsWidth;
        data.horizontalTabsHeight = horizontalTabsHeight;
        data.tabIconSize = tabIconSize;
        data.syncSelectedCreativeModeTab = syncSelectedCreativeModeTab;
        data.showCreativeTabNameInSearchbar = showCreativeTabNameInSearchbar;
        data.maxSidebarTabs = maxSidebarTabs;
        data.disabledCreativeModeTabs = new ArrayList<>(disabledCreativeModeTabs);
        data.enableStackGroups = enableStackGroups;
        data.enableCreateStackGroupButton = enableCreateStackGroupButton;
        data.stackGroupsIndex = stackGroupsIndex;
        data.stackGroupsCraftables = stackGroupsCraftables;
        data.stackGroupsWorkstation = stackGroupsWorkstation;
        data.stackGroupsFavorites = stackGroupsFavorites;
        data.disabledStackGroups = new ArrayList<>(disabledStackGroups);
        data.enableCategorizedTagPages = enableCategorizedTagPages;
        data.enableEntityTags = enableEntityTags;
        data.enableTagSearchEnhancements = enableTagSearchEnhancements;
        data.emiOnlyInRecipeBook = emiOnlyInRecipeBook;
        data.emiOnlyInRecipeBookState = emiOnlyInRecipeBookState;
        data.disableEmiGlobalConfig = disableEmiGlobalConfig;
        data.dragCheatToInventory = dragCheatToInventory;
        data.stackGroupItemOrder = new HashMap<>(stackGroupItemOrder);
        data.disablePaginationWrapping = disablePaginationWrapping;
        data.scrollInsteadOfPagination = scrollInsteadOfPagination;
        data.showTitleInsteadOfPageNumbers = showTitleInsteadOfPageNumbers;
        data.hidePageButtonWhenOnePage = hidePageButtonWhenOnePage;
        data.incrementalScrollbarFill = incrementalScrollbarFill;
        data.verticalScrollbar = verticalScrollbar;
        data.searchWidgetAlignWithPanel = searchWidgetAlignWithPanel;
        data.searchWidgetWidth = searchWidgetWidth;
        data.searchWidgetLeftOffset = searchWidgetLeftOffset;
        data.searchWidgetTopOffset = searchWidgetTopOffset;
        data.searchWidgetHorizontalPadding = searchWidgetHorizontalPadding;
        data.searchWidgetVerticalPadding = searchWidgetVerticalPadding;
        data.searchWidgetSuggestionTextColor = searchWidgetSuggestionTextColor;
        data.searchWidgetTextColor = searchWidgetTextColor;
        data.searchWidgetUseVanillaTexture = searchWidgetUseVanillaTexture;
        data.searchById = searchById;
        data.searchModPrefix = searchModPrefix;
        data.searchTagPrefix = searchTagPrefix;
        data.searchTooltipPrefix = searchTooltipPrefix;
        return data;
    }

    public enum CreativeTabSidebarTarget {
        INDEX, CRAFTABLES, WORKSTATION, FAVORITES, LEFT, RIGHT, TOP, BOTTOM;

        public boolean matches(EmiScreenManager.SidebarPanel panel) {
            if (panel == null) return false;
            return switch (this) {
                case INDEX -> panel.getType() == SidebarType.INDEX;
                case CRAFTABLES -> panel.getType() == SidebarType.CRAFTABLES;
                case WORKSTATION ->
                        WorkstationSidebarManager.WORKSTATION != null && panel.getType() == WorkstationSidebarManager.WORKSTATION;
                case FAVORITES -> panel.getType() == SidebarType.FAVORITES;
                case LEFT -> panel.side == SidebarSide.LEFT;
                case RIGHT -> panel.side == SidebarSide.RIGHT;
                case TOP -> panel.side == SidebarSide.TOP;
                case BOTTOM -> panel.side == SidebarSide.BOTTOM;
            };
        }
    }

    public enum CreativeTabTheme {
        SYNCED, HORIZONTAL, VERTICAL
    }

    private static class ConfigData {
        boolean enableCreativeModeTabs = true;
        CreativeTabSidebarTarget creativeTabSidebarTarget = CreativeTabSidebarTarget.INDEX;
        CreativeTabTheme creativeTabTheme = CreativeTabTheme.SYNCED;
        CreativeModeTabGui.TabAlignment tabAlignment = CreativeModeTabGui.TabAlignment.STRETCH;
        int verticalTabsWidth = 28;
        int verticalTabsHeight = 24;
        int horizontalTabsWidth = 22;
        int horizontalTabsHeight = 24;
        int tabIconSize = 16;
        boolean syncSelectedCreativeModeTab = true;
        boolean showCreativeTabNameInSearchbar = true;
        int maxSidebarTabs = 0;
        List<String> disabledCreativeModeTabs = new ArrayList<>(List.of("minecraft:op_blocks"));
        boolean enableStackGroups = true;
        boolean enableCreateStackGroupButton = true;
        boolean stackGroupsIndex = true;
        boolean stackGroupsCraftables = true;
        boolean stackGroupsWorkstation = true;
        boolean stackGroupsFavorites = false;
        List<String> disabledStackGroups = new ArrayList<>();
        Map<String, List<String>> stackGroupItemOrder = new HashMap<>();
        boolean emiOnlyInRecipeBook = false;
        boolean emiOnlyInRecipeBookState = false;
        boolean disableEmiGlobalConfig = false;
        boolean dragCheatToInventory = true;

        // Tags
        boolean enableCategorizedTagPages = true;
        boolean enableEntityTags = true;
        boolean enableTagSearchEnhancements = true;

        // Scrolling
        boolean disablePaginationWrapping = false;
        boolean scrollInsteadOfPagination = false;
        boolean showTitleInsteadOfPageNumbers = false;
        boolean hidePageButtonWhenOnePage = false;
        boolean incrementalScrollbarFill = false;
        boolean verticalScrollbar = false;

        // Search Widget
        boolean searchWidgetAlignWithPanel = false;
        int searchWidgetWidth = 0;
        int searchWidgetLeftOffset = 0;
        int searchWidgetTopOffset = 0;
        int searchWidgetHorizontalPadding = 4;
        int searchWidgetVerticalPadding = 2;
        int searchWidgetSuggestionTextColor = 0xFF808080;
        int searchWidgetTextColor = 0xFFFFFFFF;
        boolean searchWidgetUseVanillaTexture = false;

        // Search
        boolean searchById = true;
        boolean searchModPrefix = true;
        boolean searchTagPrefix = true;
        boolean searchTooltipPrefix = true;
    }
}