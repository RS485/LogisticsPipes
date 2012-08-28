package logisticspipes.pipes;

import java.util.UUID;

import net.minecraft.src.NBTTagCompound;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.IProvideDirectRoutingConnection;
import logisticspipes.logic.BaseRoutingLogic;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.main.RoutedPipe;
import logisticspipes.main.SimpleServiceLocator;

public class PipeItemsInvSysConnector extends RoutedPipe implements IProvideDirectRoutingConnection {
	
	private boolean init = false;
	private String DirectConnection = null;
	
	public PipeItemsInvSysConnector(int itemID) {
		super(new TemporaryLogic(), itemID); //TODO
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(!init) {
			init = true;
			if(DirectConnection != null) {
				if(!SimpleServiceLocator.connectionManager.addDirectConnection(DirectConnection, getRouter())) {
					DirectConnection = null;
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
	}
	
	
	@Override
	public int getCenterTexture() {
		return 0; //TODO
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Fast;
	}
}
