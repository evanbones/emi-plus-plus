package concerrox.emixx.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.screens.Screen

abstract class GridList<Contents>(
    private val screen: Screen
) : ContainerObjectSelectionList<GridList.TripleEntry<Contents>>(
    Minecraft.getInstance(), screen.width, screen.height, 32, screen.height - 32, TripleEntry.HEIGHT
) {

    init {
        centerListVertically = false
        setRenderHeader(true, 16)
    }

    override fun getRowWidth() = TripleEntry.WIDTH

    override fun getScrollbarPosition(): Int {
        return this.width - 6
    }

    abstract fun getContents(): Collection<Contents>

    abstract fun getEntryForContent(content: Contents?, triple: TripleEntry<Contents>): ListEntry

    fun add() {
        getContents().chunked(3).forEach { triple ->
            addEntry(TripleEntry(this, triple))
        }
    }

    class TripleEntry<Contents>(
        val listWidget: GridList<Contents>,
        contentsList: List<Contents>
    ) : Entry<TripleEntry<Contents>>() {

        companion object {
            const val GUTTER = 6
            const val WIDTH = ListEntry.WIDTH * 3 + GUTTER * 2
            const val HEIGHT = ListEntry.HEIGHT + GUTTER * 2
        }

        private val children = mutableListOf<ListEntry>().apply {
            for (i in 0..2) {
                contentsList.getOrNull(i)?.let {
                    add(listWidget.getEntryForContent(it, this@TripleEntry))
                }
            }
        }

        override fun render(
            guiGraphics: GuiGraphics,
            index: Int,
            top: Int,
            left: Int,
            width: Int,
            height: Int,
            mouseX: Int,
            mouseY: Int,
            isHovered: Boolean,
            partialTick: Float
        ) {
            var xOffset = 0
            val startX = (listWidget.screen.width - WIDTH) / 2
            for (abstractWidget in children) {
                abstractWidget.setPosition(startX + xOffset, top)
                abstractWidget.render(guiGraphics, mouseX, mouseY, partialTick)
                xOffset += ListEntry.WIDTH + GUTTER * 2
            }
        }

        override fun children() = children
        override fun narratables() = children
    }
}