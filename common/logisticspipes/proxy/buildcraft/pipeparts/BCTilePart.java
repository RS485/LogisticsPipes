package logisticspipes.proxy.buildcraft.pipeparts;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.transport.PipeWire;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.WireIconProvider;

public class BCTilePart implements IBCTilePart {
	
	public LogisticsTileGenericPipe pipe;
	
	public BCTilePart(LogisticsTileGenericPipe tile) {
		this.pipe = tile;
	}

	static class SideProperties {
		int[] facadeTypes = new int[ForgeDirection.VALID_DIRECTIONS.length];
		int[] facadeWires = new int[ForgeDirection.VALID_DIRECTIONS.length];

		Block[][] facadeBlocks = new Block[ForgeDirection.VALID_DIRECTIONS.length][2];
		int[][] facadeMeta = new int[ForgeDirection.VALID_DIRECTIONS.length][2];

		boolean[] plugs = new boolean[ForgeDirection.VALID_DIRECTIONS.length];
		boolean[] robotStations = new boolean[ForgeDirection.VALID_DIRECTIONS.length];

		public void writeToNBT (NBTTagCompound nbt) {
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				nbt.setInteger("facadeTypes[" + i + "]", facadeTypes[i]);
				nbt.setInteger("facadeWires[" + i + "]", facadeWires[i]);

				if (facadeBlocks[i][0] != null) {
					nbt.setString("facadeBlocksStr[" + i + "][0]",
							Block.blockRegistry.getNameForObject(facadeBlocks[i][0]));
				} else {
					// remove tag is useful in case we're overwritting an NBT
					// already set, for example in a blueprint.
					nbt.removeTag("facadeBlocksStr[" + i + "][0]");
				}

				if (facadeBlocks[i][1] != null) {
					nbt.setString("facadeBlocksStr[" + i + "][1]",
							Block.blockRegistry.getNameForObject(facadeBlocks[i][1]));
				} else {
					// remove tag is useful in case we're overwritting an NBT
					// already set, for example in a blueprint.
					nbt.removeTag("facadeBlocksStr[" + i + "][1]");
				}

				nbt.setInteger("facadeMeta[" + i + "][0]", facadeMeta[i][0]);
				nbt.setInteger("facadeMeta[" + i + "][1]", facadeMeta[i][1]);

				nbt.setBoolean("plug[" + i + "]", plugs[i]);
				nbt.setBoolean("robotStation[" + i + "]", robotStations[i]);
			}
		}

