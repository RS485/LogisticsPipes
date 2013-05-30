package dan200.computer.core;

public abstract interface ILuaObject {
  public abstract String[] getMethodNames();

  public abstract Object[] callMethod(int paramInt, Object[] paramArrayOfObject)
    throws Exception;
}