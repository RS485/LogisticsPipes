package logisticspipes.proxy.buildcraft.bc61.subproxies;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.bc61.gates.wrapperclasses.PipeWrapper;
import logisticspipes.proxy.buildcraft.subproxies.IBCTilePart;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.transport.IPipePluggable;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.utils.BCLog;
import buildcraft.transport.Gate;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.ItemPlug;
import buildcraft.transport.ItemRobotStation;
import buildcraft.transport.WireIconProvider;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.utils.RobotStationState;

public class BCTilePart implements IBCTilePart {
	
	public final LogisticsTileGenericPipe pipe;
	private final BCRenderState bcRenderState;
	private boolean attachPluggables = false;
	
	public BCTilePart(LogisticsTileGenericPipe tile) {
		this.pipe = tile;
		bcRenderState = (BCRenderState) tile.renderState.bcRenderState.getOriginal();
	}

	public static class SideProperties {
		IPipePluggable[] pluggables = new IPipePluggable[ForgeDirection.VALID_DIRECTIONS.length];

		public void writeToNBT(NBTTagCompound nbt) {
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				IPipePluggable pluggable = pluggables[i];
				final String key = "pluggable[" + i + "]";
				if (pluggable == null) {
					nbt.removeTag(key);
				} else {
					NBTTagCompound pluggableData = new NBTTagCompound();
					pluggableData.setString("pluggableClass", pluggable.getClass().getName());
					pluggable.writeToNBT(pluggableData);
					nbt.setTag(key, pluggableData);
				}
			}
		}

		public void readFromNBT(NBTTagCompound nbt) {
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				final String key = "pluggable[" + i + "]";
				if (!nbt.hasKey(key)) {
					continue;
				}
				try {
					NBTTagCompound pluggableData = nbt.getCompoundTag(key);
					Class<?> pluggableClass = Class.forName(pluggableData.getString("pluggableClass"));
					if (!IPipePluggable.class.isAssignableFrom(pluggableClass)) {
						BCLog.logger.warning("Wrong pluggable class: " + pluggableClass);
						continue;
					}
					IPipePluggable pluggable = (IPipePluggable) pluggableClass.newInstance();
					pluggable.readFromNBT(pluggableData);
					pluggables[i] = pluggable;
				} catch (Exception e) {
					BCLog.logger.warning("Failed to load side state");
					e.printStackTrace();
				}
			}

			// Migration code
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				IPipePluggable pluggable = null;
				if (nbt.hasKey("facadeState[" + i + "]")) {
					pluggable = new ItemFacade.FacadePluggable(ItemFacade.FacadeState.readArray(nbt.getTagList("facadeState[" + i + "]", Constants.NBT.TAG_COMPOUND)));
				} else {
					// Migration support for 5.0.x and 6.0.x
					if (nbt.hasKey("facadeBlocks[" + i + "]")) {
						// 5.0.x
						Block block = (Block) Block.blockRegistry.getObjectById(nbt.getInteger("facadeBlocks[" + i + "]"));
						int blockId = nbt.getInteger("facadeBlocks[" + i + "]");

						if (blockId != 0) {
							int metadata = nbt.getInteger("facadeMeta[" + i + "]");
							pluggable = new ItemFacade.FacadePluggable(new ItemFacade.FacadeState[]{ItemFacade.FacadeState.create(block, metadata)});
						}
					} else if (nbt.hasKey("facadeBlocksStr[" + i + "][0]")) {
						// 6.0.x
						ItemFacade.FacadeState mainState = ItemFacade.FacadeState.create(
								(Block) Block.blockRegistry.getObject(nbt.getString("facadeBlocksStr[" + i + "][0]")),
								nbt.getInteger("facadeMeta[" + i + "][0]")
						);
						if (nbt.hasKey("facadeBlocksStr[" + i + "][1]")) {
							ItemFacade.FacadeState phasedState = ItemFacade.FacadeState.create(
									(Block) Block.blockRegistry.getObject(nbt.getString("facadeBlocksStr[" + i + "][1]")),
									nbt.getInteger("facadeMeta[" + i + "][1]"),
									PipeWire.fromOrdinal(nbt.getInteger("facadeWires[" + i + "]"))
							);
							pluggable = new ItemFacade.FacadePluggable(new ItemFacade.FacadeState[]{mainState, phasedState});
						} else {
							pluggable = new ItemFacade.FacadePluggable(new ItemFacade.FacadeState[]{mainState});
						}
					}
				}

