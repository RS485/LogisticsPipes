/*
package logisticspipes.proxy.buildcraft.gates;

import logisticspipes.textures.Textures;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.statements.StatementParameterItemStack;

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
	public TextureAtlasSprite getGuiSprite() {
		return Textures.LPactionIconProvider.getIcon(getIconIndex());
	}

	@Override
	public IStatement rotateLeft() {
		return this;
	}

	@Override
	public IStatement[] getPossible() {
		return new IStatement[]{this};
	}
}
*/
