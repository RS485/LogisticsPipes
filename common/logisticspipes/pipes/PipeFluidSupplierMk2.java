package logisticspipes.pipes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;
import logisticspipes.LogisticsPipes;
import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.interfaces.routing.IRequireReliableFluidTransport;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.FluidSupplierAmount;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestTree;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.WorldUtil;

public class PipeFluidSupplierMk2 extends FluidRoutedPipe implements IRequestFluid, IRequireReliableFluidTransport {

	private boolean _lastRequestFailed = false;

	public PipeFluidSupplierMk2(int ItemID) {
		super(ItemID);
		throttleTime = 100;
	}

	@Override
	public void sendFailed(FluidIdentifier value1, Integer value2) {
		liquidLost(value1, value2);
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Fast;
	}

	@Override
	public boolean canInsertFromSideToTanks() {
		return true;
	}

	@Override
	public boolean canInsertToTanks() {
		return true;
	}

	/* TRIGGER INTERFACE */
	public boolean isRequestFailed(){
		return _lastRequestFailed;
	}

	public void setRequestFailed(boolean value){
		_lastRequestFailed = value;
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUIDSUPPLIER_MK2_TEXTURE;
	}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}
	
	//from PipeFluidSupplierMk2
	private SimpleInventory dummyInventory = new SimpleInventory(1, "Fluid to keep stocked", 127);
	private int amount = 0;
	
	private final Map<FluidIdentifier, Integer> _requestedItems = new HashMap<FluidIdentifier, Integer>();
	
	private boolean _requestPartials = false;

	@Override
	public void throttledUpdateEntity() {
		if (MainProxy.isClient(container.getWorld())) return;
		super.throttledUpdateEntity();
		if(dummyInventory.getStackInSlot(0) == null) return;
		WorldUtil worldUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
		for (AdjacentTile tile :  worldUtil.getAdjacentTileEntities(true)){
			if (!(tile.tile instanceof IFluidHandler) || tile.tile instanceof TileGenericPipe) continue;
			IFluidHandler container = (IFluidHandler) tile.tile;
			if (container.getTankInfo(ForgeDirection.UNKNOWN) == null || container.getTankInfo(ForgeDirection.UNKNOWN).length == 0) continue;
			
			//How much do I want?
			Map<FluidIdentifier, Integer> wantFluids = new HashMap<FluidIdentifier, Integer>();
			wantFluids.put(dummyInventory.getIDStackInSlot(0).getItem().getFluidIdentifier(), amount);

			//How much do I have?
			HashMap<FluidIdentifier, Integer> haveFluids = new HashMap<FluidIdentifier, Integer>();
			
			FluidTankInfo[] result = container.getTankInfo(ForgeDirection.UNKNOWN);
			for (FluidTankInfo slot : result){
				if (slot.fluid == null || !wantFluids.containsKey(FluidIdentifier.get(slot.fluid))) continue;
				Integer liquidWant = haveFluids.get(FluidIdentifier.get(slot.fluid));
				if (liquidWant==null){
					haveFluids.put(FluidIdentifier.get(slot.fluid), slot.fluid.amount);
				} else {
					haveFluids.put(FluidIdentifier.get(slot.fluid), liquidWant +  slot.fluid.amount);
				}
			}
			
			//HashMap<Integer, Integer> needFluids = new HashMap<Integer, Integer>();
			//Reduce what I have and what have been requested already
			for (Entry<FluidIdentifier, Integer> liquidId: wantFluids.entrySet()){
				Integer haveCount = haveFluids.get(liquidId.getKey());
				if (haveCount != null){
					liquidId.setValue(liquidId.getValue() - haveCount);
				}
				for (Entry<FluidIdentifier, Integer> requestedItem : _requestedItems.entrySet()){
					if(requestedItem.getKey() == liquidId.getKey()) {
						liquidId.setValue(liquidId.getValue() - requestedItem.getValue());
					}
				}
			}
			
			setRequestFailed(false);
			
			//Make request
			
			for (FluidIdentifier need : wantFluids.keySet()){
				int countToRequest = wantFluids.get(need);
				if (countToRequest < 1) continue;
				
				if(!useEnergy(11)) {
					break;
				}
				
				boolean success = false;

				if(_requestPartials) {
					countToRequest = RequestTree.requestFluidPartial(need, countToRequest, (IRequestFluid) this.container, null);
					if(countToRequest > 0) {
						success = true;
					}
				} else {
					success = RequestTree.requestFluid(need, countToRequest, (IRequestFluid) this.container, null);
				}
				
				if (success){
					Integer currentRequest = _requestedItems.get(need);
					if(currentRequest==null) {
						_requestedItems.put(need, countToRequest);
					} else {
						_requestedItems.put(need, currentRequest + countToRequest);
					}
				} else{
					setRequestFailed(true);
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);	
		dummyInventory.readFromNBT(nbttagcompound, "");
		_requestPartials = nbttagcompound.getBoolean("requestpartials");
		amount = nbttagcompound.getInteger("amount");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);
    	dummyInventory.writeToNBT(nbttagcompound, "");
    	nbttagcompound.setBoolean("requestpartials", _requestPartials);
    	nbttagcompound.setInteger("amount", amount);
    }
	
	private void decreaseRequested(FluidIdentifier liquid, int remaining) {
		//see if we can get an exact match
		Integer count = _requestedItems.get(liquid);
		if (count != null) {
			_requestedItems.put(liquid, Math.max(0, count - remaining));
			remaining -= count;
		}
		if(remaining <= 0) {
			return;
		}
		//still remaining... was from fuzzyMatch on a crafter
		for(Entry<FluidIdentifier, Integer> e : _requestedItems.entrySet()) {
			if(e.getKey() == liquid) {
				int expected = e.getValue();
				e.setValue(Math.max(0, expected - remaining));
				remaining -= expected;
			}
			if(remaining <= 0) {
				return;
			}
		}
		//we have no idea what this is, log it.
		LogisticsPipes.requestLog.info("liquid supplier got unexpected item " + liquid.toString());
	}

	@Override
	public void liquidLost(FluidIdentifier item, int amount) {
		decreaseRequested(item, amount);
	}

	@Override
	public void liquidArrived(FluidIdentifier item, int amount) {
		decreaseRequested(item, amount);
		delayThrottle();
	}

	@Override
	public void liquidNotInserted(FluidIdentifier item, int amount) {}
	
	public boolean isRequestingPartials(){
		return _requestPartials;
	}
	
	public void setRequestingPartials(boolean value){
		_requestPartials = value;
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		if(MainProxy.isServer(entityplayer.worldObj)) {
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_FluidSupplier_MK2_ID, getWorld(), getX(), getY(), getZ());
		}
	}

	public IInventory getDummyInventory() {
		return dummyInventory;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		if(MainProxy.isClient(container.getWorld())) {
			this.amount = amount;
		}
	}

	public void changeFluidAmount(int change, EntityPlayer player) {
		amount += change;
		if(amount <= 0) {
			amount = 0;
		}
//TODO 	MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.LIQUID_SUPPLIER_LIQUID_AMOUNT, xCoord, yCoord, zCoord, amount).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidSupplierAmount.class).setInteger(amount).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
	}
}
