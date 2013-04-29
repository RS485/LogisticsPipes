package logisticspipes.main;

import java.io.IOException;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsEventListener {
	
	@ForgeSubscribe
	public void onEntitySpawn(EntityJoinWorldEvent event) {
		if(event != null && event.entity instanceof EntityItem && event.entity.worldObj != null && !event.entity.worldObj.isRemote) {
			ItemStack stack = ((EntityItem)event.entity).getEntityItem(); //Get ItemStack
			if(stack != null && stack.getItem() instanceof IItemAdvancedExistance && !((IItemAdvancedExistance)stack.getItem()).canExistInWorld(stack)) {
				event.setCanceled(true);
			}
		}
	}
	
	/*
	 * subscribe forge pre stich event to register common texture
	 */
	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Pre event) throws IOException{
		if (event.map == Minecraft.getMinecraft().renderEngine.textureMapItems) {
			LogisticsPipes.textures.registerItemIcons(event.map);
		}
		if (event.map == Minecraft.getMinecraft().renderEngine.textureMapBlocks) {
			LogisticsPipes.textures.registerBlockIcons();
		}
	}
	
	@ForgeSubscribe
	public void onPlayerInteract(final PlayerInteractEvent event) {
		if(MainProxy.isServer(event.entityPlayer.worldObj)) {
			if(event.action == Action.LEFT_CLICK_BLOCK) {
				final TileEntity tile = event.entityPlayer.worldObj.getBlockTileEntity(event.x, event.y, event.z);
				if(tile instanceof TileGenericPipe) {
					if(((TileGenericPipe)tile).pipe instanceof CoreRoutedPipe) {
						if(!((CoreRoutedPipe)((TileGenericPipe)tile).pipe).canBeDestroyedByPlayer(event.entityPlayer)) {
							event.setCanceled(true);
							event.entityPlayer.sendChatToPlayer("Permission Denied");
							((TileGenericPipe)tile).scheduleNeighborChange();
							event.entityPlayer.worldObj.markBlockForUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
							((CoreRoutedPipe)((TileGenericPipe)tile).pipe).delayTo = System.currentTimeMillis() + 200;
							((CoreRoutedPipe)((TileGenericPipe)tile).pipe).repeatFor = 10;
						}
					}
				}
			}
		}
	}
}
