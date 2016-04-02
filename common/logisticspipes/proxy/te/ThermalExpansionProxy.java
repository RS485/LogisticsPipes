package logisticspipes.proxy.te;

import java.util.LinkedList;
import java.util.List;

import cofh.core.RegistryEnderAttuned;
import cofh.lib.transport.EnderRegistry;
import logisticspipes.proxy.interfaces.ICraftingParts;
import logisticspipes.proxy.interfaces.IThermalExpansionProxy;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import cofh.api.transport.IEnderItemHandler;
import cofh.thermalexpansion.block.TEBlocks;
import cofh.thermalexpansion.block.ender.TileTesseract;
import cofh.thermalexpansion.item.TEItems;

public class ThermalExpansionProxy implements IThermalExpansionProxy {

	@Override
	public boolean isTesseract(TileEntity tile) {
		return tile instanceof TileTesseract;
	}

	@Override
	public List<TileEntity> getConnectedTesseracts(TileEntity tile) {
		EnderRegistry registry = RegistryEnderAttuned.getRegistry();
		List<TileEntity> validOutputs = new LinkedList<TileEntity>();
		if(registry == null) return validOutputs;
		List<IEnderItemHandler> interfaces = registry.getLinkedItemOutputs((TileTesseract) tile);
		if (interfaces == null) {
			return validOutputs;
		}
		for (IEnderItemHandler object : interfaces) {
			if (object.canReceiveItems() && object.canSendItems() && object instanceof TileEntity) {
				validOutputs.add((TileEntity) object);
			}
		}
		return validOutputs;
	}

	@Override
	public boolean isTE() {
		return true;
	}

	@Override
	public ICraftingParts getRecipeParts() {
		return new ICraftingParts() {

			@Override
			public ItemStack getChipTear1() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ItemStack getChipTear2() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ItemStack getChipTear3() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getGearTear1() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getGearTear2() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getGearTear3() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getSortingLogic() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getBasicTransport() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getWaterProof() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getExtractorItem() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getExtractorFluid() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getBlockDynamo() {
				return new ItemStack(TEBlocks.blockDynamo, 1, 0);
			}

			@Override
			public Object getPowerCoilSilver() {
				return TEItems.powerCoilSilver;
			}

			@Override
			public Object getPowerCoilGold() {
				return TEItems.powerCoilGold;
			}

		};
	}
}
