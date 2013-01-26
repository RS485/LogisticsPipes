package logisticspipes.modules;

import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsGuiModule;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.INBTPacketProvider;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModuleApiaristSink implements ILogisticsGuiModule, INBTPacketProvider {
	
	public int xCoord;
	public int yCoord;
	public int zCoord;
	
	public enum FilterType {
		Null("",0,0),
		BeeAllele("gui.pipe.filter.BEE",3,2),
		Drone("gui.pipe.filter.DRONE",4,2),
		Princess("gui.pipe.filter.PRINCESS",5,2),
		Queen("gui.pipe.filter.QUEEN",6,2),
		Purebred("gui.pipe.filter.PURE_BREED",7,1),
		Nocturnal("gui.pipe.filter.NOCTURNAL",8,2),
		PureNocturnal("gui.pipe.filter.PURE_NOCTURNAL",9,2),
		Flyer("gui.pipe.filter.FLYER",10,2),
		PureFlyer("gui.pipe.filter.PURE_FLYER",11,2),
		Cave("gui.pipe.filter.CAVE",12,2),
		PureCave("gui.pipe.filter.PURE_CAVE",13,2);
		
		FilterType(String text, int id, int secondSlot) {
			this.path = text;
			this.icon = id;
			this.secondSlots = secondSlot;
		}
		
		public String path;
		public int icon;
		public int secondSlots;
	}
	
	public static class SinkSetting {
		
		private final ModuleApiaristSink module;
		public FilterType filterType = FilterType.Null;
		public String firstBee = "";
		public String secondBee = "";
		public int filterGroup = 0;
		
		public SinkSetting(ModuleApiaristSink module) {
			this.module = module;
		}

		public void firstBeeUp() {
			firstBee = SimpleServiceLocator.forestryProxy.getNextAlleleId(firstBee, module.worldProvider.getWorld());
		}

		public void firstBeeDown() {
			firstBee = SimpleServiceLocator.forestryProxy.getPrevAlleleId(firstBee, module.worldProvider.getWorld());
		}
		
		public void firstBeeReset() {
			firstBee = "";
		}
		
		public void secondBeeUp() {
			secondBee = SimpleServiceLocator.forestryProxy.getNextAlleleId(secondBee, module.worldProvider.getWorld());
		}
		
		public void secondBeeDown() {
			secondBee = SimpleServiceLocator.forestryProxy.getPrevAlleleId(secondBee, module.worldProvider.getWorld());
		}
		
		public void secondBeeReset() {
			secondBee = "";
		}
		
		public void filterGroupUp() {
			if(filterGroup <= 5) {
				filterGroup++;
			} else {
				filterGroup = 0;
			}
		}
		
		public void filterGroupDown() {
			if(filterGroup >= 1) {
				filterGroup--;
			} else {
				filterGroup = 6;
			}
		}
		
		public void filterGroupReset() {
			filterGroup = 0;
		}
		
		public void FilterTypeUp() {
			if(filterType.ordinal() + 1 >= FilterType.values().length) {
				filterType = FilterType.values()[0];
			} else {
				filterType = FilterType.values()[filterType.ordinal() + 1];
			}
		}

		public void FilterTypeDown() {
			if(filterType.ordinal() - 1 < 0) {
				filterType = FilterType.values()[FilterType.values().length - 1];
			} else {
				filterType = FilterType.values()[filterType.ordinal() - 1];
			}
		}

		public void FilterTypeReset() {
			filterType = FilterType.Null;
		}
		
		public void readFromNBT(NBTTagCompound nbttagcompound) {
			if(nbttagcompound.hasKey("filterType")) {
				filterType = FilterType.values()[nbttagcompound.getInteger("filterType")];
			} else {
				filterType = FilterType.Null;
			}
			firstBee = nbttagcompound.getString("firstBeeString");
			secondBee = nbttagcompound.getString("secondBeeString");
			filterGroup = nbttagcompound.getInteger("filterGroup");
		}

		public void writeToNBT(NBTTagCompound nbttagcompound) {
			nbttagcompound.setInteger("filterType", filterType.ordinal());
			nbttagcompound.setString("firstBeeString", firstBee);
			nbttagcompound.setString("secondBeeString", secondBee);
			nbttagcompound.setInteger("filterGroup", filterGroup);
		}

		private boolean allAllele(ItemStack bee) {
			return firstAllele(bee) && secondAllele(bee);
		}

		private boolean firstAllele(ItemStack bee) {
			return SimpleServiceLocator.forestryProxy.getFirstAlleleId(bee).equals(firstBee) || firstBee.equals("");
		}

		private boolean secondAllele(ItemStack bee) {
			return SimpleServiceLocator.forestryProxy.getSecondAlleleId(bee).equals(secondBee) || secondBee.equals("");
		}
		
		public boolean isFiltered(ItemStack bee) {
			switch(filterType) {
			case BeeAllele:
				return allAllele(bee);
			case Drone:
				return allAllele(bee) && SimpleServiceLocator.forestryProxy.isDrone(bee);
			case Princess:
				return allAllele(bee) && SimpleServiceLocator.forestryProxy.isPrincess(bee);
			case Queen:
				return allAllele(bee) && SimpleServiceLocator.forestryProxy.isQueen(bee);
			case Purebred:
				return firstAllele(bee) && SimpleServiceLocator.forestryProxy.isPurebred(bee);
			case Nocturnal: 
				return allAllele(bee) && SimpleServiceLocator.forestryProxy.isNocturnal(bee);
			case PureNocturnal: 
				return allAllele(bee) && SimpleServiceLocator.forestryProxy.isPureNocturnal(bee);
			case Flyer: 
				return allAllele(bee) && SimpleServiceLocator.forestryProxy.isFlyer(bee);
			case PureFlyer: 
				return allAllele(bee) && SimpleServiceLocator.forestryProxy.isPureFlyer(bee);
			case Cave: 
				return allAllele(bee) && SimpleServiceLocator.forestryProxy.isCave(bee);
			case PureCave: 
				return allAllele(bee) && SimpleServiceLocator.forestryProxy.isPureCave(bee);
			default:
				break;
			}
			
			return false;
		}
	}
	
	public SinkSetting[] filter = new SinkSetting[6];
	public IWorldProvider worldProvider;
	private IChassiePowerProvider _power;
	
	public ModuleApiaristSink() {
		filter[0] = new SinkSetting(this);
		filter[1] = new SinkSetting(this);
		filter[2] = new SinkSetting(this);
		filter[3] = new SinkSetting(this);
		filter[4] = new SinkSetting(this);
		filter[5] = new SinkSetting(this);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		NBTTagCompound filters = nbttagcompound.getCompoundTag("filters");
		for(int i=0;i < filter.length; i++) {
			NBTTagCompound filterNBT = filters.getCompoundTag(""+i);
			filter[i].readFromNBT(filterNBT);
		}
		
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		NBTTagCompound filters = new NBTTagCompound();
		for(int i=0; i < filter.length; i++) {
			NBTTagCompound filterNBT = new NBTTagCompound();
			filter[i].writeToNBT(filterNBT);
			filters.setCompoundTag(""+i, filterNBT);
		}
		nbttagcompound.setCompoundTag("filters", filters);
	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerprovider) {
		this.worldProvider = world;
		_power = powerprovider;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_Apiarist_Sink_ID;
	}
	
	public boolean isFiltered(ItemStack itemBee) {
		Boolean[] groups = new Boolean[6];
		for(int i = 0;i < 6;i++) {
			groups[i] = null;
			for(SinkSetting setting:filter) {
				if(setting.filterGroup - 1 == i) {
					if(groups[i] == null) {
						groups[i] = setting.isFiltered(itemBee);
					} else {
						groups[i] &= setting.isFiltered(itemBee);
					}
				}
			}
		}
		for(int i = 0;i < 6;i++) {
			if(groups[i] != null) {
				if(groups[i]) {
					return true;
				}
			}
		}
		for(SinkSetting setting:filter) {
			if(setting.filterGroup == 0) {
				if(setting.isFiltered(itemBee)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private final SinkReply _sinkReply = new SinkReply(FixedPriority.APIARIST_BeeSink, 0, true, false, 2, 0);
	@Override
	public SinkReply sinksItem(ItemStack item, int bestPriority, int bestCustomPriority) {
		if (bestPriority >= FixedPriority.APIARIST_BeeSink.ordinal()) return null;
		if(SimpleServiceLocator.forestryProxy.isBee(item)) {
			if(SimpleServiceLocator.forestryProxy.isAnalysedBee(item)) {
				if(isFiltered(item)) {
					if(_power.canUseEnergy(2)) {
						return _sinkReply;
					}
				}
			}
		}
		return null;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {}

	@Override
	public void readFromPacketNBT(NBTTagCompound tag) {
		readFromNBT(tag);
	}

	@Override
	public void writeToPacketNBT(NBTTagCompound tag) {
		writeToNBT(tag);
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}
}
