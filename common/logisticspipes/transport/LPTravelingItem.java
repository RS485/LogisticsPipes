package logisticspipes.transport;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import lombok.Getter;
import lombok.Setter;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequireReliableFluidTransport;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.Router;
import logisticspipes.routing.RouterManager;
import logisticspipes.routing.order.IDistanceTracker;
import logisticspipes.utils.SlidingWindowBitSet;
import logisticspipes.utils.tuples.Tuple2;
import network.rs485.logisticspipes.init.Items;
import network.rs485.logisticspipes.item.FluidContainerItem;

public abstract class LPTravelingItem {

	public static final Map<Integer, WeakReference<LPTravelingItemServer>> serverList = new HashMap<>();
	public static final Map<Integer, WeakReference<LPTravelingItemClient>> clientList = new HashMap<>();
	public static final List<Tuple2<Integer, Object>> forceKeep = new ArrayList<>();
	public static final SlidingWindowBitSet clientSideKnownIDs = new SlidingWindowBitSet(20); // 20

	private static int nextFreeId = 0;
	protected int id;
	protected float speed = 0.01F;

	public int lastTicked = 0;

	protected BlockEntity container;
	protected float position = 0;
	protected float yaw = 0;
	public Direction input = null;
	public Direction output = null;
	public final EnumSet<Direction> blacklist = EnumSet.noneOf(Direction.class);

	private LPTravelingItem() {
		id = getNextId();
	}

