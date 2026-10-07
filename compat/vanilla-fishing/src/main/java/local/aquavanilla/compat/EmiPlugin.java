package local.aquavanilla.compat;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.EmiEntrypoint;
import local.aquavanilla.Equipment;

@EmiEntrypoint
public final class EmiPlugin implements dev.emi.emi.api.EmiPlugin {
    @Override public void register(EmiRegistry registry) { registry.removeEmiStacks(stack -> Equipment.hidden(stack.getItemStack())); }
}
