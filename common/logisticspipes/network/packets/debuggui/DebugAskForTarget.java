package logisticspipes.network.packets.debuggui;

import java.io.IOException;

import cpw.mods.fml.client.FMLClientHandler;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;

public class DebugAskForTarget extends ModernPacket {

	public DebugAskForTarget(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {}

	@Override
	public void processPacket(EntityPlayer player) {
		MovingObjectPosition box = FMLClientHandler.instance().getClient().objectMouseOver;
		if (box == null) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(DebugTargetResponse.class).setMode(DebugTargetResponse.TargetMode.None));
		} else if (box.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(DebugTargetResponse.class).setMode(DebugTargetResponse.TargetMode.Block).setAdditions(new Object[] { box.blockX, box.blockY, box.blockZ }));
		} else if (box.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(DebugTargetResponse.class).setMode(DebugTargetResponse.TargetMode.Entity).setAdditions(new Object[] { box.entityHit.getEntityId() }));
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {}

	@Override
	public ModernPacket template() {
		return new DebugAskForTarget(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
