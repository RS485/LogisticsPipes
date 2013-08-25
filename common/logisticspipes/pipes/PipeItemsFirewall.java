package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.RouteLayer;
import logisticspipes.logisticspipes.RouteLayerFirewall;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.FireWallFlag;
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
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.network.Player;

public class PipeItemsFirewall extends CoreRoutedPipe {

	private IRouter[] routers = new IRouter[7];
	private String[] routerIds = new String[ForgeDirection.VALID_DIRECTIONS.length];
	
	public SimpleInventory inv = new SimpleInventory(6 * 6, "Filter Inv", 1);
	private boolean blockProvider = false;
	private boolean blockCrafer = false;
	private boolean blockSorting = false;
	private boolean blockPower = true;
	private boolean isBlocking = true;
	
	public PipeItemsFirewall(int itemID) {
		super(itemID);
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
	
	@Override
	public boolean wrenchClicked(EntityPlayer entityplayer, SecuritySettings settings) {
		if(MainProxy.isServer(getWorld())) {
			if (settings == null || settings.openGui) {
				entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_FIREWALL, getWorld(), getX(), getY(), getZ());
//TODO 			MainProxy.sendPacketToPlayer(new PacketPipeBitSet(NetworkConstants.FIREWALL_FLAG_SET, getX(), getY(), getZ(), getFlags()).getPacket(), (Player) entityplayer);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player) entityplayer);
			} else {
				entityplayer.sendChatToPlayer(ChatMessageComponent.func_111066_d("Permission denied"));
			}
		}
		return true;
	}

	@Override
	public void ignoreDisableUpdateEntity() {
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
			getRouter(dir).update(getWorld().getWorldTime() % Configs.LOGISTICS_DETECTION_FREQUENCY == _delayOffset || _initialInit);
		}
	}

	@Override
	public void invalidate() {
		for(int i=0;i<6;i++) {
			if(routers[i] != null) {
				routers[i].destroy();
				routers[i] = null;
			}
		}
		routers[6] = null;
		super.invalidate();
	}
	
	@Override
	public void onChunkUnload() {
		for(int i=0;i<6;i++) {
			if(routers[i] != null) {
				routers[i].clearPipeCache();
				routers[i].clearInterests();
			}
		}
		super.onChunkUnload();
	}
	
	private void createRouters() {
		synchronized (routerIdLock) {
			if (routerId == null || routerId == ""){
				routerId = UUID.randomUUID().toString();
			}
			router = SimpleServiceLocator.routerManager.getOrCreateFirewallRouter(UUID.fromString(routerId), MainProxy.getDimensionForWorld(getWorld()), getX(), getY(), getZ(), ForgeDirection.UNKNOWN, routers);
			for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
				if (routerIds[dir.ordinal()] == null || routerIds[dir.ordinal()].isEmpty()) {
					routerIds[dir.ordinal()] = UUID.randomUUID().toString();
				}
				routers[dir.ordinal()] = SimpleServiceLocator.routerManager.getOrCreateFirewallRouter(UUID.fromString(routerIds[dir.ordinal()]), MainProxy.getDimensionForWorld(getWorld()), getX(), getY(), getZ(), dir, routers);
			}
			routers[6] = router;
		}
	}

	@Override
	public IRouter getRouter(ForgeDirection dir) {
		if(stillNeedReplace) {
			System.out.println("Hey, don't get routers for pipes that aren't ready");
			new Throwable().printStackTrace();
		}
		if(router == null){
			createRouters();
		}
		if(dir.ordinal() < routers.length) {
			return routers[dir.ordinal()];
		}
		//this should never happen
		return super.getRouter();
	}
	
	@Override
	public IRouter getRouter() {
		if(stillNeedReplace) {
			System.out.println("Hey, don't get routers for pipes that aren't ready");
			new Throwable().printStackTrace();
		}
		if(router == null){
			createRouters();
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
//TODO 	MainProxy.sendPacketToServer(new PacketPipeBitSet(NetworkConstants.FIREWALL_FLAG_SET, getX(), getY(), getZ(), getFlags()).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	public boolean isBlockCrafer() {
		return blockCrafer;
	}

	public void setBlockCrafer(boolean blockCrafer) {
		this.blockCrafer = blockCrafer;
//TODO 	MainProxy.sendPacketToServer(new PacketPipeBitSet(NetworkConstants.FIREWALL_FLAG_SET, getX(), getY(), getZ(), getFlags()).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	public boolean isBlockSorting() {
		return blockSorting;
	}

	public void setBlockSorting(boolean blockSorting) {
		this.blockSorting = blockSorting;
//TODO 	MainProxy.sendPacketToServer(new PacketPipeBitSet(NetworkConstants.FIREWALL_FLAG_SET, getX(), getY(), getZ(), getFlags()).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	public boolean isBlockPower() {
		return blockPower;
	}

	public void setBlockPower(boolean blockPower) {
		this.blockPower = blockPower;
//TODO 	MainProxy.sendPacketToServer(new PacketPipeBitSet(NetworkConstants.FIREWALL_FLAG_SET, getX(), getY(), getZ(), getFlags()).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	public boolean isBlocking() {
		return isBlocking;
	}

	public void setBlocking(boolean isBlocking) {
		this.isBlocking = isBlocking;
//TODO 	MainProxy.sendPacketToServer(new PacketPipeBitSet(NetworkConstants.FIREWALL_FLAG_SET, getX(), getY(), getZ(), getFlags()).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FireWallFlag.class).setFlags(getFlags()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
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
