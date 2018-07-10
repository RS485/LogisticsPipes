package logisticspipes.items;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
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

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.RequestPipeDimension;
import logisticspipes.pipes.PipeItemsRemoteOrdererLogistics;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.string.StringUtils;

public class RemoteOrderer extends LogisticsItem {

	@SideOnly(Side.CLIENT)
	public void registerModels() {
		for (int i = 0; i < 17; i++) {
			ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation("logisticspipes:" + getUnlocalizedName().replace("item.", "") + "/" + i, "inventory"));
		}
	}

	@Override
	public boolean getShareTag() {
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);

		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("connectedPipe-x")) {
			tooltip.add("\u00a77Has Remote Pipe");
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
		ItemStack par1ItemStack = player.inventory.getCurrentItem();
		if (par1ItemStack.isEmpty()) {
			return null;
		}
		if (!par1ItemStack.hasTagCompound()) {
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
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(RequestPipeDimension.class).setInteger(MainProxy.getDimensionForWorld(pipe.getWorld())), player);
					player.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Orderer_ID, pipe.getWorld(), pipe.getX(), pipe.getY(), pipe.getZ());
				}
			}
		}
		return ActionResult.newResult(EnumActionResult.PASS, par1ItemStack);
	}

	public static void connectToPipe(ItemStack stack, PipeItemsRemoteOrdererLogistics pipe) {
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("connectedPipe-x", pipe.getX());
		stack.getTagCompound().setInteger("connectedPipe-y", pipe.getY());
		stack.getTagCompound().setInteger("connectedPipe-z", pipe.getZ());
		int dimension = 0;
		for (Integer dim : DimensionManager.getIDs()) {
			if (pipe.getWorld().equals(DimensionManager.getWorld(dim.intValue()))) {
				dimension = dim.intValue();
				break;
			}
		}
		stack.getTagCompound().setInteger("connectedPipe-world-dim", dimension);
	}

	public static PipeItemsRemoteOrdererLogistics getPipe(ItemStack stack) {
		if (stack == null) {
			return null;
		}
		if (!stack.hasTagCompound()) {
			return null;
		}
		if (!stack.getTagCompound().hasKey("connectedPipe-x") || !stack.getTagCompound().hasKey("connectedPipe-y") || !stack.getTagCompound().hasKey("connectedPipe-z")) {
			return null;
		}
		if (!stack.getTagCompound().hasKey("connectedPipe-world-dim")) {
			return null;
		}
		int dim = stack.getTagCompound().getInteger("connectedPipe-world-dim");
		World world = DimensionManager.getWorld(dim);
		if (world == null) {
			return null;
		}
		TileEntity tile = world.getTileEntity(new BlockPos(stack.getTagCompound().getInteger("connectedPipe-x"), stack.getTagCompound().getInteger("connectedPipe-y"), stack.getTagCompound().getInteger("connectedPipe-z")));
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
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			for (int meta = 0; meta < 17; meta++) {
				items.add(new ItemStack(this, 1, meta));
			}
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		return StringUtils.translate(getUnlocalizedName(itemstack));
	}
}
