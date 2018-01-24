

import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColourChooserFrame extends JFrame{

	private JColorChooser colourChooser = new JColorChooser();
	private Canvas canvas;
	
	public ColourChooserFrame(Canvas c){
		canvas = c;
		colourChooser.getSelectionModel().addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				canvas.setColour(colourChooser.getColor());
			}
			
		});
		add(colourChooser);
		pack();
	}
	
	public void open(){
		colourChooser.setColor(canvas.getColour());
		setVisible(true);
	}
}