package logisticspipes.proxy.buildcraft;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.VersionNotSupportedException;
import logisticspipes.proxy.buildcraft.gates.ActionDisableLogistics;
import logisticspipes.proxy.buildcraft.gates.LogisticsTriggerProvider;
import logisticspipes.proxy.buildcraft.gates.TriggerCrafting;
import logisticspipes.proxy.buildcraft.gates.TriggerHasDestination;
import logisticspipes.proxy.buildcraft.gates.TriggerNeedsPower;
import logisticspipes.proxy.buildcraft.gates.TriggerSupplierFailed;
import logisticspipes.proxy.buildcraft.recipeprovider.AssemblyTable;
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
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.utils.ReflectionHelper;
import logisticspipes.utils.tuples.LPPosition;
import lombok.SneakyThrows;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftSilicon;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.BCLog;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.CoreConstants;
import buildcraft.core.ITileBufferHolder;
import buildcraft.core.ItemMapLocation;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.ItemGateCopier;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.render.FacadeRenderHelper;
import buildcraft.transport.render.FakeBlock;
import buildcraft.transport.render.PipeRendererTESR;
import buildcraft.transport.render.PipeRendererWorld;

public class BuildCraftProxy implements IBCProxy {

	public static ITriggerInternal LogisticsFailedTrigger;
	public static ITriggerInternal LogisticsCraftingTrigger;
	public static ITriggerExternal LogisticsNeedPowerTrigger;
	public static ITriggerInternal LogisticsHasDestinationTrigger;
	public static IActionInternal LogisticsDisableAction;
	
	private Method canPipeConnect;

	public PipeType logisticsPipeType;
	public PipeType wrapperPipeType;
	
