package com.evandev.remi.feature.stackgroup;

import com.evandev.ReliableEmi;
import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.feature.stackgroup.data.EmiStackGroup;
import com.evandev.remi.feature.stackgroup.data.StackGroup;
import com.evandev.remi.feature.stackgroup.data.groups.*;
import com.evandev.remi.integration.emi.StackManager;
import com.evandev.remi.platform.Services;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.config.SidebarType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings("UnstableApiUsage")
public class StackGroupManager {
    public static final List<StackGroup> stackGroups = new ArrayList<>();
    public static final List<EmiStack> groupedEmiStacks = new ArrayList<>();
    public static final IdentityHashMap<EmiStack, List<GroupedEmiStack<EmiStack>>> stackToGroupedStacks = new IdentityHashMap<>();
    private static final Map<String, BiFunction<ResourceLocation, JsonObject, StackGroup>> typeRegistry = new HashMap<>();
    private static final Map<ResourceLocation, List<GroupedEmiStack<EmiStack>>> itemToGroupedStacks = new HashMap<>();
    public static Map<StackGroup, EmiGroupStack> groupToGroupStacks = new HashMap<>();

    static {
        registerType("remi:group", (id, json) -> EmiStackGroup.parse(json, id));
        registerType("emixx:group", (id, json) -> EmiStackGroup.parse(json, id));

        BiFunction<ResourceLocation, JsonObject, StackGroup> tagFactory = (id, json) -> {
            String tagName = GsonHelper.getAsString(json, "tag");
            String registryName = json.has("registry")
                    ? GsonHelper.getAsString(json, "registry")
                    : EmiStackGroup.resolveTagRegistry(ResourceLocation.tryParse(tagName));
            registryName = EmiStackGroup.normalizeRegistry(registryName);
            @SuppressWarnings("rawtypes")
            TagKey tagKey = TagKey.create(
                    ResourceKey.createRegistryKey(ResourceLocation.parse(registryName)),
                    ResourceLocation.parse(tagName));
            String nameKey = json.has("name") ? GsonHelper.getAsString(json, "name") : null;
            Component customName = nameKey != null ? Component.translatable(nameKey) : null;
            EmiIngredient ingredient = new TagEmiIngredient(tagKey, 1);
            if (ingredient.getEmiStacks().isEmpty()) {
                ingredient = EmiIngredient.of(tagKey);
            }

            return new EmiStackGroup(id, tagKey, Set.of(ingredient), Set.of(), List.of(), customName);
        };
        registerType("remi:tag", tagFactory);
        registerType("emixx:tag", tagFactory);

        registerType("remi:spawn_eggs", (id, json) -> new SpawnEggItemGroup());
        registerType("emixx:spawn_eggs", (id, json) -> new SpawnEggItemGroup());
        registerType("remi:pressure_plates", (id, json) -> new PressurePlateItemGroup());
        registerType("emixx:pressure_plates", (id, json) -> new PressurePlateItemGroup());
        registerType("remi:minecarts", (id, json) -> new MinecartItemGroup());
        registerType("emixx:minecarts", (id, json) -> new MinecartItemGroup());
        registerType("remi:infested_blocks", (id, json) -> new InfestedBlockItemGroup());
        registerType("emixx:infested_blocks", (id, json) -> new InfestedBlockItemGroup());
        registerType("remi:copper_blocks", (id, json) -> new CopperBlockItemGroup());
        registerType("emixx:copper_blocks", (id, json) -> new CopperBlockItemGroup());
        registerType("remi:banner_patterns", (id, json) -> new BannerPatternItemGroup());
        registerType("emixx:banner_patterns", (id, json) -> new BannerPatternItemGroup());
        registerType("remi:animal_armors", (id, json) -> new AnimalArmorItemGroup());
        registerType("emixx:animal_armors", (id, json) -> new AnimalArmorItemGroup());

        registerType("remi:regex", (id, json) -> EmiStackGroup.parse(json, id));
        registerType("emixx:regex", (id, json) -> EmiStackGroup.parse(json, id));
    }

    public static Map<ResourceLocation, List<GroupedEmiStack<EmiStack>>> getItemToGroupedStacks() {
        return itemToGroupedStacks;
    }

    public static void registerType(String type, BiFunction<ResourceLocation, JsonObject, StackGroup> factory) {
        typeRegistry.put(type, factory);
    }

