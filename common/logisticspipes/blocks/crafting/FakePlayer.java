package logisticspipes.blocks.crafting;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IChatComponent;

import java.util.UUID;

public class FakePlayer extends EntityPlayer {

	public FakePlayer(TileEntity from) {
		super(from.getWorld(), new GameProfile(UUID.fromString("e7d8e347-3828-4f39-b76f-ea519857c004"), "[LogisticsPipes]"));
		posX = from.getPos().getX();
		posY = from.getPos().getY() + 1;
		posZ = from.getPos().getZ();
	}

	@Override
	public void addChatMessage(IChatComponent c) {
	}

	@Override
	public boolean canCommandSenderUseCommand(int i, String s) {
		return false;
	}

	@Override
	public boolean isSpectator() {
		return false;
	}
}
