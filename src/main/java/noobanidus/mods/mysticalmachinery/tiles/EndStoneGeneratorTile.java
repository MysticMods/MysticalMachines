package noobanidus.mods.mysticalmachinery.tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import noobanidus.mods.mysticalmachinery.capability.SettableEnergyStorage;
import noobanidus.mods.mysticalmachinery.init.ModSounds;
import noobanidus.mods.mysticalmachinery.init.ModTiles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EndStoneGeneratorTile extends EnergyTileEntity implements ITickableTileEntity {
  public static final int MAX_FE = 1000000;
  public static final int MAX_FE_XFER = 200;
  public static final int FE_PER_ENDSTONE = 100;
  public static final int ENDSTONE_FREQUENCY = 25;

  public static final long PLAY_THRESHOLD = 12000;

  private long lastPlayed = 0;

  private int stoneAmount;
  private EndStoneHandler stoneHandler = new EndStoneHandler();
  private LazyOptional<IItemHandler> stoneCapability = LazyOptional.of(() -> stoneHandler);

  public EndStoneGeneratorTile() {
    super(ModTiles.END_STONE_FABRICATOR.get());
    this.energyStorage = new SettableEnergyStorage(MAX_FE, MAX_FE_XFER);
    this.energyHandler = LazyOptional.of(() -> this.energyStorage);
  }

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
    if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
      return stoneCapability.cast();
    }

    return super.getCapability(cap, side);
  }

  @Override
  public void read(CompoundNBT compound) {
    super.read(compound);
    this.stoneAmount = compound.getInt("StoneAmount");
  }

  @Override
  public CompoundNBT write(CompoundNBT compound) {
    CompoundNBT tag = super.write(compound);
    tag.putInt("StoneAmount", this.stoneAmount);
    return tag;
  }

  public int getAmount() {
    return stoneAmount;
  }

  @Override
  public void tick() {
    if (world == null || world.isBlockPowered(pos) || world.isRemote) {
      return;
    }

    MinecraftServer server = world.getServer();
    if (server == null) {
      return;
    }

    if (server.getTickCounter() % ENDSTONE_FREQUENCY == 0) {
      if (energyStorage.extractEnergy(FE_PER_ENDSTONE, true) == FE_PER_ENDSTONE) {
        stoneAmount++;
        energyStorage.extractEnergy(FE_PER_ENDSTONE, false);
        if (System.currentTimeMillis() - lastPlayed > PLAY_THRESHOLD) {
          world.playSound(null, pos, ModSounds.END_STONE_GENERATE.get(), SoundCategory.BLOCKS, 0.1f, 1f);
          lastPlayed = System.currentTimeMillis();
        }
      }
    }
  }

  public class EndStoneHandler implements IItemHandler {
    @Override
    public int getSlots() {
      return 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
      return new ItemStack(Items.END_STONE, stoneAmount);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
      return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
      if (amount <= stoneAmount) {
        if (!simulate) {
          stoneAmount -= amount;
        }
        return new ItemStack(Items.END_STONE, amount);
      } else {
        return ItemStack.EMPTY;
      }
    }

    @Override
    public int getSlotLimit(int slot) {
      return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
      return false;
    }
  }
}
