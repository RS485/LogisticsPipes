package logisticspipes.proxy.buildcraft.subproxies;

import java.util.LinkedList;
import java.util.List;

import logisticspipes.asm.IgnoreDisabledProxy;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public interface IBCPipePart {
	void updateGate();
	@IgnoreDisabledProxy
	void writeToNBT(NBTTagCompound data);
	@IgnoreDisabledProxy
	void readFromNBT(NBTTagCompound data);
	boolean hasGate();
	void addItemDrops(List<ItemStack> result);
	void resetGate();
	boolean isWireConnectedTo(TileEntity tile, Object color);
	int isPoweringTo(int side);
	void updateSignalState();
	boolean[] getWireSet();
	ItemStack getGateItem();
	int[] getSignalStrength();
	void openGateGui(EntityPlayer player);
	boolean isGateActive();
	boolean receiveSignal(int i, Object wire);
	Object getGate();
	void makeGate(CoreUnroutedPipe pipe, ItemStack currentEquippedItem);
	void updateCoreStateGateData();
	void updateGateFromCoreStateData();
	void checkResyncGate();
	void actionsActivated(Object actions);
	void updateEntity();
	Container getGateContainer(InventoryPlayer inventory);
	Object getClientGui(InventoryPlayer inventory);
	LinkedList<?> getActions();
	boolean isWired();
	void refreshRedStoneInput(int redstoneInput);
}