		public void readFromNBT (NBTTagCompound nbt) {
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				facadeTypes[i] = nbt.getInteger("facadeTypes[" + i + "]");
				facadeWires[i] = nbt.getInteger("facadeWires[" + i + "]");

				if (nbt.hasKey("facadeBlocks[" + i + "]")) {
					// In this case, we're on legacy pre-6.0 facade loading
					// mode.
					int blockId = nbt.getInteger("facadeBlocks[" + i + "]");

					if (blockId != 0) {
						facadeBlocks[i][0] = (Block) Block.blockRegistry.getObjectById(blockId);
					} else {
						facadeBlocks[i][0] = null;
					}

					facadeBlocks[i][1] = null;

					facadeMeta[i][0] = nbt.getInteger("facadeMeta[" + i + "]");
					facadeMeta[i][1] = 0;
				} else {
					if (nbt.hasKey("facadeBlocksStr[" + i + "][0]")) {
						facadeBlocks[i][0] = (Block) Block.blockRegistry.getObject
								(nbt.getString("facadeBlocksStr[" + i + "][0]"));
					} else {
						facadeBlocks[i][0] = null;
					}

					if (nbt.hasKey("facadeBlocksStr[" + i + "][1]")) {
						facadeBlocks[i][1] = (Block) Block.blockRegistry.getObject
								(nbt.getString("facadeBlocksStr[" + i + "][1]"));
					} else {
						facadeBlocks[i][1] = null;
					}

					facadeMeta[i][0] = nbt.getInteger("facadeMeta[" + i + "][0]");
					facadeMeta[i][1] = nbt.getInteger("facadeMeta[" + i + "][1]");
				}

				plugs[i] = nbt.getBoolean("plug[" + i + "]");
				robotStations[i] = nbt.getBoolean("robotStation[" + i + "]");
			}
		}

		public void rotateLeft() {
			int[] newFacadeTypes = new int[ForgeDirection.VALID_DIRECTIONS.length];
			int[] newFacadeWires = new int[ForgeDirection.VALID_DIRECTIONS.length];

			Block[][] newFacadeBlocks = new Block[ForgeDirection.VALID_DIRECTIONS.length][2];
			int[][] newFacadeMeta = new int[ForgeDirection.VALID_DIRECTIONS.length][2];

			boolean[] newPlugs = new boolean[ForgeDirection.VALID_DIRECTIONS.length];
			boolean[] newRobotStations = new boolean[ForgeDirection.VALID_DIRECTIONS.length];

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				ForgeDirection r = dir.getRotation(ForgeDirection.UP);

				newFacadeTypes[r.ordinal()] = facadeTypes[dir.ordinal()];
				newFacadeWires[r.ordinal()] = facadeWires[dir.ordinal()];
				newFacadeBlocks[r.ordinal()][0] = facadeBlocks[dir.ordinal()][0];
				newFacadeBlocks[r.ordinal()][1] = facadeBlocks[dir.ordinal()][1];
				newFacadeMeta[r.ordinal()][0] = facadeMeta[dir.ordinal()][0];
				newFacadeMeta[r.ordinal()][1] = facadeMeta[dir.ordinal()][1];
				newPlugs[r.ordinal()] = plugs[dir.ordinal()];
				newRobotStations[r.ordinal()] = robotStations[dir.ordinal()];
			}

			facadeTypes = newFacadeTypes;
			facadeWires = newFacadeWires;
			facadeBlocks = newFacadeBlocks;
			facadeMeta = newFacadeMeta;
			plugs = newPlugs;
			robotStations = newRobotStations;
		}
	}

	private SideProperties sideProperties = new SideProperties();

	@Override
	public void refreshRenderState() {

		// WireState
		for (PipeWire color : PipeWire.values()) {
			pipe.renderState.wireMatrix.setWire(color, pipe.pipe.bcPipePart.getWireSet()[color.ordinal()]);

			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				pipe.renderState.wireMatrix.setWireConnected(color, direction, pipe.pipe.bcPipePart.isWireConnectedTo(pipe.getTile(direction), color));
			}

			boolean lit = pipe.pipe.bcPipePart.getSignalStrength()[color.ordinal()] > 0;

			switch (color) {
				case RED:
					pipe.renderState.wireMatrix.setWireIndex(color, lit ? WireIconProvider.Texture_Red_Lit : WireIconProvider.Texture_Red_Dark);
					break;
				case BLUE:
					pipe.renderState.wireMatrix.setWireIndex(color, lit ? WireIconProvider.Texture_Blue_Lit : WireIconProvider.Texture_Blue_Dark);
					break;
				case GREEN:
					pipe.renderState.wireMatrix.setWireIndex(color, lit ? WireIconProvider.Texture_Green_Lit : WireIconProvider.Texture_Green_Dark);
					break;
				case YELLOW:
					pipe.renderState.wireMatrix.setWireIndex(color, lit ? WireIconProvider.Texture_Yellow_Lit : WireIconProvider.Texture_Yellow_Dark);
					break;
				default:
					break;

			}
		}

		// Gate Textures and movement
		pipe.renderState.setIsGateLit(pipe.pipe.bcPipePart.isGateActive());
		pipe.renderState.setIsGatePulsing(pipe.pipe.bcPipePart.isGateActive());

		// Facades
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			int type = sideProperties.facadeTypes[direction.ordinal()];

			if (type == ItemFacade.TYPE_BASIC) {
				Block block = sideProperties.facadeBlocks[direction.ordinal()][0];
				pipe.renderState.facadeMatrix.setFacade(direction, block, sideProperties.facadeMeta[direction.ordinal()][0]);
			} else if (type == ItemFacade.TYPE_PHASED) {
				PipeWire wire = PipeWire.fromOrdinal(sideProperties.facadeWires[direction.ordinal()]);
				Block block = sideProperties.facadeBlocks[direction.ordinal()][0];
				Block blockAlt = sideProperties.facadeBlocks[direction.ordinal()][1];
				int meta = sideProperties.facadeMeta[direction.ordinal()][0];
				int metaAlt = sideProperties.facadeMeta[direction.ordinal()][1];

				if (pipe.isWireActive(wire)) {
					pipe.renderState.facadeMatrix.setFacade(direction, blockAlt, metaAlt);
				} else {
					pipe.renderState.facadeMatrix.setFacade(direction, block, meta);
				}
			}
		}

		//Plugs
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			pipe.renderState.plugMatrix.setConnected(direction, sideProperties.plugs[direction.ordinal()]);
		}

		//RobotStations
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			pipe.renderState.robotStationMatrix.setConnected(direction, sideProperties.robotStations[direction.ordinal()]);
		}
	}

	@Override
	public boolean addFacade(ForgeDirection direction, int type, int wire, Block[] blocks, int[] metaValues) {
		if (pipe.getWorldObj().isRemote) {
			return false;
		}

		if (hasFacade(direction)) {
			dropFacadeItem(direction);
		}

		sideProperties.facadeTypes[direction.ordinal()] = type;

		if (type == ItemFacade.TYPE_BASIC || wire == -1) {
			sideProperties.facadeBlocks[direction.ordinal()][0] = blocks[0];
			sideProperties.facadeMeta[direction.ordinal()][0] = metaValues[0];
		} else {
			sideProperties.facadeWires[direction.ordinal()] = wire;
			sideProperties.facadeBlocks[direction.ordinal()][0] = blocks[0];
			sideProperties.facadeMeta[direction.ordinal()][0] = metaValues[0];
			sideProperties.facadeBlocks[direction.ordinal()][1] = blocks[1];
			sideProperties.facadeMeta[direction.ordinal()][1] = metaValues[1];
		}

		pipe.getWorldObj().notifyBlockChange(pipe.xCoord, pipe.yCoord, pipe.zCoord, pipe.getBlock());

		pipe.scheduleRenderUpdate();

		return true;
	}

	@Override
	public boolean hasFacade(ForgeDirection direction) {
		if (direction == null || direction == ForgeDirection.UNKNOWN) {
			return false;
		} else if (pipe.getWorldObj().isRemote) {
			return pipe.renderState.facadeMatrix.getFacadeBlock(direction) != null;
		} else {
			return sideProperties.facadeBlocks[direction.ordinal()][0] != null;
		}
	}

	@Override
	public void dropFacadeItem(ForgeDirection direction) {
		MainProxy.dropItems(pipe.getWorldObj(), getFacade(direction), pipe.xCoord, pipe.yCoord, pipe.zCoord);
	}

	@Override
	public ItemStack getFacade(ForgeDirection direction) {
		int type = sideProperties.facadeTypes[direction.ordinal()];

		if (type == ItemFacade.TYPE_BASIC) {
			return ItemFacade.getFacade(sideProperties.facadeBlocks[direction.ordinal()][0], sideProperties.facadeMeta[direction.ordinal()][0]);
		} else {
			return ItemFacade.getAdvancedFacade(PipeWire.fromOrdinal(sideProperties.facadeWires[direction.ordinal()]), sideProperties.facadeBlocks[direction.ordinal()][0], sideProperties.facadeMeta[direction.ordinal()][0], sideProperties.facadeBlocks[direction.ordinal()][1], sideProperties.facadeMeta[direction.ordinal()][1]);
		}
	}

	@Override
	public boolean dropFacade(ForgeDirection direction) {
		if (!hasFacade(direction)) {
			return false;
		}

		if (!pipe.getWorldObj().isRemote) {
			dropFacadeItem(direction);
			sideProperties.facadeTypes[direction.ordinal()] = 0;
			sideProperties.facadeWires[direction.ordinal()] = -1;
			sideProperties.facadeBlocks[direction.ordinal()][0] = null;
			sideProperties.facadeMeta[direction.ordinal()][0] = 0;
			sideProperties.facadeBlocks[direction.ordinal()][1] = null;
			sideProperties.facadeMeta[direction.ordinal()][1] = 0;
			pipe.getWorldObj().notifyBlockChange(pipe.xCoord, pipe.yCoord, pipe.zCoord, pipe.getBlock());
			pipe.scheduleRenderUpdate();
		}

		return true;
	}

	@Override
	public boolean hasPlug(ForgeDirection side) {
		if (side == null || side == ForgeDirection.UNKNOWN) {
			return false;
		}

		if (pipe.getWorldObj().isRemote) {
			return pipe.renderState.plugMatrix.isConnected(side);
		}

		return sideProperties.plugs[side.ordinal()];
	}

	@Override
	public boolean hasRobotStation(ForgeDirection side) {
		if (side == null || side == ForgeDirection.UNKNOWN) {
			return false;
		}

		if (pipe.getWorldObj().isRemote) {
			return pipe.renderState.robotStationMatrix.isConnected(side);
		}

		return sideProperties.robotStations[side.ordinal()];
	}

	@Override
	public boolean removeAndDropPlug(ForgeDirection side) {
		if (!hasPlug(side)) {
			return false;
		}

		if (!pipe.getWorldObj().isRemote) {
			sideProperties.plugs[side.ordinal()] = false;
			MainProxy.dropItems(pipe.getWorldObj(), SimpleServiceLocator.buildCraftProxy.getPipePlugItemStack(), pipe.xCoord, pipe.yCoord, pipe.zCoord);
			pipe.getWorldObj().notifyBlockChange(pipe.xCoord, pipe.yCoord, pipe.zCoord, pipe.getBlock());
			pipe.scheduleNeighborChange(); //To force recalculation of connections
			pipe.scheduleRenderUpdate();
		}

		return true;
	}

	@Override
	public boolean removeAndDropRobotStation(ForgeDirection side) {
		if (!hasRobotStation(side)) {
			return false;
		}

		if (!pipe.getWorldObj().isRemote) {
			sideProperties.robotStations[side.ordinal()] = false;
			MainProxy.dropItems(pipe.getWorldObj(), SimpleServiceLocator.buildCraftProxy.getRobotStationItemStack(), pipe.xCoord, pipe.yCoord, pipe.zCoord);
			pipe.getWorldObj().notifyBlockChange(pipe.xCoord, pipe.yCoord, pipe.zCoord, pipe.getBlock());
			pipe.scheduleNeighborChange(); //To force recalculation of connections
			pipe.scheduleRenderUpdate();
		}

		return true;
	}

	@Override
	public boolean addPlug(ForgeDirection forgeDirection) {
		if (hasPlug(forgeDirection)) {
			return false;
		}

		sideProperties.plugs[forgeDirection.ordinal()] = true;
		pipe.getWorldObj().notifyBlockChange(pipe.xCoord, pipe.yCoord, pipe.zCoord, pipe.getBlock());
		pipe.scheduleNeighborChange(); //To force recalculation of connections
		pipe.scheduleRenderUpdate();
		return true;
	}

	@Override
	public boolean addRobotStation(ForgeDirection forgeDirection) {
		if (hasRobotStation(forgeDirection)) {
			return false;
		}

		sideProperties.robotStations[forgeDirection.ordinal()] = true;
		pipe.getWorldObj().notifyBlockChange(pipe.xCoord, pipe.yCoord, pipe.zCoord, pipe.getBlock());
		pipe.scheduleNeighborChange(); //To force recalculation of connections
		pipe.scheduleRenderUpdate();
		return true;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		sideProperties.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		sideProperties.readFromNBT(nbt);
	}
}
