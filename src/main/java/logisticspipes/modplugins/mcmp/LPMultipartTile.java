package logisticspipes.modplugins.mcmp;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipartTile;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

public class LPMultipartTile implements IMultipartTile {

	private final LogisticsTileGenericPipe tile;

	public LPMultipartTile(LogisticsTileGenericPipe tile) {
		this.tile = tile;
	}

	@Override
	public TileEntity getTileEntity() {
		return tile;
	}

	public boolean isTickable() {
		return false;
	}

	public ITickable getTickable() {
		return null;
	}

	public void setPartInfo(IPartInfo info) {}

	public World getPartWorld() {
		return tile.getWorld();
	}

	public void setPartWorld(World world) {}

	public boolean hasPartWorld() {
		return tile.hasWorld();
	}

	public BlockPos getPartPos() {
		return tile.getPos();
	}

	public void setPartPos(BlockPos pos) {}

	public void readPartFromNBT(NBTTagCompound compound) {}

	public NBTTagCompound writePartToNBT(NBTTagCompound compound) {
		return compound;
	}

	public void markPartDirty() {}

	public double getMaxPartRenderDistanceSquared() {
		return 0;
	}

	public boolean isPartInvalid() {
		return false;
	}

	public void invalidatePart() {}

	public void validatePart() {}

	public void updatePartContainerInfo() {}

	public void rotatePart(Rotation rotation) {}

	public void mirrorPart(Mirror mirror) {}

	public SPacketUpdateTileEntity getPartUpdatePacket() {
		return null;
	}

	public void onPartDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) { }

	public NBTTagCompound getPartUpdateTag() {
		return new NBTTagCompound();
	}

	public void handlePartUpdateTag(NBTTagCompound tag) {}

	public void onPartChunkUnload() {}

	public boolean shouldRefreshPart(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return false;
	}

	public boolean shouldRenderPartInPass(int pass) {
		return false;
	}

	public AxisAlignedBB getPartRenderBoundingBox() {
		return Block.FULL_BLOCK_AABB;
	}

	public boolean canPartRenderBreaking() {
		return false;
	}

	public void onPartLoad() {}

	public boolean hasFastPartRenderer() {
		return false;
	}

	public boolean hasPartCapability(Capability<?> capability, EnumFacing facing) {
		return false;
	}

	public <T> T getPartCapability(Capability<T> capability, EnumFacing facing) {
		return null;
	}

	public boolean onlyOpsCanSetPartNbt() {
		return false;
	}
}
