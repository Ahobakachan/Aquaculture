package local.aquavanilla;

import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import java.util.Set;

public final class Equipment {
    private static final Set<String> GEAR = Set.of("worm", "leech", "fishing_line", "bobber", "tackle_box", "worm_farm");
    private Equipment() {}
    public static boolean disabled(ResourceLocation id) {
        if (id == null || !(id.getNamespace().equals("aquaculture") || id.getNamespace().equals("lavafishing") || id.getNamespace().equals("aq2lava"))) return false;
        String path = id.getPath();
        return GEAR.contains(path) || path.endsWith("_fishing_rod") || path.endsWith("_hook");
    }
    public static boolean hidden(ResourceLocation id) {
        return disabled(id) || (id != null && id.getNamespace().equals("aquaculture") && id.getPath().endsWith("_fillet_knife"));
    }
    public static boolean disabled(ItemStack stack) { return disabled(BuiltInRegistries.ITEM.getKey(stack.getItem())); }
    public static boolean hidden(ItemStack stack) { return hidden(BuiltInRegistries.ITEM.getKey(stack.getItem())); }
    public static boolean disabled(Block block) { return disabled(BuiltInRegistries.BLOCK.getKey(block)); }
    public static boolean disabledRecipe(ResourceLocation id, JsonElement json) {
        if (hidden(id)) return true;
        if (!json.isJsonObject()) return false;
        JsonElement result = json.getAsJsonObject().get("result");
        if (result == null) return false; // Preserve Aquaculture's custom fish + knife recipe.
        if (result.isJsonObject()) {
            var object = result.getAsJsonObject();
            result = object.has("id") ? object.get("id") : object.get("item");
        }
        return result != null && result.isJsonPrimitive() && hidden(ResourceLocation.tryParse(result.getAsString()));
    }
}
