package logisticspipes.pipes.basic;

import java.util.LinkedList;
import java.util.List;

import logisticspipes.blocks.powertile.LogisticsPowerProviderTileEntity;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyReceiver;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.WorldUtil;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;

public class PowerSupplierHandler {

	private static final double INTERNAL_RF_BUFFER_MAX = 10000;
	private static final double INTERNAL_IC2_BUFFER_MAX = 2048 * 4;

	private final CoreRoutedPipe pipe;

	private double internalBufferRF = 0F;
	private double internalBufferIC2 = 0F;

	public PowerSupplierHandler(CoreRoutedPipe pipe) {
		this.pipe = pipe;
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		if (internalBufferRF > 0) {
			nbttagcompound.setDouble("bufferRF", internalBufferRF);
		}
		if (internalBufferIC2 > 0) {
			nbttagcompound.setDouble("bufferEU", internalBufferIC2);
		}
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		if (nbttagcompound.getTag("bufferRF") instanceof NBTTagFloat) { // support for old float
			internalBufferRF = nbttagcompound.getFloat("bufferRF");
		} else {
			internalBufferRF = nbttagcompound.getDouble("bufferRF");
		}
		if (nbttagcompound.getTag("bufferEU") instanceof NBTTagFloat) { // support for old float
			internalBufferRF = nbttagcompound.getFloat("bufferEU");
		} else {
			internalBufferRF = nbttagcompound.getDouble("bufferEU");
		}
	}

