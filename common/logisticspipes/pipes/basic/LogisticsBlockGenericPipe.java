package logisticspipes.pipes.basic;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.textures.Textures;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsBlockGenericPipe extends BlockGenericPipe {

	public LogisticsBlockGenericPipe(int i) {
		super(i);
	}

	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> result = super.getBlockDropped(world, x, y, z, metadata, fortune);
		for(int i=0;i<result.size();i++) {
			ItemStack stack = result.get(i);
			if(stack.itemID == LogisticsPipes.LogisticsBrokenItem.itemID) {
				result.remove(i);
				i--;
			}
		}
		return result;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		TileEntity tile = iblockaccess.getBlockTileEntity(i, j, k);
		if(tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe)tile).pipe instanceof PipeBlockRequestTable) {
			PipeBlockRequestTable table = (PipeBlockRequestTable) ((LogisticsTileGenericPipe)tile).pipe;
			return table.getTextureFor(l);
		}
		return super.getBlockTexture(iblockaccess, i, j, k, l);
	}
	
	@Override
	public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity) {
		TileEntity tile1 = world.getBlockTileEntity(i, j, k);
		TileGenericPipe tileG = (TileGenericPipe) tile1;
		if(tileG instanceof LogisticsTileGenericPipe && tileG.pipe instanceof PipeBlockRequestTable) {
			setBlockBounds(0, 0, 0, 1, 1, 1);
			AxisAlignedBB axisalignedbb1 = this.getCollisionBoundingBoxFromPool(world, i, j, k);
			if(axisalignedbb1 != null && axisalignedbb.intersectsWith(axisalignedbb1)) {
				arraylist.add(axisalignedbb1);
			}
			return;
		}
		super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
	}
	
	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int i, int j, int k) {
		TileEntity tile1 = world.getBlockTileEntity(i, j, k);
		TileGenericPipe tileG = (TileGenericPipe) tile1;
		if(tileG instanceof LogisticsTileGenericPipe && tileG.pipe instanceof PipeBlockRequestTable) {
			return AxisAlignedBB.getBoundingBox((double) i + 0, (double) j + 0, (double) k + 0, (double) i + 1, (double) j + 1, (double) k + 1);
		}
		return super.getSelectedBoundingBoxFromPool(world, i, j, k);
	}
	
	@Override
	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 origin, Vec3 direction) {
		TileEntity tile1 = world.getBlockTileEntity(x, y, z);
		TileGenericPipe tileG = (TileGenericPipe) tile1;
		if(tileG instanceof LogisticsTileGenericPipe && tileG.pipe instanceof PipeBlockRequestTable) {
			this.setBlockBoundsBasedOnState(world, x, y, z);
			origin = origin.addVector(( -x), ( -y), ( -z));
			direction = direction.addVector(( -x), ( -y), ( -z));
			Vec3 vec32 = origin.getIntermediateWithXValue(direction, this.minX);
			Vec3 vec33 = origin.getIntermediateWithXValue(direction, this.maxX);
			Vec3 vec34 = origin.getIntermediateWithYValue(direction, this.minY);
			Vec3 vec35 = origin.getIntermediateWithYValue(direction, this.maxY);
			Vec3 vec36 = origin.getIntermediateWithZValue(direction, this.minZ);
			Vec3 vec37 = origin.getIntermediateWithZValue(direction, this.maxZ);
			if( !this.isVecInsideYZBounds(vec32)) {
				vec32 = null;
			}
			if( !this.isVecInsideYZBounds(vec33)) {
				vec33 = null;
			}
			if( !this.isVecInsideXZBounds(vec34)) {
				vec34 = null;
			}
			if( !this.isVecInsideXZBounds(vec35)) {
				vec35 = null;
			}
			if( !this.isVecInsideXYBounds(vec36)) {
				vec36 = null;
			}
			if( !this.isVecInsideXYBounds(vec37)) {
				vec37 = null;
			}
			Vec3 vec38 = null;
			if(vec32 != null && (vec38 == null || origin.squareDistanceTo(vec32) < origin.squareDistanceTo(vec38))) {
				vec38 = vec32;
			}
			if(vec33 != null && (vec38 == null || origin.squareDistanceTo(vec33) < origin.squareDistanceTo(vec38))) {
				vec38 = vec33;
			}
			if(vec34 != null && (vec38 == null || origin.squareDistanceTo(vec34) < origin.squareDistanceTo(vec38))) {
				vec38 = vec34;
			}
			if(vec35 != null && (vec38 == null || origin.squareDistanceTo(vec35) < origin.squareDistanceTo(vec38))) {
				vec38 = vec35;
			}
			if(vec36 != null && (vec38 == null || origin.squareDistanceTo(vec36) < origin.squareDistanceTo(vec38))) {
				vec38 = vec36;
			}
			if(vec37 != null && (vec38 == null || origin.squareDistanceTo(vec37) < origin.squareDistanceTo(vec38))) {
				vec38 = vec37;
			}
			if(vec38 == null) {
				return null;
			} else {
				byte b0 = -1;
				if(vec38 == vec32) {
					b0 = 4;
				}
				if(vec38 == vec33) {
					b0 = 5;
				}
				if(vec38 == vec34) {
					b0 = 0;
				}
				if(vec38 == vec35) {
					b0 = 1;
				}
				if(vec38 == vec36) {
					b0 = 2;
				}
				if(vec38 == vec37) {
					b0 = 3;
				}
				return new MovingObjectPosition(x, y, z, b0, vec38.addVector(x, y, z));
			}
		}
		return super.collisionRayTrace(world, x, y, z, origin, direction);
	}
	
	private boolean isVecInsideYZBounds(Vec3 par1Vec3) {
		return par1Vec3 == null ? false : par1Vec3.yCoord >= this.minY && par1Vec3.yCoord <= this.maxY && par1Vec3.zCoord >= this.minZ && par1Vec3.zCoord <= this.maxZ;
	}
	
	private boolean isVecInsideXZBounds(Vec3 par1Vec3) {
		return par1Vec3 == null ? false : par1Vec3.xCoord >= this.minX && par1Vec3.xCoord <= this.maxX && par1Vec3.zCoord >= this.minZ && par1Vec3.zCoord <= this.maxZ;
	}
	
	private boolean isVecInsideXYBounds(Vec3 par1Vec3) {
		return par1Vec3 == null ? false : par1Vec3.xCoord >= this.minX && par1Vec3.xCoord <= this.maxX && par1Vec3.yCoord >= this.minY && par1Vec3.yCoord <= this.maxY;
	}

    public static Icon getRequestTableTextureFromSide(int l) {
    	ForgeDirection dir = ForgeDirection.getOrientation(l);
		switch(dir) {
			case UP:
				return Textures.LOGISTICS_REQUEST_TABLE[0];
			case DOWN:
				return Textures.LOGISTICS_REQUEST_TABLE[1];
			default:
				return Textures.LOGISTICS_REQUEST_TABLE[4];
		}
    }
}
