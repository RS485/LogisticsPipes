package logisticspipes.gui;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;

import logisticspipes.LPItems;
import logisticspipes.blocks.LogisticsProgramCompilerTileEntity;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.items.ItemModule;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.CompilerTriggerTaskPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.InputBar;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.TextListDisplay;
import network.rs485.logisticspipes.util.TextUtil;

//TODO: Config Option for disabling program compilation
public class GuiProgramCompiler extends LogisticsBaseGuiScreen {

	private final LogisticsProgramCompilerTileEntity compiler;
	private final TextListDisplay.List categoryTextList;
	private final TextListDisplay.List programTextList;
	private final TextListDisplay categoryList;
	private final TextListDisplay programList;
	private final TextListDisplay programListLarge;
	private SmallGuiButton programmerButton;
	private InputBar search;

	public GuiProgramCompiler(EntityPlayer player, LogisticsProgramCompilerTileEntity compiler) {
		super(180, 190, 0, 0);
		this.compiler = compiler;

		DummyContainer dummy = new DummyContainer(player.inventory, compiler.getInventory());

		dummy.addRestrictedSlot(0, compiler.getInventory(), 10, 10, LPItems.disk);
		dummy.addRestrictedSlot(1, compiler.getInventory(), 154, 10, LPItems.logisticsProgrammer);

		dummy.addNormalSlotsForPlayerInventory(10, 105);

		categoryTextList = new TextListDisplay.List() {

			@Override
			public int getSize() {
				if (compiler.getInventory().getStackInSlot(0).isEmpty()) {
					return 0;
				}
				NBTTagList list = compiler.getNBTTagListForKey("compilerCategories");
				return (int) LogisticsProgramCompilerTileEntity.programByCategory.keySet().stream()
						.filter(it -> StreamSupport.stream(list.spliterator(), false).noneMatch(nbtBase -> ((NBTTagString) nbtBase).getString().equals(it.toString()))).count();
			}

			@Override
			public String getTextAt(int index) {
				if (compiler.getInventory().getStackInSlot(0).isEmpty()) {
					return "";
				}
				NBTTagList list = compiler.getNBTTagListForKey("compilerCategories");
				return TextUtil.translate("gui.compiler." + LogisticsProgramCompilerTileEntity.programByCategory.keySet().stream()
						.filter(it -> StreamSupport.stream(list.spliterator(), false).noneMatch(nbtBase -> ((NBTTagString) nbtBase).getString().equals(it.toString())))
						.skip(index)
						.findFirst()
						.map(it -> String.format("%s.%s", it.getResourceDomain(), it.getResourcePath()))
						.orElse(null));
			}

			@Override
			public int getTextColor(int index) {
				return 0xFFFFFF;
			}
		};
		categoryList = new TextListDisplay(this, 8, 30, 110, 104, 5, categoryTextList);

		programTextList = new TextListDisplay.List() {

			@Override
			public int getSize() {
				if (compiler.getInventory().getStackInSlot(0).isEmpty()) {
					return 0;
				}
				NBTTagList list = compiler.getNBTTagListForKey("compilerCategories");

				return getProgramListForSelectionIndex(list).size();
			}

			@Override
			public String getTextAt(int index) {
				if (compiler.getInventory().getStackInSlot(0).isEmpty()) {
					return "";
				}
				NBTTagList list = compiler.getNBTTagListForKey("compilerCategories");
				ResourceLocation sel = getProgramListForSelectionIndex(list).get(index);

				Item selItem = Item.REGISTRY.getObject(sel);
				if (selItem != null) {
					return TextUtil.translate(selItem.getUnlocalizedName() + ".name");
				}
				return "UNDEFINED";
			}

			@Override
			public int getTextColor(int index) {
				if (compiler.getInventory().getStackInSlot(0).isEmpty()) {
					return 0xFFFFFF;
				}
				NBTTagList list = compiler.getNBTTagListForKey("compilerCategories");
				ResourceLocation sel = getProgramListForSelectionIndex(list).get(index);

				NBTTagList listPrograms = compiler.getNBTTagListForKey("compilerPrograms");
				return StreamSupport.stream(listPrograms.spliterator(), false).anyMatch(it -> new ResourceLocation(((NBTTagString) it).getString()).equals(sel))
						? 0xAAFFAA : 0xFFAAAA;
			}
		};

		programList = new TextListDisplay(this, 80, 30, 8, 104, 5, programTextList);
		programListLarge = new TextListDisplay(this, 8, 30, 8, 104, 5, programTextList);

		inventorySlots = dummy;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		super.initGui();
		buttonList.clear();
		buttonList.add(new SmallGuiButton(0, guiLeft + 8, guiTop + 90, 15, 10, "/\\"));
		buttonList.add(new SmallGuiButton(1, guiLeft + 24, guiTop + 90, 15, 10, "\\/"));
		buttonList.add(new SmallGuiButton(2, guiLeft + 40, guiTop + 90, 40, 10, "Unlock"));
		buttonList.add(new SmallGuiButton(3, guiLeft + 100, guiTop + 90, 15, 10, "/\\"));
		buttonList.add(new SmallGuiButton(4, guiLeft + 116, guiTop + 90, 15, 10, "\\/"));
		buttonList.add(programmerButton = new SmallGuiButton(5, guiLeft + 132, guiTop + 90, 40, 10, "Compile"));

		search = new InputBar(fontRenderer, this, guiLeft + 30, guiTop + 11, 120, 16);
	}

