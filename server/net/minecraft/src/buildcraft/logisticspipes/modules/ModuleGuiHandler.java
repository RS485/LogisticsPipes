package net.minecraft.src.buildcraft.logisticspipes.modules;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetHandler;
import net.minecraft.src.Packet;
import net.minecraft.src.forge.ForgeHooks;
public enum ModuleGuiHandler implements IModuleGuiHandler {
	GuiExtractor,
	GuiItemSink,
	GuiLiquidSupplier,
	GuiPassiveSupplier,
	GuiProvider,
	GuiTerminus;

	@Override
	public boolean displayGui(EntityPlayer entityplayer, ILogisticsModule logisticsModule, Object object) {
		//((EntityPlayerMP)entityplayer).playerNetServerHandler.sendPacket(null);
		//Send Packet
		return true;
	}
}
