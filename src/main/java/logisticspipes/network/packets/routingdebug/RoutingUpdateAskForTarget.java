package logisticspipes.network.packets.routingdebug;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fml.client.FMLClientHandler;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.routingdebug.RoutingUpdateTargetResponse.TargetMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class RoutingUpdateAskForTarget extends ModernPacket {

	public RoutingUpdateAskForTarget(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {}

	@Override
	@ClientSideOnlyMethodContent
	public void processPacket(EntityPlayer player) {
		RayTraceResult box = FMLClientHandler.instance().getClient().objectMouseOver;
		if (box == null) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RoutingUpdateTargetResponse.class).setMode(TargetMode.None));
		} else if (box.typeOfHit == RayTraceResult.Type.BLOCK) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RoutingUpdateTargetResponse.class).setMode(TargetMode.Block)
					.setAdditions(new int[] { box.getBlockPos().getX(), box.getBlockPos().getY(), box.getBlockPos().getZ() }));
		} else if (box.typeOfHit == RayTraceResult.Type.ENTITY) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RoutingUpdateTargetResponse.class).setMode(TargetMode.Entity)
					.setAdditions(new int[] { box.entityHit.getEntityId() }));
		}
	}

	@Override
	public void writeData(LPDataOutput output) {}

	@Override
	public ModernPacket template() {
		return new RoutingUpdateAskForTarget(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
