package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import logisticspipes.config.Configs;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IFilteringPipe;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.SearchNode;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public class PipeItemsFirewall extends RoutedPipe implements IFilteringPipe {

	private IRouter[] routers = new IRouter[ForgeDirection.VALID_DIRECTIONS.length];
	private String[] routerIds = new String[ForgeDirection.VALID_DIRECTIONS.length];
	
	public PipeItemsFirewall(int itemID) {
		super(new TemporaryLogic(), itemID);
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
	
	public void ignoreDisableUpdateEntity() {
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
			getRouter(dir).update(worldObj.getWorldTime() % Configs.LOGISTICS_DETECTION_FREQUENCY == _delayOffset || _initialInit);
		}
	}
	
	@Override
	public IRouter getRouter(ForgeDirection dir) {
		if(dir.ordinal() < routers.length) {
			if (routers[dir.ordinal()] == null){
				synchronized (routerIdLock) {
					if (routerIds[dir.ordinal()] == null || routerIds[dir.ordinal()].isEmpty()) {
						routerIds[dir.ordinal()] = UUID.randomUUID().toString();
					}
					routers[dir.ordinal()] = SimpleServiceLocator.routerManager.getOrCreateRouter(UUID.fromString(routerIds[dir.ordinal()]), MainProxy.getDimensionForWorld(worldObj), xCoord, yCoord, zCoord);
				}
			}
			return routers[dir.ordinal()];
		}
		return super.getRouter();
	}
	
	public ForgeDirection getRouterSide(IRouter router) {
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
			if(getRouter(dir) == router) {
				return dir;
			}
		}
		return ForgeDirection.UNKNOWN;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		
		synchronized (routerIdLock) {
			for(int i=0;i<routerIds.length;i++) {
				nbttagcompound.setString("routerId" + i, routerIds[i]);
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		
		synchronized (routerIdLock) {
			for(int i=0;i<routerIds.length;i++) {
				routerIds[i] = nbttagcompound.getString("routerId" + i);
			}
		}
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_FIREWALL_TEXTURE;
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}

	@Override
	public List<SearchNode> getRouters(IRouter from) {
		List<SearchNode> list = new ArrayList<SearchNode>();
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
			if(getRouter(dir).equals(from)) continue;
			List<SearchNode> nodes = getRouter(dir).getIRoutersByCost();
			list.addAll(nodes);
		}
		Collections.sort(list);
		return list;
	}

	@Override
	public IFilter getFilter() {
		//TODO
		return new IFilter() {
			@Override
			public boolean isBlocked() {
				return true;
			}

			@Override
			public List<ItemIdentifier> getFilteredItems() {
				List<ItemIdentifier> list = new ArrayList<ItemIdentifier>();
				list.add(ItemIdentifier.get(1, 0, null));
				return list;
			}
		};
	}
}
