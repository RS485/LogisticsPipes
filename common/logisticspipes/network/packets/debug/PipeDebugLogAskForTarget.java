package logisticspipes.network.packets.debug;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

import cpw.mods.fml.client.FMLClientHandler;

import lombok.experimental.Accessors;

@Accessors(chain = true)
public class PipeDebugLogAskForTarget extends ModernPacket {

	public PipeDebugLogAskForTarget(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {}

	@Override
	public void processPacket(EntityPlayer player) {
		MovingObjectPosition box = FMLClientHandler.instance().getClient().objectMouseOver;
		if (box != null && box.typeOfHit == MovingObjectType.BLOCK) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(PipeDebugLogResponse.class).setPosX(box.blockX).setPosY(box.blockY).setPosZ(box.blockZ));
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {}

	@Override
	public ModernPacket template() {
		return new PipeDebugLogAskForTarget(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
