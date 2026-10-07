package local.aquavanilla.mixin;

import com.teammetallurgy.aquaculture.api.fishing.Hooks;
import com.teammetallurgy.aquaculture.entity.AquaFishingBobberEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingRodItem.class)
public abstract class FishingRodMixin {
    @Redirect(method = "use", at = @At(value = "NEW", target = "(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)Lnet/minecraft/world/entity/projectile/FishingHook;"))
    private FishingHook aquavanilla$cast(Player owner, Level world, int luck, int lure, Level level, Player player, InteractionHand hand) {
        ItemStack rod = player.getItemInHand(hand);
        if (!rod.is(Items.FISHING_ROD)) return new FishingHook(owner, world, luck, lure);
        return new AquaFishingBobberEntity(owner, world, luck, lure, Hooks.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, rod);
    }
}

