package logisticspipes.pipes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;

import net.minecraftforge.fluids.FluidTank;

import lombok.Getter;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.interfaces.routing.IRequireReliableFluidTransport;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleFluidSupplierMK2;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.FluidSupplierAmount;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestTree;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.item.ItemIdentifierStack;

public class PipeFluidSupplierMk2 extends FluidRoutedPipe implements IRequestFluid, IRequireReliableFluidTransport {

	private boolean _lastRequestFailed = false;

	public enum MinMode {
		NONE(0),
		ONEBUCKET(1000),
		TWOBUCKET(2000),
		FIVEBUCKET(5000);

		@Getter
		private final int amount;

		MinMode(int amount) {
			this.amount = amount;
		}
	}

	private final ModuleFluidSupplierMK2 moduleFluidSupplierMK2;

	public PipeFluidSupplierMk2(Item item) {
		super(item);
		throttleTime = 100;
		moduleFluidSupplierMK2 = new ModuleFluidSupplierMK2();
		moduleFluidSupplierMK2.registerHandler(this, this);
		moduleFluidSupplierMK2.registerPosition(LogisticsModule.ModulePositionType.IN_PIPE, 0);
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
	public boolean isRequestFailed() {
		return _lastRequestFailed;
	}

	public void setRequestFailed(boolean value) {
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
	private final Map<FluidIdentifier, Integer> _requestedItems = new HashMap<>();

	@Override
	public void throttledUpdateEntity() {
		if (!isEnabled()) {
			return;
		}
		if (MainProxy.isClient(Objects.requireNonNull(container).getWorld())) {
			return;
		}
		super.throttledUpdateEntity();
		if (moduleFluidSupplierMK2.dummyInventory.getIDStackInSlot(0) == null) {
			return;
		}

		PipeFluidUtil.INSTANCE.getAdjacentTanks(this, false).forEach(fluidHandlerDirectionPair -> {
			if (!fluidHandlerDirectionPair.getValue2().containsTanks()) {
				return;
			}

			//How much do I want?
			Map<FluidIdentifier, Integer> wantFluids = new HashMap<>();
			ItemIdentifierStack stack = moduleFluidSupplierMK2.dummyInventory.getIDStackInSlot(0);
			if (stack == null) return;
			FluidIdentifier fIdent = FluidIdentifier.get(stack.getItem());
			wantFluids.put(fIdent, moduleFluidSupplierMK2.amount.getValue());

			//How much do I have?
			HashMap<FluidIdentifier, Integer> haveFluids = new HashMap<>();

			//Check what is inside the connected tank
			fluidHandlerDirectionPair.getValue2().tanks()
					.map(tank -> FluidIdentifierStack.getFromStack(tank.getContents()))
					.filter(Objects::nonNull)
					.forEach(fluid -> haveFluids.merge(fluid.getFluid(), fluid.getAmount(), Integer::sum));

			//What does our sided internal tank have
			int directionOrdinal = fluidHandlerDirectionPair.getValue1().getDirection().ordinal();
			if (directionOrdinal < ((PipeFluidTransportLogistics) transport).sideTanks.length) {
				FluidTank sideTank = ((PipeFluidTransportLogistics) transport).sideTanks[directionOrdinal];
				if (sideTank != null && sideTank.getFluid() != null && wantFluids.containsKey(FluidIdentifier.get(sideTank.getFluid()))) {
					haveFluids.merge(FluidIdentifier.get(sideTank.getFluid()), sideTank.getFluid().amount, Integer::sum);
				}
			}

			//What does our center internal tank have
			FluidTank centerTank = ((PipeFluidTransportLogistics) transport).internalTank;
			if (centerTank != null && centerTank.getFluid() != null && wantFluids.containsKey(FluidIdentifier.get(centerTank.getFluid()))) {
				haveFluids.merge(FluidIdentifier.get(centerTank.getFluid()), centerTank.getFluid().amount, Integer::sum);
			}

			//HashMap<Integer, Integer> needFluids = new HashMap<Integer, Integer>();
			//Reduce what I have and what have been requested already
			for (Entry<FluidIdentifier, Integer> liquidId : wantFluids.entrySet()) {
				Integer haveCount = haveFluids.get(liquidId.getKey());
				if (haveCount != null) {
					liquidId.setValue(liquidId.getValue() - haveCount);
				}
				//@formatter:off
						_requestedItems.entrySet().stream()
								.filter(requestedItem -> requestedItem.getKey().equals(liquidId.getKey()))
								.forEach(requestedItem -> liquidId.setValue(liquidId.getValue() - requestedItem.getValue()));
						//@formatter:on
			}

			setRequestFailed(false);

			//Make request

			for (FluidIdentifier need : wantFluids.keySet()) {
				int countToRequest = wantFluids.get(need);
				if (countToRequest < 1) {
					continue;
				}
				if (moduleFluidSupplierMK2._bucketMinimum.getValue().getAmount() != 0
					&& countToRequest < moduleFluidSupplierMK2._bucketMinimum.getValue().getAmount()) {
					continue;
				}

				if (!useEnergy(11)) {
					break;
				}

				boolean success = false;

				if (moduleFluidSupplierMK2._requestPartials.getValue()) {
					countToRequest = RequestTree.requestFluidPartial(need, countToRequest, this, null);
					if (countToRequest > 0) {
						success = true;
					}
				} else {
					success = RequestTree.requestFluid(need, countToRequest, this, null);
				}

				if (success) {
					Integer currentRequest = _requestedItems.get(need);
					if (currentRequest == null) {
						_requestedItems.put(need, countToRequest);
					} else {
						_requestedItems.put(need, currentRequest + countToRequest);
					}
				} else {
					setRequestFailed(true);
				}
			}
		});
	}

	private void decreaseRequested(FluidIdentifier liquid, int remaining) {
		//see if we can get an exact match
		Integer count = _requestedItems.get(liquid);
		if (count != null) {
			_requestedItems.put(liquid, Math.max(0, count - remaining));
			remaining -= count;
		}
		if (remaining <= 0) {
			return;
		}
		//still remaining... was from fuzzyMatch on a crafter
		for (Entry<FluidIdentifier, Integer> e : _requestedItems.entrySet()) {
			if (e.getKey().equals(liquid)) {
				int expected = e.getValue();
				e.setValue(Math.max(0, expected - remaining));
				remaining -= expected;
			}
			if (remaining <= 0) {
				return;
			}
		}
		//we have no idea what this is, log it.
		debug.log("liquid supplier got unexpected item " + liquid.toString());
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

	public boolean isRequestingPartials() {
		return moduleFluidSupplierMK2._requestPartials.getValue();
	}

	public void setRequestingPartials(boolean value) {
		moduleFluidSupplierMK2._requestPartials.setValue(value);
	}

	public MinMode getMinMode() {
		return moduleFluidSupplierMK2._bucketMinimum.getValue();
	}

	public void setMinMode(MinMode value) {
		moduleFluidSupplierMK2._bucketMinimum.setValue(value);
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_FluidSupplier_MK2_ID, getWorld(), getX(), getY(), getZ());
	}

	public IInventory getDummyInventory() {
		return moduleFluidSupplierMK2.dummyInventory;
	}

	public int getAmount() {
		return moduleFluidSupplierMK2.amount.getValue();
	}

	public void setAmount(int amount) {
		if (MainProxy.isClient(Objects.requireNonNull(container).getWorld())) {
			moduleFluidSupplierMK2.amount.setValue(amount);
		}
	}

	public void changeFluidAmount(int change, EntityPlayer player) {
		moduleFluidSupplierMK2.amount.increase(change);
		if (moduleFluidSupplierMK2.amount.getValue() <= 0) {
			moduleFluidSupplierMK2.amount.setValue(0);
		}
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidSupplierAmount.class)
			.setInteger(moduleFluidSupplierMK2.amount.getValue()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), player);
	}

	@Override
	public boolean canReceiveFluid() {
		return false;
	}
}
