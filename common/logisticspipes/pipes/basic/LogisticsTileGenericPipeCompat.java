package logisticspipes.pipes.basic;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.TileGenericPipe;
import cofh.api.transport.IItemDuct;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;

import logisticspipes.LPConstants;
import logisticspipes.asm.ModDependentField;
import logisticspipes.asm.ModDependentInterface;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.computers.wrapper.CCObjectWrapper;
import logisticspipes.proxy.opencomputers.asm.BaseWrapperClass;

@ModDependentInterface(modId = { "CoFHCore", LPConstants.openComputersModID, LPConstants.openComputersModID, LPConstants.openComputersModID, "BuildCraft|Transport", "BuildCraft|Transport" }, interfacePath = { "cofh.api.transport.IItemDuct", "li.cil.oc.api.network.ManagedPeripheral", "li.cil.oc.api.network.Environment", "li.cil.oc.api.network.SidedEnvironment",
		"buildcraft.api.transport.IPipeTile", "buildcraft.api.transport.IPipeConnection" })
public class LogisticsTileGenericPipeCompat extends LogisticsTileGenericPipe implements IItemDuct, ManagedPeripheral, Environment, SidedEnvironment, IPipeTile, IPipeConnection {

	@ModDependentField(modId = LPConstants.computerCraftModID)
	public HashMap<IComputerAccess, EnumFacing> connections;

	@ModDependentField(modId = LPConstants.computerCraftModID)
	public IComputerAccess currentPC;

	@ModDependentField(modId = LPConstants.openComputersModID)
	public Node node;

	protected void preInit() {
		if (SimpleServiceLocator.ccProxy.isCC()) {
			connections = new HashMap<>();
		}
	}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public Node node() {
		return node;
	}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public void onConnect(Node node1) {}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public void onDisconnect(Node node1) {}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public void onMessage(Message message) {}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public Object[] invoke(String s, Context context, Arguments arguments) throws Exception {
		BaseWrapperClass object = (BaseWrapperClass) CCObjectWrapper.getWrappedObject(pipe, BaseWrapperClass.WRAPPER);
		object.isDirectCall = true;
		return CCObjectWrapper.createArray(object);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public String[] methods() {
		return new String[] { "getPipe" };
	}

	@Override
	@SideOnly(Side.CLIENT)
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public boolean canConnect(EnumFacing dir) {
		return !(this.getTile(dir) instanceof LogisticsTileGenericPipe) && !(this.getTile(dir) instanceof LogisticsSolidTileEntity);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public Node sidedNode(EnumFacing dir) {
		if (this.getTile(dir) instanceof LogisticsTileGenericPipe || this.getTile(dir) instanceof LogisticsSolidTileEntity) {
			return null;
		} else {
			return node();
		}
	}

	/**
	 * Used to determine where BC items can go.
	 */
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public boolean isBCPipeConnected(TileGenericPipe container, EnumFacing o) {
		return container.isPipeConnected(o);
	}

	@Override
	public Object getOCNode() {
		return node();
	}

	@Override
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public PipeType getPipeType() {
		return (PipeType) SimpleServiceLocator.buildCraftProxy.getLPPipeType();
	}

	@Override
	public World getWorldBC() {
		return null;
	}

	@Override
	public BlockPos getPosBC() {
		return null;
	}

	@Override
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public IPipe getPipe() {
		return (IPipe) tilePart.getBCPipePart().getOriginal();
	}

	@Override
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public boolean canInjectItems(EnumFacing from) {
		return isPipeConnected(from);
	}

	@Override
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public int injectItem(ItemStack itemStack, boolean b, EnumFacing enumFacing, EnumDyeColor enumDyeColor) {
		return super.injectItem(itemStack, b, enumFacing);
	}

	@Override
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public Block getNeighborBlock(EnumFacing dir) {
		return getBlock(dir);
	}

	@Override
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public TileEntity getNeighborTile(EnumFacing dir) {
		return getTile(dir);
	}

	@Override
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public IPipe getNeighborPipe(EnumFacing dir) {
		if (getTile(dir) instanceof IPipeTile) {
			return ((IPipeTile) getTile(dir)).getPipe();
		}
		return null;
	}

	@Override
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public int getPipeColor() {
		return 0;
	}

	@Override
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public boolean hasPipePluggable(EnumFacing direction) {
		return tilePart.getBCPipePluggable(direction) != null;
	}

	@Override
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public boolean hasBlockingPluggable(EnumFacing direction) {
		if (tilePart.getBCPipePluggable(direction) == null) {
			return false;
		}
		return tilePart.getBCPipePluggable(direction).isBlocking();
	}

	@Override
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public ConnectOverride overridePipeConnection(PipeType pipeType, EnumFacing EnumFacing) {
		if(this.pipe != null && this.pipe.isFluidPipe()) {
			if(pipeType == PipeType.FLUID) {
				return ConnectOverride.CONNECT;
			}
		}
		return ConnectOverride.DEFAULT;
	}
}
