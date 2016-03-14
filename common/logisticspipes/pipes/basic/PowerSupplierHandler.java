package logisticspipes.pipes.basic;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import logisticspipes.blocks.powertile.LogisticsPowerProviderTileEntity;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyReceiver;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;

import net.minecraft.util.EnumFacing;

import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper.AdjacentTileEntity;

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

			Stream<AdjacentTileEntity> adjacentTileEntityStream = new WorldCoordinatesWrapper(pipe.container).getAdjacentTileEntities();

			double globalNeed = 0;
			double[] need = new double[(int) adjacentTileEntityStream.count()];
			adjacentTileEntityStream = new WorldCoordinatesWrapper(pipe.container).getAdjacentTileEntities();
			int i = 0;
			Iterator<AdjacentTileEntity> adjacentIt = adjacentTileEntityStream.iterator();
			while (adjacentIt.hasNext()) {
				AdjacentTileEntity adjacent = adjacentIt.next();

				if (SimpleServiceLocator.cofhPowerProxy.isEnergyReceiver(adjacent.tileEntity)) {
					if (pipe.canPipeConnect(adjacent.tileEntity, adjacent.direction)) {
						ICoFHEnergyReceiver energyReceiver = SimpleServiceLocator.cofhPowerProxy.getEnergyReceiver(adjacent.tileEntity);
						EnumFacing oppositeDir = adjacent.direction.getOpposite();
						if (energyReceiver.canConnectEnergy(oppositeDir)) {
							globalNeed += need[i] = (energyReceiver.getMaxEnergyStored(oppositeDir) - energyReceiver.getEnergyStored(oppositeDir));
						}
					}
				}
				++i;
			}

			if (globalNeed != 0 && !Double.isNaN(globalNeed)) {
				double fullfillable = Math.min(1, internalBufferRF / globalNeed);
				i = 0;
				adjacentIt = adjacentTileEntityStream.iterator();
				while (adjacentIt.hasNext()) {
					AdjacentTileEntity adjacent = adjacentIt.next();

					if (SimpleServiceLocator.cofhPowerProxy.isEnergyReceiver(adjacent.tileEntity)) {
						if (pipe.canPipeConnect(adjacent.tileEntity, adjacent.direction)) {
							ICoFHEnergyReceiver energyReceiver = SimpleServiceLocator.cofhPowerProxy.getEnergyReceiver(adjacent.tileEntity);
							EnumFacing oppositeDir = adjacent.direction.getOpposite();
							if (energyReceiver.canConnectEnergy(oppositeDir)) {
								if (internalBufferRF + 1 < need[i] * fullfillable) {
									return;
								}
								int used = energyReceiver.receiveEnergy(oppositeDir, (int) (need[i] * fullfillable), false);
								if (used > 0) {
									pipe.container.addLaser(adjacent.direction, 0.5F, LogisticsPowerProviderTileEntity.RF_COLOR, false, true);
									internalBufferRF -= used;
								}
								if (internalBufferRF < 0) {
									internalBufferRF = 0;
									return;
								}
							}
						}
					}
					++i;
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

			Stream<AdjacentTileEntity> adjacentTileEntityStream = new WorldCoordinatesWrapper(pipe.container).getAdjacentTileEntities();

			double globalNeed = 0;
			double[] need = new double[(int) adjacentTileEntityStream.count()];
			adjacentTileEntityStream = new WorldCoordinatesWrapper(pipe.container).getAdjacentTileEntities();
			int i = 0;
			Iterator<AdjacentTileEntity> adjacentIt = adjacentTileEntityStream.iterator();
			while (adjacentIt.hasNext()) {
				AdjacentTileEntity adjacent = adjacentIt.next();

				if (SimpleServiceLocator.IC2Proxy.isEnergySink(adjacent.tileEntity)) {
					if (pipe.canPipeConnect(adjacent.tileEntity, adjacent.direction)) {
						if (SimpleServiceLocator.IC2Proxy.acceptsEnergyFrom(adjacent.tileEntity, pipe.container, adjacent.direction.getOpposite())) {
							globalNeed += need[i] = SimpleServiceLocator.IC2Proxy.demandedEnergyUnits(adjacent.tileEntity);
						}
					}
				}
				++i;
			}

			if (globalNeed != 0 && !Double.isNaN(globalNeed)) {
				double fullfillable = Math.min(1, internalBufferIC2 / globalNeed);
				i = 0;
				adjacentIt = adjacentTileEntityStream.iterator();
				while (adjacentIt.hasNext()) {
					AdjacentTileEntity adjacent = adjacentIt.next();

					if (SimpleServiceLocator.IC2Proxy.isEnergySink(adjacent.tileEntity) && pipe.canPipeConnect(adjacent.tileEntity, adjacent.direction)
							&& SimpleServiceLocator.IC2Proxy.acceptsEnergyFrom(adjacent.tileEntity, pipe.container, adjacent.direction.getOpposite())) {
						if (internalBufferIC2 + 1 < need[i] * fullfillable) {
							return;
						}
						double toUse = Math.min(pipe.getUpgradeManager().getIC2PowerLevel(), need[i] * fullfillable);
						double unUsed = SimpleServiceLocator.IC2Proxy.injectEnergyUnits(adjacent.tileEntity, adjacent.direction.getOpposite(), toUse);
						double used = toUse - unUsed;
						if (used > 0) {
							//MainProxy.sendPacketToAllWatchingChunk(this.pipe.getX(), this.pipe.getZ(), MainProxy.getDimensionForWorld(this.pipe.getWorld()), PacketHandler.getPacket(PowerPacketLaser.class).setColor(LogisticsPowerProviderTileEntity.IC2_COLOR).setPos(this.pipe.getLPPosition()).setRenderBall(true).setDir(adTile.orientation).setLength(0.5F));
							pipe.container.addLaser(adjacent.direction, 0.5F, LogisticsPowerProviderTileEntity.IC2_COLOR, false, true);
							internalBufferIC2 -= used;
						}
						if (internalBufferIC2 < 0) {
							internalBufferIC2 = 0;
							return;
						}
					}
					++i;
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
