package logisticspipes.pipes.upgrades;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.ModuleModBasedItemSink;
import logisticspipes.modules.ModuleOreDictItemSink;
import logisticspipes.modules.ModulePolymorphicItemSink;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractguis.UpgradeCoordinatesGuiProvider;
import logisticspipes.network.guis.upgrade.DisconnectionUpgradeConfigGuiProvider;
import logisticspipes.network.guis.upgrade.SneakyUpgradeConfigGuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ConnectionUpgradeConfig implements IConfigPipeUpgrade {

	@AllArgsConstructor
	public enum Sides {
		UP(ForgeDirection.UP, "LPDIS-UP"),
		DOWN(ForgeDirection.DOWN, "LPDIS-DOWN"),
		NORTH(ForgeDirection.NORTH, "LPDIS-NORTH"),
		SOUTH(ForgeDirection.SOUTH, "LPDIS-SOUTH"),
		EAST(ForgeDirection.EAST, "LPDIS-EAST"),
		WEST(ForgeDirection.WEST, "LPDIS-WEST");
		@Getter private ForgeDirection dir;
		@Getter private String lpName;
		public static String getNameForDirection(ForgeDirection fd) {
			Optional<Sides> opt = Arrays.stream(values()).filter(side -> side.getDir() == fd).findFirst();
			if(opt.isPresent()) {
				return opt.get().getLpName();
			}
			return "LPDIS-UNKNWON";
		}
	}

	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public boolean isAllowedForPipe(CoreRoutedPipe pipe) {
		return true;
	}

	@Override
	public boolean isAllowedForModule(LogisticsModule pipe) {
		return false;
	}

	@Override
	public String[] getAllowedPipes() {
		return new String[] { "all" };
	}

	@Override
	public String[] getAllowedModules() {
		return new String[] {};
	}

	@Override
	public UpgradeCoordinatesGuiProvider getGUI() {
		return NewGuiHandler.getGui(DisconnectionUpgradeConfigGuiProvider.class);
	}

	public Stream<ForgeDirection> getSides(ItemStack stack) {
		if(!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound nbt = stack.getTagCompound();
		return Arrays.stream(Sides.values()).filter(side -> nbt.getBoolean(side.getLpName())).map(Sides::getDir);
	}
}
