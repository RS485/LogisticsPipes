package logisticspipes.items;

import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.pipes.PipeItemsRemoteOrdererLogistics;
import logisticspipes.proxy.MainProxy;
import logisticspipes.textures.Textures;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import org.lwjgl.input.Keyboard;

import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;
import net.minecraft.client.renderer.texture.IconRegister;

public class RemoteOrderer extends Item {
	final static Icon[] icons = new Icon[17];
	
	public RemoteOrderer(int id) {
		super(id);
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		LogisticsPipes.remoteOrdererIconProvider.registerIcons(par1IconRegister);
	}

	@Override
	public boolean getShareTag() {
        return true;
    }
    

	@Override
	public Icon getIconFromDamage(int par1) {
		return LogisticsPipes.remoteOrdererIconProvider.getIcon(par1);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean flag) {
		//Add special tooltip in tribute to DireWolf
		if (itemstack != null && itemstack.itemID == LogisticsPipes.LogisticsRemoteOrderer.itemID){
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)){
				list.add("a.k.a \"Requesting Tool\" - DW20");
			}
		}
		
		if(itemstack.hasTagCompound() && itemstack.stackTagCompound.hasKey("connectedPipe-x")) {
			list.add("\u00a77Has Remote Pipe");
		}
		
		super.addInformation(itemstack, player, list, flag);
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {	
		if(par1ItemStack == null) {
			return null;
    	}
		if(!par1ItemStack.hasTagCompound()) {
			return par1ItemStack;
    	}
		PipeItemsRemoteOrdererLogistics pipe = getPipe(par1ItemStack);
		if(pipe != null) {
			if(MainProxy.isServer(par3EntityPlayer.worldObj)) {
				MainProxy.sendPacketToPlayer(new PacketInteger(NetworkConstants.REQUEST_GUI_DIMENSION, MainProxy.getDimensionForWorld(pipe.worldObj)).getPacket(), (Player)par3EntityPlayer);
				par3EntityPlayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Orderer_ID, pipe.worldObj, pipe.xCoord, pipe.yCoord, pipe.zCoord);
			}
		}
		return par1ItemStack;
    }
	
	public static void connectToPipe(ItemStack stack, PipeItemsRemoteOrdererLogistics pipe) {
		stack.stackTagCompound = new NBTTagCompound();
		stack.stackTagCompound.setInteger("connectedPipe-x", pipe.xCoord);
		stack.stackTagCompound.setInteger("connectedPipe-y", pipe.yCoord);
		stack.stackTagCompound.setInteger("connectedPipe-z", pipe.zCoord);
		int dimension = 0;
		for(Integer dim:DimensionManager.getIDs()) {
			if(pipe.worldObj.equals(DimensionManager.getWorld(dim.intValue()))) {
				dimension = dim.intValue();
				break;
			}
		}
		stack.stackTagCompound.setInteger("connectedPipe-world-dim", dimension);
	}
	
	public static PipeItemsRemoteOrdererLogistics getPipe(ItemStack stack) {
		if(stack == null) {
			return null;
    	}
		if(!stack.hasTagCompound()) {
			return null;
    	}
		if(!stack.stackTagCompound.hasKey("connectedPipe-x") || !stack.stackTagCompound.hasKey("connectedPipe-y") || !stack.stackTagCompound.hasKey("connectedPipe-z")) {
			return null;
		}
		if(!stack.stackTagCompound.hasKey("connectedPipe-world-dim")) {
			return null;
		}
		int dim = stack.stackTagCompound.getInteger("connectedPipe-world-dim");
		World world = DimensionManager.getWorld(dim);
		if(world == null) {
			return null;
		}
		TileEntity tile = world.getBlockTileEntity(stack.stackTagCompound.getInteger("connectedPipe-x"), stack.stackTagCompound.getInteger("connectedPipe-y"), stack.stackTagCompound.getInteger("connectedPipe-z"));
		if(!(tile instanceof TileGenericPipe)) {
			return null;
		}
		Pipe pipe = ((TileGenericPipe)tile).pipe;
		if(pipe instanceof PipeItemsRemoteOrdererLogistics) {
			return (PipeItemsRemoteOrdererLogistics)pipe;
		}
		return null;
	}

	@Override
	public CreativeTabs getCreativeTab()
    {
        return CreativeTabs.tabTools;
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) {
		for(int i=0;i<17;i++) {
			par3List.add(new ItemStack(par1, 1, i));
		}
    }
}
