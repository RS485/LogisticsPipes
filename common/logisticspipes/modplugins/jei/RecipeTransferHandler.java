package logisticspipes.modplugins.jei;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
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

public class RecipeTransferHandler implements IRecipeTransferHandler<DummyContainer> {

	private final IRecipeTransferHandlerHelper recipeTransferHandlerHelper;

	public RecipeTransferHandler(IRecipeTransferHandlerHelper recipeTransferHandlerHelper) {
		this.recipeTransferHandlerHelper = recipeTransferHandlerHelper;
	}

	@Nonnull
	@Override
	public Class<DummyContainer> getContainerClass() {
		return DummyContainer.class;
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(@Nonnull DummyContainer container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
		LogisticsBaseGuiScreen gui = container.guiHolderForJEI;

		if (gui instanceof GuiLogisticsCraftingTable || gui instanceof GuiRequestTable) {

			TileEntity tile;
			if (gui instanceof GuiLogisticsCraftingTable) {
				tile = ((GuiLogisticsCraftingTable) gui)._crafter;
			} else {
				tile = ((GuiRequestTable) gui)._table.container;
			}

			if (tile == null) {
				return recipeTransferHandlerHelper.createInternalError();
			}

			if (!recipeLayout.getRecipeCategory().getUid().equals(VanillaRecipeCategoryUid.CRAFTING)) {
				return recipeTransferHandlerHelper.createInternalError();
			}

			NonNullList<ItemStack> stackList = NonNullList.withSize(9, ItemStack.EMPTY);
			ItemStack[][] stacks = new ItemStack[9][];
			boolean hasCanidates = false;
			NEISetCraftingRecipe packet = PacketHandler.getPacket(NEISetCraftingRecipe.class);

			IGuiItemStackGroup guiItemStackGroup = recipeLayout.getItemStacks();
			Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = guiItemStackGroup.getGuiIngredients();

			if (doTransfer) {
				for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> ps : guiIngredients.entrySet()) {
					if (!ps.getValue().isInput()) continue;

					int slot = ps.getKey() - 1;

					if (slot < 9) {
						final ItemStack displayedIngredient = ps.getValue().getDisplayedIngredient();
						stackList.set(slot, displayedIngredient == null ? ItemStack.EMPTY : displayedIngredient);
						NonNullList<ItemStack> list = NonNullList.create();
						ps.getValue().getAllIngredients().stream().map(itemStack -> itemStack == null ? ItemStack.EMPTY : itemStack).forEach(stackList::add);
						if (!list.isEmpty()) {
							Iterator<ItemStack> iter = list.iterator();
							while (iter.hasNext()) {
								ItemStack wildCardCheckStack = iter.next();
								if (wildCardCheckStack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
									iter.remove();
									final CreativeTabs creativeTab = wildCardCheckStack.getItem().getCreativeTab();
									if (creativeTab != null) {
										NonNullList<ItemStack> secondList = NonNullList.create();
										wildCardCheckStack.getItem().getSubItems(creativeTab, secondList);
										list.addAll(secondList);
									}
									iter = list.iterator();
								}
							}
							stacks[slot] = list.toArray(new ItemStack[0]);
							if (stacks[slot].length > 1) {
								hasCanidates = true;
							} else if (stacks[slot].length == 1) {
								stackList.set(slot, stacks[slot][0]);
							}
						}
					}
				}

				if (hasCanidates) {
					gui.setSubGui(new GuiRecipeImport(tile, stacks));
				} else {
					MainProxy.sendPacketToServer(packet.setStackList(stackList).setTilePos(tile));
				}
			}
			return null;
		}
		return recipeTransferHandlerHelper.createInternalError();
	}
}