    public static ResourceLocation getTagGroupId(TagKey<?> tagKey) {
        String registry = tagKey.registry().location().toString();
        if (registry.equals("minecraft:item")) {
            return tagKey.location();
        }
        String prefix = registry.replace(':', '_') + "_";
        return ResourceLocation.fromNamespaceAndPath(tagKey.location().getNamespace(), prefix + tagKey.location().getPath());
    }

    public static Path getGroupPath(TagKey<?> tagKey) {
        ResourceLocation tag = tagKey.location();
        String registry = tagKey.registry().location().toString();
        String name = tag.getPath().replace('/', '_');
        String prefix = registry.equals("minecraft:item") ? "" : registry.replace(':', '_') + "_";
        String filename = tag.getNamespace() + "_" + prefix + name + ".json";
        return resolveGroupPath(filename);
    }

    public static Path getStackGroupsDir() {
        Path dir = Services.PLATFORM.getConfigDirectory().resolve(ReliableEmi.MOD_ID).resolve("stack_groups");
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (Exception e) {
            ReliableEmi.LOGGER.error("Failed to create stack_groups directory", e);
        }
        return dir;
    }

    public static Path getGroupPath(ResourceLocation tag) {
        String name = tag.getPath().replace('/', '_');
        String filename = tag.getNamespace() + "_" + name + ".json";
        return resolveGroupPath(filename);
    }

    private static Path resolveGroupPath(String filename) {
        Path remiDir = getStackGroupsDir();
        Path remiPath = remiDir.resolve(filename);
        if (Files.exists(remiPath)) {
            return remiPath;
        }
        Path emixxPath = Services.PLATFORM.getConfigDirectory().resolve("emixx").resolve("stack_groups").resolve(filename);
        if (Files.exists(emixxPath)) {
            return emixxPath;
        }
        return remiPath;
    }

    public static StackGroup getGroup(TagKey<?> tagKey) {
        for (StackGroup g : stackGroups) {
            if (g instanceof EmiStackGroup esg && Objects.equals(esg.getTagKey(), tagKey)) {
                return g;
            }
            if (g.getId().equals(getTagGroupId(tagKey))) {
                return g;
            }
            if (tagKey.registry().location().toString().equals("minecraft:item") && g.getId().equals(tagKey.location())) {
                return g;
            }
        }
        return null;
    }

    public static boolean hasGroup(TagKey<?> tagKey) {
        return getGroup(tagKey) != null;
    }

    public static boolean hasGroup(ResourceLocation tag) {
        for (StackGroup g : stackGroups) if (g.getId().equals(tag)) return true;
        return false;
    }

    public static boolean isGroupEnabled(TagKey<?> tagKey) {
        StackGroup g = getGroup(tagKey);
        return g != null && g.isEnabled;
    }

    public static boolean isGroupEnabled(ResourceLocation tag) {
        for (StackGroup g : stackGroups) {
            if (g.getId().equals(tag)) {
                return g.isEnabled;
            }
        }
        return false;
    }

    public static void toggleTagGroup(TagKey<?> tagKey) {
        StackGroup group = getGroup(tagKey);
        ResourceLocation groupId = group != null ? group.getId() : getTagGroupId(tagKey);
        boolean currentlyEnabled = isGroupEnabled(tagKey);
        String idStr = groupId.toString();
        String altStr = idStr.startsWith("remi:") ? "emixx:" + idStr.substring(5)
                : idStr.startsWith("emixx:") ? "remi:" + idStr.substring(6) : null;
        if (currentlyEnabled) {
            if (!ReliableEmiConfig.disabledStackGroups.contains(idStr)) {
                ReliableEmiConfig.disabledStackGroups.add(idStr);
            }
            Path file = getGroupPath(tagKey);
            if (Files.exists(file)) {
                saveGroupConfig(tagKey, false);
            }
        } else {
            ReliableEmiConfig.disabledStackGroups.remove(idStr);
            if (altStr != null) ReliableEmiConfig.disabledStackGroups.remove(altStr);
            if (group == null) {
                saveGroupConfig(tagKey, true);
            } else {
                Path file = getGroupPath(tagKey);
                if (Files.exists(file)) {
                    saveGroupConfig(tagKey, true);
                }
            }
        }
        ReliableEmiConfig.save();
        reload();
    }

