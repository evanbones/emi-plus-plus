package concerrox.emixx.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import concerrox.emixx.content.StackManager;
import concerrox.emixx.content.stackgroup.StackGroupManager;
import dev.emi.emi.api.recipe.EmiIngredientRecipe;
import dev.emi.emi.api.widget.ButtonWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.recipe.EmiTagRecipe;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmiIngredientRecipe.class, remap = false)
public class EmiIngredientRecipeMixin {

    @Unique
    private static final ResourceLocation BUTTONS_TEXTURE = new ResourceLocation("emixx", "textures/gui/buttons.png");

    @Inject(method = "addWidgets", at = @At("TAIL"))
    public void addCreateStackGroupButton(WidgetHolder widgets, CallbackInfo ci) {
        if ((Object) this instanceof EmiTagRecipe emiTagRecipe) {

            ResourceLocation tagId = emiTagRecipe.key.location();

            widgets.add(new ButtonWidget(24, 0, 12, 12, 50, 0,
                    BUTTONS_TEXTURE,
                    () -> EmiConfig.devMode,
                    (mouseX, mouseY, button) -> {
                        StackGroupManager.INSTANCE.toggleTagGroup(tagId);
                        StackManager.INSTANCE.reload();
                    }
            ) {
                @Override
                public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
                    EmiDrawContext context = EmiDrawContext.wrap(draw);

                    boolean exists = StackGroupManager.INSTANCE.hasGroup(tagId);
                    boolean hovered = getBounds().contains(mouseX, mouseY);

                    int currentV = exists ? 24 : 0;
                    if (hovered && this.isActive.getAsBoolean()) {
                        currentV += 12;
                    }

                    RenderSystem.enableDepthTest();
                    context.drawTexture(texture, this.x, this.y, this.u, currentV, this.width, this.height);
                }
            });
        }
    }
}