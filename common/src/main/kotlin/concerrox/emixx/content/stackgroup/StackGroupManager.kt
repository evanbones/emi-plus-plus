package concerrox.emixx.content.stackgroup

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.stackgroup.data.*
import concerrox.emixx.integration.kubejs.EmiPlusPlusKubeJSPlugin
import concerrox.emixx.integration.kubejs.RegisterStackGroupsEventJS
import dev.emi.emi.api.stack.Comparison
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.util.GsonHelper
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.div

object StackGroupManager {

    private val typeRegistry = mutableMapOf<String, (ResourceLocation, JsonObject) -> StackGroup?>()
    internal val stackGroups = mutableListOf<StackGroup>()

    internal val groupedEmiStacks = mutableListOf<EmiStack>()
    private val itemToGroupedStacks = mutableMapOf<EmiStack, MutableList<GroupedEmiStack<EmiStack>>>()
    internal var groupToGroupStacks = mapOf<StackGroup, EmiGroupStack>()

    init {
        registerType("emixx:group") { id, json -> EmiStackGroup.parse(json, id) }

        registerType("emixx:tag") { id, json ->
            val tagName = GsonHelper.getAsString(json, "tag")
            val tagKey = TagKey.create(Registries.ITEM, ResourceLocation(tagName))

            EmiStackGroup(id, setOf(EmiIngredient.of(tagKey)))
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

    fun getGroupPath(tag: ResourceLocation): Path {
        val name = tag.path.replace('/', '_')
        val filename = "${tag.namespace}_$name.json"
        return EmiPlusPlusConfig.CONFIG_DIRECTORY_PATH / "stack_groups" / filename
    }

    fun hasGroup(tag: ResourceLocation): Boolean {
        return stackGroups.any { it.id == tag }
    }

    fun toggleTagGroup(tag: ResourceLocation) {
        val isActive = hasGroup(tag)

        if (isActive) {
            saveGroupConfig(tag, false)
        } else {
            saveGroupConfig(tag, true)
        }
        reload()
    }

    private fun saveGroupConfig(tag: ResourceLocation, enabled: Boolean) {
        val file = getGroupPath(tag)
        val directory = file.parent

        val json = JsonObject()
        if (enabled) {
            json.addProperty("type", "emixx:tag")
            json.addProperty("id", tag.toString())
            json.addProperty("tag", tag.toString())
            json.addProperty("enabled", true)
        } else {
            json.addProperty("id", tag.toString())
            json.addProperty("enabled", false)
        }

        try {
            Files.createDirectories(directory)
            Files.newBufferedWriter(file).use { writer ->
                com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(json, writer)
            }
        } catch (e: Exception) {
            EmiPlusPlus.LOGGER.error("Failed to save stack group", e)
        }
    }

    fun appendStacksForMatchingGroups(query: String, results: MutableList<EmiStack>) {
        val lowerQuery = query.lowercase()
        val existingSet = if (results.isNotEmpty()) results.toHashSet() else mutableSetOf()

        val matchingGroups = stackGroups.filter { group ->
            group.id.path.replace('_', ' ').contains(lowerQuery)
        }

        for (group in matchingGroups) {
            val stacksInGroup = groupToGroupStacks[group]?.itemsNew ?: continue

            for (groupedStack in stacksInGroup) {
                if (existingSet.add(groupedStack.realStack)) {
                    results.add(groupedStack.realStack)
                }
            }
        }
    }

    fun reload() {
        stackGroups.clear()

        if (!EmiPlusPlusConfig.enableStackGroups.get()) return

        val loadedGroups = mutableMapOf<ResourceLocation, StackGroup>()

        val resourceManager = Minecraft.getInstance().resourceManager
        try {
            val resources = resourceManager.listResources("stack_groups") { it.path.endsWith(".json") }

            for ((location, resource) in resources) {
                val namespace = location.namespace
                val path = location.path.removePrefix("stack_groups/").removeSuffix(".json")
                val id = ResourceLocation(namespace, path)
                loadGroup(id, resource.openAsReader().use { JsonParser.parseReader(it).asJsonObject }, loadedGroups)
            }
        } catch (e: Exception) {
            EmiPlusPlus.LOGGER.error("Failed to list stack groups", e)
        }

        val configDir = EmiPlusPlusConfig.CONFIG_DIRECTORY_PATH / "stack_groups"
        if (Files.exists(configDir)) {
            try {
                Files.list(configDir).forEach { path ->
                    if (path.toString().endsWith(".json")) {
                        try {
                            val json = Files.newBufferedReader(path).use { JsonParser.parseReader(it).asJsonObject }
                            var idString = if (json.has("id")) json.get("id").asString else null
                            if (idString == null && json.has("tag")) idString = json.get("tag").asString

                            if (idString != null) {
                                val id = ResourceLocation(idString)
                                loadGroup(id, json, loadedGroups)
                            }
                        } catch (e: Exception) {
                            EmiPlusPlus.LOGGER.error("Failed to load user stack group $path", e)
                        }
                    }
                }
            } catch (e: Exception) {
                EmiPlusPlus.LOGGER.error("Failed to list user stack groups", e)
            }
        }

        stackGroups.addAll(loadedGroups.values.sortedBy { it.id.toString() })

        if (isKubeJSLoaded()) {
            EmiPlusPlusKubeJSPlugin.REGISTER_GROUPS.post(RegisterStackGroupsEventJS())
        }
    }

    private fun loadGroup(
        id: ResourceLocation,
        json: JsonObject,
        loadedGroups: MutableMap<ResourceLocation, StackGroup>
    ) {
        try {
            val enabled = GsonHelper.getAsBoolean(json, "enabled", true)
            if (!enabled) {
                loadedGroups.remove(id)
                return
            }

            val type = GsonHelper.getAsString(json, "type", "emixx:group")
            val factory = typeRegistry[type]

            if (factory != null) {
                val group = factory(id, json)
                if (group != null) {
                    loadedGroups[id] = group
                }
            } else {
                EmiPlusPlus.LOGGER.error("Unknown stack group type '$type' for $id")
            }
        } catch (e: Exception) {
            EmiPlusPlus.LOGGER.error("Failed to parse stack group $id", e)
        }
    }

    internal fun buildGroupedStacks(source: List<EmiStack>): List<EmiStack> {
        val result = mutableListOf<EmiStack>()
        val addedGroups = mutableSetOf<StackGroup>()

        val groupMatches = mutableMapOf<StackGroup, MutableList<GroupedEmiStack<EmiStack>>>()
        for (emiStack in source) {
            itemToGroupedStacks[emiStack]?.forEach { grouped ->
                if (grouped.realStack.isEqual(emiStack, Comparison.compareNbt())) {
                    val list = groupMatches.computeIfAbsent(grouped.stackGroup) { mutableListOf() }

                    if (!list.contains(grouped)) {
                        list.add(grouped)
                    }
                }
            }
        }

        for (emiStack in source) {
            val variants = itemToGroupedStacks[emiStack]

            if (variants == null) {
                result += emiStack
                continue
            }

            var wasGrouped = false
            for (grouped in variants) {
                if (!grouped.realStack.isEqual(emiStack, Comparison.compareNbt())) continue

                val group = grouped.stackGroup
                val matches = groupMatches[group] ?: continue

                if (group.isEnabled && matches.size >= 2) {
                    if (group !in addedGroups) {
                        addedGroups += group

                        val cachedGroup = groupToGroupStacks[group]
                        if (cachedGroup != null && cachedGroup.itemsNew.size == matches.size) {
                            result += cachedGroup
                        } else {
                            result += EmiGroupStack(group, matches)
                        }
                    }
                    wasGrouped = true
                    break
                }
            }

            if (!wasGrouped) {
                result += emiStack
            }
        }
        return result
    }

    internal fun buildGroupedEmiStacksAndStackGroupToContents(source: List<EmiStack>) {
        groupedEmiStacks.clear()
        itemToGroupedStacks.clear()

        val localGroupToGroupStacks = stackGroups.associateWith { EmiGroupStack(it, mutableListOf()) }

        val indexedGroups = mutableMapOf<ResourceLocation, MutableList<StackGroup>>()
        val globalGroups = mutableListOf<StackGroup>()

        for (group in stackGroups) {
            val optimizedIds = group.getOptimizedIds()
            if (!optimizedIds.isNullOrEmpty()) {
                for (id in optimizedIds) {
                    indexedGroups.computeIfAbsent(id) { mutableListOf() }.add(group)
                }
            } else {
                globalGroups.add(group)
            }
        }

        for (stack in source) {
            val stackId = stack.id

            indexedGroups[stackId]?.let { groups ->
                for (group in groups) {
                    if (group.match(stack)) {
                        registerMatch(group, stack, localGroupToGroupStacks)
                    }
                }
            }

            for (group in globalGroups) {
                if (group.match(stack)) {
                    registerMatch(group, stack, localGroupToGroupStacks)
                }
            }
        }

        this.groupToGroupStacks = localGroupToGroupStacks
    }

    private fun registerMatch(
        group: StackGroup,
        stack: EmiStack,
        groupStacksMap: Map<StackGroup, EmiGroupStack>
    ) {
        val groupStack = groupStacksMap[group] ?: return
        val groupedStack = GroupedEmiStack(stack, group)

        val wasAdded = groupStack.append(groupedStack)

        if (wasAdded && group.isEnabled) {
            if (groupedEmiStacks.none { it.isEqual(stack, Comparison.compareNbt()) }) {
                groupedEmiStacks.add(stack)
            }

            itemToGroupedStacks.computeIfAbsent(stack) { mutableListOf() }.add(groupedStack)
        }
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