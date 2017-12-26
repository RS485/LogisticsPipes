package logisticspipes.blocks;

import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import lombok.Getter;
import static net.minecraft.util.EnumBlockRenderType.ENTITYBLOCK_ANIMATED;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.blocks.powertile.LogisticsIC2PowerProviderTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.blocks.powertile.LogisticsRFPowerProviderTileEntity;
import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.interfaces.IGuiTileEntity;
import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StreamHelper;

public class LogisticsSolidBlock extends BlockContainer {


	public static final PropertyEnum<BlockType> metaProperty = PropertyEnum.create("block_sub_type", BlockType.class);

	public enum BlockType implements IStringSerializable {
		SOLDERING_STATION("soldering_station", 0),
		LOGISTICS_POWER_JUNCTION("logistics_power_junction", 1),
		LOGISTICS_SECURITY_STATION("logistics_security_station", 2),
		LOGISTICS_AUTOCRAFTING_TABLE("logistics_autocrafting_table", 3),
		LOGISTICS_FUZZYCRAFTING_TABLE("logistics_fuzzycrafting_table", 4),
		LOGISTICS_STATISTICS_TABLE("logistics_statistics_table", 5),

		//Power Provider
		LOGISTICS_RF_POWERPROVIDER("logistics_rf_powerprovider", 10),
		LOGISTICS_IC2_POWERPROVIDER("logistics_ic2_powerprovider", 11),

		LOGISTICS_BLOCK_FRAME("logistics_block_frame", 15);

		@Getter
		String name;
		@Getter
		int meta;
		private BlockType(String name, int meta) {
			this.name = name;
			this.meta = meta;
		}

		public static BlockType getForMeta(int meta) {
			return Arrays.stream(values()).filter(value -> value.meta == meta).collect(StreamHelper.singletonCollector());
		}
	}

