package noobanidus.mods.mysticalmachinery.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import noobanidus.mods.mysticalmachinery.capability.SettableEnergyStorage;
import noobanidus.mods.mysticalmachinery.tiles.BlockGeneratorTile;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class BlockGeneratorBlock extends Block {
  private final Supplier<? extends Block> block;
  private final int MAX_FE;
  private final int MAX_FE_TRANSFER;
  private final int FE_OPERATION;
  private final int FREQUENCY;

  public BlockGeneratorBlock(Properties properties, Supplier<? extends Block> block, int MAX_FE, int MAX_FE_TRANSFER, int FE_OPERATION, int FREQUENCY) {
    super(properties);
    this.block = block;
    this.MAX_FE = MAX_FE;
    this.MAX_FE_TRANSFER = MAX_FE_TRANSFER;
    this.FE_OPERATION = FE_OPERATION;
    this.FREQUENCY = FREQUENCY;
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new BlockGeneratorTile(block, MAX_FE, MAX_FE_TRANSFER, FE_OPERATION, FREQUENCY);
  }

  @Override
  public BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT;
  }

  public static long lastSentMessage = 0;

  @Override
  public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
    if (!worldIn.isRemote) {
      TileEntity te = worldIn.getTileEntity(pos);
      if (te instanceof BlockGeneratorTile) {
        if (System.currentTimeMillis() - lastSentMessage > 10) {
          SettableEnergyStorage energy = ((BlockGeneratorTile) te).getEnergyStorage();
          ItemStack type = ((BlockGeneratorTile) te).getItemType();
          int amount = ((BlockGeneratorTile) te).getAmount();
          player.sendMessage(new TranslationTextComponent("mysticalmachinery.tile.block_generator.contains", amount, type.getDisplayName(), energy.getEnergyStored(), energy.getMaxEnergyStored()));
          lastSentMessage = System.currentTimeMillis();
          return true;
        }
      }
    }
    return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
  }
}
