package net.machinemuse.api;

import net.minecraft.client.renderer.texture.IIconRegister;

public interface IPowerModule {

	public abstract void registerIcon(IIconRegister registry);

	public abstract String getCategory();

	public abstract String getDataName();
	
	public abstract String getLocalizedName();

	public abstract String getDescription();

}
