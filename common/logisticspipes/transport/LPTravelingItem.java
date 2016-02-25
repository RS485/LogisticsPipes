package logisticspipes.transport;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequireReliableFluidTransport;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.IRouterManager;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.order.IDistanceTracker;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.SlidingWindowBitSet;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;
import lombok.Setter;

public abstract class LPTravelingItem {

	public static final Map<Integer, WeakReference<LPTravelingItemServer>> serverList = new HashMap<Integer, WeakReference<LPTravelingItemServer>>();
	public static final Map<Integer, WeakReference<LPTravelingItemClient>> clientList = new HashMap<Integer, WeakReference<LPTravelingItemClient>>();
	public static final List<Pair<Integer, Object>> forceKeep = new ArrayList<Pair<Integer, Object>>();
	public static final SlidingWindowBitSet clientSideKnownIDs = new SlidingWindowBitSet(20); // 20

	private static int nextFreeId = 0;
	protected int id;
	protected float speed = 0.01F;

	public int lastTicked = 0;

	protected TileEntity container;
	protected float position = 0;
	public ForgeDirection input = ForgeDirection.UNKNOWN;
	public ForgeDirection output = ForgeDirection.UNKNOWN;
	public final EnumSet<ForgeDirection> blacklist = EnumSet.noneOf(ForgeDirection.class);

	public LPTravelingItem() {
		id = getNextId();
	}

	public LPTravelingItem(int id, float position, ForgeDirection input, ForgeDirection output) {
		this.id = id;
		this.position = position;
		this.input = input;
		this.output = output;
	}

	public LPTravelingItem(int id) {
		this.id = id;
	}

	protected int getNextId() {
		return ++LPTravelingItem.nextFreeId;
	}

	public void setPosition(float position) {
		this.position = position;
	}

