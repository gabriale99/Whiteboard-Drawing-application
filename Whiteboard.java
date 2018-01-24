

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.XMLDecoder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

public class Whiteboard extends JFrame implements CanvasSelectionListener, CanvasListListener, ModelListener{
	
	private Canvas canvas;
	private JTextField inputText;
	private JComboBox<String> fontSelection;
	private JTable table;	
	private ArrayList<Socket> connections = new ArrayList<Socket>();
	private ArrayList<ObjectOutputStream> outputStreams = new ArrayList<ObjectOutputStream>();
	private Socket clientConnection;
	private boolean isServer = false;
	private boolean isClient = false;
	public static int DEFAULT_PORT = 39857;
	
	public Whiteboard() {
		init();
	}

	public void init() {
		setTitle("Whiteboard");
		setLayout(new BorderLayout());

		canvas = new Canvas();
		canvas.addSelectionListener(this);
		canvas.addListListener(this);
		ColourChooserFrame ccf = new ColourChooserFrame(canvas);
		add(canvas, BorderLayout.CENTER);

		//Menu Bars 
		JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        Whiteboard self = this; //Use this to get around some referencing issues
        
        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("xml file", "xml"));
				int returnVal = fileChooser.showSaveDialog(self);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if(!file.getName().endsWith(".xml")) {
						file = new File(file.toString() + ".xml");
					}
					canvas.saveState(file);
				}
			}
        	
        });
        
        JMenuItem open = new JMenuItem("Open");
        open.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("xml file", "xml"));
				int returnVal = fileChooser.showOpenDialog(self);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					load(fileChooser.getSelectedFile());
				}
			}
        	
        });
       
        JMenuItem saveImage = new JMenuItem("Save Image");
        saveImage.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("png file", "png"));
				int returnVal = fileChooser.showSaveDialog(self);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if(!file.getName().endsWith(".png")) {
						file = new File(file.toString() + ".png");
					}
					canvas.saveImage(file);
				}
			}
        	
        });
        
        fileMenu.add(open);
        fileMenu.add(save);
        fileMenu.add(saveImage);
        
        setJMenuBar(menuBar);
        
        // Network Buttons - Server & Client Mode
        JTextField status = new JTextField("N/A");
        status.setEditable(false);
        JButton serverMode = new JButton("Server Start");
        serverMode.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Integer portNumber = Integer.parseInt(JOptionPane.showInputDialog("Select a port number", DEFAULT_PORT));
				Thread serverThread = new Thread() {
					@Override
					public void run() {
						status.setText("Server Mode");
						isServer = true;
						try {
							ServerSocket socket = new ServerSocket(portNumber);
							while(true) {
								Socket s = socket.accept();
								connections.add(s);
								outputStreams.add(new ObjectOutputStream(s.getOutputStream()));
								
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
				serverThread.start();
			}
        	
        });
        JButton clientMode = new JButton("Client Start");
        clientMode.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				status.setText("Client");
				isClient = true;
				String hostAndPort = JOptionPane.showInputDialog("Enter an IP:port", "127.0.0.1:" + DEFAULT_PORT);
				String host = hostAndPort.split(":")[0];
				int port = Integer.parseInt(hostAndPort.split(":")[1]);
				try {
					clientConnection = new Socket(host, port);
					Thread inputThread = new Thread() {
						ObjectInputStream reader;
						//ObjectOutputStream temp = new ObjectOutputStream(clientConnection.getOutputStream());
						@Override
						public void run() {
							//System.out.println("THREAD RUNNING");
							try {
							//	System.out.println("ENTERED TRY BLOCK");
								reader = new ObjectInputStream(clientConnection.getInputStream());
								//System.out.println("READER SET");
								while(true) {
									interpretInput((String)reader.readObject(), (DShapeModel)reader.readObject());
								}
							} catch (IOException e) {
								//System.out.println("IO EXCEPTION");
								e.printStackTrace();
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							}
						}
					};
					inputThread.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
				canvas.setEditable(false);
				//System.out.println("CANVAS UNEDITABLE");
			}
        	
        });
        
        Box mode = Box.createHorizontalBox();
        mode.add(serverMode);
        mode.add(clientMode);
        mode.add(status);
        add(mode, BorderLayout.NORTH);
		
        
        
		JPanel addButtons = new JPanel();
		JLabel addLabel = new JLabel(" Add");
		JPanel westPanel = new JPanel();
		addButtons.setLayout(new BoxLayout(addButtons, BoxLayout.X_AXIS));
		westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
		addButtons.add(addLabel);
		JButton addRect = new JButton("Rectangle");
		addRect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.addShape(new DRectModel(10, 10, 20, 20, canvas.getColour()));
				canvas.paintComponent();
			}

		});
		addButtons.add(addRect);
		JButton addOval = new JButton("Oval");
		addOval.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.addShape(new DOvalModel(10, 10, 20, 20, canvas.getColour()));
				canvas.paintComponent();
			}

		});
		addButtons.add(addOval);
		JButton addLine = new JButton("Line");
		addLine.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.addShape(new DLineModel(10, 10, 20, 20, canvas.getColour()));
				canvas.paintComponent();
			}
			
		});
		addButtons.add(addLine);
		JButton addText = new JButton("Text");
		addText.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.addShape(new DTextModel(10, 10, 20, 20, canvas.getColour()));
				canvas.paintComponent();
			}
			
		});
		addButtons.add(addText);
		westPanel.add(addButtons);
		
		JPanel colorSlot = new JPanel();
		colorSlot.setLayout(new BoxLayout(colorSlot, BoxLayout.X_AXIS));
		JButton setColour = new JButton("Set Colour");
		setColour.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ccf.open();
			}

		});
		colorSlot.add(new JLabel(" Set Color:"));
		colorSlot.add(setColour);
		westPanel.add(colorSlot);
		

		JPanel modifierButtons = new JPanel();
		modifierButtons.setLayout(new BoxLayout(modifierButtons, BoxLayout.X_AXIS));
		JButton deleteShape = new JButton("Delete Shape");
		deleteShape.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.deleteShape();
			}
			
		});
		modifierButtons.add(deleteShape);
		JButton moveForward = new JButton("Move Forward");
		moveForward.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.moveForward();	
			}
			
		});
		modifierButtons.add(moveForward);
		JButton moveBack = new JButton("Move Back");
		moveBack.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.moveBack();	
			}
			
		});
		modifierButtons.add(moveBack);
		westPanel.add(modifierButtons);
		
		
		JPanel textInspector = new JPanel();
		inputText = new JTextField();
		inputText.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				DText text = (DText)canvas.getSelected();
				((DTextModel)text.getModel()).setText(inputText.getText());
				canvas.paintComponent();
			}
			
		});
		inputText.setPreferredSize(new Dimension(150, 25));
		inputText.setEnabled(false);		
		textInspector.add(inputText);
		fontSelection = new JComboBox<String>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		fontSelection.setSelectedItem("Dialog");
		fontSelection.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				DText text = (DText)canvas.getSelected();
				((DTextModel)text.getModel()).setFont((String)fontSelection.getSelectedItem());
				canvas.paintComponent();
			}
			
		});
		fontSelection.setEnabled(false);
		textInspector.add(fontSelection);
		westPanel.add(textInspector);
		
		String[] headers = new String[]{"X", "Y", "Width", "Height"};
		DefaultTableModel tableModel = new DefaultTableModel(headers, 0);
		table = new JTable(tableModel);
		JScrollPane tablePane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		westPanel.add(tablePane);
		for (Component comp : westPanel.getComponents()) {
			((JComponent) comp).setAlignmentX(Box.LEFT_ALIGNMENT);
		}
		add(westPanel, BorderLayout.WEST);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
	}

	private static Color randomColor() {
		Color c = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
		// System.out.println(c);
		return c;
	}

	public static void main(String[] args) {
		Whiteboard w = new Whiteboard();
	}

	@Override
	public void onSelectionChange(DShape newSelection) {
		if(newSelection instanceof DText){
			inputText.setEnabled(true);
			fontSelection.setEnabled(true);
		}else{
			inputText.setEnabled(false);
			fontSelection.setEnabled(false);
		}
	}
	
	@Override
	public void onListAdd(DShape shapeAdded) {
		DShapeModel model = shapeAdded.getModel();
		model.addListener(this);
		((DefaultTableModel)table.getModel()).addRow(new Object[]{model.getCoordinates().getX(), model.getCoordinates().getY(), model.getDimensions().getX(), model.getDimensions().getY()});
		//System.out.println("Added");
		if(isServer) {
			for(int i = 0; i < connections.size(); i++) {
				try {
					ObjectOutputStream o = outputStreams.get(i);
					o.writeUnshared("add");
					o.writeUnshared(model);
					System.out.println("Sent Add command");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onListRemove(DShape shapeRemoved, int indexRemovedFrom) {
		shapeRemoved.getModel().removeListener(this);
		((DefaultTableModel)table.getModel()).removeRow(indexRemovedFrom);
		if(isServer) {
			for(int i = 0; i < connections.size(); i++) {
				try {
					ObjectOutputStream o = outputStreams.get(i);
					o.writeUnshared("remove");
					o.writeUnshared(shapeRemoved.getModel());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void load(File f) {
		ArrayList<DShapeModel> models = new ArrayList<DShapeModel>();
		try {
			XMLDecoder xml = new XMLDecoder(new FileInputStream(f));
			DShapeModel current = (DShapeModel)xml.readObject();
			while(current != null) {
				models.add(current);
				try {
					current = (DShapeModel)xml.readObject();
				}catch(ArrayIndexOutOfBoundsException e) {
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for(DShapeModel model : models) {
			canvas.addShape(model);
		}
		canvas.paintComponent();
	}
	
	public void interpretInput(String command, DShapeModel model) {
		//System.out.println("Recieved command: " + command + " | " +  model);
		if(command.equals("add")) {
			canvas.addShapeClient(model);
		}else if(command.equals("remove")) {
			canvas.removeShape(model);
		}else if(command.equals("forward")) {
			canvas.moveForward(model);
		}else if(command.equals("back")) {
			canvas.moveBack(model);
		}else if(command.equals("change")) {
			canvas.modifyShape(model);
		}
		canvas.paintComponent();
	}
	
	@Override
	public void modelChanged(DShapeModel model) {
		int row = canvas.indexOf(model);
		((DefaultTableModel)table.getModel()).setValueAt(model.getCoordinates().getX(), row, 0);
		((DefaultTableModel)table.getModel()).setValueAt(model.getCoordinates().getY(), row, 1);
		((DefaultTableModel)table.getModel()).setValueAt(model.getDimensions().getX(), row, 2);
		((DefaultTableModel)table.getModel()).setValueAt(model.getDimensions().getY(), row, 3);
		if(isServer) {
			//System.out.println("Sent command: change with object: " + model + " to " + connections.size() + " clients");
			for(int i = 0; i < connections.size(); i++) {
				try {
					ObjectOutputStream o = outputStreams.get(i);
					o.writeUnshared("change");
					o.writeUnshared(model);
					System.out.println("Sent Change command");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void onListForward(DShape shape) {
		modelChanged(shape.getModel());
		if(isServer) {
			for(int i = 0; i < connections.size(); i++) {
				try {
					ObjectOutputStream o = outputStreams.get(i);
					o.writeUnshared("forward");
					o.writeUnshared(shape.getModel());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void onListBack(DShape shape) {
		modelChanged(shape.getModel());
		if(isServer) {
			for(int i = 0; i < connections.size(); i++) {
				try {
					ObjectOutputStream o = outputStreams.get(i);
					o.writeUnshared("back");
					o.writeUnshared(shape.getModel());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
