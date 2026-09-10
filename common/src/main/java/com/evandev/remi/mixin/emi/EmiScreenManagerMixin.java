package com.evandev.remi.mixin.emi;

import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.feature.creativemodetab.gui.CreativeModeTabGui;
import com.evandev.remi.feature.stackgroup.EmiGroupStack;
import com.evandev.remi.feature.workstation.WorkstationSidebarManager;
import com.evandev.remi.gui.components.ScrollbarWidget;
import com.evandev.remi.integration.emi.Layout;
import com.evandev.remi.integration.emi.ScreenManager;
import com.evandev.remi.integration.emi.StackManager;
import com.evandev.remi.mixin.minecraft.accessor.AbstractContainerScreenAccessor;
import com.evandev.remi.mixin.minecraft.accessor.SlotWrapperAccessor;
import com.evandev.remi.util.SidebarPanelWithScrollOffset;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.config.*;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.widget.EmiSearchWidget;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = EmiScreenManager.class, remap = false)
public abstract class EmiScreenManagerMixin {

    @Unique
    private static final int REMI_SEARCH_HEIGHT = 18;
    @Unique
    private static final int REMI_VANILLA_BACKGROUND_OFFSET = 11;
    @Unique
    private static final int REMI_CORNER_BUTTON_SPACE = 22;
    @Shadow
    public static EmiSearchWidget search;
    @Shadow
    private static List<? extends EmiIngredient> searchedStacks;
    @Shadow
    private static List<EmiScreenManager.SidebarPanel> panels;
    @Final
    @Shadow
    private static int ENTRY_SIZE, SUBPANEL_SEPARATOR_SIZE;

    @Shadow
    public static EmiScreenManager.SidebarPanel getSearchPanel() {
        throw new UnsupportedOperationException();
    }

    @ModifyVariable(at = @At(value = "STORE", ordinal = 0), method = "createScreenSpace", name = "headerOffset")
    private static int modifyHeaderOffset(int headerOffset, EmiScreenManager.SidebarPanel panel, Screen screen,
                                          List<Bounds> exclusion, @Local(name = "theme") SidebarTheme theme) {
        if (ReliableEmiConfig.isCreativeTabsEnabled(panel.getType())) {
            EmiScreenManager.SidebarPanel targetPanel = ScreenManager.getTargetCreativeTabPanel();
            if (targetPanel == panel && CreativeModeTabGui.currentTheme() == CreativeModeTabGui.TabTheme.HORIZONTAL) {
                return headerOffset + ReliableEmiConfig.horizontalTabsHeight + theme.verticalPadding;
            }
        }
        return headerOffset;
    }

    @Unique
    private static EmiScreenManager.SidebarPanel remi$getEffectiveSearchPanel() {
        EmiScreenManager.SidebarPanel searchPanel = getSearchPanel();
        if (searchPanel == null) {
            return null;
        }
        if (searchPanel.space != null && searchPanel.getType() == SidebarType.INDEX) {
            return searchPanel;
        }
        for (EmiScreenManager.SidebarPanel p : panels) {
            if (p.getType() == SidebarType.INDEX && p.space != null) {
                return p;
            }
        }
        if (searchPanel.space != null) {
            return searchPanel;
        }
        for (EmiScreenManager.SidebarPanel p : panels) {
            if (p.space != null) {
                return p;
            }
        }
        return searchPanel;
    }

    @Unique
    private static EmiScreenManager.SidebarPanel remi$getSearchAnchorPanel() {
        EmiScreenManager.SidebarPanel searchPanel = getSearchPanel();
        if (searchPanel == null) {
            return null;
        }
        if (searchPanel.getType() == SidebarType.INDEX) {
            return searchPanel;
        }
        for (EmiScreenManager.SidebarPanel p : panels) {
            if (p.getType() == SidebarType.INDEX) {
                return p;
            }
        }
        return searchPanel;
    }

    @Unique
    private static boolean remi$areCornerButtonsVisible() {
        boolean visible = !EmiScreenManager.isDisabled();
        return EmiConfig.emiConfigButtonVisibility.resolve(visible)
                || EmiConfig.recipeTreeButtonVisibility.resolve(visible);
    }

    @Unique
    private static int remi$getAlignedSearchHeight() {
        return SUBPANEL_SEPARATOR_SIZE + 2 + REMI_SEARCH_HEIGHT
                + Math.max(0, ReliableEmiConfig.searchWidgetVerticalPadding - 1)
                + Math.max(0, ReliableEmiConfig.searchWidgetTopOffset);
    }

