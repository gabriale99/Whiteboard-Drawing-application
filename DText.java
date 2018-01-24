

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JTextField;

public class DText extends DShape {

	DTextModel model;

	public DText(DTextModel model, Canvas c) {
		super(model, c);
		this.model = model;
	}

	@Override
	public void draw(Graphics g) {
		if (model.needsToRecomputeSize()) {
			Font f = model.getFont(true);
			FontMetrics metrics = g.getFontMetrics(f);
			int maxLoopCounter = 1000;
			while (metrics.getHeight() < model.getDimensions().getY() && maxLoopCounter > 0) {
				model.increaseFontSize();
				f = model.getFont(false);
				metrics = g.getFontMetrics(f);
				maxLoopCounter--;
			}
			model.decreaseFontSize();
		}
		Shape clip = g.getClip();
		g.setColor(model.getColor());
		g.setFont(model.getFont(false));
		g.setClip(new Rectangle((int)model.getCoordinates().getX(), (int)model.getCoordinates().getY(), (int)model.getDimensions().getX(),
				(int)model.getDimensions().getY()));
		g.drawString(model.getText(), (int)model.getCoordinates().getX(), (int)(model.getCoordinates().getY() + (model.getDimensions().getY()*2)/3));
		g.setClip(clip);
	}

	@Override
	public DShapeModel getModel() {
		return model;
	}

}