	public float getPosition() {
		return position;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public void setContainer(TileEntity container) {
		this.container = container;
	}

	public TileEntity getContainer() {
		return container;
	}

	public int getId() {
		return id;
	}

	public abstract ItemIdentifierStack getItemIdentifierStack();

	public boolean isCorrupted() {
		return getItemIdentifierStack() == null || getItemIdentifierStack().getStackSize() <= 0;
	}

	public int getAge() {
		return 0;
	}

	public void addAge() {}

	public float getHoverStart() {
		return 0;
	}

	public static final class LPTravelingItemClient extends LPTravelingItem {

		@Setter
		private ItemIdentifierStack item;
		private int age;
		private float hoverStart = (float) (Math.random() * Math.PI * 2.0D);

		public LPTravelingItemClient(int id, float position, ForgeDirection input, ForgeDirection output) {
			super(id, position, input, output);
		}

		public LPTravelingItemClient(int id, ItemIdentifierStack stack) {
			super(id);
			item = stack;
		}

		@Override
		public ItemIdentifierStack getItemIdentifierStack() {
			return item;
		}

		public void updateInformation(ForgeDirection input, ForgeDirection output, float speed, float position) {
			this.input = input;
			this.output = output;
			this.speed = speed;
			this.position = position;
		}

		@Override
		public int getAge() {
			return 0;//age;
		}

		@Override
		public void addAge() {
			age++;
		}

		@Override
		public float getHoverStart() {
			return 0;//hoverStart;
		}
	}

	public static final class LPTravelingItemServer extends LPTravelingItem implements IRoutedItem {

		@Getter
		private ItemRoutingInformation info;

		public LPTravelingItemServer(ItemIdentifierStack stack) {
			super();
			info = new ItemRoutingInformation();
			info.setItem(stack);
		}

		public LPTravelingItemServer(ItemRoutingInformation info) {
			super();
			this.info = info;
		}

		public LPTravelingItemServer(NBTTagCompound data) {
			super();
			info = new ItemRoutingInformation();
			readFromNBT(data);
		}

		@Override
		public ItemIdentifierStack getItemIdentifierStack() {
			return info.getItem();
		}

		public void setInformation(ItemRoutingInformation info) {
			this.info = info;
		}

		@Override
		public void readFromNBT(NBTTagCompound data) {
			setPosition(data.getFloat("position"));
			setSpeed(data.getFloat("speed"));
			input = ForgeDirection.getOrientation(data.getInteger("input"));
			output = ForgeDirection.getOrientation(data.getInteger("output"));
			info.readFromNBT(data);
		}

		@Override
		public void writeToNBT(NBTTagCompound data) {
			data.setFloat("position", getPosition());
			data.setFloat("speed", getSpeed());
			data.setInteger("input", input.ordinal());
			if (output != null) {
				data.setInteger("output", output.ordinal());
			}
			info.writeToNBT(data);
		}

		public EntityItem toEntityItem() {
			World worldObj = container.getWorldObj();
			if (MainProxy.isServer(worldObj)) {
				if (getItemIdentifierStack().getStackSize() <= 0) {
					return null;
				}

				if (getItemIdentifierStack().makeNormalStack().getItem() instanceof LogisticsFluidContainer) {
					itemWasLost();
					return null;
				}

				ForgeDirection exitdirection = output;
				if (exitdirection == ForgeDirection.UNKNOWN) {
					exitdirection = input;
				}

				LPPosition position = new LPPosition(container.xCoord + 0.5, container.yCoord + 0.375, container.zCoord + 0.5);

				switch (exitdirection) {
					case DOWN:
						position.moveForward(exitdirection, 0.5);
						break;
					case UP:
						position.moveForward(exitdirection, 0.75);
						break;
					case NORTH:
					case SOUTH:
					case WEST:
					case EAST:
						position.moveForward(exitdirection, 0.625);
						break;
					case UNKNOWN:
					default:
						break;
				}

				LPPosition motion = new LPPosition(0, 0, 0);
				motion.moveForward(exitdirection, getSpeed() * 2F);

				EntityItem entityitem = getItemIdentifierStack().makeEntityItem(worldObj, position.getXD(), position.getYD(), position.getZD());

				//entityitem.lifespan = 1200;
				//entityitem.delayBeforeCanPickup = 10;

				//uniformly distributed in -0.005 .. 0.01 to increase bias toward smaller values
				float f3 = worldObj.rand.nextFloat() * 0.015F - 0.005F;
				entityitem.motionX = (float) worldObj.rand.nextGaussian() * f3 + motion.getXD();
				entityitem.motionY = (float) worldObj.rand.nextGaussian() * f3 + motion.getYD();
				entityitem.motionZ = (float) worldObj.rand.nextGaussian() * f3 + motion.getZD();
				itemWasLost();

				return entityitem;
			} else {
				return null;
			}
		}

		@Override
		public void clearDestination() {
			if (info.destinationint >= 0) {
				itemWasLost();
				info.jamlist.add(info.destinationint);
			}
			//keep buffercounter and jamlist
			info.destinationint = -1;
			info.destinationUUID = null;
			info._doNotBuffer = false;
			info.arrived = false;
			info._transportMode = TransportMode.Unknown;
			info.targetInfo = null;
		}

		public void itemWasLost() {
			if (container != null) {
				if (MainProxy.isClient(container.getWorldObj())) {
					return;
				}
			}
			if (info.destinationint >= 0 && SimpleServiceLocator.routerManager.isRouter(info.destinationint)) {
				IRouter destinationRouter = SimpleServiceLocator.routerManager.getRouter(info.destinationint);
				if (destinationRouter.getPipe() != null) {
					destinationRouter.getPipe().notifyOfReroute(info);
					if (destinationRouter.getPipe() instanceof IRequireReliableTransport) {
						((IRequireReliableTransport) destinationRouter.getPipe()).itemLost(info.getItem().clone(), info.targetInfo);
					}
					if (destinationRouter.getPipe() instanceof IRequireReliableFluidTransport) {
						if (info.getItem().getItem().isFluidContainer()) {
							FluidStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(info.getItem());
							((IRequireReliableFluidTransport) destinationRouter.getPipe()).liquidLost(FluidIdentifier.get(liquid), liquid.amount);
						}
					}
				}
			}
		}

		@Override
		public int getDestination() {
			return info.destinationint;
		}

		@Override
		public void setDestination(int destination) {
			info.destinationint = destination;
			IRouter router = SimpleServiceLocator.routerManager.getRouter(destination);
			if (router != null) {
				info.destinationUUID = router.getId();
			} else {
				info.destinationUUID = null;
			}
		}

		@Override
		public void setDoNotBuffer(boolean isBuffered) {
			info._doNotBuffer = isBuffered;
		}

		@Override
		public boolean getDoNotBuffer() {
			return info._doNotBuffer;
		}

		@Override
		public void setArrived(boolean flag) {
			info.arrived = flag;
		}

		@Override
		public boolean getArrived() {
			return info.arrived;
		}

		@Override
		public void split(int itemsToTake, ForgeDirection orientation) {
			if (getItemIdentifierStack().getItem().isFluidContainer()) {
				throw new UnsupportedOperationException("Can't split up a FluidContainer");
			}
			ItemIdentifierStack stackToKeep = getItemIdentifierStack();
			ItemIdentifierStack stackToSend = stackToKeep.clone();
			stackToKeep.setStackSize(itemsToTake);
			stackToSend.setStackSize(stackToSend.getStackSize() - itemsToTake);

			newId();

			LPTravelingItemServer newItem = new LPTravelingItemServer(stackToSend);
			newItem.setSpeed(getSpeed());
			newItem.setTransportMode(getTransportMode());

			newItem.setDestination(getDestination());
			newItem.clearDestination();

			if (container instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) container).pipe.transport instanceof PipeTransportLogistics) {
				((LogisticsTileGenericPipe) container).pipe.transport.injectItem((LPTravelingItem) newItem, orientation);
			}
		}

