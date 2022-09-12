package logisticspipes.proxy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;

import logisticspipes.LPConstants;
import logisticspipes.asm.wrapper.LogisticsWrapperHandler;
import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipeCapabilityProvider;
import logisticspipes.proxy.cc.CCProxy;
import logisticspipes.proxy.ccl.CCLProxy;
import logisticspipes.proxy.enderchest.EnderStorageProxy;
import logisticspipes.proxy.ic.IronChestProxy;
import logisticspipes.proxy.ic2.IC2Proxy;
import logisticspipes.proxy.interfaces.IBCProxy;
import logisticspipes.proxy.interfaces.ICCLProxy;
import logisticspipes.proxy.interfaces.ICCProxy;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.proxy.interfaces.IEnderIOProxy;
import logisticspipes.proxy.interfaces.IEnderStorageProxy;
import logisticspipes.proxy.interfaces.IIC2Proxy;
import logisticspipes.proxy.interfaces.IIronChestProxy;
import logisticspipes.proxy.interfaces.INEIProxy;
import logisticspipes.proxy.interfaces.IOpenComputersProxy;
import logisticspipes.proxy.interfaces.ITDProxy;
import logisticspipes.proxy.interfaces.IThermalExpansionProxy;
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
import logisticspipes.proxy.td.ThermalDynamicsProxy;
import logisticspipes.proxy.td.subproxies.ITDPart;
import logisticspipes.proxy.te.ThermalExpansionProxy;
import logisticspipes.recipes.CraftingParts;
import logisticspipes.renderer.newpipe.RenderEntry;
import network.rs485.logisticspipes.proxy.mcmp.IMCMPProxy;
import network.rs485.logisticspipes.proxy.mcmp.MCMPProxy;
import network.rs485.logisticspipes.proxy.mcmp.subproxy.IMCMPBlockAccess;
import network.rs485.logisticspipes.proxy.mcmp.subproxy.IMCMPLTGPCompanion;

public class ProxyManager {

