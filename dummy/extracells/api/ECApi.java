package extracells.api;

import net.minecraftforge.fml.common.FMLLog;

public class ECApi {
	
	private static ExtraCellsApi instance = null;
	
	public static ExtraCellsApi instance(){
		if(instance == null){
			try{
				instance = (ExtraCellsApi) Class.forName("extracells.ExtraCellsApiInstance").getField("instance").get(null);
			}catch(Exception e){
			}
		}
		return instance;
	}

}
