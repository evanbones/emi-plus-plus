package concerrox.emixx.config

import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.core.file.CommentedFileConfig
import concerrox.emixx.EmiPlusPlus
import net.neoforged.fml.config.IConfigSpec
import net.neoforged.neoforge.common.ModConfigSpec
import kotlin.io.path.div

private fun ModConfigSpec.Builder.group(path: String, action: ModConfigSpec.Builder.() -> Unit) = apply {
    push(path)
    action(this)
    pop()
}

class EmiPlusPlusConfig(builder: ModConfigSpec.Builder) {
    companion object {
        private val CONFIG_PAIR = ModConfigSpec.Builder().configure(::EmiPlusPlusConfig)
        val CONFIG_SPEC: ModConfigSpec = CONFIG_PAIR.right
        val CONFIG_DIRECTORY_PATH = EmiPlusPlus.PLATFORM.configDirectoryPath / EmiPlusPlus.MOD_ID

        fun save() {
            CONFIG_SPEC.save()
        }

        fun ensureLoaded() {
            if (!CONFIG_SPEC.isLoaded) {
                val configFile = CONFIG_DIRECTORY_PATH / "${EmiPlusPlus.MOD_ID}-common.toml"
                val configData = CommentedFileConfig.builder(configFile)
                    .sync()
                    .preserveInsertionOrder()
                    .autosave()
                    .build()
                configData.load()
                CONFIG_SPEC.acceptConfig(object : IConfigSpec.ILoadedConfig {
                    override fun config(): CommentedConfig = configData
                    override fun save() = configData.save()
                })
            }
        }

        lateinit var enableCreativeModeTabs: ModConfigSpec.BooleanValue
        lateinit var syncSelectedCreativeModeTab: ModConfigSpec.BooleanValue
        lateinit var showCreativeTabNameInSearchbar: ModConfigSpec.BooleanValue
        lateinit var disabledCreativeModeTabs: ModConfigSpec.ConfigValue<List<String>>
        lateinit var emiOnlyInRecipeBook: ModConfigSpec.BooleanValue
        lateinit var enableStackGroups: ModConfigSpec.BooleanValue
        lateinit var enableCreateStackGroupButton: ModConfigSpec.BooleanValue
    }

    init {
        builder.group("creativeModeTabs") {
            enableCreativeModeTabs = define("enableCreativeModeTabs", true)
            syncSelectedCreativeModeTab = define("syncSelectedCreativeModeTab", true)
            showCreativeTabNameInSearchbar = define("showCreativeTabNameInSearchbar", true)
            disabledCreativeModeTabs = defineListAllowEmpty(listOf("disabledCreativeModeTabs"), {
                listOf("minecraft:op_blocks")
            }, { it is String })
        }

        builder.group("stackGroups") {
            enableStackGroups = define("enableStackGroups", true)
            enableCreateStackGroupButton = define("enableCreateStackGroupButton", true)
        }

        builder.group("miscellaneous") {
            emiOnlyInRecipeBook = define("emiOnlyInRecipeBook", false)
        }
    }
}