package local.aquavanilla.mixin;

import com.teammetallurgy.aquaculture.block.WormFarmBlock;
import local.aquavanilla.EmptySidedContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WormFarmBlock.class)
public abstract class WormFarmMixin {
    @Inject(method = "getContainer", at = @At("HEAD"), cancellable = true)
    private void aquavanilla$noWormAutomation(BlockState state, LevelAccessor level, BlockPos pos, CallbackInfoReturnable<WorldlyContainer> cir) {
        cir.setReturnValue(new EmptySidedContainer());
    }
}
