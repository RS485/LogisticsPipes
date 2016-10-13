package logisticspipes.network.packets.debuggui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;

import cpw.mods.fml.client.FMLClientHandler;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class DebugAskForTarget extends ModernPacket {

	public DebugAskForTarget(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {}

	@Override
	public void processPacket(EntityPlayer player) {
		MovingObjectPosition box = FMLClientHandler.instance().getClient().objectMouseOver;
		if (box == null) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(DebugTargetResponse.class).setMode(DebugTargetResponse.TargetMode.None));
		} else if (box.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(DebugTargetResponse.class).setMode(DebugTargetResponse.TargetMode.Block)
					.setAdditions(new int[] { box.blockX, box.blockY, box.blockZ }));
		} else if (box.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(DebugTargetResponse.class).setMode(DebugTargetResponse.TargetMode.Entity)
					.setAdditions(new int[] { box.entityHit.getEntityId() }));
		}
	}

	@Override
	public void writeData(LPDataOutput output) {}

	@Override
	public ModernPacket template() {
		return new DebugAskForTarget(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
