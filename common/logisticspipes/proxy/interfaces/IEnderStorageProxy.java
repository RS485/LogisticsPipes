package logisticspipes.proxy.interfaces;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface IEnderStorageProxy {

	public boolean isEnderChestBlock(Block block);

	public void openEnderChest(World world, int x, int y, int z, EntityPlayer player);
}
