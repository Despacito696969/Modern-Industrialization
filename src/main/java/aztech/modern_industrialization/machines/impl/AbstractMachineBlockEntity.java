package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.mixin_impl.WorldRendererGetter;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;

/**
 * A generic machine BlockEntity.
 */
public abstract class AbstractMachineBlockEntity extends LockableContainerBlockEntity implements RenderAttachmentBlockEntity, BlockEntityClientSerializable {
    protected Direction facingDirection;
    protected DefaultedList<ItemStack> inventory;
    protected final int inventorySize;
    protected boolean isActive = false;

    protected AbstractMachineBlockEntity(BlockEntityType<?> blockEntityType, int inventorySize, Direction facingDirection) {
        super(blockEntityType);
        this.facingDirection = facingDirection;
        this.inventory = DefaultedList.ofSize(inventorySize, ItemStack.EMPTY);
        this.inventorySize = inventorySize;
    }

    @Override
    public int size() {
        return inventorySize;
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack stack : inventory) {
            if(!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.inventory, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if(stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        facingDirection = Direction.byId(tag.getInt("facingDirection"));
        Inventories.fromTag(tag, this.inventory);
        isActive = tag.getBoolean("isActive");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putInt("facingDirection", this.facingDirection.getId());
        Inventories.toTag(tag, this.inventory);
        tag.putBoolean("isActive", this.isActive);
        return tag;
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        setFacingDirection(Direction.byId(tag.getInt("facingDirection")));
        this.isActive = tag.getBoolean("isActive");
        ClientWorld clientWorld = (ClientWorld)world;
        WorldRendererGetter wrg = (WorldRendererGetter)clientWorld;
        wrg.modern_industrialization_getWorldRenderer().updateBlock(null, this.pos, null, null, 0);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putInt("facingDirection", this.facingDirection.getId());
        tag.putBoolean("isActive", this.isActive);
        return tag;
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(Direction facingDirection) {
        this.facingDirection = facingDirection;
        markDirty();
        if(!world.isClient) {
            sync();
        }
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public Object getRenderAttachmentData() {
        return new AttachmentData(this);
    }

    public static class AttachmentData {
        public final Direction facingDirection;
        public final boolean isActive;

        public AttachmentData(AbstractMachineBlockEntity blockEntity) {
            this.facingDirection = blockEntity.getFacingDirection();
            this.isActive = blockEntity.isActive;
        }
    }
}
