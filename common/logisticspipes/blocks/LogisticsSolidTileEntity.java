package logisticspipes.blocks;

import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;
import logisticspipes.asm.ModDependentField;
import logisticspipes.asm.ModDependentInterface;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.proxy.computers.interfaces.ICCTypeWrapped;
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

@ModDependentInterface(modId={"OpenComputers@1.3", "OpenComputers@1.3", "OpenComputers@1.3"}, interfacePath={"li.cil.oc.api.network.ManagedPeripheral", "li.cil.oc.api.network.Environment", "li.cil.oc.api.network.SidedEnvironment"})
@CCType(name="LogisticsSolidBlock")
public class LogisticsSolidTileEntity extends TileEntity implements ILPCCTypeHolder, IRotationProvider, ManagedPeripheral, Environment, SidedEnvironment, IOCTile {

	private boolean addedToNetwork = false;
	private Object ccType = null;

	public LogisticsSolidTileEntity() {
		SimpleServiceLocator.openComputersProxy.initLogisticsSolidTileEntity(this);
	}

	@ModDependentField(modId="OpenComputers@1.3")
	public Node node;
	
	public int rotation = 0;

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
		if(!addedToNetwork) {
			addedToNetwork = true;
			SimpleServiceLocator.openComputersProxy.addToNetwork(this);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		SimpleServiceLocator.openComputersProxy.handleInvalidate(this);
	}

	@Override
	@CCCommand(description="Returns the LP rotation value for this block")
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
	@ModDependentMethod(modId="OpenComputers@1.3")
	public Node node() {
		return node;
	}

	@Override
	@ModDependentMethod(modId="OpenComputers@1.3")
	public void onConnect(Node node1) {}

	@Override
	@ModDependentMethod(modId="OpenComputers@1.3")
	public void onDisconnect(Node node1) {}

	@Override
	@ModDependentMethod(modId="OpenComputers@1.3")
	public void onMessage(Message message) {}

	@Override
	@ModDependentMethod(modId="OpenComputers@1.3")
	public Object[] invoke(String s, Context context, Arguments arguments) throws Exception {
		BaseWrapperClass object = (BaseWrapperClass) CCObjectWrapper.getWrappedObject(this, BaseWrapperClass.WRAPPER);
		object.isDirectCall = true;
		return CCObjectWrapper.createArray(object);
	}

	@Override
	@ModDependentMethod(modId="OpenComputers@1.3")
	public String[] methods() {
		return new String[]{"getBlock"};
	}

	@Override
	@SideOnly(Side.CLIENT)
	@ModDependentMethod(modId="OpenComputers@1.3")
	public boolean canConnect(ForgeDirection dir) {
		return !(new WorldUtil(this).getAdjacentTileEntitie(dir) instanceof LogisticsTileGenericPipe) && !(new WorldUtil(this).getAdjacentTileEntitie(dir) instanceof LogisticsSolidTileEntity);
	}

	@Override
	@ModDependentMethod(modId="OpenComputers@1.3")
	public Node sidedNode(ForgeDirection dir) {
		if(new WorldUtil(this).getAdjacentTileEntitie(dir) instanceof LogisticsTileGenericPipe || new WorldUtil(this).getAdjacentTileEntitie(dir) instanceof LogisticsSolidTileEntity) {
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
		this.ccType = type;
	}

	@Override
	public Object getCCType() {
		return this.ccType;
	}
}
