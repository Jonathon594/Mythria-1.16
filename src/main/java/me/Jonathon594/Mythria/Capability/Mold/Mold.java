package me.Jonathon594.Mythria.Capability.Mold;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.ItemStackHandler;

public class Mold implements IMold {
    final ItemStackHandler inventory = new ItemStackHandler(2);

    @Override
    public void fromNBT(CompoundNBT nbt) {
        inventory.deserializeNBT(nbt.getCompound("inventory"));
    }

    @Override
    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("inventory", inventory.serializeNBT());
        return nbt;
    }

    public ItemStack getOriginalMoldStack() {
        return getInventory().getStackInSlot(1);
    }

    public void setOriginalMoldStack(ItemStack moldStack) {
        inventory.setStackInSlot(1, moldStack);
    }

    public ItemStack getResultStack() {
        return getInventory().getStackInSlot(0);
    }

    public void setResultStack(ItemStack resultStack) {
        inventory.setStackInSlot(0, resultStack);
    }
}
