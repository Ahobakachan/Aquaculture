package local.aquavanilla.checks;

import com.google.gson.GsonBuilder;
import com.teammetallurgy.aquaculture.api.AquacultureAPI;
import com.teammetallurgy.aquaculture.entity.AquaFishingBobberEntity;
import com.teammetallurgy.aquaculture.init.AquaDataComponents;
import com.teammetallurgy.aquaculture.init.AquaEntities;
import com.teammetallurgy.aquaculture.init.AquaBlocks;
import com.teammetallurgy.aquaculture.item.crafting.FishFilletRecipe;
import com.teammetallurgy.aquaculture.block.WormFarmBlock;
import io.netty.buffer.Unpooled;
import local.aquavanilla.Equipment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.Direction;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Tests packaged production jars, actual loaded recipes, biome predicates and fishing entities. */
@Mod("aquavanilla_checks")
public final class FishingChecks {
    private final Map<String,Object> report = new LinkedHashMap<>();
    public FishingChecks() { NeoForge.EVENT_BUS.addListener(this::started); }
    private void started(ServerStartedEvent event) {
        var server = event.getServer();
        server.execute(() -> {
            try {
                require(ModList.get().isLoaded("aquavanilla") && ModList.get().isLoaded("lavafishing"), "Production jars missing");
                report.put("farmerDelight", ModList.get().isLoaded("farmersdelight"));
                recipes(server);
                equipment(server.overworld());
                knives(server.overworld());
                var water = lootBiomes(server, server.overworld(), false,
                        List.of("river", "cold_ocean", "ocean", "desert", "jungle", "mushroom_fields", "swamp"));
                require(water.contains("aquaculture:minnow"), "Minnow was removed with bait");
                require(water.contains("aquaculture:atlantic_cod") && water.contains("aquaculture:arapaima") && water.contains("aquaculture:red_shrooma"), "Biome species missing");
                lootBiomes(server, server.overworld(), true, List.of("plains"));
                var lava = lootBiomes(server, server.getLevel(Level.NETHER), true,
                        List.of("nether_wastes", "soul_sand_valley", "crimson_forest", "warped_forest", "basalt_deltas"));
                Set<String> allLava = Set.of("flame_squat_lobster", "obsidian_sword_fish", "steam_flying_fish", "agni_fish", "arowana_fish", "quartz_fish", "scaly_foot_snail", "yeti_crab", "lava_lamprey");
                require(allLava.stream().allMatch(fish -> lava.contains("lavafishing:" + fish)), "Not all nine lava species are catchable: " + lava);
                report.put("biomeLootDraws", 15600);
                naturalCatch(server.overworld(), false, InteractionHand.MAIN_HAND);
                naturalCatch(server.overworld(), true, InteractionHand.OFF_HAND);
                naturalCatch(server.getLevel(Level.NETHER), true, InteractionHand.MAIN_HAND);
                reload(server);
                report.put("success", true);
            } catch (Throwable error) {
                report.put("success", false);
                report.put("error", error.toString());
                error.printStackTrace();
            } finally {
                try {
                    Files.createDirectories(Path.of("../build"));
                    Files.writeString(Path.of("../build/fishing-report-" + (ModList.get().isLoaded("farmersdelight") ? "with-fd" : "without-fd") + ".json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
                } catch (Exception error) { throw new RuntimeException(error); }
                server.halt(false);
            }
        });
    }
    private void recipes(MinecraftServer server) {
        int total = 0;
        for (var holder : server.getRecipeManager().getRecipes()) {
            require(!Equipment.hidden(holder.value().getResultItem(server.registryAccess())), "Disabled recipe result: " + holder.id());
            require(!Equipment.hidden(holder.id()), "Disabled recipe ID: " + holder.id());
            total++;
        }
        require(server.getRecipeManager().byKey(ResourceLocation.parse("minecraft:fishing_rod")).isPresent(), "Vanilla rod recipe missing");
        require(server.getRecipeManager().byKey(ResourceLocation.parse("aquaculture:neptunium_pickaxe")).isPresent(), "Unrelated Aquaculture tool removed");
        require(server.getRecipeManager().getRecipes().stream().anyMatch(h -> h.value() instanceof FishFilletRecipe), "Fish fillet recipe removed");
        report.put("recipeCount", total);
    }
    private void equipment(ServerLevel level) {
        var player = FakePlayerFactory.getMinecraft(level);
        int disabled = 0;
        for (var item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (!Equipment.disabled(stack)) continue;
            disabled++;
            if (BuiltInRegistries.ITEM.getKey(item).getPath().endsWith("_fishing_rod")) {
                player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                require(item.use(level, player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.FAIL, "Old rod still usable");
                require(stack.getCapability(Capabilities.ItemHandler.ITEM) == null, "Old rod still has equipment slots");
            }
        }
        require(BuiltInRegistries.ITEM.stream().filter(item -> item instanceof com.teammetallurgy.aquaculture.item.AquaFishingRodItem || item instanceof com.teammetallurgy.aquaculture.item.HookItem)
                .allMatch(item -> Equipment.disabled(new ItemStack(item))), "A mod rod or hook escaped removal");
        var worm = (WormFarmBlock)AquaBlocks.WORM_FARM.get();
        for (int compost : List.of(0, 6, 8)) require(worm.getContainer(worm.defaultBlockState().setValue(ComposterBlock.LEVEL, compost), level, BlockPos.ZERO).getContainerSize() == 0, "Worm automation remains enabled");
        BlockPos box = new BlockPos(-8, 82, -2);
        level.setBlockAndUpdate(box, AquaBlocks.TACKLE_BOX.get().defaultBlockState());
        var click = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, box, new BlockHitResult(box.getCenter(), Direction.UP, box, false));
        NeoForge.EVENT_BUS.post(click);
        require(click.isCanceled(), "Tackle box opens");
        require(level.getCapability(Capabilities.ItemHandler.BLOCK, box, Direction.UP) == null, "Tackle hopper automation enabled");
        level.setBlockAndUpdate(box, Blocks.AIR.defaultBlockState());
        CreativeModeTabs.tryRebuildTabContents(level.enabledFeatures(), true, level.registryAccess());
        for (var tab : CreativeModeTabs.allTabs()) {
            require(tab.getDisplayItems().stream().noneMatch(Equipment::hidden), "Equipment in creative tab");
            require(tab.getSearchTabDisplayItems().stream().noneMatch(Equipment::hidden), "Equipment in creative search");
        }
        report.put("disabledEquipment", disabled);
        report.put("creativeAndAutomation", true);
    }
    private void knives(ServerLevel level) {
        if (!ModList.get().isLoaded("farmersdelight")) { report.put("optionalKnifeTagsLoadWithoutFD", true); return; }
        var recipe = new FishFilletRecipe(CraftingBookCategory.MISC);
        int cases = 0;
        for (String knifeName : List.of("flint_knife", "iron_knife", "golden_knife", "diamond_knife", "netherite_knife")) {
            for (var fish : BuiltInRegistries.ITEM) {
                var id = BuiltInRegistries.ITEM.getKey(fish);
                if (!(id.getNamespace().equals("aquaculture") || id.getNamespace().equals("lavafishing")) || !AquacultureAPI.FISH_DATA.hasFilletAmount(fish)) continue;
                var knife = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("farmersdelight:" + knifeName)));
                var fishStack = new ItemStack(fish);
                var input = CraftingInput.of(2, 1, List.of(fishStack, knife));
                require(recipe.matches(input, level), "FD knife cannot fillet " + id);
                var result = recipe.assemble(input, level.registryAccess());
                require(result.getCount() == AquacultureAPI.FISH_DATA.getFilletAmount(fish), "Fillet yield changed");
                var remainder = recipe.getRemainingItems(input).get(1);
                require(remainder.is(knife.getItem()) && remainder.getDamageValue() == 1, "Knife consumed / not worn");
                cases++;
            }
        }
        require(cases >= 150, "Too few knife/fish combinations: " + cases);
        report.put("farmerDelightKnifeFishCombinations", cases);
    }
    private void basin(ServerLevel level, boolean lava) {
        for (int x = -1; x <= 0; x++) for (int z = -1; z <= 0; z++) level.getChunk(x, z);
        // Empty the entire previous basin first: replacing water piecemeal with lava creates obsidian.
        for (int x = -16; x <= 0; x++) for (int z = -16; z <= 0; z++) {
            for (int y = 79; y <= 85; y++) level.setBlock(new BlockPos(x,y,z), Blocks.AIR.defaultBlockState(), 2);
        }
        for (int x = -16; x <= 0; x++) for (int z = -16; z <= 0; z++) for (int y = 79; y <= 80; y++)
            level.setBlock(new BlockPos(x,y,z), (lava ? Blocks.LAVA : Blocks.WATER).defaultBlockState(), 2);
        require(level.getFluidState(new BlockPos(-9,80,-9)).is(lava ? net.minecraft.tags.FluidTags.LAVA : net.minecraft.tags.FluidTags.WATER), "Test basin fluid wrong");
    }
    private AquaFishingBobberEntity cast(ServerLevel level, InteractionHand hand) {
        var player = FakePlayerFactory.getMinecraft(level);
        if (player.fishing != null) player.fishing.discard();
        player.setPos(-8.5, 83, -3.5);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        player.setItemInHand(hand, new ItemStack(Items.FISHING_ROD));
        Items.FISHING_ROD.use(level, player, hand);
        require(player.fishing instanceof AquaFishingBobberEntity, "Vanilla rod did not cast Aquaculture bobber");
        var bobber = (AquaFishingBobberEntity)player.fishing;
        bobber.setPos(-8.5, 80.8, -8.5);
        bobber.setDeltaMovement(0,0,0);
        return bobber;
    }
    private Set<String> lootBiomes(MinecraftServer server, ServerLevel level, boolean lava, List<String> biomes) throws Exception {
        basin(level, lava);
        Set<String> union = new TreeSet<>();
        Map<String,Object> catches = new LinkedHashMap<>();
        Method getLoot = AquaFishingBobberEntity.class.getDeclaredMethod("getLoot", LootParams.class, ServerLevel.class);
        getLoot.setAccessible(true);
        for (String biome : biomes) {
            var source = server.createCommandSourceStack().withLevel(level).withPermission(4).withSuppressedOutput();
            int changed = server.getCommands().getDispatcher().execute("fillbiome -16 76 -16 0 84 0 minecraft:" + biome, source);
            require(changed > 0, "Biome test area not filled");
            var bobber = cast(level, InteractionHand.MAIN_HAND);
            var player = FakePlayerFactory.getMinecraft(level);
            var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, bobber.position()).withParameter(LootContextParams.TOOL, player.getMainHandItem()).withParameter(LootContextParams.THIS_ENTITY, bobber).withLuck(0).create(LootContextParamSets.FISHING);
            Set<String> found = new TreeSet<>();
            for (int i = 0; i < 1200; i++) {
                @SuppressWarnings("unchecked") List<ItemStack> loot = (List<ItemStack>)getLoot.invoke(bobber, params, level);
                require(!loot.isEmpty(), "Empty catch in " + biome);
                for (var stack : loot) {
                    requireCleanLoot(stack, 0);
                    found.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                }
            }
            if (!lava && biome.equals("river")) require(!found.contains("aquaculture:atlantic_cod") && found.contains("aquaculture:minnow"), "Ocean fish incorrectly in river");
            if (!lava && biome.equals("cold_ocean")) require(found.contains("aquaculture:atlantic_cod") && !found.contains("aquaculture:arapaima"), "Cold ocean biome rules broken");
            if (lava && biome.equals("nether_wastes")) require(!found.contains("lavafishing:steam_flying_fish") && !found.contains("lavafishing:agni_fish"), "Nether species ignore biome");
            if (lava && biome.equals("soul_sand_valley")) require(found.contains("lavafishing:steam_flying_fish"), "Soul valley fish missing");
            if (lava && biome.equals("basalt_deltas")) require(found.contains("lavafishing:yeti_crab") && found.contains("lavafishing:scaly_foot_snail"), "Basalt species missing");
            union.addAll(found);
            catches.put(biome, found);
            bobber.discard();
        }
        report.put((lava ? "lava" : "water") + "-" + level.dimension().location(), catches);
        return union;
    }
    private void naturalCatch(ServerLevel level, boolean lava, InteractionHand hand) throws Exception {
        basin(level, lava);
        var player = FakePlayerFactory.getMinecraft(level);
        var bobber = cast(level, hand);
        Field nibble = FishingHook.class.getDeclaredField("nibble");
        nibble.setAccessible(true);
        int ticks = 0;
        while (nibble.getInt(bobber) == 0 && ticks++ < 1800) {
            level.getServer().getWorldData().overworldData().setGameTime(level.getGameTime()+1);
            bobber.tick();
            require(!bobber.isRemoved(), "Bobber vanished after " + ticks + " ticks at " + bobber.position() + " in " + level.getBlockState(bobber.blockPosition()));
        }
        require(nibble.getInt(bobber) > 0, "No natural bite in " + (lava ? "lava" : "water"));
        Method open = FishingHook.class.getDeclaredMethod("calculateOpenWater", BlockPos.class);
        open.setAccessible(true);
        require((Boolean)open.invoke(bobber, new BlockPos(-9,80,-9)), "Open fluid basin not recognized");
        level.setBlockAndUpdate(new BlockPos(-9,81,-9), Blocks.STONE.defaultBlockState());
        require(!(Boolean)open.invoke(bobber, new BlockPos(-9,80,-9)), "Blocked basin counted as open fluid");
        level.setBlockAndUpdate(new BlockPos(-9,81,-9), Blocks.AIR.defaultBlockState());
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
        bobber.writeSpawnData(buffer);
        var copy = new AquaFishingBobberEntity(AquaEntities.BOBBER.get(), level);
        copy.readSpawnData(buffer);
        require(!copy.hasHook() && !copy.hasBobber() && copy.getFishingLine().isEmpty(), "Spawn data introduced equipment");
        buffer.release();
        int before = level.getEntitiesOfClass(ItemEntity.class, new AABB(-20,75,-20,5,90,5)).size();
        Items.FISHING_ROD.use(level, player, hand);
        var dropped = level.getEntitiesOfClass(ItemEntity.class, new AABB(-20,75,-20,5,90,5));
        require(player.fishing == null && bobber.isRemoved(), "Retrieve didn't remove bobber");
        require(player.getItemInHand(hand).getDamageValue() == 1, "Vanilla rod wear wrong");
        require(dropped.size() > before, "Retrieve didn't spawn actual loot");
        for (var item : dropped) {
            require(!Equipment.hidden(item.getItem()), "Removed equipment retrieved");
            if (lava) { item.lavaHurt(); require(item.isAlive(), "Lava catch burned"); }
        }
        report.put("natural-" + (lava ? "lava" : "water") + "-" + level.dimension().location() + "-" + hand, Map.of("ticksToBite",ticks,"rodDamage",1,"spawnedLoot",true,"spawnData",true));
    }
    private void reload(MinecraftServer server) {
        // Recipe filtering must survive a full server datapack reload, not only the first startup.
        var future = server.reloadResources(server.getPackRepository().getSelectedIds());
        server.managedBlock(future::isDone);
        future.join();
        recipes(server);
        report.put("datapackReload", true);
    }
    private static void requireCleanLoot(ItemStack stack, int depth) {
        require(!Equipment.hidden(stack), "Disabled equipment fished (including inside treasure containers)");
        var contents = stack.get(DataComponents.CONTAINER);
        if (contents != null && depth < 16) contents.stream().forEach(item -> requireCleanLoot(item, depth + 1));
    }
    private static void require(boolean pass, String message) { if (!pass) throw new AssertionError(message); }
}
