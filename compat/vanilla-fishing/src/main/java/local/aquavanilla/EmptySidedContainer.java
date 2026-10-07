package local.aquavanilla;

import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;

public final class EmptySidedContainer extends SimpleContainer implements WorldlyContainer {
    public EmptySidedContainer() { super(0); }
    @Override public int[] getSlotsForFace(Direction side) { return new int[0]; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) { return false; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return false; }
}
