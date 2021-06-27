package logisticspipes;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkWatchEvent.UnWatch;
import net.minecraftforge.event.world.ChunkWatchEvent.Watch;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import logisticspipes.config.Configs;
import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.PlayerConfigToClientPacket;
import logisticspipes.network.packets.chassis.ChestGuiClosed;
import logisticspipes.network.packets.chassis.ChestGuiOpened;
import logisticspipes.network.packets.gui.GuiReopenPacket;
import logisticspipes.pipes.PipeLogisticsChassis;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.GuiOverlay;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.pathfinder.changedetection.TEControl;
import logisticspipes.ticks.LPTickHandler;
import logisticspipes.ticks.VersionChecker;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.PlayerIdentifier;
import logisticspipes.utils.QuickSortChestMarkerStorage;
import logisticspipes.utils.string.ChatColor;
import network.rs485.logisticspipes.config.ClientConfiguration;
import network.rs485.logisticspipes.config.PlayerConfiguration;
import network.rs485.logisticspipes.connection.NeighborTileEntity;
import network.rs485.logisticspipes.module.AsyncQuicksortModule;
import network.rs485.logisticspipes.util.TextUtil;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

public class LogisticsEventListener {

	public static final WeakHashMap<EntityPlayer, List<WeakReference<AsyncQuicksortModule>>> chestQuickSortConnection = new WeakHashMap<>();
	public static Map<ChunkPos, PlayerCollectionList> watcherList = new ConcurrentHashMap<>();

