package com.evandev.remi.mixin.emi;

import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.feature.creativemodetab.CreativeModeTabManager;
import com.evandev.remi.feature.stackgroup.EmiGroupStack;
import com.evandev.remi.feature.stackgroup.StackGroupManager;
import com.evandev.remi.feature.workstation.WorkstationSidebarManager;
import com.evandev.remi.integration.emi.Layout;
import com.evandev.remi.integration.emi.StackManager;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiSidebars;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.StackBatcher;
import dev.emi.emi.search.EmiSearch;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Mixin(value = EmiScreenManager.ScreenSpace.class, remap = false)
public abstract class EmiScreenManagerScreenSpaceMixin {

    @Shadow
    @Final
    public int pageSize;

    @Shadow
    @Final
    public StackBatcher batcher;

    @Shadow
    @Final
    public int th;

    @Shadow
    @Final
    public int tw;

    @Shadow
    @Final
    public boolean search;

    @Unique
    private String remi$lastSearchValue;

    @Unique
    private EmiSearch.CompiledQuery remi$compiledQuery;

    @Unique
    private List<? extends EmiIngredient> remi$cachedStacks;

    @Unique
    private List<? extends EmiIngredient> remi$cacheSource;

    @Unique
    private int remi$cacheSourceSize;

    @Unique
    private SidebarType remi$cacheType;

    @Unique
    private String remi$cacheSearch;

    @Unique
    private Object remi$cacheTab;

    @Unique
    private boolean remi$cacheEditMode;

    @Unique
    private boolean remi$cacheTabsEnabled;

    @Unique
    private boolean remi$cacheGroupsEnabled;

    @Unique
    private int remi$cacheVersion;

    @Shadow
    public abstract List<? extends EmiIngredient> getStacks();

    @Shadow
    public abstract int getWidth(int y);

    @Shadow
    public abstract int getX(int x, int y);

    @Shadow
    public abstract int getY(int x, int y);

    @Shadow
    public abstract SidebarType getType();

    @Inject(method = "getStacks", at = @At("HEAD"), cancellable = true)
    private void remi$getCorrectStacks(CallbackInfoReturnable<List<? extends EmiIngredient>> cir) {
        SidebarType type = getType();
        if (type == SidebarType.CHESS || type == SidebarType.NONE || type == SidebarType.EMPTY) {
            return;
        }

        List<? extends EmiIngredient> source = remi$getSourceStacks(type);
        String searchValue = this.search && EmiScreenManager.search != null ? EmiScreenManager.search.getValue() : "";
        Object tab = CreativeModeTabManager.getCurrentTab();
        boolean editMode = EmiConfig.editMode;
        boolean tabsEnabled = ReliableEmiConfig.isCreativeTabsEnabled(type);
        boolean groupsEnabled = ReliableEmiConfig.isStackGroupsEnabled(type);
        int version = StackManager.getStacksVersion();

        if (remi$cachedStacks != null
                && remi$cacheVersion == version
                && remi$cacheSource == source
                && remi$cacheSourceSize == source.size()
                && remi$cacheType == type
                && remi$cacheTab == tab
                && remi$cacheEditMode == editMode
                && remi$cacheTabsEnabled == tabsEnabled
                && remi$cacheGroupsEnabled == groupsEnabled
                && searchValue.equals(remi$cacheSearch)) {
            cir.setReturnValue(remi$cachedStacks);
            return;
        }

        List<? extends EmiIngredient> stacks = remi$buildStacks(type, source, searchValue, tabsEnabled, groupsEnabled);

        remi$cachedStacks = stacks;
        remi$cacheVersion = StackManager.getStacksVersion();
        remi$cacheSource = source;
        remi$cacheSourceSize = source.size();
        remi$cacheType = type;
        remi$cacheTab = tab;
        remi$cacheEditMode = editMode;
        remi$cacheTabsEnabled = tabsEnabled;
        remi$cacheGroupsEnabled = groupsEnabled;
        remi$cacheSearch = searchValue;
        cir.setReturnValue(stacks);
    }

