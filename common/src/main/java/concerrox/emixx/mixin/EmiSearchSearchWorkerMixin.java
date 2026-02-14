package concerrox.emixx.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import concerrox.emixx.content.StackManager;
import concerrox.emixx.content.stackgroup.StackGroupManager;
import concerrox.emixx.util.SearchWorkerBridge;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "dev.emi.emi.search.EmiSearch$SearchWorker", remap = false)
public class EmiSearchSearchWorkerMixin implements SearchWorkerBridge {

    @Final
    @Shadow
    private String query;

    @Override
    public String emixx$getQuery() {
        return this.query;
    }

    @WrapOperation(method = "run", at = @At(value = "INVOKE",
            target = "Ldev/emi/emi/search/EmiSearch;apply(Ldev/emi/emi/search/EmiSearch$SearchWorker;Ljava/util/List;)V"))
    private void run(@Coerce Object worker, List<? extends EmiIngredient> stacks, Operation<Void> original) {

        synchronized (EmiSearch.class) {
            if (EmiScreenManager.getSearchPanel().getType() == SidebarType.INDEX) {

                List<EmiStack> combinedStacks = new ArrayList<>();
                for (EmiIngredient stack : stacks) {
                    if (stack instanceof EmiStack emiStack) {
                        combinedStacks.add(emiStack);
                    }
                }

                String query = ((SearchWorkerBridge) worker).emixx$getQuery();

                if (query != null && query.startsWith("%")) {
                    String groupQuery = query.substring(1);
                    if (!groupQuery.isEmpty()) {
                        StackGroupManager.INSTANCE.appendStacksForMatchingGroups(groupQuery, combinedStacks);
                    }
                }

                StackManager.INSTANCE.buildStacks$emixx_common(combinedStacks, query);
            } else {
                original.call(worker, stacks);
            }
        }
    }
}