				if (nbt.getBoolean("plug[" + i + "]")) {
					pluggable = new ItemPlug.PlugPluggable();
				}
				if (nbt.getBoolean("robotStation[" + i + "]")) {
					pluggable = new ItemRobotStation.RobotStationPluggable();
				}

				if (pluggable != null) {
					pluggables[i] = pluggable;
				}
			}
		}

		public void rotateLeft() {
			IPipePluggable[] newPluggables = new IPipePluggable[ForgeDirection.VALID_DIRECTIONS.length];
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				newPluggables[dir.getRotation(ForgeDirection.UP).ordinal()] = pluggables[dir.ordinal()];
			}
			pluggables = newPluggables;
		}

		public boolean dropItem(LogisticsTileGenericPipe pipe, ForgeDirection direction) {
			boolean result = false;
			IPipePluggable pluggable = pluggables[direction.ordinal()];
			if (pluggable != null) {
				pluggable.onDetachedPipe(((PipeWrapper)pipe.pipe.bcPipePart.getWrapped()).getTile(), direction);
				ItemStack[] stacks = pluggable.getDropItems(pipe);
				if (stacks != null) {
					for (ItemStack stack : stacks) {
						InvUtils.dropItems(pipe.getWorldObj(), stack, pipe.xCoord, pipe.yCoord, pipe.zCoord);
					}
				}
				result = true;
			}
			pluggables[direction.ordinal()] = null;
			pipe.notifyBlockChanged();
			return result;
		}

		public void invalidate() {
			for (IPipePluggable p : pluggables) {
				if (p != null) {
					p.invalidate();
				}
			}
		}

		public void validate(LogisticsTileGenericPipe pipe) {
			for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
				IPipePluggable p = pluggables[d.ordinal()];

				if (p != null) {
					p.validate(pipe, d);
				}
			}
		}
	}


	private SideProperties sideProperties = new SideProperties();

	@Override
	public void refreshRenderState() {
		
		// WireState
		for (PipeWire color : PipeWire.values()) {
			bcRenderState.wireMatrix.setWire(color, pipe.pipe.bcPipePart.getWireSet()[color.ordinal()]);

			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				bcRenderState.wireMatrix.setWireConnected(color, direction, pipe.pipe.bcPipePart.isWireConnectedTo(pipe.getTile(direction), color));
			}

			boolean lit = pipe.pipe.bcPipePart.getSignalStrength()[color.ordinal()] > 0;

			switch (color) {
				case RED:
					bcRenderState.wireMatrix.setWireIndex(color, lit ? WireIconProvider.Texture_Red_Lit : WireIconProvider.Texture_Red_Dark);
					break;
				case BLUE:
					bcRenderState.wireMatrix.setWireIndex(color, lit ? WireIconProvider.Texture_Blue_Lit : WireIconProvider.Texture_Blue_Dark);
					break;
				case GREEN:
					bcRenderState.wireMatrix.setWireIndex(color, lit ? WireIconProvider.Texture_Green_Lit : WireIconProvider.Texture_Green_Dark);
					break;
				case YELLOW:
					bcRenderState.wireMatrix.setWireIndex(color, lit ? WireIconProvider.Texture_Yellow_Lit : WireIconProvider.Texture_Yellow_Dark);
					break;
				default:
					break;

			}
		}

		// Gate Textures and movement
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			final Gate gate = (Gate) pipe.pipe.bcPipePart.getGate(direction.ordinal());
			bcRenderState.gateMatrix.setIsGateExists(gate != null, direction);
			bcRenderState.gateMatrix.setIsGateLit(gate != null && gate.isGateActive(), direction);
			bcRenderState.gateMatrix.setIsGatePulsing(gate != null && gate.isGatePulsing(), direction);
		}
		// Facades
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			IPipePluggable pluggable = sideProperties.pluggables[direction.ordinal()];
			if (!(pluggable instanceof ItemFacade.FacadePluggable)) {
				bcRenderState.facadeMatrix.setFacade(direction, null, 0, true);
				continue;
			}
			ItemFacade.FacadeState[] states = ((ItemFacade.FacadePluggable) pluggable).states;
			if (states == null) {
				bcRenderState.facadeMatrix.setFacade(direction, null, 0, true);
				continue;
			}
			// Iterate over all states and activate first proper
			ItemFacade.FacadeState defaultState = null, activeState = null;
			for (ItemFacade.FacadeState state : states) {
				if (state.wire == null) {
					defaultState = state;
					continue;
				}
				if (pipe.isWireActive(state.wire)) {
					activeState = state;
					break;
				}
			}
			if (activeState == null) {
				activeState = defaultState;
			}
			Block block = activeState != null ? activeState.block : null;
			int metadata = activeState != null ? activeState.metadata : 0;
			boolean transparent = activeState == null || block == null;
			bcRenderState.facadeMatrix.setFacade(direction, block, metadata, transparent);
		}

		//Plugs
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			IPipePluggable pluggable = sideProperties.pluggables[direction.ordinal()];
			bcRenderState.plugMatrix.setConnected(direction, pluggable instanceof ItemPlug.PlugPluggable);

			if (pluggable instanceof ItemRobotStation.RobotStationPluggable) {
				DockingStation station = ((ItemRobotStation.RobotStationPluggable) pluggable).getStation();

				if (station.isTaken()) {
					if (station.isMainStation()) {
						bcRenderState.robotStationMatrix.setState(direction, RobotStationState.Linked);
					} else {
						bcRenderState.robotStationMatrix.setState(direction, RobotStationState.Reserved);
					}
				} else {
					bcRenderState.robotStationMatrix.setState(direction, RobotStationState.Available);
				}

			} else {
				bcRenderState.robotStationMatrix.setState(direction, RobotStationState.None);
			}

		}

		if (bcRenderState.isDirty()) {
			bcRenderState.clean();
			pipe.sendUpdateToClient();
		}
	}

	@Override
	public boolean addFacade(ForgeDirection direction, int type, int wire, Block[] blocks, int[] metaValues) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void dropFacadeItem(ForgeDirection direction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean dropFacade(ForgeDirection direction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAndDropPlug(ForgeDirection side) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAndDropRobotStation(ForgeDirection side) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addPlug(ForgeDirection direction) {
		return setPluggable(direction, new ItemPlug.PlugPluggable());
	}

	@Override
	public boolean addRobotStation(ForgeDirection direction) {
		return setPluggable(direction, new ItemRobotStation.RobotStationPluggable());
	}
	
	@Override
	public boolean addFacade(ForgeDirection direction, Object states) {
		return setPluggable(direction, new ItemFacade.FacadePluggable((ItemFacade.FacadeState[])states));
	}

	@Override
	public boolean addGate(ForgeDirection direction, Object gate) {
		((Gate)gate).setDirection(direction);
		return setPluggable(direction, new ItemGate.GatePluggable((Gate)gate));
	}

	@Override
	public Object getStation(ForgeDirection direction) {
		IPipePluggable pluggable = sideProperties.pluggables[direction.ordinal()];
		return pluggable instanceof ItemRobotStation.RobotStationPluggable ? ((ItemRobotStation.RobotStationPluggable) pluggable).getStation() : null;
	}

	@Override
	public ItemStack getFacade(ForgeDirection direction) {
		IPipePluggable pluggable = sideProperties.pluggables[direction.ordinal()];
		return pluggable instanceof ItemFacade.FacadePluggable ? ItemFacade.getFacade(((ItemFacade.FacadePluggable) pluggable).states) : null;
	}

	@Override
	public boolean dropSideItems(ForgeDirection direction) {
		return sideProperties.dropItem(pipe, direction);
	}

	@Override
	public boolean hasFacade(ForgeDirection direction) {
		if (direction == null || direction == ForgeDirection.UNKNOWN) {
			return false;
		} else if (pipe.getWorldObj().isRemote) {
			return bcRenderState.facadeMatrix.getFacadeBlock(direction) != null;
		} else {
			return sideProperties.pluggables[direction.ordinal()] instanceof ItemFacade.FacadePluggable;
		}
	}

	@Override
	public boolean hasEnabledFacade(ForgeDirection dir) {
		return hasFacade(dir) && !bcRenderState.facadeMatrix.getFacadeTransparent(dir);
	}

	@Override
	public boolean hasGate(ForgeDirection direction) {
		if (direction == null || direction == ForgeDirection.UNKNOWN) {
			return false;
		} else if (pipe.getWorldObj().isRemote) {
			return bcRenderState.gateMatrix.isGateExists(direction);
		} else {
			return sideProperties.pluggables[direction.ordinal()] instanceof ItemGate.GatePluggable;
		}
	}

	@Override
	public boolean hasPlug(ForgeDirection side) {
		if (side == null || side == ForgeDirection.UNKNOWN) {
			return false;
		}

		if (pipe.getWorldObj().isRemote) {
			return bcRenderState.plugMatrix.isConnected(side);
		}

		return sideProperties.pluggables[side.ordinal()] instanceof ItemPlug.PlugPluggable;
	}

	@Override
	public boolean hasRobotStation(ForgeDirection side) {
		if (side == null || side == ForgeDirection.UNKNOWN) {
			return false;
		}

		if (pipe.getWorldObj().isRemote) {
			return bcRenderState.robotStationMatrix.isConnected(side);
		}

		return sideProperties.pluggables[side.ordinal()] instanceof ItemRobotStation.RobotStationPluggable;
	}

	@Override
	public void setGate(Object gate, int direction) {
		if (sideProperties.pluggables[direction] == null) {
			((Gate)gate).setDirection(ForgeDirection.getOrientation(direction));
			((Gate[])pipe.pipe.bcPipePart.getGates())[direction] = (Gate) gate;
			sideProperties.pluggables[direction] = new ItemGate.GatePluggable((Gate)gate);
		}
	}

	public boolean setPluggable(ForgeDirection direction, IPipePluggable pluggable) {
		if (pipe.getWorldObj() != null && pipe.getWorldObj().isRemote || pluggable == null) {
			return false;
		}
		sideProperties.dropItem(pipe, direction);
		sideProperties.pluggables[direction.ordinal()] = pluggable;
		pluggable.onAttachedPipe(((PipeWrapper)pipe.pipe.bcPipePart.getWrapped()).getTile(), direction);
		pipe.notifyBlockChanged();
		return true;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		sideProperties.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		sideProperties.readFromNBT(nbt);
		attachPluggables = true;
	}
	
	@Override
	public void updateEntity() {
		if (attachPluggables) {
			attachPluggables = false;
			// Attach callback
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				if (sideProperties.pluggables[i] != null) {
					sideProperties.pluggables[i].onAttachedPipe(((PipeWrapper)pipe.pipe.bcPipePart.getWrapped()).getTile(), ForgeDirection.getOrientation(i));
				}
			}
		}
	}

	@Override
	public void invalidate() {
		sideProperties.invalidate();

	}

	@Override
	public void validate() {
		sideProperties.validate(pipe);
	}

	@Override
	public Object getPluggables(int i) {
		return sideProperties.pluggables[i];
	}

	@Override
	public boolean hasBlockingPluggable(ForgeDirection side) {
		IPipePluggable pluggable = sideProperties.pluggables[side.ordinal()];
		return pluggable != null && pluggable.blocking(pipe, side);
	}
}
