package logisticspipes.transport;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import lombok.Getter;
import lombok.Setter;

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
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.order.IDistanceTracker;
import logisticspipes.utils.EnumFacingUtil;
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.SlidingWindowBitSet;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public abstract class LPTravelingItem {

	public static final Map<Integer, WeakReference<LPTravelingItemServer>> serverList = new HashMap<>();
	public static final Map<Integer, WeakReference<LPTravelingItemClient>> clientList = new HashMap<>();
	public static final List<Pair<Integer, Object>> forceKeep = new ArrayList<>();
	public static final SlidingWindowBitSet clientSideKnownIDs = new SlidingWindowBitSet(20); // 20

	private static int nextFreeId = 0;
	protected int id;
	protected float speed = 0.01F;

	public int lastTicked = 0;

	protected TileEntity container;
	protected float position = 0;
	protected float yaw = 0;
	public EnumFacing input = null;
	public EnumFacing output = null;
	public final EnumSet<EnumFacing> blacklist = EnumSet.noneOf(EnumFacing.class);

	public LPTravelingItem() {
		id = getNextId();
	}

	public LPTravelingItem(int id, float position, EnumFacing input, EnumFacing output, float yaw) {
		this.id = id;
		this.position = position;
		this.input = input;
		this.output = output;
		this.yaw = yaw;
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

	public void setYaw(float yaw) {
		this.yaw = yaw % 360;
	}

	public float getPosition() {
		return position;
	}

	public float getYaw() {
		return yaw;
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

	public abstract LPTravelingItem renderCopy();

	public static final class LPTravelingItemClient extends LPTravelingItem {

		@Setter
		private ItemIdentifierStack item;
		private int age;
		private float hoverStart = (float) (Math.random() * Math.PI * 2.0D);

		public LPTravelingItemClient(int id, float position, EnumFacing input, EnumFacing output, float yaw) {
			super(id, position, input, output, yaw);
		}

		public LPTravelingItemClient(int id, ItemIdentifierStack stack) {
			super(id);
			item = stack;
		}

		@Override
		public ItemIdentifierStack getItemIdentifierStack() {
			return item;
		}

		public void updateInformation(EnumFacing input, EnumFacing output, float speed, float position, float yaw) {
			this.input = input;
			this.output = output;
			this.speed = speed;
			this.position = position;
			this.yaw = yaw;
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

		@Override
		public LPTravelingItem renderCopy() {
			LPTravelingItemClient copy = new LPTravelingItemClient(id, position, input, output, yaw);
			copy.speed = speed;
			copy.hoverStart = hoverStart;
			copy.item = item.clone();
			copy.age = age;
			copy.container = container;
			return copy;
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
			if (data.hasKey("input")) {
				input = EnumFacingUtil.getOrientation(data.getInteger("input"));
			} else {
				input = null;
			}
			if (data.hasKey("output")) {
				output = EnumFacingUtil.getOrientation(data.getInteger("output"));
			} else {
				output = null;
			}
			info.readFromNBT(data);
		}

		@Override
		public void writeToNBT(NBTTagCompound data) {
			data.setFloat("position", getPosition());
			data.setFloat("speed", getSpeed());
			if (input != null) {
				data.setInteger("input", input.ordinal());
			}
			if (output != null) {
				data.setInteger("output", output.ordinal());
			}
			info.writeToNBT(data);
		}

		public EntityItem toEntityItem() {
			World world = container.getWorld();
			if (MainProxy.isServer(world)) {
				if (getItemIdentifierStack().getStackSize() <= 0) {
					return null;
				}

				if (getItemIdentifierStack().makeNormalStack().getItem() instanceof LogisticsFluidContainer) {
					itemWasLost();
					return null;
				}

				EnumFacing exitdirection = output;
				if (exitdirection == null) {
					exitdirection = input;
				}

				DoubleCoordinates position = new DoubleCoordinates(container).add(new DoubleCoordinates(0.5, 0.375, 0.5));

				switch (exitdirection) {
					case DOWN:
						CoordinateUtils.add(position, exitdirection, 0.5);
						break;
					case UP:
						CoordinateUtils.add(position, exitdirection, 0.75);
						break;
					case NORTH:
					case SOUTH:
					case WEST:
					case EAST:
						CoordinateUtils.add(position, exitdirection, 0.625);
						break;
					default:
						break;
				}

				DoubleCoordinates motion = new DoubleCoordinates(0, 0, 0);
				CoordinateUtils.add(motion, exitdirection, getSpeed() * 2.0);

				EntityItem entityitem = getItemIdentifierStack().makeEntityItem(world, position.getXCoord(), position.getYCoord(), position.getZCoord());

				//entityitem.lifespan = 1200;
				//entityitem.delayBeforeCanPickup = 10;

				//uniformly distributed in -0.005 .. 0.01 to increase bias toward smaller values
				float f3 = world.rand.nextFloat() * 0.015F - 0.005F;
				entityitem.motionX = (float) world.rand.nextGaussian() * f3 + motion.getXCoord();
				entityitem.motionY = (float) world.rand.nextGaussian() * f3 + motion.getYCoord();
				entityitem.motionZ = (float) world.rand.nextGaussian() * f3 + motion.getZCoord();
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
				if (MainProxy.isClient(container.getWorld())) {
					return;
				}
			}
			IRouter destinationRouter = SimpleServiceLocator.routerManager.getRouter(info.destinationint);
			if (destinationRouter != null) {
				if (destinationRouter.getPipe() != null) {
					destinationRouter.getPipe().notifyOfReroute(info);
					if (destinationRouter.getPipe() instanceof IRequireReliableTransport) {
						((IRequireReliableTransport) destinationRouter.getPipe()).itemLost(info.getItem().clone(), info.targetInfo);
					}
					if (destinationRouter.getPipe() instanceof IRequireReliableFluidTransport) {
						if (info.getItem().getItem().isFluidContainer()) {
							FluidIdentifierStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(info.getItem());
							((IRequireReliableFluidTransport) destinationRouter.getPipe()).liquidLost(liquid.getFluid(), liquid.getAmount());
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
		public void split(int itemsToTake, EnumFacing orientation) {
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
			IRouter router = SimpleServiceLocator.routerManager.getRouter(info.destinationint);
			if (router == null || info.destinationUUID != router.getId()) {
				info.destinationint = SimpleServiceLocator.routerManager.getIDforUUID(info.destinationUUID);
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

		@Override
		public LPTravelingItem renderCopy() {
			throw new UnsupportedOperationException();
		}
	}
}
