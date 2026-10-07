package local.aquavanilla;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public final class RemoveEquipmentModifier extends LootModifier {
    public static final MapCodec<RemoveEquipmentModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance).apply(instance, RemoveEquipmentModifier::new));
    public RemoveEquipmentModifier(LootItemCondition[] conditions) { super(conditions); }
    @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        loot.removeIf(Equipment::hidden);
        loot.forEach(stack -> cleanContents(stack, 0));
        return loot;
    }
    private static void cleanContents(ItemStack stack, int depth) {
        var contents = stack.get(DataComponents.CONTAINER);
        if (contents == null || depth >= 16) return;
        var kept = contents.stream().filter(item -> !Equipment.hidden(item)).map(ItemStack::copy).toList();
        kept.forEach(item -> cleanContents(item, depth + 1));
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(kept));
    }
    @Override public MapCodec<? extends IGlobalLootModifier> codec() { return CODEC; }
}
