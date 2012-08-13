package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;

public class ModuleApiaristSink implements ILogisticsModule {
	
	public enum FilterType { //TODO check names and spelling
		BeeAllele("Bee Allele",3,2),
		Drone("Drone",4,2),
		Princess("Princess",5,2),
		Queen("Queen",6,2),
		Purebred("Purebred",7,1),
		Nocturnal("Nocturnal",8,0),
		NotNocturnal("Not Nocturnal",9,0),
		Rain("Rain",10,0),
		NotRain("Not Rain",11,0),
		Daylight("Daylight",12,0),
		NoDayLight("No Daylight",13,0);
		
		FilterType(String text, int id, int secondSlot) {
			this.name = text;
			this.icon = id;
			this.secondSlots = secondSlot;
		}
		
		public String name;
		public int icon;
		public int secondSlots;
	}
	
	public static class SinkSetting {
		public FilterType filterType;
		
		public void readFromNBT(NBTTagCompound nbttagcompound) {
			nbttagcompound.setInteger("filterType", filterType.ordinal());
		}

		public void writeToNBT(NBTTagCompound nbttagcompound) {
			filterType = FilterType.values()[nbttagcompound.getInteger("filterType")];
		}
	}
	
	private IInventoryProvider _invProvider;
	private ISendRoutedItem _itemSender;
	public SinkSetting[] filter = new SinkSetting[6];
	
	public ModuleApiaristSink() {
		filter[0] = new SinkSetting();
		filter[0].filterType = FilterType.BeeAllele;
		filter[1] = new SinkSetting();
		filter[1].filterType = FilterType.Daylight;
		filter[2] = new SinkSetting();
		filter[2].filterType = FilterType.Nocturnal;
		filter[3] = new SinkSetting();
		filter[3].filterType = FilterType.Queen;
		filter[4] = new SinkSetting();
		filter[4].filterType = FilterType.Purebred;
		filter[5] = new SinkSetting();
		filter[5].filterType = FilterType.Drone;
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
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender) {
		_invProvider = invProvider;
		_itemSender = itemSender;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_Apiarist_Sink_ID;
	}

	@Override
	public SinkReply sinksItem(ItemStack item) {
		if(SimpleServiceLocator.forestryProxy.isBee(item)) {
			if(SimpleServiceLocator.forestryProxy.isAnalysedBee(item)) {
				//TODO add filter
				SinkReply reply = new SinkReply();
				reply.fixedPriority = SinkReply.FixedPriority.APIARIST_BeeSink;
				reply.isPassive = true;
				return reply;
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

}
