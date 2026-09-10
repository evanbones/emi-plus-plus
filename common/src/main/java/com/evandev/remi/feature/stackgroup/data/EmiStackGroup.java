package com.evandev.remi.feature.stackgroup.data;

import com.evandev.ReliableEmi;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public class EmiStackGroup extends StackGroup {
    private final TagKey<?> tagKey;
    private final Map<ResourceLocation, List<EmiIngredient>> targetMap;
    private final Set<ResourceLocation> allTargetIds;
    private final Set<ResourceLocation> excludedIds;
    private final List<Pattern> regexes;

    public EmiStackGroup(ResourceLocation id, @Nullable TagKey<?> tagKey, Set<EmiIngredient> targets, Set<ResourceLocation> excludedIds, List<Pattern> regexes, Component name) {
        super(id, name);
        this.tagKey = tagKey;
        this.excludedIds = excludedIds;
        this.regexes = regexes != null ? regexes : List.of();

        Map<ResourceLocation, List<EmiIngredient>> tempMap = new HashMap<>();
        Set<ResourceLocation> tempIds = new HashSet<>();
        for (EmiIngredient ingredient : targets) {
            for (EmiStack stack : getIngredientStacks(ingredient)) {
                if (stack == null || stack.isEmpty()) continue;
                ResourceLocation stackId = stack.getId();
                if (stackId == null) continue;
                tempMap.computeIfAbsent(stackId, k -> new ArrayList<>()).add(ingredient);
                tempIds.add(stackId);
            }
        }
        this.targetMap = tempMap;
        this.allTargetIds = tempIds;
    }

    public EmiStackGroup(ResourceLocation id, Set<EmiIngredient> targets, Set<ResourceLocation> excludedIds, List<Pattern> regexes, Component name) {
        this(id, null, targets, excludedIds, regexes, name);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static List<EmiStack> getIngredientStacks(EmiIngredient ingredient) {
        List<EmiStack> stacks = ingredient.getEmiStacks();
        if (stacks.isEmpty() && ingredient instanceof TagEmiIngredient tagIngredient) {
            TagKey<?> rawKey = tagIngredient.key;
            if (rawKey != null) {
                List<EmiStack> rawStacks = new ArrayList<>();
                try {
                    if (rawKey.registry().equals(BuiltInRegistries.BLOCK.key())) {
                        @SuppressWarnings("unchecked")
                        TagKey<Block> blockTagKey = (TagKey<Block>) rawKey;
                        var tagHolderList = BuiltInRegistries.BLOCK.getTag(blockTagKey);
                        if (tagHolderList.isPresent()) {
                            for (var holder : tagHolderList.get()) {
                                rawStacks.add(EmiStack.of(holder.value()));
                            }
                        }
                    } else if (rawKey.registry().equals(BuiltInRegistries.ENTITY_TYPE.key())) {
                        @SuppressWarnings("unchecked")
                        TagKey<EntityType<?>> entityTagKey = (TagKey<EntityType<?>>) rawKey;
                        var tagHolderList = BuiltInRegistries.ENTITY_TYPE.getTag(entityTagKey);
                        if (tagHolderList.isPresent()) {
                            for (var holder : tagHolderList.get()) {
                                SpawnEggItem egg = SpawnEggItem.byId(holder.value());
                                if (egg != null) {
                                    rawStacks.add(EmiStack.of(egg));
                                }
                            }
                        }
                    } else if (rawKey.registry().equals(BuiltInRegistries.FLUID.key())) {
                        @SuppressWarnings("unchecked")
                        TagKey<Fluid> fluidTagKey = (TagKey<Fluid>) rawKey;
                        var tagHolderList = BuiltInRegistries.FLUID.getTag(fluidTagKey);
                        if (tagHolderList.isPresent()) {
                            for (var holder : tagHolderList.get()) {
                                rawStacks.add(EmiStack.of(holder.value()));
                            }
                        }
                    } else {
                        @SuppressWarnings("unchecked")
                        TagKey<Item> itemTagKey = (TagKey<Item>) rawKey;
                        var tagHolderList = BuiltInRegistries.ITEM.getTag(itemTagKey);
                        if (tagHolderList.isPresent()) {
                            for (Holder<Item> holder : tagHolderList.get()) {
                                rawStacks.add(EmiStack.of(holder.value()));
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
                if (!rawStacks.isEmpty()) {
                    return rawStacks.stream().filter(s -> s != null && !s.isEmpty() && s.getId() != null).toList();
                }
            }
        }
        return stacks.stream().filter(s -> s != null && !s.isEmpty() && s.getId() != null).toList();
    }

    private static String normalizeType(String typeStr) {
        if (typeStr == null) return "item";
        String lower = typeStr.toLowerCase(Locale.ROOT);
        return switch (lower) {
            case "jeed:effect", "jeed:effects", "jeed", "effect", "effects", "remi:effect", "emixx:effect",
                 "mob_effect", "mob_effects" -> "mob_effect";
            case "emixx:tag", "remi:tag" -> "tag";
            case "emixx:item", "remi:item", "emi:item" -> "item";
            case "emixx:fluid", "remi:fluid", "emi:fluid" -> "fluid";
            default -> typeStr;
        };
    }

    public static String normalizeRegistry(String reg) {
        if (reg == null) return "minecraft:item";
        String lower = reg.toLowerCase(Locale.ROOT);
        return switch (lower) {
            case "item", "items", "minecraft:item" -> "minecraft:item";
            case "block", "blocks", "minecraft:block" -> "minecraft:block";
            case "fluid", "fluids", "minecraft:fluid" -> "minecraft:fluid";
            case "entity", "entities", "entity_type", "entity_types", "minecraft:entity_type" ->
                    "minecraft:entity_type";
            default -> reg.contains(":") ? reg : "minecraft:" + reg;
        };
    }

    public static String resolveTagRegistry(ResourceLocation tagLoc) {
        if (tagLoc == null) return "minecraft:item";

        TagKey<Item> itemKey = TagKey.create(BuiltInRegistries.ITEM.key(), tagLoc);
        if (BuiltInRegistries.ITEM.getTag(itemKey).filter(h -> h.size() > 0).isPresent()) {
            return "minecraft:item";
        }

        TagKey<Block> blockKey = TagKey.create(BuiltInRegistries.BLOCK.key(), tagLoc);
        if (BuiltInRegistries.BLOCK.getTag(blockKey).filter(h -> h.size() > 0).isPresent()) {
            return "minecraft:block";
        }

        TagKey<Fluid> fluidKey = TagKey.create(BuiltInRegistries.FLUID.key(), tagLoc);
        if (BuiltInRegistries.FLUID.getTag(fluidKey).filter(h -> h.size() > 0).isPresent()) {
            return "minecraft:fluid";
        }

        TagKey<EntityType<?>> entityKey = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), tagLoc);
        if (BuiltInRegistries.ENTITY_TYPE.getTag(entityKey).filter(h -> h.size() > 0).isPresent()) {
            return "minecraft:entity_type";
        }

        if (BuiltInRegistries.ITEM.getTag(itemKey).isPresent()) {
            return "minecraft:item";
        }
        if (BuiltInRegistries.BLOCK.getTag(blockKey).isPresent()) {
            return "minecraft:block";
        }
        if (BuiltInRegistries.FLUID.getTag(fluidKey).isPresent()) {
            return "minecraft:fluid";
        }
        if (BuiltInRegistries.ENTITY_TYPE.getTag(entityKey).isPresent()) {
            return "minecraft:entity_type";
        }

        return "minecraft:item";
    }

    private static JsonElement normalizeIngredientJson(JsonElement element) {
        if (element == null || element.isJsonNull()) return element;

        if (!element.isJsonPrimitive()) {
            if (element instanceof JsonObject obj) {
                if (obj.has("type")) {
                    String typeStr = obj.get("type").getAsString();
                    String normType = normalizeType(typeStr);
                    JsonObject copy = null;
                    if (!normType.equals(typeStr)) {
                        copy = obj.deepCopy();
                        copy.addProperty("type", normType);
                    }
                    if ("tag".equals(normType)) {
                        String reg = obj.has("registry") ? obj.get("registry").getAsString() : null;
                        if (reg == null) {
                            String tagId = obj.has("id") ? obj.get("id").getAsString() : obj.has("tag") ? obj.get("tag").getAsString() : null;
                            if (tagId != null) {
                                if (copy == null) copy = obj.deepCopy();
                                copy.addProperty("registry", resolveTagRegistry(ResourceLocation.tryParse(tagId)));
                            }
                        } else {
                            String normReg = normalizeRegistry(reg);
                            if (!normReg.equals(reg)) {
                                if (copy == null) copy = obj.deepCopy();
                                copy.addProperty("registry", normReg);
                            }
                        }
                    }
                    if (copy != null) return copy;
                } else if (obj.has("id")) {
                    String idStr = obj.get("id").getAsString();
                    ResourceLocation resLoc = ResourceLocation.tryParse(idStr);
                    if (resLoc != null) {
                        JsonObject copy = obj.deepCopy();
                        if (BuiltInRegistries.MOB_EFFECT.containsKey(resLoc)) {
                            copy.addProperty("type", "mob_effect");
                        } else if (BuiltInRegistries.FLUID.containsKey(resLoc)) {
                            copy.addProperty("type", "fluid");
                        } else {
                            copy.addProperty("type", "item");
                        }
                        return copy;
                    }
                }
            }
            return element;
        }

        String str = element.getAsString();
        if (str.startsWith("#")) {
            String value = str.substring(1);
            return getJsonObject(value);
        }

        String[] split = str.split(":");
        if (split.length >= 4) {
            String prefix = split[0] + ":" + split[1];
            String id = split[2] + ":" + split[3];
            JsonObject obj = new JsonObject();
            obj.addProperty("type", normalizeType(prefix));
            obj.addProperty("id", id);
            return obj;
        } else if (split.length == 3) {
            String prefix = split[0];
            String id = split[1] + ":" + split[2];
            JsonObject obj = new JsonObject();
            obj.addProperty("type", normalizeType(prefix));
            obj.addProperty("id", id);
            return obj;
        } else if (split.length == 2) {
            ResourceLocation resLoc = ResourceLocation.tryParse(str);
            JsonObject obj = new JsonObject();
            obj.addProperty("id", str);
            if (resLoc != null && BuiltInRegistries.MOB_EFFECT.containsKey(resLoc)) {
                obj.addProperty("type", "mob_effect");
            } else if (resLoc != null && BuiltInRegistries.FLUID.containsKey(resLoc)) {
                obj.addProperty("type", "fluid");
            } else {
                obj.addProperty("type", "item");
            }
            return obj;
        } else if (split.length == 1) {
            ResourceLocation resLoc = ResourceLocation.tryParse("minecraft:" + str);
            JsonObject obj = new JsonObject();
            obj.addProperty("id", "minecraft:" + str);
            if (resLoc != null && BuiltInRegistries.MOB_EFFECT.containsKey(resLoc)) {
                obj.addProperty("type", "mob_effect");
            } else if (resLoc != null && BuiltInRegistries.FLUID.containsKey(resLoc)) {
                obj.addProperty("type", "fluid");
            } else {
                obj.addProperty("type", "item");
            }
            return obj;
        }

        JsonObject obj = new JsonObject();
        obj.addProperty("type", "item");
        obj.addProperty("id", str);
        return obj;
    }

    private static @NotNull JsonObject getJsonObject(String value) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "tag");
        int lastColon = value.lastIndexOf(':');
        if (lastColon > 0) {
            int secondLastColon = value.lastIndexOf(':', lastColon - 1);
            if (secondLastColon > 0) {
                String reg = value.substring(0, secondLastColon);
                String tagId = value.substring(secondLastColon + 1);
                String normReg = normalizeRegistry(reg);
                obj.addProperty("registry", normReg);
                obj.addProperty("id", tagId);
                obj.addProperty("tag", tagId);
                return obj;
            }
        }
        String tagId = value.contains(":") ? value : "minecraft:" + value;
        String registry = resolveTagRegistry(ResourceLocation.tryParse(tagId));
        obj.addProperty("id", tagId);
        obj.addProperty("tag", tagId);
        obj.addProperty("registry", registry);
        return obj;
    }

    private static EmiIngredient deserialize(JsonElement element) {
        JsonElement normalized = normalizeIngredientJson(element);
        EmiIngredient ingredient = EmiIngredientSerializer.getDeserialized(normalized);
        if ((ingredient == null || ingredient.isEmpty()) && normalized instanceof JsonObject obj && "tag".equals(GsonHelper.getAsString(obj, "type", null))) {
            String registryName = GsonHelper.getAsString(obj, "registry", "minecraft:item");
            String tagId = GsonHelper.getAsString(obj, "id", GsonHelper.getAsString(obj, "tag", null));
            if (tagId != null) {
                ResourceLocation regLoc = ResourceLocation.tryParse(registryName);
                ResourceLocation idLoc = ResourceLocation.tryParse(tagId);
                if (regLoc != null && idLoc != null) {
                    TagKey<?> tagKey = TagKey.create(ResourceKey.createRegistryKey(regLoc), idLoc);
                    ingredient = new TagEmiIngredient(tagKey, 1);
                }
            }
        }
        return ingredient;
    }

    private static Pattern compilePattern(String raw) {
        if (raw == null || raw.isEmpty()) return Pattern.compile("");
        String regexStr = raw;
        if (regexStr.contains("*") && !regexStr.contains(".*")) {
            regexStr = regexStr.replace("*", ".*");
        }
        try {
            return Pattern.compile(regexStr);
        } catch (Exception e) {
            try {
                return Pattern.compile(raw);
            } catch (Exception ignored) {
                return Pattern.compile("");
            }
        }
    }

    public static EmiStackGroup parse(JsonElement json, ResourceLocation filenameId) {
        try {
            if (!(json instanceof JsonObject obj)) throw new IllegalArgumentException("Not a JSON object");

            ResourceLocation finalId = obj.has("id")
                    ? ResourceLocation.parse(GsonHelper.getAsString(obj, "id"))
                    : filenameId;

            String nameKey = obj.has("name") ? GsonHelper.getAsString(obj, "name") : null;
            Component customName = nameKey != null ? Component.translatable(nameKey) : null;

            Set<EmiIngredient> targets = Sets.newHashSet();
            if (GsonHelper.isArrayNode(obj, "contents")) {
                for (JsonElement e : obj.getAsJsonArray("contents")) {
                    targets.add(deserialize(e));
                }
            }

            List<Pattern> regexes = new ArrayList<>();
            if (obj.has("regex")) {
                regexes.add(compilePattern(GsonHelper.getAsString(obj, "regex")));
            }
            if (GsonHelper.isArrayNode(obj, "regexes")) {
                for (JsonElement e : obj.getAsJsonArray("regexes")) {
                    regexes.add(compilePattern(e.getAsString()));
                }
            }

            if (targets.isEmpty() && regexes.isEmpty()) {
                throw new IllegalArgumentException("Contents or regex(es) must be present in group configuration.");
            }

            Set<ResourceLocation> excluded = new HashSet<>();
            if (GsonHelper.isArrayNode(obj, "exclusions")) {
                for (JsonElement e : obj.getAsJsonArray("exclusions")) {
                    for (EmiStack s : getIngredientStacks(deserialize(e))) {
                        if (s != null && s.getId() != null) {
                            excluded.add(s.getId());
                        }
                    }
                }
            }

            return new EmiStackGroup(finalId, targets, excluded, regexes, customName);
        } catch (Exception e) {
            ReliableEmi.LOGGER.error("Failed to parse stack group {}: {}", filenameId, e.getMessage());
            return null;
        }
    }

    private static String getStackType(EmiStack stack) {
        try {
            var serializer = dev.emi.emi.registry.EmiIngredientSerializers.BY_CLASS.get(stack.getClass());
            if (serializer != null) {
                return serializer.getType();
            }
        } catch (Exception ignored) {
        }
        String className = stack.getClass().getName();
        if (className.contains("Effect") || className.contains("Potion")) {
            return "mob_effect";
        }
        if (className.contains("Fluid")) {
            return "fluid";
        }
        if (!stack.getItemStack().isEmpty()) {
            return "item";
        }
        return null;
    }

    private static @NotNull List<String> getCandidates(String idStr, String typeStr) {
        List<String> candidates = new ArrayList<>();
        candidates.add(idStr);
        if (typeStr != null) {
            candidates.add(typeStr + ":" + idStr);
            switch (typeStr) {
                case "mob_effect" -> {
                    candidates.add("jeed:effect:" + idStr);
                    candidates.add("jeed:effects:" + idStr);
                    candidates.add("jeed:" + idStr);
                    candidates.add("effect:" + idStr);
                    candidates.add("effects:" + idStr);
                }
                case "fluid" -> {
                    candidates.add("remi:fluid:" + idStr);
                    candidates.add("emixx:fluid:" + idStr);
                }
                case "item" -> {
                    candidates.add("remi:item:" + idStr);
                    candidates.add("emixx:item:" + idStr);
                }
            }
        }
        return candidates;
    }

    public @Nullable TagKey<?> getTagKey() {
        return tagKey;
    }

    @Override
    public Set<ResourceLocation> getOptimizedIds() {
        if (regexes != null && !regexes.isEmpty()) {
            return null;
        }
        return allTargetIds;
    }

    @Override
    public boolean match(EmiIngredient stack) {
        if (!(stack instanceof EmiStack emiStack)) return false;
        ResourceLocation stackId = emiStack.getId();
        if (stackId == null) return false;

        if (excludedIds.contains(stackId)) return false;

        if (!regexes.isEmpty()) {
            String idStr = stackId.toString();
            String typeStr = getStackType(emiStack);

            List<String> candidates = getCandidates(idStr, typeStr);

            for (Pattern pattern : regexes) {
                for (String candidate : candidates) {
                    if (pattern.matcher(candidate).matches()) return true;
                }
            }
        }

        List<EmiIngredient> relevant = targetMap.get(stackId);
        if (relevant != null) {
            for (EmiIngredient target : relevant) {
                for (EmiStack ts : getIngredientStacks(target)) {
                    if (ts.getId().equals(stackId) && (ts.getClass() == emiStack.getClass() || ts.getClass().isInstance(emiStack) || emiStack.getClass().isInstance(ts)))
                        return true;
                }
            }
        }

        return false;
    }
}