package codechicken.enderstorage.common;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockEnderStorage extends BlockContainer {
	
	protected BlockEnderStorage(int par1, Material par2Material) {
		super(par1, par2Material);
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return null;
	}
}
