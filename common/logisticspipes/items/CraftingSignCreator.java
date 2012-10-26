package logisticspipes.items;

import logisticspipes.blocks.LogisticsSignBlock;
import logisticspipes.blocks.LogisticsSignTileEntity;
import logisticspipes.config.Configs;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.src.CreativeTabs;
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
	public boolean onItemUseFirst(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int sideinput, float hitX, float hitY, float hitZ) 
    {	
		if(MainProxy.isClient(world)) return false;
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
							world.setBlockAndMetadataWithNotify(signX, signY, signZ, Configs.LOGISTICS_SIGN_ID, LogisticsSignBlock.SignBlockID);
							TileEntity tilesign = world.getBlockTileEntity(signX, signY, signZ);
							if(tilesign instanceof LogisticsSignTileEntity) {
								((PipeItemsCraftingLogistics)pipe).addSign((LogisticsSignTileEntity)tilesign, player);
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
								double disX = x + 0.5 - player.posX;
								double disZ = z + 0.5 - player.posZ;
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
										world.setBlockAndMetadataWithNotify(secondSignX, secondSignY, secondSignZ, Configs.LOGISTICS_SIGN_ID, LogisticsSignBlock.SignBlockID);
										TileEntity tilesign = world.getBlockTileEntity(secondSignX, secondSignY, secondSignZ);
										if(tilesign instanceof LogisticsSignTileEntity) {
											((PipeItemsCraftingLogistics)pipe).addSign((LogisticsSignTileEntity)tilesign, player);
											itemStack.damageItem(1, player);
										} else {
											world.setBlockAndMetadataWithNotify(secondSignX, secondSignY, secondSignZ, 0, 0);
										}
									}
								}
								this.onItemUseFirst(itemStack, player ,world, pipechecksignX, pipechecksignY, pipechecksignZ, side + 10, hitX, hitY, hitZ);
							}
						}	
					} else if(selfcalled) {
						double disX = x + 0.5 - player.posX;
						double disZ = z + 0.5 - player.posZ;
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
								world.setBlockAndMetadataWithNotify(secondSignX, secondSignY, secondSignZ, Configs.LOGISTICS_SIGN_ID, LogisticsSignBlock.SignBlockID);
								TileEntity tilesign = world.getBlockTileEntity(secondSignX, secondSignY, secondSignZ);
								if(tilesign instanceof LogisticsSignTileEntity) {
									((PipeItemsCraftingLogistics)pipe).addSign((LogisticsSignTileEntity)tilesign, player);
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

	@Override
	public CreativeTabs getCreativeTab()
    {
        return CreativeTabs.tabTools;
    }
}
