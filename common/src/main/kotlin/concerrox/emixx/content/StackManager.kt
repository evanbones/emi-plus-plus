package concerrox.emixx.content

import concerrox.emixx.content.stackgroup.EmiGroupStack
import concerrox.emixx.content.stackgroup.StackGroupManager
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.config.EmiConfig
import dev.emi.emi.registry.EmiStackList
import dev.emi.emi.runtime.EmiHidden
import dev.emi.emi.screen.EmiScreenManager
import dev.emi.emi.search.EmiSearch

object StackManager {

    /**
     * Just the unprocessed index stacks from EMI.
     */
    internal var indexStacks = EmiStackList.filteredStacks

    /**
     * The stacks to be searched, could be from the index, a creative mode tab, or a custom collection tab.
     */
    internal var sourceStacks = listOf<EmiStack>()

    /**
     * The stacks that have been searched
     */
    internal var searchedStacks = listOf<EmiStack>()

    /**
     * Stacks that include stack groups and stack collections.
     */
    private var groupedStacks = listOf<EmiStack>()

    /**
     * Index stacks that include stack groups and stack collections, so we don't have to group them every time.
     */
    private var groupedIndexStacks = listOf<EmiStack>()

    /**
     * Stacks that are already laid out on the grid, and are going to be displayed.
     */
    internal var displayedStacks = mutableListOf<EmiStack>()

    /**
     * A layout for the stacks, recreated every time when first rendered
     */
    internal var stackGrid = arrayOf(arrayOf<EmiStack?>())

    internal var stackTextureGrid = mutableListOf<Layout.Tile>()

    fun reload() {
        indexStacks = EmiStackList.filteredStacks
        groupedIndexStacks = listOf()
        StackGroupManager.buildGroupedEmiStacksAndStackGroupToContents(indexStacks)
        updateSourceStacks(indexStacks)
    }

    internal fun updateSourceStacks(sourceStacks: List<EmiStack>) {
        this.sourceStacks = sourceStacks
        buildStacks(sourceStacks, null)
    }

    internal fun search(sourceStacks: List<EmiStack>, keyword: String) {
        this.sourceStacks = sourceStacks
        EmiSearch.search(keyword)
    }

    internal fun buildStacks(searchedStacks: List<EmiStack>, query: String? = null) {
        this.searchedStacks = if (EmiConfig.editMode) {
            searchedStacks
        } else {
            searchedStacks.filter { !EmiHidden.isHidden(it) }
        }
        buildGroupedStacks(query)
        buildDisplayedStacks()
    }

    private fun buildGroupedStacks(query: String?) {
        val isFullIndex = searchedStacks.size == indexStacks.size

        groupedStacks = if (isFullIndex) groupedIndexStacks.map {
            // TODO: fix this
            it
        }.ifEmpty {
            groupedIndexStacks = StackGroupManager.buildGroupedStacks(searchedStacks)
            groupedIndexStacks
        } else StackGroupManager.buildGroupedStacks(searchedStacks)
    }

    private fun buildDisplayedStacks() {
        val newDisplayedStacks = ArrayList<EmiStack>(groupedStacks.size)
        for (emiStack in groupedStacks) {
            if (emiStack is EmiGroupStack) {
                if (emiStack.items.size == 1) {
                    newDisplayedStacks.add(emiStack.items[0].realStack)
                } else if (emiStack.isExpanded) {
                    newDisplayedStacks.add(emiStack)
                    newDisplayedStacks.addAll(emiStack.items)
                } else {
                    newDisplayedStacks.add(emiStack)
                }
            } else {
                newDisplayedStacks.add(emiStack)
            }
        }
        displayedStacks = newDisplayedStacks
    }

    @Deprecated("")
    fun onStackInteractionDeprecated(ingredient: EmiIngredient) {
        Layout.isTextureDirty = true
        when (ingredient) {
            is EmiGroupStack -> {
                val stacks = displayedStacks
                if (ingredient.isExpanded) {
                    for (i in 0 until ingredient.items.size) {
                        stacks.removeAt(stacks.indexOf(ingredient) + 1)
                    }
                } else {
                    stacks.addAll(stacks.indexOf(ingredient) + 1, ingredient.items)
                }
                // TODO: fix this
                displayedStacks = stacks.toMutableList()
                EmiScreenManager.recalculate()
                ingredient.isExpanded = !ingredient.isExpanded
            }

            else -> {}
        }
    }

}