    public static void toggleTagGroup(ResourceLocation tag) {
        boolean currentlyEnabled = isGroupEnabled(tag);
        String idStr = tag.toString();
        String altStr = idStr.startsWith("remi:") ? "emixx:" + idStr.substring(5)
                : idStr.startsWith("emixx:") ? "remi:" + idStr.substring(6) : null;
        if (currentlyEnabled) {
            if (!ReliableEmiConfig.disabledStackGroups.contains(idStr)) {
                ReliableEmiConfig.disabledStackGroups.add(idStr);
            }
        } else {
            ReliableEmiConfig.disabledStackGroups.remove(idStr);
            if (altStr != null) ReliableEmiConfig.disabledStackGroups.remove(altStr);
            if (!hasGroup(tag)) {
                saveGroupConfig(tag, true);
            } else {
                Path file = getGroupPath(tag);
                if (Files.exists(file)) {
                    saveGroupConfig(tag, true);
                }
            }
        }
        ReliableEmiConfig.save();
        reload();
    }

    public static void saveGroupConfig(TagKey<?> tagKey, boolean enabled) {
        Path file = getGroupPath(tagKey);
        try {
            Files.createDirectories(file.getParent());
            JsonObject json = new JsonObject();
            json.addProperty("type", "remi:tag");
            json.addProperty("id", getTagGroupId(tagKey).toString());
            json.addProperty("tag", tagKey.location().toString());
            json.addProperty("registry", tagKey.registry().location().toString());
            json.addProperty("enabled", enabled);
            try (var writer = Files.newBufferedWriter(file)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            }
        } catch (Exception e) {
            ReliableEmi.LOGGER.error("Failed to save stack group", e);
        }
    }

    public static void saveGroupConfig(ResourceLocation tag, boolean enabled) {
        Path file = getGroupPath(tag);
        try {
            Files.createDirectories(file.getParent());
            JsonObject json = new JsonObject();
            json.addProperty("type", "remi:tag");
            json.addProperty("id", tag.toString());
            json.addProperty("tag", tag.toString());
            json.addProperty("registry", EmiStackGroup.resolveTagRegistry(tag));
            json.addProperty("enabled", enabled);
            try (var writer = Files.newBufferedWriter(file)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            }
        } catch (Exception e) {
            ReliableEmi.LOGGER.error("Failed to save stack group", e);
        }
    }

    public static void appendStacksForMatchingGroups(String query, List<EmiStack> results) {
        String lower = query.toLowerCase(Locale.ROOT);
        Set<EmiStack> existing = new HashSet<>(results);

        Set<ResourceLocation> allowedIds = null;
        List<EmiStack> tabSource = StackManager.sourceStacks;
        if (tabSource != null && tabSource != StackManager.indexStacks) {
            allowedIds = new HashSet<>(tabSource.size());
            for (EmiStack stack : tabSource) allowedIds.add(stack.getId());
        }

        for (StackGroup group : stackGroups) {
            if (!group.isEnabled) continue;
            EmiGroupStack gs = groupToGroupStacks.get(group);
            if (gs == null) continue;

            boolean match = false;

            if (group.getId().toString().toLowerCase(Locale.ROOT).contains(lower)) {
                match = true;
            } else if (gs.getName().getString().toLowerCase(Locale.ROOT).contains(lower)) {
                match = true;
            }

            if (match) {
                for (var item : gs.getItems()) {
                    if (allowedIds != null && !allowedIds.contains(item.realStack.getId())) continue;
                    if (existing.add(item.realStack)) results.add(item.realStack);
                }
            }
        }
    }

