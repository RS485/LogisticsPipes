package logisticspipes.proxy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;

import logisticspipes.LPConstants;
import logisticspipes.asm.wrapper.LogisticsWrapperHandler;
import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.bs.BetterStorageProxy;
import logisticspipes.proxy.bs.ICrateStorageProxy;
import logisticspipes.proxy.buildcraft.subproxies.IBCClickResult;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipePart;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipePluggable;
import logisticspipes.proxy.buildcraft.subproxies.IBCPluggableState;
import logisticspipes.proxy.buildcraft.subproxies.IBCRenderState;
import logisticspipes.proxy.buildcraft.subproxies.IBCRenderTESR;
import logisticspipes.proxy.buildcraft.subproxies.IBCTilePart;
import logisticspipes.proxy.buildcraft.subproxies.IConnectionOverrideResult;
import logisticspipes.proxy.cc.CCProxy;
import logisticspipes.proxy.ccl.CCLProxy;
import logisticspipes.proxy.cofh.CoFHPowerProxy;
import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyReceiver;
import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyStorage;
import logisticspipes.proxy.enderchest.EnderStorageProxy;
import logisticspipes.proxy.factorization.FactorizationProxy;
import logisticspipes.proxy.forestry.ForestryProxy;
import logisticspipes.proxy.ic.IronChestProxy;
import logisticspipes.proxy.ic2.IC2Proxy;
import logisticspipes.proxy.interfaces.IBCProxy;
import logisticspipes.proxy.interfaces.IBetterStorageProxy;
import logisticspipes.proxy.interfaces.ICCLProxy;
import logisticspipes.proxy.interfaces.ICCProxy;
import logisticspipes.proxy.interfaces.ICoFHPowerProxy;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.proxy.interfaces.IEnderIOProxy;
import logisticspipes.proxy.interfaces.IEnderStorageProxy;
import logisticspipes.proxy.interfaces.IFactorizationProxy;
import logisticspipes.proxy.interfaces.IForestryProxy;
import logisticspipes.proxy.interfaces.IIC2Proxy;
import logisticspipes.proxy.interfaces.IIronChestProxy;
import logisticspipes.proxy.interfaces.INEIProxy;
import logisticspipes.proxy.interfaces.IOpenComputersProxy;
import logisticspipes.proxy.interfaces.ITDProxy;
import logisticspipes.proxy.interfaces.IThermalExpansionProxy;
//import logisticspipes.proxy.nei.NEIProxy;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;
import logisticspipes.proxy.object3d.interfaces.IBounds;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.interfaces.IRenderState;
import logisticspipes.proxy.object3d.interfaces.ITranslation;
import logisticspipes.proxy.object3d.interfaces.IVec3;
import logisticspipes.proxy.object3d.interfaces.TextureTransformation;
import logisticspipes.proxy.object3d.operation.LPScale;
import logisticspipes.proxy.opencomputers.IOCTile;
import logisticspipes.proxy.opencomputers.OpenComputersProxy;
import logisticspipes.proxy.td.subproxies.ITDPart;
import logisticspipes.proxy.te.ThermalExpansionProxy;
import logisticspipes.recipes.CraftingParts;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

//@formatter:off
//CHECKSTYLE:OFF

public class ProxyManager {
	public static <T> T getWrappedProxy(String modId, Class<T> interfaze, Class<? extends T> proxyClazz, T dummyProxy, Class<?>... object) {
		try {
			return LogisticsWrapperHandler.getWrappedProxy(modId, interfaze, proxyClazz, dummyProxy, object);
		} catch(Exception e) {
			if(e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e);
		}
	}

