package logisticspipes.network.packets.module;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.client.FMLClientHandler;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.gui.GuiSupplierPipe;
import logisticspipes.modules.ModuleActiveSupplier;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class SupplierPipeLimitedPacket extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private boolean isLimited;

	public SupplierPipeLimitedPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleActiveSupplier module = this.getLogisticsModule(player, ModuleActiveSupplier.class);
		if (module == null) {
			return;
		}
		module.setLimited(isLimited());
		if (MainProxy.isClient(player.world)) {
			refresh();
		} else {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SupplierPipeLimitedPacket.class).setLimited(isLimited()).setPacketPos(this), player);
		}
	}

	@ClientSideOnlyMethodContent
	private void refresh() {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiSupplierPipe) {
			((GuiSupplierPipe) FMLClientHandler.instance().getClient().currentScreen).refreshMode();
		}
	}

	@Override
	public ModernPacket template() {
		return new SupplierPipeLimitedPacket(getId());
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeBoolean(isLimited);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		isLimited = input.readBoolean();
	}
}
