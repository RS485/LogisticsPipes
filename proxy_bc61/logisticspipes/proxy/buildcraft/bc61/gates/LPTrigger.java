package logisticspipes.proxy.buildcraft.bc61.gates;


import logisticspipes.textures.Textures;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IGate;
import buildcraft.api.gates.IStatementParameter;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.StatementManager;
import buildcraft.api.gates.TriggerParameterItemStack;
import buildcraft.api.transport.IPipe;
import buildcraft.transport.Pipe;

public abstract class LPTrigger implements ITrigger {
	protected final String uniqueTag;

	public LPTrigger(String uniqueTag) {
		this.uniqueTag = uniqueTag;
		StatementManager.registerStatement(this);
	}

	@Override
	public String getUniqueTag() {
		return uniqueTag;
	}
	
    public abstract int getIconIndex();

	@Override
	public IStatementParameter createParameter(int arg0) {
		return new TriggerParameterItemStack();
	}

	@Override
	public int maxParameters() {
		return requiresParameter() ? 1 : 0;
	}

	@Override
	public int minParameters() {
		return requiresParameter() ? 1 : 0;
	}

	public boolean requiresParameter() {
		return false;
	}
	
	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public IIcon getIcon() {
		return Textures.LPactionIconProvider.getIcon(getIconIndex());
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		Textures.LPactionIconProvider.registerIcons(iconRegister);	
	}

	@Override
	public buildcraft.api.gates.ITrigger rotateLeft() {
		return this;
	}
	
	@Override
	public boolean isTriggerActive(IGate arg0, ITriggerParameter[] arg1) {
		if(arg0.getPipe() instanceof IPipe) {
			return isTriggerActive((Pipe<?>)arg0.getPipe(), arg1.length == 0 ? null : arg1[0]);
		}
		return false;
	}
	
	public boolean isTriggerActive(Pipe<?> pipe, ITriggerParameter parameter) {
		return false;
	}
}
