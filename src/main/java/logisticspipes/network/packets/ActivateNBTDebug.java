package logisticspipes.network.packets;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.modplugins.nei.LoadingHelper;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class ActivateNBTDebug extends ModernPacket {

	public ActivateNBTDebug(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {}

	@Override
	public void processPacket(EntityPlayer player) {
		try {
			Class.forName("codechicken.nei.handler.NEIClientEventHandler");
			Configs.TOOLTIP_INFO = true;
			LoadingHelper.LoadNeiNBTDebugHelper();
		} catch (ClassNotFoundException ignored) {

		} catch (Exception e1) {
			if (LogisticsPipes.isDEBUG()) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void writeData(LPDataOutput output) {}

	@Override
	public ModernPacket template() {
		return new ActivateNBTDebug(getId());
	}
}
