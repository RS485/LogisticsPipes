package net.machinemuse.api;

import net.minecraft.client.renderer.texture.IconRegister;

public interface IPowerModule {

	public abstract void registerIcon(IconRegister registry);

	public abstract String getCategory();

	public abstract String getDataName();
	
	public abstract String getLocalizedName();

	public abstract String getDescription();

}