	@Override
	public void closeGui() throws IOException {
		super.closeGui();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		switch (button.id) {
			case 0:
				categoryList.scrollDown();
				break;
			case 1:
				categoryList.scrollUp();
				break;
			case 2:
				if (categoryList.getSelected() != -1) {
					NBTTagList list = compiler.getNBTTagListForKey("compilerCategories");
					LogisticsProgramCompilerTileEntity.programByCategory.keySet().stream()
							.filter(it -> StreamSupport.stream(list.spliterator(), false).noneMatch(nbtBase -> ((NBTTagString) nbtBase).getString().equals(it.toString())))
							.skip(categoryList.getSelected())
							.findFirst()
							.ifPresent(it -> {
								MainProxy.sendPacketToServer(PacketHandler.getPacket(CompilerTriggerTaskPacket.class).setCategory(it).setType("category").setTilePos(compiler));
							});
				}
				break;
			case 3:
				if (categoryTextList.getSize() == 0 && programTextList.getSize() != 0) {
					programListLarge.scrollDown();
				} else {
					programList.scrollDown();
				}
				break;
			case 4:
				if (categoryTextList.getSize() == 0 && programTextList.getSize() != 0) {
					programListLarge.scrollUp();
				} else {
					programList.scrollUp();
				}
				break;
			case 5:
				int selIndex = programList.getSelected();
				if (categoryTextList.getSize() == 0 && programTextList.getSize() != 0) {
					selIndex = programListLarge.getSelected();
				}
				if (selIndex != -1) {
					NBTTagList list = compiler.getNBTTagListForKey("compilerCategories");
					ResourceLocation sel = getProgramListForSelectionIndex(list).get(selIndex);

					NBTTagList listPrograms = compiler.getNBTTagListForKey("compilerPrograms");
					boolean flag = StreamSupport.stream(listPrograms.spliterator(), false)
							.anyMatch(it -> new ResourceLocation(((NBTTagString) it).getString()).equals(sel));
					MainProxy.sendPacketToServer(PacketHandler.getPacket(CompilerTriggerTaskPacket.class).setCategory(sel).setType(flag ? "flash" : "program").setTilePos(compiler));
				}
				break;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		GuiGraphics.drawPlayerInventoryBackground(mc, guiLeft + 10, guiTop + 105);
		GuiGraphics.drawSlotDiskBackground(mc, guiLeft + 9, guiTop + 9);
		GuiGraphics.drawSlotProgrammerBackground(mc, guiLeft + 153, guiTop + 9);

		if (compiler.getCurrentTask() != null) {
			fontRenderer.drawString(TextUtil.translate("gui.compiler.processing"), guiLeft + 10, guiTop + 39, 0x000000);
			String name;
			Item item = Item.REGISTRY.getObject(compiler.getCurrentTask());
			if (item != null) {
				name = item.getUnlocalizedName() + ".name";
			} else {
				name = "gui.compiler." + compiler.getCurrentTask().toString().replace(':', '.');
			}
			String text = TextUtil.getTrimmedString(TextUtil.translate(name),
					160, fontRenderer, "...");
			fontRenderer.drawString(text, guiLeft + 10, guiTop + 70, 0x000000);
			drawRect(guiLeft + 9, guiTop + 50, guiLeft + 171, guiTop + 66, Color.BLACK);
			drawRect(guiLeft + 10, guiTop + 51, guiLeft + 170, guiTop + 65, Color.WHITE);
			drawRect(guiLeft + 11, guiTop + 52, guiLeft + 11 + (int) (158 * compiler.getTaskProgress()), guiTop + 64, Color.GREEN);

			if (!compiler.isWasAbleToConsumePower()) {
				fontRenderer.drawString(TextUtil.translate("gui.compiler.nopower.1"), guiLeft + 68, guiTop + 10, 0x000000);
				fontRenderer.drawString(TextUtil.translate("gui.compiler.nopower.2"), guiLeft + 35, guiTop + 20, 0x000000);
			}

			buttonList.forEach(b -> b.visible = false);
		} else {
			buttonList.forEach(b -> b.visible = true);
			if (categoryTextList.getSize() == 0 && programTextList.getSize() != 0) {
				buttonList.stream().limit(3).forEach(b -> b.visible = false);
				programListLarge.renderGuiBackground(var2, var3);
			} else {
				buttonList.stream().limit(3).forEach(b -> b.visible = true);
				categoryList.renderGuiBackground(var2, var3);
				programList.renderGuiBackground(var2, var3);
			}

			search.drawTextBox();

			int selIndex = programList.getSelected();
			if (categoryTextList.getSize() == 0 && programTextList.getSize() != 0) {
				selIndex = programListLarge.getSelected();
			}

			if (selIndex != -1) {
				NBTTagList list = compiler.getNBTTagListForKey("compilerCategories");
				ResourceLocation sel = getProgramListForSelectionIndex(list).get(selIndex);

				NBTTagList listProgramms = compiler.getNBTTagListForKey("compilerPrograms");
				if (StreamSupport.stream(listProgramms.spliterator(), false).anyMatch(it -> new ResourceLocation(((NBTTagString) it).getString()).equals(sel))) {
					programmerButton.displayString = "Flash";
					programmerButton.enabled = !compiler.getInventory().getStackInSlot(1).isEmpty();
				} else {
					programmerButton.displayString = "Compile";
					programmerButton.enabled = true;
				}
			}
		}
	}

	private List<ResourceLocation> getProgramListForSelectionIndex(NBTTagList list) {
		return StreamSupport.stream(list.spliterator(), false).flatMap(
				nbtBase -> LogisticsProgramCompilerTileEntity.programByCategory.get(new ResourceLocation(((NBTTagString) nbtBase).getString()))
						.stream())
				.filter(it -> TextUtil.translate(Item.REGISTRY.getObject(it).getUnlocalizedName() + ".name").toLowerCase().contains(search.getText().toLowerCase()))
				.sorted(Comparator.comparing(o -> getSortingClass(Item.REGISTRY.getObject((ResourceLocation) o)))
						.thenComparing(o -> TextUtil.translate(Item.REGISTRY.getObject((ResourceLocation) o).getUnlocalizedName() + ".name").toLowerCase())
				)
				.collect(Collectors.toList());
	}

	private int getSortingClass(Item object) {
		if (object instanceof ItemLogisticsPipe) {
			return 0;
		} else if (object instanceof ItemModule) {
			return 1;
		} else if (object instanceof ItemUpgrade) {
			return 2;
		}
		return 10;
	}

	@Override
	public void handleMouseInputSub() throws IOException {
		int wheel = org.lwjgl.input.Mouse.getDWheel() / 120;
		if (wheel == 0) {
			super.handleMouseInputSub();
		}
		if (compiler.getCurrentTask() == null) {
			if (categoryTextList.getSize() == 0 && programTextList.getSize() != 0) {
				if (wheel < 0) {
					programListLarge.mouseScrollUp();
				} else if (wheel > 0) {
					programListLarge.mouseScrollDown();
				}
			} else {
				if (wheel < 0) {
					categoryList.mouseScrollUp();
					programList.mouseScrollUp();
				} else if (wheel > 0) {
					categoryList.mouseScrollDown();
					programList.mouseScrollDown();
				}
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (compiler.getCurrentTask() == null) {
			if (!search.handleKey(typedChar, keyCode)) {
				super.keyTyped(typedChar, keyCode);
			}
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) throws IOException {
		super.mouseClicked(par1, par2, par3);
		if (compiler.getCurrentTask() == null) {
			search.handleClick(par1, par2, par3);
			if (categoryTextList.getSize() == 0 && programTextList.getSize() != 0) {
				programListLarge.mouseClicked(par1, par2, par3);
			} else {
				categoryList.mouseClicked(par1, par2, par3);
				programList.mouseClicked(par1, par2, par3);
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		if (compiler.getCurrentTask() == null) {
			if (categoryTextList.getSize() == 0 && programTextList.getSize() != 0) {
				programListLarge.renderGuiForeground();
			} else {
				categoryList.renderGuiForeground();
				programList.renderGuiForeground();
			}
		}
	}
}
