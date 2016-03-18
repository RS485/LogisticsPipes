package logisticspipes.proxy.buildcraft;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import buildcraft.api.events.RobotEvent;
import buildcraft.transport.*;
import buildcraft.transport.render.PipeTransportItemsRenderer;
import buildcraft.transport.render.PipeTransportRenderer;
import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.VersionNotSupportedException;
import logisticspipes.proxy.buildcraft.gates.ActionDisableLogistics;
import logisticspipes.proxy.buildcraft.gates.ActionRobotRoutingLogistics;
import logisticspipes.proxy.buildcraft.gates.LogisticsActionProvider;
import logisticspipes.proxy.buildcraft.gates.LogisticsTriggerProvider;
import logisticspipes.proxy.buildcraft.gates.TriggerCrafting;
import logisticspipes.proxy.buildcraft.gates.TriggerHasDestination;
import logisticspipes.proxy.buildcraft.gates.TriggerNeedsPower;
import logisticspipes.proxy.buildcraft.gates.TriggerSupplierFailed;
import logisticspipes.proxy.buildcraft.recipeprovider.AssemblyTable;
import logisticspipes.proxy.buildcraft.robots.LPRobotConnectionControl;
import logisticspipes.proxy.buildcraft.robots.boards.LogisticsRoutingBoardRobot;
import logisticspipes.proxy.buildcraft.robots.boards.LogisticsRoutingBoardRobotNBT;
import logisticspipes.proxy.buildcraft.subproxies.IBCClickResult;
import logisticspipes.proxy.buildcraft.subproxies.IBCRenderTESR;
import logisticspipes.proxy.buildcraft.subproxies.IBCTilePart;
import logisticspipes.proxy.buildcraft.subproxies.IConnectionOverrideResult;
import logisticspipes.proxy.buildcraft.subproxies.LPBCPipe;
import logisticspipes.proxy.buildcraft.subproxies.LPBCPipeTransportsItems;
import logisticspipes.proxy.buildcraft.subproxies.LPBCTileGenericPipe;
import logisticspipes.proxy.interfaces.IBCProxy;
import logisticspipes.proxy.interfaces.ICraftingParts;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.utils.ReflectionHelper;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.FMLCommonHandler;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftSilicon;
import buildcraft.BuildCraftTransport;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BCLog;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.ItemMapLocation;
import buildcraft.core.lib.ITileBufferHolder;
import buildcraft.robotics.EntityRobot;
import buildcraft.robotics.ItemRobot;
import buildcraft.robotics.RobotStationPluggable;
import buildcraft.transport.render.PipeRendererTESR;
import lombok.SneakyThrows;

public class BuildCraftProxy implements IBCProxy {

	public static ITriggerInternal LogisticsFailedTrigger;
	public static ITriggerInternal LogisticsCraftingTrigger;
	public static ITriggerExternal LogisticsNeedPowerTrigger;
	public static ITriggerInternal LogisticsHasDestinationTrigger;
	public static IActionInternal LogisticsDisableAction;
	public static IActionInternal LogisticsRobotRoutingAction;

	private Method canPipeConnect;

	public PipeType logisticsPipeType;

