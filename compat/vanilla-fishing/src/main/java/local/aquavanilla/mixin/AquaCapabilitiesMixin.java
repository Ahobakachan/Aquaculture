package local.aquavanilla.mixin;

import com.teammetallurgy.aquaculture.Aquaculture;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Aquaculture.class)
public abstract class AquaCapabilitiesMixin {
    @Inject(method = "registerCapabilities", at = @At("HEAD"), cancellable = true)
    private void aquavanilla$noTackleHandlers(RegisterCapabilitiesEvent event, CallbackInfo ci) { ci.cancel(); }
}
