package logisticspipes.proxy.buildcraft.gates;

import logisticspipes.textures.Textures;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;

public abstract class LPTrigger implements IStatement {

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
		return new StatementParameterItemStack();
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
	public IStatement rotateLeft() {
		return this;
	}

	public boolean isTriggerActive(IStatementContainer arg0, IStatementParameter[] arg1) {
		if (arg0.getTile() instanceof TileGenericPipe) {
			return isTriggerActive(((TileGenericPipe) arg0.getTile()).pipe, arg1.length == 0 ? null : arg1[0]);
		}
		return false;
	}

	public boolean isTriggerActive(Pipe<?> pipe, IStatementParameter parameter) {
		return false;
	}
}
