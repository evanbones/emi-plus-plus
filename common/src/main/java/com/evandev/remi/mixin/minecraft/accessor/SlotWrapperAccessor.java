package com.evandev.remi.mixin.minecraft.accessor;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$SlotWrapper")
public interface SlotWrapperAccessor {
    @Accessor("target")
    Slot remi$getTarget();
}
