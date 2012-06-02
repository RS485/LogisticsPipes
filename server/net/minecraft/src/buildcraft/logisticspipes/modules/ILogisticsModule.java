package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.buildcraft.krapht.ISaveState;
import net.minecraft.src.buildcraft.logisticspipes.GuiID;
import net.minecraft.src.krapht.ItemIdentifier;

public interface ILogisticsModule extends ISaveState{
	
	public GuiID getGuiID();
	public SinkReply sinksItem(ItemIdentifier item);
	public ILogisticsModule getSubModule(int slot);
	public void tick();
}
