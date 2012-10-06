package logisticspipes.blocks;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity_BuildCraft;
import logisticspipes.config.Textures;
import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.proxy.side.ClientProxy;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

public class LogisticsSolidBlock extends BlockContainer {

	public static final int SOLDERING_STATION = 0;
	public static final int LOGISTICS_POWER_JUNCTION = 1;
	
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
			switch(par1World.getBlockMetadata(par2, par3, par4)) {
			case SOLDERING_STATION:
				par5EntityPlayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Soldering_Station, par1World, par2, par3, par4);
				return true;
			case LOGISTICS_POWER_JUNCTION:
				
				return false;
				default:break;
			}
			return false;
		} else {
			return false;
		}
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
		return getRotatedTexture(meta, side, 2, 0);
	}

	//@Override
	///public int getRenderType() {
	//	return ClientProxy.solidBlockRenderId;
	//}
	
	@Override
	public TileEntity createNewTileEntity(World var1) {
		new UnsupportedOperationException("Please call createNewTileEntity(World,int) instead of createNewTileEntity(World).").printStackTrace();
		return createNewTileEntity(var1, 0);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
        switch(metadata) {
	    	case SOLDERING_STATION:
	    		return new LogisticsSolderingTileEntity();
	    	case LOGISTICS_POWER_JUNCTION:
				LogisticsPowerJuntionTileEntity_BuildCraft instance;
				try {
					instance = LogisticsPipes.powerTileEntity.newInstance();
				} catch (Exception e) {
					e.printStackTrace();
					instance = new LogisticsPowerJuntionTileEntity_BuildCraft();
				}
	    		return instance;
        	default: 
        		return null;
        }
    }

	@Override
	@SideOnly(Side.CLIENT)
	public int getBlockTexture(IBlockAccess access, int x, int y, int z, int side) {
		int meta = access.getBlockMetadata(x, y, z);
		TileEntity tile = access.getBlockTileEntity(x, y, z);
		if(tile instanceof IRotationProvider) {
			return getRotatedTexture(meta, side, ((IRotationProvider)tile).getRotation(), ((IRotationProvider)tile).getFrontTexture());
		} else {
			return getRotatedTexture(meta, side, 3, 0);
		}
	}
	
	private int getRotatedTexture(int meta, int side, int rotation, int front) {
		switch (meta) {
		case SOLDERING_STATION:
			if(front == 0) {
				front = 17;
			}
			switch (side) {
			case 1: //TOP
				return 1;
			case 6: //Bottom
				return 2;
			case 2: //East
				switch(rotation) {
				case 0:
					return 16;
				case 1:
					return 18;
				case 2:
					return 19;
				case 3:
					return front;
				}
			case 3: //West
				switch(rotation) {
				case 0:
					return 18;
				case 1:
					return 19;
				case 2:
					return front;
				case 3:
					return 16;
				}
				return 18;
			case 4: //South
				switch(rotation) {
				case 0:
					return 19;
				case 1:
					return front;
				case 2:
					return 16;
				case 3:
					return 18;
				}
				return 19;
			case 5: //North
				switch(rotation) {
				case 0:
					return front;
				case 1:
					return 16;
				case 2:
					return 18;
				case 3:
					return 19;
				}
				return front;
			default:
				return 0;
			}
		case LOGISTICS_POWER_JUNCTION:
			switch (side) {
			case 1:
				return 43;
			default:
				return 22;
			}
		default:
			return 0;
		}
	}
	
	@Override
	public String getTextureFile() {
		return Textures.LOGISTICS_SOLID_BLOCK;
	}
}
