package logisticspipes.proxy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.asm.wrapper.LogisticsWrapperHandler;
import logisticspipes.interfaces.IHUDConfig;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.bettersign.BetterSignProxy;
import logisticspipes.proxy.bs.BetterStorageProxy;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.proxy.buildcraft.pipeparts.IBCPipePart;
import logisticspipes.proxy.buildcraft.pipeparts.IBCTilePart;
import logisticspipes.proxy.cc.CCProxy;
import logisticspipes.proxy.enderchest.EnderStorageProxy;
import logisticspipes.proxy.enderio.EnderIOProxy;
import logisticspipes.proxy.factorization.FactorizationProxy;
import logisticspipes.proxy.forestry.ForestryProxy;
import logisticspipes.proxy.ic.IronChestProxy;
import logisticspipes.proxy.ic2.IC2Proxy;
import logisticspipes.proxy.interfaces.IBCProxy;
import logisticspipes.proxy.interfaces.IBetterSignProxy;
import logisticspipes.proxy.interfaces.IBetterStorageProxy;
import logisticspipes.proxy.interfaces.ICCProxy;
import logisticspipes.proxy.interfaces.IEnderIOProxy;
import logisticspipes.proxy.interfaces.IEnderStorageProxy;
import logisticspipes.proxy.interfaces.IFactorizationProxy;
import logisticspipes.proxy.interfaces.IForestryProxy;
import logisticspipes.proxy.interfaces.IIC2Proxy;
import logisticspipes.proxy.interfaces.IIronChestProxy;
import logisticspipes.proxy.interfaces.IModularPowersuitsProxy;
import logisticspipes.proxy.interfaces.INEIProxy;
import logisticspipes.proxy.interfaces.IOpenComputersProxy;
import logisticspipes.proxy.interfaces.IThaumCraftProxy;
import logisticspipes.proxy.interfaces.IThermalExpansionProxy;
import logisticspipes.proxy.interfaces.IToolWrenchProxy;
import logisticspipes.proxy.mps.ModularPowersuitsProxy;
import logisticspipes.proxy.nei.NEIProxy;
import logisticspipes.proxy.opencomputers.OpenComputersProxy;
import logisticspipes.proxy.te.ThermalExpansionProxy;
import logisticspipes.proxy.thaumcraft.ThaumCraftProxy;
import logisticspipes.proxy.toolWrench.ToolWrenchProxy;
import logisticspipes.renderer.state.PipeRenderState;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
		SimpleServiceLocator.setBuildCraftProxy(getWrappedProxy("BuildCraft|Transport", IBCProxy.class, BuildCraftProxy.class, new IBCProxy() {
			@Override public void resetItemRotation() {}
			@Override public boolean insertIntoBuildcraftPipe(TileEntity tile, LPTravelingItem item) {return false;}
			@Override public boolean isIPipeTile(TileEntity tile) {return false;}
			@Override public void registerPipeInformationProvider() {}
			@Override public void initProxy() {}
			@Override public boolean checkForPipeConnection(TileEntity with, ForgeDirection side, LogisticsTileGenericPipe pipe) {return true;}
			@Override public boolean checkConnectionOverride(TileEntity with, ForgeDirection side, LogisticsTileGenericPipe pipe) {return true;}
			@Override public boolean isMachineManagingSolids(TileEntity tile) {return false;}
			@Override public boolean isMachineManagingFluids(TileEntity tile) {return false;}
			@Override public IBCPipePart getBCPipePart(LogisticsTileGenericPipe tile) {
				return new IBCPipePart() {
					@Override public void updateGate() {}
					@Override public void writeToNBT(NBTTagCompound data) {}
					@Override public void readFromNBT(NBTTagCompound data) {}
					@Override public boolean hasGate() {return false;}
					@Override public void addItemDrops(List<ItemStack> result) {}
					@Override public void resetGate() {}
					@Override public boolean isWireConnectedTo(TileEntity tile, Object color) {return false;}
					@Override public boolean isWired() {return false;}
					@Override public int isPoweringTo(int side) {return 0;}
					@Override public void updateSignalState() {}
					@Override public boolean[] getWireSet() {return null;}
					@Override public ItemStack getGateItem() {return null;}
					@Override public int[] getSignalStrength() {return null;}
					@Override public void openGateGui(EntityPlayer player) {}
					@Override public boolean isGateActive() {return false;}
					@Override public boolean receiveSignal(int i, Object wire) {return false;}
					@Override public Object getGate() {return null;}
					@Override public void makeGate(CoreUnroutedPipe pipe, ItemStack currentEquippedItem) {}
					@Override public void updateCoreStateGateData() {}
					@Override public void updateGateFromCoreStateData() {}
					@Override public void checkResyncGate() {}
					@Override public void actionsActivated(Object actions) {}
					@Override public void updateEntity() {}
					@Override public Container getGateContainer(InventoryPlayer inventory) {return null;}
					@Override public Object getClientGui(InventoryPlayer inventory) {return null;}
					@Override public LinkedList<?> getActions() {return null;}
				};
			}
			@Override public boolean handleBCClickOnPipe(ItemStack currentItem, CoreUnroutedPipe pipe, World world, int x, int y, int z, EntityPlayer player, int side, LogisticsBlockGenericPipe logisticsBlockGenericPipe) {return false;}
			@Override public ItemStack getPipePlugItemStack() {return null;}
			@Override public ItemStack getRobotStationItemStack() {return null;}
			@Override public boolean stripEquipment(World world, int x, int y, int z, EntityPlayer player, CoreUnroutedPipe pipe, LogisticsBlockGenericPipe block) {return false;}
			@Override public IBCTilePart getBCTilePart(LogisticsTileGenericPipe tile) {
				return new IBCTilePart() {
					@Override public void refreshRenderState() {}
					@Override public boolean addFacade(ForgeDirection direction, int type, int wire, Block[] blocks, int[] metaValues) {return false;}
					@Override public boolean hasFacade(ForgeDirection direction) {return false;}
					@Override public void dropFacadeItem(ForgeDirection direction) {}
					@Override public ItemStack getFacade(ForgeDirection direction) {return null;}
					@Override public boolean dropFacade(ForgeDirection direction) {return false;}
					@Override public boolean hasPlug(ForgeDirection side) {return false;}
					@Override public boolean hasRobotStation(ForgeDirection side) {return false;}
					@Override public boolean removeAndDropPlug(ForgeDirection side) {return false;}
					@Override public boolean removeAndDropRobotStation(ForgeDirection side) {return false;}
					@Override public boolean addPlug(ForgeDirection forgeDirection) {return false;}
					@Override public boolean addRobotStation(ForgeDirection forgeDirection) {return false;}
					@Override public void writeToNBT(NBTTagCompound nbt) {}
					@Override public void readFromNBT(NBTTagCompound nbt) {}
				};
			}
			@Override public void notifyOfChange(LogisticsTileGenericPipe pipe, TileEntity tile, ForgeDirection o) {}
			@Override public void renderGatesWires(LogisticsTileGenericPipe pipe, double x, double y, double z) {}
			@Override public void pipeFacadeRenderer(RenderBlocks renderblocks, LogisticsBlockGenericPipe block, PipeRenderState state, int x, int y, int z) {}
			@Override public void pipePlugRenderer(RenderBlocks renderblocks, Block block, PipeRenderState state, int x, int y, int z) {}
			@Override public ItemStack getDropFacade(CoreUnroutedPipe pipe, ForgeDirection dir) {return null;}
			@Override public boolean canPipeConnect(TileEntity pipe, TileEntity tile, ForgeDirection direction) {return false;}
			@Override public void pipeRobotStationRenderer(RenderBlocks renderblocks, LogisticsBlockGenericPipe block, PipeRenderState state, int x, int y, int z) {}
			@Override public boolean isActive() {return false;}
			@Override public Object getLPPipeType() {return null;}
			@Override public boolean isInstalled() {return false;}
		}, IBCPipePart.class, IBCTilePart.class));
		
		SimpleServiceLocator.setForestryProxy(getWrappedProxy("Forestry", IForestryProxy.class, ForestryProxy.class, new IForestryProxy() {
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
			@Override public String getForestryTranslation(String input) {return input.substring(input.lastIndexOf(".") + 1).toLowerCase().replace("_", " ");}
			@Override @SideOnly(Side.CLIENT) public IIcon getIconIndexForAlleleId(String id, int phase) {return null;}
			@Override @SideOnly(Side.CLIENT) public int getColorForAlleleId(String id, int phase) {return 16777215;}
			@Override @SideOnly(Side.CLIENT) public int getRenderPassesForAlleleId(String id) {return 0;}
			@Override public void addCraftingRecipes() {}
			@Override public String getNextAlleleId(String uid, World world) {return "";}
			@Override public String getPrevAlleleId(String uid, World world) {return "";}
			@Override @SideOnly(Side.CLIENT) public IIcon getIconFromTextureManager(String name) {return null;}
		}));
		
		SimpleServiceLocator.setElectricItemProxy(getWrappedProxy("IC2", IIC2Proxy.class, IC2Proxy.class, new IIC2Proxy() {
			@Override public boolean isElectricItem(ItemStack stack) {return false;}
			@Override public boolean isSimilarElectricItem(ItemStack stack, ItemStack template) {return false;}
			@Override public boolean isFullyCharged(ItemStack stack) {return false;}
			@Override public boolean isFullyDischarged(ItemStack stack) {return false;}
			@Override public boolean isPartiallyCharged(ItemStack stack) {return false;}
			@Override public void addCraftingRecipes() {}
			@Override public boolean hasIC2() {return false;}
			@Override public void registerToEneryNet(TileEntity tile) {}
			@Override public void unregisterToEneryNet(TileEntity tile) {}
			@Override public boolean acceptsEnergyFrom(TileEntity tile1, TileEntity tile2, ForgeDirection opposite) {return false;}
			@Override public boolean isEnergySink(TileEntity tile) {return false;}
			@Override public double demandedEnergyUnits(TileEntity tile) {return 0;}
			@Override public double injectEnergyUnits(TileEntity tile, ForgeDirection opposite, double d) {return d;}
		}));
		
		SimpleServiceLocator.setCCProxy(getWrappedProxy("ComputerCraft@1.6", ICCProxy.class, CCProxy.class, new ICCProxy() {
			@Override public boolean isTurtle(TileEntity tile) {return false;}
			@Override public boolean isComputer(TileEntity tile) {return false;}
			@Override public boolean isCC() {return false;}
			@Override public boolean isLuaThread(Thread thread) {return false;}
			@Override public void queueEvent(String event, Object[] arguments, LogisticsTileGenericPipe logisticsTileGenericPipe) {}
			@Override public void setTurtleConnect(boolean flag, LogisticsTileGenericPipe logisticsTileGenericPipe) {}
			@Override public boolean getTurtleConnect(LogisticsTileGenericPipe logisticsTileGenericPipe) {return false;}
			@Override public int getLastCCID(LogisticsTileGenericPipe logisticsTileGenericPipe) {return 0;}
			@Override public void handleMesssage(int computerId, Object message, LogisticsTileGenericPipe tile, int sourceId) {}
			@Override public void addCraftingRecipes() {}
			@Override public Object getAnswer(Object object) {return object;}
		}));
		
		SimpleServiceLocator.setThaumCraftProxy(getWrappedProxy("Thaumcraft", IThaumCraftProxy.class, ThaumCraftProxy.class, new IThaumCraftProxy() {
			@Override public boolean isScannedObject(ItemStack stack, String playerName) {return false;}
			@Override public List<String> getListOfTagsForStack(ItemStack stack) {return null;}
			@Override @SideOnly(Side.CLIENT) public void renderAspectsDown(ItemStack item, int x, int y, GuiScreen gui) {}
			@Override @SideOnly(Side.CLIENT) public void renderAspectsInGrid(List<String> eTags, int x, int y, int legnth, int width, GuiScreen gui) {}
			@Override public void addCraftingRecipes() {}
		}));
		
		SimpleServiceLocator.setThermalExpansionProxy(getWrappedProxy("ThermalExpansion", IThermalExpansionProxy.class, ThermalExpansionProxy.class, new IThermalExpansionProxy() {
			@Override public boolean isTesseract(TileEntity tile) {return false;}
			@Override public boolean isTE() {return false;}
			@Override public List<TileEntity> getConnectedTesseracts(TileEntity tile) {return new ArrayList<TileEntity>(0);}
			@Override public boolean isItemConduit(TileEntity tile) {return false;}
			@Override public void handleLPInternalConduitChunkUnload(LogisticsTileGenericPipe pipe) {}
			@Override public void handleLPInternalConduitRemove(LogisticsTileGenericPipe pipe) {}
			@Override public void handleLPInternalConduitNeighborChange(LogisticsTileGenericPipe logisticsTileGenericPipe) {}
			@Override public void handleLPInternalConduitUpdate(LogisticsTileGenericPipe pipe) {}
			@Override public boolean insertIntoConduit(LPTravelingItemServer arrivingItem, TileEntity tile, CoreRoutedPipe pipe) {return false;}
			@Override public boolean isSideFree(TileEntity tile, int side) {return false;}
			@Override public boolean isEnergyHandler(TileEntity tile) {return false;}
			@Override public int getMaxEnergyStored(TileEntity tile, ForgeDirection opposite) {return 0;}
			@Override public int getEnergyStored(TileEntity tile, ForgeDirection opposite) {return 0;}
			@Override public boolean canInterface(TileEntity tile, ForgeDirection opposite) {return false;}
			@Override public int receiveEnergy(TileEntity tile, ForgeDirection opposite, int i, boolean b) {return 0;}
			@Override public void addCraftingRecipes() {}
		}));
		
		SimpleServiceLocator.setBetterStorageProxy(getWrappedProxy("betterstorage", IBetterStorageProxy.class, BetterStorageProxy.class, new IBetterStorageProxy() {
			@Override public boolean isBetterStorageCrate(TileEntity tile) {return false;}
		}));
		
		SimpleServiceLocator.setNEIProxy(getWrappedProxy("NotEnoughItems", INEIProxy.class, NEIProxy.class, new INEIProxy() {
			@Override @SideOnly(Side.CLIENT) public int getWidthForList(List<String> data, FontRenderer fontRenderer) {
				int width = 0;
				for(String s:data) {
					width = Math.max(width, fontRenderer.getStringWidth(s) + 22);
				}
				return width;
			}
			@Override public List<String> getInfoForPosition(World world, EntityPlayer player, MovingObjectPosition objectMouseOver) {return new ArrayList<String>(0);}
			@Override public ItemStack getItemForPosition(World world, EntityPlayer player, MovingObjectPosition objectMouseOver) {return null;}
		}));
		
		SimpleServiceLocator.setMPSProxy(getWrappedProxy("powersuits", IModularPowersuitsProxy.class, ModularPowersuitsProxy.class, new IModularPowersuitsProxy() {
			@Override public boolean isMPSHelm(ItemStack stack) {return false;}
			@Override public void initModules() {}
			@Override public boolean hasActiveHUDModule(ItemStack stack) {return false;}
			@Override public IHUDConfig getConfigFor(ItemStack itemStack) {
				return new IHUDConfig() {
					@Override public boolean isHUDSatellite() {return false;}
					@Override public boolean isHUDProvider() {return false;}
					@Override public boolean isHUDPowerLevel() {return false;}
					@Override public boolean isHUDInvSysCon() {return false;}
					@Override public boolean isHUDCrafting() {return false;}
					@Override public boolean isHUDChassie() {return false;}
					@Override public void setHUDChassie(boolean state) {}
					@Override public void setHUDCrafting(boolean state) {}
					@Override public void setHUDInvSysCon(boolean state) {}
					@Override public void setHUDPowerJunction(boolean state) {}
					@Override public void setHUDProvider(boolean state) {}
					@Override public void setHUDSatellite(boolean state) {}
				};
			}
			@Override public boolean isMPSHand(ItemStack stack) {return false;}
			@Override public boolean hasHelmHUDInstalled(ItemStack stack) {return false;}
		}));
		
		SimpleServiceLocator.setFactorizationProxy(getWrappedProxy("factorization", IFactorizationProxy.class, FactorizationProxy.class, new IFactorizationProxy() {
			@Override public boolean isBarral(TileEntity tile) {return false;}
		}));
		
		SimpleServiceLocator.setBetterSignProxy(getWrappedProxy("BetterSignsMod", IBetterSignProxy.class, BetterSignProxy.class, new IBetterSignProxy() {
			@Override @SideOnly(Side.CLIENT) public void hideSignSticks(ModelSign model) {
				model.signStick.showModel = false;
			}
		}));
		
		SimpleServiceLocator.setEnderIOProxy(getWrappedProxy("EnderIO", IEnderIOProxy.class, EnderIOProxy.class, new IEnderIOProxy() {
			@Override public boolean isSendAndReceive(TileEntity tile) {return false;}
			@Override public boolean isHyperCube(TileEntity tile) {return false;}
			@Override public List<TileEntity> getConnectedHyperCubes(TileEntity tile) {return new ArrayList<TileEntity>(0);}
			@Override public boolean isEnderIO() {return false;}
		}));

		SimpleServiceLocator.setIronChestProxy(getWrappedProxy("IronChest", IIronChestProxy.class, IronChestProxy.class, new IIronChestProxy() {
			@Override public boolean isIronChest(TileEntity tile) {return false;}
			@Override public @SideOnly(Side.CLIENT) boolean isChestGui(GuiScreen gui) {return false;}
		}));
		
		SimpleServiceLocator.setEnderStorageProxy(getWrappedProxy("EnderStorage", IEnderStorageProxy.class, EnderStorageProxy.class, new IEnderStorageProxy() {
			@Override public boolean isEnderChestBlock(Block block) {return false;}
			@Override public void openEnderChest(World world, int x, int y, int z, EntityPlayer player) {}
		}));
		
		SimpleServiceLocator.setOpenComputersProxy(getWrappedProxy("OpenComputers@1.3", IOpenComputersProxy.class, OpenComputersProxy.class, new IOpenComputersProxy() {
			@Override public void initLogisticsTileGenericPipe(LogisticsTileGenericPipe tile) {}
			@Override public void handleLPWriteToNBT(LogisticsTileGenericPipe tile, NBTTagCompound nbt) {}
			@Override public void handleLPReadFromNBT(LogisticsTileGenericPipe tile, NBTTagCompound nbt) {}
			@Override public void handleLPInvalidate(LogisticsTileGenericPipe tile) {}
			@Override public void handleLPChunkUnload(LogisticsTileGenericPipe tile) {}
			@Override public void addToNetwork(LogisticsTileGenericPipe tile) {}
		}));
		
		SimpleServiceLocator.setToolWrenchProxy(getWrappedProxy("!IToolWrench", IToolWrenchProxy.class, ToolWrenchProxy.class, new IToolWrenchProxy() {
			@Override public void wrenchUsed(EntityPlayer entityplayer, int x, int y, int z) {}
			@Override public boolean isWrenchEquipped(EntityPlayer entityplayer) {return false;}
			@Override public boolean canWrench(EntityPlayer entityplayer, int x, int y, int z) {return false;}
			@Override public boolean isWrench(Item item) {return false;}
		}));
	}
}
