package logisticspipes.proxy.thaumcraft;

import java.util.LinkedList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemModule;
import logisticspipes.proxy.interfaces.IThaumCraftProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import org.lwjgl.opengl.GL11;

import thaumcraft.api.EnumTag;
import thaumcraft.api.ObjectTags;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.client.lib.UtilsFX;
import buildcraft.BuildCraftSilicon;
import cpw.mods.fml.client.FMLClientHandler;

public class ThaumCraftProxy implements IThaumCraftProxy {
	
	public ThaumCraftProxy() {
		try {
			Class<?> tcConfig = Class.forName("thaumcraft.common.Config");
			itemShard = (Item)tcConfig.getField("itemShard").get((Object)null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Shard meta:
	 * 0 = air
	 * 1 = fire 
	 * 2 = water 
	 * 3 = earth
	 * 4 = vis
	 * 5 = dull
	 */
	private Item itemShard;
	
	
	/**
	 * Renders the aspect icons for a given stack downwards starting at x, y.
	 * @param x The x coord of the screen.
	 * @param y The y coord of the screen.
	 * @param item The ItemStack to render aspects for.
	 * @param gui The Gui screen to render on.
	 */
	@Override
	public void renderAspectsDown(ItemStack item, int x, int y, GuiScreen gui) {
		GL11.glPushMatrix();
		Minecraft mc = FMLClientHandler.instance().getClient();
		ObjectTags tags = getTagsForStack(item);
		tags = ThaumcraftApiHelper.getBonusObjectTags(item, tags);
		if (tags != null) {
			int index = 0;
			for (EnumTag tag : tags.getAspectsSorted()) {
				int yPos = y + index * 18;
				UtilsFX.drawTag(mc, x, yPos, tag, tags.getAmount(tag), gui, true, false);
				index++;
			}
		}
		GL11.glPopMatrix();
	}

	/**
	 * Used to get an ObjectTags of all aspects inside a given stack.
	 * @param stack The stack to get ObjectTags for.
	 * @return ObjectTags containing all of the aspects for stack.
	 */
	@Override
	public ObjectTags getTagsForStack(ItemStack stack) {
		if (stack == null) return new ObjectTags();
		ObjectTags ot = ThaumcraftApiHelper.getObjectTags(stack);
		ot = ThaumcraftApiHelper.getBonusObjectTags(stack, ot);
		return ot;
	}

	/**
	 * Used to render a icon of an aspect at a give x and y on top of a given GuiScreen.
	 * @param etag The EnumTag (aspect) to render
	 * @param x
	 * @param y
	 * @param gui The gui to render on.
	 */
	@Override
	public void renderAspectAt(Object etag, int x, int y, GuiScreen gui) {
		if (!(etag instanceof EnumTag)) return;
		GL11.glPushMatrix();
		Minecraft mc = FMLClientHandler.instance().getClient();
		UtilsFX.drawTag(mc, x, y, (EnumTag)etag, 1, gui, true, false);
		GL11.glPopMatrix();
	}


	/**
	 * Used to render a rectangle of different aspects. Algorithm does top row 
	 * then the next row down, and so on. It will stop when it runs out of 
	 * aspects to render.
	 * @param etagIDs A list of aspect IDs in Integer list to render.
	 * @param x Starting coordinate, top left.
	 * @param y Starting coordinate, top left.
	 * @param legnth Int of number of columns.
	 * @param width Int of number of rows.
	 * @param gui The GuiScreen to render on.
	 */
	@Override
	public void renderAspectsInGrid(List<Integer> etagIDs, int x, int y, int legnth, int width, GuiScreen gui) {
		if (etagIDs.size() == 0) return;
		int xshift = x;
		int yshift = y;
		int currentListIndex = 0;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < legnth; j++) {
				renderAspectAt(EnumTag.get(etagIDs.get(currentListIndex)), xshift, yshift, gui);
				currentListIndex += 1;
				if(currentListIndex == etagIDs.size()) return;
				xshift += 18;
			}
			xshift = x;
			yshift += 18;
		}
	}

	/**
	 * Used to get a list of integers representing the IDs of all the aspects
	 * in the given ItemStack.  Returns null if object has no tags.
	 * @param stack The item to get tags for.
	 * @return An Integer list of aspect IDs.
	 */
	@Override
	public List<Integer> getListOfTagIDsForStack(ItemStack stack) {
		if (stack == null) return null;
		List<Integer> list = new LinkedList<Integer>();
		ObjectTags tags = getTagsForStack(stack);
		EnumTag[] tagArray = tags.getAspectsSorted();
		if (tagArray.length == 0 || tagArray == null) return null;
		for (int i = 0; i < tagArray.length; i++) {
			if (tagArray[i] == null) continue;
			int ID = tagArray[i].id;
			list.add(ID);
		}
		return list;
	}

	/**
	 * Used to get name for given aspect ID.
	 * @param id The id to get name for.
	 * @return String of the aspect name.
	 */
	@Override
	public String getNameForTagID(int id) {
		return EnumTag.get(id).name;
	}

	@Override
	public void addCraftingRecipes() {
		
		CraftingManager.getInstance().addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.THAUMICASPECTSINK), new Object[] { 
			"wGe", 
			"rBr", 
			"fra", 
			Character.valueOf('w'), new ItemStack(itemShard, 1, 2), 
			Character.valueOf('e'), new ItemStack(itemShard, 1, 3), 
			Character.valueOf('f'), new ItemStack(itemShard, 1, 1), 
			Character.valueOf('a'), new ItemStack(itemShard, 1, 0), 
			
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
	}
	
}
