package net.machinemuse.powersuits.powermodule;

import java.util.List;

import net.machinemuse.api.IModularItem;
import net.machinemuse.api.IPowerModule;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public abstract class PowerModuleBase implements IPowerModule {

    protected Icon icon;
    
	public PowerModuleBase(List<IModularItem> singletonList) {}

	
	public PowerModuleBase addInstallCost(ItemStack stack) {
        return this;
    }

    public abstract String getTextureFile();

	@Override
	public void registerIcon(IconRegister registry) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String getCategory() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getDataName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalizedName() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
}
