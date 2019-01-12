package logisticspipes.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.List;
import java.util.UUID;

public class FakePlayerLP extends FakePlayer {

    public static GameProfile LPPLAYER = new GameProfile(UUID.fromString("e7d8e347-3828-4f39-b76f-ea519857c004"), "[LogisticsPipes]");

    public String myName = "[LogisticsPipes]";

    public FakePlayerLP(WorldServer world) {
        super(world, LPPLAYER);
        connection = new FakeNetServerHandler(FMLCommonHandler.instance().getMinecraftServerInstance(), this);
        this.addedToChunk = false;
        this.posX = 0;
        this.posY = 0;
        this.posZ = 0;
    }

    @Override
    public ITextComponent getDisplayName() {

        return new TextComponentString(getName());
    }

    @Override
    public void onUpdate() {

    }

    @Override
    public void unlockRecipes(List<IRecipe> p_192021_1_) {

    }

    @Override
    public void unlockRecipes(ResourceLocation[] p_193102_1_) {

    }

    @Override
    public void resetRecipes(List<IRecipe> p_192022_1_) {

    }
}
