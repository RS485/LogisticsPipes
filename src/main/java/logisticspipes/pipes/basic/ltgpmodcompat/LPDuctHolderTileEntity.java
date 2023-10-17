package logisticspipes.pipes.basic.ltgpmodcompat;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;

import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.DuctUnit;
import cofh.thermaldynamics.duct.tiles.IDuctHolder;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import logisticspipes.LPConstants;
import logisticspipes.asm.ModDependentInterface;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.proxy.td.subproxies.ITDPart;

@ModDependentInterface(modId = { LPConstants.thermalDynamicsModID }, interfacePath = { "cofh.thermaldynamics.duct.tiles.IDuctHolder" })
public abstract class LPDuctHolderTileEntity extends LPMicroblockTileEntity implements IDuctHolder {

	public ITDPart tdPart;

	@Nullable
	@Override
	@ModDependentMethod(modId = LPConstants.thermalDynamicsModID)
	public <T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> T getDuct(DuctToken<T, G, C> ductToken) {
		return ((IDuctHolder) tdPart.getInternalDuct()).getDuct(ductToken);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.thermalDynamicsModID)
	public boolean isSideBlocked(int i) {
		return tdPart.isLPSideBlocked(i);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.thermalDynamicsModID)
	public void setPos(BlockPos pos) {
		super.setPos(pos);
		tdPart.setPos(pos);
	}
}
