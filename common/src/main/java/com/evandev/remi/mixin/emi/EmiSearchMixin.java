package com.evandev.remi.mixin.emi;

import com.evandev.remi.config.ReliableEmiConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.searchtree.SuffixArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EmiSearch.class, remap = false)
public class EmiSearchMixin {

    @WrapOperation(
            method = "bake",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/searchtree/SuffixArray;add(Ljava/lang/Object;Ljava/lang/String;)V",
                    ordinal = 4,
                    remap = true
            )
    )
    private static void wrapAddIdPathToNames(SuffixArray instance, Object object, String contents, Operation<Void> original) {
        if (ReliableEmiConfig.searchById) {
            original.call(instance, object, contents);
        }
    }
}
