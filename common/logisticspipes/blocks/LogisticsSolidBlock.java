package logisticspipes.blocks;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.blocks.powertile.LogisticsRFPowerProviderTileEntity;
import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.interfaces.IGuiTileEntity;
import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.proxy.MainProxy;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

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

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	public LogisticsSolidBlock() {
		super(Material.iron);
		setCreativeTab(LogisticsPipes.LPCreativeTab);
		setHardness(6.0F);
	}

	@Override
	public void onBlockClicked(World par1World, BlockPos pos, EntityPlayer par5EntityPlayer) {
		super.onBlockClicked(par1World, pos, par5EntityPlayer);
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(world, pos, neighbor);
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof LogisticsSolidTileEntity) {
			((LogisticsSolidTileEntity) tile).notifyOfBlockChange();
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!player.isSneaking()) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof IGuiTileEntity) {
				if (MainProxy.isServer(player.worldObj)) {
					((IGuiTileEntity) tile).getGuiProvider().setTilePos(tile).open(player);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos,IBlockState state, EntityLivingBase entity, ItemStack itemStack) {
		super.onBlockPlacedBy(world, pos, state, entity, itemStack);
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof LogisticsCraftingTableTileEntity) {
			((LogisticsCraftingTableTileEntity) tile).placedBy(entity);
		}
		if (tile instanceof IRotationProvider) {
			double x = tile.getPos().getX() + 0.5 - entity.posX;
			double z = tile.getPos().getZ() + 0.5 - entity.posZ;
			double w = Math.atan2(x, z);
			double halfPI = Math.PI / 2;
			double halfhalfPI = halfPI / 2;
			w -= halfhalfPI;
			if (w < 0) {
				w += 2 * Math.PI;
			}
			if (0 < w && w <= halfPI) {
				((IRotationProvider) tile).setRotation(1);
			} else if (halfPI < w && w <= 2 * halfPI) {
				((IRotationProvider) tile).setRotation(2);
			} else if (2 * halfPI < w && w <= 3 * halfPI) {
				((IRotationProvider) tile).setRotation(0);
			} else if (3 * halfPI < w && w <= 4 * halfPI) {
				((IRotationProvider) tile).setRotation(3);
			}
		}
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState stat) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof LogisticsSolderingTileEntity) {
			((LogisticsSolderingTileEntity) tile).onBlockBreak();
		}
		if (tile instanceof LogisticsCraftingTableTileEntity) {
			((LogisticsCraftingTableTileEntity) tile).onBlockBreak();
		}
		super.breakBlock(worldIn, pos, stat);
	}

	@Override
	public int getRenderType() {
		return LPConstants.solidBlockModel;
	}

//TODO // FIXME: 21-2-2016 
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		switch (metadata) {
			case SOLDERING_STATION:
				return new LogisticsSolderingTileEntity();
			case LOGISTICS_POWER_JUNCTION:
				//return new LogisticsPowerJunctionTileEntity();
				return null;
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
				//return new LogisticsIC2PowerProviderTileEntity();
				return null;
			default:
				return null;
		}
	}

//	@Override
//	public int damageDropped(IBlockState state) {
//		switch (state) {
//			case SOLDERING_STATION:
//			case LOGISTICS_POWER_JUNCTION:
//			case LOGISTICS_SECURITY_STATION:
//			case LOGISTICS_AUTOCRAFTING_TABLE:
//			case LOGISTICS_FUZZYCRAFTING_TABLE:
//			case LOGISTICS_STATISTICS_TABLE:
//			case LOGISTICS_RF_POWERPROVIDER:
//			case LOGISTICS_IC2_POWERPROVIDER:
//				return state;
//		}
//		return super.damageDropped(state);
	}






