package concerrox.emixx.content.stackgroup.data

import com.google.common.collect.Sets
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import concerrox.emixx.EmiPlusPlus
import dev.emi.emi.api.stack.Comparison
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper

class EmiStackGroup(
    id: ResourceLocation,
    targets: Set<EmiIngredient>,
    private val excludedIds: Set<ResourceLocation> = emptySet()
) : StackGroup(id) {

    private val targetMap: Map<ResourceLocation, List<EmiIngredient>>
    private val allTargetIds: Set<ResourceLocation>

    init {
        val tempMap = mutableMapOf<ResourceLocation, MutableList<EmiIngredient>>()
        val tempIds = mutableSetOf<ResourceLocation>()

        for (ingredient in targets) {
            for (stack in ingredient.emiStacks) {
                val stackId = stack.id
                tempMap.computeIfAbsent(stackId) { mutableListOf() }.add(ingredient)
                tempIds.add(stackId)
            }
        }
        targetMap = tempMap
        allTargetIds = tempIds
    }

    override fun getOptimizedIds(): Set<ResourceLocation> {
        return allTargetIds
    }

    override fun match(stack: EmiIngredient): Boolean {
        val emiStack = stack as? EmiStack ?: return false
        val stackId = emiStack.id

        if (excludedIds.contains(stackId)) return false

        val relevantIngredients = targetMap[stackId] ?: return false
        return relevantIngredients.any { target ->
            if (target is EmiStack && !target.hasNbt()) {
                return@any true
            }
            target.emiStacks.any { it.isEqual(emiStack, Comparison.compareNbt()) }
        }
    }

    companion object {
        // [Existing JSON parsing logic remains unchanged]
        private fun normalizeIngredientJson(element: JsonElement): JsonElement {
            if (element.isJsonPrimitive) {
                val str = element.asString
                if (str.startsWith("#")) {
                    return JsonObject().apply {
                        addProperty("type", "tag")
                        val value = str.substring(1)
                        addProperty("id", value)
                        addProperty("tag", value)
                        addProperty("registry", "minecraft:item")
                    }
                }
                return JsonObject().apply {
                    addProperty("type", "item")
                    addProperty("id", str)
                }
            }
            return element
        }

        private fun deserialize(element: JsonElement): EmiIngredient {
            return EmiIngredientSerializer.getDeserialized(normalizeIngredientJson(element))
        }

        fun parse(json: JsonElement, filenameId: ResourceLocation): EmiStackGroup? {
            return try {
                if (json !is JsonObject) error("Not a JSON object")

                val finalId = if (json.has("id")) {
                    ResourceLocation(GsonHelper.getAsString(json, "id"))
                } else {
                    filenameId
                }

                if (!GsonHelper.isArrayNode(json, "contents"))
                    error("Contents are either not present or not a list")

                val targets = Sets.newHashSet<EmiIngredient>()
                for (element in json.getAsJsonArray("contents")) {
                    targets.add(deserialize(element))
                }

                val excludedIds = HashSet<ResourceLocation>()
                if (GsonHelper.isArrayNode(json, "exclusions")) {
                    for (element in json.getAsJsonArray("exclusions")) {
                        val exclusion = deserialize(element)
                        for (stack in exclusion.emiStacks) {
                            excludedIds.add(stack.id)
                        }
                    }
                }

                EmiStackGroup(finalId, targets, excludedIds)
            } catch (e: Exception) {
                EmiPlusPlus.LOGGER.error("Failed to parse stack group $filenameId: {}", e.message)
                null
            }
        }
    }
}