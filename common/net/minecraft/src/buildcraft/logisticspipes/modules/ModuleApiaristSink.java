package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.krapht.network.INBTPacketProvider;
import net.minecraft.src.buildcraft.krapht.network.PacketPipeInteger;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;

public class ModuleApiaristSink implements ILogisticsModule, INBTPacketProvider {
	
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
		public int firstBee = -1;
		public int secondBee = -1;
		public int filterGroup = 0;
		
		public SinkSetting(ModuleApiaristSink module) {
			this.module = module;
		}

		public void firstBeeUp() {
			boolean changed = false;
			for (int id = firstBee + 1; id <= 256; id++) {
				if (!SimpleServiceLocator.forestryProxy.isVaildAlleleId(id)	|| !SimpleServiceLocator.forestryProxy.isKnownAlleleId(id, module.worldProvider.getWorld())) {
					continue;
				}
				firstBee = id;
				changed = true;
				break;
			}
			if(!changed) {
				firstBee = -1;
			}
		}

		public void firstBeeDown() {
			boolean changed = false;
			for (int id = firstBee - 1; id >= 0; id--) {
				if (!SimpleServiceLocator.forestryProxy.isVaildAlleleId(id)	|| !SimpleServiceLocator.forestryProxy.isKnownAlleleId(id, module.worldProvider.getWorld())) {
					continue;
				}
				firstBee = id;
				changed = true;
				break;
			}
			if(!changed) {
				firstBee = -1;
			}
		}
		
		public void firstBeeReset() {
			firstBee = -1;
		}
		
		public void secondBeeUp() {
			boolean changed = false;
			for (int id = secondBee + 1; id <= 256; id++) {
				if (!SimpleServiceLocator.forestryProxy.isVaildAlleleId(id)	|| !SimpleServiceLocator.forestryProxy.isKnownAlleleId(id, module.worldProvider.getWorld())) {
					continue;
				}
				secondBee = id;
				changed = true;
				break;
			}
			if(!changed) {
				secondBee = -1;
			}
		}
		
		public void secondBeeDown() {
			boolean changed = false;
			for (int id = secondBee - 1; id >= 0; id--) {
				if (!SimpleServiceLocator.forestryProxy.isVaildAlleleId(id)	|| !SimpleServiceLocator.forestryProxy.isKnownAlleleId(id, module.worldProvider.getWorld())) {
					continue;
				}
				secondBee = id;
				changed = true;
				break;
			}
			if(!changed) {
				secondBee = -1;
			}
		}
		
		public void secondBeeReset() {
			secondBee = -1;
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
			firstBee = nbttagcompound.getInteger("firstBee");
			secondBee = nbttagcompound.getInteger("secondBee");
		}

		public void writeToNBT(NBTTagCompound nbttagcompound) {
			nbttagcompound.setInteger("filterType", filterType.ordinal());
			nbttagcompound.setInteger("firstBee", firstBee);
			nbttagcompound.setInteger("secondBee", secondBee);
		}

		private boolean allAllele(ItemStack bee) {
			return firstAllele(bee) && secondAllele(bee);
		}

		private boolean firstAllele(ItemStack bee) {
			return SimpleServiceLocator.forestryProxy.getFirstAlleleId(bee) == firstBee || firstBee == -1;
		}

		private boolean secondAllele(ItemStack bee) {
			return SimpleServiceLocator.forestryProxy.getSecondAlleleId(bee) == secondBee || secondBee == -1;
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
			}
			
			return false;
		}
	}
	
	public SinkSetting[] filter = new SinkSetting[6];
	public IWorldProvider worldProvider;
	private int slotNumber;
	
	public ModuleApiaristSink() {
		filter[0] = new SinkSetting(this);
		filter[1] = new SinkSetting(this);
		filter[2] = new SinkSetting(this);
		filter[3] = new SinkSetting(this);
		filter[4] = new SinkSetting(this);
		filter[5] = new SinkSetting(this);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		NBTTagCompound filters = nbttagcompound.getCompoundTag("filters");
		for(int i=0;i < filter.length; i++) {
			NBTTagCompound filterNBT = filters.getCompoundTag(""+i);
			filter[i].readFromNBT(filterNBT);
		}
		
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
		NBTTagCompound filters = new NBTTagCompound();
		for(int i=0; i < filter.length; i++) {
			NBTTagCompound filterNBT = new NBTTagCompound();
			filter[i].writeToNBT(filterNBT);
			filters.setCompoundTag(""+i, filterNBT);
		}
		nbttagcompound.setCompoundTag("filters", filters);
	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world) {
		this.worldProvider = world;
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
	
	@Override
	public SinkReply sinksItem(ItemStack item) {
		if(SimpleServiceLocator.forestryProxy.isBee(item)) {
			if(SimpleServiceLocator.forestryProxy.isAnalysedBee(item)) {
				if(isFiltered(item)) {
					SinkReply reply = new SinkReply();
					reply.fixedPriority = SinkReply.FixedPriority.APIARIST_BeeSink;
					reply.isPassive = true;
					return reply;
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
		readFromNBT(tag,"");
	}

	@Override
	public void writeToPacketNBT(NBTTagCompound tag) {
		writeToNBT(tag,"");
	}
}