    @Unique
    private List<? extends EmiIngredient> remi$getSourceStacks(SidebarType type) {
        List<? extends EmiIngredient> stacks;
        if (type == SidebarType.INDEX) {
            stacks = this.search ? StackManager.displayedStacks : StackManager.unsearchedStacks;
        } else if (WorkstationSidebarManager.WORKSTATION != null && type == WorkstationSidebarManager.WORKSTATION) {
            stacks = WorkstationSidebarManager.workstationStacks;
        } else {
            stacks = EmiSidebars.getStacks(type);
        }
        return stacks == null ? List.of() : stacks;
    }

    @Unique
    private List<? extends EmiIngredient> remi$buildStacks(SidebarType type, List<? extends EmiIngredient> source,
                                                           String searchValue, boolean tabsEnabled,
                                                           boolean groupsEnabled) {
        List<? extends EmiIngredient> stacks = source;
        if (type == SidebarType.INDEX) {
            if (!groupsEnabled) {
                List<EmiIngredient> ungrouped = new ArrayList<>();
                for (EmiIngredient ing : stacks) {
                    if (ing instanceof EmiGroupStack gs) {
                        for (var item : gs.getItems()) ungrouped.add(item.realStack);
                    } else {
                        ungrouped.add(ing);
                    }
                }
                stacks = ungrouped;
            }
        } else {
            if (tabsEnabled) {
                List<EmiIngredient> tabFiltered = new ArrayList<>();
                for (EmiIngredient ing : stacks) {
                    if (CreativeModeTabManager.isIngredientInCurrentTab(ing)) {
                        tabFiltered.add(ing);
                    }
                }
                stacks = tabFiltered;
            }

            if (this.search && EmiScreenManager.search != null) {
                if (!Objects.equals(searchValue, remi$lastSearchValue)) {
                    remi$lastSearchValue = searchValue;
                    remi$compiledQuery = searchValue.isEmpty() ? null : new EmiSearch.CompiledQuery(searchValue);
                    if (batcher != null) {
                        batcher.repopulate();
                    }
                }
                if (remi$compiledQuery != null && !remi$compiledQuery.isEmpty()) {
                    List<EmiIngredient> searchFiltered = new ArrayList<>();
                    for (EmiIngredient ing : stacks) {
                        List<EmiStack> emiStacks = ing.getEmiStacks();
                        if (!emiStacks.isEmpty() && remi$compiledQuery.test(emiStacks.get(0))) {
                            searchFiltered.add(ing);
                        }
                    }
                    stacks = searchFiltered;
                }
            }

            if (groupsEnabled) {
                stacks = StackGroupManager.buildGroupedIngredients(stacks, type);
            }
        }
        return stacks;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void createGrid(int tx, int ty, int tw, int th, boolean rtl, List<Bounds> exclusion,
                            Supplier<SidebarType> typeSupplier, boolean search, CallbackInfo ci) {
        if (ReliableEmiConfig.isStackGroupsEnabled(getType()))
            StackManager.stackGrid = new EmiStack[th + 9][tw + 9];
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void remi$prepareGrid(EmiDrawContext context, int mouseX, int mouseY, float delta, int startIndex, CallbackInfo ci) {
        if (!ReliableEmiConfig.isStackGroupsEnabled(getType())) return;

        Layout.checkGridSize(tw, th);
        for (EmiStack[] row : StackManager.stackGrid) {
            if (row != null) Arrays.fill(row, null);
        }

        if (pageSize > 0) {
            List<? extends EmiIngredient> stacks = getStacks();
            int i = startIndex;
            outer:
            for (int yo = 0; yo < th; yo++) {
                for (int xo = 0; xo < getWidth(yo); xo++) {
                    if (i >= stacks.size()) break outer;
                    EmiIngredient stack = stacks.get(i++);
                    EmiStack gridStack = stack instanceof EmiStack es ? es : (stack != null && !stack.getEmiStacks().isEmpty() ? stack.getEmiStacks().get(0) : null);
                    if (gridStack != null) {
                        StackManager.stackGrid[yo][xo] = gridStack;
                    }
                }
            }
        }
    }

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/StackBatcher;draw()V"))
    private void remi$renderLayoutTiles(EmiDrawContext context, int mouseX, int mouseY, float delta, int startIndex, CallbackInfo ci) {
        if (ReliableEmiConfig.isStackGroupsEnabled(getType())) {
            Layout.buildLayoutTiles((EmiScreenManager.ScreenSpace) (Object) this, context);
        }
    }
}
