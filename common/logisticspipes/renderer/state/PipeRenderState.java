package logisticspipes.renderer.state;

import java.io.IOException;

import logisticspipes.asm.ModDependentField;
import logisticspipes.interfaces.IClientState;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeRenderState implements IClientState {

	public final ConnectionMatrix pipeConnectionMatrix = new ConnectionMatrix();
	public final TextureMatrix textureMatrix = new TextureMatrix();
	@ModDependentField(modId="BuildCraft|Transport")
	public WireMatrix wireMatrix;
	public final ConnectionMatrix plugMatrix = new ConnectionMatrix();
	public final ConnectionMatrix robotStationMatrix = new ConnectionMatrix();
	public final FacadeMatrix facadeMatrix = new FacadeMatrix();
	
	public PipeRenderState() {
		if(SimpleServiceLocator.buildCraftProxy.isInstalled()) {
			wireMatrix = new WireMatrix();
		}
	}
	
	/*
	 * This is a placeholder for the pipe renderer to set to a value that the BlockGenericPipe->TileGenericPipe will then return the the WorldRenderer
	 */
	@SideOnly(Side.CLIENT)
	public IIcon currentTexture;
	@SideOnly(Side.CLIENT)
	public IIcon[] textureArray;

	private boolean dirty = true;
	private boolean isGateLit = false;
	private boolean isGatePulsing = false;
	private int gateIconIndex = 0;

	public void setIsGateLit(boolean value) {
		if (isGateLit != value) {
			isGateLit = value;
			dirty = true;
		}
	}

	public boolean isGateLit() {
		return isGateLit;
	}

	public void setIsGatePulsing(boolean value) {
		if (isGatePulsing != value) {
			isGatePulsing = value;
			dirty = true;
		}
	}

	public boolean isGatePulsing() {
		return isGatePulsing;
	}

	public void clean() {
		dirty = false;
		pipeConnectionMatrix.clean();
		textureMatrix.clean();
		facadeMatrix.clean();
		if(SimpleServiceLocator.buildCraftProxy.isInstalled()) {
			wireMatrix.clean();
		}
		plugMatrix.clean();
		robotStationMatrix.clean();
	}

	public boolean isDirty() {
		return dirty || pipeConnectionMatrix.isDirty()
				|| textureMatrix.isDirty() || (SimpleServiceLocator.buildCraftProxy.isInstalled() && wireMatrix.isDirty())
				|| facadeMatrix.isDirty() || plugMatrix.isDirty()
				|| robotStationMatrix.isDirty();
	}

	public boolean needsRenderUpdate() {
		return pipeConnectionMatrix.isDirty() || textureMatrix.isDirty()
				|| facadeMatrix.isDirty() || plugMatrix.isDirty()
				|| robotStationMatrix.isDirty();
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeBoolean(isGateLit);
		data.writeBoolean(isGatePulsing);
		data.writeInt(gateIconIndex);
		pipeConnectionMatrix.writeData(data);
		textureMatrix.writeData(data);
		if(SimpleServiceLocator.buildCraftProxy.isInstalled()) {
			wireMatrix.writeData(data);
		}
		facadeMatrix.writeData(data);
		plugMatrix.writeData(data);
		robotStationMatrix.writeData(data);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		isGateLit = data.readBoolean();
		isGatePulsing = data.readBoolean();
		gateIconIndex = data.readInt();
		pipeConnectionMatrix.readData(data);
		textureMatrix.readData(data);
		if(SimpleServiceLocator.buildCraftProxy.isInstalled()) {
			wireMatrix.readData(data);
		}
		facadeMatrix.readData(data);
		plugMatrix.readData(data);
		robotStationMatrix.readData(data);
	}
}