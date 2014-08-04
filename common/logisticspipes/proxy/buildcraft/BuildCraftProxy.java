/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.proxy.buildcraft;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import logisticspipes.Configs;
import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.PipeFluidBasic;
import logisticspipes.pipes.PipeFluidExtractor;
import logisticspipes.pipes.PipeFluidInsertion;
import logisticspipes.pipes.PipeFluidProvider;
import logisticspipes.pipes.PipeFluidRequestLogistics;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeFluidSupplierMk2;
import logisticspipes.pipes.PipeItemsApiaristAnalyser;
import logisticspipes.pipes.PipeItemsApiaristSink;
import logisticspipes.pipes.PipeItemsBasicLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk2;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk3;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsProviderLogisticsMk2;
import logisticspipes.pipes.PipeItemsRemoteOrdererLogistics;
import logisticspipes.pipes.PipeItemsRequestLogistics;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.PipeItemsSystemDestinationLogistics;
import logisticspipes.pipes.PipeItemsSystemEntranceLogistics;
import logisticspipes.pipes.PipeLogisticsChassiMk1;
import logisticspipes.pipes.PipeLogisticsChassiMk2;
import logisticspipes.pipes.PipeLogisticsChassiMk3;
import logisticspipes.pipes.PipeLogisticsChassiMk4;
import logisticspipes.pipes.PipeLogisticsChassiMk5;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe.Part;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe.RaytraceResult;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.VersionNotSupportedException;
import logisticspipes.proxy.buildcraft.gates.ActionDisableLogistics;
import logisticspipes.proxy.buildcraft.gates.LogisticsTriggerProvider;
import logisticspipes.proxy.buildcraft.gates.TriggerCrafting;
import logisticspipes.proxy.buildcraft.gates.TriggerHasDestination;
import logisticspipes.proxy.buildcraft.gates.TriggerNeedsPower;
import logisticspipes.proxy.buildcraft.gates.TriggerSupplierFailed;
import logisticspipes.proxy.buildcraft.pipeparts.BCPipePart;
import logisticspipes.proxy.buildcraft.pipeparts.IBCPipePart;
import logisticspipes.proxy.interfaces.IBCProxy;
import logisticspipes.renderer.LogisticsPipeBlockRenderer;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemClient;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.CoreConstants;
import buildcraft.core.IMachine;
import buildcraft.core.ItemRobot;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.robots.AIDocked;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.ItemPlug;
import buildcraft.transport.ItemRobotStation;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TransportProxy;
import buildcraft.transport.TransportProxyClient;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.render.PipeRendererTESR;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

public class BuildCraftProxy implements IBCProxy {
	
	public static ITrigger LogisticsFailedTrigger;
	public static ITrigger LogisticsCraftingTrigger;
	public static ITrigger LogisticsNeedPowerTrigger;
	public static ITrigger LogisticsHasDestinationTrigger;
	public static IAction LogisticsDisableAction;
	
	private Method canPipeConnect;
	
	public BuildCraftProxy() {
		String BCVersion = null;
		try {
			Field versionField = buildcraft.core.Version.class.getDeclaredField("VERSION");
			BCVersion = (String) versionField.get(null);
		} catch(Exception e) {
			e.printStackTrace();
		}
		String expectedBCVersion = "6.0.10";
		if(BCVersion != null) {
			if(!BCVersion.equals("@VERSION@") && !BCVersion.contains(expectedBCVersion)) {
				throw new VersionNotSupportedException("BC", BCVersion, expectedBCVersion, "");
			}
		} else {
			LogisticsPipes.log.info("Couldn't check the BC Version.");
		}
	}
	
	public boolean checkPipesConnections(TileEntity from, TileEntity to, ForgeDirection way) {
		return checkPipesConnections(from, to, way, false);
	}
	
