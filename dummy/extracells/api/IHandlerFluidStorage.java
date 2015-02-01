package extracells.api;

public interface IHandlerFluidStorage {

	boolean isFormatted();

	int usedBytes();

	int totalBytes();

	int usedTypes();

	int totalTypes();

}
