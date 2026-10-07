package local.aquavanilla.mixin;

import com.google.gson.JsonElement;
import local.aquavanilla.Equipment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import java.util.HashMap;
import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @ModifyVariable(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"), argsOnly = true)
    private Map<ResourceLocation, JsonElement> aquavanilla$recipes(Map<ResourceLocation, JsonElement> recipes) {
        var filtered = new HashMap<>(recipes);
        filtered.entrySet().removeIf(entry -> Equipment.disabledRecipe(entry.getKey(), entry.getValue()));
        return filtered;
    }
}
