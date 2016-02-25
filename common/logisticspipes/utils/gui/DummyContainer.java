/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils.gui;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IFuzzySlot;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.interfaces.ISlotClick;
import logisticspipes.items.ItemModule;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.ChassiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.FuzzySlotSettingsPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.resources.DictResource;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.MinecraftColor;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

public class DummyContainer extends Container {

	protected IInventory _playerInventory;
	protected IInventory _dummyInventory;
	protected IGuiOpenControler[] _controler;
	private List<Slot> transferTop = new ArrayList<Slot>();
	private List<Slot> transferBottom = new ArrayList<Slot>();
	private long lastClicked;
	private long lastDragnDropLockup;
	boolean wasDummyLookup;
	boolean overrideMCAntiSend;
	public List<BitSet> inventoryFuzzySlotsContent = new ArrayList<BitSet>();

	public DummyContainer(IInventory playerInventory, IInventory dummyInventory) {
		_playerInventory = playerInventory;
		_dummyInventory = dummyInventory;
		_controler = null;
	}

	public DummyContainer(EntityPlayer player, IInventory dummyInventory, IGuiOpenControler... controler) {
		_playerInventory = player.inventory;
		_dummyInventory = dummyInventory;
		_controler = controler;
		if (MainProxy.isServer()) {
			for (IGuiOpenControler element : _controler) {
				element.guiOpenedByPlayer(player);
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	/***
	 * Adds all slots for the player inventory and hotbar
	 * 
	 * @param xOffset
	 * @param yOffset
	 */
	public void addNormalSlotsForPlayerInventory(int xOffset, int yOffset) {
		if (_playerInventory == null) {
			return;
		}
		// Player "backpack"
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				Slot slot = new Slot(_playerInventory, column + row * 9 + 9, xOffset + column * 18, yOffset + row * 18);
				addSlotToContainer(slot);
				transferBottom.add(slot);
			}
		}

		// Player "hotbar"
		for (int i1 = 0; i1 < 9; i1++) {
			Slot slot = new Slot(_playerInventory, i1, xOffset + i1 * 18, yOffset + 58);
			addSlotToContainer(slot);
			transferBottom.add(slot);
		}
	}

	/**
	 * Add a dummy slot that will not consume players items
	 * 
	 * @param slotId
	 *            The slot number in the dummy IInventory this slot should map
	 * @param xCoord
	 *            xCoord of TopLeft corner of where the slot should be rendered
	 * @param yCoord
	 *            yCoord of TopLeft corner of where the slot should be rendered
	 */
	public Slot addDummySlot(int slotId, int xCoord, int yCoord) {
		return addSlotToContainer(new DummySlot(_dummyInventory, slotId, xCoord, yCoord));
	}

	public Slot addDummySlot(int slotId, IInventory dummy, int xCoord, int yCoord) {
		return addSlotToContainer(new DummySlot(dummy, slotId, xCoord, yCoord));
	}

	public void addNormalSlot(int slotId, IInventory inventory, int xCoord, int yCoord) {
		transferTop.add(addSlotToContainer(new Slot(inventory, slotId, xCoord, yCoord)));
	}

	public Slot addRestrictedSlot(int slotId, IInventory inventory, int xCoord, int yCoord, Item item) {
		return addSlotToContainer(new RestrictedSlot(inventory, slotId, xCoord, yCoord, item));
	}

	public Slot addStaticRestrictedSlot(int slotId, IInventory inventory, int xCoord, int yCoord, Item item, int stackLimit) {
		return addSlotToContainer(new StaticRestrictedSlot(inventory, slotId, xCoord, yCoord, item, stackLimit));
	}

	public Slot addRestrictedSlot(int slotId, IInventory inventory, int xCoord, int yCoord, ISlotCheck slotCheck) {
		return addSlotToContainer(new RestrictedSlot(inventory, slotId, xCoord, yCoord, slotCheck));
	}

	public Slot addStaticRestrictedSlot(int slotId, IInventory inventory, int xCoord, int yCoord, ISlotCheck slotCheck, int stackLimit) {
		return addSlotToContainer(new StaticRestrictedSlot(inventory, slotId, xCoord, yCoord, slotCheck, stackLimit));
	}

	public void addModuleSlot(int slotId, IInventory inventory, int xCoord, int yCoord, PipeLogisticsChassi pipe) {
		transferTop.add(addSlotToContainer(new ModuleSlot(inventory, slotId, xCoord, yCoord, pipe)));
	}

	public Slot addFluidSlot(int slotId, IInventory inventory, int xCoord, int yCoord) {
		return addSlotToContainer(new FluidSlot(inventory, slotId, xCoord, yCoord));
	}

	public Slot addColorSlot(int slotId, IInventory inventory, int xCoord, int yCoord) {
		return addSlotToContainer(new ColorSlot(inventory, slotId, xCoord, yCoord));
	}

	public Slot addUnmodifiableSlot(int slotId, IInventory inventory, int xCoord, int yCoord) {
		return addSlotToContainer(new UnmodifiableSlot(inventory, slotId, xCoord, yCoord));
	}

	public Slot addCallableSlotHandler(int slotId, IInventory inventory, int xCoord, int yCoord, ISlotClick handler) {
		return addSlotToContainer(new HandelableSlot(inventory, slotId, xCoord, yCoord, handler));
	}

	public Slot addFuzzyDummySlot(int slotId, int xCoord, int yCoord, DictResource dictResource) {
		return addSlotToContainer(new FuzzyDummySlot(_dummyInventory, slotId, xCoord, yCoord, dictResource));
	}

	public Slot addFuzzyUnmodifiableSlot(int slotId, IInventory inventory, int xCoord, int yCoord, DictResource dictResource) {
		return addSlotToContainer(new FuzzyUnmodifiableSlot(inventory, slotId, xCoord, yCoord, dictResource));
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer pl, int i) {
		if (transferTop.isEmpty() || transferBottom.isEmpty()) {
			return null;
		}
		Slot slot = (Slot) inventorySlots.get(i);
		if (slot == null || slot instanceof DummySlot || slot instanceof UnmodifiableSlot || slot instanceof FluidSlot || slot instanceof ColorSlot || slot instanceof HandelableSlot || !slot.getHasStack()) {
			return null;
		}
		if (transferTop.contains(slot)) {
			handleShiftClickLists(slot, transferBottom, true, pl);
			handleShiftClickLists(slot, transferBottom, false, pl);
			return null;
		} else if (transferBottom.contains(slot)) {
			handleShiftClickLists(slot, transferTop, true, pl);
			handleShiftClickLists(slot, transferTop, false, pl);
			return null;
		} else {
			return null;
		}
	}

	private void handleShiftClickLists(Slot from, List<Slot> toList, boolean ignoreEmpty, EntityPlayer player) {
		if (!from.getHasStack()) {
			return;
		}
		for (Slot to : toList) {
			if (handleShiftClickForSlots(from, to, ignoreEmpty, player)) {
				return;
			}
		}
	}

	private boolean handleShiftClickForSlots(Slot from, Slot to, boolean ignoreEmpty, EntityPlayer player) {
		if (!from.getHasStack()) {
			return true;
		}
		if (!to.getHasStack() && !ignoreEmpty) {
			ItemStack out = from.getStack();
			boolean remove = true;
			if(out.stackSize > to.getSlotStackLimit()) {
				out = from.decrStackSize(to.getSlotStackLimit());
				remove = false;
			}
			from.onPickupFromSlot(player, out);
			to.putStack(out);
			if(remove) {
				from.putStack(null);
			}
			return true;
		}
		if(from instanceof ModuleSlot || to instanceof ModuleSlot) {
			return false;
		}
		ItemStack out = from.getStack();
		from.onPickupFromSlot(player, out);
		if (to.getHasStack() && to.getStack().isItemEqual(out) && ItemStack.areItemStackTagsEqual(to.getStack(), from.getStack())) {
			int free = Math.min(to.getSlotStackLimit(), to.getStack().getMaxStackSize()) - to.getStack().stackSize;
			if(free > 0) {
				ItemStack toInsert = from.decrStackSize(free);
				from.onPickupFromSlot(player, toInsert);
				ItemStack toStack = to.getStack();
				if(toInsert != null && toStack != null) {
					toStack.stackSize += toInsert.stackSize;
					to.putStack(toStack);
					return !from.getHasStack();
				}
			}
		}
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ItemStack superSlotClick(int par1, int par2, int par3, EntityPlayer par4EntityPlayer) {
		ItemStack itemstack = null;
		InventoryPlayer inventoryplayer = par4EntityPlayer.inventory;
		int i1;
		ItemStack itemstack3;

		if (par3 == 5) {
			int l = field_94536_g;
			field_94536_g = Container.func_94532_c(par2);

			if ((l != 1 || field_94536_g != 2) && l != field_94536_g) {
				func_94533_d();
			} else if (inventoryplayer.getItemStack() == null) {
				func_94533_d();
			} else if (field_94536_g == 0) {
				field_94535_f = Container.func_94529_b(par2);

				if (Container.func_94528_d(field_94535_f)) {
					field_94536_g = 1;
					field_94537_h.clear();
				} else {
					func_94533_d();
				}
			} else if (field_94536_g == 1) {
				Slot slot = (Slot) inventorySlots.get(par1);

				if (slot != null && Container.func_94527_a(slot, inventoryplayer.getItemStack(), true) && slot.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize > field_94537_h.size() && canDragIntoSlot(slot)) {
					field_94537_h.add(slot);
				}
			} else if (field_94536_g == 2) {
				if (!field_94537_h.isEmpty()) {
					itemstack3 = inventoryplayer.getItemStack().copy();
					i1 = inventoryplayer.getItemStack().stackSize;
					Iterator iterator = field_94537_h.iterator();

					while (iterator.hasNext()) {
						Slot slot1 = (Slot) iterator.next();

						if (slot1 != null && Container.func_94527_a(slot1, inventoryplayer.getItemStack(), true) && slot1.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize >= field_94537_h.size() && canDragIntoSlot(slot1)) {
							ItemStack itemstack1 = itemstack3.copy();
							int j1 = slot1.getHasStack() ? slot1.getStack().stackSize : 0;
							Container.func_94525_a(field_94537_h, field_94535_f, itemstack1, j1);

							if (itemstack1.stackSize > itemstack1.getMaxStackSize()) {
								itemstack1.stackSize = itemstack1.getMaxStackSize();
							}

							if (itemstack1.stackSize > slot1.getSlotStackLimit()) {
								itemstack1.stackSize = slot1.getSlotStackLimit();
							}

							i1 -= itemstack1.stackSize - j1;
							slot1.putStack(itemstack1);
						}
					}

					itemstack3.stackSize = i1;

					if (itemstack3.stackSize <= 0) {
						itemstack3 = null;
					}

					inventoryplayer.setItemStack(itemstack3);
				}

				func_94533_d();
			} else {
				func_94533_d();
			}
		} else if (field_94536_g != 0) {
			func_94533_d();
		} else {
			Slot slot2;
			int l1;
			ItemStack itemstack5;

			if ((par3 == 0 || par3 == 1) && (par2 == 0 || par2 == 1)) {
				if (par1 == -999) {
					if (inventoryplayer.getItemStack() != null && par1 == -999) {
						if (par2 == 0) {
							par4EntityPlayer.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack(), true);
							inventoryplayer.setItemStack((ItemStack) null);
						}

						if (par2 == 1) {
							par4EntityPlayer.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack().splitStack(1), true);

							if (inventoryplayer.getItemStack().stackSize == 0) {
								inventoryplayer.setItemStack((ItemStack) null);
							}
						}
					}
				} else if (par3 == 1) {
					if (par1 < 0) {
						return null;
					}

					slot2 = (Slot) inventorySlots.get(par1);

					if (slot2 != null && slot2.canTakeStack(par4EntityPlayer)) {
						itemstack3 = transferStackInSlot(par4EntityPlayer, par1);

						if (itemstack3 != null) {
							Item item = itemstack3.getItem();
							itemstack = itemstack3.copy();

							if (slot2.getStack() != null && slot2.getStack().getItem() == item) {
								retrySlotClick(par1, par2, true, par4EntityPlayer);
							}
						}
					}
				} else {
					if (par1 < 0) {
						return null;
					}

					slot2 = (Slot) inventorySlots.get(par1);

					if (slot2 != null) {
						itemstack3 = slot2.getStack();
						ItemStack itemstack4 = inventoryplayer.getItemStack();

						if (itemstack3 != null) {
							itemstack = itemstack3.copy();
						}

						if (itemstack3 == null) {
							if (itemstack4 != null && slot2.isItemValid(itemstack4)) {
								l1 = par2 == 0 ? itemstack4.stackSize : 1;

								if (l1 > slot2.getSlotStackLimit()) {
									l1 = slot2.getSlotStackLimit();
								}

								if (itemstack4.stackSize >= l1) {
									slot2.putStack(itemstack4.splitStack(l1));
								}

								if (itemstack4.stackSize == 0) {
									inventoryplayer.setItemStack((ItemStack) null);
								}
							}
						} else if (slot2.canTakeStack(par4EntityPlayer)) {
							if (itemstack4 == null) {
								l1 = par2 == 0 ? itemstack3.stackSize : (itemstack3.stackSize + 1) / 2;
								itemstack5 = slot2.decrStackSize(l1);
								inventoryplayer.setItemStack(itemstack5);

								if (itemstack3.stackSize == 0) {
									slot2.putStack((ItemStack) null);
								}

								slot2.onPickupFromSlot(par4EntityPlayer, inventoryplayer.getItemStack());
							} else if (slot2.isItemValid(itemstack4)) {
								if (itemstack3.getItem() == itemstack4.getItem() && itemstack3.getItemDamage() == itemstack4.getItemDamage() && areEqualForMerge(itemstack3, itemstack4, slot2)) { // XXX replaced ItemStack.areItemStackTagsEqual with areEqualForMerge for slot based handling
									l1 = par2 == 0 ? itemstack4.stackSize : 1;

									if (l1 > slot2.getSlotStackLimit() - itemstack3.stackSize) {
										l1 = slot2.getSlotStackLimit() - itemstack3.stackSize;
									}

									if (l1 > itemstack4.getMaxStackSize() - itemstack3.stackSize) {
										l1 = itemstack4.getMaxStackSize() - itemstack3.stackSize;
									}

									itemstack4.splitStack(l1);

									if (itemstack4.stackSize == 0) {
										inventoryplayer.setItemStack((ItemStack) null);
									}

									itemstack3.stackSize += l1;

									slot2.putStack(itemstack3); // XXX added reinserting of the modified itemStack (Fix ItemIdentifierInventory's disappearing items)

								} else if (itemstack4.stackSize <= slot2.getSlotStackLimit()) {
									handleSwitch(slot2, itemstack3, itemstack4, par4EntityPlayer); // XXX added Slot switching handle method
									slot2.putStack(itemstack4);
									inventoryplayer.setItemStack(itemstack3);
								}
							} else if (itemstack3.getItem() == itemstack4.getItem() && itemstack4.getMaxStackSize() > 1 && (!itemstack3.getHasSubtypes() || itemstack3.getItemDamage() == itemstack4.getItemDamage()) && areEqualForMerge(itemstack3, itemstack4, slot2)) { // XXX replaced ItemStack.areItemStackTagsEqual with areEqualForMerge for slot based handling
								l1 = itemstack3.stackSize;

								if (l1 > 0 && l1 + itemstack4.stackSize <= itemstack4.getMaxStackSize()) {
									itemstack4.stackSize += l1;
									itemstack3 = slot2.decrStackSize(l1);

									if (itemstack3.stackSize == 0) {
										slot2.putStack((ItemStack) null);
									}

									slot2.onPickupFromSlot(par4EntityPlayer, inventoryplayer.getItemStack());
								}
							}
						}

						slot2.onSlotChanged();
					}
				}
			} else if (par3 == 2 && par2 >= 0 && par2 < 9) {
				slot2 = (Slot) inventorySlots.get(par1);

				if (slot2.canTakeStack(par4EntityPlayer)) {
					itemstack3 = inventoryplayer.getStackInSlot(par2);
					boolean flag = itemstack3 == null || slot2.inventory == inventoryplayer && slot2.isItemValid(itemstack3);
					l1 = -1;

					if (!flag) {
						l1 = inventoryplayer.getFirstEmptyStack();
						flag |= l1 > -1;
					}

					if (slot2.getHasStack() && flag) {
						itemstack5 = slot2.getStack();
						inventoryplayer.setInventorySlotContents(par2, itemstack5.copy());

						if ((slot2.inventory != inventoryplayer || !slot2.isItemValid(itemstack3)) && itemstack3 != null) {
							if (l1 > -1) {
								inventoryplayer.addItemStackToInventory(itemstack3);
								slot2.decrStackSize(itemstack5.stackSize);
								slot2.putStack((ItemStack) null);
								slot2.onPickupFromSlot(par4EntityPlayer, itemstack5);
							}
						} else {
							slot2.decrStackSize(itemstack5.stackSize);
							slot2.putStack(itemstack3);
							slot2.onPickupFromSlot(par4EntityPlayer, itemstack5);
						}
					} else if (!slot2.getHasStack() && itemstack3 != null && slot2.isItemValid(itemstack3)) {
						inventoryplayer.setInventorySlotContents(par2, (ItemStack) null);
						slot2.putStack(itemstack3);
					}
				}
			} else if (par3 == 3 && par4EntityPlayer.capabilities.isCreativeMode && inventoryplayer.getItemStack() == null && par1 >= 0) {
				slot2 = (Slot) inventorySlots.get(par1);

				if (slot2 != null && slot2.getHasStack()) {
					itemstack3 = slot2.getStack().copy();
					itemstack3.stackSize = itemstack3.getMaxStackSize();
					inventoryplayer.setItemStack(itemstack3);
				}
			} else if (par3 == 4 && inventoryplayer.getItemStack() == null && par1 >= 0) {
				slot2 = (Slot) inventorySlots.get(par1);

				if (slot2 != null && slot2.getHasStack() && slot2.canTakeStack(par4EntityPlayer)) {
					itemstack3 = slot2.decrStackSize(par2 == 0 ? 1 : slot2.getStack().stackSize);
					slot2.onPickupFromSlot(par4EntityPlayer, itemstack3);
					par4EntityPlayer.dropPlayerItemWithRandomChoice(itemstack3, true);
				}
			} else if (par3 == 6 && par1 >= 0) {
				slot2 = (Slot) inventorySlots.get(par1);
				itemstack3 = inventoryplayer.getItemStack();

				if (itemstack3 != null && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(par4EntityPlayer))) {
					i1 = par2 == 0 ? 0 : inventorySlots.size() - 1;
					l1 = par2 == 0 ? 1 : -1;

					for (int i2 = 0; i2 < 2; ++i2) {
						for (int j2 = i1; j2 >= 0 && j2 < inventorySlots.size() && itemstack3.stackSize < itemstack3.getMaxStackSize(); j2 += l1) {
							Slot slot3 = (Slot) inventorySlots.get(j2);

							if (slot3.getHasStack() && Container.func_94527_a(slot3, itemstack3, true) && slot3.canTakeStack(par4EntityPlayer) && func_94530_a(itemstack3, slot3) && (i2 != 0 || slot3.getStack().stackSize != slot3.getStack().getMaxStackSize())) {
								int k1 = Math.min(itemstack3.getMaxStackSize() - itemstack3.stackSize, slot3.getStack().stackSize);
								ItemStack itemstack2 = slot3.decrStackSize(k1);
								itemstack3.stackSize += k1;

								if (itemstack2.stackSize <= 0) {
									slot3.putStack((ItemStack) null);
								}

								slot3.onPickupFromSlot(par4EntityPlayer, itemstack2);
							}
						}
					}
				}

				detectAndSendChanges();
			}
		}

