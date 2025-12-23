package concerrox.emixx.config

import com.electronwill.nightconfig.core.file.CommentedFileConfig
import concerrox.emixx.EmiPlusPlus
import net.minecraftforge.common.ForgeConfigSpec
import kotlin.io.path.div

private fun ForgeConfigSpec.Builder.group(path: String, action: ForgeConfigSpec.Builder.() -> Unit) = apply {
    push(path)
    action(this)
    pop()
}

class EmiPlusPlusConfig(builder: ForgeConfigSpec.Builder) {

    companion object {
        private val CONFIG_PAIR = ForgeConfigSpec.Builder().configure(::EmiPlusPlusConfig)
        val CONFIG_SPEC: ForgeConfigSpec = CONFIG_PAIR.right
        val CONFIG_DIRECTORY_PATH = EmiPlusPlus.PLATFORM.configDirectoryPath / EmiPlusPlus.MOD_ID

        fun save() {
            CONFIG_SPEC.save()
        }

        fun ensureLoaded() {
            if (!CONFIG_SPEC.isLoaded) {
                val configFile = CONFIG_DIRECTORY_PATH / "${EmiPlusPlus.MOD_ID}-common.toml"
                val configData = CommentedFileConfig.builder(configFile).sync().build()
                configData.load()
                CONFIG_SPEC.setConfig(configData)
            }
        }

        lateinit var enableCreativeModeTabs: ForgeConfigSpec.BooleanValue
        lateinit var syncSelectedCreativeModeTab: ForgeConfigSpec.BooleanValue
        lateinit var disabledCreativeModeTabs: ForgeConfigSpec.ConfigValue<List<String>>
        lateinit var enableStackGroups: ForgeConfigSpec.BooleanValue
        lateinit var disabledStackGroups: ForgeConfigSpec.ConfigValue<List<String>>
    }

    init {
        builder.group("creativeModeTabs") {
            enableCreativeModeTabs = define("enableCreativeModeTabs", true)
            syncSelectedCreativeModeTab = define("syncSelectedCreativeModeTab", true)
            disabledCreativeModeTabs = defineListAllowEmpty(listOf("disabledCreativeModeTabs"), {
                listOf("minecraft:op_blocks")
            }, { it is String })
        }
        builder.group("stackGroups") {
            enableStackGroups = define("enableStackGroups", true)
            disabledStackGroups = defineListAllowEmpty(listOf("disabledStackGroups"), {
                listOf(
                    "minecraft:shovels",
                    "minecraft:pickaxes",
                    "minecraft:axes",
                    "minecraft:head_armor",
                    "minecraft:hoes",
                    "minecraft:swords",
                    "minecraft:chest_armor",
                    "minecraft:leg_armor",
                    "minecraft:foot_armor",
                    "c:raw_materials",
                    "minecraft:infested_blocks",
                    "minecraft:animal_armors",
                    "c:foods",
                    "minecraft:slabs",
                    "minecraft:doors",
                    "minecraft:trapdoors",
                    "minecraft:fences",
                    "minecraft:planks",
                    "minecraft:stairs",
                    "minecraft:fence_gates",
                    "minecraft:pressure_plates",
                    "minecraft:rails",
                    "minecraft:saplings",
                    "minecraft:buttons",
                    "minecraft:skulls",
                    "minecraft:minecarts",
                    "c:dyes",
                    "c:ores",
                    "minecraft:leaves",
                    "minecraft:signs",
                    "c:seeds",
                    "minecraft:logs",
                    "minecraft:hanging_signs",
                    "c:glass_blocks",
                    "minecraft:flowers",
                    "minecraft:wool",
                    "minecraft:walls",
                    "minecraft:boats",
                    "c:glass_panes",
                    "minecraft:terracotta",
                    "minecraft:banners",
                    "minecraft:wool_carpets",
                    "c:shulker_boxes",
                    "c:glazed_terracottas",
                    "c:concrete_powders",
                    "c:concretes",
                    "minecraft:beds",
                    "minecraft:candles",
                    "c:buckets",
                    "minecraft:copper_blocks"
                )
            }, { it is String })
        }
        builder.group("miscellaneous") {
        }
    }
}