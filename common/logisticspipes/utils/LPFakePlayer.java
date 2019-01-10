package logisticspipes.utils;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

public class LPFakePlayer extends FakePlayer{
	private static final UUID uuid = UUID.fromString("e7d8e347-3828-4f39-b76f-ea519857c004");

    private static GameProfile PROFILE = new GameProfile(uuid, "[LogisticsPipes]");

    public LPFakePlayer(WorldServer worldIn) {
        super(worldIn, PROFILE);
    }

    @Override
    protected void playEquipSound(ItemStack stack) {

    }

    @Override
    public void openEditSign(TileEntitySign signTile) {

    }
}
