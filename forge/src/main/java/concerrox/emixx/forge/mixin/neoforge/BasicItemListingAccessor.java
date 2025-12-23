package concerrox.emixx.forge.mixin.neoforge;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.BasicItemListing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BasicItemListing.class)
public interface BasicItemListingAccessor {

    @Accessor("price")
    ItemStack getPrice();

    @Accessor("price2")
    ItemStack getPrice2();

    @Accessor("forSale")
    ItemStack getForSale();

}