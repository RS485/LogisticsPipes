package logisticspipes.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import lombok.Getter;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.blocks.powertile.LogisticsIC2PowerProviderTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.blocks.powertile.LogisticsRFPowerProviderTileEntity;
import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.interfaces.IGuiTileEntity;
import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StreamHelper;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class LogisticsSolidBlock extends BlockContainer {

	public static final PropertyEnum<BlockType> metaProperty = PropertyEnum.create("block_sub_type", BlockType.class);
	public static final PropertyInteger rotationProperty = PropertyInteger.create("rotation", 0, 3);
	public static final PropertyInteger textureIndexProperty = PropertyInteger.create("texture_index", 0, 11);
	public static final Map<EnumFacing, PropertyBool> connectionPropertys = Arrays.stream(EnumFacing.values()).collect(Collectors.toMap(key -> key, key -> PropertyBool.create("connection_" + key.ordinal())));


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
		LOGISTICS_BC_POWERPROVIDER("logistics_bc_powerprovider", 12),

		LOGISTICS_PROGRAM_COMPILER("logistics_program_compiler", 14),

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

		public static BlockType getForName(String name) {
			return Arrays.stream(values()).filter(value -> value.name.equals(name)).collect(StreamHelper.singletonCollector());
		}
	}

	@SideOnly(Side.CLIENT)
	private static TextureAtlasSprite[] newTextures;

	static {
		if(FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			newTextures = new TextureAtlasSprite[12];
		}
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
		return true;
	}

	public LogisticsSolidBlock() {
		super(Material.IRON);
		setHardness(6.0F);
		IBlockState state = this.blockState.getBaseState().withProperty(metaProperty, BlockType.SOLDERING_STATION)
				.withProperty(rotationProperty, 0)
				.withProperty(textureIndexProperty, 0);
		connectionPropertys.values().forEach(it -> state.withProperty(it, false));
		setDefaultState(state);
		setCreativeTab(LogisticsPipes.CREATIVE_TAB_LP);
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
		if (tile instanceof LogisticsProgramCompilerTileEntity) {
			((LogisticsProgramCompilerTileEntity) tile).onBlockBreak();
		}
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	@Nonnull
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

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
			case LOGISTICS_BC_POWERPROVIDER:
				return null;//new LogisticsBCPowerProvider();
			case LOGISTICS_PROGRAM_COMPILER:
				return new LogisticsProgramCompilerTileEntity();
			case LOGISTICS_BLOCK_FRAME:
				return null;
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
			case LOGISTICS_BC_POWERPROVIDER:
			case LOGISTICS_PROGRAM_COMPILER:
			case LOGISTICS_BLOCK_FRAME:
				return state.getValue(metaProperty).meta;
		}
		return super.damageDropped(state);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(TextureMap par1IIconRegister) {
		LogisticsSolidBlock.newTextures[0] = par1IIconRegister.registerSprite(new ResourceLocation("logisticspipes:solid_block/baseTexture")); // Base
		LogisticsSolidBlock.newTextures[1] = par1IIconRegister.registerSprite(new ResourceLocation("logisticspipes:solid_block/solderTexture")); // SOLDERING_STATION
		LogisticsSolidBlock.newTextures[9] = par1IIconRegister.registerSprite(new ResourceLocation("logisticspipes:solid_block/solderTexture_active")); // SOLDERING_STATION Active
		LogisticsSolidBlock.newTextures[2] = par1IIconRegister.registerSprite(new ResourceLocation("logisticspipes:solid_block/powerTexture")); // LOGISTICS_POWER_JUNCTION
		LogisticsSolidBlock.newTextures[3] = par1IIconRegister.registerSprite(new ResourceLocation("logisticspipes:solid_block/securityTexture")); // LOGISTICS_SECURITY_STATION
		LogisticsSolidBlock.newTextures[4] = par1IIconRegister.registerSprite(new ResourceLocation("logisticspipes:solid_block/craftingTexture")); // LOGISTICS_AUTOCRAFTING_TABLE
		LogisticsSolidBlock.newTextures[5] = par1IIconRegister.registerSprite(new ResourceLocation("logisticspipes:solid_block/fuzzycraftingTexture")); // LOGISTICS_FUZZYCRAFTING_TABLE
		LogisticsSolidBlock.newTextures[6] = par1IIconRegister.registerSprite(new ResourceLocation("logisticspipes:solid_block/statisticsTexture")); // LOGISTICS_STATISTICS_TABLE
		LogisticsSolidBlock.newTextures[7] = par1IIconRegister.registerSprite(new ResourceLocation("logisticspipes:solid_block/powerRFTexture")); // LOGISTICS_RF_POWERPROVIDER
		LogisticsSolidBlock.newTextures[8] = par1IIconRegister.registerSprite(new ResourceLocation("logisticspipes:solid_block/powerIC2Texture")); // LOGISTICS_IC2_POWERPROVIDER
		LogisticsSolidBlock.newTextures[10] = par1IIconRegister.registerSprite(new ResourceLocation("logisticspipes:solid_block/powerBCTexture")); // LOGISTICS_BC_POWERPROVIDER
		LogisticsSolidBlock.newTextures[11] = par1IIconRegister.registerSprite(new ResourceLocation("logisticspipes:solid_block/compilerTexture")); // LOGISTICS_PROGRAM_COMPILER
	}

	public static TextureAtlasSprite getNewIcon(BlockType type) {
		return LogisticsSolidBlock.newTextures[getTextureIndex(type)];
	}

	public static TextureAtlasSprite getNewIcon(int index) {
		return LogisticsSolidBlock.newTextures[index];
	}

	public static int getTextureIndex(IBlockAccess access, BlockPos pos) {
		IBlockState state = access.getBlockState(pos);
		BlockType meta = state.getValue(metaProperty);
		if (meta == BlockType.SOLDERING_STATION) {
			TileEntity tile = access.getTileEntity(pos);
			if (tile instanceof IRotationProvider) {
				if (((IRotationProvider) tile).getFrontTexture() == 3) {
					return 9;
				}
			}
		}
		return LogisticsSolidBlock.getTextureIndex(meta);
	}

	public static int getTextureIndex(BlockType meta) {
		switch (meta) {
			case SOLDERING_STATION:
				return 1;
			case LOGISTICS_POWER_JUNCTION:
				return 2;
			case LOGISTICS_SECURITY_STATION:
				return 3;
			case LOGISTICS_AUTOCRAFTING_TABLE:
				return 4;
			case LOGISTICS_FUZZYCRAFTING_TABLE:
				return 5;
			case LOGISTICS_STATISTICS_TABLE:
				return 6;
			case LOGISTICS_RF_POWERPROVIDER:
				return 7;
			case LOGISTICS_IC2_POWERPROVIDER:
				return 8;
			case LOGISTICS_BC_POWERPROVIDER:
				return 10;
			case LOGISTICS_PROGRAM_COMPILER:
				return 11;
			default:
				return 0;
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		List<IProperty<?>> list = new ArrayList<>();
		list.add(metaProperty);
		list.add(rotationProperty);
		list.add(textureIndexProperty);
		list.addAll(connectionPropertys.values());
		IProperty<?>[] props = list.toArray(new IProperty<?>[list.size()]);
		return new BlockStateContainer(this, props);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(metaProperty, BlockType.getForMeta(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(metaProperty).meta;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		state = super.getActualState(state, worldIn, pos);
		TileEntity tile = worldIn.getTileEntity(pos);
		if(tile instanceof LogisticsSolidTileEntity) {
			int rotation = ((LogisticsSolidTileEntity) tile).getRotation();
			state = state.withProperty(rotationProperty, rotation);
			state = state.withProperty(textureIndexProperty, LogisticsSolidBlock.getTextureIndex(worldIn, pos));
		} else {
			state = state.withProperty(textureIndexProperty, LogisticsSolidBlock.getTextureIndex(worldIn, pos));
		}

		if(tile != null) {
			for (EnumFacing side : EnumFacing.VALUES) {
				boolean render = true;
				DoubleCoordinates newPos = CoordinateUtils.sum(new DoubleCoordinates(pos), side);
				TileEntity sideTile = newPos.getTileEntity(tile.getWorld());
				if (sideTile instanceof LogisticsTileGenericPipe) {
					LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) sideTile;
					if (tilePipe.renderState.pipeConnectionMatrix.isConnected(side.getOpposite())) {
						render = false;
					}
				}
				state = state.withProperty(connectionPropertys.get(side), render);
			}
		}

		return state;
	}
}
