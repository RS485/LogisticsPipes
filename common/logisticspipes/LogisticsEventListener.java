package logisticspipes;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ChunkWatchEvent.UnWatch;
import net.minecraftforge.event.world.ChunkWatchEvent.Watch;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import logisticspipes.config.Configs;
import logisticspipes.config.PlayerConfig;
import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.modules.ModuleQuickSort;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.PlayerConfigToClientPacket;
import logisticspipes.network.packets.chassis.ChestGuiClosed;
import logisticspipes.network.packets.chassis.ChestGuiOpened;
import logisticspipes.network.packets.gui.GuiReopenPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.LogisticsGuiOverrenderer;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.ticks.VersionChecker;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.PlayerIdentifier;
import logisticspipes.utils.QuickSortChestMarkerStorage;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

public class LogisticsEventListener {

	public static final WeakHashMap<EntityPlayer, List<WeakReference<ModuleQuickSort>>> chestQuickSortConnection = new WeakHashMap<>();
	public static Map<ChunkPos, PlayerCollectionList> watcherList = new ConcurrentHashMap<>();
	int taskCount = 0;
	public static Map<PlayerIdentifier, PlayerConfig> playerConfigs = new HashMap<>();

	@SubscribeEvent
	public void onEntitySpawn(EntityJoinWorldEvent event) {
		if (event != null && event.getEntity() instanceof EntityItem && event.getEntity().world != null && !event.getEntity().world.isRemote) {
			ItemStack stack = ((EntityItem) event.getEntity()).getItem(); //Get ItemStack
			if (stack != null && stack.getItem() instanceof IItemAdvancedExistance && !((IItemAdvancedExistance) stack.getItem()).canExistInWorld(stack)) {
				event.setCanceled(true);
			}
		}
	}

	/*
	 * subscribe forge pre stich event to register common texture
	 */
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Pre event) throws IOException {
		LogisticsPipes.textures.registerBlockIcons(event.getMap());
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

	public void onPlayerLeftClickBlock(final PlayerInteractEvent.RightClickBlock event) {
		if (MainProxy.isServer(event.getEntityPlayer().world)) {
			WorldCoordinatesWrapper worldCoordinates = new WorldCoordinatesWrapper(event.getEntityPlayer().world, event.getPos());
			TileEntity tileEntity = worldCoordinates.getTileEntity();
			if (tileEntity instanceof TileEntityChest || SimpleServiceLocator.ironChestProxy.isIronChest(tileEntity)) {
				//@formatter:off
				List<WeakReference<ModuleQuickSort>> list = worldCoordinates.getAdjacentTileEntities()
						.filter(adjacent -> adjacent.tileEntity instanceof LogisticsTileGenericPipe)
						.filter(adjacent -> ((LogisticsTileGenericPipe) adjacent.tileEntity).pipe instanceof PipeLogisticsChassi)
						.filter(adjacent -> ((PipeLogisticsChassi) ((LogisticsTileGenericPipe) adjacent.tileEntity).pipe).getPointedOrientation()
								== adjacent.direction.getOpposite())
						.map(adjacent -> (PipeLogisticsChassi) ((LogisticsTileGenericPipe) adjacent.tileEntity).pipe)
						.flatMap(pipeLogisticsChassi -> Arrays.stream(pipeLogisticsChassi.getModules().getModules()))
						.filter(logisticsModule -> logisticsModule instanceof ModuleQuickSort)
						.map(logisticsModule -> new WeakReference<>((ModuleQuickSort) logisticsModule))
						.collect(Collectors.toList());
				//@formatter:on

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
			int dim = MainProxy.getDimensionForWorld(event.getWorld());
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
			int dim = MainProxy.getDimensionForWorld(event.getWorld());
			SimpleServiceLocator.routerManager.dimensionUnloaded(dim);
		}
	}

	@SubscribeEvent
	public void watchChunk(Watch event) {
		if (!LogisticsEventListener.watcherList.containsKey(event.getChunk())) {
			LogisticsEventListener.watcherList.put(event.getChunk(), new PlayerCollectionList());
		}
		LogisticsEventListener.watcherList.get(event.getChunk()).add(event.getPlayer());
	}

	@SubscribeEvent
	public void unWatchChunk(UnWatch event) {
		if (LogisticsEventListener.watcherList.containsKey(event.getChunk())) {
			LogisticsEventListener.watcherList.get(event.getChunk()).remove(event.getPlayer());
		}
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event) {
		if (MainProxy.isServer(event.player.world)) {
			SimpleServiceLocator.securityStationManager.sendClientAuthorizationList(event.player);
		}

		SimpleServiceLocator.serverBufferHandler.clear(event.player);
		PlayerConfig config = LogisticsEventListener.getPlayerConfig(PlayerIdentifier.get(event.player));
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PlayerConfigToClientPacket.class).setConfig(config), event.player);
	}

	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent event) {
		SimpleServiceLocator.serverBufferHandler.clear(event.player);
		PlayerIdentifier ident = PlayerIdentifier.get(event.player);
		PlayerConfig config = LogisticsEventListener.getPlayerConfig(ident);
		if (config != null) {
			config.writeToFile();
		}
		LogisticsEventListener.playerConfigs.remove(ident);
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
					LogisticsGuiOverrenderer.getInstance().setOverlaySlotActive(false);
				}
			} else {
				GuiEntry part = LogisticsEventListener.getGuiPos().peek();
				part.setActive(true);
			}
		}
		if (event.getGui() == null) {
			LogisticsGuiOverrenderer.getInstance().setOverlaySlotActive(false);
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
				assert playerEntity != null;

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

	public static void serverShutdown() {
		LogisticsEventListener.playerConfigs.values().forEach(PlayerConfig::writeToFile);
		LogisticsEventListener.playerConfigs.clear();
	}

	public static PlayerConfig getPlayerConfig(PlayerIdentifier ident) {
		PlayerConfig config = LogisticsEventListener.playerConfigs.get(ident);
		if (config == null) {
			config = new PlayerConfig(ident);
			config.readFromFile();
			LogisticsEventListener.playerConfigs.put(ident, config);
		}
		return config;
	}
}
