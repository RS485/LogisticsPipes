package logisticspipes.pipes.basic.ltgpmodcompat;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.block.TileMultipartContainer;

import logisticspipes.LPConstants;
import logisticspipes.asm.ModDependentInterface;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.utils.ReflectionHelper;
import network.rs485.logisticspipes.proxy.mcmp.subproxy.IMCMPLTGPCompanion;

@ModDependentInterface(modId = { LPConstants.mcmpModID }, interfacePath = { "mcmultipart.api.container.IMultipartContainer" })
public abstract class LPMicroblockTileEntity extends TileEntity implements IMultipartContainer {

	public IMCMPLTGPCompanion imcmpltgpCompanion;

	public abstract boolean isMultipartAllowedInPipe();

	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public TileMultipartContainer getMultipartContainer() {
		return (TileMultipartContainer) imcmpltgpCompanion.getMCMPTileEntity();
	}

	@Nonnull
	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = super.getUpdateTag();
		if (this.getMultipartContainer() != null) tag.setTag("LogisticsPipes:MCMultiPartData", this.getMultipartContainer().getUpdateTag());
		return tag;
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
		super.handleUpdateTag(tag);
		if (tag.hasKey("LogisticsPipes:MCMultiPartData")) {
			if (this.getMultipartContainer() != null) this.getMultipartContainer().handleUpdateTag(tag.getCompoundTag("LogisticsPipes:MCMultiPartData"));
		}
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		if (this.getMultipartContainer() != null) {
			SPacketUpdateTileEntity packet = this.getMultipartContainer().getUpdatePacket();
			nbt = ReflectionHelper.getPrivateField(SPacketUpdateTileEntity.class, packet, "nbt", "field_148860_e");
		}
		SPacketUpdateTileEntity superPacket = super.getUpdatePacket();
		if (superPacket != null) {
			nbt.setTag("LogisticsPipes:SuperUpdatePacket", superPacket.getNbtCompound());
		}
		return new SPacketUpdateTileEntity(this.getPartPos(), 0, nbt);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		if (this.getMultipartContainer() != null) this.getMultipartContainer().onDataPacket(net, pkt);
		if (pkt.getNbtCompound().hasKey("LogisticsPipes:SuperUpdatePacket")) {
			super.onDataPacket(net, new SPacketUpdateTileEntity(getPos(), 1, pkt.getNbtCompound().getCompoundTag("LogisticsPipes:SuperUpdatePacket")));
		}
	}

	@Nonnull
	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag = super.writeToNBT(tag);
		NBTTagCompound multiPartTag = new NBTTagCompound();
		if (this.getMultipartContainer() != null) multiPartTag = this.getMultipartContainer().writeToNBT(multiPartTag);
		tag.setTag("MCMultiPartData", multiPartTag);
		return tag;
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		if (tag.hasKey("MCMultiPartData")) {
			if (this.getMultipartContainer() != null) this.getMultipartContainer().readFromNBT(tag.getCompoundTag("MCMultiPartData"));
		}
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void mirror(Mirror mirror) {
		super.mirror(mirror);
		if (this.getMultipartContainer() != null) this.getMultipartContainer().mirror(mirror);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void onLoad() {
		super.onLoad();
		if (this.getMultipartContainer() != null) this.getMultipartContainer().onLoad();
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void onChunkUnload() {
		super.onChunkUnload();
		if (this.getMultipartContainer() != null) this.getMultipartContainer().onChunkUnload();
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void rotate(Rotation rotation) {
		super.rotate(rotation);
		if (this.getMultipartContainer() != null) this.getMultipartContainer().rotate(rotation);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void setWorld(@Nonnull World world) {
		super.setWorld(world);
		if (this.getMultipartContainer() != null) this.getMultipartContainer().setWorld(world);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	protected void setWorldCreate(World worldIn) {
		super.setWorldCreate(worldIn);
		if (this.getMultipartContainer() != null) ReflectionHelper.invokePrivateMethod(TileMultipartContainer.class, this.getMultipartContainer(), "setWorldCreate", "func_190201_b", new Class[] { World.class }, new Object[] { worldIn });
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void invalidate() {
		super.invalidate();
		if (this.getMultipartContainer() != null) this.getMultipartContainer().invalidate();
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean shouldRenderInPass(int pass) {
		if (this.getMultipartContainer() == null) return super.shouldRenderInPass(pass);
		return this.getMultipartContainer().shouldRenderInPass(pass) || super.shouldRenderInPass(pass);
	}

	/*
	public boolean canRenderBreaking() {
		super.canRenderBreaking()
		return this.getMultipartContainer().canRenderBreaking();
	}
	*/

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void updateContainingBlockInfo() {
		super.updateContainingBlockInfo();
		if (this.getMultipartContainer() != null) this.getMultipartContainer().updateContainingBlockInfo();
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void setPos(BlockPos pos) {
		super.setPos(pos);
		if (this.getMultipartContainer() != null) this.getMultipartContainer().setPos(pos);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void validate() {
		super.validate();
		if (this.getMultipartContainer() != null) this.getMultipartContainer().validate();
	}

	// IMultipartContainer implementation below

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public Optional<IPartInfo> get(IPartSlot iPartSlot) {
		if (this.getMultipartContainer() == null) return Optional.empty();
		return getMultipartContainer().get(iPartSlot);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public Optional<IBlockState> getState(IPartSlot slot) {
		if (this.getMultipartContainer() == null) return Optional.empty();
		return getMultipartContainer().getState(slot);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public Map<IPartSlot, ? extends IPartInfo> getParts() {
		if (this.getMultipartContainer() == null) return Collections.emptyMap();
		return getMultipartContainer().getParts();
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean canAddPart(IPartSlot iPartSlot, IBlockState iBlockState, IMultipartTile iMultipartTile) {
		if (this.getMultipartContainer() == null) return false;
		if (!isMultipartAllowedInPipe()) return false;
		return getMultipartContainer().canAddPart(iPartSlot, iBlockState, iMultipartTile);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public BlockPos getPartPos() {
		if (this.getMultipartContainer() == null) return new BlockPos(0, 1, 0);
		return getMultipartContainer().getPartPos();
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean canAddPart(IPartSlot slot, IBlockState state) {
		if (this.getMultipartContainer() == null) return false;
		if (!isMultipartAllowedInPipe()) return false;
		return getMultipartContainer().canAddPart(slot, state);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public Optional<IMultipart> getPart(IPartSlot slot) {
		if (this.getMultipartContainer() == null) return Optional.empty();
		return getMultipartContainer().getPart(slot);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public World getPartWorld() {
		if (this.getMultipartContainer() == null) return this.getWorld();
		return getMultipartContainer().getPartWorld();
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public Optional<IMultipartTile> getPartTile(IPartSlot slot) {
		if (this.getMultipartContainer() == null) return Optional.empty();
		return getMultipartContainer().getPartTile(slot);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void addPart(IPartSlot iPartSlot, IBlockState iBlockState, IMultipartTile iMultipartTile) {
		if (this.getMultipartContainer() == null) return;
		if (!isMultipartAllowedInPipe()) return;
		getMultipartContainer().addPart(iPartSlot, iBlockState, iMultipartTile);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void removePart(IPartSlot iPartSlot) {
		if (this.getMultipartContainer() == null) return;
		getMultipartContainer().removePart(iPartSlot);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void notifyChange(IPartInfo part) {
		if (this.getMultipartContainer() == null) return;
		getMultipartContainer().notifyChange(part);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void addPart(IPartSlot slot, IBlockState state) {
		if (this.getMultipartContainer() == null) return;
		if (!isMultipartAllowedInPipe()) return;
		getMultipartContainer().addPart(slot, state);
	}
}
