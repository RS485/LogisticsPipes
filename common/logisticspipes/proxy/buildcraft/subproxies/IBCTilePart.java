package logisticspipes.proxy.buildcraft.subproxies;

import logisticspipes.asm.IgnoreDisabledProxy;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public interface IBCTilePart {
	void refreshRenderState();
	boolean addFacade(ForgeDirection direction, int type, int wire, Block[] blocks, int[] metaValues);
	boolean hasFacade(ForgeDirection direction);
	void dropFacadeItem(ForgeDirection direction);
	ItemStack getFacade(ForgeDirection direction);
	boolean dropFacade(ForgeDirection direction);
	boolean hasPlug(ForgeDirection side);
	boolean hasRobotStation(ForgeDirection side);
	boolean removeAndDropPlug(ForgeDirection side);
	boolean removeAndDropRobotStation(ForgeDirection side);
	boolean addPlug(ForgeDirection forgeDirection);
	boolean addRobotStation(ForgeDirection forgeDirection);
	@IgnoreDisabledProxy
	void writeToNBT(NBTTagCompound nbt);
	@IgnoreDisabledProxy
	void readFromNBT(NBTTagCompound nbt);
	void invalidate();
	void validate();
	/** BC6.1 */
	Object getPluggables(int i);
	/** BC6.1 */
	void updateEntity();
	/** BC6.1 */
	boolean hasGate(ForgeDirection side);
	/** BC6.1 */
	void setGate(Object makeGate, int i);
	/** BC6.1 */
	boolean hasEnabledFacade(ForgeDirection dir);
	/** BC6.1 */
	boolean dropSideItems(ForgeDirection sideHit);
	/** BC6.1 */
	boolean hasBlockingPluggable(ForgeDirection side);
	/** BC6.1 */
	Object getStation(ForgeDirection sideHit);
	/** BC6.1 */
	boolean addGate(ForgeDirection side, Object makeGate);
	/** BC6.1 */
	boolean addFacade(ForgeDirection direction, Object states);
}
