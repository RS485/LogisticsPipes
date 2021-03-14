package logisticspipes.items;

import javax.annotation.Nonnull;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import net.minecraftforge.common.ISpecialArmor;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.IHUDArmor;
import logisticspipes.interfaces.ILogisticsItem;
import logisticspipes.network.GuiIDs;
import logisticspipes.proxy.MainProxy;

public class ItemHUDArmor extends ItemArmor implements ISpecialArmor, IHUDArmor, ILogisticsItem {

	public ItemHUDArmor() {
		super(ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.HEAD);
	}

	@Override
	public ArmorProperties getProperties(EntityLivingBase player, @Nonnull ItemStack armor, DamageSource source, double damage, int slot) {
		return new ArmorProperties(0, 0, 0);
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, @Nonnull ItemStack armor, int slot) {
		return 0;
	}

	@Override
	public void damageArmor(EntityLivingBase entity, @Nonnull ItemStack stack, DamageSource source, int damage, int slot) {
		// Does not get dammaged
	}

	@Override
	public boolean getShareTag() {
		return true;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand handIn) {
		ItemStack stack = player.getHeldItem(handIn);
		if (MainProxy.isClient(world)) {
			return new ActionResult<>(EnumActionResult.PASS, stack);
		}
		useItem(player, world);
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		useItem(player, world);
		if (MainProxy.isClient(world)) {
			return EnumActionResult.PASS;
		}
		return EnumActionResult.SUCCESS;
	}

	private void useItem(EntityPlayer player, World world) {
		player.openGui(LogisticsPipes.instance, GuiIDs.GUI_HUD_Settings, world, player.inventory.currentItem, -1, 0);
	}

	@Nonnull
	@Override
	public CreativeTabs[] getCreativeTabs() {
		// is visible in the LP creative tab and the ItemArmor creative tab
		return new CreativeTabs[] { getCreativeTab(), LogisticsPipes.CREATIVE_TAB_LP };
	}

	@Override
	public boolean isEnabled(@Nonnull ItemStack item) {
		return true;
	}

	@Override
	public String getArmorTexture(@Nonnull ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		return "logisticspipes:textures/armor/LogisticsHUD_1.png";
	}

	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack itemstack) {
		return I18n.translateToLocal(getUnlocalizedName(itemstack) + ".name").trim();
	}

}
