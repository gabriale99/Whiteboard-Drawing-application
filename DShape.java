

import java.awt.Graphics;
import java.awt.Point;
import java.io.Serializable;

public abstract class DShape implements ModelListener, Serializable{

	private transient Canvas canvas;
	private DShapeModel model;
	
	public DShape(DShapeModel model, Canvas c){
		this.model = model;
		model.addListener(this);
		canvas = c;
	}
	public abstract void draw(Graphics g);
	
	public abstract DShapeModel getModel();
	
	public void modelChanged(DShapeModel model){
		canvas.paintComponent();
	}

	public Canvas getCanvas(){
		return canvas;
	}
	
	public Point[] getKnobs(){
		Point c = model.getCoordinates();
		Point d = model.getDimensions();
		return new Point[]{new Point((int)c.getX(), (int)c.getY()), new Point((int)(c.getX() + d.getX()), (int)c.getY()), new Point((int)(c.getX() + d.getX()), (int)(c.getY() + d.getY())), new Point((int)c.getX(), (int)(c.getY() + d.getY()))};
	}
}
