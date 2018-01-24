

import java.awt.Color;
import java.awt.Graphics;

public class DOval extends DShape {

	private DOvalModel model;

	public DOval(DOvalModel model, Canvas c) {
		super(model, c);
		this.model = model;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(model.getColor());
		g.fillOval((int)model.getCoordinates().getX(), (int)model.getCoordinates().getY(), (int)model.getDimensions().getX(),
				(int)model.getDimensions().getY());
	}

	@Override
	public DShapeModel getModel() {
		return model;
	}
}
