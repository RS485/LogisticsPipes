package logisticspipes.network.packets.module;

import logisticspipes.LogisticsPipes;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.modules.ExtractorModuleMode;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyModuleContainer;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

public class AdvancedExtractorSneakyGuiPacket extends IntegerCoordinatesPacket {

	public AdvancedExtractorSneakyGuiPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new AdvancedExtractorSneakyGuiPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final int slot = getInteger();
		if(slot < 0) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleAdvancedExtractor) {
					player.closeScreen();
					player.openGui(LogisticsPipes.instance, GuiIDs.GUI_Module_Extractor_ID + (100 * getInteger()), player.worldObj, getPosX(), getPosY(), getPosZ());
//TODO				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, getPosX(), getPosY(), getPosZ(), -1, ((ModuleAdvancedExtractor) dummy.getModule()).getSneakyDirection().ordinal()).getPacket(), (Player) player);
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ExtractorModuleMode.class).setInteger2(-1).setInteger(((ModuleAdvancedExtractor) dummy.getModule()).getSneakyDirection().ordinal()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
				}
			}
			return;
		}
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if( !(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;
		if(piperouted.getLogisticsModule() == null) {
			return;
		}
		if(slot <= 0) {
			if(piperouted.getLogisticsModule() instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor) piperouted.getLogisticsModule();
				player.openGui(LogisticsPipes.instance, GuiIDs.GUI_Module_Extractor_ID + (100 * getInteger()), player.worldObj, getPosX(), getPosY(), getPosZ());
//TODO			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, getPosX(), getPosY(), getPosZ(), -1, module.getSneakyDirection().ordinal()).getPacket(), (Player) player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ExtractorModuleMode.class).setInteger2(-1).setInteger(module.getSneakyDirection().ordinal()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
				return;
			}
		} else {
			if(piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor) piperouted.getLogisticsModule().getSubModule(slot - 1);
				player.openGui(LogisticsPipes.instance, GuiIDs.GUI_Module_Extractor_ID + (100 * getInteger()), player.worldObj, getPosX(), getPosY(), getPosZ());
//TODO			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, getPosX(), getPosY(), getPosZ(), slot - 1, module.getSneakyDirection().ordinal()).getPacket(), (Player) player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ExtractorModuleMode.class).setInteger2(slot - 1).setInteger(module.getSneakyDirection().ordinal()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
				return;
			}
		}
	}
}

