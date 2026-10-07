package local.aquavanilla.compat;

import local.aquavanilla.AquaVanilla;
import local.aquavanilla.Equipment;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@mezz.jei.api.JeiPlugin
public final class JeiPlugin implements IModPlugin {
    @Override public ResourceLocation getPluginUid() { return ResourceLocation.fromNamespaceAndPath(AquaVanilla.ID, "equipment"); }
    @Override public void onRuntimeAvailable(IJeiRuntime runtime) {
        var hidden = BuiltInRegistries.ITEM.stream().map(ItemStack::new).filter(Equipment::hidden).toList();
        runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, hidden);
    }
}
