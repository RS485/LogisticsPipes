package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.network.SendNBTTagCompound;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain = true)
public class NEISetCraftingRecipe extends CoordinatesPacket {
	
	@Getter
	@Setter
	private ItemStack[] content = new ItemStack[9];
	
	public NEISetCraftingRecipe(int id) {
		super(id);
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		TileEntity tile = getTile(player.worldObj , TileEntity.class);
		if(tile instanceof LogisticsCraftingTableTileEntity) {
			((LogisticsCraftingTableTileEntity)tile).handleNEIRecipePacket(getContent());
		} else if(tile instanceof TileGenericPipe && ((TileGenericPipe)tile).pipe instanceof PipeBlockRequestTable) {
			((PipeBlockRequestTable)((TileGenericPipe)tile).pipe).handleNEIRecipePacket(getContent());
		}
	}
	
	@Override
	public ModernPacket template() {
		return new NEISetCraftingRecipe(getId());
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		
		data.writeInt(content.length);
		
		for(int i = 0; i < content.length; i++) {
			final ItemStack itemstack = content[i];
			
			if(itemstack != null) {
				data.writeByte(i);
				data.writeInt(itemstack.itemID);
				data.writeInt(itemstack.stackSize);
				data.writeInt(itemstack.getItemDamage());
				SendNBTTagCompound.writeNBTTagCompound(itemstack.getTagCompound(), data);
			}
		}
		data.writeByte( -1); // mark packet end
	}
	
	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		
		content = new ItemStack[data.readInt()];
		
		byte index = data.readByte();
		
		while(index != -1) { // read until the end
			final int itemID = data.readInt();
			int stackSize = data.readInt();
			int damage = data.readInt();
			ItemStack stack = new ItemStack(itemID, stackSize, damage);
			stack.setTagCompound(SendNBTTagCompound.readNBTTagCompound(data));
			content[index] = stack;
			index = data.readByte(); // read the next slot
		}
	}
}

