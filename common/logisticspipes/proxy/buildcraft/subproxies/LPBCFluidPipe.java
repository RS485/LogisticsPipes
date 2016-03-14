package logisticspipes.proxy.buildcraft.subproxies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;

import buildcraft.api.core.IIconProvider;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.StatementSlot;
import buildcraft.transport.Pipe;

public class LPBCFluidPipe extends Pipe<LPBCPipeTransportsFluids> implements IBCPipePart {

	public final LogisticsTileGenericPipe pipe;
	public final LPBCPipe bcPipe;

	public LPBCFluidPipe(LPBCPipeTransportsFluids transport, LogisticsTileGenericPipe pipe, LPBCPipe bcPipe) {
		super(transport, null);
		this.pipe = pipe;
		this.bcPipe = bcPipe;
	}

	@Override
	public IIconProvider getIconProvider() {
		return bcPipe.getIconProvider();
	}

	@Override
	public int getIconIndex(EnumFacing direction) {
		return bcPipe.getIconIndex(direction);
	}

	@Override
	public Container getGateContainer(InventoryPlayer inventory, int side) {
		return bcPipe.getGateContainer(inventory, side);
	}

	@Override
	public Object getClientGui(InventoryPlayer inventory, int side) {
		return bcPipe.getClientGui(inventory, side);
	}

	@Override
	public void addItemDrops(ArrayList<ItemStack> result) {
		bcPipe.addItemDrops(result);
	}

	@Override
	public Object getOriginal() {
		return this;
	}

	@Override
	public LinkedList<IActionInternal> getActions() {
		return bcPipe.getActions();
	}

	@Override
	protected void actionsActivated(Collection<StatementSlot> actions) {
		bcPipe.actionsActivated(actions);
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, EnumFacing side) {
		return bcPipe.canPipeConnect(tile, side);
	}
}
