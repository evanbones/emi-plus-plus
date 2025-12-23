package concerrox.emixx.forge.mixin.mekanism;

import concerrox.emixx.gui.components.ImageButton;
import mekanism.client.jei.GuiElementHandler;
import net.minecraft.client.gui.components.AbstractWidget;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GuiElementHandler.class, remap = false)
public class GuiElementHandlerMixin {

    @Redirect(method = "getAreasFor",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/AbstractWidget;visible:Z",
                    opcode = Opcodes.GETFIELD))
    private static boolean skipEmiPlusPlusButtons(AbstractWidget instance) {
        if (instance instanceof ImageButton) return false;
        return instance.visible;
    }

}