	public BuildCraftProxy() {
		String BCVersion = null;
		try {
			Field versionField = buildcraft.core.Version.class.getDeclaredField("VERSION");
			BCVersion = (String) versionField.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (BCVersion != null && !BCVersion.equals("@VERSION@")) {
			if (!BCVersion.startsWith("7.1")) {
				throw new VersionNotSupportedException("BC", BCVersion, "7.1.5", "");
			}
		} else {
			LogisticsPipes.log.info("Couldn't check the BC Version.");
		}
	}

	@Override
	public void resetItemRotation() {
		try {
			Object renderer = TileEntityRendererDispatcher.instance.mapSpecialRenderers.get(TileGenericPipe.class);
			Field f = PipeTransportItemsRenderer.class.getDeclaredField("dummyEntityItem");
			f.setAccessible(true);
			EntityItem item = (EntityItem) f.get(renderer);
			item.hoverStart = 0;
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isIPipeTile(TileEntity tile) {
		return tile instanceof IPipeTile;
	}

	@Override
	public void registerPipeInformationProvider() {
		SimpleServiceLocator.pipeInformationManager.registerProvider(TileGenericPipe.class, BCPipeInformationProvider.class);
	}

	@Override
	public void initProxy() {

		RedstoneBoardRegistry.instance.registerBoardClass(LogisticsRoutingBoardRobotNBT.instance, 10);
		RobotManager.registerAIRobot(LogisticsRoutingBoardRobot.class, "boardLogisticsRoutingRobot", "logisticspipes.proxy.buildcraft.robots.boards.LogisticsRoutingBoardRobot");
		SimpleServiceLocator.specialpipeconnection.registerHandler(LPRobotConnectionControl.instance);

		if(MainProxy.proxy.getSide().equals("Client")) {
			PipeTransportRenderer.RENDERER_MAP.put(PipeTransportItems.class, new BCLPPipeTransportItemsRenderer());
		}

		try {
			canPipeConnect = TileGenericPipe.class.getDeclaredMethod("canPipeConnect", new Class[] { TileEntity.class, ForgeDirection.class });
			canPipeConnect.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		PipeEventBus.registerGlobalHandler(new BCEventHandler());
	}

	@Override
	public boolean checkForPipeConnection(TileEntity with, ForgeDirection side, LogisticsTileGenericPipe pipe) {
		if (with instanceof TileGenericPipe) {
			if (ReflectionHelper.invokePrivateMethodCatched(Boolean.class, TileGenericPipe.class, with, "hasBlockingPluggable", new Class[] { ForgeDirection.class }, new Object[] { side.getOpposite() }).booleanValue()) {
				return false;
			}
			Pipe<?> otherPipe = ((TileGenericPipe) with).pipe;

			if (!BlockGenericPipe.isValid(otherPipe)) {
				return false;
			}

			if(pipe.pipe != null && pipe.pipe.isFluidPipe()) {
				if (!(otherPipe.transport instanceof PipeTransportItems) && !(otherPipe.transport instanceof PipeTransportFluids)) {
					return false;
				}
			} else {
				if (!(otherPipe.transport instanceof PipeTransportItems)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public IConnectionOverrideResult checkConnectionOverride(TileEntity with, ForgeDirection side, LogisticsTileGenericPipe pipe) {
		if (with instanceof IPipeConnection) {
			IPipeConnection.ConnectOverride override = ((IPipeConnection) with).overridePipeConnection(PipeType.ITEM, side.getOpposite());
			if (override == IPipeConnection.ConnectOverride.DISCONNECT) {
				//if it doesn't want to connect to item pipes, how about fluids?
				if (pipe.pipe.transport instanceof PipeFluidTransportLogistics || pipe.pipe instanceof PipeItemsFluidSupplier) {
					override = ((IPipeConnection) with).overridePipeConnection(PipeType.FLUID, side.getOpposite());
				}
				if (override == IPipeConnection.ConnectOverride.DISCONNECT) {
					//nope, maybe you'd like some BC power?
					if (pipe.pipe.getUpgradeManager().hasRFPowerSupplierUpgrade()) {
						override = ((IPipeConnection) with).overridePipeConnection(PipeType.POWER, side.getOpposite());
					}
				}
			}
			if (override == IPipeConnection.ConnectOverride.DISCONNECT) {
				return new IConnectionOverrideResult() {

					@Override
					public boolean forceConnect() {
						return false;
					}

					@Override
					public boolean forceDisconnect() {
						return true;
					}
				};
			}

			if (override == IPipeConnection.ConnectOverride.CONNECT) {
				return new IConnectionOverrideResult() {

					@Override
					public boolean forceConnect() {
						return true;
					}

					@Override
					public boolean forceDisconnect() {
						return false;
					}
				};
			}
		}
		return new IConnectionOverrideResult() {

			@Override
			public boolean forceConnect() {
				return false;
			}

			@Override
			public boolean forceDisconnect() {
				return false;
			}
		};
	}

	@Override
	public boolean canPipeConnect(TileEntity pipe, TileEntity with, ForgeDirection side) {
		if (canPipeConnect == null) {
			initProxy();
		}
		if (!(pipe instanceof TileGenericPipe)) {
			throw new IllegalArgumentException();
		}
		try {
			return (Boolean) canPipeConnect.invoke(pipe, with, side);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public boolean isInstalled() {
		return true;
	}

	@Override
	public Object getLPPipeType() {
		if (logisticsPipeType == null) {
			logisticsPipeType = net.minecraftforge.common.util.EnumHelper.addEnum(PipeType.class, "LOGISTICS", new Class<?>[] {}, new Object[] {});
		}
		return logisticsPipeType;
	}

	@Override
	public void registerTrigger() {
		StatementManager.registerTriggerProvider(new LogisticsTriggerProvider());
		StatementManager.registerActionProvider(new LogisticsActionProvider());
		/* Triggers */
		BuildCraftProxy.LogisticsFailedTrigger = new TriggerSupplierFailed();
		BuildCraftProxy.LogisticsNeedPowerTrigger = new TriggerNeedsPower();
		BuildCraftProxy.LogisticsCraftingTrigger = new TriggerCrafting();
		BuildCraftProxy.LogisticsHasDestinationTrigger = new TriggerHasDestination();
		/* Actions */
		BuildCraftProxy.LogisticsDisableAction = new ActionDisableLogistics();
		BuildCraftProxy.LogisticsRobotRoutingAction = new ActionRobotRoutingLogistics();
	}

	@Override
	public ICraftingParts getRecipeParts() {
		return new ICraftingParts() {

			@Override
			public ItemStack getChipTear1() {
				return new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1);
			}

			@Override
			public ItemStack getChipTear2() {
				return new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2);
			}

			@Override
			public ItemStack getChipTear3() {
				return new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3);
			}

			@Override
			public Object getGearTear1() {
				return "gearIron";
			}

			@Override
			public Object getGearTear2() {
				return "gearGold";
			}

			@Override
			public Object getGearTear3() {
				return "gearDiamond";
			}

			@Override
			public Object getSortingLogic() {
				return BuildCraftTransport.pipeItemsDiamond;
			}

			@Override
			public Object getBasicTransport() {
				return BuildCraftTransport.pipeItemsCobblestone;
			}

			@Override
			public Object getWaterProof() {
				return BuildCraftTransport.pipeWaterproof;
			}

			@Override
			public Object getExtractorItem() {
				return BuildCraftTransport.pipeItemsWood;
			}

			@Override
			public Object getExtractorFluid() {
				return BuildCraftTransport.pipeFluidsWood;
			}

			@Override
			public Object getBlockDynamo() {
				return new ItemStack(BuildCraftCore.engineBlock, 1, 2);
			}

			@Override
			public Object getPowerCoilSilver() {
				return getChipTear1();
			}

			@Override
			public Object getPowerCoilGold() {
				return getChipTear2();
			}
		};
	}

	@Override
	public void addCraftingRecipes(ICraftingParts parts) {}

	@Override
	public Class<? extends ICraftingRecipeProvider> getAssemblyTableProviderClass() {
		return AssemblyTable.class;
	}

	@Override
	public void notifyOfChange(LogisticsTileGenericPipe pipe, TileEntity tile, ForgeDirection o) {
		if (tile instanceof ITileBufferHolder) {
			((ITileBufferHolder) tile).blockCreated(o, BuildCraftTransport.genericPipeBlock, pipe);
		}
		if (tile instanceof TileGenericPipe) {
			((TileGenericPipe) tile).scheduleNeighborChange();
		}
	}

	@Override
	@SneakyThrows({ NoSuchFieldException.class, SecurityException.class, IllegalArgumentException.class, IllegalAccessException.class })
	public IBCTilePart getBCTilePart(LogisticsTileGenericPipe pipe) {
		return new LPBCTileGenericPipe(new LPBCPipe(new LPBCPipeTransportsItems(pipe), pipe), pipe);
	}

	@Override
	public IBCClickResult handleBCClickOnPipe(World world, int x, int y, int z, EntityPlayer player, int side, float xOffset, float yOffset, float zOffset, CoreUnroutedPipe pipe) {
		boolean result = BuildCraftTransport.genericPipeBlock.onBlockActivated(world, x, y, z, player, side, xOffset, yOffset, zOffset);

		world.notifyBlocksOfNeighborChange(x, y, z, LogisticsPipes.LogisticsPipeBlock); //Again because not all changes have been applied before the call inside the BC method is made.
		boolean block = false;
		if (!result) {
			ItemStack currentItem = player.getCurrentEquippedItem();
			if (currentItem != null) {
				if (currentItem.getItem() == Items.sign) {
					block = true;
				} else if (currentItem.getItem() instanceof ItemPipe) {
					block = true;
				} else if (currentItem.getItem() instanceof ItemGateCopier) {
					block = true;
				} else if (currentItem.getItem() instanceof ItemMapLocation) {
					block = true;
				} else if (currentItem.getItem() instanceof ItemRobot) {
					result = checkRobot(world, x, y, z, player, currentItem);
				}
			}
		}
		final boolean fResult = result;
		final boolean fBlock = block;
		return new IBCClickResult() {

			@Override
			public boolean handled() {
				return fResult;
			}

			@Override
			public boolean blocked() {
				return fBlock;
			}
		};
	}

	/**
	 * @see buildcraft.robotics.ItemRobot#onItemUse(ItemStack, EntityPlayer,
	 *      World, int, int, int, int, float, float, float)
	 */
	private boolean checkRobot(World world, int x, int y, int z, EntityPlayer player, ItemStack currentItem) {
		if (!world.isRemote) {
			Pipe<?> bcPipe = BlockGenericPipe.getPipe(world, x, y, z);
			if (bcPipe == null) {
				return false;
			}

			BlockGenericPipe pipeBlock = BuildCraftTransport.genericPipeBlock;
			BlockGenericPipe.RaytraceResult rayTraceResult = pipeBlock.doRayTrace(world, x, y, z, player);

			if (rayTraceResult != null && rayTraceResult.hitPart == BlockGenericPipe.Part.Pluggable && bcPipe.container.getPipePluggable(rayTraceResult.sideHit) instanceof RobotStationPluggable) {
				RobotStationPluggable pluggable = (RobotStationPluggable) bcPipe.container.getPipePluggable(rayTraceResult.sideHit);
				DockingStation station = pluggable.getStation();

				if (!station.isTaken()) {
					RedstoneBoardRobotNBT robotNBT = ItemRobot.getRobotNBT(currentItem);
					if (robotNBT == RedstoneBoardRegistry.instance.getEmptyRobotBoard()) {
						return true;
					}

					EntityRobot robot = ((ItemRobot) currentItem.getItem())
							.createRobot(currentItem, world);

					RobotEvent.Place robotEvent = new RobotEvent.Place(robot, player);
					FMLCommonHandler.instance().bus().post(robotEvent);
					if (robotEvent.isCanceled()) {
						return true;
					}

					if (robot != null && robot.getRegistry() != null) {
						robot.setUniqueRobotId(robot.getRegistry().getNextRobotId());

						float px = x + 0.5F + rayTraceResult.sideHit.offsetX * 0.5F;
						float py = y + 0.5F + rayTraceResult.sideHit.offsetY * 0.5F;
						float pz = z + 0.5F + rayTraceResult.sideHit.offsetZ * 0.5F;

						robot.setPosition(px, py, pz);
						station.takeAsMain(robot);
						robot.dock(robot.getLinkedStation());
						world.spawnEntityInWorld(robot);

						if (!player.capabilities.isCreativeMode) {
							player.getCurrentEquippedItem().stackSize--;
						}
					}
				}

				return true;
			}
		}
		return false;
	}

	@Override
	public void callBCNeighborBlockChange(World world, int x, int y, int z, Block block) {
		BuildCraftTransport.genericPipeBlock.onNeighborBlockChange(world, x, y, z, block);
	}

	@Override
	public void callBCRemovePipe(World world, int x, int y, int z) {
		BlockGenericPipe.removePipe(BlockGenericPipe.getPipe(world, x, y, z));
	}

	@Override
	public void logWarning(String format) {
		BCLog.logger.warn(format);
	}

	@Override
	public IBCRenderTESR getBCRenderTESR() {
		return new IBCRenderTESR() {

			@Override
			@SneakyThrows(Exception.class)
			public void renderWires(LogisticsTileGenericPipe pipe, double x, double y, double z) {
				TileGenericPipe tgPipe = (TileGenericPipe) pipe.tilePart.getOriginal();
				ReflectionHelper.invokePrivateMethod(Object.class, PipeRendererTESR.class, PipeRendererTESR.INSTANCE, "renderGatesWires", new Class[] { TileGenericPipe.class, double.class, double.class, double.class }, new Object[] { tgPipe, x, y, z });
			}

			@Override
			@SneakyThrows(Exception.class)
			public void dynamicRenderPluggables(LogisticsTileGenericPipe pipe, double x, double y, double z) {
				TileGenericPipe tgPipe = (TileGenericPipe) pipe.tilePart.getOriginal();
				ReflectionHelper.invokePrivateMethod(Object.class, PipeRendererTESR.class, PipeRendererTESR.INSTANCE, "renderPluggables", new Class[] { TileGenericPipe.class, double.class, double.class, double.class }, new Object[] { tgPipe, x, y, z });
			}
		};
	}

	@Override
	public boolean isTileGenericPipe(TileEntity tile) {
		return tile instanceof TileGenericPipe;
	}

	@Override
	public void cleanup() {
		LPRobotConnectionControl.instance.cleanup();
	}
}
