package logisticspipes.pipes.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.multiblock.MultiBlockCoordinatesPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.renderer.state.PipeSubRenderState;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.routing.pathfinder.ISubMultiBlockPipeInformationProvider;
import logisticspipes.utils.TileBuffer;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class LogisticsTileGenericSubMultiBlock extends TileEntity implements ISubMultiBlockPipeInformationProvider, ITickable {

	private Set<DoubleCoordinates> mainPipePos = new HashSet<>();
	private List<LogisticsTileGenericPipe> mainPipe;
	private List<CoreMultiBlockPipe.SubBlockTypeForShare> subTypes = new ArrayList<>();
	private TileBuffer[] tileBuffer;
	public final PipeSubRenderState renderState;

	@Deprecated
	public LogisticsTileGenericSubMultiBlock() {
		renderState = new PipeSubRenderState();
	}

	public LogisticsTileGenericSubMultiBlock(DoubleCoordinates pos) {
		if (pos != null) {
			mainPipePos.add(pos);
		}
		mainPipe = null;
		renderState = new PipeSubRenderState();
	}

	@Override
	public void setPos(BlockPos posIn) {
		super.setPos(posIn);
		if (MainProxy.isClient()) {
			System.out.println("Multi Pipe Created at: " + posIn);
		}
	}

	public List<LogisticsTileGenericPipe> getMainPipe() {
		if (mainPipe == null) {
			mainPipe = new ArrayList<>();
			for (DoubleCoordinates pos : mainPipePos) {
				TileEntity tile = pos.getTileEntity(getWorld());
				if (tile instanceof LogisticsTileGenericPipe) {
					mainPipe.add((LogisticsTileGenericPipe) tile);
				}
			}
			mainPipe = Collections.unmodifiableList(mainPipe);
		}
		if (MainProxy.isServer(world)) {
			boolean allInvalid = true;
			for (LogisticsTileGenericPipe pipe : mainPipe) {
				if (!pipe.isInvalid()) {
					allInvalid = false;
					break;
				}
			}
			if (mainPipe.isEmpty() || allInvalid) {
				getWorld().setBlockToAir(getPos());
			}
		}
		if (mainPipe != null) {
			return mainPipe;
		}
		return Collections.emptyList();
	}

	public List<CoreMultiBlockPipe.SubBlockTypeForShare> getSubTypes() {
		return Collections.unmodifiableList(subTypes);
	}

	@Override
	public void update() {
		if (MainProxy.isClient(getWorld())) {
			return;
		}
		List<LogisticsTileGenericPipe> pipes = getMainPipe();
		for (LogisticsTileGenericPipe pipe : pipes) {
			pipe.subMultiBlock.add(new DoubleCoordinates(this));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if (nbt.hasKey("MainPipePos_xPos")) {
			mainPipePos.clear();
			DoubleCoordinates pos = DoubleCoordinates.readFromNBT("MainPipePos_", nbt);
			if (pos != null) {
				mainPipePos.add(pos);
			}
		}
		if (nbt.hasKey("MainPipePosList")) {
			NBTTagList list = nbt.getTagList("MainPipePosList", new NBTTagCompound().getId());
			for (int i = 0; i < list.tagCount(); i++) {
				DoubleCoordinates pos = DoubleCoordinates.readFromNBT("MainPipePos_", list.getCompoundTagAt(i));
				if (pos != null) {
					mainPipePos.add(pos);
				}
			}
		}
		if (nbt.hasKey("SubTypeList")) {
			NBTTagList list = nbt.getTagList("SubTypeList", new NBTTagString().getId());
			subTypes.clear();
			for (int i = 0; i < list.tagCount(); i++) {
				String name = list.getStringTagAt(i);
				CoreMultiBlockPipe.SubBlockTypeForShare type = CoreMultiBlockPipe.SubBlockTypeForShare.valueOf(name);
				if (type != null) {
					subTypes.add(type);
				}
			}
		}
		mainPipe = null;
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		NBTTagList nbtList = new NBTTagList();
		for (DoubleCoordinates pos : mainPipePos) {
			NBTTagCompound compound = new NBTTagCompound();
			pos.writeToNBT("MainPipePos_", compound);
			nbtList.appendTag(compound);
		}
		nbt.setTag("MainPipePosList", nbtList);
		NBTTagList nbtTypeList = new NBTTagList();
		for (CoreMultiBlockPipe.SubBlockTypeForShare type : subTypes) {
			if (type == null) continue;
			nbtTypeList.appendTag(new NBTTagString(type.name()));
		}
		nbt.setTag("SubTypeList", nbtTypeList);
		return nbt;
	}

	@Nonnull
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = super.getUpdateTag();
		try {
			PacketHandler.addPacketToNBT(getLPDescriptionPacket(), nbt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nbt;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
		PacketHandler.queueAndRemovePacketFromNBT(tag);
		super.handleUpdateTag(tag);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		try {
			PacketHandler.addPacketToNBT(getLPDescriptionPacket(), nbt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SPacketUpdateTileEntity(getPos(), 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		PacketHandler.queueAndRemovePacketFromNBT(packet.getNbtCompound());
	}

	public ModernPacket getLPDescriptionPacket() {
		MultiBlockCoordinatesPacket packet = PacketHandler.getPacket(MultiBlockCoordinatesPacket.class);
		packet.setTilePos(this);
		packet.setTargetPos(mainPipePos);
		packet.setSubTypes(subTypes);
		return packet;
	}

	public void setPosition(Set<DoubleCoordinates> lpPosition, List<CoreMultiBlockPipe.SubBlockTypeForShare> subTypes) {
		mainPipePos = lpPosition;
		this.subTypes = subTypes;
		mainPipe = null;
	}

	public TileEntity getTile() {
		return this;
	}

	public TileEntity getTile(EnumFacing to) {
		return getTile(to, false);
	}

	public TileEntity getTile(EnumFacing to, boolean force) {
		TileBuffer[] cache = getTileCache();
		if (cache != null) {
			if (force) {
				cache[to.ordinal()].refresh();
			}
			return cache[to.ordinal()].getTile();
		} else {
			return null;
		}
	}

	public Block getBlock(EnumFacing to) {
		TileBuffer[] cache = getTileCache();
		if (cache != null) {
			return cache[to.ordinal()].getBlock();
		} else {
			return null;
		}
	}

	public TileBuffer[] getTileCache() {
		if (tileBuffer == null) {
			tileBuffer = TileBuffer.makeBuffer(world, getPos(), true);
		}
		return tileBuffer;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		tileBuffer = null;
	}

	@Override
	public void validate() {
		super.validate();
		tileBuffer = null;
	}

	public void scheduleNeighborChange() {
		tileBuffer = null;
	}

	public void addSubTypeTo(CoreMultiBlockPipe.SubBlockTypeForShare type) {
		if (type == null) throw new NullPointerException();
		subTypes.add(type);
	}

	public void addMultiBlockMainPos(DoubleCoordinates placeAt) {
		if (mainPipePos.add(placeAt)) {
			mainPipe = null;
		}
	}

	public boolean removeMainPipe(DoubleCoordinates doubleCoordinates) {
		mainPipePos.remove(doubleCoordinates);
		return mainPipePos.isEmpty();
	}

	public void removeSubType(CoreMultiBlockPipe.SubBlockTypeForShare type) {
		subTypes.remove(type);
	}

	@Override
	public IPipeInformationProvider getMainTile() {
		List<LogisticsTileGenericPipe> mainTiles = this.getMainPipe();
		if (mainTiles.size() != 1) {
			return null;
		}
		return mainTiles.get(0);
	}
}
