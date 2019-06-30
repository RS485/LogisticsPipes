package logisticspipes.pipes.signs;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import logisticspipes.modules.LogisticsModule.ModulePositionType;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.cpipe.CPipeSatelliteImportBack;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.utils.item.ItemIdentifierStack;

public class CraftingPipeSign implements IPipeSign {

	public CoreRoutedPipe pipe;
	public EnumFacing dir;

	private Object fbo;
	private ItemIdentifierStack oldRenderedStack = null;
	private String oldSatelliteName = "";

	@Override
	public boolean isAllowedFor(CoreRoutedPipe pipe) {
		return pipe instanceof PipeItemsCraftingLogistics;
	}

	@Override
	public void addSignTo(CoreRoutedPipe pipe, EnumFacing dir, EntityPlayer player) {
		pipe.addPipeSign(dir, new CraftingPipeSign(), player);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {}

	@Override
	public void writeToNBT(NBTTagCompound tag) {}

	@Override
	public ModernPacket getPacket() {
		PipeItemsCraftingLogistics cpipe = (PipeItemsCraftingLogistics) pipe;
		return PacketHandler.getPacket(CPipeSatelliteImportBack.class)
				.setInventory(cpipe.getDummyInventory())
				.setType(ModulePositionType.IN_PIPE)
				.setPosX(cpipe.getX())
				.setPosY(cpipe.getY())
				.setPosZ(cpipe.getZ());
	}

	@Override
	public void updateServerSide() {}

	@Override
	public void init(CoreRoutedPipe pipe, EnumFacing dir) {
		this.pipe = pipe;
		this.dir = dir;
	}

	@Override
	public void activate(EntityPlayer player) {}

	@Override
	@SideOnly(Side.CLIENT)
	public void render(CoreRoutedPipe pipe, LogisticsRenderPipe renderer) {
		PipeItemsCraftingLogistics cpipe = (PipeItemsCraftingLogistics) pipe;
		FontRenderer var17 = renderer.getFontRenderer();
		oldRenderedStack = null;
		if (cpipe != null) {
			List<ItemIdentifierStack> craftables = cpipe.getCraftedItems();

			String name = "";
			if (craftables != null && craftables.size() > 0) {
				ItemIdentifierStack itemstack = craftables.get(0);
				oldRenderedStack = itemstack;

				GlStateManager.depthMask(false);
				GlStateManager.depthMask(true);
				renderer.renderItemStackOnSign(itemstack.unsafeMakeNormalStack());
				Item item = itemstack.getItem().item;

				GlStateManager.depthMask(false);
				GL11.glRotatef(-180.0F, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0.5F, +0.08F, 0.0F);
				GL11.glScalef(1.0F / 90.0F, 1.0F / 90.0F, 1.0F / 90.0F);

				try {
					name = item.getItemStackDisplayName(itemstack.unsafeMakeNormalStack());
				} catch (Exception e) {
					try {
						name = item.getUnlocalizedName();
					} catch (Exception ignored) {}
				}

				var17.drawString(String.format("ID: %d", Item.getIdFromItem(item)), -var17.getStringWidth(String.format("ID: %d", Item.getIdFromItem(item))) / 2, 0 * 10 - 4 * 5, 0);
				ModuleCrafter logisticsMod = cpipe.getLogisticsModule();
				oldSatelliteName = logisticsMod.clientSideSatelliteNames.satelliteName;
				if (!oldSatelliteName.isEmpty()) {
					var17.drawString("Sat: " + oldSatelliteName, -var17.getStringWidth("Sat: " + oldSatelliteName) / 2, 1 * 10 - 4 * 5, 0);
				}
			} else {
				GL11.glRotatef(-180.0F, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0.5F, +0.08F, 0.0F);
				GL11.glScalef(1.0F / 90.0F, 1.0F / 90.0F, 1.0F / 90.0F);
				name = "Empty";
			}

			name = renderer.cut(name, var17);

			var17.drawString(name, -var17.getStringWidth(name) / 2 - 15, 3 * 10 - 4 * 5, 0);

			GlStateManager.depthMask(true);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Framebuffer getMCFrameBufferForSign() {
		if(!OpenGlHelper.isFramebufferEnabled()) {
			return null;
		}
		if(fbo == null) {
			fbo = new Framebuffer(128, 128, true);
		}
		return (Framebuffer) fbo;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean doesFrameBufferNeedUpdating(CoreRoutedPipe pipe, LogisticsRenderPipe renderer) {
		ItemIdentifierStack itemstack = getItemIdentifierStack((PipeItemsCraftingLogistics) pipe);
		if (itemstack != null && oldRenderedStack != null) {
			return fbo == null || !oldRenderedStack.equals(itemstack);
		} else if (itemstack == null && oldRenderedStack == null) {
			return fbo == null;
		} else {
			return true;
		}
	}

	@Nullable
	private ItemIdentifierStack getItemIdentifierStack(PipeItemsCraftingLogistics cpipe) {
		if(cpipe == null) return null;
		List<ItemIdentifierStack> craftables = cpipe.getCraftedItems();
		ItemIdentifierStack itemstack = null;
		if (craftables != null && craftables.size() > 0) {
			itemstack = craftables.get(0);
		}
		return itemstack;
	}
}
