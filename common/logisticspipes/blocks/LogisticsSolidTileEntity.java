package logisticspipes.blocks;

import logisticspipes.LPConstants;
import logisticspipes.asm.ModDependentField;
import logisticspipes.asm.ModDependentInterface;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.RequestRotationPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.proxy.computers.wrapper.CCObjectWrapper;
import logisticspipes.proxy.opencomputers.IOCTile;
import logisticspipes.proxy.opencomputers.asm.BaseWrapperClass;
import logisticspipes.utils.WorldUtil;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;

@ModDependentInterface(modId = {LPConstants.openComputersModID, LPConstants.openComputersModID, LPConstants.openComputersModID }, interfacePath = { "li.cil.oc.api.network.ManagedPeripheral", "li.cil.oc.api.network.Environment", "li.cil.oc.api.network.SidedEnvironment" })
@CCType(name = "LogisticsSolidBlock")
public class LogisticsSolidTileEntity extends TileEntity implements ILPCCTypeHolder, IRotationProvider, ManagedPeripheral, Environment, SidedEnvironment, IOCTile {

	private boolean addedToNetwork = false;
	private Object ccType = null;
	private boolean init = false;
	public int rotation = 0;

	@ModDependentField(modId = LPConstants.openComputersModID)
	public Node node;

	public LogisticsSolidTileEntity() {
		SimpleServiceLocator.openComputersProxy.initLogisticsSolidTileEntity(this);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		rotation = nbt.getInteger("rotation");
		SimpleServiceLocator.openComputersProxy.handleReadFromNBT(this, nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("rotation", rotation);
		SimpleServiceLocator.openComputersProxy.handleWriteToNBT(this, nbt);
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		SimpleServiceLocator.openComputersProxy.handleChunkUnload(this);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!addedToNetwork) {
			addedToNetwork = true;
			SimpleServiceLocator.openComputersProxy.addToNetwork(this);
		}
		if (MainProxy.isClient(getWorldObj())) {
			if (!init) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestRotationPacket.class).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord));
				init = true;
			}
			return;
		}
	}

	@Override
	public final boolean canUpdate() {
		return true;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		SimpleServiceLocator.openComputersProxy.handleInvalidate(this);
	}

	@Override
	@CCCommand(description = "Returns the LP rotation value for this block")
	public int getRotation() {
		return rotation;
	}

	@Override
	public int getFrontTexture() {
		return 0;
	}

	@Override
	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public void notifyOfBlockChange() {}

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
		BaseWrapperClass object = (BaseWrapperClass) CCObjectWrapper.getWrappedObject(this, BaseWrapperClass.WRAPPER);
		object.isDirectCall = true;
		return CCObjectWrapper.createArray(object);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public String[] methods() {
		return new String[] { "getBlock" };
	}

	@Override
	@SideOnly(Side.CLIENT)
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public boolean canConnect(ForgeDirection dir) {
		return !(new WorldUtil(this).getAdjacentTileEntitie(dir) instanceof LogisticsTileGenericPipe) && !(new WorldUtil(this).getAdjacentTileEntitie(dir) instanceof LogisticsSolidTileEntity);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.openComputersModID)
	public Node sidedNode(ForgeDirection dir) {
		if (new WorldUtil(this).getAdjacentTileEntitie(dir) instanceof LogisticsTileGenericPipe || new WorldUtil(this).getAdjacentTileEntitie(dir) instanceof LogisticsSolidTileEntity) {
			return null;
		} else {
			return node();
		}
	}

	@Override
	public Object getOCNode() {
		return node();
	}

	public LPPosition getLPPosition() {
		return new LPPosition(this);
	}

	@Override
	public void setCCType(Object type) {
		ccType = type;
	}

	@Override
	public Object getCCType() {
		return ccType;
	}
}