		return itemstack;
	}

	private void handleSwitch(Slot slot2, ItemStack out, ItemStack in, EntityPlayer player) {
		if(slot2 instanceof ModuleSlot) {
			ChassiModule logisticsModule = (ChassiModule) ((ModuleSlot) slot2).get_pipe().getLogisticsModule();
			int moduleIndex = ((ModuleSlot) slot2).get_moduleIndex();
			if (out.getItem() instanceof ItemModule) {
				ItemModuleInformationManager.saveInfotmation(out, logisticsModule.getSubModule(moduleIndex));
				if (logisticsModule.hasModule(moduleIndex)) {
					logisticsModule.removeModule(moduleIndex);
				}
			}
		}
	}

	private boolean areEqualForMerge(ItemStack itemstack3, ItemStack itemstack4, Slot slot) {
		if(slot instanceof ModuleSlot) {
			return false;
		}
		return ItemStack.areItemStackTagsEqual(itemstack3, itemstack4);
	}

	/**
	 * Clone/clear itemstacks for items
	 */
	@Override
	public ItemStack slotClick(int slotId, int mouseButton, int isShift, EntityPlayer entityplayer) {
		lastClicked = System.currentTimeMillis();
		if (slotId < 0) {
			return superSlotClick(slotId, mouseButton, isShift, entityplayer);
		}
		Slot slot = (Slot) inventorySlots.get(slotId);
		//debug dump
		if (LPConstants.DEBUG && slot != null) {
			ItemStack stack = slot.getStack();
			if (stack != null) {
				ItemIdentifier.get(stack).debugDumpData(entityplayer.worldObj.isRemote);
			}
		}
		if (slot == null || (!(slot instanceof DummySlot) && !(slot instanceof UnmodifiableSlot) && !(slot instanceof FluidSlot) && !(slot instanceof ColorSlot) && !(slot instanceof HandelableSlot))) {
			ItemStack stack1 = superSlotClick(slotId, mouseButton, isShift, entityplayer);
			ItemStack stack2 = slot.getStack();
			if (stack2 != null && stack2.getItem() == LogisticsPipes.ModuleItem) {
				if (entityplayer instanceof EntityPlayerMP && MainProxy.isServer(entityplayer.worldObj)) {
					((EntityPlayerMP) entityplayer).sendSlotContents(this, slotId, stack2);
				}
			}
			return stack1;
		}

		InventoryPlayer inventoryplayer = entityplayer.inventory;

		ItemStack currentlyEquippedStack = inventoryplayer.getItemStack();

		// we get a leftclick *and* a doubleclick message if there's a doubleclick with no item on the pointer, filter it out
		if (currentlyEquippedStack == null && isShift == 6) {
			return currentlyEquippedStack;
		}

		if (slot instanceof HandelableSlot) {
			overrideMCAntiSend = true;
			if (currentlyEquippedStack == null) {
				inventoryplayer.setItemStack(((HandelableSlot) slot).getProvidedStack());
				return null;
			}
			return currentlyEquippedStack;
		}

		if (slot instanceof UnmodifiableSlot) {
			return currentlyEquippedStack;
		}

		handleDummyClick(slot, slotId, currentlyEquippedStack, mouseButton, isShift, entityplayer);
		return currentlyEquippedStack;
	}

	public void handleDummyClick(Slot slot, int slotId, ItemStack currentlyEquippedStack, int mouseButton, int isShift, EntityPlayer entityplayer) {
		if (slot instanceof FluidSlot) {
			if (currentlyEquippedStack != null) {
				FluidStack liquidId = FluidContainerRegistry.getFluidForFilledItem(currentlyEquippedStack);
				if (liquidId != null) {
					FluidIdentifier ident = FluidIdentifier.get(liquidId);
					if (mouseButton == 0) {
						if (ident == null) {
							slot.putStack(null);
						} else {
							slot.putStack(ident.getItemIdentifier().unsafeMakeNormalStack(1));
						}
					} else {
						slot.putStack(null);
					}
					return;
				}
				FluidIdentifier ident = FluidIdentifier.get(currentlyEquippedStack);
				if (ident != null) {
					if (mouseButton == 0) {
						slot.putStack(ident.getItemIdentifier().unsafeMakeNormalStack(1));
					} else {
						slot.putStack(null);
					}
					return;
				}
			}
			FluidIdentifier ident = null;
			if (slot.getStack() != null) {
				ident = FluidIdentifier.get(ItemIdentifier.get(slot.getStack()));
			}
			if (ident == null) {
				if (MainProxy.isClient(entityplayer.getEntityWorld())) {
					MainProxy.proxy.openFluidSelectGui(slotId);
				}
			}
			slot.putStack(null);
			return;
		}

		if (slot instanceof ColorSlot) {
			MinecraftColor equipped = MinecraftColor.getColor(currentlyEquippedStack);
			MinecraftColor color = MinecraftColor.getColor(slot.getStack());
			if (MinecraftColor.BLANK.equals(equipped)) {
				if (mouseButton == 0) {
					color = color.getNext();
				} else if (mouseButton == 1) {
					color = color.getPrev();
				} else {
					color = MinecraftColor.BLANK;
				}
				slot.putStack(color.getItemStack());
			} else {
				if (mouseButton == 1) {
					slot.putStack(MinecraftColor.BLANK.getItemStack());
				} else {
					slot.putStack(equipped.getItemStack());
				}
			}
			if (entityplayer instanceof EntityPlayerMP && MainProxy.isServer(entityplayer.worldObj)) {
				((EntityPlayerMP) entityplayer).sendSlotContents(this, slotId, slot.getStack());
			}
			return;
		}

		if (slot instanceof DummySlot) {
			((DummySlot) slot).setRedirectCall(true);
		}

		if (currentlyEquippedStack == null) {
			if (slot.getStack() != null && mouseButton == 1) {
				ItemStack tstack = slot.getStack();
				if (isShift == 1) {
					tstack.stackSize = Math.min(slot.getSlotStackLimit(), tstack.stackSize * 2);
				} else {
					tstack.stackSize /= 2;
					if (tstack.stackSize <= 0) {
						tstack = null;
					}
				}
				slot.putStack(tstack);
			} else {
				slot.putStack(null);
			}
			if (slot instanceof DummySlot) {
				((DummySlot) slot).setRedirectCall(false);
			}
			return;
		}

		if (!slot.getHasStack()) {
			ItemStack tstack = currentlyEquippedStack.copy();
			if (mouseButton == 1) {
				tstack.stackSize = 1;
			}
			if (tstack.stackSize > slot.getSlotStackLimit()) {
				tstack.stackSize = slot.getSlotStackLimit();
			}
			slot.putStack(tstack);
			if (slot instanceof DummySlot) {
				((DummySlot) slot).setRedirectCall(false);
			}
			return;
		}

		ItemIdentifier currentItem = ItemIdentifier.get(currentlyEquippedStack);
		ItemIdentifier slotItem = ItemIdentifier.get(slot.getStack());
		if (currentItem.equals(slotItem)) {
			ItemStack tstack = slot.getStack();
			// Do manual shift-checking to play nice with NEI
			int counter = isShift == 1 ? 10 : 1;
			if (mouseButton == 1) {
				if (tstack.stackSize + counter <= slot.getSlotStackLimit()) {
					tstack.stackSize += counter;
				} else {
					tstack.stackSize = slot.getSlotStackLimit();
				}
				slot.putStack(tstack);
			} else if (mouseButton == 0) {
				tstack.stackSize -= counter;
				if (tstack.stackSize <= 0) {
					tstack = null;
				}
				slot.putStack(tstack);
			}
			if (slot instanceof DummySlot) {
				((DummySlot) slot).setRedirectCall(false);
			}
			return;
		}

		ItemStack tstack = currentlyEquippedStack.copy();
		if (tstack.stackSize > slot.getSlotStackLimit()) {
			tstack.stackSize = slot.getSlotStackLimit();
		}
		slot.putStack(tstack);
		if (slot instanceof DummySlot) {
			((DummySlot) slot).setRedirectCall(false);
		}
		return;
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		if (_controler != null) {
			for (IGuiOpenControler element : _controler) {
				element.guiClosedByPlayer(par1EntityPlayer);
			}
		}
		super.onContainerClosed(par1EntityPlayer);
	}

	@Override
	protected void retrySlotClick(int i, int j, boolean flag, EntityPlayer entityplayer) {
		Thread.dumpStack();
	}

	public void addRestrictedHotbarForPlayerInventory(int xOffset, int yOffset) {
		if (_playerInventory == null) {
			return;
		}
		// Player "hotbar"
		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new UnmodifiableSlot(_playerInventory, i1, xOffset + i1 * 18, yOffset));
		}
	}

	public void addRestrictedArmorForPlayerInventory(int xOffset, int yOffset) {
		if (_playerInventory == null) {
			return;
		}
		for (int i1 = 0; i1 < 4; i1++) {
			addSlotToContainer(new UnmodifiableSlot(_playerInventory, i1 + 36, xOffset, yOffset - i1 * 18));
		}
	}

	@Override
	public boolean canDragIntoSlot(Slot slot) {
		if (slot == null || slot instanceof UnmodifiableSlot || slot instanceof FluidSlot || slot instanceof ColorSlot || slot instanceof HandelableSlot) {
			return false;
		}
		if (lastDragnDropLockup <= lastClicked) { // Slot was clicked after last lookup
			lastDragnDropLockup = System.currentTimeMillis();
			if (slot instanceof DummySlot) {
				wasDummyLookup = true;
				return true;
			}
			wasDummyLookup = false;
			return true;
		} else { // Still lookingUp (during drag'n'drop)
			lastDragnDropLockup = System.currentTimeMillis();
			if (slot instanceof DummySlot) {
				return wasDummyLookup;
			}
			return !wasDummyLookup;
		}
	}

	// Hacky overrides to handle client/server player inv sync with 0-slot containers
	@Override
	public Slot getSlotFromInventory(IInventory par1IInventory, int par2) {
		Slot s = super.getSlotFromInventory(par1IInventory, par2);
		if (s != null) {
			return s;
		}
		if (inventorySlots.isEmpty() && par1IInventory == _playerInventory) {
			s = new Slot(_playerInventory, par2, 0, 0);
			s.slotNumber = par2;
			return s;
		}
		return null;
	}

	@Override
	public void putStackInSlot(int par1, ItemStack par2ItemStack) {
		if (inventorySlots.isEmpty()) {
			_playerInventory.setInventorySlotContents(par1, par2ItemStack);
			_playerInventory.markDirty();
			return;
		}
		super.putStackInSlot(par1, par2ItemStack);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void detectAndSendChanges() {
		for (int i = 0; i < inventorySlots.size(); ++i) {
			if(inventorySlots.get(i) instanceof IFuzzySlot) {
				IFuzzySlot fuzzySlot = (IFuzzySlot) inventorySlots.get(i);
				BitSet set = inventoryFuzzySlotsContent.get(i);
				if(set == null) {
					set = fuzzySlot.getFuzzyFlags().getBitSet();
					MainProxy.sendToPlayerList(PacketHandler.getPacket(FuzzySlotSettingsPacket.class).setSlotNumber(fuzzySlot.getSlotId()).setFlags(set), crafters);
					inventoryFuzzySlotsContent.set(i, set);
				} else {
					BitSet setB = fuzzySlot.getFuzzyFlags().getBitSet();
					if(!set.equals(setB)) {
						MainProxy.sendToPlayerList(PacketHandler.getPacket(FuzzySlotSettingsPacket.class).setSlotNumber(fuzzySlot.getSlotId()).setFlags(setB), crafters);
						inventoryFuzzySlotsContent.set(i, setB);
					}
				}
			}
			ItemStack itemstack = ((Slot) inventorySlots.get(i)).getStack();
			ItemStack itemstack1 = (ItemStack) inventoryItemStacks.get(i);

			if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
				itemstack1 = itemstack == null ? null : itemstack.copy();
				inventoryItemStacks.set(i, itemstack1);

				for (int j = 0; j < crafters.size(); ++j) {
					boolean revert = false;
					if (overrideMCAntiSend && crafters.get(j) instanceof EntityPlayerMP && ((EntityPlayerMP) crafters.get(j)).isChangingQuantityOnly) {
						((EntityPlayerMP) crafters.get(j)).isChangingQuantityOnly = false;
						revert = true;
					}
					((ICrafting) crafters.get(j)).sendSlotContents(this, i, itemstack1);
					if (revert) {
						((EntityPlayerMP) crafters.get(j)).isChangingQuantityOnly = true;
					}
				}
			}
		}
		overrideMCAntiSend = false;
	}

	@Override
	protected Slot addSlotToContainer(Slot p_75146_1_) {
		this.inventoryFuzzySlotsContent.add(null);
		return super.addSlotToContainer(p_75146_1_);
	}
}