	//TODO generalise more for TE support
	public boolean checkPipesConnections(TileEntity from, TileEntity to, ForgeDirection way, boolean ignoreSystemDisconnection) {
		if(from instanceof TileGenericPipe && to instanceof TileGenericPipe && (((TileGenericPipe)from).pipe instanceof CoreRoutedPipe || ((TileGenericPipe)to).pipe instanceof CoreRoutedPipe)) {
			if(((TileGenericPipe)from).pipe instanceof CoreRoutedPipe) {
				if (!((CoreRoutedPipe)((TileGenericPipe)from).pipe).canPipeConnect(to, way, ignoreSystemDisconnection)) {
					return false;
				}
			} else {
				((CoreRoutedPipe)((TileGenericPipe) to).pipe).globalIgnoreConnectionDisconnection = true;
				if (!canPipeConnect((TileGenericPipe) from, to, way)) {
					((CoreRoutedPipe)((TileGenericPipe) to).pipe).globalIgnoreConnectionDisconnection = false;
					return false;
				}
				((CoreRoutedPipe)((TileGenericPipe) to).pipe).globalIgnoreConnectionDisconnection = false;
			}
			if(((TileGenericPipe)to).pipe instanceof CoreRoutedPipe) {
				if (!((CoreRoutedPipe)((TileGenericPipe) to).pipe).canPipeConnect(from, way.getOpposite(), ignoreSystemDisconnection)) {
					return false;
				}
			} else {
				((CoreRoutedPipe)((TileGenericPipe) from).pipe).globalIgnoreConnectionDisconnection = true;
				if (!canPipeConnect((TileGenericPipe) to, from, way.getOpposite())) {
					((CoreRoutedPipe)((TileGenericPipe) from).pipe).globalIgnoreConnectionDisconnection = false;
					return false;
				}
				((CoreRoutedPipe)((TileGenericPipe) from).pipe).globalIgnoreConnectionDisconnection = false;
			}
			return true;
		} else if(from instanceof TileGenericPipe && ((TileGenericPipe)from).pipe instanceof CoreRoutedPipe) {
			if (!((CoreRoutedPipe)((TileGenericPipe)from).pipe).canPipeConnect(to, way, ignoreSystemDisconnection)) {
				return false;
			}
			return true;
		} else if(to instanceof TileGenericPipe && ((TileGenericPipe)to).pipe instanceof CoreRoutedPipe) {
			if (!((CoreRoutedPipe)((TileGenericPipe) to).pipe).canPipeConnect(from, way.getOpposite(), ignoreSystemDisconnection)) {
				return false;
			}
			return true;
		} else {
			return Utils.checkPipesConnections(from, to);
		}
	}

