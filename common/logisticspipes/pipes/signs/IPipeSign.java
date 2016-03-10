package logisticspipes.pipes.signs;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.renderer.LogisticsRenderPipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IPipeSign {

	// Methods used when assigning a sign
	boolean isAllowedFor(CoreRoutedPipe pipe);

	void addSignTo(CoreRoutedPipe pipe, ForgeDirection dir, EntityPlayer player);

	// For Final Pipe
	void readFromNBT(NBTTagCompound tag);

	void writeToNBT(NBTTagCompound tag);

	void init(CoreRoutedPipe pipe, ForgeDirection dir);

	void activate(EntityPlayer player);

	ModernPacket getPacket();

	void updateServerSide();

	@SideOnly(Side.CLIENT)
	void render(CoreRoutedPipe pipe, LogisticsRenderPipe renderer);

	IPipeSignData getRenderData(CoreRoutedPipe pipe);
}
