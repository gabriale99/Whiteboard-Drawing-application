

import java.awt.Color;
import java.awt.Graphics;

public class DRect extends DShape {

	private DRectModel model;

	public DRect(DRectModel model, Canvas c) {
		super(model, c);
		this.model = model;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(model.getColor());
		g.fillRect((int)model.getCoordinates().getX(), (int)model.getCoordinates().getY(), (int)model.getDimensions().getX(),
				(int)model.getDimensions().getY());
	}

	@Override
	public DShapeModel getModel() {
		return model;
	}

}
