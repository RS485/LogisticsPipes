package logisticspipes.items;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;

public class ItemBlankModule extends LogisticsItem {

	public ItemBlankModule() {
		super();
	}

	@Override
	public void registerModels() {
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation("logisticspipes:itemmodule" + "/blank", "inventory"));
	}
}
