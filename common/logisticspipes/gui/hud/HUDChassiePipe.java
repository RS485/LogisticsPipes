package logisticspipes.gui.hud;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.hud.HUDConfig;
import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.logisticspipes.ChassiModule;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.hud.BasicHUDButton;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class HUDChassiePipe extends BasicHUDGui {
	
	private final PipeLogisticsChassi pipe;
	private final ChassiModule module;
	private final SimpleInventory moduleInventory;
	
	private int selected = -1;
	private int modulePage = 0;

	private int xCursor;
	private int yCursor;
	
	public HUDChassiePipe(PipeLogisticsChassi pipeLogisticsChassi, ChassiModule _module, SimpleInventory _moduleInventory) {
		this.pipe = pipeLogisticsChassi;
		this.module = _module;
		this.moduleInventory = _moduleInventory;
		for(int i=0;i<pipe.getChassiSize();i++) {
			this.addButton(new ItemButton(moduleInventory, i, -45, -35 + ((i % 3) * 27), 20, 25));
		}
		this.addButton(new BasicHUDButton("<",-45,-45,8,8) {
			
			@Override
			public boolean shouldRenderButton() {
				return !isSlotSelected();
			}
			
			@Override
			public void clicked() {
				modulePage--;
			}
			
			@Override
			public boolean buttonEnabled() {
				return modulePage > 0;
			}
		});
		this.addButton(new BasicHUDButton(">",-33,-45,8,8) {
			
			@Override
			public boolean shouldRenderButton() {
				return !isSlotSelected();
			}
			
			@Override
			public void clicked() {
				modulePage++;
			}
			
			@Override
			public boolean buttonEnabled() {
				return modulePage < ((pipe.getChassiSize() - 1) / 3);
			}
		});
		this.addButton(new BasicHUDButton("x",37,-45,8,8) {
			
			@Override
			public boolean shouldRenderButton() {
				return isSlotSelected();
			}
			
			@Override
			public void clicked() {
				resetSelection();
			}
			
			@Override
			public boolean buttonEnabled() {
				return true;
			}
		});
	}

	@Override
	public void renderHeadUpDisplay(double distance, boolean day, Minecraft mc, HUDConfig config) {
		if(day) {
        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)64);
        } else {
        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)64);	
        }
		BasicGuiHelper.drawGuiBackGround(mc, -50, -50, 50, 50, 0, false);
		if(day) {
        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)127);
        } else {
        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)127);	
        }
		GL11.glTranslatef(0.0F, 0.0F,(float) (-0.00005F * distance));
		super.renderHeadUpDisplay(distance, day, mc, config);
		if(selected != -1) {
			LogisticsModule selectedmodule = module.getSubModule(selected);
			if(selectedmodule == null) return;
			
        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)64);
			BasicGuiHelper.drawGuiBackGround(mc, -23, -35, 45, 45, 0, false);
        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)127);	

			if(selectedmodule instanceof IHUDModuleHandler && ((IHUDModuleHandler)selectedmodule).getRenderer() != null) {
				GL11.glTranslatef(11.0F, 5.0F, (float) (-0.00005F * distance));
				((IHUDModuleHandler)selectedmodule).getRenderer().renderContent();
				if(((IHUDModuleHandler)selectedmodule).getRenderer().getButtons() != null) {
					for(IHUDButton button:((IHUDModuleHandler)selectedmodule).getRenderer().getButtons()) {
					button.renderAlways();
						if(button.shouldRenderButton()) {
							button.renderButton(button.isFocused(), button.isblockFocused());
						}
						if(!button.buttonEnabled() || !button.shouldRenderButton()) continue;
						if((button.getX() - 1 < (xCursor - 11) && (xCursor - 11) < (button.getX() + button.sizeX() + 1)) && (button.getY() - 1 < (yCursor - 5) && (yCursor - 5) < (button.getY() + button.sizeY() + 1))) {
							if(!button.isFocused() && !button.isblockFocused()) {
								button.setFocused();
							} else if(button.focusedTime() > 400 && !button.isblockFocused()) {
								button.clicked();
								button.blockFocused();
							}
						} else if(button.isFocused() || button.isblockFocused()) {
							button.clearFocused();
						}
					}
				}
				GL11.glTranslatef(-11.0F, -5.0F, (float) (0.00005F * distance));
			} else {
				GL11.glTranslatef(0.0F, 0.0F, (float) (-0.00005F * distance));
				mc.fontRenderer.drawString("Nothing" , -5, -15, 0);
				mc.fontRenderer.drawString("to" , 9, -5, 0);
				mc.fontRenderer.drawString("display" , -5, 5, 0);
				GL11.glTranslatef(0.0F, 0.0F, (float) (0.00005F * distance));
			}
		} else {
			GL11.glTranslatef(0.0F, 0.0F, (float) (-0.005F * distance));
			GL11.glScalef(1.5F, 1.5F, 0.0001F);
			GL11.glScalef(0.8F, 0.8F, -1F);
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(pipe.displayList, null, 0, -15, -35, 3, 12, 18, 18, mc, true, true, true, true);
		}
		GL11.glTranslatef(0.0F, 0.0F, (float) (0.00005F * distance));
	}

	@Override
	public boolean display(HUDConfig config) {
		if(!config.isHUDChassie()) return false;
		for(int i=0;i<moduleInventory.getSizeInventory();i++) {
			ItemStack stack = moduleInventory.getStackInSlot(i);
			if(stack != null && stack.itemID != 0) {
				return true;
			}
		}
		return true;
	}

	@Override
	public boolean cursorOnWindow(int x, int y) {
		return -50 < x && x < 50 && -50 < y && y < 50;
	}

	@Override
	public void handleCursor(int x, int y) {
		super.handleCursor(x,y);
		xCursor = x;
		yCursor = y;
	}
	
	private void moduleClicked(int number) {
		selected = number;
		if(selected != -1) {
			LogisticsModule selectedmodule = module.getSubModule(selected);
			if(selectedmodule instanceof IHUDModuleHandler) {
				((IHUDModuleHandler)selectedmodule).startWatching();
			}
		}
	}
	
	private void resetSelection() {
		if(selected != -1) {
			LogisticsModule selectedmodule = module.getSubModule(selected);
			if(selectedmodule instanceof IHUDModuleHandler) {
				((IHUDModuleHandler)selectedmodule).stopWatching();
			}
		}
		selected = -1;
	}
	
	private boolean isSlotSelected() {
		return selected != -1;
	}
	
	private boolean isSlotSelected(int number) {
		return selected == number;
	}
	
	private boolean shouldDisplayButton(int number) {
		return modulePage * 3 <= number && number < (modulePage + 1) * 3;
	}

	public void stopWatching() {
		resetSelection();
	}
	
	private class ItemButton extends BasicHUDButton {
		
		private SimpleInventory inv;
		private int position;
		
		public ItemButton(SimpleInventory inv, int position,int x, int y, int width, int heigth) {
			super("item." + position, x, y, width, heigth);
			this.inv = inv;
			this.position = position;
		}

		@Override
		public void clicked() {
			moduleClicked(position);
		}

		@Override
		public void renderButton(boolean hover, boolean clicked) {
			GL11.glTranslatef(0.0F, 0.0F, -0.00005F);
			Minecraft mc = FMLClientHandler.instance().getClient();
			if(hover && !isSlotSelected(position)) {
				GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)127);
				if(!clicked) {
					GL11.glTranslatef(0.0F, 0.0F, -0.01F);
				}
			} else {
				if(!this.buttonEnabled() && !isSlotSelected(position)) {
					GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)32);
				} else {
					GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)64);
				}
			}
			GL11.glScaled(0.5D, 0.5D, 1.0D);
			if(isSlotSelected(position)) {
				BasicGuiHelper.drawGuiBackGround(mc, posX * 2, posY * 2, (posX + sizeX) * 2 + 19, (posY + sizeY) * 2, 0, false, true, true, true, false);
			} else {
				BasicGuiHelper.drawGuiBackGround(mc, posX * 2, posY * 2, (posX + sizeX) * 2, (posY + sizeY) * 2, 0, false);
			}
			GL11.glScaled(2.0D, 2.0D, 1.0D);

			if(clicked) {
				GL11.glTranslatef(0.0F, 0.0F, -0.01F);
			}
			
			ItemStack module = inv.getStackInSlot(position);
			List<ItemIdentifierStack> list = new ArrayList<ItemIdentifierStack>();
			list.add(ItemIdentifierStack.GetFromStack(module));
			GL11.glTranslatef(0.0F, 0.0F, -0.00005F);
			if(!this.buttonEnabled() && !isSlotSelected(position)) {
				GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)32);
			} else {
				GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)127);
			}
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(list, null, 0, posX + ((sizeX - 16) / 2), posY + ((sizeY - 16) / 2), 1, 1, 18, 18, mc, false, false, this.buttonEnabled() || isSlotSelected(position), true);
			if(hover) {
				GL11.glTranslatef(0.0F, 0.0F, 0.01F);
			}
			GL11.glTranslatef(0.0F, 0.0F, 0.0001F);
		}


		@Override
		public void renderAlways() {
			if(inv.getStackInSlot(position) == null && shouldDisplayButton(position)) {
				GL11.glPushMatrix();
				GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)64);
				GL11.glScaled(0.5D, 0.5D, 1.0D);
				Minecraft mc = FMLClientHandler.instance().getClient();
				BasicGuiHelper.drawGuiBackGround(mc, posX * 2, posY * 2, (posX + sizeX) * 2, (posY + sizeY) * 2, 0, false);
				GL11.glPopMatrix();
			}
		}
		
		@Override
		public boolean shouldRenderButton() {
			boolean result = inv.getStackInSlot(position) != null && shouldDisplayButton(position);
			return result;
		}

		@Override
		public boolean buttonEnabled() {
			return !isSlotSelected();
		}
	}
}
