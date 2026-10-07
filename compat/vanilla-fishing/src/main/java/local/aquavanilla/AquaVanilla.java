package local.aquavanilla;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.ArrayList;

@Mod(AquaVanilla.ID)
public final class AquaVanilla {
    public static final String ID = "aquavanilla";
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ID);
    static { LOOT.register("remove_equipment", () -> RemoveEquipmentModifier.CODEC); }
    public AquaVanilla(IEventBus bus) {
        LOOT.register(bus);
        bus.addListener(EventPriority.LOWEST, this::creative);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::interact);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::rightClick);
    }
    private void creative(BuildCreativeModeTabContentsEvent event) {
        var stacks = new ArrayList<>(event.getParentEntries());
        stacks.addAll(event.getSearchEntries());
        for (var stack : stacks) if (Equipment.hidden(stack)) event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }
    private void interact(PlayerInteractEvent.RightClickBlock event) {
        if (Equipment.disabled(event.getItemStack()) || Equipment.disabled(event.getLevel().getBlockState(event.getPos()).getBlock())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }
    private void rightClick(PlayerInteractEvent.RightClickItem event) {
        if (Equipment.disabled(event.getItemStack())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }
}
