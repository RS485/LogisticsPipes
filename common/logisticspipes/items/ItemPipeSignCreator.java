package logisticspipes.items;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.signs.CraftingPipeSign;
import logisticspipes.pipes.signs.IPipeSign;
import logisticspipes.pipes.signs.ItemAmountPipeSign;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPipeSignCreator extends LogisticsItem {

	public static final List<Class<? extends IPipeSign>> signTypes = new ArrayList<Class<? extends IPipeSign>>();

	private IIcon[] itemIcon = new IIcon[2];

	public ItemPipeSignCreator() {
		super();
		setMaxStackSize(1);
		setMaxDamage(250);
	}

	@Override
	public boolean onItemUseFirst(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int sideinput, float hitX, float hitY, float hitZ) {
		if (MainProxy.isClient(world)) {
			return false;
		}
		if (itemStack.getItemDamage() > this.getMaxDamage() || itemStack.stackSize == 0) {
			return false;
		}
		TileEntity tile = world.getTileEntity(x, y, z);
		if (!(tile instanceof LogisticsTileGenericPipe)) {
			return false;
		}

		if (!itemStack.hasTagCompound()) {
			itemStack.setTagCompound(new NBTTagCompound());
		}
		itemStack.getTagCompound().setInteger("PipeClicked", 0);

		int mode = itemStack.getTagCompound().getInteger("CreatorMode");

		ForgeDirection dir = ForgeDirection.getOrientation(sideinput);
		if (dir == ForgeDirection.UNKNOWN) {
			return false;
		}

		if(!(((LogisticsTileGenericPipe) tile).pipe instanceof CoreRoutedPipe)) {
			return false;
		}

		CoreRoutedPipe pipe = (CoreRoutedPipe) ((LogisticsTileGenericPipe) tile).pipe;
		if (pipe == null) {
			return false;
		}
		if (!player.isSneaking()) {
			if (pipe.hasPipeSign(dir)) {
				pipe.activatePipeSign(dir, player);
				return true;
			} else if (mode >= 0 && mode < ItemPipeSignCreator.signTypes.size()) {
				Class<? extends IPipeSign> signClass = ItemPipeSignCreator.signTypes.get(mode);
				try {
					IPipeSign sign = signClass.newInstance();
					if (sign.isAllowedFor(pipe)) {
						itemStack.damageItem(1, player);
						sign.addSignTo(pipe, dir, player);
						return true;
					} else {
						return false;
					}
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			} else {
				return false;
			}
		} else {
			if (pipe.hasPipeSign(dir)) {
				pipe.removePipeSign(dir, player);
				itemStack.damageItem(-1, player);
			}
			return true;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		super.registerIcons(par1IconRegister); // Fallback
		for (int i = 0; i < ItemPipeSignCreator.signTypes.size(); i++) {
			itemIcon[i] = par1IconRegister.registerIcon("logisticspipes:" + getUnlocalizedName().replace("item.", "") + "." + i);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconIndex(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		int mode = stack.getTagCompound().getInteger("CreatorMode");
		if (mode < ItemPipeSignCreator.signTypes.size()) {
			return itemIcon[mode];
		} else {
			return super.getIconIndex(stack); // Fallback
		}
	}

	@Override
	public IIcon getIcon(ItemStack stack, int pass) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		int mode = stack.getTagCompound().getInteger("CreatorMode");
		if (mode < ItemPipeSignCreator.signTypes.size()) {
			return itemIcon[mode];
		} else {
			return super.getIcon(stack, pass); // Fallback
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		if (!itemstack.hasTagCompound()) {
			itemstack.setTagCompound(new NBTTagCompound());
		}
		int mode = itemstack.getTagCompound().getInteger("CreatorMode");
		return StringUtils.translate(getUnlocalizedName(itemstack) + "." + mode);
	}

	@Override
	public CreativeTabs getCreativeTab() {
		return CreativeTabs.tabTools;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (MainProxy.isClient(world)) {
			return stack;
		}
		if (player.isSneaking()) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			if (!stack.getTagCompound().hasKey("PipeClicked")) {
				int mode = stack.getTagCompound().getInteger("CreatorMode");
				mode++;
				if (mode >= ItemPipeSignCreator.signTypes.size()) {
					mode = 0;
				}
				stack.getTagCompound().setInteger("CreatorMode", mode);
			}
		}
		if (stack.hasTagCompound()) {
			stack.getTagCompound().removeTag("PipeClicked");
		}
		return stack;
	}

	public static void registerPipeSignTypes() {
		// Never change this order. It defines the id each signType has.
		ItemPipeSignCreator.signTypes.add(CraftingPipeSign.class);
		ItemPipeSignCreator.signTypes.add(ItemAmountPipeSign.class);
	}
}
