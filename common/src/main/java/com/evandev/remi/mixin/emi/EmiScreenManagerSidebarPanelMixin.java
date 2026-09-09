package com.evandev.remi.mixin.emi;

import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.feature.creativemodetab.gui.CreativeModeTabGui;
import com.evandev.remi.gui.components.ScrollbarWidget;
import com.evandev.remi.integration.emi.ScreenManager;
import com.evandev.remi.util.SidebarPanelWithScrollOffset;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.config.SidebarPages;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.config.SidebarTheme;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.widget.SidebarButtonWidget;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BooleanSupplier;

import static com.evandev.remi.integration.emi.ScreenManager.ENTRY_SIZE;

@Mixin(value = EmiScreenManager.SidebarPanel.class, remap = false)
public abstract class EmiScreenManagerSidebarPanelMixin implements SidebarPanelWithScrollOffset {

    @Shadow
    public EmiScreenManager.ScreenSpace space;

    @Final
    @Shadow
    public SizedButtonWidget pageLeft, pageRight;

    @Final
    @Shadow
    public SidebarButtonWidget cycle;

    @Final
    @Shadow
    public SidebarPages pages;

    @Shadow
    public int page;

    @Shadow
    public SidebarTheme theme;

    @Unique
    private ScrollbarWidget remi$scrollbar;

    @Unique
    private int remi$scrollOffsetRows = 0;

    @ModifyVariable(method = "drawHeader", at = @At(value = "STORE"), name = "scrollLeft")
    private int modifyScrollX(int scrollLeft) {
        if (ReliableEmiConfig.scrollInsteadOfPagination) {
            return scrollLeft - 18;
        } else {
            return scrollLeft;
        }
    }

    @ModifyVariable(method = "drawHeader", at = @At(value = "STORE"), name = "scrollWidth")
    private int modifyScrollWidth(int scrollWidth) {
        if (ReliableEmiConfig.scrollInsteadOfPagination) {
            return scrollWidth + 36;
        } else {
            return scrollWidth;
        }
    }

    @Unique
    private static void remi$drawIncrementalScrollBar(EmiDrawContext context, int x, int y, int width, int height, int filled, int total, int color) {
        if (total <= 1) {
            return;
        }

        int fillWidth = (int) Math.round((double) width * Math.min(filled, total) / total);
        fillWidth = Math.max(fillWidth, Math.min(width, height));

        context.fill(x, y, fillWidth, height, color);
    }

    @Shadow
    public abstract SidebarType getType();

    @Shadow
    public abstract boolean supportsType(SidebarType type);

    public ScrollbarWidget remi$getScrollbarWidget() {
        return this.remi$scrollbar;
    }

    public int remi$getScrollOffset() {
        return this.remi$scrollOffsetRows * this.space.tw;
    }

    public void remi$setScrollOffset(int offset) {
        this.remi$scrollOffsetRows = Math.max(0, Math.min(offset, this.remi$getTotalScrollRows()));
        space.batcher.repopulate();
    }

    public int remi$getScrollOffsetRows() {
        return this.remi$scrollOffsetRows;
    }

    public int remi$getTotalScrollRows() {
        if (space.tw <= 0) {
            return 0;
        }
        return Math.max((space.getStacks().size() - 1) / space.tw + 1 - space.th, 0);
    }

    @WrapOperation(method = "drawHeader", at = @At(value = "INVOKE",
            target = "Ldev/emi/emi/EmiRenderHelper;getPageText(III)Lnet/minecraft/network/chat/Component;", remap = true))
    private Component replaceIndexHeader(int page, int total, int maxWidth, Operation<Component> original) {
        int availWidth = remi$getAvailableHeaderWidth();
        if (getType() == SidebarType.INDEX && ScreenManager.customIndexTitle != null) {
            return remi$truncateTitleToWidth(ScreenManager.customIndexTitle, availWidth);
        } else if (ReliableEmiConfig.showTitleInsteadOfPageNumbers) {
            return remi$truncateTitleToWidth(getType().getText(), availWidth);
        }

        return original.call(page, total, availWidth);
    }

