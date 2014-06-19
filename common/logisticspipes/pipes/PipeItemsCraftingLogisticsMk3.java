package logisticspipes.pipes;

import logisticspipes.gui.hud.HUDCraftingMK3;
import logisticspipes.interfaces.IBufferItems;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.modules.ModuleCrafterMK3;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.item.ItemIdentifierStack;

public class PipeItemsCraftingLogisticsMk3 extends PipeItemsCraftingLogisticsMk2 implements IBufferItems {
	
	private HUDCraftingMK3 HUD = new HUDCraftingMK3(this);
	
	public PipeItemsCraftingLogisticsMk3(int itemID) {
		super(itemID);
		craftingModule = new ModuleCrafterMK3(this);
	}
	
	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
	}

	@Override
	public void onAllowedRemoval() {
		super.onAllowedRemoval();
		craftingModule.onAllowedRemoval();
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_CRAFTERMK3_TEXTURE;
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
	}
	
	@Override
	public ModuleCrafter getLogisticsModule() {
		return this.craftingModule;
	}
	
	public ModuleCrafterMK3 getMk3Module() {
		return (ModuleCrafterMK3)craftingModule;
	}

	@Override
	public int addToBuffer(ItemIdentifierStack stack, IAdditionalTargetInformation info) {
		return getMk3Module().addToBuffer(stack, info);
	}
}
