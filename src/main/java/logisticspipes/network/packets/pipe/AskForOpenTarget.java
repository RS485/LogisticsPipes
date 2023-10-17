package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fml.client.FMLClientHandler;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class AskForOpenTarget extends ModernPacket {

	public AskForOpenTarget(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {}

	@Override
	public void processPacket(EntityPlayer player) {
		RayTraceResult box = FMLClientHandler.instance().getClient().objectMouseOver;
		if (box.typeOfHit == RayTraceResult.Type.BLOCK) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SlotFinderActivatePacket.class)
					.setTargetPosX(box.getBlockPos().getX())
					.setTargetPosY(box.getBlockPos().getY())
					.setTargetPosZ(box.getBlockPos().getZ()));
		}
	}

	@Override
	public void writeData(LPDataOutput output) {}

	@Override
	public ModernPacket template() {
		return new AskForOpenTarget(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
