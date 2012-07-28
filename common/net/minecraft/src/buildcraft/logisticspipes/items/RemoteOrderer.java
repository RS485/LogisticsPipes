package net.minecraft.src.buildcraft.logisticspipes.items;

import java.util.List;
import java.util.Random;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.mod_LogisticsPipes;
import buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRemoteOrdererLogistics;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;
import net.minecraft.src.forge.DimensionManager;
import net.minecraft.src.forge.ITextureProvider;

public class RemoteOrderer extends Item implements ITextureProvider {
	
	protected RemoteOrderer(int id) {
		super(id);
	}

	@Override
	public String getTextureFile() {
		return core_LogisticsPipes.LOGISTICSITEMS_TEXTURE_FILE;
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
			if(!APIProxy.isClient(par2World)) {
				par3EntityPlayer.openGui(mod_LogisticsPipes.instance, GuiIDs.GUI_Orderer_ID, pipe.worldObj, pipe.xCoord, pipe.yCoord, pipe.zCoord);
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
