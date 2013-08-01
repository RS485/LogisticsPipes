package logisticspipes.transport;

import java.util.BitSet;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.pipe.PipeLiquidUpdate;
import logisticspipes.proxy.MainProxy;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.core.utils.Utils;

public class PipeLiquidTransportLogistics extends PipeTransportLogistics implements IFluidHandler {

	public FluidTank[] sideTanks = new FluidTank[ForgeDirection.VALID_DIRECTIONS.length];
	public FluidTank internalTank = new FluidTank(getInnerCapacity());
	
	public FluidStack[] renderCache = new FluidStack[7];
	
	public PipeLiquidTransportLogistics() {
		for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
			sideTanks[dir.ordinal()] = new FluidTank(getSideCapacity());
		}
	}
	
	
	
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if(from.ordinal() < ForgeDirection.VALID_DIRECTIONS.length) {
			return sideTanks[from.ordinal()].fill(resource, doFill);
		} else {
			return 0;//internalTank.fill(resource, doFill);
		}
	}

	@Override
	public int fill(int tankIndex, FluidStack resource, boolean doFill) {
		return fill(ForgeDirection.getOrientation(tankIndex), resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if(from.ordinal() < ForgeDirection.VALID_DIRECTIONS.length) {
			return sideTanks[from.ordinal()].drain(maxDrain, doDrain);
		} else {
			return null;//internalTank.drain(maxDrain, doDrain);
		}
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resouce, boolean doDrain) {
		return drain(ForgeDirection.getOrientation(tankIndex), resource, doDrain);
	}

	@Override
	public IFluidTank[] getTanks(ForgeDirection direction) {
		if(direction.ordinal() < ForgeDirection.VALID_DIRECTIONS.length) {
			return new IFluidTank[]{sideTanks[direction.ordinal()]};
		} else {
			return null;//new IFluidTank[]{internalTank};
		}
	}


	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (nbttagcompound.hasKey("tank[" + direction.ordinal() + "]")) {
				sideTanks[direction.ordinal()].readFromNBT(nbttagcompound.getCompoundTag("tank[" + direction.ordinal() + "]"));
			}
		}
		if (nbttagcompound.hasKey("tank[middle]")) {
			internalTank.readFromNBT(nbttagcompound.getCompoundTag("tank[middle]"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			NBTTagCompound subTag = new NBTTagCompound();
			sideTanks[direction.ordinal()].writeToNBT(subTag);
			nbttagcompound.setTag("tank[" + direction.ordinal() + "]", subTag);
		}
		NBTTagCompound subTag = new NBTTagCompound();
		internalTank.writeToNBT(subTag);
		nbttagcompound.setTag("tank[middle]", subTag);
	}
	
	@Override
	public IFluidTank getTank(ForgeDirection direction, FluidStack type) {
		return null;
	}
	
	/*
	@Override
	public boolean allowsConnect(PipeTransport with) {
		return super.allowsConnect(with) || with instanceof LogisitcsLiquidConnectionTransport;
	}
	*/

	public int getInnerCapacity() {
		return 10000;
	}

	public int getSideCapacity() {
		return 5000;
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (!Utils.checkPipesConnections(container.getTile(orientations[direction.ordinal()]), container)) {
				if(MainProxy.isServer(getWorld())) {
					FluidStack stack = sideTanks[direction.ordinal()].getFluid();
					if(stack != null) {
						sideTanks[direction.ordinal()].setFluid(null);
						internalTank.fill(stack, true);
					}
				}
				if(renderCache[direction.ordinal()] != null) {
					renderCache[direction.ordinal()].amount = 1;
				}
			}
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		updateLiquid();
	}
	
	/*
	 * BuildCraft Liquid Sync Code
	 */
	private final SafeTimeTracker tracker = new SafeTimeTracker();
	private long clientSyncCounter = BuildCraftCore.longUpdateFactor - 10;
	public byte initClient = 0;
	
	private static final ForgeDirection[] orientations = ForgeDirection.values();

	private void updateLiquid() {
		if(MainProxy.isClient(getWorld())) return;
		if (tracker.markTimeIfDelay(getWorld(), BuildCraftCore.updateFactor)) {

			boolean init = false;
			if (++clientSyncCounter > BuildCraftCore.longUpdateFactor) {
				clientSyncCounter = 0;
				init = true;
			}
			if(clientSyncCounter < 0) clientSyncCounter = 0;
			ModernPacket packet = computeLiquidUpdate(init, true);
			if (packet != null) {
				MainProxy.sendPacketToAllWatchingChunk(xCoord, zCoord, MainProxy.getDimensionForWorld(getWorld()), packet);
			}
		}
	}

	/**
	 * Computes the PacketLiquidUpdate packet for transmission to a client
	 * 
	 * @param initPacket
	 *            everything is sent, no delta stuff ( first packet )
	 * @param persistChange
	 *            The render cache change is persisted
	 * @return PacketLiquidUpdate liquid update packet
	 */
	private ModernPacket computeLiquidUpdate(boolean initPacket, boolean persistChange) {

		boolean changed = false;
		BitSet delta = new BitSet(21);

		if (initClient > 0) {
			initClient--;
			if (initClient == 1) {
				changed = true;
				delta.set(0, 21);
			}
		}

		FluidStack[] renderCache = this.renderCache.clone();

		for (ForgeDirection dir : orientations) {
			FluidStack current;
			if(dir != ForgeDirection.UNKNOWN) {
				current = sideTanks[dir.ordinal()].getFluid();
			} else {
				current = internalTank.getFluid();
			}
			FluidStack prev = renderCache[dir.ordinal()];

			if (prev == null && current == null) {
				continue;
			}

			if (prev == null && current != null) {
				changed = true;
				renderCache[dir.ordinal()] = current.copy();
				delta.set(dir.ordinal() * 3 + 0);
				delta.set(dir.ordinal() * 3 + 1);
				delta.set(dir.ordinal() * 3 + 2);
				continue;
			}

			if (prev != null && current == null) {
				changed = true;
				renderCache[dir.ordinal()] = null;
				delta.set(dir.ordinal() * 3 + 0);
				delta.set(dir.ordinal() * 3 + 1);
				delta.set(dir.ordinal() * 3 + 2);
				continue;
			}

			if (prev.itemID != current.itemID || initPacket) {
				changed = true;
				renderCache[dir.ordinal()]=new FluidStack(current.itemID,renderCache[dir.ordinal()].amount);
				//TODO check: @GUIpsp Possibly instanciating multiple times, might be slow
				delta.set(dir.ordinal() * 3 + 0);
			}

			if (prev.itemMeta != current.itemMeta || initPacket) {
				changed = true;
				renderCache[dir.ordinal()]=new FluidStack(current.itemID,renderCache[dir.ordinal()].amount,current.itemMeta);
				delta.set(dir.ordinal() * 3 + 1);
			}

			int displayQty = (prev.amount * 4 + current.amount) / 5;
			if (displayQty == 0 && current.amount > 0 || initPacket) {
				displayQty = current.amount;
			}
			if(dir != ForgeDirection.UNKNOWN) {
				displayQty = Math.min(getSideCapacity(), displayQty);
			} else {
				displayQty = Math.min(getInnerCapacity(), displayQty);
			}

			if (prev.amount != displayQty || initPacket) {
				changed = true;
				renderCache[dir.ordinal()].amount = displayQty;
				delta.set(dir.ordinal() * 3 + 2);
			}
		}

		if (persistChange) {
			this.renderCache = renderCache;
		}

		if (changed || initPacket) {
			return PacketHandler.getPacket(PipeLiquidUpdate.class).setRenderCache(renderCache).setDelta(delta).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord).setChunkDataPacket(initPacket);
		}

		return null;

	}

	@Override
	protected boolean isItemExitable(ItemStack stack) {
		return true;
	}



	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