	public static void load() {
		SimpleServiceLocator.setBuildCraftProxy(ProxyManager.getWrappedProxy("BuildCraft|Transport+BuildCraft|Silicon+BuildCraft|Robotics", IBCProxy.class, null /*BuildCraftProxy.class*/, new IBCProxy() {
			@Override public void resetItemRotation() {}
			@Override public boolean isIPipeTile(TileEntity tile) {return false;}
			@Override public void registerPipeInformationProvider() {}
			@Override public void initProxy() {}
			@Override public boolean checkForPipeConnection(TileEntity with, EnumFacing side, LogisticsTileGenericPipe pipe) {return true;}
			@Override public IConnectionOverrideResult checkConnectionOverride(TileEntity with, EnumFacing side, LogisticsTileGenericPipe pipe) {
				return new IConnectionOverrideResult() {
					@Override public boolean forceConnect() {return false;}
					@Override public boolean forceDisconnect() {return false;}
				};
			}
			@Override public boolean canPipeConnect(TileEntity pipe, TileEntity tile, EnumFacing direction) {return false;}
			@Override public boolean isActive() {return false;}
			@Override public boolean isInstalled() {return false;}
			@Override public Object getLPPipeType() {return null;}
			@Override public void registerTrigger() {}
			@Override public CraftingParts getRecipeParts() {return null;}
			@Override public void addCraftingRecipes(CraftingParts parts) {}
			@Override public Class<? extends ICraftingRecipeProvider> getAssemblyTableProviderClass() {return null;}
			@Override public void notifyOfChange(LogisticsTileGenericPipe logisticsTileGenericPipe, TileEntity tile, EnumFacing o) {}
			@Override public IBCTilePart getBCTilePart(LogisticsTileGenericPipe logisticsTileGenericPipe) {
				return new IBCTilePart() {
					@Override public boolean hasBlockingPluggable(EnumFacing side) {return false;}
					@Override public void writeToNBT_LP(NBTTagCompound nbt) {}
					@Override public void readFromNBT_LP(NBTTagCompound nbt) {}
					@Override public boolean isSolidOnSide(EnumFacing side) {return false;}
					@Override public void invalidate_LP() {}
					@Override public void validate_LP() {}
					@Override public void updateEntity_LP() {}
					@Override public void scheduleNeighborChange() {}
					@Override public boolean hasGate(EnumFacing orientation) {return false;}
					@Override public IBCRenderState getBCRenderState() {
						return new IBCRenderState() {
							@Override public boolean needsRenderUpdate() {return false;}
							@Override public boolean isDirty() {return false;}
							@Override public void writeData_LP(LPDataOutput output)  {
								output.writeBoolean(false);
							}
							@Override public void readData_LP(LPDataInput input) {}
							@Override public void clean() {}
						};
					}
					@Override public IBCPipePart getBCPipePart() {
						return new IBCPipePart() {
							@Override public boolean canConnectRedstone() {return false;}
							@Override public int isPoweringTo(EnumFacing l) {return 0;}
							@Override public int isIndirectlyPoweringTo(EnumFacing l) {return 0;}
							@Override public Object getClientGui(InventoryPlayer inventory, int side) {return null;}
							@Override public Container getGateContainer(InventoryPlayer inventory, int side) {return null;}
							@Override public void addItemDrops(ArrayList<ItemStack> result) {}
							@Override public Object getOriginal() {return null;}
						};
					}
					@Override public IBCPluggableState getBCPlugableState() {
						return new IBCPluggableState() {
							@Override public void writeData(LPDataOutput output)  {}
							@Override public void readData(LPDataInput input)  {}
							@Override public boolean isDirty(boolean clean) {return false;}
						};
					}
					@Override public boolean hasEnabledFacade(EnumFacing dir) {return false;}
					@Override public IBCPipePluggable getBCPipePluggable(EnumFacing sideHit) {
						return new IBCPipePluggable() {
							@Override public ItemStack[] getDropItems(LogisticsTileGenericPipe container) {return new ItemStack[]{};}
							@Override public boolean isBlocking() {return false;}
							@Override public Object getOriginal() {return null;}
							@Override @SideOnly(Side.CLIENT) public void renderPluggable(EnumFacing dir, int renderPass, int x, int y, int z) {}
							@Override public boolean isAcceptingItems(LPTravelingItemServer arrivingItem) {return false;}
							@Override public LPTravelingItemServer handleItem(LPTravelingItemServer arrivingItem) {return arrivingItem;}
						};
					}
					@Override public void readOldRedStone(NBTTagCompound nbt) {}
					@Override public void afterStateUpdated() {}
					@Override public Object getOriginal() {return null;}
					@Override public boolean hasPipePluggable(EnumFacing dir) {return false;}
					@Override public void setWorld_LP(World world) {}
				};
			}
			@Override public IBCClickResult handleBCClickOnPipe(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float xOffset, float yOffset, float zOffset, CoreUnroutedPipe pipe) {
				return new IBCClickResult() {
					@Override public boolean handled() {return false;}
					@Override public boolean blocked() {return false;}
				};
			}
			@Override public void callBCNeighborBlockChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {}
			@Override public void callBCRemovePipe(World world, int x, int y, int z) {}
			@Override public void logWarning(String format) {}
			@Override public IBCRenderTESR getBCRenderTESR() {
				return new IBCRenderTESR() {
					@Override public void renderWires(LogisticsTileGenericPipe pipe, double x, double y, double z) {}
					@Override public void dynamicRenderPluggables(LogisticsTileGenericPipe pipe, double x, double y, double z) {}
				};
			}
			@Override public boolean isTileGenericPipe(TileEntity tile) {return false;}
			@Override public void cleanup() {}
		}, IBCTilePart.class, IBCPipePart.class, IBCPipePluggable.class, IBCPluggableState.class, IBCRenderState.class, IBCRenderTESR.class));

		SimpleServiceLocator.setForestryProxy(ProxyManager.getWrappedProxy("Forestry", IForestryProxy.class, ForestryProxy.class, new IForestryProxy() {
			@Override public boolean isBee(ItemStack item) {return false;}
			@Override public boolean isBee(ItemIdentifier item) {return false;}
			@Override public boolean isAnalysedBee(ItemStack item) {return false;}
			@Override public boolean isAnalysedBee(ItemIdentifier item) {return false;}
			@Override public boolean isTileAnalyser(TileEntity tile) {return false;}
			@Override public boolean isKnownAlleleId(String uid, World world) {return false;}
			@Override public String getAlleleName(String uid) {return "";}
			@Override public String getFirstAlleleId(ItemStack bee) {return "";}
			@Override public String getSecondAlleleId(ItemStack bee) {return "";}
			@Override public boolean isDrone(ItemStack bee) {return false;}
			@Override public boolean isFlyer(ItemStack bee) {return false;}
			@Override public boolean isPrincess(ItemStack bee) {return false;}
			@Override public boolean isQueen(ItemStack bee) {return false;}
			@Override public boolean isPurebred(ItemStack bee) {return false;}
			@Override public boolean isNocturnal(ItemStack bee) {return false;}
			@Override public boolean isPureNocturnal(ItemStack bee) {return false;}
			@Override public boolean isPureFlyer(ItemStack bee) {return false;}
			@Override public boolean isCave(ItemStack bee) {return false;}
			@Override public boolean isPureCave(ItemStack bee) {return false;}
			@Override public String getForestryTranslation(String input) {return input.substring(input.lastIndexOf(".") + 1).toLowerCase(Locale.US).replace("_", " ");}
			@Override @SideOnly(Side.CLIENT) public TextureAtlasSprite getIconIndexForAlleleId(String id, int phase) {return null;}
			@Override @SideOnly(Side.CLIENT) public int getColorForAlleleId(String id, int phase) {return 16777215;}
			@Override @SideOnly(Side.CLIENT) public int getRenderPassesForAlleleId(String id) {return 0;}
			@Override public void addCraftingRecipes(CraftingParts parts) {}
			@Override public String getNextAlleleId(String uid, World world) {return "";}
			@Override public String getPrevAlleleId(String uid, World world) {return "";}
			@Override @SideOnly(Side.CLIENT) public TextureAtlasSprite getIconFromTextureManager(String name) {return null;}
			@Override public void syncTracker(World world, EntityPlayer player) {}
		}));

		SimpleServiceLocator.setElectricItemProxy(ProxyManager.getWrappedProxy("IC2", IIC2Proxy.class, IC2Proxy.class, new IIC2Proxy() {
			@Override public boolean isElectricItem(ItemStack stack) {return false;}
			@Override public boolean isSimilarElectricItem(ItemStack stack, ItemStack template) {return false;}
			@Override public boolean isFullyCharged(ItemStack stack) {return false;}
			@Override public boolean isFullyDischarged(ItemStack stack) {return false;}
			@Override public boolean isPartiallyCharged(ItemStack stack) {return false;}
			@Override public void addCraftingRecipes(CraftingParts parts) {}
			@Override public boolean hasIC2() {return false;}
			@Override public void registerToEneryNet(TileEntity tile) {}
			@Override public void unregisterToEneryNet(TileEntity tile) {}
			@Override public boolean acceptsEnergyFrom(TileEntity tile1, TileEntity tile2, EnumFacing opposite) {return false;}
			@Override public boolean isEnergySink(TileEntity tile) {return false;}
			@Override public double demandedEnergyUnits(TileEntity tile) {return 0;}
			@Override public double injectEnergyUnits(TileEntity tile, EnumFacing opposite, double d) {return d;}
		}));

		SimpleServiceLocator.setCCProxy(ProxyManager.getWrappedProxy(LPConstants.computerCraftModID, ICCProxy.class, CCProxy.class, new ICCProxy() {
			@Override public boolean isTurtle(TileEntity tile) {return false;}
			@Override public boolean isComputer(TileEntity tile) {return false;}
			@Override public boolean isCC() {return false;}
			@Override public boolean isLuaThread(Thread thread) {return false;}
			@Override public void queueEvent(String event, Object[] arguments, LogisticsTileGenericPipe logisticsTileGenericPipe) {}
			@Override public void setTurtleConnect(boolean flag, LogisticsTileGenericPipe logisticsTileGenericPipe) {}
			@Override public boolean getTurtleConnect(LogisticsTileGenericPipe logisticsTileGenericPipe) {return false;}
			@Override public int getLastCCID(LogisticsTileGenericPipe logisticsTileGenericPipe) {return 0;}
			@Override public void handleMesssage(int computerId, Object message, LogisticsTileGenericPipe tile, int sourceId) {}
			@Override public void addCraftingRecipes(CraftingParts parts) {}
			@Override public Object getAnswer(Object object) {return object;}
		}));

		SimpleServiceLocator.setThermalExpansionProxy(ProxyManager.getWrappedProxy("ThermalExpansion", IThermalExpansionProxy.class, ThermalExpansionProxy.class, new IThermalExpansionProxy() {
			@Override public boolean isTE() {return false;}
			@Override public CraftingParts getRecipeParts() {return null;}
			@Override public boolean isToolHammer(Item stack) {return false;}
			@Override public boolean canHammer(ItemStack stack, EntityPlayer entityplayer, BlockPos pos) {return false;}
			@Override public void toolUsed(ItemStack stack, EntityPlayer entityplayer, BlockPos pos) {}
		}));

		SimpleServiceLocator.setBetterStorageProxy(ProxyManager.getWrappedProxy("betterstorage", IBetterStorageProxy.class, BetterStorageProxy.class, new IBetterStorageProxy() {
			@Override public boolean isBetterStorageCrate(TileEntity tile) {return false;}
			@Override public ICrateStorageProxy getCrateStorageProxy(TileEntity tile) {
				return new ICrateStorageProxy() {
					@Override public Iterable<ItemStack> getContents() {return null;}
					@Override public int getUniqueItems() {return 0;}
					@Override public int getItemCount(ItemStack stack) {return 0;}
					@Override public ItemStack extractItems(ItemStack stack, int count) {return null;}
					@Override public int getSpaceForItem(ItemStack stack) {return 0;}
					@Override public ItemStack insertItems(ItemStack stack) {return stack;}
				};
			}
		}, ICrateStorageProxy.class));

		SimpleServiceLocator.setNEIProxy(ProxyManager.getWrappedProxy("NotEnoughItems", INEIProxy.class, null /*NEIProxy.class*/, new INEIProxy() {
			@Override public List<String> getInfoForPosition(World world, EntityPlayer player, RayTraceResult objectMouseOver) {return new ArrayList<>(0);}
			@Override @SideOnly(Side.CLIENT) public boolean renderItemToolTip(int posX, int posY, List<String> msg, TextFormatting rarityColor, ItemStack stack) {return false;}
			@Override @SideOnly(Side.CLIENT) public List<String> getItemToolTip(ItemStack stack, EntityPlayer thePlayer, ITooltipFlag advancedItemTooltips, GuiContainer screen) {return stack.getTooltip(thePlayer, advancedItemTooltips);}
			@Override public ItemStack getItemForPosition(World world, EntityPlayer player, RayTraceResult objectMouseOver) {return null;}
		}));

		SimpleServiceLocator.setFactorizationProxy(ProxyManager.getWrappedProxy("factorization", IFactorizationProxy.class, FactorizationProxy.class, tile-> false));

		SimpleServiceLocator.setEnderIOProxy(ProxyManager.getWrappedProxy("EnderIO", IEnderIOProxy.class, null/*EnderIOProxy.class*/, new IEnderIOProxy() {
			@Override public boolean isSendAndReceive(TileEntity tile) {return false;}
			@Override public boolean isTransceiver(TileEntity tile) {return false;}
			@Override public List<TileEntity> getConnectedTransceivers(TileEntity tile) {return null;}
			@Override public boolean isEnderIO() {return false;}
			@Override public boolean isItemConduit(TileEntity tile, EnumFacing dir) {return false;}
			@Override public boolean isFluidConduit(TileEntity tile, EnumFacing dir) {return false;}
			@Override public boolean isBundledPipe(TileEntity tile) {return false;}
		}));

		SimpleServiceLocator.setIronChestProxy(ProxyManager.getWrappedProxy("IronChest", IIronChestProxy.class, IronChestProxy.class, new IIronChestProxy() {
			@Override public boolean isIronChest(TileEntity tile) {return false;}
			@Override public @SideOnly(Side.CLIENT) boolean isChestGui(GuiScreen gui) {return false;}
		}));

		SimpleServiceLocator.setEnderStorageProxy(ProxyManager.getWrappedProxy("EnderStorage", IEnderStorageProxy.class, EnderStorageProxy.class, new IEnderStorageProxy() {
			@Override public boolean isEnderChestBlock(Block block) {return false;}
			@Override public void openEnderChest(World world, int x, int y, int z, EntityPlayer player) {}
		}));

		SimpleServiceLocator.setOpenComputersProxy(ProxyManager.getWrappedProxy(LPConstants.openComputersModID, IOpenComputersProxy.class, OpenComputersProxy.class, new IOpenComputersProxy() {
			@Override public void initLogisticsTileGenericPipe(LogisticsTileGenericPipe tile) {}
			@Override public void initLogisticsSolidTileEntity(LogisticsSolidTileEntity tile) {}
			@Override public void handleWriteToNBT(IOCTile tile, NBTTagCompound nbt) {}
			@Override public void handleReadFromNBT(IOCTile tile, NBTTagCompound nbt) {}
			@Override public void handleInvalidate(IOCTile tile) {}
			@Override public void handleChunkUnload(IOCTile tile) {}
			@Override public void addToNetwork(TileEntity tile) {}
		}));

/*		SimpleServiceLocator.setToolWrenchProxy(ProxyManager.getWrappedProxy("!IToolWrench", IToolWrenchProxy.class, ToolWrenchProxy.class, new IToolWrenchProxy() {
			@Override public void wrenchUsed(EntityPlayer entityplayer, int x, int y, int z) {}
			@Override public boolean isWrenchEquipped(EntityPlayer entityplayer) {return false;}
			@Override public boolean canWrench(EntityPlayer entityplayer, int x, int y, int z) {return false;}
			@Override public boolean isWrench(Item item) {return false;}
		}));*/

		SimpleServiceLocator.setCoFHPowerProxy(ProxyManager.getWrappedProxy("CoFHAPI|energy", ICoFHPowerProxy.class, CoFHPowerProxy.class, new ICoFHPowerProxy() {
			@Override public boolean isEnergyReceiver(TileEntity tile) {return false;}
			@Override public ICoFHEnergyReceiver getEnergyReceiver(TileEntity tile) {
				return new ICoFHEnergyReceiver() {
					@Override public int getMaxEnergyStored(EnumFacing opposite) {return 0;}
					@Override public int getEnergyStored(EnumFacing opposite) {return 0;}
					@Override public boolean canConnectEnergy(EnumFacing opposite) {return false;}
					@Override public int receiveEnergy(EnumFacing opposite, int i, boolean b) {return 0;}
				};
			}
			@Override public void addCraftingRecipes(CraftingParts parts) {}
			@Override public ICoFHEnergyStorage getEnergyStorage(int i) {
				return new ICoFHEnergyStorage() {
					@Override public int extractEnergy(int space, boolean b) {return 0;}
					@Override public int receiveEnergy(int maxReceive, boolean simulate) {return 0;}
					@Override public int getEnergyStored() {return 0;}
					@Override public int getMaxEnergyStored() {return 0;}
					@Override public void readFromNBT(NBTTagCompound nbt) {}
					@Override public void writeToNBT(NBTTagCompound nbt) {}
				};
			}
			@Override public boolean isAvailable() {return false;}
		}, ICoFHEnergyReceiver.class, ICoFHEnergyStorage.class));

		SimpleServiceLocator.setThermalDynamicsProxy(ProxyManager.getWrappedProxy("ThermalDynamics", ITDProxy.class, null /*ThermalDynamicsProxy.class */, new ITDProxy() {
			@Override public ITDPart getTDPart(final LogisticsTileGenericPipe pipe) {
				return new ITDPart() {
					@Override public TileEntity getInternalDuctForSide(EnumFacing opposite) {return pipe;}
					@Override public void setWorld_LP(World world) {}
					@Override public void invalidate() {}
					@Override public void onChunkUnload() {}
					@Override public void scheduleNeighborChange() {}
					@Override public void connectionsChanged() {}
				};
			}
			@Override public boolean isActive() {return false;}
			@Override public void registerPipeInformationProvider() {}
			@Override public boolean isItemDuct(TileEntity tile) {return false;}
			@Override @SideOnly(Side.CLIENT) public void renderPipeConnections(LogisticsTileGenericPipe pipeTile) {}
			@Override public void registerTextures(TextureMap iconRegister) {}
			@Override public boolean isBlockedSide(TileEntity with, EnumFacing opposite) {return false;}
		}, ITDPart.class));
		
		final IBounds dummyBounds = new IBounds() {
			@Override public IVec3 min() {
				return new IVec3() {
					@Override public double x() {return 0;}
					@Override public double y() {return 0;}
					@Override public double z() {return 0;}
					@Override public Object getOriginal() {return null;}
				};
			}
			@Override public IVec3 max() {
				return new IVec3() {
					@Override public double x() {return 0;}
					@Override public double y() {return 0;}
					@Override public double z() {return 0;}
					@Override public Object getOriginal() {return null;}
				};
			}
			@Override public AxisAlignedBB toAABB() {return null;}
		};
		final IModel3D dummy3DModel = new IModel3D() {
			@Override public IModel3D backfacedCopy() {return this;}
			@Override public void render(I3DOperation... i3dOperations) {}
			@Override public List<BakedQuad> renderToQuads(VertexFormat format, I3DOperation... i3dOperations) {return Lists.newArrayList();}
			@Override public void computeNormals() {}
			@Override public void computeStandardLighting() {}
			@Override public IBounds bounds() {
				return dummyBounds;
			}
			@Override public IModel3D apply(I3DOperation translation) {return this;}
			@Override public IModel3D copy() {return this;}
			@Override public IModel3D twoFacedCopy() {return this;}
			@Override public Object getOriginal() {return this;}
			@Override public IBounds getBoundsInside(AxisAlignedBB boundingBox) {
				return dummyBounds;
			}
		};
		ICCLProxy dummyCCLProxy = new ICCLProxy() {
			@Override public TextureTransformation createIconTransformer(TextureAtlasSprite registerIcon) {
				return new TextureTransformation() {
					@Override public Object getOriginal() {return null;}
					@Override public void update(TextureAtlasSprite registerIcon) {}
					@Override public TextureAtlasSprite getTexture() {return null;}
				};
			}
			@Override public IRenderState getRenderState() {
				return new IRenderState() {
					@Override public void reset() {}
					@Override public void setAlphaOverride(int i) {}
					@Override public void draw() {}
					@Override public void setBrightness(IBlockAccess world, BlockPos pos) {}
					@Override public void startDrawing(int mode, VertexFormat format) {}
				};
			}
			@Override public Map<String, IModel3D> parseObjModels(InputStream resourceAsStream, int i, LPScale scale) {return new HashMap<>();}
			@Override public Object getRotation(int i, int j) {return null;}
			@Override public Object getScale(double d, double e, double f) {return null;}
			@Override public Object getScale(double d) {return null;}
			@Override public ITranslation getTranslation(double d, double e, double f) {
				return new ITranslation() {
					@Override public ITranslation inverse() {return this;}
					@Override public Object getOriginal() {return null;}
				};
			}
			@Override public ITranslation getTranslation(IVec3 min) {
				return new ITranslation() {
					@Override public ITranslation inverse() {return this;}
					@Override public Object getOriginal() {return null;}
				};
			}
			@Override public Object getUVScale(double i, double d) {return null;}
			@Override public Object getUVTranslation(float i, float f) {return null;}
			@Override public Object getUVTransformationList(I3DOperation[] uvTranslation) {return null;}
			@Override public IModel3D wrapModel(Object model) {
				return dummy3DModel;
			}
			@Override public boolean isActivated() {return false;}
			@Override public Object getRotation(double d, int i, int j, int k) {return null;}
			@Override public IModel3D combine(Collection<IModel3D> list) {
				return dummy3DModel;
			}
			@Override public Object getColourMultiplier(int i) {return null;}
		};
		Class<?>[] cclSubWrapper = new Class<?>[] {TextureTransformation.class, IRenderState.class, IModel3D.class, ITranslation.class, IVec3.class, IBounds.class};
		SimpleServiceLocator.setCCLProxy(ProxyManager.getWrappedProxy("!CCLRender", ICCLProxy.class, CCLProxy.class, dummyCCLProxy, cclSubWrapper));
		SimpleServiceLocator.setToolWrenchHandler(new ToolWrenchHandler());
	}
}