	private static final TextureAtlasSprite[] icons = new TextureAtlasSprite[18];
	private static final TextureAtlasSprite[] newTextures = new TextureAtlasSprite[10];

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
		return true;
	}

	public LogisticsSolidBlock() {
		super(Material.IRON);
		setCreativeTab(LogisticsPipes.LPCreativeTab);
		setHardness(6.0F);
		setDefaultState(this.blockState.getBaseState().withProperty(metaProperty, BlockType.SOLDERING_STATION));
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neigbour) {
		super.onNeighborChange(world, pos, neigbour);
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof LogisticsSolidTileEntity) {
			((LogisticsSolidTileEntity) tile).notifyOfBlockChange();
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!playerIn.isSneaking()) {
			TileEntity tile = worldIn.getTileEntity(pos);
			if (tile instanceof IGuiTileEntity) {
				if (MainProxy.isServer(playerIn.world)) {
					((IGuiTileEntity) tile).getGuiProvider().setTilePos(tile).open(playerIn);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	@Nonnull
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, placer, stack);
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof LogisticsCraftingTableTileEntity) {
			((LogisticsCraftingTableTileEntity) tile).placedBy(placer);
		}
		if (tile instanceof IRotationProvider) {
			double x = tile.getPos().getX() + 0.5 - placer.posX;
			double z = tile.getPos().getZ() + 0.5 - placer.posZ;
			// x and z is correct, atan2's first parameter is named y though
			@SuppressWarnings("SuspiciousNameCombination")
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
	public void breakBlock(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof LogisticsSolderingTileEntity) {
			((LogisticsSolderingTileEntity) tile).onBlockBreak();
		}
		if (tile instanceof LogisticsCraftingTableTileEntity) {
			((LogisticsCraftingTableTileEntity) tile).onBlockBreak();
		}
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	@Nonnull
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return ENTITYBLOCK_ANIMATED;
	}

	/*@Override
	public TextureAtlasSprite getIcon(int side, int meta) {
		return getRotatedTexture(meta, side, 2, 0);
	}
	*/

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		switch (BlockType.getForMeta(meta)) {
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
				throw new IllegalArgumentException("Undefined meta");
		}
	}

	@Override
	public int damageDropped(IBlockState state) {
		switch (state.getValue(metaProperty)) {
			case SOLDERING_STATION:
			case LOGISTICS_POWER_JUNCTION:
			case LOGISTICS_SECURITY_STATION:
			case LOGISTICS_AUTOCRAFTING_TABLE:
			case LOGISTICS_FUZZYCRAFTING_TABLE:
			case LOGISTICS_STATISTICS_TABLE:
			case LOGISTICS_RF_POWERPROVIDER:
			case LOGISTICS_IC2_POWERPROVIDER:
			case LOGISTICS_BLOCK_FRAME:
				return state.getValue(metaProperty).meta;
		}
		return super.damageDropped(state);
	}

	/*
	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIcon(IBlockAccess access, int x, int y, int z, int side) {
		int meta = access.getBlockMetadata(x, y, z);
		TileEntity tile = access.getTileEntity(x, y, z);
		if (tile instanceof IRotationProvider) {
			return getRotatedTexture(meta, side, ((IRotationProvider) tile).getRotation(), ((IRotationProvider) tile).getFrontTexture());
		} else {
			return getRotatedTexture(meta, side, 3, 0);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IIconRegister) {
		for (int i = 0; i < LogisticsSolidBlock.icons.length; i++) {
			LogisticsSolidBlock.icons[i] = par1IIconRegister.registerIcon("logisticspipes:lpsolidblock/" + i);
		}
		LogisticsSolidBlock.newTextures[0] = par1IIconRegister.registerIcon("logisticspipes:lpsolidblock/baseTexture"); // Base
		LogisticsSolidBlock.newTextures[1] = par1IIconRegister.registerIcon("logisticspipes:lpsolidblock/solderTexture"); // SOLDERING_STATION
		LogisticsSolidBlock.newTextures[9] = par1IIconRegister.registerIcon("logisticspipes:lpsolidblock/solderTexture_active"); // SOLDERING_STATION Active
		LogisticsSolidBlock.newTextures[2] = par1IIconRegister.registerIcon("logisticspipes:lpsolidblock/powerTexture"); // LOGISTICS_POWER_JUNCTION
		LogisticsSolidBlock.newTextures[3] = par1IIconRegister.registerIcon("logisticspipes:lpsolidblock/securityTexture"); // LOGISTICS_SECURITY_STATION
		LogisticsSolidBlock.newTextures[4] = par1IIconRegister.registerIcon("logisticspipes:lpsolidblock/craftingTexture"); // LOGISTICS_AUTOCRAFTING_TABLE
		LogisticsSolidBlock.newTextures[5] = par1IIconRegister.registerIcon("logisticspipes:lpsolidblock/fuzzycraftingTexture"); // LOGISTICS_FUZZYCRAFTING_TABLE
		LogisticsSolidBlock.newTextures[6] = par1IIconRegister.registerIcon("logisticspipes:lpsolidblock/statisticsTexture"); // LOGISTICS_STATISTICS_TABLE
		LogisticsSolidBlock.newTextures[7] = par1IIconRegister.registerIcon("logisticspipes:lpsolidblock/powerRFTexture"); // LOGISTICS_RF_POWERPROVIDER
		LogisticsSolidBlock.newTextures[8] = par1IIconRegister.registerIcon("logisticspipes:lpsolidblock/powerIC2Texture"); // LOGISTICS_IC2_POWERPROVIDER
	}
	*/

	private TextureAtlasSprite getRotatedTexture(BlockType meta, int side, int rotation, int front) {
		switch (meta) {
			case SOLDERING_STATION:
				if (front == 0) {
					front = 8;
				}
				switch (side) {
					case 1: //TOP
						return LogisticsSolidBlock.icons[1];
					case 0: //Bottom
						return LogisticsSolidBlock.icons[2];
					case 2: //East
						switch (rotation) {
							case 0:
							case 1:
							case 2:
							default:
								return LogisticsSolidBlock.icons[7];
							case 3:
								return LogisticsSolidBlock.icons[front];
						}
					case 3: //West
						switch (rotation) {
							case 0:
							case 1:
							case 3:
							default:
								return LogisticsSolidBlock.icons[7];
							case 2:
								return LogisticsSolidBlock.icons[front];
						}
					case 4: //South
						switch (rotation) {
							case 0:
							case 2:
							case 3:
							default:
								return LogisticsSolidBlock.icons[7];
							case 1:
								return LogisticsSolidBlock.icons[front];
						}
					case 5: //North
						switch (rotation) {
							case 0:
								return LogisticsSolidBlock.icons[front];
							case 1:
							case 2:
							case 3:
							default:
								return LogisticsSolidBlock.icons[7];
						}

					default:
						return LogisticsSolidBlock.icons[0];
				}
			case LOGISTICS_POWER_JUNCTION:
				switch (side) {
					case 1: //TOP
						return LogisticsSolidBlock.icons[4];
					case 0: //Bottom
						return LogisticsSolidBlock.icons[5];
					default: //Front
						return LogisticsSolidBlock.icons[6];
				}
			case LOGISTICS_SECURITY_STATION:
				switch (side) {
					case 1: //TOP
						return LogisticsSolidBlock.icons[9];
					case 0: //Bottom
						return LogisticsSolidBlock.icons[5];
					default: //Front
						return LogisticsSolidBlock.icons[6];
				}
			case LOGISTICS_AUTOCRAFTING_TABLE:
				switch (side) {
					case 1: //TOP
						return LogisticsSolidBlock.icons[11];
					case 0: //Bottom
						return LogisticsSolidBlock.icons[12];
					default: //Front
						return LogisticsSolidBlock.icons[10];
				}
			case LOGISTICS_FUZZYCRAFTING_TABLE:
				switch (side) {
					case 1: //TOP
						return LogisticsSolidBlock.icons[16];
					case 0: //Bottom
						return LogisticsSolidBlock.icons[12];
					default: //Front
						return LogisticsSolidBlock.icons[10];
				}
			case LOGISTICS_STATISTICS_TABLE:
				switch (side) {
					case 1: //TOP
						return LogisticsSolidBlock.icons[17];
					case 0: //Bottom
						return LogisticsSolidBlock.icons[5];
					default: //Front
						return LogisticsSolidBlock.icons[6];
				}
			case LOGISTICS_RF_POWERPROVIDER:
				switch (side) {
					case 1: //TOP
						return LogisticsSolidBlock.icons[14];
					case 0: //Bottom
						return LogisticsSolidBlock.icons[5];
					default: //Front
						return LogisticsSolidBlock.icons[6];
				}
			case LOGISTICS_IC2_POWERPROVIDER:
				switch (side) {
					case 1: //TOP
						return LogisticsSolidBlock.icons[15];
					case 0: //Bottom
						return LogisticsSolidBlock.icons[5];
					default: //Front
						return LogisticsSolidBlock.icons[6];
				}
			case LOGISTICS_BLOCK_FRAME:
				switch (side) {
					case 1: //TOP
						return LogisticsSolidBlock.icons[10];
					default:
						return LogisticsSolidBlock.icons[2];
				}
			default:
				return LogisticsSolidBlock.icons[0];
		}
	}

	public static TextureAtlasSprite getNewIcon(IBlockAccess access, BlockPos pos) {
		IBlockState state = access.getBlockState(pos);
		BlockType meta = state.getValue(metaProperty);
		if (meta == BlockType.SOLDERING_STATION) {
			TileEntity tile = access.getTileEntity(pos);
			if (tile instanceof IRotationProvider) {
				if (((IRotationProvider) tile).getFrontTexture() == 3) {
					return LogisticsSolidBlock.newTextures[9];
				}
			}
		}
		return LogisticsSolidBlock.getNewIcon(meta);
	}

	public static TextureAtlasSprite getNewIcon(BlockType meta) {
		switch (meta) {
			case SOLDERING_STATION:
				return LogisticsSolidBlock.newTextures[1];
			case LOGISTICS_POWER_JUNCTION:
				return LogisticsSolidBlock.newTextures[2];
			case LOGISTICS_SECURITY_STATION:
				return LogisticsSolidBlock.newTextures[3];
			case LOGISTICS_AUTOCRAFTING_TABLE:
				return LogisticsSolidBlock.newTextures[4];
			case LOGISTICS_FUZZYCRAFTING_TABLE:
				return LogisticsSolidBlock.newTextures[5];
			case LOGISTICS_STATISTICS_TABLE:
				return LogisticsSolidBlock.newTextures[6];
			case LOGISTICS_RF_POWERPROVIDER:
				return LogisticsSolidBlock.newTextures[7];
			case LOGISTICS_IC2_POWERPROVIDER:
				return LogisticsSolidBlock.newTextures[8];
			default:
				return LogisticsSolidBlock.newTextures[0];
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, metaProperty);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(metaProperty, BlockType.getForMeta(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(metaProperty).meta;
	}
}
