package logisticspipes.items;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSignBlock;
import logisticspipes.blocks.LogisticsSignTileEntity;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
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
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(!(tile instanceof TileGenericPipe)) {
			return false;
		}
		Pipe pipe = ((TileGenericPipe)tile).pipe;
		if(pipe == null) {
			return false;
		}
		if(!(pipe instanceof CoreRoutedPipe)) {
			itemStack.damageItem(10, player);
			return true;
		}
		if(pipe.logic instanceof BaseLogicCrafting) {
			if(sideinput > 1 && sideinput < 6) {
				int signX = x;
				int signY = y;
				int signZ = z;
				switch(sideinput) {
				case 2:
					signZ--;
					break;
				case 3:
					signZ++;
					break;
				case 4:
					signX--;
					break;
				case 5:
					signX++;
					break;
				}
				if(world.getBlockId(signX, signY, signZ) == 0) {
					if(((PipeItemsCraftingLogistics)pipe).canRegisterSign()) {
						world.setBlockMetadataWithNotify(signX, signY, signZ, LogisticsPipes.logisticsSign.blockID, LogisticsSignBlock.SignBlockID);
						TileEntity tilesign = world.getBlockTileEntity(signX, signY, signZ);
						if(tilesign instanceof LogisticsSignTileEntity) {
							((PipeItemsCraftingLogistics)pipe).addSign((LogisticsSignTileEntity)tilesign, player);
							itemStack.damageItem(1, player);
						} else {
							world.setBlockMetadataWithNotify(signX, signY, signZ, 0, 0);
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