	public BuildCraftProxy() {
		String BCVersion = null;
		try {
			Field versionField = buildcraft.core.Version.class.getDeclaredField("VERSION");
			BCVersion = (String) versionField.get(null);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(BCVersion != null && !BCVersion.equals("@VERSION@")) {
			if(!BCVersion.contains("6.3") || BCVersion.contains("6.3.0") || BCVersion.contains("6.3.1")) {
				throw new VersionNotSupportedException("BC", BCVersion, "6.3.2", "");
			} else if(!BCVersion.contains("6.3.2")) {
				LogisticsPipes.log.error("The BC version you are using is untested with this LP version. It may work but it could also destroy your world. Use at your own risk. Recommanded BC version for this LP version is BC 6.2.0");
			}
		} else {
			LogisticsPipes.log.info("Couldn't check the BC Version.");
		}
	}
	
	@Override
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

	@Override
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
					return true;
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
	public void registerPipeInformationProvider() {
		SimpleServiceLocator.pipeInformaitonManager.registerProvider(TileGenericPipe.class, BCPipeInformationProvider.class);
	}

	@Override
	public void initProxy() {
		try {
			canPipeConnect = TileGenericPipe.class.getDeclaredMethod("canPipeConnect", new Class[]{TileEntity.class, ForgeDirection.class});
			canPipeConnect.setAccessible(true);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean checkForPipeConnection(TileEntity with, ForgeDirection side, LogisticsTileGenericPipe pipe) {
		if (with instanceof TileGenericPipe) {
			if (ReflectionHelper.invokePrivateMethodCatched(Boolean.class, TileGenericPipe.class, with, "hasBlockingPluggable", new Class[]{ForgeDirection.class}, new Object[]{side.getOpposite()}).booleanValue()) //((TileGenericPipe) with).hasBlockingPluggable(side.getOpposite())
				return false;
			Pipe otherPipe = ((TileGenericPipe) with).pipe;

			if (!BlockGenericPipe.isValid(otherPipe))
				return false;
			
			if(!(otherPipe.transport instanceof PipeTransportItems))
				return false;
		}
		return true;
	}

	@Override
	public IConnectionOverrideResult checkConnectionOverride(TileEntity with, ForgeDirection side, LogisticsTileGenericPipe pipe) {
		if (with instanceof IPipeConnection) {
			IPipeConnection.ConnectOverride override = ((IPipeConnection) with).overridePipeConnection(PipeType.ITEM, side.getOpposite());
			if(override == IPipeConnection.ConnectOverride.DISCONNECT) {
				//if it doesn't want to connect to item pipes, how about fluids?
				if(pipe.pipe.transport instanceof PipeFluidTransportLogistics || pipe.pipe instanceof PipeItemsFluidSupplier) {
					override = ((IPipeConnection) with).overridePipeConnection(PipeType.FLUID, side.getOpposite());
				}
				if(override == IPipeConnection.ConnectOverride.DISCONNECT) {
					//nope, maybe you'd like some BC power?
					if(pipe.getCPipe().getUpgradeManager().hasRFPowerSupplierUpgrade()) {
						override = ((IPipeConnection) with).overridePipeConnection(PipeType.POWER, side.getOpposite());
					}
				}
			}
			if (override == IPipeConnection.ConnectOverride.DISCONNECT) {
				return new IConnectionOverrideResult() {
					@Override public boolean forceConnect() {return false;}
					@Override public boolean forceDisconnect() {return true;}
				};
			}
			
			if(override == IPipeConnection.ConnectOverride.CONNECT) {
				return new IConnectionOverrideResult() {
					@Override public boolean forceConnect() {return true;}
					@Override public boolean forceDisconnect() {return false;}
				};
			}
		}
		return new IConnectionOverrideResult() {
			@Override public boolean forceConnect() {return false;}
			@Override public boolean forceDisconnect() {return false;}
		};
	}

	@Override
	public boolean canPipeConnect(TileEntity pipe, TileEntity with, ForgeDirection side) {
		if(canPipeConnect == null) {
			initProxy();
		}
		if(!(pipe instanceof TileGenericPipe)) throw new IllegalArgumentException();
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
		if(logisticsPipeType == null) {
			logisticsPipeType = net.minecraftforge.common.util.EnumHelper.addEnum(PipeType.class, "LOGISTICS", new Class<?>[]{}, new Object[]{});
			wrapperPipeType = net.minecraftforge.common.util.EnumHelper.addEnum(PipeType.class, "WRAPPED-LOGISTICS", new Class<?>[]{}, new Object[]{});
		}
		return logisticsPipeType;
	}

	@Override
	public void registerTrigger() {
		StatementManager.registerTriggerProvider(new LogisticsTriggerProvider());
		/* Triggers */
		LogisticsFailedTrigger = new TriggerSupplierFailed();
		LogisticsNeedPowerTrigger = new TriggerNeedsPower();
		LogisticsCraftingTrigger = new TriggerCrafting();
		LogisticsHasDestinationTrigger = new TriggerHasDestination();
		/* Actions */
		LogisticsDisableAction = new ActionDisableLogistics();
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
	@SneakyThrows({NoSuchFieldException.class, SecurityException.class, IllegalArgumentException.class, IllegalAccessException.class})
	public IBCTilePart getBCTilePart(LogisticsTileGenericPipe pipe) {
		return new LPBCTileGenericPipe(new LPBCPipe(new LPBCPipeTransportsItems(pipe), pipe), pipe);
	}

	@Override
	@SneakyThrows({SecurityException.class, IllegalAccessException.class, IllegalArgumentException.class, NoSuchFieldException.class})
	public IBCClickResult handleBCClickOnPipe(World world, int x, int y, int z, EntityPlayer player, int side, float xOffset, float yOffset, float zOffset, CoreUnroutedPipe pipe) {
		Object trace = BuildCraftTransport.genericPipeBlock.doRayTrace(world, x, y, z, player);
		String type = "null";
		if(trace != null) {
			Enum<?> hitPart = ReflectionHelper.getPrivateField(Enum.class, trace.getClass(), "hitPart", trace);
			if(hitPart != null) {
				type = hitPart.name();
			}
		}
		if("Pluggable".equals(type)) {
			pipe.container.tilePart.disablePluggableAccess();
		}
		final boolean result = BuildCraftTransport.genericPipeBlock.onBlockActivated(world, x, y, z, player, side, xOffset, yOffset, zOffset);
		if("Pluggable".equals(type)) {
			pipe.container.tilePart.reenablePluggableAccess();
		}
		boolean block = false;
		if(!result) {
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
				}
			}
		}
		final boolean fBlock = block;
		return new IBCClickResult() {
			@Override
			public boolean handled() {
				return result;
			}

			@Override
			public boolean blocked() {
				return fBlock;
			}};
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
		final PipeRendererTESR renderer = new PipeRendererTESR();
		renderer.func_147497_a(TileEntityRendererDispatcher.instance);
		return new IBCRenderTESR() {
			@Override
			@SneakyThrows(Exception.class)
			public void renderWires(LogisticsTileGenericPipe pipe, double x, double y, double z) {
				TileGenericPipe tgPipe = (TileGenericPipe) pipe.tilePart.getOriginal();
				ReflectionHelper.invokePrivateMethod(Object.class, PipeRendererTESR.class, renderer, "renderGatesWires", new Class[]{TileGenericPipe.class, double.class, double.class, double.class}, new Object[]{tgPipe, x, y, z});
			}

			@Override
			@SneakyThrows(Exception.class)
			public void renderGates(LogisticsTileGenericPipe pipe, double x, double y, double z) {
				TileGenericPipe tgPipe = (TileGenericPipe) pipe.tilePart.getOriginal();
				ReflectionHelper.invokePrivateMethod(Object.class, PipeRendererTESR.class, renderer, "renderGates", new Class[]{TileGenericPipe.class, double.class, double.class, double.class}, new Object[]{tgPipe, x, y, z});
			}
		};
	}

	@Override
	public void pipeFacadeRenderer(RenderBlocks renderblocks, LogisticsBlockGenericPipe block, LogisticsTileGenericPipe pipe, int x, int y, int z, int renderPass) {
		TileGenericPipe tile = (TileGenericPipe) pipe.tilePart.getOriginal();
		PipeRendererWorld.renderPass = renderPass;
		FacadeRenderHelper.pipeFacadeRenderer(renderblocks, FakeBlock.INSTANCE, tile, tile.renderState, x, y, z);
	}
}
