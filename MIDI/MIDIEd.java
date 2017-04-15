package MIDI;
import javax.sound.midi.*;

import java.io.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public final class MIDIEd extends JFrame implements WindowListener {
	private static final int DEFAULT_RESOLUTION = 96 ;

	private static final int END_OF_TRACK = 47; //MetaEvent constant

	// String constants for actioncommands
	private static final String OPEN = "open" ;
	private static final String SAVE = "save" ;
	private static final String PLAY = "play" ;
	private static final String ZOOM_IN_V = "inv" ;
	private static final String ZOOM_OUT_V = "outv" ;
	private static final String ZOOM_IN_H = "inh" ;
	private static final String ZOOM_OUT_H = "outh" ;
	private static final String OFF = "off";
	private static final String INSTRUMENTS = "instruments";
	private static final String ADD_BAR = "addbar";

	// The ScrollPane manages a viewport onto the larger piano roll
	private JScrollPane prScrollPane ;
	private PianoRollPanel pianoRollPanel ;

	// The sequencer plays the sequence
	private Sequencer sequencer ;

	// The Sequence object we are editing
	private Sequence sequence ;

	//Fields for components
	private SpinnerNumberModel spinnerModel;
	private JComboBox noteLengthBox;
	private JComboBox channelBox;

	private PianoImagePanel piano;

	/* ======================================================================
     Constructor
	 ====================================================================== */
	public MIDIEd() { 
		super("MIDI Editor");
		// Initialise the Sequencer
		try {
			sequencer = MidiSystem.getSequencer();
		}
		catch(MidiUnavailableException mue) {
			System.out.println("Midi device unavailable!");
		}	
		// Initialise the sequence
		sequence = null ;
		// Initialise the GUI
		makeGUI();
		addWindowListener(this);
		pack();
		setResizable(false);
		setVisible(true);
	}  // end of MIDIEd constructor

	/* ======================================================================
     makeGUI
	 ====================================================================== */
	private void makeGUI() {
		Container c = getContentPane();

		pianoRollPanel = new PianoRollPanel(this) ;
		prScrollPane = new JScrollPane(pianoRollPanel) ;
		prScrollPane.setPreferredSize(new Dimension(500,600)) ;
		c.add(prScrollPane) ;

		// Add the menu
		JMenuBar menuBar = new JMenuBar() ;
		JMenu menu = new JMenu("File");
		menuBar.add(menu) ;
		JMenuItem openItem = new JMenuItem("Open") ;
		openItem.setActionCommand(OPEN) ;
		openItem.addActionListener(new MidiMenuListener()) ;
		JMenuItem saveItem = new JMenuItem("Save") ;
		saveItem.setActionCommand(SAVE) ;
		saveItem.addActionListener(new MidiMenuListener()) ;	
		menu.add(openItem) ;
		menu.add(saveItem) ;
		menu = new JMenu("Zoom");
		menuBar.add(menu) ;
		JMenuItem zoomItem = new JMenuItem("Zoom In (V)") ;
		zoomItem.setActionCommand(ZOOM_IN_V) ;
		zoomItem.addActionListener(new MidiMenuListener()) ;
		menu.add(zoomItem);
		zoomItem = new JMenuItem("Zoom Out (V)") ;
		zoomItem.setActionCommand(ZOOM_OUT_V) ;
		zoomItem.addActionListener(new MidiMenuListener()) ;
		menu.add(zoomItem);
		zoomItem = new JMenuItem("Zoom In (H)") ;
		zoomItem.setActionCommand(ZOOM_IN_H) ;
		zoomItem.addActionListener(new MidiMenuListener()) ;
		menu.add(zoomItem);
		zoomItem = new JMenuItem("Zoom Out (H)") ;
		zoomItem.setActionCommand(ZOOM_OUT_H) ;
		zoomItem.addActionListener(new MidiMenuListener()) ;
		menu.add(zoomItem);
		menu = new JMenu("Channel Filter");
		menuBar.add(menu);
		JRadioButtonMenuItem radioButton;
		ButtonGroup group = new ButtonGroup();
		radioButton = new JRadioButtonMenuItem("Off");
		radioButton.setSelected(true);
		radioButton.setActionCommand(OFF);
		radioButton.addActionListener(new MidiMenuListener());
		group.add(radioButton);
		menu.add(radioButton);
		//Add a radio button for each channel
		for(int channelC = 0; channelC < 16; channelC ++){
			radioButton = new JRadioButtonMenuItem(String.valueOf(channelC));
			radioButton.setActionCommand(String.valueOf(channelC));
			radioButton.addActionListener(new MidiMenuListener());
			group.add(radioButton);
			menu.add(radioButton);
		}
		menu = new JMenu("Quantisation");
		menuBar.add(menu);
		group = new ButtonGroup();
		JRadioButtonMenuItem snapButton;
		snapButton = new JRadioButtonMenuItem("Off");
		snapButton.setSelected(true);
		snapButton.setActionCommand("Q" + OFF);
		snapButton.addActionListener(new MidiMenuListener());
		group.add(snapButton);
		menu.add(snapButton);
		//Add radio buttons for each note length snapable to
		for(int snapC = 4; snapC <= 32; snapC *= 2){
			snapButton = new JRadioButtonMenuItem("1/" + snapC);
			snapButton.setActionCommand("Q1/" + snapC);
			snapButton.addActionListener(new MidiMenuListener());
			group.add(snapButton);
			menu.add(snapButton);
		}
		menu = new JMenu("Change Instruments/Number Of Bars");
		menuBar.add(menu);
		JMenuItem instruments = new JMenuItem("Instruments/Program Change");
		instruments.setActionCommand(INSTRUMENTS);
		instruments.addActionListener(new MidiMenuListener());
		menu.add(instruments);
		JMenuItem addBar = new JMenuItem("Add Bar");
		addBar.setActionCommand(ADD_BAR);
		addBar.addActionListener(new MidiMenuListener());
		menu.add(addBar);

		setJMenuBar(menuBar) ;
		JPanel controls = new JPanel() ;
		controls.setLayout( new BoxLayout(controls, BoxLayout.X_AXIS)) ;

		// Add the play button
		JButton playButton = new JButton("play") ;
		playButton.setActionCommand(PLAY) ;
		playButton.addActionListener(new MidiButtonListener()) ;
		controls.add(playButton) ;

		//Add the velocity label
		JLabel velocityLabel = new JLabel("Velocity");
		controls.add(velocityLabel);

		//Add the velocity spinner
		spinnerModel = new SpinnerNumberModel(100, 0, 127, 1);
		JSpinner velocitySpinner = new JSpinner(spinnerModel);
		controls.add(velocitySpinner);

		//Add the note length label
		JLabel lengthLabel = new JLabel("Length");
		controls.add(lengthLabel);

		//Add the note length combo box
		String[] noteValues = {"1", "1/2", "1/4", "1/8", "1/16", "1/32"};
		noteLengthBox = new JComboBox(noteValues);
		noteLengthBox.setEditable(false);
		controls.add(noteLengthBox);

		//Add the note channel label
		JLabel channelLabel = new JLabel("Channel");
		controls.add(channelLabel);

		//Add the note channel combo box
		Integer[] channels = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
		channelBox = new JComboBox(channels);
		channelBox.setEditable(false);
		controls.add(channelBox);

		c.add(controls, "South") ;

		piano = null;

		//Add the piano image panel
		try {
			piano = new PianoImagePanel(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JScrollPane scrollPane = new JScrollPane(piano, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(58, 600));

		prScrollPane.setVerticalScrollBar(scrollPane.getVerticalScrollBar());

		c.add(scrollPane, "West");
	}  // end of makeGUI()

	/* ======================================================================
     MIDIButtonListener
	 ====================================================================== */  
	private class MidiButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			String command = ev.getActionCommand() ;	
			if (command == PLAY) play() ;	  
		}
	}

	/* ======================================================================
     MidiMenuListener
	 ====================================================================== */  
	private class MidiMenuListener implements ActionListener {  
		public void actionPerformed(ActionEvent ev) {
			String command = ev.getActionCommand() ;
			if (command == OPEN) {
				openFile() ;
			}
			else if (command == SAVE) {
				saveFile() ;
			}else if (command == ZOOM_IN_V)
				pianoRollPanel.modifyVerticalZoom(true) ;
			else if (command == ZOOM_OUT_V)
				pianoRollPanel.modifyVerticalZoom(false) ;
			else if (command == ZOOM_IN_H)
				pianoRollPanel.modifyHorizontalZoom(true) ;
			else if (command == ZOOM_OUT_H)
				pianoRollPanel.modifyHorizontalZoom(false) ;
			else if (command == INSTRUMENTS)
				displayInstrumentMenu();
			else if (command == ADD_BAR){
				pianoRollPanel.addBar();
				pianoRollPanel.update();
			}
			else if (command.startsWith("Q"))
				pianoRollPanel.setQuantisation(command.substring(1));
			else
				pianoRollPanel.setFilter(command);
			piano.setPreferredSize(new Dimension(58, 1125 + 127 * pianoRollPanel.getNoteHeight()));
			piano.setSize(new Dimension(58, 1125 + 127 * pianoRollPanel.getNoteHeight()));
			piano.repaint();
		}


	}

	private void displayInstrumentMenu() {
		InstrumentListPanel instrumentPanel = new InstrumentListPanel(this);
		JFrame instrumentFrame = new JFrame("Instruments/Program Change");
		instrumentFrame.add(instrumentPanel);
		instrumentFrame.pack();
		instrumentFrame.setVisible(true);
		instrumentFrame.requestFocus();
	}


	/**
	 *  Open a midi file and read it into the sequence field
	 */
	public void openFile() {
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			pianoRollPanel.clear() ;
			File file = fc.getSelectedFile();
			try {
				sequence = MidiSystem.getSequence(file) ;
				pianoRollPanel.setSequence(sequence) ;	  
			}
			catch(IOException ioe) {
				System.out.println("I/O Error!");
			}
			catch(InvalidMidiDataException imde) {
				System.out.println("Invalid Midi data!");
			}
		}
	}

	/**
	 * Save the current sequence as a type 0 midi file
	 */
	public void saveFile() {
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
				MidiSystem.write(sequence, 0, file);
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
				System.exit(1);
			}		
		}
	}

	/* ======================================================================
     createBlankSequence
	 ====================================================================== */
	public void createBlankSequence() {
		try {
			sequence = new Sequence(Sequence.PPQ, DEFAULT_RESOLUTION);
		}
		catch (InvalidMidiDataException e) {
			e.printStackTrace();
			System.exit(1);
		}
		sequence.createTrack() ;  	
		pianoRollPanel.setSequence(sequence) ;
	}


	/**
	 * Changed from original code
	 * Plays the MIDI sequence
	 */
	public void play() {
		try {
			sequencer.setSequence(sequence);
			sequencer.open();
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}

		sequencer.start();
		sequencer.addMetaEventListener(new MetaEventListener() {

			public void meta(MetaMessage meta) {
				//If it's the end of the track, stop the sequencer
				if(meta.getType() == END_OF_TRACK){
					sequencer.stop();
					sequencer.close();
				}	
			}
		});
	}

	public int getVelocity(){
		return (Integer)spinnerModel.getValue();
	}

	public String getNoteLength(){
		return (String) noteLengthBox.getSelectedItem();
	}

	public int getChannel() {
		return (Integer) channelBox.getSelectedItem();
	}

	public PianoRollPanel getRollPanel(){
		return pianoRollPanel;
	}

	// ----------------- window listener methods -----------------------------------
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowClosing(WindowEvent e) {System.exit(0) ;}
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}

	/* =============================================================================
     main method
     ============================================================================= */
	public static void main(String[] args) {
		MIDIEd midiEd = new MIDIEd() ;
		midiEd.createBlankSequence() ;
	} // end of main
}