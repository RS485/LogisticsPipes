package logisticspipes.main;

import logisticspipes.interfaces.IItemAdvancedExistance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class LogisticsEventListener {
	
	@ForgeSubscribe
	public void onEntitySpawn(EntityJoinWorldEvent event) {
		if(event != null && event.entity instanceof EntityItem && event.entity.worldObj != null && !event.entity.worldObj.isRemote) {
			ItemStack stack = ((EntityItem)event.entity).func_92014_d(); //Get ItemStack
			if(stack != null && stack.getItem() instanceof IItemAdvancedExistance && !((IItemAdvancedExistance)stack.getItem()).canExistInWorld()) {
				event.setCanceled(true);
			}
		}
	}
}
