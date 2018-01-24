

public interface CanvasListListener {

	public void onListAdd(DShape shapeAdded);
	
	public void onListRemove(DShape shapeRemoved, int indexRemovedFrom);

	public void onListForward(DShape shapeMoved);
	
	public void onListBack(DShape shapeMoved);
}