    @Unique
    private static int remi$getBottomReserve(EmiScreenManager.SidebarPanel panel, SidebarSettings settings) {
        SidebarTheme theme = panel.getType() == SidebarType.CHESS ? SidebarTheme.MODERN : settings.theme();
        boolean anchor = remi$getSearchAnchorPanel() == panel;
        int buttonSpace = panel.side == SidebarSide.LEFT && remi$areCornerButtonsVisible()
                ? REMI_CORNER_BUTTON_SPACE
                : 0;

        int below;
        if (ReliableEmiConfig.searchWidgetAlignWithPanel) {
            below = anchor ? remi$getAlignedSearchHeight() + buttonSpace : buttonSpace;
        } else if (anchor && !EmiConfig.centerSearchBar
                && (panel.side == SidebarSide.LEFT || panel.side == SidebarSide.RIGHT)) {
            int barSpace = (panel.side == SidebarSide.RIGHT ? 21 : 21 + 21)
                    + Math.max(0, ReliableEmiConfig.searchWidgetTopOffset);
            below = Math.max(buttonSpace, barSpace);
        } else {
            below = buttonSpace;
        }

        if (below <= 0) {
            return 0;
        }
        below += theme == SidebarTheme.VANILLA ? REMI_VANILLA_BACKGROUND_OFFSET : 0;
        return Math.max(0, below - settings.margins().bottom() - theme.verticalPadding);
    }

    @Unique
    private static boolean remi$applySearchWidgetLayout() {
        if (!ReliableEmiConfig.searchWidgetAlignWithPanel && EmiConfig.centerSearchBar) {
            return false;
        }
        EmiScreenManager.SidebarPanel panel = remi$getSearchAnchorPanel();
        if (panel == null || panel.space == null) {
            return false;
        }
        Screen screen = Minecraft.getInstance().screen;
        if (screen == null) {
            return false;
        }

        int panelWidth = panel.space.tw * ENTRY_SIZE + (panel.theme.horizontalPadding * 2) + (ReliableEmiConfig.isVerticalScrollbarEnabled() && panel.theme == SidebarTheme.VANILLA ? ScrollbarWidget.WIDTH - panel.theme.horizontalPadding : 0);
        search.setX(panel.space.tx - panel.theme.horizontalPadding + ReliableEmiConfig.searchWidgetLeftOffset + ReliableEmiConfig.searchWidgetHorizontalPadding);
        search.setWidth(Math.max(1, panelWidth + ReliableEmiConfig.searchWidgetWidth - ReliableEmiConfig.searchWidgetHorizontalPadding * 2));

        if (ReliableEmiConfig.searchWidgetAlignWithPanel) {
            int totalHeight = panel.theme == SidebarTheme.VANILLA ? REMI_VANILLA_BACKGROUND_OFFSET : 0;
            for (EmiScreenManager.ScreenSpace space : panel.getSpaces()) {
                totalHeight += space.th * ENTRY_SIZE + SUBPANEL_SEPARATOR_SIZE;
            }
            search.setY(panel.space.ty + totalHeight + 2 + ReliableEmiConfig.searchWidgetTopOffset);
        } else if (panel.side == SidebarSide.RIGHT) {
            search.setY(screen.height - 21 + ReliableEmiConfig.searchWidgetTopOffset);
        } else {
            search.setY(screen.height - 21 - 21 + ReliableEmiConfig.searchWidgetTopOffset);
        }
        return true;
    }

    @Inject(method = "repopulatePanels", at = @At("HEAD"))
    private static void remi$invalidateStackCache(SidebarType type, CallbackInfo ci) {
        StackManager.invalidateStacks();
    }

    @Inject(method = "recalculate", at = @At("HEAD"))
    private static void remi$updateWorkstationCraftables(CallbackInfo ci) {
        WorkstationSidebarManager.updateWorkstationCraftables();
        StackManager.repopulateIndexPanelsIfDirty();
    }

