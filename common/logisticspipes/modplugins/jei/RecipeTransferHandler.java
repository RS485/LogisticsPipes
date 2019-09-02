package logisticspipes.modplugins.jei;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;

import net.minecraftforge.oredict.OreDictionary;

import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

import logisticspipes.gui.GuiLogisticsCraftingTable;
import logisticspipes.gui.orderer.GuiRequestTable;
import logisticspipes.gui.popup.GuiRecipeImport;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.NEISetCraftingRecipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;

public class RecipeTransferHandler implements IRecipeTransferHandler {

	private IRecipeTransferHandlerHelper recipeTransferHandlerHelper;

	public RecipeTransferHandler(IRecipeTransferHandlerHelper recipeTransferHandlerHelper) {
		this.recipeTransferHandlerHelper = recipeTransferHandlerHelper;
	}

	@Nonnull
	@Override
	public Class getContainerClass() {
		return DummyContainer.class;
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(@Nonnull Container container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
		if(container instanceof DummyContainer) {
			DummyContainer dContainer = (DummyContainer) container;

			LogisticsBaseGuiScreen gui = dContainer.guiHolderForJEI;

			if(gui instanceof GuiLogisticsCraftingTable || gui instanceof GuiRequestTable) {

				TileEntity tile;
				if (gui instanceof GuiLogisticsCraftingTable) {
					tile = ((GuiLogisticsCraftingTable) gui)._crafter;
				} else {
					tile = ((GuiRequestTable) gui)._table.container;
				}

				if(tile == null) {
					return recipeTransferHandlerHelper.createInternalError();
				}

				if(!recipeLayout.getRecipeCategory().getUid().equals(VanillaRecipeCategoryUid.CRAFTING)) {
					return recipeTransferHandlerHelper.createInternalError();
				}

				ItemStack[] stack = new ItemStack[9];
				ItemStack[][] stacks = new ItemStack[9][];
				boolean hasCanidates = false;
				NEISetCraftingRecipe packet = PacketHandler.getPacket(NEISetCraftingRecipe.class);

				IGuiItemStackGroup guiItemStackGroup = recipeLayout.getItemStacks();
				Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = guiItemStackGroup.getGuiIngredients();

				if(doTransfer) {
					for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> ps : guiIngredients.entrySet()) {
						if(!ps.getValue().isInput()) continue;

						int slot = ps.getKey() - 1;

						if (slot < 9) {
							stack[slot] = ps.getValue().getDisplayedIngredient();
							List<ItemStack> list = new ArrayList<>(ps.getValue().getAllIngredients());
							if(!list.isEmpty()) {
								Iterator<ItemStack> iter = list.iterator();
								while (iter.hasNext()) {
									ItemStack wildCardCheckStack = iter.next();
									if (wildCardCheckStack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
										iter.remove();
										NonNullList<ItemStack> secondList = NonNullList.create();
										wildCardCheckStack.getItem().getSubItems(wildCardCheckStack.getItem().getCreativeTab(), secondList);
										list.addAll(secondList);
										iter = list.iterator();
									}
								}
								stacks[slot] = list.toArray(new ItemStack[0]);
								if (stacks[slot].length > 1) {
									hasCanidates = true;
								} else if(stacks[slot].length == 1) {
									stack[slot] = stacks[slot][0];
								}
							}
						}
					}

					if (hasCanidates) {
						gui.setSubGui(new GuiRecipeImport(tile, stacks));
					} else {
						MainProxy.sendPacketToServer(packet.setContent(stack).setTilePos(tile));
					}
				}
				return null;
			}
		}
		return recipeTransferHandlerHelper.createInternalError();
	}
}
