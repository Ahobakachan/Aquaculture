package local.aquavanilla.mixin;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "club.redux.sunset.lavafishing.registry.ModItems")
public abstract class LavaCapabilitiesMixin {
    @Inject(method = "onRegisterCapabilities", at = @At("HEAD"), cancellable = true)
    private void aquavanilla$noRodEquipment(RegisterCapabilitiesEvent event, CallbackInfo ci) { ci.cancel(); }
}
