package logisticspipes;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.GuiReopenPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.LogisticsGuiOverrenderer;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.ticks.VersionChecker;
import logisticspipes.utils.PlayerCollectionList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.ChunkWatchEvent.UnWatch;
import net.minecraftforge.event.world.ChunkWatchEvent.Watch;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsEventListener implements IPlayerTracker {
	
	@SubscribeEvent
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
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Pre event) throws IOException{
		if (event.map.textureType == 1) {
			LogisticsPipes.textures.registerItemIcons(event.map);
		}
		if (event.map.textureType == 0) {
			LogisticsPipes.textures.registerBlockIcons(event.map);
		}
	}
	
	@SubscribeEvent
	public void onPlayerInteract(final PlayerInteractEvent event) {
		if(MainProxy.isServer(event.entityPlayer.worldObj)) {
			if(event.action == Action.LEFT_CLICK_BLOCK) {
				final TileEntity tile = event.entityPlayer.worldObj.getTileEntity(event.x, event.y, event.z);
				if(tile instanceof LogisticsTileGenericPipe) {
					if(((LogisticsTileGenericPipe)tile).pipe instanceof CoreRoutedPipe) {
						if(!((CoreRoutedPipe)((LogisticsTileGenericPipe)tile).pipe).canBeDestroyedByPlayer(event.entityPlayer)) {
							event.setCanceled(true);
							event.entityPlayer.addChatMessage(new ChatComponentText("Permission Denied"));
							((LogisticsTileGenericPipe)tile).scheduleNeighborChange();
							event.entityPlayer.worldObj.markBlockForUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
							((CoreRoutedPipe)((LogisticsTileGenericPipe)tile).pipe).delayTo = System.currentTimeMillis() + 200;
							((CoreRoutedPipe)((LogisticsTileGenericPipe)tile).pipe).repeatFor = 10;
						} else {
							((CoreRoutedPipe)((LogisticsTileGenericPipe)tile).pipe).setDestroyByPlayer();
						}
					}
				}
			}
		}
	}

	public static HashMap<Integer, Long> WorldLoadTime = new HashMap<Integer, Long>();
	
	@SubscribeEvent
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

	@SubscribeEvent
	public void WorldUnload(WorldEvent.Unload event) {
		if(MainProxy.isServer(event.world)) {
			int dim = MainProxy.getDimensionForWorld(event.world);
			SimpleServiceLocator.routerManager.dimensionUnloaded(dim);
		}
	}

	public static Map<ChunkCoordIntPair, PlayerCollectionList> watcherList = new ConcurrentHashMap<ChunkCoordIntPair, PlayerCollectionList>();

	int taskCount = 0;
	
	@SubscribeEvent
	public void watchChunk(Watch event) {
		if(!watcherList.containsKey(event.chunk)) {
			watcherList.put(event.chunk, new PlayerCollectionList());
		}
		watcherList.get(event.chunk).add(event.player);
	}
	
	@SubscribeEvent
	public void unWatchChunk(UnWatch event) {
		if(watcherList.containsKey(event.chunk)) {
			watcherList.get(event.chunk).remove(event.player);
		}
	}
	
	@Override
	public void onPlayerLogin(EntityPlayer player) {
		if(MainProxy.isServer(player.worldObj)) {
			SimpleServiceLocator.securityStationManager.sendClientAuthorizationList(player);
			SimpleServiceLocator.craftingPermissionManager.sendCraftingPermissionsToPlayer(player);
		}
		if(VersionChecker.hasNewVersion) {
			player.addChatMessage(new ChatComponentText("Your LogisticsPipes version is outdated. The newest version is #" + VersionChecker.newVersion + "."));
			player.addChatMessage(new ChatComponentText("Use \"/logisticspipes changelog\" to see a changelog."));
		}
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {}

	@AllArgsConstructor
	private static class GuiEntry {
		@Getter
		private final int xCoord;
		@Getter
		private final int yCoord;
		@Getter
		private final int zCoord;
		@Getter
		private final int guiID;
		@Getter @Setter
		private boolean isActive;
	}

	@Getter(lazy=true)
	private static final Queue<GuiEntry> guiPos = new LinkedList<GuiEntry>();

	//Handle GuiRepoen
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onGuiOpen(GuiOpenEvent event) {
		if(!getGuiPos().isEmpty()) {
			if(event.gui == null) {
				GuiEntry part = getGuiPos().peek();
				if(part.isActive()) {
					part = getGuiPos().poll();
					MainProxy.sendPacketToServer(PacketHandler.getPacket(GuiReopenPacket.class).setGuiID(part.getGuiID()).setPosX(part.getXCoord()).setPosY(part.getYCoord()).setPosZ(part.getZCoord()));
					LogisticsGuiOverrenderer.getInstance().setActive(false);
				}
			} else {
				GuiEntry part = getGuiPos().peek();
				part.setActive(true);
			}
		}
		if(event.gui == null) {
			LogisticsGuiOverrenderer.getInstance().setActive(false);
		}
	}

	@SideOnly(Side.CLIENT)
	public static void addGuiToReopen(int xCoord, int yCoord, int zCoord, int guiID) {
		getGuiPos().add(new GuiEntry(xCoord, yCoord, zCoord, guiID, false));
	}
}
