package logisticspipes.blocks.crafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;

public class FakePlayer extends EntityPlayer {

	public FakePlayer(TileEntity from) {
		super(from.worldObj);
		posX = from.xCoord;
		posY = from.yCoord + 1;
		posZ = from.zCoord;
		username = "[LogisticsPipes]";
	}
	
	@Override public void sendChatToPlayer(String s) {}
	@Override public boolean canCommandSenderUseCommand(int i, String s) {return false;}
	@Override public ChunkCoordinates getPlayerCoordinates() {return null;}	
}
