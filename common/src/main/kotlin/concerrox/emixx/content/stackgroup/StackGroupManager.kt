package concerrox.emixx.content.stackgroup

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.stackgroup.data.*
import concerrox.emixx.integration.kubejs.EmiPlusPlusKubeJSPlugin
import concerrox.emixx.integration.kubejs.RegisterStackGroupsEventJS
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.util.GsonHelper
import net.minecraft.world.item.Item

object StackGroupManager {

    private val typeRegistry = mutableMapOf<String, (ResourceLocation, JsonObject) -> StackGroup?>()

    internal val stackGroups = mutableListOf<StackGroup>()
    internal var groupToGroupStacks = mapOf<StackGroup, EmiGroupStack>()

    internal val groupedEmiStacks = hashSetOf<EmiStack>()
    internal var stackGroupToGroupStacks = mapOf<StackGroup, EmiGroupStack>()

    init {
        registerType("emixx:group") { id, json -> EmiStackGroup.parse(json, id) }

        registerType("emixx:tag") { id, json ->
            val tagName = GsonHelper.getAsString(json, "tag")
            val tagKey = TagKey.create(Registries.ITEM, ResourceLocation(tagName))

            val targets = mutableSetOf<EmiIngredient>()
            if (Minecraft.getInstance().level != null) {
                val ingredient = EmiIngredient.of(tagKey)
                targets.add(ingredient)
                targets.addAll(ingredient.emiStacks)
            }
            EmiStackGroup(id, targets)
        }

        registerType("emixx:spawn_eggs") { _, _ -> SpawnEggItemGroup() }
        registerType("emixx:pressure_plates") { _, _ -> PressurePlateItemGroup() }
        registerType("emixx:minecarts") { _, _ -> MinecartItemGroup() }
        registerType("emixx:infested_blocks") { _, _ -> InfestedBlockItemGroup() }
        registerType("emixx:copper_blocks") { _, _ -> CopperBlockItemGroup() }
        registerType("emixx:banner_patterns") { _, _ -> BannerPatternItemGroup() }
        registerType("emixx:animal_armors") { _, _ -> AnimalArmorItemGroup() }
    }

    fun registerType(type: String, factory: (ResourceLocation, JsonObject) -> StackGroup?) {
        typeRegistry[type] = factory
    }

    internal fun reload() {
        stackGroups.clear()

        if (!EmiPlusPlusConfig.enableStackGroups.get()) return

        val resourceManager = Minecraft.getInstance().resourceManager
        val loadedGroups = mutableMapOf<ResourceLocation, StackGroup>()

        val resources = resourceManager.listResources("stack_groups") { it.path.endsWith(".json") }

        for ((location, resource) in resources) {
            val namespace = location.namespace
            val path = location.path.removePrefix("stack_groups/").removeSuffix(".json")
            val id = ResourceLocation(namespace, path)

            try {
                resource.openAsReader().use { reader ->
                    val json = JsonParser.parseReader(reader).asJsonObject

                    if (GsonHelper.getAsBoolean(json, "enabled", true)) {
                        val type = GsonHelper.getAsString(json, "type", "emixx:group")
                        val factory = typeRegistry[type]

                        if (factory != null) {
                            val group = factory(id, json)
                            if (group != null) {
                                loadedGroups[id] = group
                            }
                        } else {
                            EmiPlusPlus.LOGGER.error("Unknown stack group type '$type' in $location")
                        }
                    }
                }
            } catch (e: Exception) {
                EmiPlusPlus.LOGGER.error("Failed to load stack group $id", e)
            }
        }

        stackGroups.addAll(loadedGroups.values.sortedBy { it.id.toString() })

        if (isKubeJSLoaded()) {
            EmiPlusPlusKubeJSPlugin.REGISTER_GROUPS.post(RegisterStackGroupsEventJS())
        }
    }

    internal fun buildGroupedStacks(source: List<EmiStack>): List<EmiStack> {
        val result = mutableListOf<EmiStack>()
        val addedStackGroups = mutableSetOf<StackGroup>()

        val localGroupToGroupStacks = stackGroups.associateWith { group -> EmiGroupStack(group, listOf()) }
        val groupsToCheck = stackGroups.toList()

        for (emiStack in source) {
            if (emiStack !in groupedEmiStacks) {
                result += emiStack
                continue
            }
            for (stackGroup in groupsToCheck) {
                val groupStack = localGroupToGroupStacks[stackGroup]!!
                if (stackGroup.match(emiStack)) {
                    groupStack.itemsNew += GroupedEmiStack(emiStack, stackGroup)
                    if (stackGroup !in addedStackGroups) {
                        addedStackGroups += stackGroup
                        if (stackGroup.isEnabled) result += groupStack
                    }
                }
            }
        }
        groupToGroupStacks = localGroupToGroupStacks
        return result
    }

    internal fun buildGroupedEmiStacksAndStackGroupToContents(source: List<EmiStack>) {
        groupedEmiStacks.clear()
        val stackGroupToGroupStacks = stackGroups.associateWith { EmiGroupStack(it, listOf()) }
        for (emiStack in source) {
            for (stackGroup in stackGroups) {
                if (!stackGroup.match(emiStack)) continue
                if (stackGroup.isEnabled) groupedEmiStacks.add(emiStack)
                stackGroupToGroupStacks[stackGroup]!!.itemsNew += GroupedEmiStack(emiStack, stackGroup)
            }
        }
        this.stackGroupToGroupStacks = stackGroupToGroupStacks
    }

    private fun isKubeJSLoaded(): Boolean {
        return try {
            Class.forName("dev.latvian.mods.kubejs.KubeJSPlugin")
            true
        } catch (_: Throwable) {
            false
        }
    }
}