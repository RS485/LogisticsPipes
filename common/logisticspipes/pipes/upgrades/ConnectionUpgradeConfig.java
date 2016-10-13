package logisticspipes.pipes.upgrades;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.AllArgsConstructor;
import lombok.Getter;

import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractguis.UpgradeCoordinatesGuiProvider;
import logisticspipes.network.guis.upgrade.DisconnectionUpgradeConfigGuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;

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
