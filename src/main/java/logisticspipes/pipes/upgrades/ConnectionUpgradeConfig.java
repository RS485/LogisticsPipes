package logisticspipes.pipes.upgrades;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import lombok.AllArgsConstructor;
import lombok.Getter;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractguis.UpgradeCoordinatesGuiProvider;
import logisticspipes.network.guis.upgrade.DisconnectionUpgradeConfigGuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public class ConnectionUpgradeConfig implements IConfigPipeUpgrade {

	public static String getName() {
		return "disconnection";
	}

	@AllArgsConstructor
	public enum Sides {
		UP(EnumFacing.UP, "LPDIS-UP"),
		DOWN(EnumFacing.DOWN, "LPDIS-DOWN"),
		NORTH(EnumFacing.NORTH, "LPDIS-NORTH"),
		SOUTH(EnumFacing.SOUTH, "LPDIS-SOUTH"),
		EAST(EnumFacing.EAST, "LPDIS-EAST"),
		WEST(EnumFacing.WEST, "LPDIS-WEST");
		@Getter
		private EnumFacing dir;
		@Getter
		private String lpName;

		public static String getNameForDirection(EnumFacing fd) {
			Optional<Sides> opt = Arrays.stream(values()).filter(side -> side.getDir() == fd).findFirst();
			if (opt.isPresent()) {
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

	@Nonnull
	public Stream<EnumFacing> getSides(@Nonnull ItemStack stack) {
		if (stack.isEmpty()) return Stream.empty();
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		final NBTTagCompound tag = Objects.requireNonNull(stack.getTagCompound());
		return Arrays.stream(Sides.values()).filter(side -> tag.getBoolean(side.getLpName())).map(Sides::getDir);
	}
}
