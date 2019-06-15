package logisticspipes.gui.guidebook.book;

import java.util.ArrayList;

import lombok.Getter;

import logisticspipes.utils.string.StringUtils;

public class GuideBook {

	@Getter
	private String title;

	private String name;
	public ArrayList<MenuItem> menuItems;
	public ArrayList<String> menuItemsType;

	public GuideBook(String name) {
		this.name = name + ".";
		this.title = StringUtils.translate(this.name + "title");
		this.menuItems = new ArrayList<>();
		this.menuItemsType = new ArrayList<>();
	}

	public void loadBook() {
		String str = StringUtils.translate(name + "menu");
		String[] menu = str.split(",");
		for (int index = 0; index < menu.length; index++) {
			switch (StringUtils.translate(name + menu[index])) {
				case "item":
					menuItems.add(new MenuItem(menu[index], name, index));
					menuItemsType.add("item");
					break;
				case "text":
					menuItems.add(new MenuItemText(menu[index], name, index));
					menuItemsType.add("text");
					break;
				default:
					menuItems.add(new MenuItem(menu[index], name, index));
					break;
			}
		}
		for (MenuItem item : menuItems){
		item.loadMenuItem();
		}
	}
}
