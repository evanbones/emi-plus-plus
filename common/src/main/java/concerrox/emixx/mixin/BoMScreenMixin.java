package concerrox.emixx.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.config.SidebarTheme;
import dev.emi.emi.screen.BoMScreen;
import dev.emi.emi.config.EmiConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BoMScreen.class, remap = false)
public class BoMScreenMixin extends Screen {

    protected BoMScreenMixin(Component title) {
        super(title);
    }

    @Inject(at = @At("HEAD"), method = "render", remap = true)
    private void render(GuiGraphics raw, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (EmiConfig.rightSidebarTheme == SidebarTheme.VANILLA) {
            raw.pose().pushPose();
            raw.pose().translate(0, 0, -200);
            this.renderBackground(raw);
            raw.pose().popPose();
        } else {
            this.renderBackground(raw);
        }
    }

    @WrapOperation(method = "render",
            at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/BoMScreen;renderDirtBackground(Lnet/minecraft/client/gui/GuiGraphics;)V"),
            remap = true)
    private void suppressDirtBackground(BoMScreen instance, GuiGraphics guiGraphics, Operation<Void> original) {
    }
}