		@Override
		public void setTransportMode(TransportMode transportMode) {
			info._transportMode = transportMode;
		}

		@Override
		public TransportMode getTransportMode() {
			return info._transportMode;
		}

		@Override
		public void addToJamList(IRouter router) {
			info.jamlist.add(router.getSimpleID());
		}

		@Override
		public List<Integer> getJamList() {
			return info.jamlist;
		}

		@Override
		public int getBufferCounter() {
			return info.bufferCounter;
		}

		@Override
		public void setBufferCounter(int counter) {
			info.bufferCounter = counter;
		}

		@Override
		public UUID getDestinationUUID() {
			return info.destinationUUID;
		}

		@Override
		public void checkIDFromUUID() {
			IRouterManager rm = SimpleServiceLocator.routerManager;
			IRouter router = rm.getRouter(info.destinationint);
			if (router == null || info.destinationUUID != router.getId()) {
				info.destinationint = rm.getIDforUUID(info.destinationUUID);
			}
		}

		public void refreshDestinationInformation() {
			IRouter destinationRouter = SimpleServiceLocator.routerManager.getRouter(info.destinationint);
			if (destinationRouter != null && destinationRouter.getPipe() instanceof CoreRoutedPipe) {
				destinationRouter.getPipe().refreshItem(getInfo());
			}
		}

		@Override
		public void setDistanceTracker(IDistanceTracker tracker) {
			info.tracker = tracker;
		}

		@Override
		public IDistanceTracker getDistanceTracker() {
			return info.tracker;
		}

		public void resetDelay() {
			info.resetDelay();
		}

		@Override
		public void setAdditionalTargetInformation(IAdditionalTargetInformation targetInfo) {
			info.targetInfo = targetInfo;
		}

		@Override
		public IAdditionalTargetInformation getAdditionalTargetInformation() {
			return info.targetInfo;
		}

		public void newId() {
			id = getNextId();
		}
	}
}
