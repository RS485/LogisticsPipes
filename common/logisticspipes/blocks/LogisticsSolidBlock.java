package logisticspipes.blocks;

import logisticspipes.LogisticsPipes;
import logisticspipes.main.GuiIDs;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class LogisticsSolidBlock extends BlockContainer {
	
	public static final int SOLDERING_STATION = 0;
	
	public LogisticsSolidBlock(int par1) {
		super(par1, Material.iron);
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	@Override
	public void onBlockClicked(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer) {
		super.onBlockClicked(par1World, par2, par3, par4, par5EntityPlayer);
	}

	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7,float par8, float par9) {
		if(!par5EntityPlayer.isSneaking()) {
			par5EntityPlayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Soldering_Station, par1World, par2, par3, par4);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int par1, int par2) {
		switch (par2) {
		case SOLDERING_STATION:
			switch (par1) {
			case 1:
				return 43;
			default:
				return 22;
			}
		default:
			return super.getBlockTextureFromSideAndMetadata(par1, par2);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return createNewTileEntity(var1, 0);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
        switch(metadata) {
        	case SOLDERING_STATION:
        		return new LogisticsSolderingTileEntity();
        	default: 
        		return null;
        }
    }
}