    @WrapOperation(
            method = "updateMouse",
            at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/EmiScreenManager$ScreenSpace;getType()Ldev/emi/emi/config/SidebarType;", ordinal = 0)
    )
    private static SidebarType remi$modifyHoveredSpaceType(EmiScreenManager.ScreenSpace instance, Operation<SidebarType> original) {
        SidebarType type = original.call(instance);
        if (WorkstationSidebarManager.WORKSTATION != null && type == WorkstationSidebarManager.WORKSTATION) {
            return SidebarType.CRAFTABLES;
        }
        return type;
    }

    @Redirect(method = "recalculate",
            at = @At(value = "FIELD", target = "Ldev/emi/emi/screen/EmiScreenManager;searchedStacks:Ljava/util/List;",
                    opcode = Opcodes.PUTSTATIC))
    private static void redirectStacksSourceToEmixx(List<? extends EmiIngredient> value) {
        EmiScreenManager.SidebarPanel searchPanel = remi$getEffectiveSearchPanel();
        if (searchPanel != null && searchPanel.getType() == SidebarType.INDEX) {
            searchedStacks = StackManager.displayedStacks;
        } else {
            searchedStacks = EmiSearch.stacks;
        }
    }

    @ModifyExpressionValue(method = "recalculate",
            at = @At(value = "FIELD", target = "Ldev/emi/emi/search/EmiSearch;stacks:Ljava/util/List;",
                    opcode = Opcodes.GETSTATIC))
    private static List<? extends EmiIngredient> redirectCachedStacksToEmixx(List<? extends EmiIngredient> original) {
        EmiScreenManager.SidebarPanel searchPanel = remi$getEffectiveSearchPanel();
        if (searchPanel != null && searchPanel.getType() == SidebarType.INDEX) {
            Layout.textureDirty = true;
            return StackManager.displayedStacks;
        }
        return original;
    }

    @Inject(method = "getSearchSource", at = @At(value = "RETURN"), cancellable = true)
    private static void redirectSearchSourceToEmixx(CallbackInfoReturnable<List<? extends EmiIngredient>> cir) {
        EmiScreenManager.SidebarPanel searchPanel = remi$getEffectiveSearchPanel();
        if (searchPanel != null && searchPanel.getType() == SidebarType.INDEX)
            cir.setReturnValue(StackManager.sourceStacks);
    }

    @Inject(at = @At("HEAD"), method = "addWidgets")
    private static void addEmiPlusPlusWidgets(Screen screen, CallbackInfo ci) {
        ScreenManager.onScreenInitialized(screen);
        if (search != null) {
            search.update();
        }
    }