	public void update() {
		if (SimpleServiceLocator.cofhPowerProxy.isAvailable() && pipe.getUpgradeManager().hasRFPowerSupplierUpgrade()) {
			//Use Buffer
			WorldUtil worldUtil = new WorldUtil(pipe.getWorld(), pipe.getX(), pipe.getY(), pipe.getZ());
			LinkedList<AdjacentTile> adjacent = worldUtil.getAdjacentTileEntities(false);
			double globalNeed = 0;
			double[] need = new double[adjacent.size()];
			int i = 0;
			for (AdjacentTile adTile : adjacent) {
				if (SimpleServiceLocator.cofhPowerProxy.isEnergyReceiver(adTile.tile) && pipe.canPipeConnect(adTile.tile, adTile.orientation)) {
					ICoFHEnergyReceiver handler = SimpleServiceLocator.cofhPowerProxy.getEnergyReceiver(adTile.tile);
					if (handler.canConnectEnergy(adTile.orientation.getOpposite())) {
						globalNeed += need[i] =
								handler.getMaxEnergyStored(adTile.orientation.getOpposite()) - handler.getEnergyStored(adTile.orientation.getOpposite());
					}
				}
				i++;
			}
			if (globalNeed != 0 && !Double.isNaN(globalNeed)) {
				double fullfillable = Math.min(1, internalBufferRF / globalNeed);
				i = 0;
				for (AdjacentTile adTile : adjacent) {
					if (SimpleServiceLocator.cofhPowerProxy.isEnergyReceiver(adTile.tile) && pipe.canPipeConnect(adTile.tile, adTile.orientation)) {
						ICoFHEnergyReceiver handler = SimpleServiceLocator.cofhPowerProxy.getEnergyReceiver(adTile.tile);
						if (handler.canConnectEnergy(adTile.orientation.getOpposite())) {
							if (internalBufferRF + 1 < need[i] * fullfillable) {
								return;
							}
							int used = handler.receiveEnergy(adTile.orientation.getOpposite(), (int) (need[i] * fullfillable), false);
							if (used > 0) {
								pipe.container.addLaser(adTile.orientation, 0.5F, LogisticsPowerProviderTileEntity.RF_COLOR, false, true);
								internalBufferRF -= used;
							}
							if (internalBufferRF < 0) {
								internalBufferRF = 0;
								return;
							}
						}
					}
					i++;
				}
			}
			//Rerequest Buffer
			List<Pair<ISubSystemPowerProvider, List<IFilter>>> provider = pipe.getRouter().getSubSystemPowerProvider();
			double available = 0;
			outer:
			for (Pair<ISubSystemPowerProvider, List<IFilter>> pair : provider) {
				for (IFilter filter : pair.getValue2()) {
					if (filter.blockPower()) {
						continue outer;
					}
				}
				if (pair.getValue1().usePaused()) {
					continue;
				}
				if (!pair.getValue1().getBrand().equals("RF")) {
					continue;
				}
				available += pair.getValue1().getPowerLevel();
			}
			if (available > 0) {
				double neededPower = PowerSupplierHandler.INTERNAL_RF_BUFFER_MAX - internalBufferRF;
				if (neededPower > 0) {
					if (pipe.useEnergy((int) (neededPower / 100), false)) {
						outer:
						for (Pair<ISubSystemPowerProvider, List<IFilter>> pair : provider) {
							for (IFilter filter : pair.getValue2()) {
								if (filter.blockPower()) {
									continue outer;
								}
							}
							if (pair.getValue1().usePaused()) {
								continue;
							}
							if (!pair.getValue1().getBrand().equals("RF")) {
								continue;
							}
							double requestamount = neededPower * (pair.getValue1().getPowerLevel() / available);
							pair.getValue1().requestPower(pipe.getRouterId(), requestamount);
						}
					}
				}
			}
		}
		if (SimpleServiceLocator.IC2Proxy.hasIC2() && pipe.getUpgradeManager().getIC2PowerLevel() > 0) {
			//Use Buffer
			WorldUtil worldUtil = new WorldUtil(pipe.getWorld(), pipe.getX(), pipe.getY(), pipe.getZ());
			LinkedList<AdjacentTile> adjacent = worldUtil.getAdjacentTileEntities(false);
			double globalNeed = 0;
			double[] need = new double[adjacent.size()];
			int i = 0;
			for (AdjacentTile adTile : adjacent) {
				if (SimpleServiceLocator.IC2Proxy.isEnergySink(adTile.tile) && pipe.canPipeConnect(adTile.tile, adTile.orientation)
						&& SimpleServiceLocator.IC2Proxy.acceptsEnergyFrom(adTile.tile, pipe.container, adTile.orientation.getOpposite())) {
					globalNeed += need[i] = (double) SimpleServiceLocator.IC2Proxy.demandedEnergyUnits(adTile.tile);
				}
				i++;
			}
			if (globalNeed != 0 && !Double.isNaN(globalNeed)) {
				double fullfillable = Math.min(1, internalBufferIC2 / globalNeed);
				i = 0;
				for (AdjacentTile adTile : adjacent) {
					if (SimpleServiceLocator.IC2Proxy.isEnergySink(adTile.tile) && pipe.canPipeConnect(adTile.tile, adTile.orientation)
							&& SimpleServiceLocator.IC2Proxy.acceptsEnergyFrom(adTile.tile, pipe.container, adTile.orientation.getOpposite())) {
						if (internalBufferIC2 + 1 < need[i] * fullfillable) {
							return;
						}
						double toUse = Math.min(pipe.getUpgradeManager().getIC2PowerLevel(), need[i] * fullfillable);
						double unUsed = SimpleServiceLocator.IC2Proxy.injectEnergyUnits(adTile.tile, adTile.orientation.getOpposite(), toUse);
						double used = toUse - unUsed;
						if (used > 0) {
							//MainProxy.sendPacketToAllWatchingChunk(this.pipe.getX(), this.pipe.getZ(), MainProxy.getDimensionForWorld(this.pipe.getWorld()), PacketHandler.getPacket(PowerPacketLaser.class).setColor(LogisticsPowerProviderTileEntity.IC2_COLOR).setPos(this.pipe.getLPPosition()).setRenderBall(true).setDir(adTile.orientation).setLength(0.5F));
							pipe.container.addLaser(adTile.orientation, 0.5F, LogisticsPowerProviderTileEntity.IC2_COLOR, false, true);
							internalBufferIC2 -= used;
						}
						if (internalBufferIC2 < 0) {
							internalBufferIC2 = 0;
							return;
						}
					}
					i++;
				}
			}
			//Rerequest Buffer
			List<Pair<ISubSystemPowerProvider, List<IFilter>>> provider = pipe.getRouter().getSubSystemPowerProvider();
			double available = 0;
			outer:
			for (Pair<ISubSystemPowerProvider, List<IFilter>> pair : provider) {
				for (IFilter filter : pair.getValue2()) {
					if (filter.blockPower()) {
						continue outer;
					}
				}
				if (pair.getValue1().usePaused()) {
					continue;
				}
				if (!pair.getValue1().getBrand().equals("EU")) {
					continue;
				}
				available += pair.getValue1().getPowerLevel();
			}
			if (available > 0) {
				double neededPower = PowerSupplierHandler.INTERNAL_IC2_BUFFER_MAX - internalBufferIC2;
				if (neededPower > 0) {
					if (pipe.useEnergy((int) (neededPower / 10000), false)) {
						outer:
						for (Pair<ISubSystemPowerProvider, List<IFilter>> pair : provider) {
							for (IFilter filter : pair.getValue2()) {
								if (filter.blockPower()) {
									continue outer;
								}
							}
							if (pair.getValue1().usePaused()) {
								continue;
							}
							if (!pair.getValue1().getBrand().equals("EU")) {
								continue;
							}
							double requestamount = neededPower * (pair.getValue1().getPowerLevel() / available);
							pair.getValue1().requestPower(pipe.getRouterId(), requestamount);
						}
					}
				}
			}
		}
	}

	public void addRFPower(double toSend) {
		internalBufferRF += toSend;
	}

	public void addIC2Power(double toSend) {
		internalBufferIC2 += toSend;
	}
}
