package logisticspipes.blocks;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity_BuildCraft;
import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.textures.Textures;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsSolidBlock extends BlockContainer {

	public static final int SOLDERING_STATION = 0;
	public static final int LOGISTICS_POWER_JUNCTION = 1;
	public static final int LOGISTICS_SECURITY_STATION = 2;
	
	private static final Icon[] icons = new Icon[12];
	
	public LogisticsSolidBlock(int par1) {
		super(par1, Material.iron);
		this.setCreativeTab(CreativeTabs.tabBlock);
		this.setHardness(6.0F);
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
				par5EntityPlayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Soldering_Station_ID, par1World, par2, par3, par4);
				return true;
			case LOGISTICS_POWER_JUNCTION:
				par5EntityPlayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Power_Junction_ID, par1World, par2, par3, par4);
				return true;
			case LOGISTICS_SECURITY_STATION:
				par5EntityPlayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Security_Station_ID, par1World, par2, par3, par4);
				return true;
				default:break;
			}
			return false;
		} else {
			return false;
		}
	}

	@Override
	public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLiving par5EntityLiving, ItemStack itemStack) {
		super.onBlockPlacedBy(par1World, par2, par3, par4, par5EntityLiving, itemStack);
		TileEntity tile = par1World.getBlockTileEntity(par2, par3, par4);
		if(tile instanceof IRotationProvider) {
			double x = tile.xCoord - par5EntityLiving.posX;
			double z = tile.zCoord - par5EntityLiving.posZ;
			double w = Math.atan2(x, z);
			double halfPI = Math.PI / 2;
			double halfhalfPI = halfPI / 2;
			w -= halfhalfPI;
			if(w < 0) {
				w += 2 * Math.PI;
			}
			if(0 < w && w <= halfPI) {
				((IRotationProvider)tile).setRotation(1);
			} else if(halfPI < w && w <= 2*halfPI) {
				((IRotationProvider)tile).setRotation(2);
			} else if(2*halfPI < w && w <= 3*halfPI) {
				((IRotationProvider)tile).setRotation(0);
			} else if(3*halfPI < w && w <= 4*halfPI) {
				((IRotationProvider)tile).setRotation(3);
			}
		}
	}

	@Override
	public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6) {
		TileEntity tile = par1World.getBlockTileEntity(par2, par3, par4);
		if(tile instanceof LogisticsSolderingTileEntity) {
			((LogisticsSolderingTileEntity)tile).onBlockBreak();
		}
		super.breakBlock(par1World, par2, par3, par4, par5, par6);
	}

	@Override
	public Icon getIcon(int side, int meta) {
		return getRotatedTexture(meta, side, 2, 0);
	}
	
	@Override
	public TileEntity createNewTileEntity(World var1) {
		new UnsupportedOperationException("Please call createNewTileEntity(World,int) instead of createNewTileEntity(World).").printStackTrace();
		return createTileEntity(var1, 0);
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata) {
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
	    	case LOGISTICS_SECURITY_STATION:
	    		return new LogisticsSecurityTileEntity();
        	default: 
        		return null;
        }
    }

	@Override
	public int damageDropped(int par1) {
		switch(par1) {
		case SOLDERING_STATION:
		case LOGISTICS_POWER_JUNCTION:
			return par1;
		}
		return super.damageDropped(par1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess access, int x, int y, int z, int side) {
		int meta = access.getBlockMetadata(x, y, z);
		TileEntity tile = access.getBlockTileEntity(x, y, z);
		if(tile instanceof IRotationProvider) {
			return getRotatedTexture(meta, side, ((IRotationProvider)tile).getRotation(), ((IRotationProvider)tile).getFrontTexture());
		} else {
			return getRotatedTexture(meta, side, 3, 0);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		/*icons = new Icon[3];
		int i = 0;
		for (String s : sideNames)
		{
			//do sth
		}*/

	}
	
	private Icon getRotatedTexture(int meta, int side, int rotation, int front) {
		/*switch (meta) {
		case SOLDERING_STATION:
			if(front == 0) {
				front = 17;
			}
			switch (side) {
			case 1: //TOP
				return 1;
			case 0: //Bottom
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
				return 16;
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
			case 1: //TOP
				return 4;
			case 0: //Bottom
				return 5;
			default: //Front
				return 6;
			}
		case LOGISTICS_SECURITY_STATION:
			switch (side) {
			case 1: //TOP
				return 20;
			case 0: //Bottom
				return 21;
			default: //Front
				return 22;
			}
		default:
			return 0;
		}*/
		return Textures.BASE_TEXTURE_FILE;
	}
	
}
