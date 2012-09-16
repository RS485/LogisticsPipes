package logisticspipes.items;

import logisticspipes.config.Textures;
import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EnumArmorMaterial;
import net.minecraft.src.ItemArmor;
import net.minecraft.src.ItemStack;
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

}
