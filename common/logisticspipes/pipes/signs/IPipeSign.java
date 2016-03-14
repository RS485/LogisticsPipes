package logisticspipes.pipes.signs;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.renderer.LogisticsRenderPipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPipeSign {

	// Methods used when assigning a sign
	boolean isAllowedFor(CoreRoutedPipe pipe);

	void addSignTo(CoreRoutedPipe pipe, EnumFacing dir, EntityPlayer player);

	// For Final Pipe
	void readFromNBT(NBTTagCompound tag);

	void writeToNBT(NBTTagCompound tag);

	void init(CoreRoutedPipe pipe, EnumFacing dir);

	void activate(EntityPlayer player);

	ModernPacket getPacket();

	void updateServerSide();

	@SideOnly(Side.CLIENT)
	void render(CoreRoutedPipe pipe, LogisticsRenderPipe renderer);

	IPipeSignData getRenderData(CoreRoutedPipe pipe);
}
