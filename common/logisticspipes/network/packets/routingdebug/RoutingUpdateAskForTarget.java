package logisticspipes.network.packets.routingdebug;

import java.io.IOException;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.routingdebug.RoutingUpdateTargetResponse.TargetMode;
import logisticspipes.proxy.MainProxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

import cpw.mods.fml.client.FMLClientHandler;

public class RoutingUpdateAskForTarget extends ModernPacket {

	public RoutingUpdateAskForTarget(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {}

	@Override
	@ClientSideOnlyMethodContent
	public void processPacket(EntityPlayer player) {
		MovingObjectPosition box = FMLClientHandler.instance().getClient().objectMouseOver;
		if (box == null) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RoutingUpdateTargetResponse.class).setMode(TargetMode.None));
		} else if (box.typeOfHit == MovingObjectType.BLOCK) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RoutingUpdateTargetResponse.class).setMode(TargetMode.Block).setAdditions(new Object[] { box.blockX, box.blockY, box.blockZ }));
		} else if (box.typeOfHit == MovingObjectType.ENTITY) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RoutingUpdateTargetResponse.class).setMode(TargetMode.Entity).setAdditions(new Object[] { box.entityHit.getEntityId() }));
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {}

	@Override
	public ModernPacket template() {
		return new RoutingUpdateAskForTarget(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
