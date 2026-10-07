package local.aquavanilla.mixin;

import com.teammetallurgy.aquaculture.entity.AquaFishingBobberEntity;
import com.teammetallurgy.aquaculture.init.AquaLootTables;
import local.aquavanilla.AquaVanilla;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(AquaFishingBobberEntity.class)
public abstract class AquaBobberMixin extends FishingHook {
    @Shadow private ItemStack fishingRod;
    @Invoker("lavaFishingTick") protected abstract void aquavanilla$lavaTick();
    protected AquaBobberMixin(EntityType<? extends FishingHook> type, Level level) { super(type, level); }
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void aquavanilla$tick(CallbackInfo ci) {
        if (fishingRod != null && fishingRod.is(Items.FISHING_ROD) && !level().getFluidState(blockPosition()).is(FluidTags.WATER)) {
            aquavanilla$lavaTick();
            ci.cancel();
        }
    }
    @Inject(method = "lavaHurt", at = @At("HEAD"), cancellable = true)
    private void aquavanilla$fireproof(CallbackInfo ci) { if (fishingRod != null && fishingRod.is(Items.FISHING_ROD)) ci.cancel(); }
    @Inject(method = "isLavaHookInLava", at = @At("HEAD"), cancellable = true)
    private void aquavanilla$fluid(AquaFishingBobberEntity bobber, Level world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (fishingRod != null && fishingRod.is(Items.FISHING_ROD)) cir.setReturnValue(world.getFluidState(pos).is(FluidTags.LAVA));
    }
    @Inject(method = "getLoot", at = @At("HEAD"), cancellable = true)
    private void aquavanilla$loot(LootParams params, ServerLevel level, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (fishingRod == null || !fishingRod.is(Items.FISHING_ROD)) return;
        ResourceKey<LootTable> key = BuiltInLootTables.FISHING;
        if (level.getFluidState(blockPosition()).is(FluidTags.LAVA)) {
            key = level.dimensionType().hasCeiling() ? AquaLootTables.NETHER_FISHING : AquaLootTables.LAVA_FISHING;
        }
        LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
        List<ItemStack> loot = table.getRandomItems(params);
        // Removed bait/equipment entries must not turn a successful bite into an empty catch.
        for (int retry = 0; loot.isEmpty() && retry < 16; retry++) loot = table.getRandomItems(params);
        cir.setReturnValue(loot);
    }
    @Inject(method = "spawnLoot", at = @At("HEAD"), cancellable = true)
    private void aquavanilla$spawnLoot(Player player, List<ItemStack> loot, CallbackInfo ci) {
        if (fishingRod == null || !fishingRod.is(Items.FISHING_ROD)) return;
        boolean lava = level().getFluidState(blockPosition()).is(FluidTags.LAVA);
        for (ItemStack stack : loot) {
            ItemEntity item = lava ? new ItemEntity(level(), getX(), getY(), getZ(), stack) {
                @Override public void lavaHurt() {}
                @Override public boolean displayFireAnimation() { return false; }
                @Override public boolean isInvulnerableTo(DamageSource source) { return source.is(DamageTypeTags.IS_FIRE) || super.isInvulnerableTo(source); }
            } : new ItemEntity(level(), getX(), getY(), getZ(), stack);
            double x = player.getX() - getX(), y = player.getY() - getY(), z = player.getZ() - getZ();
            item.setDeltaMovement(x * 0.1, y * 0.1 + Math.sqrt(Math.sqrt(x*x + y*y + z*z)) * 0.08 + (lava ? 0.2 : 0), z * 0.1);
            level().addFreshEntity(item);
            level().addFreshEntity(new ExperienceOrb(level(), player.getX(), player.getY()+0.5, player.getZ()+0.5, random.nextInt(6)+1));
            if (stack.is(ItemTags.FISHES)) player.awardStat(Stats.FISH_CAUGHT, 1);
        }
        ci.cancel();
    }
}
