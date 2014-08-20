package logisticspipes.blocks.crafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;

import com.mojang.authlib.GameProfile;

public class FakePlayer extends EntityPlayer {

	public FakePlayer(TileEntity from) {
		super(from.getWorldObj(), new GameProfile(null, "[LogisticsPipes]"));
		posX = from.xCoord;
		posY = from.yCoord + 1;
		posZ = from.zCoord;
	}
	
	@Override public void addChatMessage(IChatComponent c) {}
	@Override public boolean canCommandSenderUseCommand(int i, String s) {return false;}
	@Override public ChunkCoordinates getPlayerCoordinates() {return null;}	
}
