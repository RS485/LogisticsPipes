package logisticspipes.recipes;

import net.minecraft.item.ItemStack;

import lombok.Data;

@Data
public class CraftingParts {
	/**
	 * Iron Chip
	 * FPGA
	 */
	private final ItemStack chipFpga;
	/**
	 * Gold Chip
	 * Basic Microcontroller
	 */
	private final ItemStack chipBasic;
	/**
	 * Diamond Chip
	 * Advanced Microcontroller
	 */
	private final ItemStack chipAdvanced;
}
