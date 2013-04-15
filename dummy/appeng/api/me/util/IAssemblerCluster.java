package appeng.api.me.util;

import net.minecraft.tileentity.TileEntity;

public interface IAssemblerCluster {

	public void cycleCpus();
	void addCraft();
	boolean canCraft();
	TileEntity getAssembler(int i);
	
}
