package net.minecraft.src.buildcraft.krapht;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.krapht.gui.GuiCraftingPipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiLiquidSupplierPipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiOrderer;
import net.minecraft.src.buildcraft.krapht.gui.GuiProviderPipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiRoutingStats;
import net.minecraft.src.buildcraft.krapht.gui.GuiSatellitePipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiSupplierPipe;
import net.minecraft.src.buildcraft.krapht.logic.BaseRoutingLogic;
import net.minecraft.src.buildcraft.krapht.logic.LogicCrafting;
import net.minecraft.src.buildcraft.krapht.logic.LogicLiquidSupplier;
import net.minecraft.src.buildcraft.krapht.logic.LogicProvider;
import net.minecraft.src.buildcraft.krapht.logic.LogicSatellite;
import net.minecraft.src.buildcraft.krapht.logic.LogicSupplier;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.forge.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		if(!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(!(tile instanceof TileGenericPipe))
			return null;

		TileGenericPipe pipe = (TileGenericPipe)tile;
		
		switch(ID) {

		case GuiIDs.GUI_CRAFTINGPIPE_ID:
			if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicCrafting)) return null;
			return new GuiCraftingPipe(player, ((LogicCrafting)pipe.pipe.logic).get_dummyInventory(), (LogicCrafting)pipe.pipe.logic);
		
		case GuiIDs.GUI_LiquidSupplier_ID:
			if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicLiquidSupplier)) return null;
			return new GuiLiquidSupplierPipe(player.inventory, ((LogicLiquidSupplier)pipe.pipe.logic).getDummyInventory(), (LogicLiquidSupplier)pipe.pipe.logic);
			
		case GuiIDs.GUI_ProviderPipe_ID:
			if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicProvider)) return null;
			return new GuiProviderPipe(player.inventory, ((LogicProvider)pipe.pipe.logic).getDummyInventory(), (LogicProvider)pipe.pipe.logic);
		
		case GuiIDs.GUI_SatelitePipe_ID:
			if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicSatellite)) return null;
			return new GuiSatellitePipe((LogicSatellite)pipe.pipe.logic);
			
		case GuiIDs.GUI_SupplierPipe_ID:
			if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicSupplier)) return null;
			return new GuiSupplierPipe(player.inventory, ((LogicSupplier)pipe.pipe.logic).getDummyInventory(), (LogicSupplier)pipe.pipe.logic);
			
			
			/*** Modules ***/
		case GuiIDs.GUI_Module_Extractor_ID:
			return null;
			
			
		case GuiIDs.GUI_RoutingStats_ID:
			if(pipe.pipe == null || !(pipe.pipe.logic instanceof BaseRoutingLogic)) return null;
			return new GuiRoutingStats(((BaseRoutingLogic)pipe.pipe.logic).getRouter());
			
		case GuiIDs.GUI_Orderer_ID:
			if(pipe.pipe == null || !(pipe.pipe.logic instanceof BaseRoutingLogic)) return null;
			return new GuiOrderer(((BaseRoutingLogic)pipe.pipe.logic).getRoutedPipe(), player);
			
		default:
			return null;
		}
	}
}
