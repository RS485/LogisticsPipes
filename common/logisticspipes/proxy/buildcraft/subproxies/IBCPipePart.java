package logisticspipes.proxy.buildcraft.subproxies;

import java.util.LinkedList;
import java.util.List;

import buildcraft.transport.Gate;
import logisticspipes.asm.IgnoreDisabledProxy;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface IBCPipePart {
	void updateGate();
	@IgnoreDisabledProxy
	void writeToNBT(NBTTagCompound data);
	@IgnoreDisabledProxy
	void readFromNBT(NBTTagCompound data);
	boolean hasGate(ForgeDirection sideHit);
	void addItemDrops(List<ItemStack> result);
	void resetGate();
	boolean isWireConnectedTo(TileEntity tile, Object color);
	int isPoweringTo(int side);
	void updateSignalState();
	boolean[] getWireSet();
	ItemStack getGateItem(int side);
	int[] getSignalStrength();
	void openGateGui(EntityPlayer player, int side);
	boolean isGateActive();
	boolean receiveSignal(int i, Object wire);
	Object getGate(int i);
	void makeGate(CoreUnroutedPipe pipe, ItemStack currentEquippedItem);
	void updateCoreStateGateData();
	void updateGateFromCoreStateData();
	void checkResyncGate();
	void actionsActivated(Object actions);
	void updateEntity();
	Container getGateContainer(InventoryPlayer inventory, int side);
	Object getClientGui(InventoryPlayer inventory, int side);
	LinkedList<?> getActions();
	boolean isWired();
	void refreshRedStoneInput(int redstoneInput);
	void resolveActions();
	Object getWrapped();
	boolean hasGate();
	Object getOriginal();
	Object getGates();
}
