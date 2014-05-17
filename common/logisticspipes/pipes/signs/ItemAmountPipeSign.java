package logisticspipes.pipes.signs;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiHandler;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.gui.GuiArgument;
import logisticspipes.network.packets.pipe.ItemAmountSignUpdatePacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.string.StringUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemAmountPipeSign implements IPipeSign, ISimpleInventoryEventHandler {
	
	public ItemIdentifierInventory	itemTypeInv	= new ItemIdentifierInventory(1, "", 1);
	public int						amount		= 100;
	public CoreRoutedPipe pipe;
	public ForgeDirection dir;
	
	public ItemAmountPipeSign() {
		itemTypeInv.addListener(this);
	}
	
	@Override
	public boolean isAllowedFor(CoreRoutedPipe pipe) {
		return true;
	}
	
	@Override
	public void addSignTo(CoreRoutedPipe pipe, ForgeDirection dir, EntityPlayer player) {
		pipe.addPipeSign(dir, new ItemAmountPipeSign(), player);
		openGUI(pipe, dir, player);
	}
	
	private void openGUI(CoreRoutedPipe pipe, ForgeDirection dir, EntityPlayer player) {
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(GuiArgument.class).setGuiID(GuiIDs.GUI_Item_Amount_Sign).setArgs(dir), (Player)player);
		GuiHandler.argumentQueueServer.put(GuiIDs.GUI_Item_Amount_Sign, new Object[] { dir });
		player.openGui(LogisticsPipes.instance, GuiIDs.GUI_Item_Amount_Sign, player.worldObj, pipe.getX(), pipe.getY(), pipe.getZ());
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		itemTypeInv.readFromNBT(tag);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		itemTypeInv.writeToNBT(tag);
	}
	
	@Override
	public ModernPacket getPacket() {
		return PacketHandler.getPacket(ItemAmountSignUpdatePacket.class).setStack(itemTypeInv.getIDStackInSlot(0)).setInteger2(amount).setInteger(dir.ordinal()).setTilePos(pipe.container);
	}
	
	@Override
	public void updateServerSide() {
		if(pipe.getWorld().getTotalWorldTime() % 5 != 0) return;
		int newAmount = 0;
		if(itemTypeInv.getIDStackInSlot(0) != null) {
			newAmount = SimpleServiceLocator.logisticsManager.getAmountFor(itemTypeInv.getIDStackInSlot(0).getItem(), pipe.getRouter().getIRoutersByCost());
		}
		if(newAmount != amount) {
			amount = newAmount;
			sendUpdatePacket();
		}
	}

	@Override
	public void activate(EntityPlayer player) {
		openGUI(pipe, dir, player);
	}

	@Override
	public void init(CoreRoutedPipe pipe, ForgeDirection dir) {
		this.pipe = pipe;
		this.dir = dir;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void render(CoreRoutedPipe pipe, LogisticsRenderPipe renderer) {
		FontRenderer var17 = renderer.getFontRenderer();
		if(pipe != null) {
			String name = "";
			if(itemTypeInv != null && itemTypeInv.getIDStackInSlot(0) != null) {
				ItemStack itemstack = itemTypeInv.getIDStackInSlot(0).unsafeMakeNormalStack();
				
				renderer.renderItemStackOnSign(itemstack);
				Item item = itemstack.getItem();
				
				GL11.glDepthMask(false);
				GL11.glRotatef(-180.0F, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0.5F, +0.08F, 0.0F);
				GL11.glScalef(1.0F / 90.0F, 1.0F / 90.0F, 1.0F / 90.0F);
				
				try {
					name = item.getItemDisplayName(itemstack);
				} catch(Exception e) {
					try {
						name = item.getUnlocalizedName();
					} catch(Exception e1) {}
				}
				
				var17.drawString("ID: " + String.valueOf(item.itemID), -var17.getStringWidth("ID: " + String.valueOf(item.itemID)) / 2, 0 * 10 - 4 * 5, 0);
				String displayAmount = StringUtil.getFormatedStackSize(amount);
				var17.drawString("Amount:", -var17.getStringWidth("Amount:") / 2, 1 * 10 - 4 * 5, 0);
				var17.drawString(String.valueOf(displayAmount), -var17.getStringWidth(String.valueOf(displayAmount)) / 2, 2 * 10 - 4 * 5, 0);
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
	public void InventoryChanged(IInventory inventory) {
		if(inventory == itemTypeInv) {
			sendUpdatePacket();
		}
	}
	
	private void sendUpdatePacket() {
		MainProxy.sendPacketToAllWatchingChunk(pipe.getX(), pipe.getZ(), MainProxy.getDimensionForWorld(pipe.getWorld()), getPacket());
	}
}
