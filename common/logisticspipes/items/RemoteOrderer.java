package logisticspipes.items;

import java.util.Random;

import logisticspipes.LogisticsPipes;
import logisticspipes.main.GuiIDs;
import logisticspipes.pipes.PipeItemsRemoteOrdererLogistics;
import logisticspipes.proxy.MainProxy;


import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.DimensionManager;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;

public class RemoteOrderer extends Item {
	
	protected RemoteOrderer(int id) {
		super(id);
	}

	@Override
	public String getTextureFile() {
		return LogisticsPipes.LOGISTICSITEMS_TEXTURE_FILE;
	}

	//Client
    public boolean func_46056_k() {
        return true;
    }

	//Server
    public boolean func_46003_i() {
        return true;
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
		Random rand = new Random();
		for (int l = 0; l < 32; ++l){
			pipe.worldObj.spawnParticle("portal", pipe.xCoord + 0.5D, pipe.yCoord + 0.5D, pipe.zCoord + 0.5D, rand.nextGaussian() * 0.5D, 0.0D, rand.nextGaussian() * 0.5D);
		}
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
		World world = DimensionManager.getWorld(stack.stackTagCompound.getInteger("connectedPipe-world-dim"));
		if(world == null) {
			if(FMLCommonHandler.instance().getSide().isClient()) {
				world = FMLClientHandler.instance().getClient().theWorld;
			}
		}
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
}
