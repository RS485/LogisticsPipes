package logisticspipes.gates;

import logisticspipes.textures.Textures;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.TriggerParameter;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class LPTrigger implements ITrigger {

	protected int id;

	public LPTrigger(int id) {
		this.id = id;
		ActionManager.triggers[id] = this;
	}

	@Override
	public int getId() {
		return this.id;
	}

    @Override
    @SideOnly(Side.CLIENT)
    public IIconProvider getIconProvider() {
    	return Textures.LPactionIconProvider;
    }
    
    @Override
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
}
