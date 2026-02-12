package concerrox.emixx.content

import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.creativemodetab.CreativeModeTabManager
import concerrox.emixx.content.creativemodetab.gui.CreativeModeTabGui
import dev.emi.emi.screen.EmiScreenManager
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

object ScreenManager {

    const val ENTRY_SIZE = 18

    private var screen: Screen? = null
    var indexScreenSpace: EmiScreenManager.ScreenSpace? = null

    internal val isSearching
        get() = indexScreenSpace?.search == true && EmiScreenManager.search.value.isNotEmpty()

    private val isCreativeModeTabEnabled
        get() = EmiPlusPlusConfig.enableCreativeModeTabs.get()

    internal var customIndexTitle: Component? = null

    fun onScreenInitialized(screen: Screen) {
        this.screen = screen
    }

    fun onIndexScreenSpaceCreated(indexScreenSpace: EmiScreenManager.ScreenSpace) {
        this.indexScreenSpace = indexScreenSpace

        val currentScreen = screen ?: return

        if (isCreativeModeTabEnabled) {
            CreativeModeTabGui.initialize(currentScreen)
            CreativeModeTabManager.initialize()
        }
    }

    fun onMouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        return if (isCreativeModeTabEnabled && indexScreenSpace != null && CreativeModeTabGui.contains(
                mouseX,
                mouseY
            )
        ) {
            CreativeModeTabGui.onMouseScrolled(amount)
        } else false
    }

    fun removeCustomIndexTitle(component: Component?) {
        if (customIndexTitle == component) customIndexTitle = null
    }
}