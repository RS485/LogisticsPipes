package logisticspipes.network.packets.debug;

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

public class PipeDebugLogAskForTarget extends ModernPacket {

	public PipeDebugLogAskForTarget(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {}

	@Override
	public void processPacket(EntityPlayer player) {
		MovingObjectPosition box = FMLClientHandler.instance().getClient().objectMouseOver;
		if (box != null && box.typeOfHit == MovingObjectType.BLOCK) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(PipeDebugLogResponse.class).setPosX(box.blockX).setPosY(box.blockY).setPosZ(box.blockZ));
		}
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {}

	@Override
	public ModernPacket template() {
		return new PipeDebugLogAskForTarget(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