    @Inject(at = @At("RETURN"), method = "mouseScrolled", cancellable = true)
    private static void mouseScrolled(double mouseX, double mouseY, double amount,
                                      CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValueZ() || ScreenManager.onMouseScrolled(mouseX, mouseY, amount));
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/stack/EmiIngredient;isEmpty()Z", ordinal = 0),
            method = "mouseReleased")
    private static boolean modifyMouseReleased(EmiIngredient instance, Operation<Boolean> original, @Local(name = "mouseX") double mouseX, @Local(name = "mouseY") double mouseY) {
        if (instance instanceof EmiGroupStack) {
            EmiScreenManager.ScreenSpace space = EmiScreenManager.getHoveredSpace((int) mouseX, (int) mouseY);
            SidebarType type = space != null ? space.getType() : SidebarType.INDEX;
            StackManager.onStackInteraction(instance, type);
        }
        return original.call(instance);
    }

    @WrapOperation(
            at = @At(value = "INVOKE", target = "Ldev/emi/emi/registry/EmiDragDropHandlers;dropStack(Lnet/minecraft/client/gui/screens/Screen;Ldev/emi/emi/api/stack/EmiIngredient;II)Z"),
            method = "mouseReleased"
    )
    private static boolean wrapDropStack(Screen screen, EmiIngredient stack, int x, int y, Operation<Boolean> original) {
        boolean handled = original.call(screen, stack, x, y);
        if (!handled && ReliableEmiConfig.dragCheatToInventory && EmiApi.isCheatMode()) {
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                handled = remi$giveDraggedToInventory(containerScreen, stack, x, y);
            }
        }
        return handled;
    }

    @Unique
    private static Slot remi$unwrapSlot(Slot slot) {
        if (slot instanceof SlotWrapperAccessor accessor) {
            return accessor.remi$getTarget();
        }
        return slot;
    }

    @Unique
    private static Slot remi$getSlotUnderMouse(AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        if (screen instanceof AbstractContainerScreenAccessor accessor) {
            Slot slot = accessor.remi$findSlot(mouseX, mouseY);
            if (slot != null) {
                return slot;
            }
            return accessor.remi$getHoveredSlot();
        }
        return null;
    }

    @Unique
    private static String remi$getCommandSlotName(int containerSlot) {
        if (containerSlot >= 0 && containerSlot <= 8) {
            return "hotbar." + containerSlot;
        } else if (containerSlot >= 9 && containerSlot <= 35) {
            return "inventory." + (containerSlot - 9);
        } else if (containerSlot == 36) {
            return "armor.feet";
        } else if (containerSlot == 37) {
            return "armor.legs";
        } else if (containerSlot == 38) {
            return "armor.chest";
        } else if (containerSlot == 39) {
            return "armor.head";
        } else if (containerSlot == 40) {
            return "weapon.offhand";
        }
        return null;
    }

    @Unique
    private static int remi$getCreativeSlotId(int containerSlot) {
        if (containerSlot >= 0 && containerSlot <= 8) {
            return containerSlot + 36;
        } else if (containerSlot >= 9 && containerSlot <= 35) {
            return containerSlot;
        } else if (containerSlot == 36) {
            return 8;
        } else if (containerSlot == 37) {
            return 7;
        } else if (containerSlot == 38) {
            return 6;
        } else if (containerSlot == 39) {
            return 5;
        } else if (containerSlot == 40) {
            return 45;
        }
        return -1;
    }

    @Unique
    private static boolean remi$giveDraggedToInventory(AbstractContainerScreen<?> screen, EmiIngredient ingredient, int x, int y) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return false;
        }
        if (!EmiApi.isCheatMode()) {
            return false;
        }
        if (!client.player.hasPermissions(2) && !client.player.isCreative()) {
            return false;
        }
        if (ingredient == null || ingredient.isEmpty()) {
            return false;
        }
        List<EmiStack> stacks = ingredient.getEmiStacks();
        if (stacks == null || stacks.isEmpty()) {
            return false;
        }
        EmiStack emiStack = stacks.getFirst();
        ItemStack itemStack = emiStack.getItemStack();
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }

        Slot targetSlot = remi$getSlotUnderMouse(screen, x, y);
        if (targetSlot == null) {
            return false;
        }

        Slot effectiveSlot = remi$unwrapSlot(targetSlot);
        if (effectiveSlot == null || effectiveSlot.container != client.player.getInventory()) {
            return false;
        }

        ItemStack toGive = itemStack.copy();
        ItemStack current = targetSlot.getItem();
        int amount = Screen.hasShiftDown() ? toGive.getMaxStackSize() : 1;
        if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, toGive) && !Screen.hasShiftDown()) {
            amount = Math.min(current.getCount() + 1, toGive.getMaxStackSize());
        }
        toGive.setCount(amount);

        if (!targetSlot.mayPlace(toGive) || !effectiveSlot.mayPlace(toGive)) {
            return false;
        }

        if (client.player.isCreative() && client.gameMode != null) {
            int creativeSlotId;
            if (effectiveSlot.index >= 0 && effectiveSlot.index < client.player.inventoryMenu.slots.size()
                    && client.player.inventoryMenu.slots.get(effectiveSlot.index) == effectiveSlot) {
                creativeSlotId = effectiveSlot.index;
            } else {
                creativeSlotId = remi$getCreativeSlotId(effectiveSlot.getContainerSlot());
            }

            if (creativeSlotId != -1) {
                targetSlot.setByPlayer(toGive);
                if (effectiveSlot != targetSlot) {
                    effectiveSlot.setByPlayer(toGive);
                }
                client.gameMode.handleCreativeModeItemAdd(toGive, creativeSlotId);
                return true;
            }
        } else {
            String slotName = remi$getCommandSlotName(effectiveSlot.getContainerSlot());
            if (slotName != null && client.level != null) {
                ItemInput argument = new ItemInput(toGive.getItemHolder(), toGive.getComponentsPatch());
                String command = "item replace entity @s " + slotName + " with " + argument.serialize(client.level.registryAccess()) + " " + amount;
                if (command.length() < 256) {
                    client.player.connection.sendUnsignedCommand(command);
                    return true;
                }
            }
        }
        return false;
    }

    @ModifyVariable(at = @At("HEAD"), method = "createScreenSpace", argsOnly = true)
    private static Bounds modifyEmixxBounds(Bounds bounds, @Local(ordinal = 0, argsOnly = true) EmiScreenManager.SidebarPanel panel) {
        if (ReliableEmiConfig.isCreativeTabsEnabled(panel.getType())) {
            EmiScreenManager.SidebarPanel targetPanel = ScreenManager.getTargetCreativeTabPanel();
            if (targetPanel == panel && CreativeModeTabGui.currentTheme() == CreativeModeTabGui.TabTheme.VERTICAL) {
                int tabSpace = 35;
                bounds = new Bounds(
                        bounds.x() + tabSpace,
                        bounds.y(),
                        Math.max(0, bounds.width() - tabSpace),
                        bounds.height()
                );
            }
        }
        return bounds;
    }

    @ModifyVariable(at = @At("HEAD"), method = "createScreenSpace", argsOnly = true)
    private static List<Bounds> remi$reserveBottomSpace(List<Bounds> exclusion,
                                                        @Local(ordinal = 0, argsOnly = true) EmiScreenManager.SidebarPanel panel,
                                                        @Local(ordinal = 0, argsOnly = true) Bounds bounds,
                                                        @Local(ordinal = 0, argsOnly = true) SidebarSettings settings) {
        int reserve = remi$getBottomReserve(panel, settings);
        if (reserve <= 0) {
            return exclusion;
        }
        List<Bounds> withReserve = new java.util.ArrayList<>(exclusion);
        withReserve.add(new Bounds(bounds.x(), bounds.bottom() - reserve, bounds.width(), reserve));
        return withReserve;
    }

    @ModifyVariable(method = "getHoveredStack(IIZZ)Ldev/emi/emi/api/stack/EmiStackInteraction;", at = @At(value = "STORE", ordinal = 1), name = "n")
    private static int addOffsetToHoveredStack(int n, @Local(name = "panel") EmiScreenManager.SidebarPanel panel) {
        if (ReliableEmiConfig.scrollInsteadOfPagination) {
            return n + ((SidebarPanelWithScrollOffset) panel).remi$getScrollOffset();
        } else {
            return n;
        }
    }

    @Inject(method = "addWidgets", at = @At("TAIL"))
    private static void searchWidgetVerticalAlign(Screen screen, CallbackInfo ci) {
        if (remi$applySearchWidgetLayout()) {
            return;
        }
        search.setY(search.getY() + ReliableEmiConfig.searchWidgetTopOffset);
        search.setX(search.getX() + ReliableEmiConfig.searchWidgetLeftOffset);
    }

    @Inject(method = "recalculate", at = @At("TAIL"))
    private static void remi$realignSearchWidget(CallbackInfo ci) {
        remi$applySearchWidgetLayout();
    }

    @Inject(method = "mouseClicked", at = @At("TAIL"), cancellable = true)
    private static void scrollbarMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        for (EmiScreenManager.SidebarPanel panel : panels) {
            SidebarPanelWithScrollOffset scrollPanel = (SidebarPanelWithScrollOffset) panel;
            if (scrollPanel.remi$getScrollbarWidget().mouseClicked(mouseX, mouseY, button)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "mouseReleased", at = @At("TAIL"))
    private static void scrollbarMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        for (EmiScreenManager.SidebarPanel panel : panels) {
            SidebarPanelWithScrollOffset scrollPanel = (SidebarPanelWithScrollOffset) panel;
            scrollPanel.remi$getScrollbarWidget().stopDragging();
        }
    }

    @Inject(method = "mouseDragged", at = @At("TAIL"), cancellable = true)
    private static void scrollbarMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        for (EmiScreenManager.SidebarPanel panel : panels) {
            SidebarPanelWithScrollOffset scrollPanel = (SidebarPanelWithScrollOffset) panel;
            if (scrollPanel.remi$getScrollbarWidget().mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                cir.setReturnValue(true);
            }
        }
    }

    @ModifyVariable(method = "createScreenSpace", at = @At(value = "STORE"), name = "xMax")
    private static int addScrollbarToXMax(int xMax, @Local(name = "panel") EmiScreenManager.SidebarPanel panel, @Local(name = "theme") SidebarTheme theme) {
        if (ReliableEmiConfig.isVerticalScrollbarEnabled()) {
            xMax -= ScrollbarWidget.WIDTH - theme.horizontalPadding;
        }
        return xMax;
    }
}
