package local.aquavanilla.mixin;

import com.teammetallurgy.aquaculture.item.AquaFishingRodItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AquaFishingRodItem.class)
public abstract class AquaRodMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void aquavanilla$disabled(Level world, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(hand)));
    }
}
