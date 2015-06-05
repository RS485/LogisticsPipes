package logisticspipes.proxy.thaumcraft;

import java.util.LinkedList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.items.ItemModule;
import logisticspipes.items.ItemPipeComponents;
import logisticspipes.proxy.interfaces.ICraftingParts;
import logisticspipes.proxy.interfaces.IThaumCraftProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.research.ScanManager;

public class ThaumCraftProxy implements IThaumCraftProxy {

	public ThaumCraftProxy() throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Class<?> tcConfig = Class.forName("thaumcraft.common.config.ConfigItems");
		itemShard = (Item) tcConfig.getField("itemShard").get((Object) null);
	}

	/**
	 * Shard meta: 0 = air 1 = fire 2 = water 3 = earth 4 = vis 5 = dull
	 */
	private Item itemShard;

	/**
	 * Renders the aspect icons for a given stack downwards starting at x, y.
	 * 
	 * @param x
	 *            The x coord of the screen.
	 * @param y
	 *            The y coord of the screen.
	 * @param item
	 *            The ItemStack to render aspects for.
	 * @param gui
	 *            The Gui screen to render on.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void renderAspectsDown(ItemStack item, int x, int y, GuiScreen gui) {
		GL11.glPushMatrix();
		AspectList tags = getTagsForStack(item);
		tags = ThaumcraftApiHelper.getBonusObjectTags(item, tags);
		if (tags != null) {
			int index = 0;
			for (Aspect tag : tags.getAspectsSortedAmount()) {
				if (tag == null) {
					continue;
				}
				int yPos = y + index * 18;
				renderAspectAt(tag, x, yPos, gui, tags.getAmount(tag), true);
				index++;
			}
		}
		GL11.glPopMatrix();
	}

	/**
	 * Used to get an ObjectTags of all aspects inside a given stack.
	 * 
	 * @param stack
	 *            The stack to get ObjectTags for.
	 * @return ObjectTags containing all of the aspects for stack.
	 */
	private AspectList getTagsForStack(ItemStack stack) {
		if (stack == null) {
			return new AspectList();
		}
		AspectList ot = ThaumcraftApiHelper.getObjectAspects(stack);
		ot = ThaumcraftApiHelper.getBonusObjectTags(stack, ot);
		return ot;
	}

	/**
	 * Used to render a icon of an aspect at a give x and y on top of a given
	 * GuiScreen.
	 * 
	 * @param tag
	 *            The EnumTag (aspect) to render
	 * @param x
	 * @param y
	 * @param gui
	 *            The gui to render on.
	 */
	private void renderAspectAt(Aspect tag, int x, int y, GuiScreen gui, int amount, boolean drawBackground) {
		if (!(tag instanceof Aspect)) {
			return;
		}
		Minecraft mc = FMLClientHandler.instance().getClient();
		if (drawBackground) {
			UtilsFX.bindTexture("textures/aspects/_back.png");
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTranslated(x - 2, y - 2, 0.0D);
			GL11.glScaled(1.25D, 1.25D, 0.0D);
			UtilsFX.drawTexturedQuadFull(0, 0, gui.zLevel);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glPopMatrix();
		}
		if (Thaumcraft.proxy.playerKnowledge.hasDiscoveredAspect(mc.thePlayer.getDisplayName(), tag)) {
			UtilsFX.drawTag(x, y, tag, amount, 0, gui.zLevel);
		} else {
			UtilsFX.bindTexture("textures/aspects/_unknown.png");
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTranslated(x, y, 0.0D);
			UtilsFX.drawTexturedQuadFull(0, 0, gui.zLevel);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glPopMatrix();
		}
	}

	/**
	 * Used to render a rectangle of different aspects. Algorithm does top row
	 * then the next row down, and so on. It will stop when it runs out of
	 * aspects to render.
	 * 
	 * @param etagIDs
	 *            A list of aspect IDs in Integer list to render.
	 * @param x
	 *            Starting coordinate, top left.
	 * @param y
	 *            Starting coordinate, top left.
	 * @param legnth
	 *            Int of number of columns.
	 * @param width
	 *            Int of number of rows.
	 * @param gui
	 *            The GuiScreen to render on.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void renderAspectsInGrid(List<String> etagIDs, int x, int y, int legnth, int width, GuiScreen gui) {
		if (etagIDs.size() == 0) {
			return;
		}
		int xshift = x;
		int yshift = y;
		int currentListIndex = 0;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < legnth; j++) {
				renderAspectAt(Aspect.getAspect(etagIDs.get(currentListIndex)), xshift, yshift, gui, 0, false);
				currentListIndex += 1;
				if (currentListIndex == etagIDs.size()) {
					return;
				}
				xshift += 18;
			}
			xshift = x;
			yshift += 18;
		}
	}

	/**
	 * Used to get a list of integers representing the IDs of all the aspects in
	 * the given ItemStack. Returns null if object has no tags.
	 * 
	 * @param stack
	 *            The item to get tags for.
	 * @return An Integer list of aspect IDs.
	 */
	@Override
	public List<String> getListOfTagsForStack(ItemStack stack) {
		if (stack == null) {
			return null;
		}
		List<String> list = new LinkedList<String>();
		AspectList tags = getTagsForStack(stack);
		Aspect[] tagArray = tags.getAspectsSorted();
		if (tagArray.length == 0 || tagArray == null) {
			return null;
		}
		for (Aspect element : tagArray) {
			if (element == null) {
				continue;
			}
			list.add(element.getTag());
		}
		return list;
	}

	@Override
	public void addCraftingRecipes(ICraftingParts parts) {
		if (!Configs.ENABLE_BETA_RECIPES) {
			CraftingManager.getInstance().addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.THAUMICASPECTSINK),
					new Object[] { "wGe", "rBr", "fra", Character.valueOf('w'), new ItemStack(itemShard, 1, 2), Character.valueOf('e'), new ItemStack(itemShard, 1, 3), Character.valueOf('f'), new ItemStack(itemShard, 1, 1), Character.valueOf('a'), new ItemStack(itemShard, 1, 0),

					Character.valueOf('G'), parts.getChipTear1(), Character.valueOf('r'), Items.redstone, Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK) });
		}
		if (Configs.ENABLE_BETA_RECIPES) {
			CraftingManager.getInstance().addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.THAUMICASPECTSINK),
					new Object[] { "wGe", "rBr", "fra", Character.valueOf('w'), new ItemStack(itemShard, 1, 2), Character.valueOf('e'), new ItemStack(itemShard, 1, 3), Character.valueOf('f'), new ItemStack(itemShard, 1, 1), Character.valueOf('a'), new ItemStack(itemShard, 1, 0),

					Character.valueOf('G'), new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_MICROPACKAGER), Character.valueOf('r'), Items.redstone, Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK) });
		}
	}

	@Override
	public boolean isScannedObject(ItemStack stack, String playerName) {
		int h = ScanManager.generateItemHash(stack.getItem(), stack.getItemDamage());
		List<String> list = Thaumcraft.proxy.getScannedObjects().get(playerName);
		return (list != null) && (list.contains("@" + h) || list.contains("#" + h));
	}
}
