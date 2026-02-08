package concerrox.emixx.content

import concerrox.emixx.content.ScreenManager.ENTRY_SIZE
import concerrox.emixx.content.stackgroup.GroupedEmiStack
import dev.emi.emi.runtime.EmiDrawContext
import dev.emi.emi.screen.EmiScreenManager

object Layout {

    internal data class Tile(val x: Int, val y: Int, var type: Int) {
        internal fun check(bit: TileType) = this.type and bit.bit == bit.bit
    }

    internal enum class TileType(val bit: Int) {
        LEFT(1), TOP(2), RIGHT(4), BOTTOM(8), TOP_LEFT(16), TOP_RIGHT(32), BOTTOM_LEFT(64), BOTTOM_RIGHT(128)
    }

    var startIndex = -10
    var isClean = true

    var isTextureDirty = true
        set(value) {
            field = value
            val space = ScreenManager.indexScreenSpace
            if (space != null && space.th > 0) {
                StackManager.stackGrid = Array(space.th) { arrayOfNulls(space.tw) }
            }
        }

    // TODO: refactor this
    fun buildLayoutTiles(screenSpace: EmiScreenManager.ScreenSpace, context: EmiDrawContext) {
        if (isTextureDirty) {
            StackManager.stackTextureGrid.clear()
            for (y in 0 until screenSpace.th) {
                for (x in 0 until screenSpace.tw) {
                    val emiStack = StackManager.stackGrid.getOrNull(y)?.getOrNull(x)
                    if (emiStack == null || emiStack !is GroupedEmiStack<*>) continue

                    val tile = Tile(x, y, 0)
                    if (y == 0 || at(y - 1, x)?.stackGroup != emiStack.stackGroup) {
                        tile.type = tile.type or TileType.TOP.bit
                    }
                    if (x == 0 || at(y, x - 1)?.stackGroup != emiStack.stackGroup) {
                        tile.type = tile.type or TileType.LEFT.bit
                    }
                    if (y == screenSpace.th - 1 || at(y + 1, x)?.stackGroup != emiStack.stackGroup) {
                        tile.type = tile.type or TileType.BOTTOM.bit
                    }
                    if (x == screenSpace.tw - 1 || at(y, x + 1)?.stackGroup != emiStack.stackGroup) {
                        tile.type = tile.type or TileType.RIGHT.bit
                    }

                    if (at(y - 1, x - 1)?.stackGroup != emiStack.stackGroup
                        && at(y - 1, x)?.stackGroup == emiStack.stackGroup
                        && at(y, x - 1)?.stackGroup == emiStack.stackGroup
                    ) {
                        tile.type = tile.type or TileType.TOP_LEFT.bit
                    }
                    if (at(y - 1, x + 1)?.stackGroup != emiStack.stackGroup
                        && at(y - 1, x)?.stackGroup == emiStack.stackGroup
                        && at(y, x + 1)?.stackGroup == emiStack.stackGroup
                    ) {
                        tile.type = tile.type or TileType.TOP_RIGHT.bit
                    }
                    if (at(y + 1, x - 1)?.stackGroup != emiStack.stackGroup
                        && at(y + 1, x)?.stackGroup == emiStack.stackGroup
                        && at(y, x - 1)?.stackGroup == emiStack.stackGroup
                    ) {
                        tile.type = tile.type or TileType.BOTTOM_LEFT.bit
                    }
                    if (at(y + 1, x + 1)?.stackGroup != emiStack.stackGroup
                        && at(y + 1, x)?.stackGroup == emiStack.stackGroup
                        && at(y, x + 1)?.stackGroup == emiStack.stackGroup
                    ) {
                        tile.type = tile.type or TileType.BOTTOM_RIGHT.bit
                    }
                    if (tile.type != 0) StackManager.stackTextureGrid.add(tile)
                }
            }
        }
        isTextureDirty = false
        render(screenSpace, context)
    }

    fun render(screenSpace: EmiScreenManager.ScreenSpace, context: EmiDrawContext) {
        StackManager.stackTextureGrid.forEach {
            val px = screenSpace.tx + it.x * ENTRY_SIZE
            val py = screenSpace.ty + it.y * ENTRY_SIZE
            var ret = false
            if (it.check(TileType.TOP)) {
                context.fill(px, py, ENTRY_SIZE, 1, 0x66FFFFFF)
                ret = true
            }
            if (it.check(TileType.LEFT)) {
                context.fill(px, py, 1, ENTRY_SIZE, 0x66FFFFFF)
                ret = true
            }
            if (it.check(TileType.BOTTOM)) {
                context.fill(px, py + 17, ENTRY_SIZE, 1, 0x66FFFFFF)
                ret = true
            }
            if (it.check(TileType.RIGHT)) {
                context.fill(px + 17, py, 1, ENTRY_SIZE, 0x66FFFFFF)
                ret = true
            }

            if (it.check(TileType.TOP_LEFT)) {
                context.fill(px, py, 1, 1, 0x66FFFFFF)
            }
            if (it.check(TileType.TOP_RIGHT)) {
                context.fill(px + ENTRY_SIZE - 1, py, 1, 1, 0x66FFFFFF)
            }
            if (it.check(TileType.BOTTOM_LEFT)) {
                context.fill(px, py + ENTRY_SIZE - 1, 1, 1, 0x66FFFFFF)
            }
            if (it.check(TileType.BOTTOM_RIGHT)) {
                context.fill(px + ENTRY_SIZE - 1, py + ENTRY_SIZE - 1, 1, 1, 0x66FFFFFF)
            }
        }
    }

    private fun at(y: Int, x: Int): GroupedEmiStack<*>? {
        return StackManager.stackGrid.getOrNull(y)?.getOrNull(x) as? GroupedEmiStack<*>
    }

}