package extracells.api.definitions;

import appeng.api.util.AEItemDefinition;

public interface IPartDefinition {

	AEItemDefinition partFluidImportBus();
	
	AEItemDefinition partFluidExportBus();
	
	AEItemDefinition partFluidStorageBus();
	
	AEItemDefinition partFluidTerminal();
	
	AEItemDefinition partFluidLevelEmitter();
	
	AEItemDefinition partFluidAnnihilationPlane();
	
	AEItemDefinition partFluidFormationPlane();
	
	AEItemDefinition partBattery();
	
	AEItemDefinition partDrive();
	
	AEItemDefinition partInterface();
	
	AEItemDefinition partStorageMonitor();
	
	AEItemDefinition partConversionMonitor();
	
	AEItemDefinition partOreDictExportBus();
}
