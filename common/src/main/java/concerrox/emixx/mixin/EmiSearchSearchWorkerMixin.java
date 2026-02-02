package concerrox.emixx.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import concerrox.emixx.content.StackManager;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

import java.util.List;

@Mixin(targets = "dev.emi.emi.search.EmiSearch$SearchWorker", remap = false)
public class EmiSearchSearchWorkerMixin {

    @WrapOperation(method = "run", at = @At(value = "INVOKE",
            target = "Ldev/emi/emi/search/EmiSearch;apply(Ldev/emi/emi/search/EmiSearch$SearchWorker;Ljava/util/List;)V"))
    private void run(@Coerce Object worker, List<? extends EmiIngredient> stacks, Operation<Void> original) {

        synchronized (EmiSearch.class) {
            if (EmiScreenManager.getSearchPanel().getType() == SidebarType.INDEX) {
                //noinspection unchecked: All the stacks in the index are EmiStack
                StackManager.INSTANCE.buildStacks$emixx_common((List<? extends EmiStack>) stacks);
            } else {
                original.call(worker, stacks);
            }
        }
    }
}