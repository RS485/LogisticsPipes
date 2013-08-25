package logisticspipes.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.ticks.VersionChecker;
import logisticspipes.utils.PlayerCollectionList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.ChunkWatchEvent.UnWatch;
import net.minecraftforge.event.world.ChunkWatchEvent.Watch;
import net.minecraftforge.event.world.WorldEvent;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsEventListener implements IPlayerTracker {
	
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
		if (event.map.textureType == 1) {
			LogisticsPipes.textures.registerItemIcons(event.map);
		}
		if (event.map.textureType == 0) {
			LogisticsPipes.textures.registerBlockIcons(event.map);
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
							event.entityPlayer.sendChatToPlayer(ChatMessageComponent.func_111066_d("Permission Denied"));
							((TileGenericPipe)tile).scheduleNeighborChange();
							event.entityPlayer.worldObj.markBlockForUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
							((CoreRoutedPipe)((TileGenericPipe)tile).pipe).delayTo = System.currentTimeMillis() + 200;
							((CoreRoutedPipe)((TileGenericPipe)tile).pipe).repeatFor = 10;
						} else {
							((CoreRoutedPipe)((TileGenericPipe)tile).pipe).setDestroyByPlayer();
						}
					}
				}
			}
		}
	}

	public static HashMap<Integer, Long> WorldLoadTime = new HashMap<Integer, Long>();
	
	@ForgeSubscribe
	public void WorldLoad(WorldEvent.Load event) {
		if(MainProxy.isServer(event.world)) {
			int dim = MainProxy.getDimensionForWorld(event.world);
			if(!WorldLoadTime.containsKey(dim)) {
				WorldLoadTime.put(dim, System.currentTimeMillis());
			}
		}
		if(MainProxy.isClient(event.world)) {
			SimpleServiceLocator.routerManager.clearClientRouters();
			LogisticsHUDRenderer.instance().clear();
		}
	}

	@ForgeSubscribe
	public void WorldUnload(WorldEvent.Unload event) {
		if(MainProxy.isServer(event.world)) {
			int dim = MainProxy.getDimensionForWorld(event.world);
			SimpleServiceLocator.routerManager.dimensionUnloaded(dim);
		}
	}

	public static Map<ChunkCoordIntPair, PlayerCollectionList> watcherList = new ConcurrentHashMap<ChunkCoordIntPair, PlayerCollectionList>();

	int taskCount = 0;
	
	@ForgeSubscribe
	public void watchChunk(Watch event) {
		if(!watcherList.containsKey(event.chunk)) {
			watcherList.put(event.chunk, new PlayerCollectionList());
		}
		watcherList.get(event.chunk).add(event.player);
	}
	
	@ForgeSubscribe
	public void unWatchChunk(UnWatch event) {
		if(watcherList.containsKey(event.chunk)) {
			watcherList.get(event.chunk).remove(event.player);
		}
	}
	
	@Override
	public void onPlayerLogin(EntityPlayer player) {
		if(MainProxy.isServer(player.worldObj)) {
			SimpleServiceLocator.securityStationManager.sendClientAuthorizationList(player);
		}
		if(VersionChecker.hasNewVersion) {
			player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Your LogisticsPipes version is outdated. The newest version is #" + VersionChecker.newVersion + "."));
			player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Use \"/logisticspipes changelog\" to see a changelog."));
		}
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {}
}
