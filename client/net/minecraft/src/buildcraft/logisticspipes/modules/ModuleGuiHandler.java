package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ModLoader;
import net.minecraft.src.buildcraft.krapht.gui.GuiChassiPipe;

public enum ModuleGuiHandler implements IModuleGuiHandler{
	GuiExtractor(new IGuiHandlerCall() {

		@Override
		public boolean displayGui(EntityPlayer entityplayer, ILogisticsModule module, GuiScreen previousGui) {
			if(module instanceof ModuleExtractor) {
				ModLoader.getMinecraftInstance().displayGuiScreen(new GuiExtractor(entityplayer.inventory, (ModuleExtractor) module, previousGui));
				return true;
			}
			return false;
		}

	}),
	GuiItemSink(new IGuiHandlerCall() {

		@Override
		public boolean displayGui(EntityPlayer entityplayer, ILogisticsModule module, GuiScreen previousGui) {
			if(module instanceof ModuleItemSink) {
				ModLoader.getMinecraftInstance().displayGuiScreen(new GuiItemSink(entityplayer.inventory, (ModuleItemSink) module, previousGui));
				return true;
			}
			return false;
		}

	}),
	GuiLiquidSupplier(new IGuiHandlerCall() {

		@Override
		public boolean displayGui(EntityPlayer entityplayer, ILogisticsModule module, GuiScreen previousGui) {
			if(module instanceof ModuleLiquidSupplier) {
				ModLoader.getMinecraftInstance().displayGuiScreen(new GuiLiquidSupplier(entityplayer.inventory, (ModuleLiquidSupplier) module, previousGui));
				return true;
			}
			return false;
		}

	}),
	GuiPassiveSupplier(new IGuiHandlerCall() {

		@Override
		public boolean displayGui(EntityPlayer entityplayer, ILogisticsModule module, GuiScreen previousGui) {
			if(module instanceof ModulePassiveSupplier) {
				ModLoader.getMinecraftInstance().displayGuiScreen(new GuiPassiveSupplier(entityplayer.inventory, (ModulePassiveSupplier) module, previousGui));
				return true;
			}
			return false;
		}

	}),
	GuiProvider(new IGuiHandlerCall() {

		@Override
		public boolean displayGui(EntityPlayer entityplayer, ILogisticsModule module, GuiScreen previousGui) {
			if(module instanceof ModuleProvider) {
				ModLoader.getMinecraftInstance().displayGuiScreen(new GuiProvider(entityplayer.inventory, (ModuleProvider) module, previousGui));
				return true;
			}
			return false;
		}

	}),
	GuiTerminus(new IGuiHandlerCall() {

		@Override
		public boolean displayGui(EntityPlayer entityplayer, ILogisticsModule module, GuiScreen previousGui) {
			if(module instanceof ModuleTerminus) {
				ModLoader.getMinecraftInstance().displayGuiScreen(new GuiTerminus(entityplayer.inventory, (ModuleTerminus) module, previousGui));
				return true;
			}
			return false;
		}

	});
	
	public IGuiHandlerCall callhandler;
	
	ModuleGuiHandler(IGuiHandlerCall call) {
		callhandler = call;
	}
	
	public boolean displayGui(EntityPlayer entityplayer,ILogisticsModule module, GuiScreen previousGui) {
		return callhandler.displayGui(entityplayer, module, previousGui);
	}
}
