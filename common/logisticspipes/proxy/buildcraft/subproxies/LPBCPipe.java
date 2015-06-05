package logisticspipes.proxy.buildcraft.subproxies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.proxy.buildcraft.gates.ActionDisableLogistics;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IIconProvider;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.StatementSlot;
import buildcraft.transport.Pipe;
import buildcraft.transport.gui.ContainerGateInterface;
import buildcraft.transport.gui.GuiGateInterface;

public class LPBCPipe extends Pipe<LPBCPipeTransportsItems> implements IBCPipePart {

	public final LogisticsTileGenericPipe pipe;

	public LPBCPipe(LPBCPipeTransportsItems transport, LogisticsTileGenericPipe pipe) {
		super(transport, null);
		this.pipe = pipe;
	}

	@Override
	public IIconProvider getIconProvider() {
		return null;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return 0;
	}

	@Override
	public Container getGateContainer(InventoryPlayer inventory, int side) {
		ContainerGateInterface gui = new ContainerGateInterface(inventory, this);
		gui.setGate(side);
		return gui;
	}

	@Override
	public Object getClientGui(InventoryPlayer inventory, int side) {
		GuiGateInterface gui = new GuiGateInterface(inventory, this);
		gui.mc = Minecraft.getMinecraft();
		gui.setGate(gates[side]);
		((ContainerGateInterface) gui.inventorySlots).setGate(side);
		gui.slots.clear();
		return gui;
	}

	@Override
	public void addItemDrops(ArrayList<ItemStack> result) {
		result.addAll(computeItemDrop());
	}

	@Override
	public Object getOriginal() {
		return this;
	}

	@Override
	public LinkedList<IActionInternal> getActions() {
		LinkedList<IActionInternal> result = new LinkedList<IActionInternal>();
		if (pipe.pipe instanceof CoreRoutedPipe) {
			if (BuildCraftProxy.LogisticsDisableAction != null) {
				result.add(BuildCraftProxy.LogisticsDisableAction);
			}
		}
		return result;
	}

	@Override
	protected void actionsActivated(Collection<StatementSlot> actions) {
		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		((CoreRoutedPipe) pipe.pipe).setEnabled(true);
		// Activate the actions
		for (StatementSlot slot : actions) {
			if (slot.statement instanceof ActionDisableLogistics) {
				((CoreRoutedPipe) pipe.pipe).setEnabled(false);
			}
		}
	}
}
