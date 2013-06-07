package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.logisticspipes.RouteLayer;
import logisticspipes.logisticspipes.RouteLayerFirewall;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.oldpackets.PacketPipeBitSet;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.network.Player;

public class PipeItemsFirewall extends CoreRoutedPipe {

	private IRouter[] routers = new IRouter[ForgeDirection.VALID_DIRECTIONS.length];
	private String[] routerIds = new String[ForgeDirection.VALID_DIRECTIONS.length];
	
	public SimpleInventory inv = new SimpleInventory(6 * 6, "Filter Inv", 1);
	private boolean blockProvider = false;
	private boolean blockCrafer = false;
	private boolean blockSorting = false;
	private boolean blockPower = true;
	private boolean isBlocking = true;
	
	public PipeItemsFirewall(int itemID) {
		super(new TemporaryLogic(), itemID);
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
	
	@Override
	public boolean wrenchClicked(World world, int x, int y, int z, EntityPlayer entityplayer, SecuritySettings settings) {
		if(MainProxy.isServer(world)) {
			if (settings == null || settings.openGui) {
				entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_FIREWALL, world, x, y, z);
				MainProxy.sendPacketToPlayer(new PacketPipeBitSet(NetworkConstants.FIREWALL_FLAG_SET, getX(), getY(), getZ(), getFlags()).getPacket(), (Player) entityplayer);
			} else {
				entityplayer.sendChatToPlayer("Permission denied");
			}
		}
		return true;
	}

