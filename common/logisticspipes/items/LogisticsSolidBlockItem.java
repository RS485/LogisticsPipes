package logisticspipes.items;

import java.util.List;

import logisticspipes.blocks.LogisticsSolidBlock;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

public class LogisticsSolidBlockItem extends ItemBlock {

	public LogisticsSolidBlockItem(int par1) {
		super(par1);
	}


    @SideOnly(Side.CLIENT)
    public String getItemDisplayName(ItemStack stack)
    {
    switch(stack.getItemDamage()) {
		case LogisticsSolidBlock.SOLDERING_STATION:
			return "Soldering Station";
		}
		return super.getItemDisplayName(stack);
	}

	@Override
	public CreativeTabs getCreativeTab() {
		return CreativeTabs.tabDeco;
	}

	@Override
	public int getMetadata(int par1)
    {
        return par1;
    }
	
	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) {
		par3List.add(new ItemStack(this,1,0));
	}

}
