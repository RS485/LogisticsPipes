package logisticspipes.proxy.te;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import cofh.api.item.IToolHammer;

import logisticspipes.proxy.interfaces.IThermalExpansionProxy;
import logisticspipes.recipes.CraftingParts;

public class ThermalExpansionProxy implements IThermalExpansionProxy {

	/*
	@Override
	public boolean isTesseract(TileEntity tile) {
		return tile instanceof TileTesseract;
	}

	@Override
	public List<TileEntity> getConnectedTesseracts(TileEntity tile) {
		EnderRegistry registry = RegistryEnderAttuned.getRegistry();
		List<TileEntity> validOutputs = new LinkedList<>();
		if(registry == null) return validOutputs;
		List<IEnderItemHandler> interfaces = registry.getLinkedItemOutputs((TileTesseract) tile);
		if (interfaces == null) {
			return validOutputs;
		}
		validOutputs.addAll(interfaces.stream()
				.filter(object -> object.canReceiveItems() && object.canSendItems() && object instanceof TileEntity)
				.map(object -> (TileEntity) object).collect(Collectors.toList()));
		return validOutputs;
	}
	*/

	@Override
	public boolean isTE() {
		return true;
	}

	@Override
	public CraftingParts getRecipeParts() {
		return null;
	}

	@Override
	public boolean isToolHammer(Item item) {
		return item instanceof IToolHammer;
	}

	@Override
	public boolean canHammer(@Nonnull ItemStack stack, EntityPlayer entityplayer, BlockPos pos) {
		return isToolHammer(stack.getItem()) && ((IToolHammer) stack.getItem()).isUsable(stack, entityplayer, pos);
	}

	@Override
	public void toolUsed(@Nonnull ItemStack stack, EntityPlayer entityplayer, BlockPos pos) {
		if (isToolHammer(stack.getItem())) ((IToolHammer) stack.getItem()).toolUsed(stack, entityplayer, pos);
	}
}
