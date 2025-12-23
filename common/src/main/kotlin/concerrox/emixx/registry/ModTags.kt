package concerrox.emixx.registry

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

object ModTags {

    object Item {

        val MUSIC_DISCS = common("music_discs")
        val ORES = common("ores")
        val GLASS_BLOCKS = common("glass_blocks")
        val GLASS_PANES = common("glass_panes")
        val SHULKER_BOXES = common("shulker_boxes")
        val CONCRETES = common("concretes")
        val CONCRETE_POWDERS = common("concrete_powders")
        val GLAZED_TERRACOTTAS = common("glazed_terracottas")
        val BUCKETS = common("buckets")
        val FOODS = common("foods")
        val SEEDS = common("seeds")
        val DYES = common("dyes")
        val RAW_MATERIALS = common("raw_materials")
        val DUSTS = common("dusts")
        val NUGGETS = common("nuggets")
        val INGOTS = common("ingots")

        private fun common(path: String): TagKey<net.minecraft.world.item.Item> {
            return TagKey.create(Registries.ITEM, ResourceLocation("c", path))
        }


    }

}