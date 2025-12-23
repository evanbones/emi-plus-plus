package concerrox.emixx.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import concerrox.emixx.content.Layout;
import concerrox.emixx.content.StackManager;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiHidden;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.StackBatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

import static concerrox.emixx.content.ScreenManager.ENTRY_SIZE;

@Mixin(value = EmiScreenManager.ScreenSpace.class, remap = false)
public abstract class EmiScreenManagerScreenSpaceMixin {

    @Shadow
    @Final
    public int pageSize;

    @Shadow
    @Final
    public StackBatcher batcher;

    @Shadow
    public abstract List<? extends EmiIngredient> getStacks();

    @Shadow
    public abstract int getRawOffsetFromMouse(int mouseX, int mouseY);

    @Shadow
    public abstract int getRawX(int off);

    @Shadow
    public abstract int getRawY(int off);

    @Shadow
    @Final
    public int th;

    @Shadow
    public abstract int getWidth(int y);

    @Shadow
    public abstract int getX(int x, int y);

    @Shadow
    public abstract int getY(int x, int y);

    @Shadow
    public abstract SidebarType getType();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void createGrid(int tx, int ty, int tw, int th, boolean rtl, List<Bounds> exclusion,
                            Supplier<SidebarType> typeSupplier, boolean search, CallbackInfo ci) {
        if (getType() == SidebarType.INDEX)
            StackManager.INSTANCE.setStackGrid$emixx_common(new EmiStack[th + 9][tw + 9]);
    }

    /**
     * @author ConcerroX
     * @reason I could not figure out how to capture the coordinates without @Overwrite.
     */
    @Overwrite
    public void render(EmiDrawContext context, int mouseX, int mouseY, float delta, int startIndex) {
        if (startIndex != Layout.INSTANCE.getStartIndex()) {
            Layout.INSTANCE.setStartIndex(startIndex);
            Layout.INSTANCE.setTextureDirty(true);
            Layout.INSTANCE.setClean(false);
        }

        if (pageSize > 0) {
            RenderSystem.enableDepthTest();
            EmiPort.setPositionTexShader();
            context.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            int hx, hy;
            batcher.begin(0, 0, 0);
            int i = startIndex;
            List<? extends EmiIngredient> stacks = getStacks();
            int hovered = getRawOffsetFromMouse(mouseX, mouseY);
            if (hovered != -1 && EmiConfig.showHoverOverlay && startIndex + hovered < stacks.size()) {
                hx = getRawX(hovered);
                hy = getRawY(hovered);
                EmiRenderHelper.drawSlotHightlight(context, hx, hy, ENTRY_SIZE, ENTRY_SIZE);
            }
            context.push();
            outer:
            for (int yo = 0; yo < th; yo++) {
                for (int xo = 0; xo < getWidth(yo); xo++) {
                    if (i >= stacks.size()) {
                        break outer;
                    }
                    int cx = getX(xo, yo);
                    int cy = getY(xo, yo);
                    EmiIngredient stack = stacks.get(i++);

                    if (getType() == SidebarType.INDEX && !Layout.INSTANCE.isClean())
                        StackManager.INSTANCE.getStackGrid$emixx_common()[yo][xo] = (EmiStack) stack;

                    batcher.render(stack, context.raw(), cx + 1, cy + 1, delta);
                    if (getType() == SidebarType.INDEX) {
                        if (EmiConfig.editMode && EmiHidden.isHidden(stack)) {
                            RenderSystem.enableDepthTest();
                            context.fill(cx, cy, ENTRY_SIZE, ENTRY_SIZE, 0x33ff0000);
                        } else if (EmiConfig.highlightDefaulted && BoM.getRecipe(stack) != null) {
                            RenderSystem.enableDepthTest();
                            context.fill(cx, cy, ENTRY_SIZE, ENTRY_SIZE, 0x3300ff00);
                        }
                    }
                }
            }
            batcher.draw();
            context.pop();
            if (getType() == SidebarType.INDEX)
                Layout.INSTANCE.buildLayoutTiles(EmiScreenManager.ScreenSpace.class.cast(this), context);
        }
    }

}