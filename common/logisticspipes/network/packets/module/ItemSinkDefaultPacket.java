package logisticspipes.network.packets.module;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.gui.DummyModuleContainer;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class ItemSinkDefaultPacket extends IntegerCoordinatesPacket {

	public ItemSinkDefaultPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ItemSinkDefaultPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final int value = ((getInteger() % 10) + 10) % 10;
		final int slot = getInteger() / 10;
		if(slot < 0) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleItemSink) {
					((ModuleItemSink) dummy.getModule()).setDefaultRoute(value == 1);
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
			if(piperouted.getLogisticsModule() instanceof ModuleItemSink) {
				final ModuleItemSink module = (ModuleItemSink) piperouted.getLogisticsModule();
				module.setDefaultRoute(value == 1);
				return;
			}
		} else {
			if(piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleItemSink) {
				final ModuleItemSink module = (ModuleItemSink) piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.setDefaultRoute(value == 1);
				return;
			}
		}
	}
}

