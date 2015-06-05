package logisticspipes.proxy.buildcraft.robots.boards;

import java.util.List;

import logisticspipes.utils.string.StringUtils;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;

public class LogisticsRoutingBoardRobotNBT extends RedstoneBoardRobotNBT {

	public static LogisticsRoutingBoardRobotNBT instance = new LogisticsRoutingBoardRobotNBT();

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/robots/robot_router.png");

	private IIcon icon;

	@Override
	public RedstoneBoardRobot create(EntityRobotBase robot) {
		return new LogisticsRoutingBoardRobot(robot);
	}

	@Override
	public ResourceLocation getRobotTexture() {
		return LogisticsRoutingBoardRobotNBT.TEXTURE;
	}

	@Override
	public String getID() {
		return "logisticspipes:boardRobotRouter";
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		list.add(StringUtils.translate("robot.logisticsRouting"));
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:board_green");
	}

	@Override
	public IIcon getIcon(NBTTagCompound nbt) {
		return icon;
	}
}