    @Unique
    private int remi$getAvailableHeaderWidth() {
        if (this.space == null) return 0;
        int leftButtonsWidth = 0;
        int rightButtonsWidth = 0;

        if (ReliableEmiConfig.scrollInsteadOfPagination) {
            if (this.cycle.visible) {
                leftButtonsWidth += 18;
            }
        } else {
            if (this.pageLeft.visible) {
                leftButtonsWidth += 18;
            }
            if (this.cycle.visible) {
                leftButtonsWidth += 18;
            }
            if (this.pageRight.visible) {
                rightButtonsWidth += 18;
            }
        }

        int leftBound = space.tx + leftButtonsWidth;
        int rightBound = space.tx + space.tw * ScreenManager.ENTRY_SIZE - rightButtonsWidth;
        return Math.max(0, rightBound - leftBound);
    }

    @Unique
    private Component remi$truncateTitleToWidth(Component text, int maxWidth) {
        if (text == null) return Component.empty();
        if (maxWidth <= 0) return Component.empty();
        var font = Minecraft.getInstance().font;
        if (font.width(text) <= maxWidth) return text;
        int ellipsisWidth = font.width("...");
        if (maxWidth <= ellipsisWidth) return Component.literal(".");
        String plain = font.plainSubstrByWidth(text.getString(), maxWidth - ellipsisWidth);
        return Component.literal(plain + "...");
    }

    @Inject(at = @At("TAIL"), method = "setSpaces")
    private void addEmiPlusPlusWidgets(EmiScreenManager.ScreenSpace main, List<EmiScreenManager.ScreenSpace> subpanels, CallbackInfo ci) {
        if (supportsType(SidebarType.INDEX)) {
            ScreenManager.onIndexScreenSpaceCreated(main);
        }
        if (ReliableEmiConfig.enableCreativeModeTabs) {
            CreativeModeTabGui.onLayout();
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "dev/emi/emi/screen/EmiScreenManager$ScreenSpace.render (Ldev/emi/emi/runtime/EmiDrawContext;IIFI)V", ordinal = 0))
    private void addScrollOffsetToScreen(EmiScreenManager.ScreenSpace space, EmiDrawContext context, int mouseX, int mouseY, float delta, int startIndex, Operation<Void> original) {
        if (ReliableEmiConfig.scrollInsteadOfPagination) {
            original.call(space, context, mouseX, mouseY, delta, this.remi$getScrollOffset());
        } else {
            original.call(space, context, mouseX, mouseY, delta, startIndex);
        }
    }

    @Inject(method = "drawHeader", at = @At("HEAD"))
    private void drawVerticalScrollbar(EmiDrawContext context, int mouseX, int mouseY, float delta, int page, int totalPages, CallbackInfo ci) {
        if (!ReliableEmiConfig.isVerticalScrollbarEnabled()) {
            return;
        }
        this.remi$scrollbar.render(context.raw(), mouseX, mouseY, delta);
    }

    @WrapOperation(method = "drawHeader", at = @At(value = "INVOKE", target = "Ldev/emi/emi/EmiRenderHelper;drawScroll(Ldev/emi/emi/runtime/EmiDrawContext;IIIIIII)V"))
    private void drawScrollBar(EmiDrawContext context, int x, int y, int width, int height, int progress, int total, int color, Operation<Void> original) {
        if (ReliableEmiConfig.scrollInsteadOfPagination) {
            if (ReliableEmiConfig.isVerticalScrollbarEnabled()) {
                return;
            }
            progress = this.remi$getScrollOffsetRows();
            int totalScrollRows = this.remi$getTotalScrollRows();
            total = totalScrollRows + this.space.th;

            if (ReliableEmiConfig.incrementalScrollbarFill) {
                remi$drawIncrementalScrollBar(context, x, y, width, height, progress + 1, totalScrollRows + 1, color);
            } else {
                double segment = (double) width / total;
                int start = (int) (x + segment * progress);
                int end = (int) (start + segment * this.space.th);

                if (progress == this.remi$getTotalScrollRows()) {
                    end = x + width;
                    start = (int) (end - Math.max(segment * this.space.th, 1));
                }

                context.fill(start, y, end - start, height, color);
            }
        } else if (ReliableEmiConfig.incrementalScrollbarFill) {
            remi$drawIncrementalScrollBar(context, x, y, width, height, progress + 1, total, color);
        } else {
            original.call(context, x, y, width, height, progress, total, color);
        }
    }

