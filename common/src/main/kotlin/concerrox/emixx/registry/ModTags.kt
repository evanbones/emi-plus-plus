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

        val MEKANISM_UNITS = mekanism("unit")
        val MEKANISM_DIRTY_DUSTS = mekanism("dirty_dusts")
        val MEKANISM_CLUMPS = mekanism("clumps")
        val MEKANISM_CRYSTALS = mekanism("crystals")
        val MEKANISM_ENRICHED = mekanism("enriched")
        val MEKANISM_SHARDS = mekanism("shards")

        private fun common(path: String): TagKey<net.minecraft.world.item.Item> {
            return TagKey.create(Registries.ITEM, ResourceLocation("c", path))
        }

        private fun mekanism(path: String): TagKey<net.minecraft.world.item.Item> {
            return TagKey.create(Registries.ITEM, ResourceLocation("mekanism", path))
        }


    }

}