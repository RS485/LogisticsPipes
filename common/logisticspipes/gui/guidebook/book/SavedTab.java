package logisticspipes.gui.guidebook.book;

import net.minecraft.nbt.NBTTagCompound;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.utils.GuideBookContents;
import logisticspipes.gui.guidebook.GuiGuideBook;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class SavedTab {

	private GuideBookContents gbc;
	public GuideBookContents.Page page;
	public IDrawable drawable;
	@Getter
	@Setter
	private float progress;
	public int color;

	/* Page getters */
	public int getPage() {
		return page.getIndex();
	}

	public int getChapter() {
		return page.getCindex();
	}

	public int getDivision() {
		return page.getDindex();
	}

	public int getPageCount() {
		return gbc.getDivision(page.getDindex()).getChapter(page.getCindex()).getNPages();
	}

	/* Page Setters */
	public void setPage(MenuItem item) {
		setPage(item.getChapter().getDindex(), item.getChapter().getCindex(), 0, 0.0F);
	}

	public void setPage(int dindex, int cindex, int index, float progress) {
		this.page = gbc.getDivision(dindex).getChapter(cindex).getPage(index);
		this.progress = progress;
	}

	public void nextPage() {
		page = gbc.getDivision(page.getDindex()).getChapter(page.getCindex()).getPage(page.getIndex() + 1);
	}

	public void prevPage() {
		page = gbc.getDivision(page.getDindex()).getChapter(page.getCindex()).getPage(page.getIndex() - 1);
	}

	public SavedTab(GuideBookContents.Page page, IDrawable drawable) {
		this.gbc = GuiGuideBook.gbc;
		this.page = page;
		this.drawable = drawable;
		this.progress = 0.0F;
	}

	public SavedTab(GuideBookContents.Page page, IDrawable drawable, int colorIndex, float progress) {
		this.gbc = GuiGuideBook.gbc;
		this.page = page;
		this.drawable = drawable;
		this.progress = progress;
		this.color = colorIndex;
	}

	public SavedTab() {
		this.gbc = GuiGuideBook.gbc;
		this.page = new GuideBookContents.Page(0, 0, 0, "");
		this.drawable = GuiGuideBook.menu;
	}

	public SavedTab(SavedTab tab) {
		this.gbc = GuiGuideBook.gbc;
		this.page = tab.page;
		this.drawable = tab.drawable;
		this.progress = tab.progress;
	}

	public SavedTab fromBytes(LPDataInput input) {
		return new SavedTab(
				gbc.getDivision(input.readInt())
						.getChapter(input.readInt())
						.getPage(input.readInt()),
				GuiGuideBook.page,
				input.readInt(),
				input.readFloat());
	}

	public void toBytes(LPDataOutput output) {
		output.writeInt(page.getDindex());
		output.writeInt(page.getCindex());
		output.writeInt(page.getIndex());
		output.writeInt(color);
		output.writeFloat(progress);
	}

	public SavedTab fromTag(NBTTagCompound nbt) {
		return new SavedTab(
				gbc.getDivision(nbt.getInteger("divisionIndex"))
						.getChapter(nbt.getInteger("chapterIndex"))
						.getPage(nbt.getInteger("pageIndex")),
				GuiGuideBook.page,
				nbt.getInteger("color"),
				nbt.getFloat("progress"));
	}

	public NBTTagCompound toTag() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("divisionIndex", page.getDindex());
		nbt.setInteger("chapterIndex", page.getCindex());
		nbt.setInteger("pageIndex", page.getIndex());
		nbt.setInteger("color", color);
		nbt.setFloat("progress", progress);
		return nbt;
	}
}