    @ModifyVariable(method = "drawHeader", at = @At(value = "STORE"), name = "maxLeft")
    private int modifyMaxLeft(int maxLeft) {
        return Math.max(0, maxLeft);
    }

    @ModifyVariable(method = "drawHeader", at = @At(value = "STORE"), name = "x")
    private int centerHeaderX(int x) {
        if (this.space == null) return x;

        int leftButtonsWidth = 0;
        int rightButtonsWidth = 0;

        if (ReliableEmiConfig.scrollInsteadOfPagination) {
            if (this.cycle.visible) {
                leftButtonsWidth += 18;
            }
        } else {
            if (this.pageLeft.visible) {
                leftButtonsWidth += 18;
            }
            if (this.cycle.visible) {
                leftButtonsWidth += 18;
            }
            if (this.pageRight.visible) {
                rightButtonsWidth += 18;
            }
        }

        int leftBound = space.tx + leftButtonsWidth;
        int rightBound = space.tx + space.tw * ScreenManager.ENTRY_SIZE - rightButtonsWidth;
        return (leftBound + rightBound) / 2;
    }

    @WrapOperation(method = "drawHeader", at = @At(value = "INVOKE",
            target = "Ldev/emi/emi/runtime/EmiDrawContext;drawCenteredText(Lnet/minecraft/network/chat/Component;II)V", remap = true))
    private void verticalCenterHeaderText(EmiDrawContext instance, Component text, int x, int y, Operation<Void> original) {
        if (ReliableEmiConfig.isVerticalScrollbarEnabled()) {
            y += 1;
        }
        original.call(instance, text, x, y);
    }

