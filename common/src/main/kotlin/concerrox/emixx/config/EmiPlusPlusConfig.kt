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
        lateinit var enableBerryTheme: ForgeConfigSpec.BooleanValue
        lateinit var disabledCreativeModeTabs: ForgeConfigSpec.ConfigValue<List<String>>
        lateinit var enableStackGroups: ForgeConfigSpec.BooleanValue
    }

    init {
        builder.group("creativeModeTabs") {
            enableCreativeModeTabs = define("enableCreativeModeTabs", true)
            syncSelectedCreativeModeTab = define("syncSelectedCreativeModeTab", true)
            enableBerryTheme = define("enableBerryTheme", false)
            disabledCreativeModeTabs = defineListAllowEmpty(listOf("disabledCreativeModeTabs"), {
                listOf("minecraft:op_blocks")
            }, { it is String })
        }
        builder.group("stackGroups") {
            enableStackGroups = define("enableStackGroups", true)
        }
        builder.group("miscellaneous") {
        }
    }
}