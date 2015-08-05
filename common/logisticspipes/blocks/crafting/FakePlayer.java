package logisticspipes.blocks.crafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

public class FakePlayer extends EntityPlayer {

	public FakePlayer(TileEntity from) {
		super(from.getWorldObj(), new GameProfile(UUID.fromString("e7d8e347-3828-4f39-b76f-ea519857c004"), "[LogisticsPipes]"));
		posX = from.xCoord;
		posY = from.yCoord + 1;
		posZ = from.zCoord;
	}

	@Override
	public void addChatMessage(IChatComponent c) {}

	@Override
	public boolean canCommandSenderUseCommand(int i, String s) {
		return false;
	}

	@Override
	public ChunkCoordinates getPlayerCoordinates() {
		return null;
	}
}
