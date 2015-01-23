package logisticspipes.blocks;


import logisticspipes.LPConstants;
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.blocks.powertile.LogisticsIC2PowerProviderTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.blocks.powertile.LogisticsRFPowerProviderTileEntity;
import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.interfaces.IGuiTileEntity;
import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.proxy.MainProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsSolidBlock extends BlockContainer {

	public static final int SOLDERING_STATION = 0;
	public static final int LOGISTICS_POWER_JUNCTION = 1;
	public static final int LOGISTICS_SECURITY_STATION = 2;
	public static final int LOGISTICS_AUTOCRAFTING_TABLE = 3;
	public static final int LOGISTICS_FUZZYCRAFTING_TABLE = 4;
	public static final int LOGISTICS_STATISTICS_TABLE = 5;

	//Power Provider
	public static final int LOGISTICS_RF_POWERPROVIDER = 11;
	public static final int LOGISTICS_IC2_POWERPROVIDER = 12;
	
	private static final IIcon[] icons = new IIcon[18];
	
	public LogisticsSolidBlock() {
		super(Material.iron);
		this.setCreativeTab(CreativeTabs.tabBlock);
		this.setHardness(6.0F);
	}

	@Override
	public void onBlockClicked(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer) {
		super.onBlockClicked(par1World, par2, par3, par4, par5EntityPlayer);
	}

	@Override
	public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
		super.onNeighborChange(world, x, y, z, tileX, tileY, tileZ);
		TileEntity tile = world.getTileEntity(x, y, z);
		if(tile instanceof LogisticsSolidTileEntity) {
			((LogisticsSolidTileEntity)tile).notifyOfBlockChange();
		}
	}

	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7,float par8, float par9) {
		if(!par5EntityPlayer.isSneaking()) {
			TileEntity tile = par1World.getTileEntity(par2, par3, par4);
			if(tile instanceof IGuiTileEntity) {
				if(MainProxy.isServer(par5EntityPlayer.worldObj)) {
					((IGuiTileEntity)tile).getGuiProvider().setTilePos(tile).open(par5EntityPlayer);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, int posX, int posY, int posZ, EntityLivingBase entity, ItemStack itemStack) {
		super.onBlockPlacedBy(world, posX, posY, posZ, entity, itemStack);
		TileEntity tile = world.getTileEntity(posX, posY, posZ);
		if(tile instanceof LogisticsCraftingTableTileEntity) {
			((LogisticsCraftingTableTileEntity)tile).placedBy(entity);
		}
		if(tile instanceof IRotationProvider) {
			double x = tile.xCoord + 0.5 - entity.posX;
			double z = tile.zCoord + 0.5 - entity.posZ;
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
	public void breakBlock(World par1World, int par2, int par3, int par4, Block par5, int par6) {
		TileEntity tile = par1World.getTileEntity(par2, par3, par4);
		if(tile instanceof LogisticsSolderingTileEntity) {
			((LogisticsSolderingTileEntity)tile).onBlockBreak();
		}
		if(tile instanceof LogisticsCraftingTableTileEntity) {
			((LogisticsCraftingTableTileEntity)tile).onBlockBreak();
		}
		super.breakBlock(par1World, par2, par3, par4, par5, par6);
	}

	@Override
	public int getRenderType() {
		return LPConstants.solidBlockModel;
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return getRotatedTexture(meta, side, 2, 0);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
        switch(metadata) {
	    	case SOLDERING_STATION:
	    		return new LogisticsSolderingTileEntity();
	    	case LOGISTICS_POWER_JUNCTION:
	    		return new LogisticsPowerJunctionTileEntity();
	    	case LOGISTICS_SECURITY_STATION:
	    		return new LogisticsSecurityTileEntity();
			case LOGISTICS_AUTOCRAFTING_TABLE:
			case LOGISTICS_FUZZYCRAFTING_TABLE:
				return new LogisticsCraftingTableTileEntity();
			case LOGISTICS_STATISTICS_TABLE:
				return new LogisticsStatisticsTileEntity();
			case LOGISTICS_RF_POWERPROVIDER:
				return new LogisticsRFPowerProviderTileEntity();
			case LOGISTICS_IC2_POWERPROVIDER:
				return new LogisticsIC2PowerProviderTileEntity();
        	default: 
        		return null;
        }
    }

	@Override
	public int damageDropped(int par1) {
		switch(par1) {
		case SOLDERING_STATION:
		case LOGISTICS_POWER_JUNCTION:
		case LOGISTICS_SECURITY_STATION:
		case LOGISTICS_AUTOCRAFTING_TABLE:
		case LOGISTICS_FUZZYCRAFTING_TABLE:
		case LOGISTICS_STATISTICS_TABLE:
		case LOGISTICS_RF_POWERPROVIDER:
		case LOGISTICS_IC2_POWERPROVIDER:
			return par1;
		}
		return super.damageDropped(par1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess access, int x, int y, int z, int side) {
		int meta = access.getBlockMetadata(x, y, z);
		TileEntity tile = access.getTileEntity(x, y, z);
		if(tile instanceof IRotationProvider) {
			return getRotatedTexture(meta, side, ((IRotationProvider)tile).getRotation(), ((IRotationProvider)tile).getFrontTexture());
		} else {
			return getRotatedTexture(meta, side, 3, 0);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IIconRegister) {
		for(int i=0;i<icons.length;i++) {
			icons[i]=par1IIconRegister.registerIcon("logisticspipes:lpsolidblock/"+i);
		}
	}
	
	private IIcon getRotatedTexture(int meta, int side, int rotation, int front) {
		switch (meta) {
		case SOLDERING_STATION:
			if(front == 0) {
				front = 8;
			}
			switch (side) {
			case 1: //TOP
				return icons[1];
			case 0: //Bottom
				return icons[2];
			case 2: //East
				switch(rotation) {
				case 0:
				case 1:
				case 2:
				default:
					return icons[7];
				case 3:
					return icons[front];
				}
			case 3: //West
				switch(rotation) {
				case 0:
				case 1:
				case 3:
				default:
					return icons[7];
				case 2:
					return icons[front];
				}
			case 4: //South
				switch(rotation) {
				case 0:
				case 2:
				case 3:
				default:
				return icons[7];
				case 1:
					return icons[front];
				}
			case 5: //North
				switch(rotation) {
				case 0:
					return icons[front];
				case 1:
				case 2:
				case 3:
				default:
					return icons[7];
				}
				
			default:
				return icons[0];
			}
		case LOGISTICS_POWER_JUNCTION:
			switch (side) {
			case 1: //TOP
				return icons[4];
			case 0: //Bottom
				return icons[5];
			default: //Front
				return icons[6];
			}
		case LOGISTICS_SECURITY_STATION:
			switch (side) {
			case 1: //TOP
				return icons[9];
			case 0: //Bottom
				return icons[5];
			default: //Front
				return icons[6];
			}
		case LOGISTICS_AUTOCRAFTING_TABLE:
			switch (side) {
			case 1: //TOP
				return icons[11];
			case 0: //Bottom
				return icons[12];
			default: //Front
				return icons[10];
			}
		case LOGISTICS_FUZZYCRAFTING_TABLE:
			switch (side) {
			case 1: //TOP
				return icons[16];
			case 0: //Bottom
				return icons[12];
			default: //Front
				return icons[10];
			}
		case LOGISTICS_STATISTICS_TABLE:
			switch (side) {
			case 1: //TOP
				return icons[17];
			case 0: //Bottom
				return icons[5];
			default: //Front
				return icons[6];
			}
		case LOGISTICS_RF_POWERPROVIDER:
			switch (side) {
			case 1: //TOP
				return icons[14];
			case 0: //Bottom
				return icons[5];
			default: //Front
				return icons[6];
			}
		case LOGISTICS_IC2_POWERPROVIDER:
			switch (side) {
			case 1: //TOP
				return icons[15];
			case 0: //Bottom
				return icons[5];
			default: //Front
				return icons[6];
			}
		default:
			return icons[0];
		}
	}
}