    @WrapOperation(method = "drawHeader", at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiDrawContext;fill(IIIII)V"))
    private void hideHorizontalScrollbar(EmiDrawContext instance, int x, int y, int width, int height, int color, Operation<Void> original) {
        if (!ReliableEmiConfig.isVerticalScrollbarEnabled()) {
            original.call(instance, x, y, width, height, color);
        }
    }

    @Inject(method = "wrapPage", at = @At("HEAD"), cancellable = true)
    public void clampScrollOffset(CallbackInfo ci) {
        if (ReliableEmiConfig.scrollInsteadOfPagination) {
            ci.cancel();
            int clamped = Math.max(0, Math.min(remi$scrollOffsetRows, this.remi$getTotalScrollRows()));
            if (clamped != remi$scrollOffsetRows) {
                remi$scrollOffsetRows = clamped;
                space.batcher.repopulate();
            }
        } else if (ReliableEmiConfig.disablePaginationWrapping) {
            ci.cancel();
            int totalPages = (space.getStacks().size() - 1) / space.pageSize + 1;
            int clamped = Math.max(0, Math.min(this.page, totalPages - 1));
            if (clamped != this.page) {
                this.page = clamped;
                space.batcher.repopulate();
            }
        }
    }

    @Inject(method = "updateWidgetVisibility", at = @At("TAIL"))
    public void hideButtons(CallbackInfo ci) {
        if (ReliableEmiConfig.scrollInsteadOfPagination) {
            pageLeft.visible = false;
            pageRight.visible = false;
        }
        if (ReliableEmiConfig.hidePageButtonWhenOnePage && this.pages.pages.size() == 1) {
            cycle.visible = false;
        }

        boolean enabled = ReliableEmiConfig.isVerticalScrollbarEnabled() && this.space != null;
        this.remi$scrollbar.active = enabled && this.remi$getTotalScrollRows() > 0;
    }

    @Inject(method = "updateWidgetPosition", at = @At("HEAD"))
    public void updateScrollbarPosition(CallbackInfo ci) {
        boolean enabled = ReliableEmiConfig.isVerticalScrollbarEnabled() && this.space != null;
        if (enabled) {
            int x = this.space.tx + this.space.tw * ENTRY_SIZE;
            int y = this.space.ty;
            int height = this.space.th * ENTRY_SIZE;

            this.remi$scrollbar.setX(x);
            this.remi$scrollbar.setY(y);
            this.remi$scrollbar.setWidth(ScrollbarWidget.WIDTH);
            this.remi$scrollbar.setHeight(height);
        }
    }

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "dev/emi/emi/screen/widget/SizedButtonWidget", ordinal = 0))
    private SizedButtonWidget pageLeftButton(int x, int y, int width, int height, int u, int v, BooleanSupplier isActive, Button.OnPress action, Operation<SizedButtonWidget> original) {
        BooleanSupplier hasPrevPage = () -> {
            if (ReliableEmiConfig.disablePaginationWrapping) {
                return isActive.getAsBoolean() && this.page > 0;
            } else {
                return isActive.getAsBoolean();
            }
        };
        return original.call(x, y, width, height, u, v, hasPrevPage, action);
    }

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "dev/emi/emi/screen/widget/SizedButtonWidget", ordinal = 1))
    private SizedButtonWidget pageRightButton(int x, int y, int width, int height, int u, int v, BooleanSupplier isActive, Button.OnPress action, Operation<SizedButtonWidget> original) {
        BooleanSupplier hasNextPage = () -> {
            if (ReliableEmiConfig.disablePaginationWrapping) {
                int totalPages = (this.space.getStacks().size() - 1) / this.space.pageSize;
                return isActive.getAsBoolean() && this.page < totalPages;
            } else {
                return isActive.getAsBoolean();
            }
        };
        return original.call(x, y, width, height, u, v, hasNextPage, action);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void addScrollbar(SidebarSide side, SidebarPages pages, CallbackInfo ci) {
        this.remi$scrollbar = new ScrollbarWidget((EmiScreenManager.SidebarPanel) (Object) this);
    }

    @Inject(method = "updateWidgetPosition", at = @At("TAIL"))
    public void moveCycleButton(CallbackInfo ci) {
        if (ReliableEmiConfig.scrollInsteadOfPagination) {
            cycle.setX(space.tx);
        }
        if (ReliableEmiConfig.isVerticalScrollbarEnabled()) {
            cycle.setY(cycle.getY() + 1);
        }
    }

    @Inject(method = "scroll", at = @At("HEAD"), cancellable = true)
    public void scroll(int delta, CallbackInfo ci) {
        if (ReliableEmiConfig.scrollInsteadOfPagination) {
            ci.cancel();

            if (this.space == null) {
                return;
            }

            if (this.space.pageSize == 0) {
                return;
            }

            if (this.remi$getTotalScrollRows() == 0) {
                return;
            }

            remi$scrollOffsetRows += delta;

            if (remi$scrollOffsetRows >= this.remi$getTotalScrollRows()) {
                remi$scrollOffsetRows = this.remi$getTotalScrollRows();
            } else if (remi$scrollOffsetRows <= 0) {
                remi$scrollOffsetRows = 0;
            }

            space.batcher.repopulate();
        } else if (ReliableEmiConfig.disablePaginationWrapping) {
            ci.cancel();

            if (this.space == null) {
                return;
            }
            if (space.pageSize == 0) {
                return;
            }
            page += delta;
            int pageSize = space.pageSize;
            int totalPages = (space.getStacks().size() - 1) / pageSize + 1;
            if (totalPages <= 1) {
                return;
            }
            if (page >= totalPages) {
                page = totalPages - 1;
            } else if (page < 0) {
                page = 0;
            }
            space.batcher.repopulate();
        }
    }

    @WrapOperation(method = "getBounds", at = @At(value = "NEW", target = "(IIII)Ldev/emi/emi/api/widget/Bounds;"))
    private Bounds addScrollbarToBounds(int x, int y, int width, int height, Operation<Bounds> original) {
        if (ReliableEmiConfig.isVerticalScrollbarEnabled()) {
            width += ScrollbarWidget.WIDTH - theme.horizontalPadding;
        }
        return original.call(x, y, width, height);
    }

    @ModifyVariable(method = "drawBackground", at = @At(value = "STORE", ordinal = 0), name = "totalHeight")
    private int fixSeperatorSpacingAlwaysAddedToHeight(int totalHeight) {
        return totalHeight - 3;
    }
}
