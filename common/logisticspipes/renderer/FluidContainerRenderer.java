package logisticspipes.renderer;

import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.MinecraftColor;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.fluids.FluidStack;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class FluidContainerRenderer implements IItemRenderer {

	private final EntityItem dummyEntityItem = new EntityItem(null);
	private boolean useThis = true;
	private RenderItem renderItem;
	public static boolean skipNext;

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		if (useThis && item != null) {
			if (item.getItem() instanceof LogisticsFluidContainer) {
				switch (type) {
					case ENTITY:
						return true;
					case INVENTORY:
						return true;
					default:
						return false;
				}
			}
			if (item.getItem() instanceof LogisticsItemCard) {
				switch (type) {
					case INVENTORY:
						return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
					default:
						return false;
				}
			}
		}
		return false;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		if (FluidContainerRenderer.skipNext) {
			FluidContainerRenderer.skipNext = false;
			return;
		}
		GL11.glPushMatrix();
		Minecraft mc = FMLClientHandler.instance().getClient();
		if (item.getItem() instanceof LogisticsFluidContainer) {
			FluidStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(ItemIdentifierStack.getFromStack(item));
			if ((type != ItemRenderType.INVENTORY && type != ItemRenderType.ENTITY) || liquid == null) {
				doRenderItem(item, mc, type, data);
				GL11.glPopMatrix();
				return;
			}
			doRenderFluid(liquid, mc, type, data);
			doRenderItem(item, mc, type, data);
		} else if (item.getItem() instanceof LogisticsItemCard) {
			doRenderItem(item, mc, type, data);
			NBTTagCompound nbt = item.getTagCompound();
			if (nbt == null || !nbt.hasKey("colors")) {
				GL11.glPopMatrix();
				return;
			}
			NBTTagCompound colors = nbt.getCompoundTag("colors");
			if (colors == null) {
				GL11.glPopMatrix();
				return;
			}
			if (type == ItemRenderType.ENTITY) {
				GL11.glScaled(0.07, 0.07, 1);
				GL11.glTranslated(-3, 3.5, -0.025);
			}
			for (int i = 0; i < 6; i++) {
				int colorCode = colors.getInteger("color:" + i);
				MinecraftColor color = MinecraftColor.values()[colorCode];
				int x = i / 3;
				int y = i % 3;
				if (type == ItemRenderType.INVENTORY) {
					Gui.drawRect(x * 5 + 4, y * 3 + 3, x * 5 + 7, y * 3 + 5, color.getColorCode());
				}
				if (type == ItemRenderType.ENTITY) {
					Gui.drawRect(-x * 5 + 4, -y * 3 + 3, -x * 5 + 7, -y * 3 + 5, color.getColorCode());
					GL11.glRotatef(180, 1, 0, 0);
					Gui.drawRect(-x * 5 + 4, -y * 3 + 3, -x * 5 + 7, -y * 3 + 5, color.getColorCode());
					GL11.glRotatef(180, 1, 0, 0);
				}
			}
		}
		GL11.glPopMatrix();
	}

	public void doRenderFluid(FluidStack liquid, Minecraft mc, ItemRenderType type, Object[] data) {
		GL11.glPushMatrix();
		if (type == ItemRenderType.ENTITY) {
			GL11.glRotatef(((((EntityItem) data[1]).age) / 20.0F + ((EntityItem) data[1]).hoverStart) * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
			GL11.glScaled(0.063, 0.065, 1);
			GL11.glTranslated(-8, -4, -0.02);
		}
		GL11.glDisable(GL11.GL_LIGHTING);
		ResourceLocation resourcelocation = mc.renderEngine.getResourceLocation(liquid.getFluid().getSpriteNumber());
		mc.renderEngine.bindTexture(resourcelocation);

		int i1 = liquid.getFluid().getColor();
		float f = (i1 >> 16 & 255) / 255.0F;
		float f1 = (i1 >> 8 & 255) / 255.0F;
		float f2 = (i1 & 255) / 255.0F;

		GL11.glColor4f(f, f1, f2, 1.0F);

		IIcon icon = liquid.getFluid().getIcon();
		if (icon != null) {
			renderIcon(5, 2, icon, 6, 12, 0);
			if (type == ItemRenderType.ENTITY) {
				GL11.glPopMatrix();
				GL11.glPushMatrix();
				GL11.glRotatef(((((EntityItem) data[1]).age) / 20.0F + ((EntityItem) data[1]).hoverStart) * (180F / (float) Math.PI) + 180, 0.0F, 1.0F, 0.0F);
				GL11.glScaled(0.063, 0.065, 1);
				GL11.glTranslated(-8, -4, -0.042);
				renderIcon(5, 2, icon, 6, 12, 0);
			}
		}
		GL11.glEnable(GL11.GL_LIGHTING);

		GL11.glPopMatrix();
	}

	public void renderIcon(int x, int y, IIcon par3Icon, int width, int height, double zLevel) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x + 0, y + height, zLevel, par3Icon.getInterpolatedU(x), par3Icon.getInterpolatedV(y + height));
		tessellator.addVertexWithUV(x + width, y + height, zLevel, par3Icon.getInterpolatedU(x + width), par3Icon.getInterpolatedV(y + height));
		tessellator.addVertexWithUV(x + width, y + 0, zLevel, par3Icon.getInterpolatedU(x + width), par3Icon.getInterpolatedV(y));
		tessellator.addVertexWithUV(x + 0, y + 0, zLevel, par3Icon.getInterpolatedU(x), par3Icon.getInterpolatedV(y));
		tessellator.draw();
	}

	public void doRenderItem(ItemStack itemstack, Minecraft mc, ItemRenderType type, Object[] data) {
		useThis = false;
		if (renderItem == null) {
			renderItem = new RenderItem() {

				@Override
				public boolean shouldBob() {
					return false;
				};

				@Override
				public boolean shouldSpreadItems() {
					return false;
				};
			};
			renderItem.setRenderManager(RenderManager.instance);
		}
		if (type == ItemRenderType.INVENTORY) {
			renderItem.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, itemstack, 0, 0);
		} else {
			GL11.glPushMatrix();
			dummyEntityItem.setEntityItemStack(itemstack);
			dummyEntityItem.hoverStart = 0;
			GL11.glScalef(2F, 2F, 2F);
			GL11.glRotatef(((((EntityItem) data[1]).age) / 20.0F + ((EntityItem) data[1]).hoverStart) * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
			renderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);
			GL11.glPopMatrix();
		}
		useThis = true;
	}
}
