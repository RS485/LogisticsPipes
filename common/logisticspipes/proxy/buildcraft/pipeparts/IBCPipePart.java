package logisticspipes.proxy.buildcraft.pipeparts;

import java.util.List;

import buildcraft.api.transport.PipeWire;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public interface IBCPipePart {
	void updateGate();
	void writeToNBT(NBTTagCompound data);
	void readFromNBT(NBTTagCompound data);
	boolean hasGate();
	void addItemDrops(List<ItemStack> result);
	void resetGate();
	boolean isWireConnectedTo(TileEntity tile, PipeWire color);
	boolean isWired(PipeWire color);
	int isPoweringTo(int side);
	void updateSignalStateForColor(PipeWire wire);
	boolean[] getWireSet();
	ItemStack getGateItem();
	int[] getSignalStrength();
	void openGateGui(EntityPlayer player);
}
