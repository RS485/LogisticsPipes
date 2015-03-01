package cofh.thermaldynamics.multiblock;

import gnu.trove.list.linked.TByteLinkedList;

public class Route {
	public TByteLinkedList	pathDirections	= new TByteLinkedList();
	public IMultiBlockRoute	endPoint;
	public IMultiBlockRoute	startPoint;
	  public int pathWeight = 0;
	public Route copy() {
		throw new UnsupportedOperationException();
	}

}
