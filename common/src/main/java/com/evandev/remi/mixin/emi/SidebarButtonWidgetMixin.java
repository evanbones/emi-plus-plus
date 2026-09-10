package com.evandev.remi.mixin.emi;

import com.evandev.ReliableEmi;
import com.evandev.remi.feature.workstation.WorkstationSidebarManager;
import com.evandev.remi.mixin.emi.accessor.SizedButtonWidgetAccessor;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.widget.SidebarButtonWidget;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SidebarButtonWidget.class, remap = false)
public abstract class SidebarButtonWidgetMixin {

    @Unique
    private static final ResourceLocation WORKSTATION_TEXTURE = ReliableEmi.res("textures/gui/workstation_icon.png");

    @Final
    @Shadow
    private EmiScreenManager.SidebarPanel panel;

    @Inject(method = "getU", at = @At("HEAD"), cancellable = true)
    private void remi$getWorkstationU(int mouseX, int mouseY, CallbackInfoReturnable<Integer> cir) {
        if (WorkstationSidebarManager.WORKSTATION != null && panel.getType() == WorkstationSidebarManager.WORKSTATION) {
            ((SizedButtonWidgetAccessor) this).remi$setTexture(WORKSTATION_TEXTURE);
            cir.setReturnValue(0);
        } else {
            ((SizedButtonWidgetAccessor) this).remi$setTexture(EmiRenderHelper.WIDGETS);
        }
    }

    @Inject(method = "getV", at = @At("HEAD"), cancellable = true)
    private void remi$getWorkstationV(int mouseX, int mouseY, CallbackInfoReturnable<Integer> cir) {
        if (WorkstationSidebarManager.WORKSTATION != null && panel.getType() == WorkstationSidebarManager.WORKSTATION) {
            SidebarButtonWidget widget = (SidebarButtonWidget) (Object) this;
            widget.active = !panel.pages.pages.isEmpty();
            if (widget.active && widget.isMouseOver(mouseX, mouseY)) {
                cir.setReturnValue(16);
            } else {
                cir.setReturnValue(0);
            }
        }
    }
}
