package local.aquavanilla.mixin;

import com.teammetallurgy.aquaculture.entity.AquaFishingBobberEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {
    @Inject(method = "calculateOpenWater", at = @At("HEAD"), cancellable = true)
    private void aquavanilla$openLava(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        FishingHook self = (FishingHook)(Object)this;
        if (!(self instanceof AquaFishingBobberEntity aqua) || !((AquaBobberAccessor)aqua).aquavanilla$getRod().is(Items.FISHING_ROD) || !self.level().getFluidState(pos).is(FluidTags.LAVA)) return;
        // Match vanilla's 5 x 5, four-layer open-water requirement for lava.
        for (int y = -1; y <= 2; y++) {
            for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
                BlockPos check = pos.offset(x, y, z);
                var state = self.level().getBlockState(check);
                var fluid = state.getFluidState();
                if (y <= 0) {
                    if (!fluid.is(FluidTags.LAVA) || !fluid.isSource() || !state.getCollisionShape(self.level(), check).isEmpty()) { cir.setReturnValue(false); return; }
                } else if (!state.isAir()) { cir.setReturnValue(false); return; }
            }
        }
        cir.setReturnValue(true);
    }
}
