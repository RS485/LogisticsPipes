package logisticspipes.items;

import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;

public class CraftingSignCreator extends LogisticsItem {

	public CraftingSignCreator(int i) {
		super(i);
		this.setMaxStackSize(1);
		this.setMaxDamage(250);
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
		if(pipe instanceof PipeItemsCraftingLogistics) {
			ForgeDirection dir = ForgeDirection.getOrientation(sideinput);
			if(dir == ForgeDirection.UNKNOWN) return false;
			if(!player.isSneaking()) {
				if(((PipeItemsCraftingLogistics)pipe).setCraftingSign(dir, true, player)) {
					itemStack.damageItem(1, player);
				}
			} else {
				if(((PipeItemsCraftingLogistics)pipe).setCraftingSign(dir, false, player)) {
					itemStack.damageItem(-1, player);
				}
			}
		}
        return true;
    }

	@Override
	public CreativeTabs getCreativeTab()
    {
        return CreativeTabs.tabTools;
    }
}
