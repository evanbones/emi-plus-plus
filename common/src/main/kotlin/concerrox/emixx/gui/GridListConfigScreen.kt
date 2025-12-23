package concerrox.emixx.gui

import concerrox.emixx.text
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents

abstract class GridListConfigScreen(val name: String) : Screen(text("gui", name)) {

    private lateinit var list: GridList<*>

    abstract fun createList(): GridList<*>
    abstract fun save()
    abstract fun reload()

    override fun init() {
        list = createList()
        list.updateSize(width, height, 32, height - 32)

        addWidget(list)

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE) {
            save()
            onClose()
            reload()
        }.bounds(this.width / 2 - 100, this.height - 27, 200, 20).build())

        list.add()
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics)
        list.render(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun repositionElements() {
        if (::list.isInitialized) {
            list.updateSize(width, height, 32, height - 32)
        }
    }
}