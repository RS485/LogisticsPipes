package logisticspipes.network.packets.chassis;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.LogisticsPipes;
import logisticspipes.modules.LogisticsGuiModule;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.modules.ModuleExtractor;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.modules.AdvancedExtractorInclude;
import logisticspipes.network.packets.modules.ExtractorModuleMode;
import logisticspipes.network.packets.modules.ItemSinkDefault;
import logisticspipes.network.packets.modules.ProviderModuleInclude;
import logisticspipes.network.packets.modules.ProviderModuleMode;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.Player;

@Accessors(chain = true)
public class ChassisGUI extends CoordinatesPacket {
	
	@Getter
	@Setter
	private int buttonID;
	
	public ChassisGUI(int id) {
		super(id);
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(buttonID);
		super.writeData(data);
	}
	
	@Override
	public void readData(DataInputStream data) throws IOException {
		buttonID = data.readInt();
		super.readData(data);
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		
		if( !(pipe.pipe instanceof PipeLogisticsChassi)) {
			return;
		}
		
		final PipeLogisticsChassi cassiPipe = (PipeLogisticsChassi) pipe.pipe;
		
		if( !(cassiPipe.getLogisticsModule().getSubModule(getButtonID()) instanceof LogisticsGuiModule))
			return;
		
		player.openGui(LogisticsPipes.instance,((LogisticsGuiModule) cassiPipe.getLogisticsModule().getSubModule(getButtonID())).getGuiHandlerID() + (100 * (getButtonID() + 1)), player.worldObj, getPosX(), getPosY(), getPosZ());
		if(cassiPipe.getLogisticsModule().getSubModule(getButtonID()) instanceof ModuleItemSink) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ItemSinkDefault.class).setInteger2(getButtonID()).setInteger((((ModuleItemSink) cassiPipe.getLogisticsModule().getSubModule(getButtonID())).isDefaultRoute() ? 1 : 0)).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
		}
		if(cassiPipe.getLogisticsModule().getSubModule(getButtonID()) instanceof ModuleExtractor) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ExtractorModuleMode.class).setInteger2(getButtonID()).setInteger((((ModuleExtractor) cassiPipe.getLogisticsModule().getSubModule(getButtonID())).getSneakyDirection().ordinal())).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
		}
		if(cassiPipe.getLogisticsModule().getSubModule(getButtonID()) instanceof ModuleProvider) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ProviderModuleInclude.class).setInteger((((ModuleProvider) cassiPipe.getLogisticsModule().getSubModule(getButtonID())).isExcludeFilter() ? 1 : 0)).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ProviderModuleMode.class).setInteger((((ModuleProvider) cassiPipe.getLogisticsModule().getSubModule(getButtonID())).getExtractionMode().ordinal())).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
		}
		if(cassiPipe.getLogisticsModule().getSubModule(getButtonID()) instanceof ModuleAdvancedExtractor) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(AdvancedExtractorInclude.class).setInteger2(getButtonID()).setInteger((((ModuleAdvancedExtractor) cassiPipe.getLogisticsModule().getSubModule(getButtonID())).areItemsIncluded() ? 1 : 0)).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
		}
	}
	
	@Override
	public ModernPacket template() {
		return new ChassisGUI(getId());
	}
	
}