    public static void reload() {
        StackManager.invalidateStacks();
        stackGroups.clear();
        if (!ReliableEmiConfig.enableStackGroups) return;

        Map<ResourceLocation, StackGroup> loaded = new LinkedHashMap<>();

        var resourceManager = Minecraft.getInstance().getResourceManager();
        try {
            var resources = resourceManager.listResources("stack_groups", loc -> loc.getPath().endsWith(".json"));
            for (var entry : resources.entrySet()) {
                var location = entry.getKey();
                var resource = entry.getValue();
                String namespace = location.getNamespace();
                String path = location.getPath().substring("stack_groups/".length());
                path = path.substring(0, path.length() - ".json".length());
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
                try (var reader = resource.openAsReader()) {
                    loadGroup(id, JsonParser.parseReader(reader).getAsJsonObject(), loaded);
                }
            }
        } catch (Exception e) {
            ReliableEmi.LOGGER.error("Failed to list stack groups", e);
        }

        List<Path> configDirs = new ArrayList<>();
        Path primaryDir = getStackGroupsDir();
        if (Files.exists(primaryDir)) {
            configDirs.add(primaryDir);
        }
        Path emixxDir = Services.PLATFORM.getConfigDirectory().resolve("emixx").resolve("stack_groups");
        if (Files.exists(emixxDir) && !configDirs.contains(emixxDir)) {
            configDirs.add(emixxDir);
        }

        Set<ResourceLocation> userLoadedIds = new HashSet<>();
        for (Path dir : configDirs) {
            try (var stream = Files.walk(dir)) {
                stream.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".json")).forEach(path -> {
                    try (var reader = Files.newBufferedReader(path)) {
                        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                        String idString;
                        if (json.has("id")) {
                            idString = json.get("id").getAsString();
                        } else if (json.has("tag")) {
                            String tag = json.get("tag").getAsString();
                            String registry = json.has("registry")
                                    ? json.get("registry").getAsString()
                                    : EmiStackGroup.resolveTagRegistry(ResourceLocation.tryParse(tag));
                            registry = EmiStackGroup.normalizeRegistry(registry);
                            if (registry.equals("minecraft:item")) {
                                idString = tag;
                            } else {
                                ResourceLocation tagLoc = ResourceLocation.parse(tag);
                                idString = ResourceLocation.fromNamespaceAndPath(tagLoc.getNamespace(), registry.replace(':', '_') + "_" + tagLoc.getPath()).toString();
                            }
                        } else {
                            idString = null;
                        }
                        if (idString != null) {
                            ResourceLocation resId = ResourceLocation.parse(idString);
                            if (userLoadedIds.add(resId)) {
                                loadGroup(resId, json, loaded);
                            }
                        }
                    } catch (Exception e) {
                        ReliableEmi.LOGGER.error("Failed to load user stack group {}", path, e);
                    }
                });
            } catch (Exception e) {
                ReliableEmi.LOGGER.error("Failed to list user stack groups in {}", dir, e);
            }
        }

        List<StackGroup> sorted = new ArrayList<>(loaded.values());
        sorted.sort(Comparator.<StackGroup>comparingInt(g -> -g.priority)
                .thenComparing(g -> g.getId().toString()));
        stackGroups.addAll(sorted);
    }

    private static void loadGroup(ResourceLocation id, JsonObject json, Map<ResourceLocation, StackGroup> loaded) {
        try {
            boolean jsonEnabled = GsonHelper.getAsBoolean(json, "enabled", true);
            String type = GsonHelper.getAsString(json, "type", "remi:group");
            BiFunction<ResourceLocation, JsonObject, StackGroup> factory = typeRegistry.get(type);
            if (factory == null && type.startsWith("emixx:")) {
                factory = typeRegistry.get("remi:" + type.substring(6));
            }

            if (factory != null) {
                StackGroup group = factory.apply(id, json);
                if (group != null) {
                    if (json.has("priority")) {
                        group.priority = GsonHelper.getAsInt(json, "priority", group.priority);
                    }
                    if (!jsonEnabled || isGroupDisabledInConfig(id.toString())) {
                        group.isEnabled = false;
                    }
                    loaded.put(id, group);
                }
            } else if (!jsonEnabled) {
                if (loaded.containsKey(id)) {
                    loaded.get(id).isEnabled = false;
                }
            } else {
                ReliableEmi.LOGGER.error("Unknown stack group type '{}' for {}", type, id);
            }
        } catch (Exception e) {
            ReliableEmi.LOGGER.error("Failed to parse stack group {}", id, e);
        }
    }

    public static boolean isGroupDisabledInConfig(String idStr) {
        if (ReliableEmiConfig.disabledStackGroups.contains(idStr)) {
            return true;
        }
        if (idStr.startsWith("remi:")) {
            return ReliableEmiConfig.disabledStackGroups.contains("emixx:" + idStr.substring(5));
        } else if (idStr.startsWith("emixx:")) {
            return ReliableEmiConfig.disabledStackGroups.contains("remi:" + idStr.substring(6));
        }
        return false;
    }

    public static List<EmiStack> buildGroupedStacks(List<EmiStack> source) {
        List<EmiStack> result = new ArrayList<>(source.size());
        Set<StackGroup> addedGroups = Collections.newSetFromMap(new IdentityHashMap<>());
        IdentityHashMap<StackGroup, List<GroupedEmiStack<EmiStack>>> groupMatches = new IdentityHashMap<>();

        for (EmiStack emiStack : source) {
            List<GroupedEmiStack<EmiStack>> variants = stackToGroupedStacks.get(emiStack);
            if (variants == null) {
                List<GroupedEmiStack<EmiStack>> idVariants = itemToGroupedStacks.get(emiStack.getId());
                if (idVariants == null) continue;
                variants = new ArrayList<>();
                for (var v : idVariants) {
                    if (v.realStack.isEqual(emiStack, Comparison.compareComponents())) variants.add(v);
                }
                if (variants.isEmpty()) continue;
            }
            for (var grouped : variants) {
                groupMatches.computeIfAbsent(grouped.stackGroup, k -> new ArrayList<>()).add(grouped);
            }
        }

        for (EmiStack emiStack : source) {
            List<GroupedEmiStack<EmiStack>> variants = stackToGroupedStacks.get(emiStack);
            if (variants == null) {
                List<GroupedEmiStack<EmiStack>> idVariants = itemToGroupedStacks.get(emiStack.getId());
                if (idVariants != null) {
                    variants = new ArrayList<>();
                    for (var v : idVariants) {
                        if (v.realStack.isEqual(emiStack, Comparison.compareComponents())) variants.add(v);
                    }
                }
            }

            if (variants == null || variants.isEmpty()) {
                result.add(emiStack);
                continue;
            }

            boolean wasGrouped = false;
            for (var grouped : variants) {
                StackGroup group = grouped.stackGroup;
                List<GroupedEmiStack<EmiStack>> matches = groupMatches.get(group);
                if (matches == null) continue;
                if (group.isEnabled && matches.size() >= 2) {
                    if (addedGroups.add(group)) {
                        EmiGroupStack cached = groupToGroupStacks.get(group);
                        if (cached != null && cached.itemsNew.size() == matches.size()) {
                            result.add(cached);
                        } else {
                            result.add(new EmiGroupStack(group, new ArrayList<>(matches)));
                        }
                    }
                    wasGrouped = true;
                    break;
                }
            }
            if (!wasGrouped) result.add(emiStack);
        }
        return result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<EmiIngredient> buildGroupedIngredients(List<? extends EmiIngredient> source, SidebarType type) {
        if (source == null || source.isEmpty()) return List.of();
        List<EmiIngredient> result = new ArrayList<>(source.size());
        Set<StackGroup> addedGroups = Collections.newSetFromMap(new IdentityHashMap<>());
        IdentityHashMap<StackGroup, List<GroupedEmiStack<EmiStack>>> groupMatches = new IdentityHashMap<>();

        Map<EmiIngredient, EmiStack> ingredientToPrimaryStack = new IdentityHashMap<>();
        for (EmiIngredient ing : source) {
            if (ing == null) continue;
            EmiStack primary = ing instanceof EmiStack es ? es : (!ing.getEmiStacks().isEmpty() ? ing.getEmiStacks().getFirst() : null);
            if (primary != null) {
                ingredientToPrimaryStack.put(ing, primary);
                List<GroupedEmiStack<EmiStack>> variants = stackToGroupedStacks.get(primary);
                if (variants == null) {
                    List<GroupedEmiStack<EmiStack>> idVariants = itemToGroupedStacks.get(primary.getId());
                    if (idVariants != null) {
                        variants = new ArrayList<>();
                        for (var v : idVariants) {
                            if (v.realStack.isEqual(primary, Comparison.compareComponents())) variants.add(v);
                        }
                    }
                }
                if (variants != null) {
                    for (var grouped : variants) {
                        GroupedEmiStack wrapped = new GroupedEmiStack(primary, ing, grouped.stackGroup);
                        groupMatches.computeIfAbsent(grouped.stackGroup, k -> new ArrayList<>()).add(wrapped);
                    }
                }
            }
        }

        for (EmiIngredient ing : source) {
            if (ing == null) continue;
            EmiStack primary = ingredientToPrimaryStack.get(ing);
            if (primary == null) {
                result.add(ing);
                continue;
            }

            List<GroupedEmiStack<EmiStack>> variants = stackToGroupedStacks.get(primary);
            if (variants == null) {
                List<GroupedEmiStack<EmiStack>> idVariants = itemToGroupedStacks.get(primary.getId());
                if (idVariants != null) {
                    variants = new ArrayList<>();
                    for (var v : idVariants) {
                        if (v.realStack.isEqual(primary, Comparison.compareComponents())) variants.add(v);
                    }
                }
            }

            if (variants == null || variants.isEmpty()) {
                result.add(ing);
                continue;
            }

            boolean wasGrouped = false;
            for (var grouped : variants) {
                StackGroup group = grouped.stackGroup;
                List<GroupedEmiStack<EmiStack>> matches = groupMatches.get(group);
                if (matches == null) continue;
                if (group.isEnabled && matches.size() >= 2) {
                    if (addedGroups.add(group)) {
                        EmiGroupStack groupStack = new EmiGroupStack(group, new ArrayList<>(matches));
                        groupStack.isExpanded = StackManager.isGroupExpanded(type, group.getId());
                        if (groupStack.isExpanded) {
                            result.add(groupStack);
                            for (GroupedEmiStack<EmiStack> item : matches) {
                                result.add(item.realIngredient != null ? item.realIngredient : item.realStack);
                            }
                        } else {
                            result.add(groupStack);
                        }
                    }
                    wasGrouped = true;
                    break;
                }
            }
            if (!wasGrouped) result.add(ing);
        }
        return result;
    }

    public static void buildGroupedEmiStacksAndStackGroupToContents(List<EmiStack> source) {
        groupedEmiStacks.clear();
        itemToGroupedStacks.clear();
        stackToGroupedStacks.clear();

        Map<StackGroup, EmiGroupStack> localGroupMap = new IdentityHashMap<>();
        for (StackGroup g : stackGroups) localGroupMap.put(g, new EmiGroupStack(g, new ArrayList<>()));

        for (EmiStack stack : source) {
            ResourceLocation stackId = stack.getId();

            for (StackGroup group : stackGroups) {
                if (!group.isEnabled) continue;

                Set<ResourceLocation> optimizedIds = group.getOptimizedIds();
                if (optimizedIds != null && !optimizedIds.isEmpty()) {
                    if (!optimizedIds.contains(stackId)) continue;
                }

                if (group.match(stack)) {
                    registerMatch(group, stack, localGroupMap);
                    break;
                }
            }
        }

        groupToGroupStacks = localGroupMap;

        for (var entry : localGroupMap.entrySet()) {
            String groupId = entry.getKey().getId().toString();
            List<String> savedOrder = ReliableEmiConfig.stackGroupItemOrder.get(groupId);
            if (savedOrder == null && groupId.startsWith("remi:")) {
                savedOrder = ReliableEmiConfig.stackGroupItemOrder.get("emixx:" + groupId.substring(5));
            } else if (savedOrder == null && groupId.startsWith("emixx:")) {
                savedOrder = ReliableEmiConfig.stackGroupItemOrder.get("remi:" + groupId.substring(6));
            }
            if (savedOrder != null && !savedOrder.isEmpty()) {
                final List<String> itemOrder = savedOrder;
                entry.getValue().itemsNew.sort((a, b) -> {
                    int idxA = itemOrder.indexOf(a.realStack.getId().toString());
                    int idxB = itemOrder.indexOf(b.realStack.getId().toString());
                    if (idxA == -1 && idxB == -1) return 0;
                    if (idxA == -1) return 1;
                    if (idxB == -1) return -1;
                    return Integer.compare(idxA, idxB);
                });
            }
        }
    }

    private static void registerMatch(StackGroup group, EmiStack stack, Map<StackGroup, EmiGroupStack> groupStacksMap) {
        EmiGroupStack groupStack = groupStacksMap.get(group);
        if (groupStack == null) return;
        GroupedEmiStack<EmiStack> groupedStack = new GroupedEmiStack<>(stack, group);
        boolean added = groupStack.append(groupedStack);
        if (added && group.isEnabled) {
            boolean alreadyGrouped = false;
            for (EmiStack gs : groupedEmiStacks) {
                if (gs.isEqual(stack, Comparison.compareComponents())) {
                    alreadyGrouped = true;
                    break;
                }
            }
            if (!alreadyGrouped) groupedEmiStacks.add(stack);
            itemToGroupedStacks.computeIfAbsent(stack.getId(), k -> new ArrayList<>()).add(groupedStack);
            stackToGroupedStacks.computeIfAbsent(stack, k -> new ArrayList<>()).add(groupedStack);
        }
    }
}