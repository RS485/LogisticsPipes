package logisticspipes.items;

import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.pipes.PipeItemsRemoteOrdererLogistics;
import logisticspipes.proxy.MainProxy;
import logisticspipes.textures.Textures;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.DimensionManager;

import org.lwjgl.input.Keyboard;

import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

public class RemoteOrderer extends Item {
	
	public RemoteOrderer(int id) {
		super(id);
	}

	@Override
	public String getTextureFile() {
		return Textures.LOGISTICSITEMS_TEXTURE_FILE;
	}

	@Override
	public boolean getShareTag() {
        return true;
    }
    

	@Override
	public int getIconFromDamage(int par1) {
    	return Textures.LOGISTICSREMOTEORDERER_ICONINDEX;
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean flag) {
		//Add special tooltip in tribute to DireWolf
		if (itemstack != null && itemstack.itemID == LogisticsPipes.LogisticsRemoteOrderer.shiftedIndex){
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)){
				list.add("a.k.a \"Requesting Tool\" - DW20");
			}
		}
		
		if(itemstack.hasTagCompound() && itemstack.stackTagCompound.hasKey("connectedPipe-x")) {
			list.add("\u00a77Has Remote Pipe");
		}
		
		super.addInformation(itemstack, player, list, flag);
	}
	
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
			return par1ItemStack.copy();
		} else {
			return par1ItemStack;
		}
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
		World world = MainProxy.getWorld(dim);
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
}
