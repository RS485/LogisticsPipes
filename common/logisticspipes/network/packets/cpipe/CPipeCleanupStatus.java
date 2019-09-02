package logisticspipes.network.packets.cpipe;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.gui.GuiCraftingPipe;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class CPipeCleanupStatus extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private boolean mode;

	public CPipeCleanupStatus(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CPipeCleanupStatus(getId());
	}

	@Override
	@ClientSideOnlyMethodContent
	public void processPacket(EntityPlayer player) {
		final ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if (module == null) {
			return;
		}
		module.cleanupModeIsExclude = mode;
		if (Minecraft.getMinecraft().currentScreen instanceof GuiCraftingPipe) {
			((GuiCraftingPipe) Minecraft.getMinecraft().currentScreen).onCleanupModeChange();
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeBoolean(mode);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		mode = input.readBoolean();
	}
}
