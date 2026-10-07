package local.aquavanilla.mixin;

import com.teammetallurgy.aquaculture.entity.AquaFishingBobberEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AquaFishingBobberEntity.class)
public interface AquaBobberAccessor {
    @Accessor("fishingRod") ItemStack aquavanilla$getRod();
}
