package thaumcraft.common;

import java.util.List;
import java.util.Map;

import thaumcraft.common.lib.research.PlayerKnowledge;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler {
	public PlayerKnowledge playerKnowledge;
	@Override public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {return null;}
	@Override public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {return null;}
	public Map<String, List<String>> getScannedObjects() {return null;}
}
