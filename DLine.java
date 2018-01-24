

import java.awt.Graphics;
import java.awt.Point;

public class DLine extends DShape{

	DLineModel model;
	
	public DLine(DLineModel model, Canvas c) {
		super(model, c);
		this.model = model;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(model.getColor());
		if(!model.isFlipped()){
			g.drawLine((int)model.getStartCoordinates().getX(), (int)model.getStartCoordinates().getY(), (int)model.getEndCoordinates().getX(), (int)model.getEndCoordinates().getY());
		}else{
			g.drawLine((int)model.getStartCoordinates().getX(), (int)model.getEndCoordinates().getY(), (int)model.getEndCoordinates().getX(), (int)model.getStartCoordinates().getY());
		}
	}

	@Override
	public DShapeModel getModel() {
		return model;
	}
	
	@Override
	public Point[] getKnobs(){
		if(!model.isFlipped()){
			return new Point[]{new Point((int)model.getStartCoordinates().getX(), (int)model.getStartCoordinates().getY()), new Point((int)model.getEndCoordinates().getX(), (int)model.getEndCoordinates().getY())};
		}else{
			return new Point[]{new Point((int)model.getStartCoordinates().getX(), (int)model.getEndCoordinates().getY()), new Point((int)model.getEndCoordinates().getX(), (int)model.getStartCoordinates().getY())};
		}
	}

}
