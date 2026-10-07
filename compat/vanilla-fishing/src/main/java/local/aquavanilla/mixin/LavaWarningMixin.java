package local.aquavanilla.mixin;

import com.teammetallurgy.aquaculture.entity.AquaFishingBobberEntity;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "club.redux.sunset.lavafishing.event.EventFishingHook")
public abstract class LavaWarningMixin {
    @Inject(method = "onEntityTickPost", at = @At("HEAD"), cancellable = true)
    private static void aquavanilla$validFluid(EntityTickEvent.Post event, CallbackInfo ci) {
        if (event.getEntity() instanceof AquaFishingBobberEntity bobber && ((AquaBobberAccessor)bobber).aquavanilla$getRod().is(Items.FISHING_ROD)) ci.cancel();
    }
}
