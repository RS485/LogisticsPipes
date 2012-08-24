package logisticspipes.buildcraft.logisticspipes.items;

import logisticspipes.mod_LogisticsPipes;
import logisticspipes.buildcraft.krapht.LogisticsItem;
import logisticspipes.buildcraft.krapht.RoutedPipe;
import logisticspipes.buildcraft.krapht.logic.BaseLogicCrafting;
import logisticspipes.buildcraft.krapht.pipes.PipeItemsCraftingLogistics;
import logisticspipes.buildcraft.logisticspipes.blocks.LogisticsBlock;
import logisticspipes.buildcraft.logisticspipes.blocks.LogisticsTileEntiy;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;

public class CraftingSignCreator extends LogisticsItem {

	public CraftingSignCreator(int i) {
		super(i);
		this.setMaxStackSize(1);
	}

	@Override
	public boolean onItemUseFirst(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int sideinput)
    {	
		if(itemStack.getItemDamage() > this.getMaxDamage() || itemStack.stackSize == 0) {
			return false;
		}
		int side = sideinput % 10;
		boolean selfcalled = sideinput > 10;
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(!(tile instanceof TileGenericPipe)) {
			return false;
		}
		Pipe pipe = ((TileGenericPipe)tile).pipe;
		if(pipe == null) {
			return false;
		}
		if(!(pipe instanceof RoutedPipe)) {
			itemStack.damageItem(10, player);
			return true;
		}
		if(pipe.logic instanceof BaseLogicCrafting) {
			if(side > 1 && side < 6) {
				int signX = x;
				int signY = y;
				int signZ = z;
				int pipechecksignX = x;
				int pipechecksignY = y;
				int pipechecksignZ = z;
				switch(side) {
				case 2:
					signZ--;
					pipechecksignZ++;
					break;
				case 3:
					signZ++;
					pipechecksignZ--;
					break;
				case 4:
					signX--;
					pipechecksignX++;
					break;
				case 5:
					signX++;
					pipechecksignX--;
					break;
				}
				if(!(world.getBlockTileEntity(pipechecksignX, pipechecksignY, pipechecksignZ) instanceof TileGenericPipe && ((TileGenericPipe)tile).pipe instanceof PipeItemsCraftingLogistics) && !selfcalled) {
					if(world.getBlockId(signX, signY, signZ) == 0) {
						if(((PipeItemsCraftingLogistics)pipe).canRegisterSign()) {
							world.setBlockAndMetadataWithNotify(signX, signY, signZ, mod_LogisticsPipes.LOGISTICS_BLOCK_ID, LogisticsBlock.SignBlockID);
							TileEntity tilesign = world.getBlockTileEntity(signX, signY, signZ);
							if(tilesign instanceof LogisticsTileEntiy) {
								((PipeItemsCraftingLogistics)pipe).addSign((LogisticsTileEntiy)tilesign);
								itemStack.damageItem(1, player);
							} else {
								world.setBlockAndMetadataWithNotify(signX, signY, signZ, 0, 0);
							}
						}
					}
				} else {
					TileEntity secondTile = world.getBlockTileEntity(pipechecksignX, pipechecksignY, pipechecksignZ);
					if(secondTile instanceof TileGenericPipe) {
						Pipe secondpipe = ((TileGenericPipe)secondTile).pipe;
						if(secondpipe != null) {
							if(secondpipe instanceof PipeItemsCraftingLogistics) {
								double disX = x - player.posX;
								double disZ = z - player.posZ;
								int secondSignX = x;
								int secondSignY = y;
								int secondSignZ = z;
								switch(side) {
								case 2:
									if(disX > 0) {
										secondSignX--;
									} else {
										secondSignX++;
									}
									break;
								case 3:
									if(disX > 0) {
										secondSignX--;
									} else {
										secondSignX++;
									}
									break;
								case 4:
									if(disZ > 0) {
										secondSignZ--;
									} else {
										secondSignZ++;
									}
									break;
								case 5:
									if(disZ > 0) {
										secondSignZ--;
									} else {
										secondSignZ++;
									}
									break;
								}
								if(world.getBlockId(secondSignX, secondSignY, secondSignZ) == 0) {
									if(((PipeItemsCraftingLogistics)pipe).canRegisterSign()) {
										world.setBlockAndMetadataWithNotify(secondSignX, secondSignY, secondSignZ, mod_LogisticsPipes.LOGISTICS_BLOCK_ID, LogisticsBlock.SignBlockID);
										TileEntity tilesign = world.getBlockTileEntity(secondSignX, secondSignY, secondSignZ);
										if(tilesign instanceof LogisticsTileEntiy) {
											((PipeItemsCraftingLogistics)pipe).addSign((LogisticsTileEntiy)tilesign);
											itemStack.damageItem(1, player);
										} else {
											world.setBlockAndMetadataWithNotify(secondSignX, secondSignY, secondSignZ, 0, 0);
										}
									}
								}
								this.onItemUseFirst(itemStack, player ,world, pipechecksignX, pipechecksignY, pipechecksignZ, side + 10);
							}
						}	
					} else if(selfcalled) {
						double disX = x - player.posX;
						double disZ = z - player.posZ;
						int secondSignX = x;
						int secondSignY = y;
						int secondSignZ = z;
						switch(side) {
						case 2:
							if(disX > 0) {
								secondSignX--;
							} else {
								secondSignX++;
							}
							break;
						case 3:
							if(disX > 0) {
								secondSignX--;
							} else {
								secondSignX++;
							}
							break;
						case 4:
							if(disZ > 0) {
								secondSignZ--;
							} else {
								secondSignZ++;
							}
							break;
						case 5:
							if(disZ > 0) {
								secondSignZ--;
							} else {
								secondSignZ++;
							}
							break;
						}
						if(world.getBlockId(secondSignX, secondSignY, secondSignZ) == 0) {
							if(((PipeItemsCraftingLogistics)pipe).canRegisterSign()) {
								world.setBlockAndMetadataWithNotify(secondSignX, secondSignY, secondSignZ, mod_LogisticsPipes.LOGISTICS_BLOCK_ID, LogisticsBlock.SignBlockID);
								TileEntity tilesign = world.getBlockTileEntity(secondSignX, secondSignY, secondSignZ);
								if(tilesign instanceof LogisticsTileEntiy) {
									((PipeItemsCraftingLogistics)pipe).addSign((LogisticsTileEntiy)tilesign);
									itemStack.damageItem(1, player);
								} else {
									world.setBlockAndMetadataWithNotify(secondSignX, secondSignY, secondSignZ, 0, 0);
								}
							}
						}
					}
				}
			}
		}
        return true;
    }
	
	@Override
	public int getMaxDamage() {
		return 100;
	}
}
