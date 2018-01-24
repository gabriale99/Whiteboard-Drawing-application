

import java.awt.Color;
import java.awt.Point;

public class DLineModel extends DShapeModel{
	
	private Boolean flipped = false;
	
	public DLineModel() {}
	
	public DLineModel(int startX, int startY, int endX, int endY, Color color){
		super(startX, startY, endX, endY, color);
	}
	
	public Point getStartCoordinates(){
		return getCoordinates();
	}
	
	public Point getEndCoordinates(){
		return new Point((int)(getCoordinates().getX() + getDimensions().getX()), (int)(getCoordinates().getY() + getDimensions().getY()));
	}
	
	@Override
	public void setDimensions(int x, int y){
		boolean xFlip = x < 0;
		boolean yFlip = y < 0;
		flipped = (xFlip || yFlip) && !(xFlip && yFlip);
		super.setDimensions(x, y);
	}
	
	public void setFlipped(boolean newFlip) {
		flipped = newFlip;
	}
	
	public boolean isFlipped(){
		return flipped;
	}
	
	public boolean getFlipped() {
		return flipped;
	}
	
	public void mimic(DShapeModel m) {
		super.mimic(m);
		if(m instanceof DLineModel)
			setFlipped(((DLineModel)m).isFlipped());
	}
}
