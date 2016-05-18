package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import cpw.mods.fml.client.FMLClientHandler;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class PipeDebugAskForTarget extends ModernPacket {

	@Setter
	@Getter
	private boolean isServer;

	public PipeDebugAskForTarget(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		isServer = input.readBoolean();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		MovingObjectPosition box = FMLClientHandler.instance().getClient().objectMouseOver;
		if (box != null && box.typeOfHit == MovingObjectType.BLOCK) {
			if (!isServer) {
				TileEntity tile = new DoubleCoordinates(box.blockX, box.blockY, box.blockZ).getTileEntity(player.getEntityWorld());
				if (tile instanceof LogisticsTileGenericPipe) {
					((LogisticsTileGenericPipe) tile).pipe.debug.debugThisPipe = !((LogisticsTileGenericPipe) tile).pipe.debug.debugThisPipe;
					if (((LogisticsTileGenericPipe) tile).pipe.debug.debugThisPipe) {
						player.addChatComponentMessage(new ChatComponentText("Debug enabled On Client"));
					} else {
						player.addChatComponentMessage(new ChatComponentText("Debug disabled On Client"));
					}
				}
			} else {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(PipeDebugResponse.class).setPosX(box.blockX).setPosY(box.blockY).setPosZ(box.blockZ));
			}
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeBoolean(isServer);
	}

	@Override
	public ModernPacket template() {
		return new PipeDebugAskForTarget(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
