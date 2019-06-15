package logisticspipes.gui.guidebook.book;

import logisticspipes.utils.string.StringUtils;

public class Page {

	public int pageNumber;
	private String PREFIX;
	public String text;

	public Page(int index, String parentName){
		this.pageNumber = index;
		this.PREFIX = parentName + pageNumber;
		this.text = StringUtils.translate(PREFIX);
	}
}
