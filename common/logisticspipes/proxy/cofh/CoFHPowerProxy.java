package logisticspipes.proxy.cofh;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyReceiver;
import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyStorage;
import logisticspipes.proxy.interfaces.ICoFHPowerProxy;
import logisticspipes.recipes.CraftingParts;
import logisticspipes.recipes.RecipeManager;
import logisticspipes.recipes.RecipeManager.LocalCraftingManager;

public class CoFHPowerProxy implements ICoFHPowerProxy {

	private class MEnergyStorage extends EnergyStorage {

		public MEnergyStorage(int capacity) {
			super(capacity);
		}

		public void readFromNBT(NBTTagCompound nbt) {
			this.energy = nbt.getInteger("Energy");

			if (energy > capacity) {
				energy = capacity;
			}
		}

		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			if (energy < 0) {
				energy = 0;
			}
			nbt.setInteger("Energy", energy);
			return nbt;
		}
	}

	@Override
	public boolean isEnergyReceiver(TileEntity tile, EnumFacing face) {
		if(tile.hasCapability(CapabilityEnergy.ENERGY, face)) {
			return tile.getCapability(CapabilityEnergy.ENERGY, face).canReceive();
		}
		return tile instanceof IEnergyStorage;
	}

	@Override
	public ICoFHEnergyReceiver getEnergyReceiver(TileEntity tile, EnumFacing face) {
		IEnergyStorage bHandler = null;
		if(tile.hasCapability(CapabilityEnergy.ENERGY, face)) {
			bHandler = tile.getCapability(CapabilityEnergy.ENERGY, face);
		} else if(tile instanceof IEnergyStorage) {
			bHandler = (IEnergyStorage) tile;
		}
		final IEnergyStorage handler = bHandler;
		return new ICoFHEnergyReceiver() {

			@Override
			public int getMaxEnergyStored() {
				return handler.getMaxEnergyStored();
			}

			@Override
			public int getEnergyStored() {
				return handler.getEnergyStored();
			}

			@Override
			public int receiveEnergy(EnumFacing opposite, int amount, boolean simulate) {
				return handler.receiveEnergy(amount, simulate);
			}
		};
	}

	@Override
	public void addCraftingRecipes(CraftingParts parts) {
		LocalCraftingManager craftingManager = RecipeManager.craftingManager;
		/*
		craftingManager.addRecipe(
				new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_RF_SUPPLIER),
				CraftingDependency.Power_Distribution,
				new Object[] { false, "PEP", "RBR", "PTP", Character.valueOf('B'), new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_TRANSPORTATION), Character.valueOf('P'), Items.paper, Character.valueOf('E'), parts.getBlockDynamo(), Character.valueOf('T'), parts.getPowerCoilSilver(),
						Character.valueOf('R'), parts.getPowerCoilGold() });
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_RF_POWERPROVIDER), CraftingDependency.Power_Distribution,
				new Object[] { false, "PEP", "RBR", "PTP", Character.valueOf('B'), Blocks.redstone_block, Character.valueOf('P'), Items.paper, Character.valueOf('E'), parts.getBlockDynamo(), Character.valueOf('T'), parts.getPowerCoilSilver(), Character.valueOf('R'), parts.getPowerCoilGold() });
		*/
	}

	@Override
	public ICoFHEnergyStorage getEnergyStorage(int i) {
		final MEnergyStorage energy = new MEnergyStorage(i);
		return new ICoFHEnergyStorage() {

			@Override
			public int extractEnergy(int space, boolean b) {
				return energy.extractEnergy(space, b);
			}

			@Override
			public int receiveEnergy(int maxReceive, boolean simulate) {
				return energy.receiveEnergy(maxReceive, simulate);
			}

			@Override
			public int getEnergyStored() {
				return energy.getEnergyStored();
			}

			@Override
			public int getMaxEnergyStored() {
				return energy.getMaxEnergyStored();
			}

			@Override
			public void readFromNBT(NBTTagCompound nbt) {
				energy.readFromNBT(nbt);
			}

			@Override
			public void writeToNBT(NBTTagCompound nbt) {
				energy.writeToNBT(nbt);
			}

		};
	}

	@Override
	public boolean isAvailable() {
		return true;
	}
}
