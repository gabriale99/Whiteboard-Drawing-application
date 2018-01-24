

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class DShapeModel implements Serializable{
	
	public static int numCreated = 0;
	private Point coordinates = new Point();
	private Point dimensions = new Point();
	private transient ArrayList<ModelListener> listeners = new ArrayList<ModelListener>();
	private Color color;
	private int ID;
	
	public DShapeModel() {
		color = Color.BLACK;
		listeners = new ArrayList<ModelListener>();
		//ID = numCreated++;
	};
	
	public DShapeModel(int x, int y, int width, int height, Color color){
		setCoordinates(x, y);
		setDimensions(width, height);
		setColor(color);
		ID = numCreated++;
	}
	
	public Point getCoordinates(){
		return coordinates;
	}
	
	public void setCoordinates(int x, int y){
		/*coordinates[0] = x;
		coordinates[1] = y;*/
		setCoordinates(new Point(x, y));
		notifyListeners();
	}
	
	public void setCoordinates(Point newCoords) {
		coordinates = newCoords;
	}
	
	public Point getDimensions(){
		return dimensions;
	}
	
	public void setDimensions(int width, int height){
		dimensions.setLocation(Math.abs(width), Math.abs(height));
		setDimensions(new Point(dimensions));
		notifyListeners();
	}
	
	public void setDimensions(Point newDim) {
		dimensions = newDim;
	}
	
	public Color getColor(){
		return color;
	}
	
	public void setColor(Color c){
		color = c;
		notifyListeners();
	}
	
	public void notifyListeners(){
		for(ModelListener l : listeners){
			l.modelChanged(this);
		}
	}
	
	public void addListener(ModelListener ml){
		if(listeners == null) {
			listeners = new ArrayList<ModelListener>();
		}
		listeners.add(ml);
	}
	
	public void removeListener(ModelListener ml){
		listeners.remove(ml);
	}
	
	public int getID() {
		return ID;
	}

	public void mimic(DShapeModel model) {
		setCoordinates(model.getCoordinates());
		setDimensions(model.getDimensions());
		setColor(model.getColor());
	}
	
	public String toString() {
		return getClass() + " @ " + coordinates + " size: " + dimensions + "ID: " + ID;
	}

}
