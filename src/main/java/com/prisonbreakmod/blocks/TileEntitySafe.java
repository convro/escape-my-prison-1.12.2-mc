package com.prisonbreakmod.blocks;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

/** Director's safe — contains Master Key. Requires Picklock Mk2 to open. */
public class TileEntitySafe extends TileEntity {

    private final List<ItemStack> contents = new ArrayList<>();
    private boolean locked = true;
    private int pickUseCount = 0; // Mk2 has 3 uses

    public boolean isLocked() { return locked; }

    public boolean tryUnlock(int pickTier) {
        if (pickTier < 2) return false;
        pickUseCount++;
        if (pickUseCount >= 3) {
            locked = false;
            markDirty();
            return true;
        }
        return false;
    }

    public void forceUnlock() {
        locked = false;
        markDirty();
    }

    public void addItem(ItemStack stack) {
        contents.add(stack.copy());
        markDirty();
    }

    public List<ItemStack> getContents() { return contents; }

    public ItemStack takeItem(int index) {
        if (index < 0 || index >= contents.size()) return ItemStack.EMPTY;
        ItemStack item = contents.remove(index);
        markDirty();
        return item;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        locked = compound.getBoolean("locked");
        pickUseCount = compound.getInteger("pickUses");
        contents.clear();
        NBTTagList list = compound.getTagList("contents", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack s = new ItemStack(list.getCompoundTagAt(i));
            if (!s.isEmpty()) contents.add(s);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("locked", locked);
        compound.setInteger("pickUses", pickUseCount);
        NBTTagList list = new NBTTagList();
        for (ItemStack s : contents) {
            NBTTagCompound tag = new NBTTagCompound();
            s.writeToNBT(tag);
            list.appendTag(tag);
        }
        compound.setTag("contents", list);
        return compound;
    }
}
