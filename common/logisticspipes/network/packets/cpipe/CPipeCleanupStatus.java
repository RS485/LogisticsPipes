package logisticspipes.network.packets.cpipe;

import java.io.IOException;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.gui.GuiCraftingPipe;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(mode);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		mode = data.readBoolean();
	}
}
