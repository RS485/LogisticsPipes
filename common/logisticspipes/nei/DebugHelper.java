package logisticspipes.nei;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;

import logisticspipes.config.Configs;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

import codechicken.nei.guihook.IContainerTooltipHandler;
import org.lwjgl.input.Keyboard;

public class DebugHelper implements IContainerTooltipHandler {

	private static long lastTime = 0;

	@Override
	public List<String> handleTooltip(GuiContainer gui, int mousex, int mousey, List<String> currenttip) {
		return currenttip;
	}

	@Override
	public List<String> handleItemDisplayName(GuiContainer paramGuiContainer, ItemStack paramItemStack, List<String> paramList) {
		return paramList;
	}

	@Override
	public List<String> handleItemTooltip(GuiContainer gui, final ItemStack itemstack, int paramInt1, int paramInt2, List<String> currenttip) {
		if (Configs.TOOLTIP_INFO && itemstack != null) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_H)) {
				if (DebugHelper.lastTime + 1000 < System.currentTimeMillis()) {
					DebugHelper.lastTime = System.currentTimeMillis();
					new Thread(new Runnable() {

						@Override
						public void run() {
							while (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_H)) {
								try {
									Thread.sleep(50);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							DefaultMutableTreeNode node = new DefaultMutableTreeNode(ItemIdentifier.get(itemstack).getFriendlyName());
							node.add(new DefaultMutableTreeNode("ItemId: " + Item.getIdFromItem(itemstack.getItem())));
							node.add(new DefaultMutableTreeNode("ItemDamage: " + itemstack.getItemDamage()));
							if (itemstack.hasTagCompound()) {
								DefaultMutableTreeNode tag = new DefaultMutableTreeNode("Tag:");
								try {
									addNBTToTree(itemstack.getTagCompound(), tag);
								} catch (Exception e) {
									tag.add(new DefaultMutableTreeNode(e));
								}
								node.add(tag);
							}
							JTree tree = new JTree(node);
							JScrollPane treeView = new JScrollPane(tree);
							JFrame frame = new JFrame("Item Info");
							frame.getContentPane().add(treeView, BorderLayout.CENTER);
							frame.setLocationRelativeTo(null);
							frame.pack();
							frame.setVisible(true);
						}
					}).start();
				}
			}
		}
		return currenttip;
	}

	@SuppressWarnings("rawtypes")
	private void addNBTToTree(NBTBase nbt, DefaultMutableTreeNode node) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if (nbt == null) {
			return;
		}
		if (nbt instanceof NBTTagByte) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagByte");
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagByte) nbt).func_150290_f()));
			node.add(type);
		} else if (nbt instanceof NBTTagByteArray) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagByteArray");
			DefaultMutableTreeNode content = new DefaultMutableTreeNode("Data");
			int i = 0;
			for (byte byt : ((NBTTagByteArray) nbt).func_150292_c()) {
				content.add(new DefaultMutableTreeNode("[" + i + "]: " + Byte.toString(byt)));
				i++;
			}
			node.add(content);
			node.add(type);
		} else if (nbt instanceof NBTTagDouble) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagDouble");
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagDouble) nbt).func_150286_g()));
			node.add(type);
		} else if (nbt instanceof NBTTagFloat) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagFloat");
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagFloat) nbt).func_150288_h()));
			node.add(type);
		} else if (nbt instanceof NBTTagInt) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagInt");
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagInt) nbt).func_150287_d()));
			node.add(type);
		} else if (nbt instanceof NBTTagIntArray) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagIntArray");
			DefaultMutableTreeNode content = new DefaultMutableTreeNode("Data");
			int i = 0;
			for (int byt : ((NBTTagIntArray) nbt).func_150302_c()) {
				content.add(new DefaultMutableTreeNode("[" + i + "]: " + byt));
				i++;
			}
			type.add(content);
			node.add(type);
		} else if (nbt instanceof NBTTagList) {
			List internal = ((NBTTagList) nbt).tagList;

			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagList");
			DefaultMutableTreeNode content = new DefaultMutableTreeNode("Data");

			int i = 0;

			for (Object object : internal) {
				if (object instanceof NBTBase) {
					DefaultMutableTreeNode nbtNode = new DefaultMutableTreeNode("[" + i + "]");
					addNBTToTree((NBTBase) object, nbtNode);
					content.add(nbtNode);
					i++;
				}
			}
			type.add(content);
			node.add(type);
		} else if (nbt instanceof NBTTagCompound) {
			Map internal = ((NBTTagCompound) nbt).tagMap;
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagCompound");
			DefaultMutableTreeNode content = new DefaultMutableTreeNode("Data");

			for (Object objectKey : internal.keySet()) {
				if (internal.get(objectKey) instanceof NBTBase) {
					DefaultMutableTreeNode nbtNode = new DefaultMutableTreeNode(objectKey);
					addNBTToTree((NBTBase) internal.get(objectKey), nbtNode);
					content.add(nbtNode);
				}
			}
			type.add(content);
			node.add(type);
		} else if (nbt instanceof NBTTagLong) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagLong");
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagLong) nbt).func_150291_c()));
			node.add(type);
		} else if (nbt instanceof NBTTagShort) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagShort");
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagShort) nbt).func_150289_e()));
			node.add(type);
		} else if (nbt instanceof NBTTagString) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagString");
			type.add(new DefaultMutableTreeNode("Data: '" + ((NBTTagString) nbt).func_150285_a_() + "'"));
			node.add(type);
		} else {
			throw new UnsupportedOperationException("Unsupported NBTBase of type:" + nbt.getClass().getName());
		}
	}
}
