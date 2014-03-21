package net.machinemuse.powersuits.powermodule;

import java.util.List;

import net.machinemuse.api.IModularItem;
import net.machinemuse.api.IPowerModule;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public abstract class PowerModuleBase implements IPowerModule {

    protected IIcon icon;
    
	public PowerModuleBase(List<IModularItem> singletonList) {}

	
	public PowerModuleBase addInstallCost(ItemStack stack) {
        return this;
    }

    public abstract String getTextureFile();

	@Override
	public void registerIcon(IIconRegister registry) {
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
