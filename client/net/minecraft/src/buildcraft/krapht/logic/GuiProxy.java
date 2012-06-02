package net.minecraft.src.buildcraft.krapht.logic;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiCraftingPipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiLiquidSupplierPipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiOrderer;
import net.minecraft.src.buildcraft.krapht.gui.GuiProviderPipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiRoutingStats;
import net.minecraft.src.buildcraft.krapht.gui.GuiSatellitePipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiSupplierPipe;
import net.minecraft.src.buildcraft.krapht.routing.IRouter;
import net.minecraft.src.krapht.SimpleInventory;

public class GuiProxy {
	
	public static void openGuiRoutingStats(IRouter router) {
		ModLoader.getMinecraftInstance().displayGuiScreen(new GuiRoutingStats(router));
	}

	public static void openGuiOrderer(RoutedPipe routedPipe, EntityPlayer entityplayer) {
		ModLoader.getMinecraftInstance().displayGuiScreen(new GuiOrderer(routedPipe, entityplayer));
	}

	public static void openGuiCraftingPipe(InventoryPlayer inventory, SimpleInventory _dummyInventory, LogicCrafting logicCrafting) {
		ModLoader.getMinecraftInstance().displayGuiScreen(new GuiCraftingPipe(inventory, _dummyInventory, logicCrafting));
	}

	public static void openGuiLiquidSupplierPipe(InventoryPlayer inventory, SimpleInventory dummyInventory, LogicLiquidSupplier logicLiquidSupplier) {
		ModLoader.getMinecraftInstance().displayGuiScreen(new GuiLiquidSupplierPipe(inventory, dummyInventory, logicLiquidSupplier));
	}

	public static void openGuiProviderPipe(InventoryPlayer inventory, SimpleInventory dummyInventory, LogicProvider logicProvider) {
		ModLoader.getMinecraftInstance().displayGuiScreen(new GuiProviderPipe(inventory, dummyInventory, logicProvider));
	}

	public static void GuiSatellitePipe(LogicSatellite logicSatellite) {
		ModLoader.getMinecraftInstance().displayGuiScreen(new GuiSatellitePipe(logicSatellite));
	}

	public static void openGuiSupplierPipe(InventoryPlayer inventory, SimpleInventory dummyInventory, LogicSupplier logicSupplier) {
		ModLoader.getMinecraftInstance().displayGuiScreen(new GuiSupplierPipe(inventory, dummyInventory, logicSupplier));
		
	}
	
	
}
