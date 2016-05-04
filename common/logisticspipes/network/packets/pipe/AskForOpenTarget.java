package logisticspipes.network.packets.pipe;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import cpw.mods.fml.client.FMLClientHandler;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class AskForOpenTarget extends ModernPacket {

	public AskForOpenTarget(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {}

	@Override
	public void processPacket(EntityPlayer player) {
		MovingObjectPosition box = FMLClientHandler.instance().getClient().objectMouseOver;
		if (box.typeOfHit == MovingObjectType.BLOCK) {
			MainProxy.sendPacketToServer(
					PacketHandler.getPacket(SlotFinderActivatePacket.class).setTagetPosX(box.blockX).setTagetPosY(box.blockY).setTagetPosZ(box.blockZ));
		}
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {}

	@Override
	public ModernPacket template() {
		return new AskForOpenTarget(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
