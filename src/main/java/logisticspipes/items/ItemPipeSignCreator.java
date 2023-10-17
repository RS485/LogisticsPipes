package logisticspipes.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.signs.CraftingPipeSign;
import logisticspipes.pipes.signs.IPipeSign;
import logisticspipes.pipes.signs.ItemAmountPipeSign;
import logisticspipes.proxy.MainProxy;

public class ItemPipeSignCreator extends LogisticsItem {

	public static final List<Class<? extends IPipeSign>> signTypes = new ArrayList<>();

	//private TextureAtlasSprite[] itemIcon = new TextureAtlasSprite[2];

	public ItemPipeSignCreator() {
		super();
		setMaxStackSize(1);
		setMaxDamage(250);
		setHasSubtypes(true);
	}

	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack) {
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTable(@Nonnull ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (MainProxy.isClient(world)) {
			return EnumActionResult.FAIL;
		}
		ItemStack itemStack = player.inventory.getCurrentItem();
		if (itemStack.isEmpty() || itemStack.getItemDamage() > this.getMaxDamage()) {
			return EnumActionResult.FAIL;
		}
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof LogisticsTileGenericPipe)) {
			return EnumActionResult.FAIL;
		}

		if (!itemStack.hasTagCompound()) {
			itemStack.setTagCompound(new NBTTagCompound());
		}
		itemStack.getTagCompound().setInteger("PipeClicked", 0);

		int mode = itemStack.getTagCompound().getInteger("CreatorMode");

		if (facing == null) {
			return EnumActionResult.FAIL;
		}

		if (!(((LogisticsTileGenericPipe) tile).pipe instanceof CoreRoutedPipe)) {
			return EnumActionResult.FAIL;
		}

		CoreRoutedPipe pipe = (CoreRoutedPipe) ((LogisticsTileGenericPipe) tile).pipe;
		if (pipe == null) {
			return EnumActionResult.FAIL;
		}
		if (!player.isSneaking()) {
			if (pipe.hasPipeSign(facing)) {
				pipe.activatePipeSign(facing, player);
				return EnumActionResult.SUCCESS;
			} else if (mode >= 0 && mode < ItemPipeSignCreator.signTypes.size()) {
				Class<? extends IPipeSign> signClass = ItemPipeSignCreator.signTypes.get(mode);
				try {
					IPipeSign sign = signClass.newInstance();
					if (sign.isAllowedFor(pipe)) {
						itemStack.damageItem(1, player);
						sign.addSignTo(pipe, facing, player);
						return EnumActionResult.SUCCESS;
					} else {
						return EnumActionResult.FAIL;
					}
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			} else {
				return EnumActionResult.FAIL;
			}
		} else {
			if (pipe.hasPipeSign(facing)) {
				pipe.removePipeSign(facing, player);
				itemStack.damageItem(-1, player);
			}
			return EnumActionResult.SUCCESS;
		}
	}

	@Override
	public int getMetadata(@Nonnull ItemStack stack) {
		if (stack.isEmpty() || !stack.hasTagCompound()) return 0;
		int mode = Objects.requireNonNull(stack.getTagCompound()).getInteger("CreatorMode");
		return Math.min(mode, ItemPipeSignCreator.signTypes.size() - 1);
	}

	@Override
	public int getModelCount() {
		return signTypes.size();
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, @Nonnull final EnumHand hand) {
		ItemStack stack = player.inventory.getCurrentItem();
		if (MainProxy.isClient(world)) {
			return ActionResult.newResult(EnumActionResult.PASS, stack);
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
		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}

	public static void registerPipeSignTypes() {
		// Never change this order. It defines the id each signType has.
		ItemPipeSignCreator.signTypes.add(CraftingPipeSign.class);
		ItemPipeSignCreator.signTypes.add(ItemAmountPipeSign.class);
	}
}
