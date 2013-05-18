package logisticspipes.pipes.basic;

import net.minecraft.crash.CrashReportCategory;
import buildcraft.transport.TileGenericPipe;

public class LogisticsTileGenericPipe extends TileGenericPipe {

	public void queueEvent(String event, Object[] arguments) {}
	public void setTurtrleConnect(boolean flag) {}
	public boolean getTurtrleConnect() {return false;}
	public int getLastCCID() {return -1;}
	
	protected CoreRoutedPipe getCPipe() {
		if(pipe instanceof CoreRoutedPipe) {
			return (CoreRoutedPipe) pipe;
		}
		return null;
	}

	@Override
	public void invalidate() {
		if(!getCPipe().blockRemove()) {
			super.invalidate();
		}
	}
	@Override
	public void func_85027_a(CrashReportCategory par1CrashReportCategory) {
		super.func_85027_a(par1CrashReportCategory);
		if(this.pipe != null) {
			par1CrashReportCategory.addCrashSection("Pipe", this.pipe.getClass().getCanonicalName());
			if(this.pipe.transport != null) {
				par1CrashReportCategory.addCrashSection("Transport", this.pipe.transport.getClass().getCanonicalName());
			} else {
				par1CrashReportCategory.addCrashSection("Transport", "null");
			}
			if(this.pipe.logic != null) {
				par1CrashReportCategory.addCrashSection("Logic", this.pipe.logic.getClass().getCanonicalName());
			} else {
				par1CrashReportCategory.addCrashSection("Logic", "null");
			}
			if(this.pipe instanceof CoreRoutedPipe) {
				try {
					((CoreRoutedPipe)this.pipe).addCrashReport(par1CrashReportCategory);
				} catch(Exception e) {
					par1CrashReportCategory.addCrashSectionThrowable("Internal LogisticsPipes Error", e);
				}
			}
		}
	}
}
