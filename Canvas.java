

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Canvas extends JPanel {

	private ArrayList<DShape> shapes = new ArrayList<DShape>();
	private DShape selected;
	private double selectedXOffset;
	private double selectedYOffset;
	private Color colour = Color.GRAY;
	public static final int KNOB_SIZE = 9;
	private boolean resizing;
	private double anchorX, anchorY;
	private ArrayList<CanvasSelectionListener> selectionListeners = new ArrayList<CanvasSelectionListener>();
	private ArrayList<CanvasListListener> listListeners = new ArrayList<CanvasListListener>();
	private BufferedImage image;
	private boolean editable = true;

	public Canvas() {
		this.setPreferredSize(new Dimension(400, 400));
		this.setBackground(Color.WHITE);
		this.addMouseListener(new MouseListener() {

			int mouseStartX = 0;
			int mouseStartY = 0;

			@Override
			public void mouseClicked(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {
				mouseStartX = e.getX();
				mouseStartY = e.getY();
				int i;
				resizing = false;
				for (i = shapes.size() - 1; i >= 0; i--) {
					DShapeModel model = shapes.get(i).getModel();
					if (mouseCoordinatesWithinShape(mouseStartX, mouseStartY, model)) {
						selected = shapes.get(i);
						break;
					}
				}
				if (i == -1) {
					if (selected != null) {
						selectedXOffset = selected.getModel().getCoordinates().getX() - mouseStartX;
						selectedYOffset = selected.getModel().getCoordinates().getY() - mouseStartY;
						if (mouseCoordinatesOnKnob(mouseStartX, mouseStartY, selected.getModel())) {
							if (-selectedXOffset > selected.getModel().getDimensions().getX() / 2) {
								anchorX = selected.getModel().getCoordinates().getX();
							} else {
								anchorX = selected.getModel().getCoordinates().getX()
										+ selected.getModel().getDimensions().getX();
							}
							if (-selectedYOffset > selected.getModel().getDimensions().getY() / 2) {
								anchorY = selected.getModel().getCoordinates().getY();
							} else {
								anchorY = selected.getModel().getCoordinates().getY()
										+ selected.getModel().getDimensions().getY();
							}
							resizing = true;
						} else {
							selected = null;
						}
					} else {
						selected = null;
					}
				} else {
					selectedXOffset = selected.getModel().getCoordinates().getX() - mouseStartX;
					selectedYOffset = selected.getModel().getCoordinates().getY() - mouseStartY;
					if (mouseCoordinatesOnKnob(mouseStartX, mouseStartY, selected.getModel())) {
						if (-selectedXOffset > selected.getModel().getDimensions().getX() / 2) {
							anchorX = selected.getModel().getCoordinates().getX();
						} else {
							anchorX = selected.getModel().getCoordinates().getX() + selected.getModel().getDimensions().getX();
						}
						if (-selectedYOffset > selected.getModel().getDimensions().getY() / 2) {
							anchorY = selected.getModel().getCoordinates().getY();
						} else {
							anchorY = selected.getModel().getCoordinates().getY() + selected.getModel().getDimensions().getY();
						}
						resizing = true;
					}
				}
				notifySelectionListeners();
				paintComponent();
			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

		});
		this.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (editable && selected != null) {
					if (resizing) {
						selected.getModel().setCoordinates((int)Math.min(anchorX, e.getX()), (int)Math.min(anchorY, e.getY()));
						selected.getModel().setDimensions((int)(e.getX() - anchorX), (int)(e.getY() - anchorY));
					} else {
						selected.getModel().setCoordinates((int)(e.getX() + selectedXOffset), (int)(e.getY() + selectedYOffset));
					}
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {

			}

		});
	}

	private boolean mouseCoordinatesWithinShape(int x, int y, DShapeModel model) {
		return x >= model.getCoordinates().getX() && y >= model.getCoordinates().getY()
				&& x <= model.getCoordinates().getX() + model.getDimensions().getX()
				&& y <= model.getCoordinates().getY() + model.getDimensions().getY();
	}

	private boolean mouseCoordinatesOnKnob(int x, int y, DShapeModel model) {
		double modelX1 = model.getCoordinates().getX(), modelY1 = model.getCoordinates().getY(),
				modelX2 = model.getDimensions().getX() + modelX1, modelY2 = model.getDimensions().getY() + modelY1;
		boolean xCheck = Math.abs(x - modelX1) <= KNOB_SIZE / 2 || Math.abs(x - modelX2) <= KNOB_SIZE / 2;
		boolean yCheck = Math.abs(y - modelY1) <= KNOB_SIZE / 2 || Math.abs(y - modelY2) <= KNOB_SIZE / 2;
		return xCheck && yCheck;
	}

	public void paintComponent() {
		super.paintComponent(getGraphics());
		image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		removeAll();
		for (DShape shape : shapes) {
			// shape.draw(getGraphics());
			shape.draw(image.getGraphics());
		}
		getGraphics().drawImage(image, 0, 0, null);
		if (selected != null) {
			Point[] knobs = selected.getKnobs();
			for (Point p : knobs) {
				getGraphics().fillRect(Math.max((int) p.getX() - KNOB_SIZE / 2, 0),
						Math.max((int) p.getY() - KNOB_SIZE / 2, 0), KNOB_SIZE, KNOB_SIZE);
			}
		}
	}

	public void addShape(DShapeModel model) {
		if (editable) {
			if (model instanceof DRectModel) {
				shapes.add(new DRect((DRectModel) model, this));
			} else if (model instanceof DOvalModel) {
				shapes.add(new DOval((DOvalModel) model, this));
			} else if (model instanceof DLineModel) {
				shapes.add(new DLine((DLineModel) model, this));
			} else if (model instanceof DTextModel) {
				shapes.add(new DText((DTextModel) model, this));
			}
			notifyListListenersOfAdd(shapes.get(shapes.size() - 1));
		}
	}

	public void addShapeClient(DShapeModel model) {
		if (model instanceof DRectModel) {
			shapes.add(new DRect((DRectModel) model, this));
		} else if (model instanceof DOvalModel) {
			shapes.add(new DOval((DOvalModel) model, this));
		} else if (model instanceof DLineModel) {
			shapes.add(new DLine((DLineModel) model, this));
		} else if (model instanceof DTextModel) {
			shapes.add(new DText((DTextModel) model, this));
		}
		notifyListListenersOfAdd(shapes.get(shapes.size() - 1));
	}
	
	public Color getColour() {
		return colour;
	}

	public void setColour(Color colour) {
		if (editable) {
			this.colour = colour;
			if (selected != null) {
				selected.getModel().setColor(colour);
			}
		}
	}

	public void deleteShape() {
		if (editable && selected != null) {
			int index;
			for (index = 0; !shapes.get(index).equals(selected); index++)
				;
			shapes.remove(index);
			notifyListListenersOfRemoval(selected, index);
			selected = null;
			paintComponent();
			notifySelectionListeners();
		}
	}

	public void removeShape(DShapeModel model) {
		for(int i = 0; i < shapes.size(); i++) {
			if(shapes.get(i).getModel().getID() == model.getID()) {
				shapes.remove(i);
				notifyListListenersOfRemoval(selected, i);
				selected = null;
				paintComponent();
				notifySelectionListeners();
			}
		}
	}
	
	public void moveForward() {
		if (editable) {
			int index = shapes.indexOf(selected);
			if (index < shapes.size() - 1) {
				shapes.set(index, shapes.get(index + 1));
				shapes.set(index + 1, selected);
				shapes.get(index).getModel().notifyListeners();
				for(CanvasListListener l : listListeners) {
					l.onListForward(shapes.get(index + 1));
				}
				shapes.get(index + 1).getModel().notifyListeners();
			}
			paintComponent();
		}
	}

	public void moveForward(DShapeModel model) {
		for(int i = 0; i < shapes.size(); i++) {
			if(shapes.get(i).getModel().getID() == model.getID()) {
				int index = i;
				if (index < shapes.size() - 1) {
					selected = shapes.get(index);
					shapes.set(index, shapes.get(index + 1));
					shapes.set(index + 1, selected);
					shapes.get(index).getModel().notifyListeners();
					for(CanvasListListener l : listListeners) {
						l.onListForward(shapes.get(index + 1));
					}
					shapes.get(index + 1).getModel().notifyListeners();
					selected = null;
				}
				paintComponent();
				break;
			}
		}
	}
	
	public void moveBack() {
		if (editable) {
			int index = shapes.indexOf(selected);
			if (index > 0) {
				shapes.set(index, shapes.get(index - 1));
				shapes.set(index - 1, selected);
				shapes.get(index).getModel().notifyListeners();
				for(CanvasListListener l : listListeners) {
					l.onListBack(shapes.get(index-1));
				}
				shapes.get(index - 1).getModel().notifyListeners();
			}
			paintComponent();
		}
	}

	public void moveBack(DShapeModel model) {
		for(int i = 0; i < shapes.size(); i++) {
			if(shapes.get(i).getModel().getID() == model.getID()) {
				int index = i;
				if (index > 0) {
					selected = shapes.get(index);
					shapes.set(index, shapes.get(index - 1));
					shapes.set(index - 1, selected);
					shapes.get(index).getModel().notifyListeners();
					for(CanvasListListener l : listListeners) {
						l.onListBack(shapes.get(index-1));
					}
					shapes.get(index - 1).getModel().notifyListeners();
					selected = null;
				}
				paintComponent();
				break;
			}
		}
	}
	
	public DShape getSelected() {
		return selected;
	}

	public void notifySelectionListeners() {
		for (CanvasSelectionListener l : selectionListeners) {
			l.onSelectionChange(selected);
		}
	}

	public void addSelectionListener(CanvasSelectionListener l) {
		selectionListeners.add(l);
	}

	public void removeSelectionListener(CanvasSelectionListener l) {
		selectionListeners.remove(l);
	}

	public void notifyListListenersOfAdd(DShape shape) {
		for (CanvasListListener l : listListeners) {
			l.onListAdd(shape);
		}
	}

	public void notifyListListenersOfRemoval(DShape shape, int indexRemovedFrom) {
		for (CanvasListListener l : listListeners) {
			l.onListRemove(shape, indexRemovedFrom);
		}
	}

	public void addListListener(CanvasListListener l) {
		listListeners.add(l);
	}

	public void removeListListener(CanvasListListener l) {
		listListeners.remove(l);
	}

	public ArrayList<DShape> getShapesList() {
		return shapes;
	}

	public int indexOf(DShapeModel model) {
		int index;
		for (index = 0; index < shapes.size() && shapes.get(index).getModel() != model; index++)
			;
		return index;
	}

	public void saveImage(File file) {
		try {
			ImageIO.write(image, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveState(File file) {
		DShapeModel[] models = new DShapeModel[shapes.size()];
		for (int i = 0; i < models.length; i++) {
			models[i] = shapes.get(i).getModel();
		}
		XMLEncoder xml = null;
		try {
			xml = new XMLEncoder(new FileOutputStream(file));
			for (DShapeModel model : models) {
				xml.writeObject(model);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		xml.close();
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void modifyShape(DShapeModel model) {
		for(int i = 0; i < shapes.size(); i++) {
			if(shapes.get(i).getModel().getID() == model.getID()) {
				shapes.get(i).getModel().mimic(model);
			}
		}
		paintComponent();
	}
}
