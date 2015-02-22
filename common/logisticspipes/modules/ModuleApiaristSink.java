package logisticspipes.modules;

import java.util.List;

import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.ApiaristSinkModuleInHand;
import logisticspipes.network.guis.module.inpipe.ApiaristSinkModuleSlot;
import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleApiaristSink extends LogisticsGuiModule {

	public enum FilterType {
		Null("","anything",0),
		BeeAllele("gui.pipe.filter.BEE","bee",2),
		Drone("gui.pipe.filter.DRONE","drone",2),
		Princess("gui.pipe.filter.PRINCESS","princess",2),
		Queen("gui.pipe.filter.QUEEN","queen",2),
		Purebred("gui.pipe.filter.PURE_BREED","pure_breed",1),
		Nocturnal("gui.pipe.filter.NOCTURNAL","nocturnal",2),
		PureNocturnal("gui.pipe.filter.PURE_NOCTURNAL","pure_nocturnal",2),
		Flyer("gui.pipe.filter.FLYER","flyer",2),
		PureFlyer("gui.pipe.filter.PURE_FLYER","pure_flyer",2),
		Cave("gui.pipe.filter.CAVE","cave",2),
		PureCave("gui.pipe.filter.PURE_CAVE","pure_flyer",2);
		
		FilterType(String text, String id, int secondSlot) {
			this.path = text;
			this.icon = id;
			this.secondSlots = secondSlot;
		}
		
		public String path;
		public String icon;
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
			firstBee = SimpleServiceLocator.forestryProxy.getNextAlleleId(firstBee, module._world.getWorld());
		}

		public void firstBeeDown() {
			firstBee = SimpleServiceLocator.forestryProxy.getPrevAlleleId(firstBee, module._world.getWorld());
		}
		
		public void firstBeeReset() {
			firstBee = "";
		}
		
		public void secondBeeUp() {
			secondBee = SimpleServiceLocator.forestryProxy.getNextAlleleId(secondBee, module._world.getWorld());
		}
		
		public void secondBeeDown() {
			secondBee = SimpleServiceLocator.forestryProxy.getPrevAlleleId(secondBee, module._world.getWorld());
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
		
		public boolean isFiltered(ItemIdentifier itemID) {
			ItemStack item = itemID.makeNormalStack(1);
			switch(filterType) {
			case BeeAllele:
				return allAllele(item);
			case Drone:
				return allAllele(item) && SimpleServiceLocator.forestryProxy.isDrone(item);
			case Princess:
				return allAllele(item) && SimpleServiceLocator.forestryProxy.isPrincess(item);
			case Queen:
				return allAllele(item) && SimpleServiceLocator.forestryProxy.isQueen(item);
			case Purebred:
				return firstAllele(item) && SimpleServiceLocator.forestryProxy.isPurebred(item);
			case Nocturnal: 
				return allAllele(item) && SimpleServiceLocator.forestryProxy.isNocturnal(item);
			case PureNocturnal: 
				return allAllele(item) && SimpleServiceLocator.forestryProxy.isPureNocturnal(item);
			case Flyer: 
				return allAllele(item) && SimpleServiceLocator.forestryProxy.isFlyer(item);
			case PureFlyer: 
				return allAllele(item) && SimpleServiceLocator.forestryProxy.isPureFlyer(item);
			case Cave: 
				return allAllele(item) && SimpleServiceLocator.forestryProxy.isCave(item);
			case PureCave: 
				return allAllele(item) && SimpleServiceLocator.forestryProxy.isPureCave(item);
			default:
				break;
			}
			
			return false;
		}
	}
	
	public SinkSetting[] filter = new SinkSetting[6];
	
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
			filters.setTag(""+i, filterNBT);
		}
		nbttagcompound.setTag("filters", filters);
	}

	@Override
	protected ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		NBTTagCompound tag = new NBTTagCompound();
		this.writeToNBT(tag);
		return NewGuiHandler.getGui(ApiaristSinkModuleSlot.class).setNbt(tag);
	}

	@Override
	protected ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(ApiaristSinkModuleInHand.class);
	}
	
	public boolean isFiltered(ItemIdentifier item) {
		for (int i = 0; i < 6; i++) {
			Boolean accept = null;
			for (SinkSetting setting : filter) {
				if (setting.filterGroup - 1 == i) {
					if (accept == null) {
						accept = setting.isFiltered(item);
					} else {
						accept = accept && setting.isFiltered(item);
					}
				}
			}
			if (accept != null && accept) {
				return true;
			}
		}
		for (SinkSetting setting : filter) {
			if (setting.filterGroup == 0) {
				if (setting.isFiltered(item)) {
					return true;
				}
			}
	    }
		return false;
	}
	
	private SinkReply _sinkReply;
	
	@Override
	public void registerPosition(ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.APIARIST_BeeSink, 0, true, false, 2, 0, new ChassiTargetInformation(this.getPositionInt()));
	}
	
	@Override
	public SinkReply sinksItem(ItemIdentifier itemID, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		ItemStack item = itemID.makeNormalStack(1);
		if(SimpleServiceLocator.forestryProxy.isBee(item)) {
			if(SimpleServiceLocator.forestryProxy.isAnalysedBee(item)) {
				if(isFiltered(itemID)) {
					if(_service.canUseEnergy(2)) {
						return _sinkReply;
					}
				}
			}
		}
		return null;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {}
	
	@Override
	public boolean hasGenericInterests() {
		return true;
	}

	@Override
	public List<ItemIdentifier> getSpecificInterests() {
		return null;
	}

	@Override
	public boolean interestedInAttachedInventory() {		
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconTexture(IIconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleApiaristSink");
	}
}
