package logisticspipes.pipes.signs;

import java.util.List;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.cpipe.CPipeSatelliteImportBack;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.utils.item.ItemIdentifierStack;

import lombok.Data;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

public class CraftingPipeSign implements IPipeSign {

	@Data
	private static class CraftingPipeSignData implements IPipeSignData {
		private final ItemIdentifierStack item;
		private final int satID;

		@Override
		@SideOnly(Side.CLIENT)
		public boolean isListCompatible(LogisticsRenderPipe render) {
			return item == null || item.getItem().isRenderListCompatible(render);
		}
	}

	public CoreRoutedPipe pipe;
	public ForgeDirection dir;

	@Override
	public boolean isAllowedFor(CoreRoutedPipe pipe) {
		return pipe instanceof PipeItemsCraftingLogistics;
	}

	@Override
	public void addSignTo(CoreRoutedPipe pipe, ForgeDirection dir, EntityPlayer player) {
		pipe.addPipeSign(dir, new CraftingPipeSign(), player);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {}

	@Override
	public void writeToNBT(NBTTagCompound tag) {}

	@Override
	public ModernPacket getPacket() {
		PipeItemsCraftingLogistics cpipe = (PipeItemsCraftingLogistics) pipe;
		return PacketHandler.getPacket(CPipeSatelliteImportBack.class).setInventory(cpipe.getDummyInventory()).setType(ModulePositionType.IN_PIPE).setPosX(cpipe.getX()).setPosY(cpipe.getY()).setPosZ(cpipe.getZ());
	}

	@Override
	public void updateServerSide() {}

	@Override
	public void init(CoreRoutedPipe pipe, ForgeDirection dir) {
		this.pipe = pipe;
		this.dir = dir;
	}

	@Override
	public void activate(EntityPlayer player) {}

	@Override
	@SideOnly(Side.CLIENT)
	public void render(CoreRoutedPipe pipe, LogisticsRenderPipe renderer) {
		PipeItemsCraftingLogistics cpipe = (PipeItemsCraftingLogistics) pipe;
		FontRenderer var17 = renderer.func_147498_b();
		if (cpipe != null) {
			List<ItemIdentifierStack> craftables = cpipe.getCraftedItems();

			String name = "";
			if (craftables != null && craftables.size() > 0) {
				ItemStack itemstack = craftables.get(0).unsafeMakeNormalStack();

				renderer.renderItemStackOnSign(itemstack);
				Item item = itemstack.getItem();

				GL11.glDepthMask(false);
				GL11.glRotatef(-180.0F, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0.5F, +0.08F, 0.0F);
				GL11.glScalef(1.0F / 90.0F, 1.0F / 90.0F, 1.0F / 90.0F);

				try {
					name = item.getItemStackDisplayName(itemstack);
				} catch (Exception e) {
					try {
						name = item.getUnlocalizedName();
					} catch (Exception e1) {}
				}

				var17.drawString("ID: " + String.valueOf(Item.getIdFromItem(item)), -var17.getStringWidth("ID: " + String.valueOf(Item.getIdFromItem(item))) / 2, 0 * 10 - 4 * 5, 0);
				ModuleCrafter logisticsMod = cpipe.getLogisticsModule();
				if (logisticsMod.satelliteId != 0) {
					var17.drawString("Sat ID: " + String.valueOf(logisticsMod.satelliteId), -var17.getStringWidth("Sat ID: " + String.valueOf(logisticsMod.satelliteId)) / 2, 1 * 10 - 4 * 5, 0);
				}
			} else {
				GL11.glRotatef(-180.0F, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0.5F, +0.08F, 0.0F);
				GL11.glScalef(1.0F / 90.0F, 1.0F / 90.0F, 1.0F / 90.0F);
				name = "Empty";
			}

			name = renderer.cut(name, var17);

			var17.drawString(name, -var17.getStringWidth(name) / 2 - 15, 3 * 10 - 4 * 5, 0);

			GL11.glDepthMask(true);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	@Override
	public IPipeSignData getRenderData(CoreRoutedPipe pipe) {
		PipeItemsCraftingLogistics cpipe = (PipeItemsCraftingLogistics) pipe;
		if (cpipe != null) {
			List<ItemIdentifierStack> craftables = cpipe.getCraftedItems();
			if (craftables != null && craftables.size() > 0) {
				ItemIdentifierStack itemIdentifierStack = craftables.get(0);
				ModuleCrafter logisticsMod = cpipe.getLogisticsModule();
				return new CraftingPipeSignData(itemIdentifierStack, logisticsMod.satelliteId);
			} else {
				return new CraftingPipeSignData(null, -1);
			}
		}
		return null;
	}
}
