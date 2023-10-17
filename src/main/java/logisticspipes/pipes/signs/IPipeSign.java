package logisticspipes.pipes.signs;

import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.renderer.LogisticsRenderPipe;

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

	@SideOnly(Side.CLIENT)
	Framebuffer getMCFrameBufferForSign();

	@SideOnly(Side.CLIENT)
	boolean doesFrameBufferNeedUpdating(CoreRoutedPipe pipe, LogisticsRenderPipe renderer);
}