	private LPTravelingItem(int id, float position, Direction input, Direction output, float yaw) {
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

	public void setContainer(BlockEntity container) {
		this.container = container;
	}

	public BlockEntity getContainer() {
		return container;
	}

	public int getId() {
		return id;
	}

	public abstract ItemStack getStack();

	public boolean isCorrupted() {
		return getStack() == null || getStack().getCount() <= 0;
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
		private ItemStack item;
		private int age;
		private float hoverStart = (float) (Math.random() * Math.PI * 2.0D);

		public LPTravelingItemClient(int id, float position, Direction input, Direction output, float yaw) {
			super(id, position, input, output, yaw);
		}

		public LPTravelingItemClient(int id, ItemStack stack) {
			super(id);
			item = stack;
		}

		@Override
		public ItemStack getStack() {
			return item;
		}

		public void updateInformation(Direction input, Direction output, float speed, float position, float yaw) {
			this.input = input;
			this.output = output;
			this.speed = speed;
			this.position = position;
			this.yaw = yaw;
		}

		@Override
		public int getAge() {
			return 0;// age;
		}

		@Override
		public void addAge() {
			age++;
		}

		@Override
		public float getHoverStart() {
			return 0;// hoverStart;
		}

		@Override
		public LPTravelingItem renderCopy() {
			LPTravelingItemClient copy = new LPTravelingItemClient(id, position, input, output, yaw);
			copy.speed = speed;
			copy.hoverStart = hoverStart;
			copy.item = item.copy();
			copy.age = age;
			copy.container = container;
			return copy;
		}
	}

	public static final class LPTravelingItemServer extends LPTravelingItem implements IRoutedItem {

		@Getter
		private ItemRoutingInformation info;

		public LPTravelingItemServer(ItemStack stack) {
			super();
			info = new ItemRoutingInformation();
			info.setItem(stack);
		}

		public LPTravelingItemServer(ItemRoutingInformation info) {
			super();
			this.info = info;
		}

		public LPTravelingItemServer(CompoundTag data) {
			super();
			info = new ItemRoutingInformation();
			readFromNBT(data);
		}

		@Nonnull
		@Override
		public ItemStack getStack() {
			return info.getItem();
		}

		public void setInformation(ItemRoutingInformation info) {
			this.info = info;
		}

		@Override
		public void readFromNBT(CompoundTag data) {
			setPosition(data.getFloat("position"));
			setSpeed(data.getFloat("speed"));
			if (data.containsKey("input")) {
				input = Direction.byId(data.getInt("input"));
			} else {
				input = null;
			}
			if (data.containsKey("output")) {
				output = Direction.byId(data.getInt("output"));
			} else {
				output = null;
			}
			info.readFromNBT(data);
		}

		@Override
		public void writeToNBT(CompoundTag data) {
			data.putFloat("position", getPosition());
			data.putFloat("speed", getSpeed());
			if (input != null) {
				data.putInt("input", input.ordinal());
			}
			if (output != null) {
				data.putInt("output", output.ordinal());
			}
			info.writeToNBT(data);
		}

		public ItemEntity toEntityItem() {
			World world = container.getWorld();
			if (world != null && world.isClient) {
				if (getStack().isEmpty()) {
					return null;
				}

				if (getStack().getItem() != Items.INSTANCE.getFluidContainer()) {
					itemWasLost();
					return null;
				}

				Direction exitDirection = output;
				if (exitDirection == null) {
					exitDirection = input;
				}

				Vec3d position = new Vec3d(container.getPos()).add(0.5, 0.375, 0.5);

				switch (exitDirection) {
					case DOWN:
						position = position.add(new Vec3d(exitDirection.getVector()).multiply(0.5));
						break;
					case UP:
						position = position.add(new Vec3d(exitDirection.getVector()).multiply(0.75));
						break;
					case NORTH:
					case SOUTH:
					case WEST:
					case EAST:
						position = position.add(new Vec3d(exitDirection.getVector()).multiply(0.625));
						break;
					default:
						break;
				}

				Vec3d motion = new Vec3d(exitDirection.getVector()).multiply(getSpeed() * 2.0);

				ItemEntity item = new ItemEntity(world, position.x, position.y, position.z, getStack());

				// item.lifespan = 1200;
				// item.delayBeforeCanPickup = 10;

				// uniformly distributed in -0.005 .. 0.01 to increase bias toward smaller values
				float f3 = world.getRandom().nextFloat() * 0.015F - 0.005F;
				item.setVelocity(
						(float) world.getRandom().nextGaussian() * f3 + motion.x,
						(float) world.getRandom().nextGaussian() * f3 + motion.y,
						(float) world.getRandom().nextGaussian() * f3 + motion.z
				);
				itemWasLost();

				return item;
			} else {
				return null;
			}
		}

		@Override
		public void clearDestination() {
			if (info.destinationId >= 0) {
				itemWasLost();
				info.jamlist.add(info.destinationId);
			}
			// keep buffercounter and jamlist
			info.destinationId = -1;
			info.destinationUUID = null;
			info._doNotBuffer = false;
			info.arrived = false;
			info._transportMode = TransportMode.Unknown;
			info.targetInfo = null;
		}

		public void itemWasLost() {
			if (container != null) {
				if (container.getWorld() == null || container.getWorld().isClient) {
					return;
				}
			}
			if (info.destinationId >= 0 && RouterManager.getInstance().isRouter(info.destinationId)) {
				Router destinationRouter = RouterManager.getInstance().getRouter(info.destinationId);
				if (destinationRouter.getPipe() != null) {
					destinationRouter.getPipe().notifyOfReroute(info);
					if (destinationRouter.getPipe() instanceof IRequireReliableTransport) {
						((IRequireReliableTransport) destinationRouter.getPipe()).itemLost(info.getItem(), info.targetInfo);
					}
					if (destinationRouter.getPipe() instanceof IRequireReliableFluidTransport) {
						if (info.getItem().getItem() == Items.INSTANCE.getFluidContainer()) {
							FluidVolume liquid = FluidContainerItem.Companion.getFluid(info.getItem());
							((IRequireReliableFluidTransport) destinationRouter.getPipe()).liquidLost(liquid.getFluidKey(), liquid.getAmount());
						}
					}
				}
			}
		}

		@Override
		public int getDestination() {
			return info.destinationId;
		}

		@Override
		public void setDestination(int destination) {
			info.destinationId = destination;
			Router router = RouterManager.getInstance().getRouter(destination);
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
		public void split(int itemsToTake, Direction orientation) {
			if (getStack().getItem() == Items.INSTANCE.getFluidContainer()) {
				throw new UnsupportedOperationException("Can't split up a FluidContainer");
			}

			ItemStack stackToKeep = getStack();
			ItemStack stackToSend = stackToKeep.split(stackToKeep.getCount() - itemsToTake);

			newId();

			LPTravelingItemServer newItem = new LPTravelingItemServer(stackToSend);
			newItem.setSpeed(getSpeed());
			newItem.setTransportMode(getTransportMode());

			newItem.setDestination(getDestination());
			newItem.clearDestination();

			if (container instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) container).pipe.transport != null) {
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
		public void addToJamList(Router router) {
			info.jamlist.add(router.getSimpleId());
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
			RouterManager rm = RouterManager.getInstance();
			Router router = rm.getRouter(info.destinationId);
			if (router == null || info.destinationUUID != router.getId()) {
				info.destinationId = rm.getIdForUuid(info.destinationUUID);
			}
		}

		public void refreshDestinationInformation() {
			Router destinationRouter = RouterManager.getInstance().getRouter(info.destinationId);
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
