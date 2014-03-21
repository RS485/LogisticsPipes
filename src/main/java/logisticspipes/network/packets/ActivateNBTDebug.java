package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.Configs;
import logisticspipes.LogisticsPipes;
import logisticspipes.nei.LoadingHelper;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;

public class ActivateNBTDebug extends ModernPacket {

	public ActivateNBTDebug(int id) {
		super(id);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {}

	@Override
	public void processPacket(EntityPlayer player) {
		try {
			Class.forName("codechicken.nei.forge.GuiContainerManager");
			Configs.TOOLTIP_INFO = true;
			LoadingHelper.LoadNeiNBTDebugHelper();
		} catch(ClassNotFoundException e) {

		} catch(Exception e1) {
			if(LogisticsPipes.DEBUG) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {}

	@Override
	public ModernPacket template() {
		return new ActivateNBTDebug(getId());
	}
}