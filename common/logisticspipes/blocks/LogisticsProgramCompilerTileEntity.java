package logisticspipes.blocks;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import lombok.Getter;

import logisticspipes.config.Configs;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.IGuiTileEntity;
import logisticspipes.items.ItemLogisticsProgrammer;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.guis.block.ProgramCompilerGui;
import logisticspipes.network.packets.block.CompilerStatusPacket;
import logisticspipes.pipes.PipeItemsBasicLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.item.SimpleStackInventory;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class LogisticsProgramCompilerTileEntity extends LogisticsSolidTileEntity implements IGuiTileEntity, IGuiOpenControler {

	public static class ProgrammCategories {

		public static final ResourceLocation BASIC = new ResourceLocation("logisticspipes", "compilercategory.basic");
		public static final ResourceLocation TIER_2 = new ResourceLocation("logisticspipes", "compilercategory.tier_2");
		public static final ResourceLocation FLUID = new ResourceLocation("logisticspipes", "compilercategory.fluid");
		public static final ResourceLocation TIER_3 = new ResourceLocation("logisticspipes", "compilercategory.tier_3");
		public static final ResourceLocation CHASSIS = new ResourceLocation("logisticspipes", "compilercategory.chassis");
		public static final ResourceLocation CHASSIS_2 = new ResourceLocation("logisticspipes", "compilercategory.chassis_2");
		public static final ResourceLocation CHASSIS_3 = new ResourceLocation("logisticspipes", "compilercategory.chassis_3");
		public static final ResourceLocation MODDED = new ResourceLocation("logisticspipes", "compilercategory.modded");

		static {
			//Force the order of keys
			programByCategory.put(BASIC, new HashSet<>());
			programByCategory.put(TIER_2, new HashSet<>());
			programByCategory.put(FLUID, new HashSet<>());
			programByCategory.put(TIER_3, new HashSet<>());
			programByCategory.put(CHASSIS, new HashSet<>());
			programByCategory.put(CHASSIS_2, new HashSet<>());
			programByCategory.put(CHASSIS_3, new HashSet<>());
			programByCategory.put(MODDED, new HashSet<>());
		}
	}

	public static final Map<ResourceLocation, Set<ResourceLocation>> programByCategory = new LinkedHashMap<>();
	private final PlayerCollectionList playerList = new PlayerCollectionList();
	private String taskType = "";
	@Getter
	private ResourceLocation currentTask = null;
	@Getter
	private double taskProgress = 0;
	@Getter
	private boolean wasAbleToConsumePower = false;

	@Getter
	private SimpleStackInventory inventory = new SimpleStackInventory(2, "programcompilerinv", 64);

	@Override
	public CoordinatesGuiProvider getGuiProvider() {
		return NewGuiHandler.getGui(ProgramCompilerGui.class);
	}

	public NBTTagList getNBTTagListForKey(String key) {
		NBTTagCompound nbt = this.getInventory().getStackInSlot(0).getTagCompound();
		if (nbt == null) {
			this.getInventory().getStackInSlot(0).setTagCompound(new NBTTagCompound());
			nbt = this.getInventory().getStackInSlot(0).getTagCompound();
		}

		if (!nbt.hasKey(key)) {
			NBTTagList list = new NBTTagList();
			nbt.setTag(key, list);
		}
		return nbt.getTagList(key, 8 /* String */);
	}

	public void triggerNewTask(ResourceLocation category, String taskType) {
		if (currentTask != null) return;
		this.taskType = taskType;
		currentTask = category;
		taskProgress = 0;
		wasAbleToConsumePower = true;
		updateClient();
	}

	@Override
	public void guiOpenedByPlayer(EntityPlayer player) {
		playerList.add(player);
		MainProxy.sendPacketToPlayer(getClientUpdatePacket(), player);
	}

	private CoordinatesPacket getClientUpdatePacket() {
		return PacketHandler.getPacket(CompilerStatusPacket.class)
				.setCategory(currentTask)
				.setProgress(taskProgress)
				.setWasAbleToConsumePower(wasAbleToConsumePower)
				.setDisk(getInventory().getStackInSlot(0))
				.setProgrammer(getInventory().getStackInSlot(1))
				.setTilePos(this);
	}

	@Override
	public void guiClosedByPlayer(EntityPlayer player) {
		playerList.remove(player);
	}

	@Override
	public void update() {
		super.update();
		if (MainProxy.isServer(world)) {
			if (currentTask != null) {
				wasAbleToConsumePower = false;
				for (EnumFacing dir : EnumFacing.VALUES) {
					if (dir == EnumFacing.UP) continue;
					DoubleCoordinates pos = CoordinateUtils.add(new DoubleCoordinates(this), dir);
					TileEntity tile = pos.getTileEntity(getWorld());
					if (!(tile instanceof LogisticsTileGenericPipe)) {
						continue;
					}
					LogisticsTileGenericPipe tPipe = (LogisticsTileGenericPipe) tile;
					if (!(tPipe.pipe.getClass() == PipeItemsBasicLogistics.class)) {
						continue;
					}
					CoreRoutedPipe pipe = (CoreRoutedPipe) tPipe.pipe;
					if (pipe.useEnergy(10)) {
						switch (taskType) {
							case "category":
								taskProgress += 0.0005 * Configs.COMPILER_SPEED;
								break;
							case "program":
								taskProgress += 0.0025 * Configs.COMPILER_SPEED;
								break;
							case "flash":
								taskProgress += 0.01 * Configs.COMPILER_SPEED;
								break;
							default:
								taskProgress = 1;
								break;
						}
						wasAbleToConsumePower = true;
					}
				}
				if (taskProgress >= 1) {
					switch (taskType) {
						case "category": {
							NBTTagList list = getNBTTagListForKey("compilerCategories");
							list.appendTag(new NBTTagString(currentTask.toString()));
							break;
						}
						case "program": {
							NBTTagList list = getNBTTagListForKey("compilerPrograms");
							list.appendTag(new NBTTagString(currentTask.toString()));
							break;
						}
						case "flash":
							if (!getInventory().getStackInSlot(1).isEmpty()) {
								ItemStack programmer = getInventory().getStackInSlot(1);
								if (!programmer.hasTagCompound()) {
									programmer.setTagCompound(new NBTTagCompound());
								}
								assert programmer.getTagCompound() != null;
								programmer.getTagCompound()
										.setString(ItemLogisticsProgrammer.RECIPE_TARGET, currentTask.toString());
							}
							break;
						default:
							throw new UnsupportedOperationException(taskType);
					}

					taskType = "";
					currentTask = null;
					taskProgress = 0;
					wasAbleToConsumePower = false;
				}
				updateClient();
			}
		}
	}

	public void updateClient() {
		MainProxy.sendToPlayerList(getClientUpdatePacket(), playerList);
	}

	@Override
	public void onBlockBreak() {
		inventory.dropContents(world, getPos());
	}

	public void setStateOnClient(CompilerStatusPacket compilerStatusPacket) {
		getInventory().setInventorySlotContents(0, compilerStatusPacket.getDisk());
		getInventory().setInventorySlotContents(1, compilerStatusPacket.getProgrammer());
		currentTask = compilerStatusPacket.getCategory();
		taskProgress = compilerStatusPacket.getProgress();
		wasAbleToConsumePower = compilerStatusPacket.isWasAbleToConsumePower();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		inventory.readFromNBT(nbt, "programcompilerinv");
		super.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		inventory.writeToNBT(nbt, "programcompilerinv");
		return super.writeToNBT(nbt);
	}
}
