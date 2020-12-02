package com.infinityraider.infinitylib.utility.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Simple class to wrap an IItemHandler as an IInventory while maintaining IItemHandler functionality
 */
public class InventoryItemHandler implements IInventoryItemHandler {
    private final IItemHandler itemHandler;

    public InventoryItemHandler(IItemHandler itemHandler) {
        this.itemHandler = itemHandler;
    }

    public IItemHandler getItemHandler() {
        return this.itemHandler;
    }

    /**
     * --------------------
     * IItemHandler methods
     * --------------------
     */

    @Override
    public int getSlots() {
        return this.getItemHandler().getSlots();
    }

    @Nullable
    @Override
    public ItemStack getStackInSlot(int index) {
        return this.getItemHandler().getStackInSlot(index);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return this.getItemHandler().insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.getItemHandler().extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.getItemHandler().getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return this.getItemHandler().isItemValid(slot, stack);
    }


    /**
     * ------------------
     * IInventory methods
     * ------------------
     */

    @Override
    public int getSizeInventory() {
        return this.getSlots();
    }

    @Nullable
    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = this.getStackInSlot(index);
        if(stack != null) {
            stack = this.extractItem(index, count, false);
        }
        return stack;
    }

    @Nullable
    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = this.getStackInSlot(index);
        if(stack != null) {
            stack = this.extractItem(index, stack.getCount(), false);
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
        ItemStack inSlot = this.getStackInSlot(index);
        if(inSlot != null) {
            this.extractItem(index, inSlot.getCount(), false);
        }
        this.insertItem(index, stack, false);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }

    @Override
    public void openInventory(PlayerEntity player) {}

    @Override
    public void closeInventory(PlayerEntity player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        ItemStack simulated = this.insertItem(index, stack, true);
        return simulated.getCount() != stack.getCount();
    }

    @Override
    public void clear() {
        for(int i = 0; i < this.getSizeInventory(); i++) {
            this.setInventorySlotContents(i, ItemStack.EMPTY);
        }
    }
}
