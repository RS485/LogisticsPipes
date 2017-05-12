package logisticspipes.proxy.object3d.interfaces;

import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IRenderState {

	void reset();

	void setAlphaOverride(int i);

	void draw();

	void setBrightness(IBlockAccess world, BlockPos pos);

	@SideOnly(Side.CLIENT)
	void startDrawing(int mode, VertexFormat format);

}
