package logisticspipes.proxy.interfaces;

import logisticspipes.utils.ItemIdentifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.Player;

public interface IProxy {
	public String getSide();
	public World getWorld();
	public void registerTileEntitis();
	public World getWorld(int _dimension);
	public EntityPlayer getClientPlayer();
	public boolean isMainThreadRunning();
	public void addLogisticsPipesOverride(int index, String override1, String override2);
	public void registerParticles();
	public String getName(ItemIdentifier item);
	public void updateNames(ItemIdentifier item, String name);
	public void tick();
	public void sendNameUpdateRequest(Player player);
}