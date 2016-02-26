package logisticspipes.pipes.basic;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.multiblock.MultiBlockCoordinatesPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.state.PipeSubRenderState;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.TileBuffer;

import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import network.rs485.logisticspipes.world.DoubleCoordinates;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

public class LogisticsTileGenericSubMultiBlock extends TileEntity implements IPipeInformationProvider {

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
		if(pos != null) {
			mainPipePos.add(pos);
		}
		mainPipe = null;
		renderState = new PipeSubRenderState();
	}

	public List<LogisticsTileGenericPipe> getMainPipe() {
		if (mainPipe == null) {
			mainPipe = new ArrayList<>();
			for(DoubleCoordinates pos:mainPipePos) {
				TileEntity tile = pos.getTileEntity(getWorldObj());
				if (tile instanceof LogisticsTileGenericPipe) {
					mainPipe.add((LogisticsTileGenericPipe) tile);
				}
			}
			mainPipe = Collections.unmodifiableList(mainPipe);
		}
		boolean allInvalid = true;
		for(LogisticsTileGenericPipe pipe:mainPipe) {
			if(!pipe.isInvalid()) {
				allInvalid = false;
				break;
			}
		}
		if (mainPipe.isEmpty() || allInvalid) {
			getWorldObj().setBlockToAir(xCoord, yCoord, zCoord);
		}
		if(mainPipe != null) {
			return mainPipe;
		}
		return Collections.EMPTY_LIST;
	}

	public List<CoreMultiBlockPipe.SubBlockTypeForShare> getSubTypes() {
		return Collections.unmodifiableList(subTypes);
	}

	@Override
	public void updateEntity() {
		if (MainProxy.isClient(getWorldObj())) {
			return;
		}
		List<LogisticsTileGenericPipe> pipes = getMainPipe();
		for (LogisticsTileGenericPipe pipe:pipes) {
			pipe.subMultiBlock.add(new DoubleCoordinates((TileEntity) this));
		}
	}

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(nbt.hasKey("MainPipePos_xPos")) {
			mainPipePos.clear();
			DoubleCoordinates pos = DoubleCoordinates.readFromNBT("MainPipePos_", nbt);
			if(pos != null) {
				mainPipePos.add(pos);
			}
		}
		if(nbt.hasKey("MainPipePosList")) {
			NBTTagList list = nbt.getTagList("MainPipePosList", new NBTTagCompound().getId());
			for(int i=0; i< list.tagCount();i++) {
				DoubleCoordinates pos = DoubleCoordinates.readFromNBT("MainPipePos_", list.getCompoundTagAt(i));
				if(pos != null) {
					mainPipePos.add(pos);
				}
			}
		}
		if(nbt.hasKey("SubTypeList")) {
			NBTTagList list = nbt.getTagList("SubTypeList", new NBTTagString().getId());
			subTypes.clear();
			for(int i=0; i< list.tagCount();i++) {
				String name = list.getStringTagAt(i);
				CoreMultiBlockPipe.SubBlockTypeForShare type = CoreMultiBlockPipe.SubBlockTypeForShare.valueOf(name);
				if(type != null) {
					subTypes.add(type);
				}
			}
		}
		mainPipe = null;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		NBTTagList nbtList = new NBTTagList();
		for(DoubleCoordinates pos: mainPipePos) {
			NBTTagCompound compound = new NBTTagCompound();
			pos.writeToNBT("MainPipePos_", compound);
			nbtList.appendTag(compound);
		}
		nbt.setTag("MainPipePosList", nbtList);
		NBTTagList nbtTypeList = new NBTTagList();
		for(CoreMultiBlockPipe.SubBlockTypeForShare type: subTypes) {
			nbtTypeList.appendTag(new NBTTagString(type.name()));
		}
		nbt.setTag("SubTypeList", nbtTypeList);
	}

	@Override
	public Packet getDescriptionPacket() {
		try {
			return PacketHandler.toFMLPacket(getLPDescriptionPacket());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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

	@Override
	public boolean isCorrect(ConnectionPipeType type) {
		return getMainPipe().size() == 1;
	}

	@Override
	public int getX() {
		return xCoord;
	}

	@Override
	public int getY() {
		return yCoord;
	}

	@Override
	public int getZ() {
		return zCoord;
	}

	@Override
	public World getWorld() {
		return getWorldObj();
	}

	@Override
	public boolean isRouterInitialized() {
		return true;
	}

	@Override
	public boolean isRoutingPipe() {
		return false;
	}

	@Override
	public CoreRoutedPipe getRoutingPipe() {
		return null;
	}

	@Override
	public TileEntity getNextConnectedTile(ForgeDirection to) {
		CoreUnroutedPipe pipe = this.getMainPipe().get(0).pipe;
		if(pipe instanceof CoreMultiBlockPipe) {
			return ((CoreMultiBlockPipe)pipe).getConnectedEndTile(to);
		}
		return null;
	}

	@Override
	public boolean isFirewallPipe() {
		return false;
	}

	@Override
	public IFilter getFirewallFilter() {
		return null;
	}

	public TileEntity getTile() {
		return this;
	}

	@Override
	public boolean divideNetwork() {
		return false;
	}

	@Override
	public boolean powerOnly() {
		return false;
	}

	@Override
	public boolean isOnewayPipe() {
		return false;
	}

	@Override
	public boolean isOutputOpen(ForgeDirection direction) {
		return true;
	}

	@Override
	public boolean canConnect(TileEntity to, ForgeDirection direction, boolean flag) {
		return getNextConnectedTile(direction) != null;
	}

	@Override
	public double getDistance() {
		CoreUnroutedPipe pipe = this.getMainPipe().get(0).pipe;
		if(pipe instanceof CoreMultiBlockPipe) {
			return ((CoreMultiBlockPipe)pipe).getPipeLength();
		}
		return 0;
	}

	@Override
	public boolean isItemPipe() {
		return true;
	}

	@Override
	public boolean isFluidPipe() {
		return false;
	}

	@Override
	public boolean isPowerPipe() {
		return false;
	}

	@Override
	public double getDistanceTo(int destinationint, ForgeDirection ignore, ItemIdentifier ident, boolean isActive, double traveled, double max, List<DoubleCoordinates> visited) {
		if (traveled >= max) {
			return Integer.MAX_VALUE;
		}
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (ignore == dir) {
				continue;
			}
			IPipeInformationProvider information = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(getNextConnectedTile(dir));
			if (information != null) {
				DoubleCoordinates pos = new DoubleCoordinates(information);
				if (visited.contains(pos)) {
					continue;
				}
				visited.add(pos);
				double result = information.getDistanceTo(destinationint, dir.getOpposite(), ident, isActive, traveled + getDistance(), max, visited);
				visited.remove(pos);
				if (result == Integer.MAX_VALUE) {
					return result;
				}
				return result + (int) getDistance();
			}
		}
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean acceptItem(LPTravelingItem item, TileEntity from) {
		CoreUnroutedPipe pipe = this.getMainPipe().get(0).pipe;
		if(pipe instanceof CoreMultiBlockPipe) {
			return ((CoreMultiBlockPipe)pipe).transport.injectItem(item, OrientationsUtil.getOrientationOfTilewithTile(this, from).getOpposite()) == 0;
		}
		return false;
	}

	@Override
	public void refreshTileCacheOnSide(ForgeDirection side) {

	}

	public TileEntity getTile(ForgeDirection to) {
		return getTile(to, false);
	}

	public TileEntity getTile(ForgeDirection to, boolean force) {
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

	public Block getBlock(ForgeDirection to) {
		TileBuffer[] cache = getTileCache();
		if (cache != null) {
			return cache[to.ordinal()].getBlock();
		} else {
			return null;
		}
	}

	public TileBuffer[] getTileCache() {
		if (tileBuffer == null) {
			tileBuffer = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord, true);
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
		subTypes.add(type);
	}

	public void addMultiBlockMainPos(DoubleCoordinates placeAt) {
		if(mainPipePos.add(placeAt)) {
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
}
