package logisticspipes.nei;

import java.awt.BorderLayout;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import logisticspipes.config.Configs;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ObfuscationHelper;
import logisticspipes.utils.ObfuscationHelper.NAMES;
import net.minecraft.client.gui.inventory.GuiContainer;
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

import org.lwjgl.input.Keyboard;

import codechicken.nei.forge.IContainerTooltipHandler;

public class DebugHelper implements IContainerTooltipHandler {
	
	private static long lastTime = 0;
	
	@Override
	public List<String> handleTooltipFirst(GuiContainer gui, int mousex, int mousey, List<String> currenttip) {
		return currenttip;
	}

	@Override
	public List<String> handleItemTooltip(GuiContainer gui, final ItemStack itemstack, List<String> currenttip) {
		if(Configs.TOOLTIP_INFO && itemstack != null) {
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_H)) {
				if(lastTime + 1000 < System.currentTimeMillis()) {
					lastTime = System.currentTimeMillis();
					new Thread(new Runnable() {
						@Override
						public void run() {
							while(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_H)) {
								try {
									Thread.sleep(50);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							DefaultMutableTreeNode node = new DefaultMutableTreeNode(ItemIdentifier.get(itemstack).getFriendlyName());
							node.add(new DefaultMutableTreeNode("ItemId: " + itemstack.itemID));
							node.add(new DefaultMutableTreeNode("ItemId: " + itemstack.getItemDamage()));
							if(itemstack.hasTagCompound()) {
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
		if(nbt == null) {
			return;
		}
		if(nbt instanceof NBTTagByte) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagByte");
			type.add(new DefaultMutableTreeNode("Name: " + nbt.getName()));
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagByte)nbt).data));
			node.add(type);
		} else if(nbt instanceof NBTTagByteArray) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagByteArray");
			type.add(new DefaultMutableTreeNode("Name: " + nbt.getName()));
			DefaultMutableTreeNode content = new DefaultMutableTreeNode("Data");
			int i = 0;
			for(byte byt:((NBTTagByteArray)nbt).byteArray) {
				content.add(new DefaultMutableTreeNode("[" + i + "]: " + Byte.toString(byt)));
				i++;
			}
			node.add(content);
			node.add(type);
		} else if(nbt instanceof NBTTagDouble) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagDouble");
			type.add(new DefaultMutableTreeNode("Name: " + nbt.getName()));
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagDouble)nbt).data));
			node.add(type);
		} else if(nbt instanceof NBTTagFloat) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagFloat");
			type.add(new DefaultMutableTreeNode("Name: " + nbt.getName()));
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagFloat)nbt).data));
			node.add(type);
		} else if(nbt instanceof NBTTagInt) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagInt");
			type.add(new DefaultMutableTreeNode("Name: " + nbt.getName()));
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagInt)nbt).data));
			node.add(type);
		} else if(nbt instanceof NBTTagIntArray) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagIntArray");
			type.add(new DefaultMutableTreeNode("Name: " + nbt.getName()));
			DefaultMutableTreeNode content = new DefaultMutableTreeNode("Data");
			int i = 0;
			for(int byt:((NBTTagIntArray)nbt).intArray) {
				content.add(new DefaultMutableTreeNode("[" + i + "]: " + byt));
				i++;
			}
			type.add(content);
			node.add(type);
		} else if(nbt instanceof NBTTagList) {
			ArrayList internal = new ArrayList();
			Field fList = ObfuscationHelper.getDeclaredField(NAMES.tagList);
			fList.setAccessible(true);
			internal = (ArrayList) fList.get(nbt);
			
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagList");
			type.add(new DefaultMutableTreeNode("Name: " + nbt.getName()));
			DefaultMutableTreeNode content = new DefaultMutableTreeNode("Data");
			
			int i = 0;
			
			for(Object object:internal) {
				if(object instanceof NBTBase) {
					DefaultMutableTreeNode nbtNode = new DefaultMutableTreeNode("[" + i + "]");
					addNBTToTree((NBTBase)object, nbtNode);
					content.add(nbtNode);
					i++;
				}
			}
			type.add(content);
			node.add(type);
		} else if(nbt instanceof NBTTagCompound) {
			HashMap internal = new HashMap();
			Field fMap = ObfuscationHelper.getDeclaredField(NAMES.tagMap);
			fMap.setAccessible(true);
			internal = (HashMap) fMap.get(nbt);
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagCompound");
			type.add(new DefaultMutableTreeNode("Name: " + nbt.getName()));
			DefaultMutableTreeNode content = new DefaultMutableTreeNode("Data");
			
			for(Object objectKey:internal.keySet()) {
				if(internal.get(objectKey) instanceof NBTBase) {
					DefaultMutableTreeNode nbtNode = new DefaultMutableTreeNode(objectKey);
					addNBTToTree((NBTBase)internal.get(objectKey), nbtNode);
					content.add(nbtNode);
				}
			}
			type.add(content);
			node.add(type);
		} else if(nbt instanceof NBTTagLong) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagLong");
			type.add(new DefaultMutableTreeNode("Name: " + nbt.getName()));
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagLong)nbt).data));
			node.add(type);
		} else if(nbt instanceof NBTTagShort) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagShort");
			type.add(new DefaultMutableTreeNode("Name: " + nbt.getName()));
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagShort)nbt).data));
			node.add(type);
		} else if(nbt instanceof NBTTagString) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagString");
			type.add(new DefaultMutableTreeNode("Name: " + nbt.getName()));
			type.add(new DefaultMutableTreeNode("Data: '" + ((NBTTagString)nbt).data + "'"));
			node.add(type);
		} else {
			throw new UnsupportedOperationException("Unsupported NBTBase of type:" + nbt.getClass().getName());
		}
	}
}
