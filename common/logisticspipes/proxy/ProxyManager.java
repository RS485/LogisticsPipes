package logisticspipes.proxy;

import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.cc.CCProxy;
import logisticspipes.proxy.cc.CCTurtleProxy;
import logisticspipes.proxy.forestry.ForestryProxy;
import logisticspipes.proxy.ic2.IC2Proxy;
import logisticspipes.proxy.interfaces.ICCProxy;
import logisticspipes.proxy.interfaces.IForestryProxy;
import logisticspipes.proxy.interfaces.IIC2Proxy;
import logisticspipes.proxy.interfaces.IThaumCraftProxy;
import logisticspipes.proxy.thaumcraft.ThaumCraftProxy;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import thaumcraft.api.EnumTag;
import thaumcraft.api.ObjectTags;
import cpw.mods.fml.common.Loader;

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
				@Override public int getIconIndexForAlleleId(String id, int phase) {return 0;}
				@Override public int getColorForAlleleId(String id, int phase) {return 0;}
				@Override public int getRenderPassesForAlleleId(String id) {return 0;}
				@Override public void addCraftingRecipes() {}
				@Override public String getNextAlleleId(String uid, World world) {return "";}
				@Override public String getPrevAlleleId(String uid, World world) {return "";}
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
				@Override public int getCharge(ItemStack stack) {return 0;}
				@Override public int getMaxCharge(ItemStack stack) {return 0;}
				@Override public boolean isFullyCharged(ItemStack stack) {return false;}
				@Override public boolean isFullyDischarged(ItemStack stack) {return false;}
				@Override public boolean isPartiallyCharged(ItemStack stack) {return false;}
				@Override public void addCraftingRecipes() {}
				@Override public boolean hasIC2() {return false;}
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
				@Override public ForgeDirection getOrientation(Object computer, int side, TileEntity tile) {return ForgeDirection.UNKNOWN;}
				@Override public boolean isLuaThread(Thread thread) {return false;}
			});
			LogisticsPipes.log.info("Loaded CC DummyProxy");
		}
		
		if(Loader.isModLoaded("Thaumcraft")) {
			SimpleServiceLocator.setThaumCraftProxy(new ThaumCraftProxy());
			LogisticsPipes.log.info("Loaded Thaumcraft Proxy");
		} else {
			SimpleServiceLocator.setThaumCraftProxy(new IThaumCraftProxy() {
				@Override public void renderAspectsDown(ItemStack item, int x, int y, GuiScreen gui) {}
				@Override public void renderAspectAt(EnumTag etag, int x, int y, GuiScreen gui) {}
				@Override public ObjectTags getTagsForStack(ItemStack stack) {return null;}
				@Override public void renderAspectsInGrid(List<EnumTag> etag, int x, int y, int legnth, int width, GuiScreen gui) {}
			});
			LogisticsPipes.log.info("Loaded Thaumcraft DummyProxy");
		}
	}
}