	public static <T> T getWrappedProxy(String modId, Class<T> interfaze, Class<? extends T> proxyClazz, T dummyProxy, Class<?>... wrapperInterfaces) {
		try {
			return LogisticsWrapperHandler.getWrappedProxy(modId, interfaze, proxyClazz, dummyProxy, wrapperInterfaces);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e);
		}
	}

	public static void load() {
		//@formatter:off
		//CHECKSTYLE:OFF

		SimpleServiceLocator.setBuildCraftProxy(ProxyManager.getWrappedProxy(LPConstants.bcTransportModID + "+" + LPConstants.bcSiliconModID, IBCProxy.class, BuildCraftProxy.class, new IBCProxy() {
			@Override public void registerPipeInformationProvider() {}
			@Override public void initProxy() {}
			@Override public boolean isActive() {return false;}
			@Override public boolean isInstalled() {return false;}
			@Override public CraftingParts getRecipeParts() {return null;}
			@Override public void addCraftingRecipes(CraftingParts parts) {}
			@Override public Class<? extends ICraftingRecipeProvider> getAssemblyTableProviderClass() {return null;}
			@Override public void registerInventoryHandler() {}
			@Override public IBCPipeCapabilityProvider getIBCPipeCapabilityProvider(LogisticsTileGenericPipe pipe) {
				return new IBCPipeCapabilityProvider() {
					@Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {return false;}
					@Nullable@Override public<T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {return null;}
				};
			}
			@Override public Object createMjReceiver(@Nonnull LogisticsPowerJunctionTileEntity te) {return null;}
			@Override public boolean isBuildCraftPipe(TileEntity tile) {return false;}
		}, IBCPipeCapabilityProvider.class));

		SimpleServiceLocator.setElectricItemProxy(ProxyManager.getWrappedProxy(LPConstants.ic2ModID, IIC2Proxy.class, IC2Proxy.class, new IIC2Proxy() {
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

		SimpleServiceLocator.setThermalExpansionProxy(ProxyManager.getWrappedProxy(LPConstants.thermalExpansionModID, IThermalExpansionProxy.class, ThermalExpansionProxy.class, new IThermalExpansionProxy() {
			@Override public boolean isTE() {return false;}
			@Override public CraftingParts getRecipeParts() {return null;}
			@Override public boolean isToolHammer(Item stack) {return false;}
			@Override public boolean canHammer(@Nonnull ItemStack stack, EntityPlayer entityplayer, BlockPos pos) {return false;}
			@Override public void toolUsed(@Nonnull ItemStack stack, EntityPlayer entityplayer, BlockPos pos) {}
		}));

		SimpleServiceLocator.setNEIProxy(ProxyManager.getWrappedProxy(LPConstants.neiModID, INEIProxy.class, null /*NEIProxy.class*/, new INEIProxy() {
			@Override public List<String> getInfoForPosition(World world, EntityPlayer player, RayTraceResult objectMouseOver) {return new ArrayList<>(0);}
			@Override @SideOnly(Side.CLIENT) public boolean renderItemToolTip(int posX, int posY, List<String> msg, TextFormatting rarityColor, @Nonnull ItemStack stack) {return false;}
			@Override @SideOnly(Side.CLIENT) public List<String> getItemToolTip(@Nonnull ItemStack stack, EntityPlayer thePlayer, ITooltipFlag advancedItemTooltips, GuiContainer screen) {return stack.getTooltip(thePlayer, advancedItemTooltips);}
			@Override public@Nonnull  ItemStack getItemForPosition(World world, EntityPlayer player, RayTraceResult objectMouseOver) {return null;}
		}));

		SimpleServiceLocator.setEnderIOProxy(ProxyManager.getWrappedProxy(LPConstants.enderioModID, IEnderIOProxy.class, null/*EnderIOProxy.class*/, new IEnderIOProxy() {
			@Override public boolean isSendAndReceive(TileEntity tile) {return false;}
			@Override public boolean isTransceiver(TileEntity tile) {return false;}
			@Override public List<TileEntity> getConnectedTransceivers(TileEntity tile) {return null;}
			@Override public boolean isEnderIO() {return false;}
			@Override public boolean isItemConduit(TileEntity tile, EnumFacing dir) {return false;}
			@Override public boolean isFluidConduit(TileEntity tile, EnumFacing dir) {return false;}
			@Override public boolean isBundledPipe(TileEntity tile) {return false;}
		}));

		SimpleServiceLocator.setIronChestProxy(ProxyManager.getWrappedProxy(LPConstants.ironChestModID, IIronChestProxy.class, IronChestProxy.class, new IIronChestProxy() {
			@Override public boolean isIronChest(TileEntity tile) {return false;}
			@Override public @SideOnly(Side.CLIENT) boolean isChestGui(GuiScreen gui) {return false;}
		}));

		SimpleServiceLocator.setEnderStorageProxy(ProxyManager.getWrappedProxy("enderstorage", IEnderStorageProxy.class, EnderStorageProxy.class, new IEnderStorageProxy() {
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

		SimpleServiceLocator.setThermalDynamicsProxy(ProxyManager.getWrappedProxy(LPConstants.thermalDynamicsModID, ITDProxy.class, ThermalDynamicsProxy.class, new ITDProxy() {
			@Override public ITDPart getTDPart(final LogisticsTileGenericPipe pipe) {
				return new ITDPart() {
					@Override public TileEntity getInternalDuct() {return pipe;}
					@Override public void setWorld_LP(World world) {}
					@Override public void invalidate() {}
					@Override public void onChunkUnload() {}
					@Override public void scheduleNeighborChange() {}
					@Override public void connectionsChanged() {}
					@Override public boolean isLPSideBlocked(int i) {return false;}
					@Override public void setPos(BlockPos pos) {}
				};
			}
			@Override public boolean isActive() {return false;}
			@Override public void registerPipeInformationProvider() {}
			@Override public boolean isItemDuct(TileEntity tile) {return false;}
			@Override @SideOnly(Side.CLIENT) public void renderPipeConnections(LogisticsTileGenericPipe pipeTile, List<RenderEntry> list) {}
			@Override public void registerTextures(TextureMap iconRegister) {}
			@Override public boolean isBlockedSide(TileEntity with, EnumFacing opposite) {return false;}
		}, ITDPart.class));

		SimpleServiceLocator.setMCMPProxy(ProxyManager.getWrappedProxy(LPConstants.mcmpModID, IMCMPProxy.class, MCMPProxy.class, new IMCMPProxy() {
			@Override public IMCMPLTGPCompanion createMCMPCompanionFor(LogisticsTileGenericPipe pipe) {
				return new IMCMPLTGPCompanion() {
					@Override public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {return false;}
					@Nullable @Override public<T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {return null;}
					@Override public NBTTagCompound getUpdateTag() {return new NBTTagCompound();}
					@Override public void handleUpdateTag(NBTTagCompound tag) {}
					@Override public TileEntity getMCMPTileEntity() {return null;}
					@Override public void update() {}
				};
			}
			@Override public IMCMPBlockAccess createMCMPBlockAccess() {return new IMCMPBlockAccess() {
					@Override public void addBlockState(BlockStateContainer.Builder builder) {}
					@Override public IBlockState getExtendedState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {return state;}
					@Override public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity, boolean isActualState) {}
					@Override public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {return null;}
					@Override public Block getBlock() {return null;}
					@Override public void addDrops(NonNullList<ItemStack> list, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {}
				};
			}
		    @Override public void addQuads(@Nonnull List<BakedQuad> list, IBlockState state, EnumFacing side, long rand) {}
		    @Override public void registerTileEntities() {}
		    @Override public boolean checkIntersectionWith(LogisticsTileGenericPipe logisticsTileGenericPipe, AxisAlignedBB aabb) {return false;}
		    @Override public boolean hasParts(LogisticsTileGenericPipe pipeTile) {return false;}
		    @Override @SideOnly(Side.CLIENT) public void renderTileEntitySpecialRenderer(LogisticsTileGenericPipe tileentity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {}
		}, IMCMPLTGPCompanion.class));

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
			@Override public IBounds getBoundsInside(AxisAlignedBB boundingBox) {return dummyBounds;}
		};
		ICCLProxy dummyCCLProxy = new ICCLProxy() {
			@SideOnly(Side.CLIENT) @Override public TextureTransformation createIconTransformer(TextureAtlasSprite registerIcon) {
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
			@Override public IModelState getDefaultBlockState() {return null;}
		};

		//@formatter:on
		//CHECKSTYLE:ON

		Class<?>[] cclSubWrapper = new Class<?>[] { TextureTransformation.class, IRenderState.class, IModel3D.class, ITranslation.class, IVec3.class, IBounds.class };
		SimpleServiceLocator.setCCLProxy(ProxyManager.getWrappedProxy("!" + LPConstants.cclrenderModID, ICCLProxy.class, CCLProxy.class, dummyCCLProxy, cclSubWrapper));

		SimpleServiceLocator.setConfigToolHandler(new ConfigToolHandler());
		SimpleServiceLocator.configToolHandler.registerWrapper();

		SimpleServiceLocator.setPowerProxy(new PowerProxy());

	}
}
