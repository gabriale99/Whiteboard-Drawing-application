

import java.awt.Color;
import java.awt.Font;

public class DTextModel extends DShapeModel{

	private String text = "Hello";
	private String font = "Dialog";
	private double fontSize = 1;
	private boolean recomputeSize = true;
	
	public DTextModel() {}
	
	public DTextModel(int x, int y, int width, int height, Color color) {
		super(x, y, width, height, color);
	}

	@Override
	public void setDimensions(int width, int height){
		super.setDimensions(width, height);
		recomputeSize = true;
	}
	
	public boolean needsToRecomputeSize(){
		return recomputeSize;
	}
	
	public Font getFont(boolean reset){
		if(reset){
			fontSize = 1;
			recomputeSize = false;
		}
		return new Font(font, Font.PLAIN, (int)fontSize);
	}
	
	public String getFont() {
		return font;
	}
	public void setFont(String font){
		this.font = font;
	}
	
	public void increaseFontSize(){
		fontSize = fontSize * 1.1 + 1;
	}
	
	public void decreaseFontSize(){
		fontSize = (fontSize - 1)/1.1;
	}
	
	public String getText(){
		return text;
	}
	
	public void setText(String newText){
		text = newText;
		notifyListeners();
	}

	public double getFontSize() {
		return fontSize;
	}
	
	public void setFontSize(double newSize) {
		fontSize = newSize;
	}

	public void mimic(DShapeModel m) {
		super.mimic(m);
		if(m instanceof DTextModel) {
			DTextModel t = (DTextModel)m;
			setText(t.getText());
			setFont(t.getFont());
			setFontSize(t.getFontSize());
		}
	}
}
