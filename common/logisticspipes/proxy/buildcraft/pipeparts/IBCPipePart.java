package logisticspipes.proxy.buildcraft.pipeparts;

import java.util.List;
import java.util.Map;

import logisticspipes.asm.IgnoreDisabledProxy;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import buildcraft.api.gates.IAction;
import buildcraft.api.transport.PipeWire;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
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
	boolean isWireConnectedTo(TileEntity tile, PipeWire color);
	boolean isWired(PipeWire color);
	int isPoweringTo(int side);
	void updateSignalStateForColor(PipeWire wire);
	boolean[] getWireSet();
	ItemStack getGateItem();
	int[] getSignalStrength();
	void openGateGui(EntityPlayer player);
	boolean isGateActive();
	boolean receiveSignal(int i, PipeWire wire);
	Object getGate();
	void makeGate(CoreUnroutedPipe pipe, ItemStack currentEquippedItem);
	void updateCoreStateGateData();
	void updateGateFromCoreStateData();
	void checkResyncGate();
	void actionsActivated(Object actions);
}
