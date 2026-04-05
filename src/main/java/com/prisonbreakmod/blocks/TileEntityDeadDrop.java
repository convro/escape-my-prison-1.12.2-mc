package com.prisonbreakmod.blocks;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayDeque;
import java.util.Deque;

/** Dead drop — hidden item/note storage point for covert communication. */
public class TileEntityDeadDrop extends TileEntity {

    private final Deque<ItemStack> items = new ArrayDeque<>();
    private static final int MAX_ITEMS = 4;
    private String ownerNpcId = "";

    public boolean deposit(ItemStack stack) {
        if (items.size() >= MAX_ITEMS) return false;
        items.addLast(stack.copy());
        markDirty();
        return true;
    }

    public ItemStack retrieve() {
        if (items.isEmpty()) return ItemStack.EMPTY;
        ItemStack item = items.removeFirst();
        markDirty();
        return item;
    }

    public boolean isEmpty() { return items.isEmpty(); }
    public int size() { return items.size(); }
    public String getOwnerNpcId() { return ownerNpcId; }
    public void setOwnerNpcId(String id) { this.ownerNpcId = id; markDirty(); }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        items.clear();
        ownerNpcId = compound.getString("owner");
        NBTTagList list = compound.getTagList("items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack s = new ItemStack(list.getCompoundTagAt(i));
            if (!s.isEmpty()) items.addLast(s);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("owner", ownerNpcId);
        NBTTagList list = new NBTTagList();
        for (ItemStack s : items) {
            NBTTagCompound tag = new NBTTagCompound();
            s.writeToNBT(tag);
            list.appendTag(tag);
        }
        compound.setTag("items", list);
        return compound;
    }
}
