package logisticspipes.pipes.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.MainProxy;

import net.minecraft.client.Minecraft;
import network.rs485.logisticspipes.world.DoubleCoordinates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LogisticsBlockGenericSubMultiBlock extends BlockContainer {

	protected final Random rand = new Random();

	public LogisticsBlockGenericSubMultiBlock() {
		super(Material.glass);
		setCreativeTab(null);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> list = new ArrayList<>();
		return list;
	}

	@Override
	public TextureAtlasSprite getIcon(int p_149691_1_, int p_149691_2_) {
		return LogisticsPipes.LogisticsPipeBlock.getIcon(p_149691_1_, p_149691_2_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({ "all" })
	public TextureAtlasSprite getIcon(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		DoubleCoordinates pos = new DoubleCoordinates(i, j, k);
		TileEntity tile = pos.getTileEntity(iblockaccess);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipe = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			if (!mainPipe.isEmpty() && mainPipe.get(0).pipe != null && mainPipe.get(0).pipe.isMultiBlock()) {
				return LogisticsPipes.LogisticsPipeBlock.getIcon(iblockaccess, mainPipe.get(0).xCoord, mainPipe.get(0).yCoord, mainPipe.get(0).zCoord, l);
			}
		}
		return null;
	}

	public static DoubleCoordinates currentCreatedMultiBlock;

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		if (LogisticsBlockGenericSubMultiBlock.currentCreatedMultiBlock == null && MainProxy.isServer(p_149915_1_)) {
			new RuntimeException("Unknown MultiBlock controller").printStackTrace();
		}
		return new LogisticsTileGenericSubMultiBlock(LogisticsBlockGenericSubMultiBlock.currentCreatedMultiBlock);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
		DoubleCoordinates pos = new DoubleCoordinates(x, y, z);
		TileEntity tile = pos.getTileEntity(world);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			boolean handled = false;
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			for(LogisticsTileGenericPipe mainPipe:mainPipeList) {
				if (mainPipe != null && mainPipe.pipe != null && mainPipe.pipe.isMultiBlock()) {
					if (LogisticsPipes.LogisticsPipeBlock.doRayTrace(world, mainPipe.xCoord, mainPipe.yCoord, mainPipe.zCoord, Minecraft.getMinecraft().thePlayer) != null) {
						DoubleCoordinates mainPipePos = mainPipe.pipe.getLPPosition();
						mainPipePos.setBlockToAir(world);
						handled = true;
					}
				}
			}
			if(!handled) {
				mainPipeList.stream()
						.filter(mainPipe -> mainPipe != null && mainPipe.pipe != null && mainPipe.pipe.isMultiBlock())
						.forEach(mainPipe -> {
							DoubleCoordinates mainPipePos = mainPipe.pipe.getLPPosition();
							mainPipePos.setBlockToAir(world);
						});
			}
		}
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getRenderType() {
		return LPConstants.pipeModel;
	}

	@Override
	public int getRenderBlockPass() {
		return 1;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axisalignedbb, List arraylist, Entity entity) {
		DoubleCoordinates pos = new DoubleCoordinates(x, y, z);
		TileEntity tile = pos.getTileEntity(world);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			mainPipeList.stream()
					.filter(mainPipe -> mainPipe != null && mainPipe.pipe != null && mainPipe.pipe.isMultiBlock())
					.forEach(mainPipe -> LogisticsPipes.LogisticsPipeBlock.addCollisionBoxesToList(world, mainPipe.xCoord, mainPipe.yCoord, mainPipe.zCoord, axisalignedbb, arraylist, entity));
		}
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 origin, Vec3 direction) {
		DoubleCoordinates pos = new DoubleCoordinates(x, y, z);
		TileEntity tile = pos.getTileEntity(world);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			for(LogisticsTileGenericPipe mainPipe:mainPipeList) {
				if (mainPipe != null && mainPipe.pipe != null && mainPipe.pipe.isMultiBlock()) {
					MovingObjectPosition result = LogisticsPipes.LogisticsPipeBlock.collisionRayTrace(world, mainPipe.xCoord, mainPipe.yCoord, mainPipe.zCoord, origin, direction);
					if (result != null) {
						result.blockX = x;
						result.blockY = y;
						result.blockZ = z;
						return result;
					}
				}
			}
		}
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		DoubleCoordinates pos = new DoubleCoordinates(x, y, z);
		TileEntity tile = pos.getTileEntity(world);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			for(LogisticsTileGenericPipe mainPipe:mainPipeList) {
				if (mainPipe != null && mainPipe.pipe != null && mainPipe.pipe.isMultiBlock()) {
					if (LogisticsPipes.LogisticsPipeBlock.doRayTrace(world, mainPipe.xCoord, mainPipe.yCoord, mainPipe.zCoord, Minecraft.getMinecraft().thePlayer) != null) {
						return LogisticsPipes.LogisticsPipeBlock.getSelectedBoundingBoxFromPool(world, mainPipe.xCoord, mainPipe.yCoord, mainPipe.zCoord);
					}
				}
			}
		}
		return super.getSelectedBoundingBoxFromPool(world, x, y, z).expand(-0.85F, -0.85F, -0.85F);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		DoubleCoordinates pos = new DoubleCoordinates(x, y, z);
		TileEntity tile = pos.getTileEntity(world);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			((LogisticsTileGenericSubMultiBlock) tile).scheduleNeighborChange();
		}
	}

	@Override
	public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
		DoubleCoordinates pos = new DoubleCoordinates(x, y, z);
		TileEntity tile = pos.getTileEntity(world);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			for(LogisticsTileGenericPipe mainPipe:mainPipeList) {
				if (mainPipe != null && mainPipe.pipe != null && mainPipe.pipe.isMultiBlock()) {
					if (LogisticsPipes.LogisticsPipeBlock.doRayTrace(world, mainPipe.xCoord, mainPipe.yCoord, mainPipe.zCoord, Minecraft.getMinecraft().thePlayer) != null) {
						return LogisticsPipes.LogisticsPipeBlock.addDestroyEffects(world, mainPipe.xCoord, mainPipe.yCoord, mainPipe.zCoord, meta, effectRenderer);
					}
				}
			}
		}
		return super.addDestroyEffects(world, x, y, z, meta, effectRenderer);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
		DoubleCoordinates pos = new DoubleCoordinates(x, y, z);
		TileEntity tile = pos.getTileEntity(world);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			for(LogisticsTileGenericPipe mainPipe:mainPipeList) {
				if (mainPipe != null && mainPipe.pipe != null && mainPipe.pipe.isMultiBlock()) {
					if (LogisticsPipes.LogisticsPipeBlock.doRayTrace(world, mainPipe.xCoord, mainPipe.yCoord, mainPipe.zCoord, Minecraft.getMinecraft().thePlayer) != null) {
						return LogisticsPipes.LogisticsPipeBlock.getPickBlock(target, world, mainPipe.xCoord, mainPipe.yCoord, mainPipe.zCoord, player);
					}
				}
			}
		}
		return super.getPickBlock(target, world, x, y, z, player);
	}

	@Override
	@SuppressWarnings("deprecation")
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		DoubleCoordinates pos = new DoubleCoordinates(x, y, z);
		TileEntity tile = pos.getTileEntity(world);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			for(LogisticsTileGenericPipe mainPipe:mainPipeList) {
				if (mainPipe != null && mainPipe.pipe != null && mainPipe.pipe.isMultiBlock()) {
					if (LogisticsPipes.LogisticsPipeBlock.doRayTrace(world, mainPipe.xCoord, mainPipe.yCoord, mainPipe.zCoord, Minecraft.getMinecraft().thePlayer) != null) {
						return LogisticsPipes.LogisticsPipeBlock.getPickBlock(target, world, mainPipe.xCoord, mainPipe.yCoord, mainPipe.zCoord);
					}
				}
			}
			if (!mainPipeList.isEmpty() && mainPipeList.get(0).pipe != null && mainPipeList.get(0).pipe.isMultiBlock()) {
				return LogisticsPipes.LogisticsPipeBlock.getPickBlock(target, world, mainPipeList.get(0).xCoord, mainPipeList.get(0).yCoord, mainPipeList.get(0).zCoord);
			}
		}
		return super.getPickBlock(target, world, x, y, z);
	}

	@Override
	public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer) {
		int x = target.blockX;
		int y = target.blockY;
		int z = target.blockZ;
		DoubleCoordinates pos = new DoubleCoordinates(x, y, z);
		TileEntity tile = pos.getTileEntity(worldObj);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			for(LogisticsTileGenericPipe mainPipe:mainPipeList) {
				if (mainPipe != null && mainPipe.pipe != null && mainPipe.pipe.isMultiBlock()) {
					if (LogisticsPipes.LogisticsPipeBlock.doRayTrace(worldObj, mainPipe.xCoord, mainPipe.yCoord, mainPipe.zCoord, Minecraft.getMinecraft().thePlayer) != null) {
						CoreUnroutedPipe pipe = mainPipe.pipe;
						if (pipe == null) {
							return false;
						}

						TextureAtlasSprite icon = pipe.getIconProvider().getIcon(pipe.getIconIndexForItem());

						int sideHit = target.sideHit;

						Block block = LogisticsPipes.LogisticsPipeBlock;
						float b = 0.1F;
						double px = x + rand.nextDouble() * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - (b * 2.0F)) + b + block.getBlockBoundsMinX();
						double py = y + rand.nextDouble() * (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() - (b * 2.0F)) + b + block.getBlockBoundsMinY();
						double pz = z + rand.nextDouble() * (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - (b * 2.0F)) + b + block.getBlockBoundsMinZ();

						if (sideHit == 0) {
							py = y + block.getBlockBoundsMinY() - b;
						}

						if (sideHit == 1) {
							py = y + block.getBlockBoundsMaxY() + b;
						}

						if (sideHit == 2) {
							pz = z + block.getBlockBoundsMinZ() - b;
						}

						if (sideHit == 3) {
							pz = z + block.getBlockBoundsMaxZ() + b;
						}

						if (sideHit == 4) {
							px = x + block.getBlockBoundsMinX() - b;
						}

						if (sideHit == 5) {
							px = x + block.getBlockBoundsMaxX() + b;
						}

						EntityDiggingFX fx = new EntityDiggingFX(worldObj, px, py, pz, 0.0D, 0.0D, 0.0D, block, sideHit, worldObj.getBlockMetadata(x, y, z));
						fx.setParticleIcon(icon);
						effectRenderer.addEffect(fx.applyColourMultiplier(x, y, z).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
						return true;
					}
				}
			}
		}
		return super.addHitEffects(worldObj, target, effectRenderer);
	}
}