	public void initProxy() {
		try {
			canPipeConnect = TileGenericPipe.class.getDeclaredMethod("canPipeConnect", new Class[]{TileEntity.class, ForgeDirection.class});
			canPipeConnect.setAccessible(true);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public boolean canPipeConnect(TileGenericPipe tile, TileEntity with, ForgeDirection side) {
		try {
			return (Boolean) canPipeConnect.invoke(tile, with, side);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void registerTrigger() {
		ActionManager.registerTriggerProvider(new LogisticsTriggerProvider());
		
		/* Triggers */
		LogisticsFailedTrigger = new TriggerSupplierFailed();
		LogisticsNeedPowerTrigger = new TriggerNeedsPower();
		LogisticsCraftingTrigger = new TriggerCrafting();
		LogisticsHasDestinationTrigger = new TriggerHasDestination();
		
		/* Actions */
		LogisticsDisableAction = new ActionDisableLogistics();
	}

	public boolean isUpgradeManagerEquipped(EntityPlayer entityplayer) {
		return entityplayer != null && entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsUpgradeManager;
	}
	
	public void resetItemRotation() {
		try {
			Object renderer = TileEntityRendererDispatcher.instance.mapSpecialRenderers.get(TileGenericPipe.class);
			Field f = PipeRendererTESR.class.getDeclaredField("dummyEntityItem");
			f.setAccessible(true);
			EntityItem item = (EntityItem) f.get(renderer);
			item.hoverStart = 0;
		} catch(NoSuchFieldException e) {
			e.printStackTrace();
		} catch(SecurityException e) {
			e.printStackTrace();
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void registerPipeInformationProvider() {
		SimpleServiceLocator.pipeInformaitonManager.registerProvider(TileGenericPipe.class, BCPipeInformationProvider.class);
	}
	
	public boolean insertIntoBuildcraftPipe(TileEntity tile, LPTravelingItem item) {
		if(tile instanceof TileGenericPipe) {
			TileGenericPipe pipe = (TileGenericPipe)tile;
			if(BlockGenericPipe.isValid(pipe.pipe) && pipe.pipe.transport instanceof PipeTransportItems) {
				TravelingItem bcItem = null;
				if(item instanceof LPTravelingItemServer) {
					LPRoutedBCTravelingItem lpBCItem = new LPRoutedBCTravelingItem();
					lpBCItem.setRoutingInformation(((LPTravelingItemServer)item).getInfo());
					lpBCItem.saveToExtraNBTData();
					bcItem = lpBCItem;
				} else {
					//TODO is this needed ClientSide ?
					//bcItem = TravelingItem.make();
				}
				LPPosition p = new LPPosition(tile.xCoord + 0.5F, tile.yCoord + CoreConstants.PIPE_MIN_POS, tile.zCoord + 0.5F);
				if(item.output.getOpposite() == ForgeDirection.DOWN) {
					p.moveForward(item.output.getOpposite(), 0.24F);
				} else if(item.output.getOpposite() == ForgeDirection.UP) {
					p.moveForward(item.output.getOpposite(), 0.74F);
				} else {
					p.moveForward(item.output.getOpposite(), 0.49F);
				}
				bcItem.setPosition(p.getXD(), p.getYD(), p.getZD());
				bcItem.setSpeed(item.getSpeed());
				if(item.getItemIdentifierStack() != null) {
					bcItem.setItemStack(item.getItemIdentifierStack().makeNormalStack());
				}
				((PipeTransportItems)pipe.pipe.transport).injectItem(bcItem, item.output);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isIPipeTile(TileEntity tile) {
		return tile instanceof IPipeTile;
	}

	@Override
	public boolean checkForPipeConnection(TileEntity with, ForgeDirection side, LogisticsTileGenericPipe pipe) {
		if (with instanceof TileGenericPipe) {
			if (((TileGenericPipe) with).hasPlug(side.getOpposite()))
				return false;
			Pipe otherPipe = ((TileGenericPipe) with).pipe;

			if (!BlockGenericPipe.isValid(otherPipe))
				return false;

			if (!otherPipe.canPipeConnect(pipe, side.getOpposite()))
				return false;
		}
		return true;
	}

	@Override
	public boolean checkConnectionOverride(TileEntity with, ForgeDirection side, LogisticsTileGenericPipe pipe) {
		if (with instanceof IPipeConnection) {
			IPipeConnection.ConnectOverride override = ((IPipeConnection) with).overridePipeConnection(PipeType.ITEM, side.getOpposite());
			if(override == IPipeConnection.ConnectOverride.DISCONNECT) {
				//if it doesn't don't want to connect to item pipes, how about fluids?
				if(pipe.pipe.transport instanceof PipeFluidTransportLogistics || pipe.pipe instanceof PipeItemsFluidSupplier) {
					override = ((IPipeConnection) with).overridePipeConnection(PipeType.FLUID, side.getOpposite());
				}
				if(override == IPipeConnection.ConnectOverride.DISCONNECT) {
					//nope, maybe you'd like some BC power?
					if(pipe.getCPipe().getUpgradeManager().hasBCPowerSupplierUpgrade()) {
						override = ((IPipeConnection) with).overridePipeConnection(PipeType.POWER, side.getOpposite());
					}
				}
			}
			if (override == IPipeConnection.ConnectOverride.DISCONNECT)
				return false;
		}
		return true;
	}

	@Override
	public boolean isMachineManagingSolids(TileEntity tile) {
		return tile instanceof IMachine && ((IMachine)tile).manageSolids();
	}

	@Override
	public boolean isMachineManagingFluids(TileEntity tile) {
		return tile instanceof IMachine && ((IMachine) tile).manageFluids();
	}
	
	@Override
	public boolean handleBCClickOnPipe(ItemStack currentItem, CoreUnroutedPipe pipe, World world, int x, int y, int z, EntityPlayer player, int side, LogisticsBlockGenericPipe block) {
		if(PipeWire.RED.isPipeWire(currentItem)) {
			if(addOrStripWire(player, pipe, PipeWire.RED)) { return true; }
		} else if(PipeWire.BLUE.isPipeWire(currentItem)) {
			if(addOrStripWire(player, pipe, PipeWire.BLUE)) { return true; }
		} else if(PipeWire.GREEN.isPipeWire(currentItem)) {
			if(addOrStripWire(player, pipe, PipeWire.GREEN)) { return true; }
		} else if(PipeWire.YELLOW.isPipeWire(currentItem)) {
			if(addOrStripWire(player, pipe, PipeWire.YELLOW)) { return true; }
		} else if(currentItem.getItem() instanceof ItemGate) {
			if(addOrStripGate(world, x, y, z, player, pipe, block)) { return true; }
		} else if(currentItem.getItem() instanceof ItemPlug) {
			if(addOrStripPlug(world, x, y, z, player, ForgeDirection.getOrientation(side), pipe, block)) { return true; }
		} else if(currentItem.getItem() instanceof ItemRobotStation) {
			if(addOrStripRobotStation(world, x, y, z, player, ForgeDirection.getOrientation(side), pipe, block)) { return true; }
		} else if(currentItem.getItem() instanceof ItemFacade) {
			if(addOrStripFacade(world, x, y, z, player, ForgeDirection.getOrientation(side), pipe, block)) { return true; }
		} else if(currentItem.getItem() instanceof ItemRobot) {
			if(!world.isRemote) {
				RaytraceResult rayTraceResult = block.doRayTrace(world, x, y, z, player);
				
				if(rayTraceResult.hitPart == Part.RobotStation) {
					EntityRobot robot = ((ItemRobot)currentItem.getItem()).createRobot(world);
					
					float px = x + 0.5F + rayTraceResult.sideHit.offsetX * 0.5F;
					float py = y + 0.5F + rayTraceResult.sideHit.offsetY * 0.5F;
					float pz = z + 0.5F + rayTraceResult.sideHit.offsetZ * 0.5F;
					
					robot.setPosition(px, py, pz);
					
					//robot.setDockingStation(pipe.container, rayTraceResult.sideHit);
					robot.dockingStation.x = pipe.container.xCoord;
					robot.dockingStation.y = pipe.container.yCoord;
					robot.dockingStation.z = pipe.container.zCoord;
					robot.dockingStation.side = rayTraceResult.sideHit;
					
					robot.currentAI = new AIDocked();
					world.spawnEntityInWorld(robot);
					
					if(!player.capabilities.isCreativeMode) {
						player.getCurrentEquippedItem().stackSize--;
					}
					
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean addOrStripGate(World world, int x, int y, int z, EntityPlayer player, CoreUnroutedPipe pipe, LogisticsBlockGenericPipe block) {
		if(addGate(player, pipe)) { return true; }
		if(player.isSneaking()) {
			RaytraceResult rayTraceResult = block.doRayTrace(world, x, y, z, player);
			if(rayTraceResult != null && rayTraceResult.hitPart == Part.Gate) {
				if(stripGate(pipe)) { return true; }
			}
		}
		return false;
	}
	
	private boolean addGate(EntityPlayer player, CoreUnroutedPipe pipe) {
		if(!pipe.hasGate()) {
			pipe.gate = GateFactory.makeGate(pipe, player.getCurrentEquippedItem());
			if(!player.capabilities.isCreativeMode) {
				player.getCurrentEquippedItem().splitStack(1);
			}
			pipe.container.scheduleRenderUpdate();
			return true;
		}
		return false;
	}
	
	private boolean stripGate(CoreUnroutedPipe pipe) {
		if(pipe.hasGate()) {
			if(!pipe.container.getWorldObj().isRemote) {
				pipe.gate.dropGate();
			}
			pipe.resetGate();
			return true;
		}
		return false;
	}
	
	private boolean addOrStripWire(EntityPlayer player, CoreUnroutedPipe pipe, PipeWire color) {
		if(addWire(pipe, color)) {
			if(!player.capabilities.isCreativeMode) {
				player.getCurrentEquippedItem().splitStack(1);
			}
			return true;
		}
		return player.isSneaking() && stripWire(pipe, color);
	}
	
	private boolean addWire(CoreUnroutedPipe pipe, PipeWire color) {
		if(!pipe.bcPipePart.getWireSet()[color.ordinal()]) {
			pipe.bcPipePart.getWireSet()[color.ordinal()] = true;
			pipe.bcPipePart.getSignalStrength()[color.ordinal()] = 0;
			pipe.container.scheduleNeighborChange();
			return true;
		}
		return false;
	}
	
	private boolean stripWire(CoreUnroutedPipe pipe, PipeWire color) {
		if(pipe.bcPipePart.getWireSet()[color.ordinal()]) {
			if(!pipe.container.getWorldObj().isRemote) {
				dropWire(color, pipe);
			}
			pipe.bcPipePart.getWireSet()[color.ordinal()] = false;
			pipe.container.scheduleRenderUpdate();
			return true;
		}
		return false;
	}
	
	private boolean addOrStripFacade(World world, int x, int y, int z, EntityPlayer player, ForgeDirection side, CoreUnroutedPipe pipe, LogisticsBlockGenericPipe block) {
		RaytraceResult rayTraceResult = block.doRayTrace(world, x, y, z, player);
		if(player.isSneaking()) {
			if(rayTraceResult != null && rayTraceResult.hitPart == Part.Facade) {
				if(stripFacade(pipe, rayTraceResult.sideHit)) { return true; }
			}
		}
		if(rayTraceResult != null && (rayTraceResult.hitPart != Part.Facade)) {
			if(addFacade(player, pipe, rayTraceResult.sideHit != null && rayTraceResult.sideHit != ForgeDirection.UNKNOWN ? rayTraceResult.sideHit : side)) { return true; }
		}
		return false;
	}
	
	private boolean addFacade(EntityPlayer player, CoreUnroutedPipe pipe, ForgeDirection side) {
		ItemStack stack = player.getCurrentEquippedItem();
		if(stack != null && stack.getItem() instanceof ItemFacade && pipe.container.addFacade(side, ItemFacade.getType(stack), ItemFacade.getWireType(stack), ItemFacade.getBlocks(stack), ItemFacade.getMetaValues(stack))) {
			if(!player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
			return true;
		}
		return false;
	}
	
	private boolean stripFacade(CoreUnroutedPipe pipe, ForgeDirection side) {
		return pipe.container.dropFacade(side);
	}
	
	private boolean addOrStripPlug(World world, int x, int y, int z, EntityPlayer player, ForgeDirection side, CoreUnroutedPipe pipe, LogisticsBlockGenericPipe block) {
		RaytraceResult rayTraceResult = block.doRayTrace(world, x, y, z, player);
		if(player.isSneaking()) {
			if(rayTraceResult != null && rayTraceResult.hitPart == Part.Plug) {
				if(stripPlug(pipe, rayTraceResult.sideHit)) { return true; }
			}
		}
		if(rayTraceResult != null && (rayTraceResult.hitPart == Part.Pipe || rayTraceResult.hitPart == Part.Gate)) {
			if(addPlug(player, pipe, rayTraceResult.sideHit != null && rayTraceResult.sideHit != ForgeDirection.UNKNOWN ? rayTraceResult.sideHit : side)) { return true; }
		}
		return false;
	}
	
	private boolean addOrStripRobotStation(World world, int x, int y, int z, EntityPlayer player, ForgeDirection side, CoreUnroutedPipe pipe, LogisticsBlockGenericPipe block) {
		RaytraceResult rayTraceResult = block.doRayTrace(world, x, y, z, player);
		if(player.isSneaking()) {
			if(rayTraceResult != null && rayTraceResult.hitPart == Part.RobotStation) {
				if(stripRobotStation(pipe, rayTraceResult.sideHit)) { return true; }
			}
		}
		if(rayTraceResult != null && (rayTraceResult.hitPart == Part.Pipe || rayTraceResult.hitPart == Part.Gate)) {
			if(addRobotStation(player, pipe, rayTraceResult.sideHit != null && rayTraceResult.sideHit != ForgeDirection.UNKNOWN ? rayTraceResult.sideHit : side)) { return true; }
		}
		return false;
	}
	
	private boolean addPlug(EntityPlayer player, CoreUnroutedPipe pipe, ForgeDirection side) {
		ItemStack stack = player.getCurrentEquippedItem();
		if(pipe.container.addPlug(side)) {
			if(!player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
			return true;
		}
		return false;
	}
	
	private boolean addRobotStation(EntityPlayer player, CoreUnroutedPipe pipe, ForgeDirection side) {
		ItemStack stack = player.getCurrentEquippedItem();
		if(pipe.container.addRobotStation(side)) {
			if(!player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
			return true;
		}
		return false;
	}
	
	private boolean stripPlug(CoreUnroutedPipe pipe, ForgeDirection side) {
		return pipe.container.removeAndDropPlug(side);
	}
	
	private boolean stripRobotStation(CoreUnroutedPipe pipe, ForgeDirection side) {
		return pipe.container.removeAndDropPlug(side);
	}
	
	@Override
	public boolean stripEquipment(World world, int x, int y, int z, EntityPlayer player, CoreUnroutedPipe pipe, LogisticsBlockGenericPipe block) {
		// Try to strip facades first
		RaytraceResult rayTraceResult = block.doRayTrace(world, x, y, z, player);
		if(rayTraceResult != null && rayTraceResult.hitPart == Part.Facade) {
			if(stripFacade(pipe, rayTraceResult.sideHit)) { return true; }
		}
		
		// Try to strip wires second, starting with yellow.
		for(PipeWire color: PipeWire.values()) {
			if(stripWire(pipe, color)) { return true; }
		}
		
		return stripGate(pipe);
	}
	
	/**
	 * Drops a pipe wire item of the passed color.
	 *
	 * @param pipeWire
	 */
	private void dropWire(PipeWire pipeWire, CoreUnroutedPipe pipe) {
		pipe.dropItem(pipeWire.getStack());
	}
	
	@Override
	public IBCPipePart getBCPipePart(CoreUnroutedPipe pipe) {
		return new BCPipePart(pipe.container);
	}
	
	@Override
	public ItemStack getPipePlugItemStack() {
		return new ItemStack(BuildCraftTransport.plugItem);
	}
	
	@Override
	public ItemStack getRobotTrationItemStack() {
		return new ItemStack(BuildCraftTransport.robotStationItem);
	}
}
