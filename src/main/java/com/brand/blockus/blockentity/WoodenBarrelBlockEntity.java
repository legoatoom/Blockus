package com.brand.blockus.blockentity;

import com.brand.blockus.content.Barrels;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChestStateManager;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class WoodenBarrelBlockEntity extends LootableContainerBlockEntity {
    private final ChestStateManager stateManager;
    private DefaultedList<ItemStack> inventory;

    public WoodenBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(Barrels.WOODEN_BARREL, pos, state);
        this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
        this.stateManager = new ChestStateManager() {
            protected void onChestOpened(World world, BlockPos pos, BlockState state) {
                WoodenBarrelBlockEntity.this.playSound(state, SoundEvents.BLOCK_BARREL_OPEN);
                WoodenBarrelBlockEntity.this.setOpen(state, true);
            }

            protected void onChestClosed(World world, BlockPos pos, BlockState state) {
                WoodenBarrelBlockEntity.this.playSound(state, SoundEvents.BLOCK_BARREL_CLOSE);
                WoodenBarrelBlockEntity.this.setOpen(state, false);
            }

            protected void onInteracted(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
            }

            protected boolean isPlayerViewing(PlayerEntity player) {
                if (player.currentScreenHandler instanceof GenericContainerScreenHandler) {
                    Inventory inventory = ((GenericContainerScreenHandler) player.currentScreenHandler).getInventory();
                    return inventory == WoodenBarrelBlockEntity.this;
                } else {
                    return false;
                }
            }
        };
    }

    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        if (!this.serializeLootTable(tag)) {
            Inventories.toTag(tag, this.inventory);
        }
        return tag;
    }

    public void fromTag(CompoundTag compoundTag) {
        super.fromTag(compoundTag);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(compoundTag)) {
            Inventories.fromTag(compoundTag, this.inventory);
        }

    }

    public int size() {
        return 27;
    }

    protected DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }

    protected Text getContainerName() {
        return new TranslatableText("container.barrel", new Object[0]);
    }

    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, this);
    }

    public void onOpen(PlayerEntity player) {
        if (!player.isSpectator()) {
            this.stateManager.openChest(this.getWorld(), this.getPos(), this.getCachedState());
        }

    }

    public void onClose(PlayerEntity player) {
        if (!player.isSpectator()) {
            this.stateManager.closeChest(this.getWorld(), this.getPos(), this.getCachedState());
        }

    }

    public void tick() {
        this.stateManager.updateViewerCount(this.getWorld(), this.getPos(), this.getCachedState());
    }

    private void setOpen(BlockState state, boolean open) {
        this.world.setBlockState(this.getPos(), state.with(BarrelBlock.OPEN, open), 3);
    }

    private void playSound(BlockState state, SoundEvent soundEvent) {
        Vec3i vec3i = state.get(BarrelBlock.FACING).getVector();
        double d = this.pos.getX() + 0.5D + vec3i.getX() / 2.0D;
        double e = this.pos.getY() + 0.5D + vec3i.getY() / 2.0D;
        double f = this.pos.getZ() + 0.5D + vec3i.getZ() / 2.0D;
        this.world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
    }
}
