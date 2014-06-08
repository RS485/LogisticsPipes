package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.block.CraftingPipeNextAdvancedSatellitePacket;
import logisticspipes.network.packets.block.CraftingPipePrevAdvancedSatellitePacket;
import logisticspipes.network.packets.cpipe.CPipeNextSatellite;
import logisticspipes.network.packets.cpipe.CPipePrevSatellite;
import logisticspipes.network.packets.cpipe.CPipeSatelliteId;
import logisticspipes.network.packets.cpipe.CPipeSatelliteImport;
import logisticspipes.network.packets.cpipe.CPipeSatelliteImportBack;
import logisticspipes.network.packets.cpipe.CraftingAdvancedSatelliteId;
import logisticspipes.network.packets.cpipe.CraftingFuzzyFlag;
import logisticspipes.network.packets.cpipe.CraftingPipeOpenConnectedGuiPacket;
import logisticspipes.network.packets.gui.GuiArgument;
import logisticspipes.network.packets.hud.HUDStartWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopWatchingPacket;
import logisticspipes.network.packets.orderer.OrdererManagerContent;
import logisticspipes.network.packets.pipe.CraftingPipePriorityDownPacket;
import logisticspipes.network.packets.pipe.CraftingPipePriorityUpPacket;
import logisticspipes.network.packets.pipe.CraftingPipeStackMovePacket;
import logisticspipes.network.packets.pipe.CraftingPipeUpdatePacket;
import logisticspipes.network.packets.pipe.CraftingPriority;
import logisticspipes.network.packets.pipe.FluidCraftingAdvancedSatelliteId;
import logisticspipes.network.packets.pipe.FluidCraftingAmount;
import logisticspipes.network.packets.pipe.FluidCraftingPipeAdvancedSatelliteNextPacket;
import logisticspipes.network.packets.pipe.FluidCraftingPipeAdvancedSatellitePrevPacket;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.signs.CraftingPipeSign;
import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.proxy.interfaces.IFuzzyRecipeProvider;
import logisticspipes.request.CraftingTemplate;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.order.LogisticsOrder;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.CraftingRequirement;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.WorldUtil;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleCrafter extends LogisticsGuiModule implements ICraftItems {
	
	private CoreRoutedPipe _pipe;
	
	private IInventoryProvider _invProvider;
	private IRoutedPowerProvider _power;
	private ForgeDirection _sneakyDirection = ForgeDirection.UNKNOWN;
	private IWorldProvider _world;


	public int satelliteId = 0;
	public int[] advancedSatelliteIdArray = new int[9];
	public int[] fuzzyCraftingFlagArray = new int[9];
	public int priority = 0;

	
	// from PipeItemsCraftingLogistics
	protected ItemIdentifierInventory _dummyInventory = new ItemIdentifierInventory(11, "Requested items", 127);
	protected ItemIdentifierInventory _liquidInventory = new ItemIdentifierInventory(ItemUpgrade.MAX_LIQUID_CRAFTER, "Fluid items", 1, true);
	
	protected int[] amount = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
	public int[] liquidSatelliteIdArray = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
	public int liquidSatelliteId = 0;

	public boolean[] craftingSigns = new boolean[6];

	public ModuleCrafter() {
	}

	
	public ModuleCrafter(PipeItemsCraftingLogistics parent) {
		_pipe=parent;
		_invProvider = parent;
		_power=parent;
	}
	
	@Override
	public void registerHandler(IInventoryProvider invProvider, IWorldProvider world, IRoutedPowerProvider powerprovider) {}
	
	@Override
	public void registerSlot(int slot) {}
	
	@Override
	public final int getX() {
		return this._invProvider.getX();
	}
	
	@Override
	public final int getY() {
		return this._invProvider.getY();
	}
	
	@Override
	public final int getZ() {
		return this._invProvider.getZ();
	}
	
	protected static final SinkReply	_sinkReply	= new SinkReply(FixedPriority.ItemSink, 0, true, false, 1, 0);
	
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		return new SinkReply(_sinkReply, spaceFor(item, includeInTransit));
	}
	
	protected int spaceFor(ItemIdentifier item, boolean includeInTransit) {
		int count = 0;
		WorldUtil wUtil = new WorldUtil(_pipe.getWorld(), _invProvider.getX(), _invProvider.getY(), _invProvider.getZ());
		for(AdjacentTile tile: wUtil.getAdjacentTileEntities(true)) {
			if(!(tile.tile instanceof IInventory)) continue;
			if(tile.tile instanceof TileGenericPipe) continue;
			IInventory base = (IInventory)tile.tile;
			if(base instanceof net.minecraft.inventory.ISidedInventory) {
				base = new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory)base, tile.orientation.getOpposite(), false);
			}
			IInventoryUtil inv = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(base);
			count += inv.roomForItem(item, 9999);
		}
		if(includeInTransit) {
			count -= _pipe.countOnRoute(item);
		}
		return count;
	}
	
	
	public int getPriority() {
		return priority;
	}
	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}
	
	@Override
	public void tick() {}
	
	@Override
	public boolean hasGenericInterests() {
		return false;
	}
	
	@Override
	public Set<ItemIdentifier> getSpecificInterests() {
		List<ItemIdentifierStack> result = getCraftedItems();
		if(result == null) return null;
		Set<ItemIdentifier> l1 = new TreeSet<ItemIdentifier>();
		for(ItemIdentifierStack craftable:result){
			l1.add(craftable.getItem());
		}
		//for(int i=0; i<9;i++)
		//	l1.add(getMaterials(i));
		return l1;
	}
	
	@Override
	public boolean interestedInAttachedInventory() {
		return false;
		// when we are default we are interested in everything anyway, otherwise we're only interested in our filter.
	}
	
	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}
	
	@Override
	public boolean recievePassive() {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return null;
	}

	@Override
	public void canProvide(RequestTreeNode tree, int donePromisses,
			List<IFilter> filter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LogisticsOrder fullFill(LogisticsPromise promise,
			IRequestItems destination) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> list,
			List<IFilter> filter) {		
	}

	@Override
	public IRouter getRouter() {
		return _invProvider.getRouter();
	}

	@Override
	public void itemCouldNotBeSend(ItemIdentifierStack item) {
		// TODO Auto-generated method stub
		_pipe.itemCouldNotBeSend(item);
		
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int compareTo(IRequestItems value2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void registerExtras(LogisticsPromise promise) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CraftingTemplate addCrafting(ItemIdentifier toCraft) {

		List<ItemIdentifierStack> stack = getCraftedItems(); 
		if (stack == null) return null;
		boolean found = false;
		ItemIdentifierStack craftingStack = null;
		for(ItemIdentifierStack craftable:stack) {
			craftingStack = craftable;
			if(craftingStack.getItem().equals(toCraft)) {
				found = true;
				break;
			}
				
		}
		if(found == false)
			return null;

		IRequestItems[] target = new IRequestItems[9];
		for(int i=0;i<9;i++) {
			target[i] = this;
		}

		boolean hasSatellite = isSatelliteConnected();
		if(!hasSatellite) return null;
		if(!getUpgradeManager().isAdvancedSatelliteCrafter()) {
			if(satelliteId != 0) {
				IRouter r = getSatelliteRouter(-1);
				if(r != null) {
					IRequestItems sat = r.getPipe();
					for(int i=6;i<9;i++) {
						target[i] = sat;
					}
				}
			}
		} else {
			for(int i=0;i<9;i++) {
				if(advancedSatelliteIdArray[i] != 0) {
					IRouter r = getSatelliteRouter(i);
					if(r != null) target[i] = r.getPipe();
				}
			}
		}

		CraftingTemplate template = new CraftingTemplate(craftingStack, this, priority);

		//Check all materials
		for (int i = 0; i < 9; i++){
			ItemIdentifierStack resourceStack = getMaterials(i);
			if (resourceStack == null || resourceStack.getStackSize() == 0) continue;
			CraftingRequirement req = new CraftingRequirement();
			req.stack = resourceStack;
			if(getUpgradeManager().isFuzzyCrafter())
			{
				if((fuzzyCraftingFlagArray[i] & 0x1) != 0)
					req.use_od = true;
				if((fuzzyCraftingFlagArray[i] & 0x2) != 0)
					req.ignore_dmg = true;
				if((fuzzyCraftingFlagArray[i] & 0x4) != 0)
					req.ignore_nbt = true;
				if((fuzzyCraftingFlagArray[i] & 0x8) != 0)
					req.use_category = true;
			}
			template.addRequirement(req, target[i]);
		}
		
		int liquidCrafter = this.getUpgradeManager().getFluidCrafter();
		IRequestFluid[] liquidTarget = new IRequestFluid[liquidCrafter];
		
		if(!getUpgradeManager().isAdvancedSatelliteCrafter()) {
			if(liquidSatelliteId != 0) {
				IRouter r = getFluidSatelliteRouter(-1);
				if(r != null) {
					IRequestFluid sat = (IRequestFluid) r.getPipe();
					for(int i=0;i<liquidCrafter;i++) {
					liquidTarget[i] = sat;
				}
			}
			}
		} else {
			for(int i=0;i<liquidCrafter;i++) {
				if(liquidSatelliteIdArray[i] != 0) {
					IRouter r = getFluidSatelliteRouter(i);
					if(r != null) liquidTarget[i] = (IRequestFluid) r.getPipe();
				}
			}
		}
		
		for (int i = 0; i < liquidCrafter; i++){
			FluidIdentifier liquid = getFluidMaterial(i);
			int amount = getFluidAmount()[i];
			if (liquid == null || amount <= 0 || liquidTarget[i] == null) continue;
			template.addRequirement(liquid, amount, liquidTarget[i]);
		}
		
		if(this.getUpgradeManager().hasByproductExtractor() && getByproductItem() != null) {
			template.addByproduct(getByproductItem());
		}
		
		return template;
	}

	private UpgradeManager getUpgradeManager() {
		return _pipe.getUpgradeManager();
	}

	public boolean isSatelliteConnected() {
	final List<ExitRoute> routes = getRouter().getIRoutersByCost();
		if(!_pipe.getUpgradeManager().isAdvancedSatelliteCrafter()) {
			if(satelliteId == 0) return true;
			for (final PipeItemsSatelliteLogistics satellite : PipeItemsSatelliteLogistics.AllSatellites) {
				if (satellite.satelliteId == satelliteId) {
					CoreRoutedPipe satPipe = satellite;
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					IRouter satRouter = satPipe.getRouter();
					for (ExitRoute route:routes) {
						if (route.destination == satRouter) {
							return true;
						}
					}
				}
			}
		} else {
			boolean foundAll = true;
			for(int i=0;i<9;i++) {
				boolean foundOne = false;
				if(advancedSatelliteIdArray[i] == 0) {
					continue;
				}
				for (final PipeItemsSatelliteLogistics satellite : PipeItemsSatelliteLogistics.AllSatellites) {
					if (satellite.satelliteId == advancedSatelliteIdArray[i]) {
						CoreRoutedPipe satPipe = satellite;
						if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
							continue;
						IRouter satRouter = satPipe.getRouter();
						for (ExitRoute route:routes) {
							if (route.destination == satRouter) {
								foundOne = true;
								break;
							}
						}
					}
				}
				foundAll &= foundOne;
			}
			return foundAll;
		}
		//TODO check for FluidCrafter
		return false;
	}

	@Override
	public List<ItemIdentifierStack> getCraftedItems() {
		//TODO: AECrafting check.
		List<ItemIdentifierStack> list = new ArrayList<ItemIdentifierStack>(1);
		if(_dummyInventory.getIDStackInSlot(9)!=null)
			list.add(_dummyInventory.getIDStackInSlot(9));
		return list;
	}

	@Override
	public int getTodo() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_CRAFTINGPIPE_ID;
	}



	protected int getNextConnectSatelliteId(boolean prev, int x) {
		int closestIdFound = prev ? 0 : Integer.MAX_VALUE;
		for (final PipeItemsSatelliteLogistics satellite : PipeItemsSatelliteLogistics.AllSatellites) {
			CoreRoutedPipe satPipe = satellite;
			if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null || satPipe.isFluidPipe()) continue;
			IRouter satRouter = satPipe.getRouter();
			List<ExitRoute> routes = getRouter().getDistanceTo(satRouter);
			if(routes != null && !routes.isEmpty()) {
				boolean filterFree = false;
				for(ExitRoute route: routes) {
					if(route.filters.isEmpty()) {
						filterFree = true;
						break;
					}
				}
				if(!filterFree) continue;
				if(x == -1) {
					if (!prev && satellite.satelliteId > satelliteId && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < satelliteId && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				} else {
					if (!prev && satellite.satelliteId > advancedSatelliteIdArray[x] && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < advancedSatelliteIdArray[x] && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				}
			}
		}
		if (closestIdFound == Integer.MAX_VALUE) {
			if(x == -1) {
				return satelliteId;
			} else {
				return advancedSatelliteIdArray[x];
			}
		}
		return closestIdFound;
	}
	
	protected int getNextConnectFluidSatelliteId(boolean prev, int x) {
		int closestIdFound = prev ? 0 : Integer.MAX_VALUE;
		for (final PipeFluidSatellite satellite : PipeFluidSatellite.AllSatellites) {
			CoreRoutedPipe satPipe = satellite;
			if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null || !satPipe.isFluidPipe()) continue;
			IRouter satRouter = satPipe.getRouter();
			List<ExitRoute> routes = getRouter().getDistanceTo(satRouter);
			if(routes != null && !routes.isEmpty()) {
				boolean filterFree = false;
				for(ExitRoute route: routes) {
					if(route.filters.isEmpty()) {
						filterFree = true;
						break;
					}
				}
				if(!filterFree) continue;
				if(x == -1) {
					if (!prev && satellite.satelliteId > liquidSatelliteId && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < liquidSatelliteId && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				} else {
					if (!prev && satellite.satelliteId > liquidSatelliteIdArray[x] && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < liquidSatelliteIdArray[x] && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				}
			}
		}
		if (closestIdFound == Integer.MAX_VALUE) {
			if(x == -1) {
				return liquidSatelliteId;
			} else {
				return liquidSatelliteIdArray[x];
			}
		}
		return closestIdFound;
	}

	public void setNextSatellite(EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeNextSatellite.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
			MainProxy.sendPacketToServer(packet);
		} else {
			satelliteId = getNextConnectSatelliteId(false, -1);
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteId.class).setPipeId(satelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
			MainProxy.sendPacketToPlayer(packet, (Player)player);
		}

	}
	
	// This is called by the packet PacketCraftingPipeSatelliteId
	public void setSatelliteId(int satelliteId, int x) {
		if(x == -1) {
			this.satelliteId = satelliteId;
		} else {
			advancedSatelliteIdArray[x] = satelliteId;
		}
	}

	public void setPrevSatellite(EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipePrevSatellite.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
			MainProxy.sendPacketToServer(packet);
		} else {
			satelliteId = getNextConnectSatelliteId(true, -1);
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteId.class).setPipeId(satelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
			MainProxy.sendPacketToPlayer(packet, (Player)player);
		}
	}

	public IRouter getSatelliteRouter(int x) {
		if(x == -1) {
			for (final PipeItemsSatelliteLogistics satellite : PipeItemsSatelliteLogistics.AllSatellites) {
				if (satellite.satelliteId == satelliteId) {
					CoreRoutedPipe satPipe = satellite;
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					return satPipe.getRouter();
				}
			}
		} else {
			for (final PipeItemsSatelliteLogistics satellite : PipeItemsSatelliteLogistics.AllSatellites) {
				if (satellite.satelliteId == advancedSatelliteIdArray[x]) {
					CoreRoutedPipe satPipe = satellite;
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					return satPipe.getRouter();
				}
			}
		}
		return null;
	}
	
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
//		super.readFromNBT(nbttagcompound);
		_dummyInventory.readFromNBT(nbttagcompound, "");
		_liquidInventory.readFromNBT(nbttagcompound, "FluidInv");
		satelliteId = nbttagcompound.getInteger("satelliteid");
		
		priority = nbttagcompound.getInteger("priority");
		for(int i=0;i<9;i++) {
			advancedSatelliteIdArray[i] = nbttagcompound.getInteger("advancedSatelliteId" + i);
		}
		for(int i=0;i<9;i++) {
			fuzzyCraftingFlagArray[i] = nbttagcompound.getByte("fuzzyCraftingFlag" + i);
		}
		for(int i=0;i<6;i++) {
			craftingSigns[i] = nbttagcompound.getBoolean("craftingSigns" + i);
		}
		if(nbttagcompound.hasKey("FluidAmount")) {
			amount = nbttagcompound.getIntArray("FluidAmount");
		}
		if(amount.length < ItemUpgrade.MAX_LIQUID_CRAFTER) {
			amount = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
		}
		for(int i=0;i<ItemUpgrade.MAX_LIQUID_CRAFTER;i++) {
			liquidSatelliteIdArray[i] = nbttagcompound.getInteger("liquidSatelliteIdArray" + i);
		}
		for(int i=0;i<ItemUpgrade.MAX_LIQUID_CRAFTER;i++) {
			liquidSatelliteIdArray[i] = nbttagcompound.getInteger("liquidSatelliteIdArray" + i);
		}
		liquidSatelliteId = nbttagcompound.getInteger("liquidSatelliteId"); 
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
//		super.writeToNBT(nbttagcompound);
		_dummyInventory.readFromNBT(nbttagcompound, "");
		_liquidInventory.readFromNBT(nbttagcompound, "FluidInv");
		satelliteId = nbttagcompound.getInteger("satelliteid");
		
		priority = nbttagcompound.getInteger("priority");
		for(int i=0;i<9;i++) {
			advancedSatelliteIdArray[i] = nbttagcompound.getInteger("advancedSatelliteId" + i);
		}
		for(int i=0;i<9;i++) {
			fuzzyCraftingFlagArray[i] = nbttagcompound.getByte("fuzzyCraftingFlag" + i);
		}
		if(nbttagcompound.hasKey("FluidAmount")) {
			amount = nbttagcompound.getIntArray("FluidAmount");
		}
		if(amount.length < ItemUpgrade.MAX_LIQUID_CRAFTER) {
			amount = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
		}
		for(int i=0;i<ItemUpgrade.MAX_LIQUID_CRAFTER;i++) {
			liquidSatelliteIdArray[i] = nbttagcompound.getInteger("liquidSatelliteIdArray" + i);
		}
		for(int i=0;i<ItemUpgrade.MAX_LIQUID_CRAFTER;i++) {
			liquidSatelliteIdArray[i] = nbttagcompound.getInteger("liquidSatelliteIdArray" + i);
		}
		liquidSatelliteId = nbttagcompound.getInteger("liquidSatelliteId");
		
		for(int i=0;i<6;i++) {
			if(nbttagcompound.getBoolean("craftingSigns" + i)) {
				_pipe.addPipeSign(ForgeDirection.getOrientation(i), new CraftingPipeSign(), null);
			}
		}
	}
	
	public ModernPacket getCPipePacket() {
		return PacketHandler.getPacket(CraftingPipeUpdatePacket.class).setAmount(amount).setLiquidSatelliteIdArray(liquidSatelliteIdArray).setLiquidSatelliteId(liquidSatelliteId).setCraftingSigns(craftingSigns).setSatelliteId(satelliteId).setAdvancedSatelliteIdArray(advancedSatelliteIdArray).setFuzzyCraftingFlagArray(fuzzyCraftingFlagArray).setPriority(priority).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
	}
	
	public void handleCraftingUpdatePacket(CraftingPipeUpdatePacket packet) {
		amount = packet.getAmount();
		liquidSatelliteIdArray = packet.getLiquidSatelliteIdArray();
		liquidSatelliteId = packet.getLiquidSatelliteId();
		craftingSigns = packet.getCraftingSigns();
		satelliteId = packet.getSatelliteId();
		advancedSatelliteIdArray = packet.getAdvancedSatelliteIdArray();
		fuzzyCraftingFlagArray = packet.getFuzzyCraftingFlagArray();
		priority = packet.getPriority();
	}
	
	public void onWrenchClicked(EntityPlayer entityplayer) {
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(GuiArgument.class)
				.setGuiID(GuiIDs.GUI_CRAFTINGPIPE_ID)
				.setArgs(new Object[]{getUpgradeManager().isAdvancedSatelliteCrafter(),
						getUpgradeManager().getFluidCrafter(),
						amount,
						getUpgradeManager().hasByproductExtractor(),
						getUpgradeManager().isFuzzyCrafter()}),
						(Player) entityplayer);
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_CRAFTINGPIPE_ID, _pipe.getWorld(), getX(), getY(), getZ());
	}
	
	public List<ForgeDirection> getCraftingSigns() {
		List<ForgeDirection> list = new ArrayList<ForgeDirection>();
		for(int i=0;i<6;i++) {
			if(craftingSigns[i]) {
				list.add(ForgeDirection.VALID_DIRECTIONS[i]);
			}
		}
		return list;
	}

	public boolean setCraftingSign(ForgeDirection dir, boolean b, EntityPlayer player) {
		if(dir.ordinal() < 6) {
			if(craftingSigns[dir.ordinal()] != b) {
				craftingSigns[dir.ordinal()] = b;
				final ModernPacket packetA = this.getCPipePacket();
				final ModernPacket packetB = PacketHandler.getPacket(CPipeSatelliteImportBack.class).setInventory(getDummyInventory()).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
				if(player != null) {
					MainProxy.sendPacketToPlayer(packetA, (Player)player);
					MainProxy.sendPacketToPlayer(packetB, (Player)player);
				}
				MainProxy.sendPacketToAllWatchingChunk(getX(), getZ(), MainProxy.getDimensionForWorld(_pipe.getWorld()), packetA);
				MainProxy.sendPacketToAllWatchingChunk(getX(), getZ(), MainProxy.getDimensionForWorld(_pipe.getWorld()), packetB);
				_pipe.refreshRender(false);
				return true;
			}
		}
		return false;
	}

	/**
	 * Simply get the dummy inventory
	 * 
	 * @return the dummy inventory
	 */
	public ItemIdentifierInventory getDummyInventory() {
		return _dummyInventory;
	}

	public ItemIdentifierInventory getFluidInventory() {
		return _liquidInventory;
	}
	
	public void setDummyInventorySlot(int slot, ItemStack itemstack) {
		_dummyInventory.setInventorySlotContents(slot, itemstack);
	}
	
		public void importFromCraftingTable(EntityPlayer player) {
		if (MainProxy.isClient(getWorld())) {
			// Send packet asking for import
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteImport.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
			MainProxy.sendPacketToServer(packet);
		} else{
			boolean fuzzyFlagsChanged = false;
			final WorldUtil worldUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
			for (final AdjacentTile tile : worldUtil.getAdjacentTileEntities(true)) {
				for (ICraftingRecipeProvider provider : SimpleServiceLocator.craftingRecipeProviders) {
					if (provider.importRecipe(tile.tile, _dummyInventory)) {
						if (provider instanceof IFuzzyRecipeProvider) {
							fuzzyFlagsChanged = ((IFuzzyRecipeProvider)provider).importFuzzyFlags(tile.tile, _dummyInventory, fuzzyCraftingFlagArray);
						}
						break;
					}
				}
			}
			// Send inventory as packet
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteImportBack.class).setInventory(_dummyInventory).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
			if(player != null) {
				MainProxy.sendPacketToPlayer(packet, (Player)player);
			}
			MainProxy.sendPacketToAllWatchingChunk(this.getX(), this.getZ(), MainProxy.getDimensionForWorld(getWorld()), packet);
			
			if(fuzzyFlagsChanged && this.getUpgradeManager().isFuzzyCrafter()) {
				for (int i = 0; i < 9; i++) {
					final ModernPacket pak = PacketHandler.getPacket(CraftingFuzzyFlag.class).setInteger2(fuzzyCraftingFlagArray[i]).setInteger(i).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
					if(player != null)
						MainProxy.sendPacketToPlayer(pak, (Player)player);
					MainProxy.sendPacketToAllWatchingChunk(this.getX(), this.getZ(), MainProxy.getDimensionForWorld(getWorld()), pak);
				}
			}
		}
	}

	private World getWorld() {
			return _pipe.getWorld();
		}

	public void handleStackMove(int number) {
		if(MainProxy.isClient(this.getWorld())) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipeStackMovePacket.class).setInteger(number).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		}
		ItemStack stack = _dummyInventory.getStackInSlot(number);
		if(stack == null ) return;
		for(int i = 6;i < 9;i++) {
			ItemStack stackb = _dummyInventory.getStackInSlot(i);
			if(stackb == null) {
				_dummyInventory.setInventorySlotContents(i, stack);
				_dummyInventory.clearInventorySlotContents(number);
				break;
			}
		}
	}
	
	public void priorityUp(EntityPlayer player) {
		priority++;
		if(MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipePriorityUpPacket.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else if(player != null && MainProxy.isServer(player.worldObj)) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingPriority.class).setInteger(priority).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
		}
	}
	
	public void priorityDown(EntityPlayer player) {
		priority--;
		if(MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipePriorityDownPacket.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else if(player != null && MainProxy.isServer(player.worldObj)) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingPriority.class).setInteger(priority).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
		}
	}
	
	public void setPriority(int amount) {
		priority = amount;
	}

	public ItemIdentifierStack getByproductItem() {
		return _dummyInventory.getIDStackInSlot(10);
	}
	
	public ItemIdentifierStack getMaterials(int slotnr) {
		return _dummyInventory.getIDStackInSlot(slotnr);
	}

	public FluidIdentifier getFluidMaterial(int slotnr) {
		ItemIdentifierStack stack = _liquidInventory.getIDStackInSlot(slotnr);
		if(stack == null) return null;
		return FluidIdentifier.get(stack.getItem());
	}
	

	public void setNextSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipeNextAdvancedSatellitePacket.class).setInteger(i).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else {
			advancedSatelliteIdArray[i] = getNextConnectSatelliteId(false, i);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(advancedSatelliteIdArray[i]).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
		}
	}

	public void setPrevSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipePrevAdvancedSatellitePacket.class).setInteger(i).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else {
			advancedSatelliteIdArray[i] = getNextConnectSatelliteId(true, i);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(advancedSatelliteIdArray[i]).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
		}
	}

	public void changeFluidAmount(int change, int slot, EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(FluidCraftingAmount.class).setInteger2(slot).setInteger(change).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else {
			amount[slot] += change;
			if(amount[slot] <= 0) {
				amount[slot] = 0;
			}
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAmount.class).setInteger2(slot).setInteger(amount[slot]).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
		}
	}

		public void setPrevFluidSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(FluidCraftingPipeAdvancedSatellitePrevPacket.class).setInteger(i).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else {
			if(i == -1) {
				liquidSatelliteId = getNextConnectFluidSatelliteId(true, i);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(liquidSatelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
			} else {
				liquidSatelliteIdArray[i] = getNextConnectFluidSatelliteId(true, i);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(liquidSatelliteIdArray[i]).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
			}
		}
	}

	public void setNextFluidSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(FluidCraftingPipeAdvancedSatelliteNextPacket.class).setInteger(i).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else {
			if(i == -1) {
				liquidSatelliteId = getNextConnectFluidSatelliteId(false, i);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(liquidSatelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
			} else {
				liquidSatelliteIdArray[i] = getNextConnectFluidSatelliteId(false, i);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(liquidSatelliteIdArray[i]).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
			}
		}
	}

	public void setFluidAmount(int[] amount) {
		if(MainProxy.isClient(getWorld())) {
			this.amount = amount;
		}
	}

	public void defineFluidAmount(int integer, int slot) {
		if(MainProxy.isClient(getWorld())) {
			amount[slot] = integer;
		}
	}
	
	public int[] getFluidAmount() {
		return amount;
	}

	public void setFluidSatelliteId(int integer, int slot) {
		if(slot == -1) {
			liquidSatelliteId = integer;
		} else {
			liquidSatelliteIdArray[slot] = integer;
		}	
	}


	public IRouter getFluidSatelliteRouter(int x) {
		if(x == -1) {
			for (final PipeFluidSatellite satellite : PipeFluidSatellite.AllSatellites) {
				if (satellite.satelliteId == liquidSatelliteId) {
					CoreRoutedPipe satPipe = satellite;
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					return satPipe.getRouter();
				}
			}
		} else {
			for (final PipeFluidSatellite satellite : PipeFluidSatellite.AllSatellites) {
				if (satellite.satelliteId == liquidSatelliteIdArray[x]) {
					CoreRoutedPipe satPipe = satellite;
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					return satPipe.getRouter();
				}
			}
		}
		return null;
	}

	public void setFuzzyCraftingFlag(int slot, int flag, EntityPlayer player)
	{
		if(slot < 0 || slot >= 9)
			return;
		if(MainProxy.isClient(this.getWorld()))
			if(player == null)
				MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingFuzzyFlag.class).setInteger2(flag).setInteger(slot).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
			else
				fuzzyCraftingFlagArray[slot] = flag;
		else
		{
			fuzzyCraftingFlagArray[slot] ^= 1 << flag;
			ModernPacket pak = PacketHandler.getPacket(CraftingFuzzyFlag.class).setInteger2(fuzzyCraftingFlagArray[slot]).setInteger(slot).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
			if(player != null)
				MainProxy.sendPacketToPlayer(pak, (Player)player);
			MainProxy.sendPacketToAllWatchingChunk(getX(), getZ(), MainProxy.getDimensionForWorld(getWorld()), pak);
		}
	}

	public boolean hasCraftingSign() {
		for(int i=0;i<6;i++) {
			if(signItem[i] instanceof CraftingPipeSign) {
				return true;
			}
		}
		return false;
	}
/*
	@Override
	public void startWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}
*/


}
