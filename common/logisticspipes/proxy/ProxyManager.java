package logisticspipes.proxy;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.bs.BetterStorageProxy;
import logisticspipes.proxy.cc.CCProxy;
import logisticspipes.proxy.cc.CCTurtleProxy;
import logisticspipes.proxy.forestry.ForestryProxy;
import logisticspipes.proxy.ic2.IC2Proxy;
import logisticspipes.proxy.interfaces.IBetterStorageProxy;
import logisticspipes.proxy.interfaces.ICCProxy;
import logisticspipes.proxy.interfaces.IForestryProxy;
import logisticspipes.proxy.interfaces.IIC2Proxy;
import logisticspipes.proxy.interfaces.IThaumCraftProxy;
import logisticspipes.proxy.interfaces.IThermalExpansionProxy;
import logisticspipes.proxy.te.ThermalExpansionProxy;
import logisticspipes.proxy.thaumcraft.ThaumCraftProxy;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProxyManager {

	public static void load() {
		if(Loader.isModLoaded("Forestry")) {
			SimpleServiceLocator.setForestryProxy(new ForestryProxy());
			LogisticsPipes.log.info("Loaded ForestryProxy");
		} else {
			//DummyProxy
			SimpleServiceLocator.setForestryProxy(new IForestryProxy() {
				@Override public boolean isBee(ItemStack item) {return false;}
				@Override public boolean isBee(ItemIdentifier item) {return false;}
				@Override public boolean isAnalysedBee(ItemStack item) {return false;}
				@Override public boolean isAnalysedBee(ItemIdentifier item) {return false;}
				@Override public boolean isTileAnalyser(TileEntity tile) {return false;}
				@Override public boolean forestryEnabled() {return false;}
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
				@Override @SideOnly(Side.CLIENT) public Icon getIconIndexForAlleleId(String id, int phase) {return null;}
				@Override @SideOnly(Side.CLIENT) public int getColorForAlleleId(String id, int phase) {return 0;}
				@Override @SideOnly(Side.CLIENT) public int getRenderPassesForAlleleId(String id) {return 0;}
				@Override public void addCraftingRecipes() {}
				@Override public String getNextAlleleId(String uid, World world) {return "";}
				@Override public String getPrevAlleleId(String uid, World world) {return "";}
				@Override @SideOnly(Side.CLIENT) public Icon getIconFromTextureManager(String name) {return null;}
			});
			LogisticsPipes.log.info("Loaded Forestry DummyProxy");
		}
		if(Loader.isModLoaded("IC2")) {
			SimpleServiceLocator.setElectricItemProxy(new IC2Proxy());
			LogisticsPipes.log.info("Loaded IC2Proxy");
		} else {
			//DummyProxy
			SimpleServiceLocator.setElectricItemProxy(new IIC2Proxy() {
				@Override public boolean isElectricItem(ItemStack stack) {return false;}
				@Override public boolean isSimilarElectricItem(ItemStack stack, ItemStack template) {return false;}
				@Override public boolean isFullyCharged(ItemStack stack) {return false;}
				@Override public boolean isFullyDischarged(ItemStack stack) {return false;}
				@Override public boolean isPartiallyCharged(ItemStack stack) {return false;}
				@Override public void addCraftingRecipes() {}
				@Override public boolean hasIC2() {return false;}
				@Override public void registerToEneryNet(TileEntity tile) {}
				@Override public void unregisterToEneryNet(TileEntity tile) {}
			});
			LogisticsPipes.log.info("Loaded IC2 DummyProxy");
		}
		if(Loader.isModLoaded("ComputerCraft")) {
			if(Loader.isModLoaded("CCTurtle")) {
				SimpleServiceLocator.setCCProxy(new CCTurtleProxy());
				LogisticsPipes.log.info("Loaded CCTurtleProxy");
			} else {
				SimpleServiceLocator.setCCProxy(new CCProxy());
				LogisticsPipes.log.info("Loaded CCProxy");
			}
		} else {
			//DummyProxy
			SimpleServiceLocator.setCCProxy(new ICCProxy() {
				@Override public boolean isTurtle(TileEntity tile) {return false;}
				@Override public boolean isComputer(TileEntity tile) {return false;}
				@Override public boolean isCC() {return false;}
				@Override public ForgeDirection getOrientation(Object computer, TileEntity tile) {return ForgeDirection.UNKNOWN;}
				@Override public boolean isLuaThread(Thread thread) {return false;}
				@Override public void queueEvent(String event, Object[] arguments, LogisticsTileGenericPipe logisticsTileGenericPipe) {}
				@Override public void setTurtleConnect(boolean flag, LogisticsTileGenericPipe logisticsTileGenericPipe) {}
				@Override public boolean getTurtleConnect(LogisticsTileGenericPipe logisticsTileGenericPipe) {return false;}
				@Override public int getLastCCID(LogisticsTileGenericPipe logisticsTileGenericPipe) {return 0;}
			});
			LogisticsPipes.log.info("Loaded CC DummyProxy");
		}
		
		if(Loader.isModLoaded("Thaumcraft")) {
			SimpleServiceLocator.setThaumCraftProxy(new ThaumCraftProxy());
			LogisticsPipes.log.info("Loaded Thaumcraft Proxy");
		} else {
			SimpleServiceLocator.setThaumCraftProxy(new IThaumCraftProxy() {
				@Override public void renderAspectsDown(ItemStack item, int x, int y, GuiScreen gui) {}
				@Override public void renderAspectAt(Object etag, int x, int y, GuiScreen gui) {}
				@Override public Object getTagsForStack(ItemStack stack) {return null;}
				@Override public void renderAspectsInGrid(List<Integer> etagIDs, int x, int y, int legnth, int width, GuiScreen gui) {}
				@Override public List<Integer> getListOfTagIDsForStack(ItemStack stack) {return null;}
				@Override public String getNameForTagID(int id) {return null;}
				@Override public void addCraftingRecipes() {return;}
			});
			LogisticsPipes.log.info("Loaded Thaumcraft DummyProxy");
		}
		
		if(Loader.isModLoaded("ThermalExpansion")) {
			SimpleServiceLocator.setThermalExpansionProxy(new ThermalExpansionProxy());
			LogisticsPipes.log.info("Loaded ThermalExpansion Proxy");
		} else {
			SimpleServiceLocator.setThermalExpansionProxy(new IThermalExpansionProxy() {
				@Override public boolean isTesseract(TileEntity tile) {return false;}
				@Override public boolean isTE() {return false;}
				@Override public List<TileEntity> getConnectedTesseracts(TileEntity tile) {return new ArrayList<TileEntity>(0);}
			});
			LogisticsPipes.log.info("Loaded ThermalExpansion DummyProxy");
		}
		
		if(Loader.isModLoaded("BetterStorage")) {
			SimpleServiceLocator.setBetterStorageProxy(new BetterStorageProxy());
			LogisticsPipes.log.info("Loaded BetterStorage Proxy");
		} else {
			SimpleServiceLocator.setBetterStorageProxy(new IBetterStorageProxy() {
				@Override public boolean isBetterStorageCrate(TileEntity tile) {return false;}
			});
			LogisticsPipes.log.info("Loaded BetterStorage DummyProxy");
		}
	}
}
