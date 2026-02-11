package concerrox.emixx.content.stackgroup.data

import com.google.common.collect.Sets
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import concerrox.emixx.EmiPlusPlus
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper

class EmiStackGroup(
    id: ResourceLocation,
    targets: Set<EmiIngredient>,
) : StackGroup(id) {

    private val targetIds: Set<ResourceLocation>
    private val otherTargets: List<EmiIngredient>

    init {
        val ids = HashSet<ResourceLocation>()
        val others = ArrayList<EmiIngredient>()
        for (target in targets) {
            if (target is EmiStack) {
                ids.add(target.id)
            } else {
                others.add(target)
            }
        }
        targetIds = ids
        otherTargets = others
    }

    companion object {

        private fun normalizeIngredientJson(element: JsonElement): JsonElement {
            if (element.isJsonPrimitive) {
                val str = element.asString
                if (str.startsWith("#")) {
                    return JsonObject().apply {
                        addProperty("tag", str.substring(1))
                    }
                }
                return JsonObject().apply {
                    addProperty("type", "item")
                    addProperty("id", str)
                }
            } else {
                return element
            }
        }

        private fun deserialize(element: JsonElement): EmiIngredient {
            return EmiIngredientSerializer.getDeserialized(
                normalizeIngredientJson(element)
            )
        }

        private fun MutableSet<EmiIngredient>.addIngredientWithStacks(ingredient: EmiIngredient) {
            add(ingredient)
            addAll(ingredient.emiStacks)
        }

        private fun MutableSet<EmiIngredient>.removeIngredientWithStacks(ingredient: EmiIngredient) {
            remove(ingredient)
            removeAll(ingredient.emiStacks)
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
                    targets.addIngredientWithStacks(deserialize(element))
                }

                if (GsonHelper.isArrayNode(json, "exclusions")) {
                    for (element in json.getAsJsonArray("exclusions")) {
                        targets.removeIngredientWithStacks(deserialize(element))
                    }
                }

                EmiStackGroup(finalId, targets)
            } catch (e: Exception) {
                EmiPlusPlus.LOGGER.error("Failed to parse stack group $filenameId: {}", e.message)
                null
            }
        }
    }

    override fun match(stack: EmiIngredient): Boolean {
        if (stack is EmiStack) {
            if (targetIds.contains(stack.id)) return true
        }

        if (otherTargets.isNotEmpty()) {
            return otherTargets.any { target ->
                if (target is EmiStack && stack is EmiStack) {
                    target.id == stack.id
                } else {
                    target.emiStacks.contains(stack)
                }
            }
        }

        return false
    }

    override fun getSafeMatchingIds(): Collection<ResourceLocation>? {
        return if (otherTargets.isEmpty()) targetIds else null
    }
}