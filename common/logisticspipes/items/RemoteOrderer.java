package logisticspipes.items;

import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.RequestPipeDimension;
import logisticspipes.pipes.PipeItemsRemoteOrdererLogistics;
import logisticspipes.proxy.MainProxy;
import net.minecraft.client.renderer.texture.IconRegister;
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

public class RemoteOrderer extends Item {
	final static Icon[] _icons = new Icon[17];
	
	public RemoteOrderer(int id) {
		super(id);
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		for(int i=0;i<17;i++)
		{
			_icons[i]=par1IconRegister.registerIcon("logisticspipes:"+getUnlocalizedName().replace("item.", "")+"/"+i);
		}
	}

	@Override
	public boolean getShareTag() {
        return true;
    }
    

	@Override
	public Icon getIconFromDamage(int par1) {
		if(par1>16)
			par1=0;
		return _icons[par1];
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
				int energyUse=0;
				if(pipe.getWorld() != par3EntityPlayer.worldObj)
					energyUse += 500;
				energyUse += Math.abs(pipe.getX()-par3EntityPlayer.posX) + Math.abs(pipe.getY()-par3EntityPlayer.posY) + Math.abs(pipe.getZ()-par3EntityPlayer.posZ);
				energyUse *= 5; // x5 converts from lp to mj energy cost.
				if(pipe.useEnergy(energyUse)) { 
//TODO 			MainProxy.sendPacketToPlayer(new PacketInteger(NetworkConstants.REQUEST_GUI_DIMENSION, MainProxy.getDimensionForWorld(pipe.getWorld())).getPacket(), (Player)par3EntityPlayer);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(RequestPipeDimension.class).setInteger(MainProxy.getDimensionForWorld(pipe.getWorld())), (Player)par3EntityPlayer);
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
		for(Integer dim:DimensionManager.getIDs()) {
			if(pipe.getWorld().equals(DimensionManager.getWorld(dim.intValue()))) {
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
