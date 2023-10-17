package logisticspipes.items;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.DimensionManager;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.RequestPipeDimension;
import logisticspipes.pipes.PipeItemsRemoteOrdererLogistics;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;

public class RemoteOrderer extends LogisticsItem {

	@Override
	public String getModelSubdir() {
		return "remote_orderer";
	}

	@Override
	public int getModelCount() {
		return 17;
	}

	@Override
	public boolean getShareTag() {
		return true;
	}

	@Override
	public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);

		if (stack.hasTagCompound() && Objects.requireNonNull(stack.getTagCompound()).hasKey("connectedPipe-x")) {
			tooltip.add("\u00a77Has Remote Pipe");
		}
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand handIn) {
		ItemStack par1ItemStack = player.inventory.getCurrentItem();
		if (par1ItemStack.isEmpty() || !par1ItemStack.hasTagCompound()) {
			return ActionResult.newResult(EnumActionResult.FAIL, par1ItemStack);
		}
		PipeItemsRemoteOrdererLogistics pipe = RemoteOrderer.getPipe(par1ItemStack);
		if (pipe != null) {
			if (MainProxy.isServer(player.world)) {
				int energyUse = 0;
				if (pipe.getWorld() != player.world) {
					energyUse += 2500;
				}
				energyUse += Math.sqrt(Math.pow(pipe.getX() - player.posX, 2) + Math.pow(pipe.getY() - player.posY, 2) + Math.pow(pipe.getZ() - player.posZ, 2));
				if (pipe.useEnergy(energyUse)) {
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(RequestPipeDimension.class).setInteger(pipe.getWorld().provider.getDimension()), player);
					player.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Orderer_ID, pipe.getWorld(), pipe.getX(), pipe.getY(), pipe.getZ());
				}
			}
		}
		return ActionResult.newResult(EnumActionResult.PASS, par1ItemStack);
	}

	public static void connectToPipe(@Nonnull ItemStack stack, PipeItemsRemoteOrdererLogistics pipe) {
		stack.setTagCompound(new NBTTagCompound());
		final NBTTagCompound tag = Objects.requireNonNull(stack.getTagCompound());
		tag.setInteger("connectedPipe-x", pipe.getX());
		tag.setInteger("connectedPipe-y", pipe.getY());
		tag.setInteger("connectedPipe-z", pipe.getZ());
		int dimension = 0;
		for (Integer dim : DimensionManager.getIDs()) {
			if (pipe.getWorld().equals(DimensionManager.getWorld(dim))) {
				dimension = dim;
				break;
			}
		}
		tag.setInteger("connectedPipe-world-dim", dimension);
	}

	public static PipeItemsRemoteOrdererLogistics getPipe(@Nonnull ItemStack stack) {
		if (stack.isEmpty() || !stack.hasTagCompound()) {
			return null;
		}
		final NBTTagCompound tag = Objects.requireNonNull(stack.getTagCompound());
		if (!tag.hasKey("connectedPipe-x") || !tag.hasKey("connectedPipe-y") || !tag.hasKey("connectedPipe-z")) {
			return null;
		}
		if (!tag.hasKey("connectedPipe-world-dim")) {
			return null;
		}
		int dim = tag.getInteger("connectedPipe-world-dim");
		World world = DimensionManager.getWorld(dim);
		if (world == null) {
			return null;
		}
		TileEntity tile = world.getTileEntity(new BlockPos(tag.getInteger("connectedPipe-x"), tag.getInteger("connectedPipe-y"), tag.getInteger("connectedPipe-z")));
		if (!(tile instanceof LogisticsTileGenericPipe)) {
			return null;
		}
		CoreUnroutedPipe pipe = ((LogisticsTileGenericPipe) tile).pipe;
		if (pipe instanceof PipeItemsRemoteOrdererLogistics) {
			return (PipeItemsRemoteOrdererLogistics) pipe;
		}
		return null;
	}

	@Override
	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			for (int meta = 0; meta < 17; meta++) {
				items.add(new ItemStack(this, 1, meta));
			}
		}
	}

}
