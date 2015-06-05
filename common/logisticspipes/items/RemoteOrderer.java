package logisticspipes.items;

import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.RequestPipeDimension;
import logisticspipes.pipes.PipeItemsRemoteOrdererLogistics;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import net.minecraftforge.common.DimensionManager;

import org.lwjgl.input.Keyboard;

public class RemoteOrderer extends Item {

	final static IIcon[] _icons = new IIcon[17];

	@Override
	public void registerIcons(IIconRegister par1IIconRegister) {
		for (int i = 0; i < 17; i++) {
			RemoteOrderer._icons[i] = par1IIconRegister.registerIcon("logisticspipes:" + getUnlocalizedName().replace("item.", "") + "/" + i);
		}
	}

	@Override
	public boolean getShareTag() {
		return true;
	}

	@Override
	public IIcon getIconFromDamage(int par1) {
		if (par1 > 16) {
			par1 = 0;
		}
		return RemoteOrderer._icons[par1];
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean flag) {
		// Add special tooltip in tribute to DireWolf
		if (itemstack != null && itemstack.getItem() == LogisticsPipes.LogisticsRemoteOrderer) {
			if ((Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) && Configs.EASTER_EGGS) {
				list.add("a.k.a \"Requesting Tool\" - DW20");
			}
		}

		if (itemstack.hasTagCompound() && itemstack.stackTagCompound.hasKey("connectedPipe-x")) {
			list.add("\u00a77Has Remote Pipe");
		}

		super.addInformation(itemstack, player, list, flag);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		if (par1ItemStack == null) {
			return null;
		}
		if (!par1ItemStack.hasTagCompound()) {
			return par1ItemStack;
		}
		PipeItemsRemoteOrdererLogistics pipe = RemoteOrderer.getPipe(par1ItemStack);
		if (pipe != null) {
			if (MainProxy.isServer(par3EntityPlayer.worldObj)) {
				int energyUse = 0;
				if (pipe.getWorld() != par3EntityPlayer.worldObj) {
					energyUse += 2500;
				}
				energyUse += Math.sqrt(Math.pow(pipe.getX() - par3EntityPlayer.posX, 2) + Math.pow(pipe.getY() - par3EntityPlayer.posY, 2) + Math.pow(pipe.getZ() - par3EntityPlayer.posZ, 2));
				if (pipe.useEnergy(energyUse)) {
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(RequestPipeDimension.class).setInteger(MainProxy.getDimensionForWorld(pipe.getWorld())), par3EntityPlayer);
					par3EntityPlayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Orderer_ID, pipe.getWorld(), pipe.getX(), pipe.getY(), pipe.getZ());

				}
			}
		}
		return par1ItemStack;
	}

	public static void connectToPipe(ItemStack stack, PipeItemsRemoteOrdererLogistics pipe) {
		stack.stackTagCompound = new NBTTagCompound();
		stack.stackTagCompound.setInteger("connectedPipe-x", pipe.getX());
		stack.stackTagCompound.setInteger("connectedPipe-y", pipe.getY());
		stack.stackTagCompound.setInteger("connectedPipe-z", pipe.getZ());
		int dimension = 0;
		for (Integer dim : DimensionManager.getIDs()) {
			if (pipe.getWorld().equals(DimensionManager.getWorld(dim.intValue()))) {
				dimension = dim.intValue();
				break;
			}
		}
		stack.stackTagCompound.setInteger("connectedPipe-world-dim", dimension);
	}

	public static PipeItemsRemoteOrdererLogistics getPipe(ItemStack stack) {
		if (stack == null) {
			return null;
		}
		if (!stack.hasTagCompound()) {
			return null;
		}
		if (!stack.stackTagCompound.hasKey("connectedPipe-x") || !stack.stackTagCompound.hasKey("connectedPipe-y") || !stack.stackTagCompound.hasKey("connectedPipe-z")) {
			return null;
		}
		if (!stack.stackTagCompound.hasKey("connectedPipe-world-dim")) {
			return null;
		}
		int dim = stack.stackTagCompound.getInteger("connectedPipe-world-dim");
		World world = DimensionManager.getWorld(dim);
		if (world == null) {
			return null;
		}
		TileEntity tile = world.getTileEntity(stack.stackTagCompound.getInteger("connectedPipe-x"), stack.stackTagCompound.getInteger("connectedPipe-y"), stack.stackTagCompound.getInteger("connectedPipe-z"));
		if (!(tile instanceof LogisticsTileGenericPipe)) {
			return null;
		}
		CoreUnroutedPipe pipe = ((LogisticsTileGenericPipe) tile).pipe;
		if (pipe instanceof PipeItemsRemoteOrdererLogistics) {
			return (PipeItemsRemoteOrdererLogistics) pipe;
		}
		return null;
	}

	@Override
	public CreativeTabs getCreativeTab() {
		return CreativeTabs.tabTools;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		for (int i = 0; i < 17; i++) {
			par3List.add(new ItemStack(par1, 1, i));
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		return StringUtils.translate(getUnlocalizedName(itemstack));
	}
}
