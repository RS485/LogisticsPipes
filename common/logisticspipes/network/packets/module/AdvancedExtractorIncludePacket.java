package logisticspipes.network.packets.module;

import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.modules.AdvancedExtractorInclude;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyModuleContainer;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

public class AdvancedExtractorIncludePacket extends IntegerCoordinatesPacket {

	public AdvancedExtractorIncludePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new AdvancedExtractorIncludePacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final int slot = getInteger() / 10;
		if(slot < 0) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleAdvancedExtractor) {
					((ModuleAdvancedExtractor) dummy.getModule()).setItemsIncluded( !((ModuleAdvancedExtractor) dummy.getModule()).areItemsIncluded());
//TODO				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, getPosX(), getPosY(), getPosZ(), 20, ((ModuleAdvancedExtractor) dummy.getModule()).areItemsIncluded() ? 1 : 0).getPacket(), (Player) player);
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(AdvancedExtractorInclude.class).setInteger2(20).setInteger(((ModuleAdvancedExtractor) dummy.getModule()).areItemsIncluded() ? 1 : 0).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
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
				module.setItemsIncluded( !module.areItemsIncluded());
//TODO			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, getPosX(), getPosY(), getPosZ(), -1, module.areItemsIncluded() ? 1 : 0).getPacket(), (Player) player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(AdvancedExtractorInclude.class).setInteger2(-1).setInteger(module.areItemsIncluded() ? 1 : 0).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
				return;
			}
		} else {
			if(piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor) piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.setItemsIncluded( !module.areItemsIncluded());
//TODO			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, getPosX(), getPosY(), getPosZ(), slot - 1, (module.areItemsIncluded() ? 1 : 0)).getPacket(), (Player) player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(AdvancedExtractorInclude.class).setInteger2(slot - 1).setInteger((module.areItemsIncluded() ? 1 : 0)).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
				return;
			}
		}
	}
}