	@Override
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
					routers[dir.ordinal()] = SimpleServiceLocator.routerManager.getOrCreateFirewallRouter(UUID.fromString(routerIds[dir.ordinal()]), MainProxy.getDimensionForWorld(worldObj), getX(), getY(), getZ(), dir);
				}
			}
			return routers[dir.ordinal()];
		}
		return super.getRouter();
	}
	
	@Override
	public IRouter getRouter() {
		if (router == null){
			synchronized (routerIdLock) {
				if (routerId == null || routerId == ""){
					routerId = UUID.randomUUID().toString();
				}
				router = SimpleServiceLocator.routerManager.getOrCreateFirewallRouter(UUID.fromString(routerId), MainProxy.getDimensionForWorld(worldObj), getX(), getY(), getZ(), ForgeDirection.UNKNOWN);
			}
		}
		return router;
	}
	
	@Override
	public RouteLayer getRouteLayer(){
		if (_routeLayer == null){
			_routeLayer = new RouteLayerFirewall(getRouter(), getTransportLayer());
		}
		return _routeLayer;
	}
	
	public ForgeDirection getRouterSide(IRouter router) {
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
			if(getRouter(dir) == router) {
				return dir;
			}
		}
		return ForgeDirection.UNKNOWN;
	}

	public boolean isIdforOtherSide(int id) {
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
			if(getRouter(dir).getSimpleID() == id) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		
		synchronized (routerIdLock) {
			for(int i=0;i<routerIds.length;i++) {
				nbttagcompound.setString("routerId" + i, routerIds[i]);
			}
		}
		inv.writeToNBT(nbttagcompound);
		nbttagcompound.setBoolean("blockProvider", blockProvider);
		nbttagcompound.setBoolean("blockCrafer", blockCrafer);
		nbttagcompound.setBoolean("blockSorting", blockSorting);
		nbttagcompound.setBoolean("blockPower", blockPower);
		nbttagcompound.setBoolean("isBlocking", isBlocking);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		
		synchronized (routerIdLock) {
			for(int i=0;i<routerIds.length;i++) {
				routerIds[i] = nbttagcompound.getString("routerId" + i);
			}
		}
		inv.readFromNBT(nbttagcompound);
		blockProvider = nbttagcompound.getBoolean("blockProvider");
		blockCrafer = nbttagcompound.getBoolean("blockCrafer");
		blockSorting = nbttagcompound.getBoolean("blockSorting");
		if(nbttagcompound.hasKey("blockPower")) {
			blockPower = nbttagcompound.getBoolean("blockPower");
		}
		isBlocking = nbttagcompound.getBoolean("isBlocking");
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_FIREWALL_TEXTURE;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return null;
	}
	
	public List<ExitRoute> getRouters(IRouter from) {
		List<ExitRoute> list = new ArrayList<ExitRoute>();
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
			if(getRouter(dir).equals(from)) continue;
			List<ExitRoute> nodes = getRouter(dir).getIRoutersByCost();
			list.addAll(nodes);
		}
		Collections.sort(list);
		return list;
	}
	
	public IFilter getFilter(final UUID id, final int simpleid) {
		return new IFilter() {
			@Override
			public boolean isBlocked() {
				return isBlocking;
			}

			@Override
			public boolean isFilteredItem(ItemIdentifier item) {
				return inv.containsUndamagedItem(item);
			}

			@Override
			public boolean blockProvider() {
				return blockProvider;
			}

			@Override
			public boolean blockCrafting() {
				return blockCrafer;
			}

			@Override
			public UUID getUUID() {
				return id;
			}

			@Override
			public int getSimpleID() {
				return simpleid;
			}

			@Override
			public boolean blockRouting() {
				return blockSorting;
			}

			@Override
			public boolean blockPower() {
				return blockPower;
			}
		};
	}

	public boolean isBlockProvider() {
		return blockProvider;
	}

	public void setBlockProvider(boolean blockProvider) {
		this.blockProvider = blockProvider;
		MainProxy.sendPacketToServer(new PacketPipeBitSet(NetworkConstants.FIREWALL_FLAG_SET, getX(), getY(), getZ(), getFlags()).getPacket());
	}

	public boolean isBlockCrafer() {
		return blockCrafer;
	}

	public void setBlockCrafer(boolean blockCrafer) {
		this.blockCrafer = blockCrafer;
		MainProxy.sendPacketToServer(new PacketPipeBitSet(NetworkConstants.FIREWALL_FLAG_SET, getX(), getY(), getZ(), getFlags()).getPacket());
	}

	public boolean isBlockSorting() {
		return blockSorting;
	}

	public void setBlockSorting(boolean blockSorting) {
		this.blockSorting = blockSorting;
		MainProxy.sendPacketToServer(new PacketPipeBitSet(NetworkConstants.FIREWALL_FLAG_SET, getX(), getY(), getZ(), getFlags()).getPacket());
	}

	public boolean isBlockPower() {
		return blockPower;
	}

	public void setBlockPower(boolean blockPower) {
		this.blockPower = blockPower;
		MainProxy.sendPacketToServer(new PacketPipeBitSet(NetworkConstants.FIREWALL_FLAG_SET, getX(), getY(), getZ(), getFlags()).getPacket());
	}

	public boolean isBlocking() {
		return isBlocking;
	}

	public void setBlocking(boolean isBlocking) {
		this.isBlocking = isBlocking;
		MainProxy.sendPacketToServer(new PacketPipeBitSet(NetworkConstants.FIREWALL_FLAG_SET, getX(), getY(), getZ(), getFlags()).getPacket());
	}
	
	private BitSet getFlags() {
		BitSet flags = new BitSet();
		flags.set(0, blockProvider);
		flags.set(1, blockCrafer);
		flags.set(2, blockSorting);
		flags.set(3, blockPower);
		flags.set(4, isBlocking);
		return flags;
	}
	
	public void setFlags(BitSet flags) {
		blockProvider = flags.get(0);
		blockCrafer = flags.get(1);
		blockSorting = flags.get(2);
		blockPower = flags.get(3);
		isBlocking = flags.get(4);
	}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}

	@Override
	protected void addRouterCrashReport(CrashReportCategory crashReportCategory) {
		for(int i=0; i<7;i++) {
			ForgeDirection dir = ForgeDirection.getOrientation(i);
			crashReportCategory.addCrashSection("Router (" + dir.toString() + ")", this.getRouter(dir));
		}
	}
}
