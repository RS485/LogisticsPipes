package logisticspipes.renderer;

import logisticspipes.items.LogisticsItemCard;
import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FluidContainerRenderer implements IItemRenderer {

	private final EntityItem dummyEntityItem = new EntityItem(null);
	private boolean useThis = true;
	private RenderItem renderItem;

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		if(useThis && item != null) {
			if (item.getItem() instanceof LogisticsFluidContainer) {
				switch(type) {
				case ENTITY:
					return true;
				case INVENTORY:
					return true;
				default:
					return false;
				}
			}
			if(item.getItem() instanceof LogisticsItemCard) {
				switch(type) {
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
		GL11.glPushMatrix();
		Minecraft mc = FMLClientHandler.instance().getClient();
		if (item.getItem() instanceof LogisticsFluidContainer) {
			FluidStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(item);
			if (liquid == null) {
				doRenderItem(item, mc, type, data);
				GL11.glPopMatrix();
				return;
			}
			//FIXME
			/*
			ItemStack liquidItem = liquid.asItemStack();
			GL11.glPushMatrix();
			if(type == ItemRenderType.INVENTORY) {
				GL11.glScaled(0.45, 0.75, 1);
				GL11.glTranslated(10, 2.5, 0);
			} else {
				GL11.glScaled(0.45, 0.75, 0.45);
				GL11.glTranslated(0, 0.09, 0);
			}
			doRenderItem(liquidItem, mc, type, data);
			GL11.glPopMatrix();*/
			doRenderItem(item, mc, type, data);
		} else if(item.getItem() instanceof LogisticsItemCard) {
			doRenderItem(item, mc, type, data);
			NBTTagCompound nbt = item.getTagCompound();
			if(nbt == null || !nbt.hasKey("colors")) {
				GL11.glPopMatrix();
				return;
			}
			NBTTagCompound colors = nbt.getCompoundTag("colors");
			if(colors == null) {
				GL11.glPopMatrix();
				return;
			}
			if(type == ItemRenderType.ENTITY) {
				GL11.glScaled(0.07, 0.07, 1);
				GL11.glTranslated(-3, 3.5, -0.025);
			}
			for(int i=0;i<6;i++) {
				int colorCode = colors.getInteger("color:" + i);
				Colors color = Colors.values()[colorCode];
				int x = i / 3;
				int y = i % 3;
				if(type == ItemRenderType.INVENTORY) {
					drawRect(x * 5 + 4, y * 3 + 3, x * 5 + 7, y * 3 + 5, color.getColorCode());
				}
				if(type == ItemRenderType.ENTITY) {
					drawRect(-x * 5 + 4, -y * 3 + 3, -x * 5 + 7, -y * 3 + 5, color.getColorCode());
					GL11.glRotatef(180, 1, 0, 0);
					drawRect(-x * 5 + 4, -y * 3 + 3, -x * 5 + 7, -y * 3 + 5, color.getColorCode());
					GL11.glRotatef(180, 1, 0, 0);
				}
			}
		}
		GL11.glPopMatrix();
	}

	public void doRenderItem(ItemStack itemstack, Minecraft mc, ItemRenderType type, Object[] data) {
		useThis = false;
		if (renderItem == null) {
			renderItem = new RenderItem() {
				public boolean shouldBob() {
					return false;
				};

				public boolean shouldSpreadItems() {
					return false;
				};
			};
			renderItem.setRenderManager(RenderManager.instance);
		}
		if(type == ItemRenderType.INVENTORY) {
			renderItem.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, itemstack, 0, 0);
		} else {
			GL11.glPushMatrix();
			dummyEntityItem.setEntityItemStack(itemstack);
			dummyEntityItem.hoverStart = 0;
            GL11.glScalef(2F, 2F, 2F);
            GL11.glRotatef(((((EntityItem)data[1]).age) / 20.0F + ((EntityItem)data[1]).hoverStart) * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
			renderItem.doRenderItem(dummyEntityItem, 0, 0, 0, 0, 0);
			GL11.glPopMatrix();
		}
		useThis = true;
	}
	
	private void drawRect(int par0, int par1, int par2, int par3, int par4) {
        int j1;

        if (par0 < par2)
        {
            j1 = par0;
            par0 = par2;
            par2 = j1;
        }

        if (par1 < par3)
        {
            j1 = par1;
            par1 = par3;
            par3 = j1;
        }

        float f = (par4 >> 24 & 255) / 255.0F;
        float f1 = (par4 >> 16 & 255) / 255.0F;
        float f2 = (par4 >> 8 & 255) / 255.0F;
        float f3 = (par4 & 255) / 255.0F;
        Tessellator tessellator = Tessellator.instance;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(f1, f2, f3, f);
        tessellator.startDrawingQuads();
        tessellator.addVertex(par0, par3, 0.0D);
        tessellator.addVertex(par2, par3, 0.0D);
        tessellator.addVertex(par2, par1, 0.0D);
        tessellator.addVertex(par0, par1, 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
