package logisticspipes.network.packets.module;

import logisticspipes.interfaces.ISneakyDirectionReceiver;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.modules.ExtractorModuleMode;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyModuleContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

public class ExtractorModuleDirectionPacket extends IntegerCoordinatesPacket {

	public ExtractorModuleDirectionPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ExtractorModuleDirectionPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final int value = ((getInteger() % 10) + 10) % 10;
		final int slot = getInteger() / 10;
		if(slot < 0) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ISneakyDirectionReceiver) {
					final ISneakyDirectionReceiver module = (ISneakyDirectionReceiver) dummy.getModule();
					module.setSneakyDirection(ForgeDirection.getOrientation(value));
//TODO 				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, getPosX(), getPosY(), getPosZ(), -1, module.getSneakyDirection().ordinal()).getPacket(), (Player) player);
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ExtractorModuleMode.class).setInteger2(-1).setInteger(module.getSneakyDirection().ordinal()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
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
			if(piperouted.getLogisticsModule() instanceof ISneakyDirectionReceiver) {
				final ISneakyDirectionReceiver module = (ISneakyDirectionReceiver) piperouted.getLogisticsModule();
				module.setSneakyDirection(ForgeDirection.getOrientation(value));
//TODO 			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, getPosX(), getPosY(), getPosZ(), -1, module.getSneakyDirection().ordinal()).getPacket(), (Player) player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ExtractorModuleMode.class).setInteger2(-1).setInteger(module.getSneakyDirection().ordinal()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
				return;
			}
		} else {
			if(piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ISneakyDirectionReceiver) {
				final ISneakyDirectionReceiver module = (ISneakyDirectionReceiver) piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.setSneakyDirection(ForgeDirection.getOrientation(value));
//TODO 			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, getPosX(), getPosY(), getPosZ(), slot - 1, module.getSneakyDirection().ordinal()).getPacket(), (Player) player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ExtractorModuleMode.class).setInteger2(slot - 1).setInteger(module.getSneakyDirection().ordinal()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
				return;
			}
		}
	}
}