	@SubscribeEvent
	public void onEntitySpawn(EntityJoinWorldEvent event) {
		if (event != null && event.getEntity() instanceof EntityItem && event.getEntity().world != null && !event.getEntity().world.isRemote) {
			ItemStack stack = ((EntityItem) event.getEntity()).getItem(); //Get ItemStack
			if (!stack.isEmpty() && stack.getItem() instanceof IItemAdvancedExistance && !((IItemAdvancedExistance) stack.getItem()).canExistInWorld(stack)) {
				event.setCanceled(true);
			}
			if (stack.hasTagCompound()) {
				for (String key : Objects.requireNonNull(stack.getTagCompound(), "nbt for stack must be non-null").getKeySet()) {
					if (key.startsWith("logisticspipes:routingdata")) {
						ItemRoutingInformation info = ItemRoutingInformation.restoreFromNBT(stack.getTagCompound().getCompoundTag(key));
						info.setItemTimedout();
						((EntityItem) event.getEntity()).setItem(info.getItem().getItem().makeNormalStack(stack.getCount()));
						break;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLeftClickBlock(final PlayerInteractEvent.LeftClickBlock event) {
		if (MainProxy.isServer(event.getEntityPlayer().world)) {
			final TileEntity tile = event.getEntityPlayer().world.getTileEntity(event.getPos());
			if (tile instanceof LogisticsTileGenericPipe) {
				if (((LogisticsTileGenericPipe) tile).pipe instanceof CoreRoutedPipe) {
					if (!((CoreRoutedPipe) ((LogisticsTileGenericPipe) tile).pipe).canBeDestroyedByPlayer(event.getEntityPlayer())) {
						event.setCanceled(true);
						event.getEntityPlayer().sendMessage(new TextComponentTranslation("lp.chat.permissiondenied"));
						((LogisticsTileGenericPipe) tile).scheduleNeighborChange();
						World world = event.getEntityPlayer().world;
						BlockPos pos = tile.getPos();
						IBlockState state = world.getBlockState(pos);
						world.markAndNotifyBlock(tile.getPos(), world.getChunkFromBlockCoords(pos), state, state, 2);
						((CoreRoutedPipe) ((LogisticsTileGenericPipe) tile).pipe).delayTo = System.currentTimeMillis() + 200;
						((CoreRoutedPipe) ((LogisticsTileGenericPipe) tile).pipe).repeatFor = 10;
					} else {
						((CoreRoutedPipe) ((LogisticsTileGenericPipe) tile).pipe).setDestroyByPlayer();
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLeftClickBlock(final PlayerInteractEvent.RightClickBlock event) {
		if (MainProxy.isServer(event.getEntityPlayer().world)) {
			WorldCoordinatesWrapper worldCoordinates = new WorldCoordinatesWrapper(event.getEntityPlayer().world, event.getPos());
			TileEntity tileEntity = worldCoordinates.getTileEntity();
			if (tileEntity instanceof TileEntityChest || SimpleServiceLocator.ironChestProxy.isIronChest(tileEntity)) {
				List<WeakReference<AsyncQuicksortModule>> list = worldCoordinates.allNeighborTileEntities().stream()
						.filter(NeighborTileEntity::isLogisticsPipe)
						.filter(adjacent -> ((LogisticsTileGenericPipe) adjacent.getTileEntity()).pipe instanceof PipeLogisticsChassis)
						.filter(adjacent -> ((PipeLogisticsChassis) ((LogisticsTileGenericPipe) adjacent.getTileEntity()).pipe).getPointedOrientation()
								== adjacent.getOurDirection())
						.map(adjacent -> (PipeLogisticsChassis) ((LogisticsTileGenericPipe) adjacent.getTileEntity()).pipe)
						.flatMap(chassis -> chassis.getModules().getModules())
						.filter(logisticsModule -> logisticsModule instanceof AsyncQuicksortModule)
						.map(logisticsModule -> new WeakReference<>((AsyncQuicksortModule) logisticsModule))
						.collect(Collectors.toList());

				if (!list.isEmpty()) {
					LogisticsEventListener.chestQuickSortConnection.put(event.getEntityPlayer(), list);
				}
			}
		}
	}

	public static HashMap<Integer, Long> WorldLoadTime = new HashMap<>();

	@SubscribeEvent
	public void WorldLoad(WorldEvent.Load event) {
		if (MainProxy.isServer(event.getWorld())) {
			int dim = event.getWorld().provider.getDimension();
			if (!LogisticsEventListener.WorldLoadTime.containsKey(dim)) {
				LogisticsEventListener.WorldLoadTime.put(dim, System.currentTimeMillis());
			}
		}
		if (MainProxy.isClient(event.getWorld())) {
			SimpleServiceLocator.routerManager.clearClientRouters();
			LogisticsHUDRenderer.instance().clear();
		}
	}

	@SubscribeEvent
	public void WorldUnload(WorldEvent.Unload event) {
		if (MainProxy.isServer(event.getWorld())) {
			int dim = event.getWorld().provider.getDimension();
			SimpleServiceLocator.routerManager.dimensionUnloaded(dim);
		}
	}

	@SubscribeEvent
	public void watchChunk(Watch event) {
		ChunkPos pos = event.getChunkInstance().getPos();
		if (!LogisticsEventListener.watcherList.containsKey(pos)) {
			LogisticsEventListener.watcherList.put(pos, new PlayerCollectionList());
		}
		LogisticsEventListener.watcherList.get(pos).add(event.getPlayer());
	}

	@SubscribeEvent
	public void unWatchChunk(UnWatch event) {
		ChunkPos pos = event.getChunkInstance().getPos();
		if (LogisticsEventListener.watcherList.containsKey(pos)) {
			LogisticsEventListener.watcherList.get(pos).remove(event.getPlayer());
		}
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event) {
		if (MainProxy.isServer(event.player.world)) {
			SimpleServiceLocator.securityStationManager.sendClientAuthorizationList(event.player);
		}

		SimpleServiceLocator.serverBufferHandler.clear(event.player);
		ClientConfiguration config = LogisticsPipes.getServerConfigManager().getPlayerConfiguration(PlayerIdentifier.get(event.player));
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PlayerConfigToClientPacket.class).setConfig(config), event.player);
	}

	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent event) {
		SimpleServiceLocator.serverBufferHandler.clear(event.player);
	}

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
		@Getter
		@Setter
		private boolean isActive;
	}

	@Getter(lazy = true)
	private static final Queue<GuiEntry> guiPos = new LinkedList<>();

	//Handle GuiRepoen
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onGuiOpen(GuiOpenEvent event) {
		if (!LogisticsEventListener.getGuiPos().isEmpty()) {
			if (event.getGui() == null) {
				GuiEntry part = LogisticsEventListener.getGuiPos().peek();
				if (part.isActive()) {
					part = LogisticsEventListener.getGuiPos().poll();
					MainProxy.sendPacketToServer(PacketHandler.getPacket(GuiReopenPacket.class).setGuiID(part.getGuiID()).setPosX(part.getXCoord()).setPosY(part.getYCoord()).setPosZ(part.getZCoord()));
					GuiOverlay.getInstance().setOverlaySlotActive(false);
				}
			} else {
				GuiEntry part = LogisticsEventListener.getGuiPos().peek();
				part.setActive(true);
			}
		}
		if (event.getGui() == null) {
			GuiOverlay.getInstance().setOverlaySlotActive(false);
		}
		if (event.getGui() instanceof GuiChest || (SimpleServiceLocator.ironChestProxy != null && SimpleServiceLocator.ironChestProxy.isChestGui(event.getGui()))) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(ChestGuiOpened.class));
		} else {
			QuickSortChestMarkerStorage.getInstance().disable();
			MainProxy.sendPacketToServer(PacketHandler.getPacket(ChestGuiClosed.class));
		}
	}

	@SideOnly(Side.CLIENT)
	public static void addGuiToReopen(int xCoord, int yCoord, int zCoord, int guiID) {
		LogisticsEventListener.getGuiPos().add(new GuiEntry(xCoord, yCoord, zCoord, guiID, false));
	}

	@SubscribeEvent
	public void clientLoggedIn(ClientConnectedToServerEvent event) {
		SimpleServiceLocator.clientBufferHandler.clear();

		if (Configs.CHECK_FOR_UPDATES) {
			LogisticsPipes.singleThreadExecutor.execute(() -> {
				// try to get player entity ten times, once a second
				int times = 0;
				EntityPlayerSP playerEntity;
				do {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						return;
					}
					playerEntity = FMLClientHandler.instance().getClientPlayerEntity();
					++times;
				} while (playerEntity == null && times <= 10);

				if (times > 10) {
					return;
				}

				VersionChecker checker = LogisticsPipes.versionChecker;

				// send player message
				String versionMessage = checker.getVersionCheckerStatus();

				if (checker.isVersionCheckDone() && checker.getVersionInfo().isNewVersionAvailable() && !checker.getVersionInfo().isImcMessageSent()) {
					playerEntity.sendMessage(new TextComponentString(versionMessage));
					playerEntity.sendMessage(new TextComponentString("Use \"/logisticspipes changelog\" to see a changelog."));
				} else if (!checker.isVersionCheckDone()) {
					playerEntity.sendMessage(new TextComponentString(versionMessage));
				}
			});
		}
	}

	@SubscribeEvent
	public void onBlockUpdate(BlockEvent.NeighborNotifyEvent event) {
		TEControl.handleBlockUpdate(event.getWorld(), LPTickHandler.getWorldInfo(event.getWorld()), event.getPos());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onItemStackToolTip(ItemTooltipEvent event) {
		if (event.getItemStack().hasTagCompound()) {
			for (String key : event.getItemStack().getTagCompound().getKeySet()) {
				if (key.startsWith("logisticspipes:routingdata")) {
					ItemRoutingInformation info = ItemRoutingInformation.restoreFromNBT(event.getItemStack().getTagCompound().getCompoundTag(key));
					List<String> list = event.getToolTip();
					list.set(0, ChatColor.RED + "!!! " + ChatColor.WHITE + list.get(0) + ChatColor.RED + " !!!" + ChatColor.WHITE);
					list.add(1, TextUtil.translate("itemstackinfo.lprouteditem"));
					list.add(2, TextUtil.translate("itemstackinfo.lproutediteminfo"));
					list.add(3, TextUtil.translate("itemstackinfo.lprouteditemtype") + ": " + info.getItem().toString());
				}
			}
		}
	}

	@SubscribeEvent
	public void onItemCrafting(PlayerEvent.ItemCraftedEvent event) {
		if (event.player.isServerWorld() && !event.crafting.isEmpty()) {
			if (event.crafting.getItem().getRegistryName().getResourceDomain().equals(LPConstants.LP_MOD_ID)) {
				PlayerIdentifier identifier = PlayerIdentifier.get(event.player);
				PlayerConfiguration config = LogisticsPipes.getServerConfigManager().getPlayerConfiguration(identifier);
				if (!config.getHasCraftedLPItem() && !LogisticsPipes.isDEBUG()) {
					ItemStack book = new ItemStack(LPItems.itemGuideBook, 1);
					event.player.addItemStackToInventory(book);

					config.setHasCraftedLPItem(true);
					LogisticsPipes.getServerConfigManager().setPlayerConfiguration(identifier, config);
				}
			}
		}
	}
}
