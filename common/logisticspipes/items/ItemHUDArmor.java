package logisticspipes.items;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Textures;
import logisticspipes.network.GuiIDs;
import logisticspipes.proxy.MainProxy;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EnumArmorMaterial;
import net.minecraft.src.ItemArmor;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraftforge.common.IArmorTextureProvider;
import net.minecraftforge.common.ISpecialArmor;

public class ItemHUDArmor extends ItemArmor implements IArmorTextureProvider, ISpecialArmor {

	public ItemHUDArmor(int par1, int renderIndex) {
		super(par1, EnumArmorMaterial.CHAIN, renderIndex, 0);
	}

	@Override
	public ArmorProperties getProperties(EntityLiving player, ItemStack armor, DamageSource source, double damage, int slot) {
		return new ArmorProperties(0, 0, 0);
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
		return 0;
	}

	@Override
	public void damageArmor(EntityLiving entity, ItemStack stack, DamageSource source, int damage, int slot) {
		//Does not get dammaged
	}

	@Override
	public String getArmorTextureFile(ItemStack itemstack) {
		return Textures.LOGISTICSPIPE_HUD_TEXTURE_FILE;
	}

	@Override
	public String getTextureFile() {
		return Textures.LOGISTICSITEMS_TEXTURE_FILE;
	}

	@Override
	public boolean getShareTag() {
		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(MainProxy.isClient()) return stack;
		useItem(player, world);
		return stack.copy();
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		useItem(player, world);
		if(MainProxy.isClient()) return false;
		return true;
	}
	
	private void useItem(EntityPlayer player, World world) {
		player.openGui(LogisticsPipes.instance, GuiIDs.GUI_HUD_Settings, world, player.inventory.currentItem, -1, 0);
	}

	@Override
	public CreativeTabs[] getCreativeTabs() {
        return new CreativeTabs[]{ getCreativeTab() , LogisticsPipes.LPCreativeTab };
	}
}
