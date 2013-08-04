package logisticspipes.gates;

import logisticspipes.textures.Textures;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.TriggerParameter;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class LPTrigger implements ITrigger {
	protected final int legacyId;
	protected final String uniqueTag;

	public LPTrigger(int legacyId, String uniqueTag) {
		this.legacyId = legacyId;
		this.uniqueTag = uniqueTag;
		ActionManager.registerTrigger(this);
	}


	@Override
	public int getLegacyId() {
		return this.legacyId;
	}

	@Override
	public String getUniqueTag() {
		return uniqueTag;
	}
	
    public abstract int getIconIndex();

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		return false;
	}

	@Override
	public final ITriggerParameter createParameter() {
		return new TriggerParameter();
	}



	@Override
	public Icon getIcon() {
		return Textures.LPactionIconProvider.getIcon(getIconIndex());
	}


	@Override
	public void registerIcons(IconRegister iconRegister) {
		Textures.LPactionIconProvider.registerIcons(iconRegister);	
	}
}
