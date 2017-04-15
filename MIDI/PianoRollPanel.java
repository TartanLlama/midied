package MIDI;
// PianoRollPanel.java

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Iterator ;
import java.util.HashMap ;

public final class PianoRollPanel extends JPanel {
	// parent frame
	private MIDIEd theFrame;

	// Useful colour
	private static final Color LIGHT_GREY = new Color(180,180,180) ;  

	// vertical zoom varies from 1-10
	private int verticalZoom = 1 ;  
	// noteHeight varies with vertical zoom
	private static int NOTEHEIGHTUNIT = 10 ;
	private int noteHeight = 10 ;
	private float horizontalZoom = 1f;
	// beatWidth varies with horizontal zoom
	private static int DEFAULTBEATHWIDTH = 100;
	private int beatWidth = 100 ;
	// beatScaleFactor translates from panel positions to ticks  
	private float beatScaleFactor = 1f ;

	// Ticks per beat?  
	private int resolution = 96 ;

	// How many beats do we want to display?
	private static int DISPLAY_BEATS = 16 ;

	// HashMap of noteButtons: facilitates access from notePressed
	private HashMap<String,NoteButton> noteButtons ;

	// The sequence that this panel displays.
	private Sequence sequence ;

	//Mapping of note values eg. 1/4, 1/8 to ticks
	private HashMap<String, Integer> noteLengths;
	
	private TwoWayMap programMap;

	private static final String OFF = "off";

	//Used to quantise notes
	private int quantisation = 0;
	
	//Current instrument for each channel
	private String[] instruments = new String[16];
	
	//All possible instruments
	private String[] possibilities = {"Acoustic Grand Piano", "Bright Acoustic Grand", "Electric Grand", "Honky Tonk", "Electric Piano 1", "Electric Piano 2", "Harpsichord", "Clavinet", "Celesta", "Glockenspiel", "Music Box", "Vibraphone", "Marimba", "Xylophone", "Tubular Bells", "Dulcimer", "Drawback Organ", "Percussive Organ", "Rock Organ", "Church Organ", "Reed Organ", "Accordian", "Harmonica", "Tango Accordian", "Nylon String Guitar", "Steel String Guitar", "Electric Jazz Guitar", "Electric Clean Guitar", "Electric Muted Guitar", "Overdriven Guitar", "Distortion Guitar", "Guitar Harmonics", "Acoustic Bass", "Electric Bass Fingerpicked", "Electric Bass Picked", "Fretless Bass", "Slap Bass 1", "Slap Bass 2", "Synth Bass 1", "Synth Bass 2", "Violin", "Viola", "Cello", "Contrabass", "Tremolo Strings", "Pizzicato Strings", "Orchestral Strings", "Timpani", "String Ensemble 1", "String Ensemble 2", "Synth Strings 1", "Synth Strings 2", "Choir Aahs", "Voice Oohs", "Synth Voice", "Orchestra Hit", "Trumpet", "Trombone", "Tuba", "Muted Trumpet", "French Horn", "Brass Section", "Synth Brass 1", "Synth Brass 2", "Soprano Sax", "Alto Sax", "Tenor Sax", "Baritone Sax", "Oboe", "English Horn", "Basson", "Clarinet", "Piccolo", "Flute", "Recorder", "Pan Flute", "Blown Bottle", "Skakuhachi", "Whistle", "Ocarina of Time", "Synth Lead 1", "Synth Lead 2", "Synth Lead 3", "Synth Lead 4", "Synth Lead 5", "Synth Lead 6", "Synth Lead 7", "Synth Lead 8", "Synth Pad 1", "Synth Pad 2", "Synth Pad 3", "Synth Pad 4", "Synth Pad 5", "Synth Pad 6", "Synth Pad 7", "Synth Pad 8", "Synth FX 1", "Synth FX 2", "Synth FX 3", "Synth FX 4", "Synth FX 5", "Synth FX 6", "Synth FX 7", "Synth FX 8", "Sitar", "Banjo", "Shamisen", "Koto", "Kalimba", "Bagpipe", "Fiddle", "Shanai", "Tinkle Bell", "Agogo", "Steel Drums", "Woodblock", "Taiko Drum", "Melodic Tom", "Synth Drum", "Reverse Cymbal", "Guitar Fret Noise", "Breath Noise", "Seashore", "Bird Tweet", "Telephone Ring", "Helicopter", "Applause", "Gunshot"};

	/* ======================================================================
     Constructor
	 ====================================================================== */
	public PianoRollPanel(MIDIEd inFrame) {
		theFrame = inFrame ;
		//want to position the noteButtons manually
		setLayout(null) ;
		// parent paintComponent can take care of the bg
		setBackground(Color.white);
		// This will vary based on zoom
		setPreferredSize( new Dimension(beatWidth*DISPLAY_BEATS, noteHeight*128) );
		// keep a record of the notes
		noteButtons = new HashMap<String,NoteButton>() ;

		initializeNoteLengths();
		findInstruments();
		
		setFocusable(true);
		requestFocus();
		handleMouseInput();

	} // end of PianoRollPanel constructor

	/**
	 * Gets the length of the track and sets the number of beats to display accordingly
	 */
	private void getTrackLength() {
		Track track = sequence.getTracks()[0];
		
		long furthestTick = 0;
		//Get the furthest away tick
		for (int e = 0; e < track.size(); e++) {
			if(track.get(e).getTick() > furthestTick){
				furthestTick = track.get(e).getTick();
			}
		}
		long numberOfBars = furthestTick/(resolution*4)+1;
		DISPLAY_BEATS = (int) (numberOfBars * 4);
	}

	/**
	 * Get the default instruments for the track
	 */
	private void findInstruments() {
		programMap = new TwoWayMap(possibilities);
		
		for(int channelC = 0; channelC < 16; channelC ++){
			instruments[channelC] = programMap.getString(channelC);
		}
	}

	private void initializeNoteLengths() {
		noteLengths = new HashMap<String, Integer>();
		noteLengths.put("1", resolution * 4);
		noteLengths.put("1/2", resolution * 2);
		noteLengths.put("1/4", resolution);
		noteLengths.put("1/8", resolution / 2);
		noteLengths.put("1/16", resolution / 4);
		noteLengths.put("1/32", resolution / 8);
	}

	/* ======================================================================
     paintComponent
	 ====================================================================== */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g) ;
		// horizontal note boundaries
		g.setColor(LIGHT_GREY) ;	
		for (int i = 0; i < 128; i++)
			g.drawLine(0,i*noteHeight,DISPLAY_BEATS*beatWidth,i*noteHeight) ;
		// Draw sub-beat, beat, bar boundaries
		int panelHeight = noteHeight*128 ;
		int x = 0 ;
		for (int bar = 0; bar < DISPLAY_BEATS/4; bar++) {
			g.setColor(Color.red) ;
			g.drawLine(x,0,x,panelHeight) ;
			x += beatWidth/4 ;	  
			for (int beat = 0; beat < 4; beat++) {
				g.setColor(LIGHT_GREY) ;	  
				for (int subBeat = 1; subBeat < 4; subBeat++) {
					g.drawLine(x,0,x,panelHeight) ;
					x += beatWidth/4 ;
				}
				if (beat < 3) {
					g.setColor(Color.black) ;
					g.drawLine(x,0,x,panelHeight) ;
					x += beatWidth/4 ;
				}
			}
		}
	}

	/* ======================================================================
     clear
	 ====================================================================== */
	public void clear() {
		Iterator<NoteButton> it = noteButtons.values().iterator() ;
		while (it.hasNext()) {
			NoteButton noteButton = it.next() ;
			remove(noteButton) ;	  
		}	
		repaint() ;
	}

	/* ======================================================================
     setSequence
	 ====================================================================== */
	public void setSequence(Sequence inSequence) {
		sequence = inSequence ;
		System.out.println(sequence.getTracks().length);
		getTrackLength();
		
		resolution = sequence.getResolution() ;
		// beatScaleFactor translates from panel positions to ticks
		beatScaleFactor = resolution/(float)beatWidth ;
		noteButtons = new HashMap<String,NoteButton>() ;
		findNotes() ;
		update() ;
	}

	/* ======================================================================
     modifyVerticalZoom
	 ====================================================================== */
	public void modifyVerticalZoom(boolean in) {
		if (in && (verticalZoom < 10)) {
			verticalZoom++ ;
		}
		else if (!in && (verticalZoom > 1)) {
			verticalZoom-- ;
		}
		noteHeight = verticalZoom*NOTEHEIGHTUNIT ;
		update() ;
	}

	/* ======================================================================
    modifyHorizontalZoom
	 ====================================================================== */
	public void modifyHorizontalZoom(boolean in) {
		if (in && (horizontalZoom > 1/4)) {
			horizontalZoom /= 2 ;
		}
		else if (!in && (horizontalZoom < 1)) {
			horizontalZoom *= 2 ;
		}
		beatWidth = (int) (DEFAULTBEATHWIDTH / horizontalZoom); 
		beatScaleFactor = resolution/(float)beatWidth ;
		update() ;
	}

	/* ======================================================================
     update
	 ====================================================================== */
	public void update() {
		setPreferredSize( new Dimension(beatWidth*DISPLAY_BEATS, noteHeight*128) );	
		// make sure the note buttons are the right size.
		Iterator it = noteButtons.values().iterator() ;
		while (it.hasNext()) {
			NoteButton noteButton = (NoteButton)it.next() ;
			noteButton.setPositionAndSize(beatScaleFactor, noteHeight, horizontalZoom) ;	  
		}
		revalidate() ;  
	}  

	/* ======================================================================
     findNotes
	 We are going to assume type 0: i.e. a single track.
	 ====================================================================== */
	private void findNotes() {
		Track track = sequence.getTracks()[0] ;
		// Keep a reference to the start of each note
		MidiEvent[][] noteStarts = new MidiEvent[128][16] ;
		for (int i1 = 0; i1 < 128; i1++)
			for (int i2 = 0; i2 < 16; i2++)
				noteStarts[i1][i2] = null ;
		// Iterate over track.
		for (int e = 0; e < track.size(); e++) {
			MidiEvent event = track.get(e) ;
			long tick = event.getTick() ;
			MidiMessage msg = event.getMessage() ;
			// we only care about short messages
			if (msg instanceof ShortMessage) {
				ShortMessage shortMsg = (ShortMessage)msg ;
				int command = shortMsg.getCommand() ; 
				int channel = shortMsg.getChannel() ;
				if (command == ShortMessage.NOTE_ON) {
					int key = shortMsg.getData1() ;
					// is this the start of a new note?
					if (noteStarts[key][channel] == null)
						noteStarts[key][channel] = event ;
					// if not, check that vel is 0 (note end)
					else if (shortMsg.getData2() == 0) {
						addNoteButton(track, noteStarts[key][channel], event) ;
						// get ready for new note
						noteStarts[key][channel] = null ;
					}
				} // end of NOTE_ON block
				else if (command == ShortMessage.NOTE_OFF) {
					int key = shortMsg.getData1() ;
					// have we seen a corresponding note on?
					if (noteStarts[key][channel] != null) {
						addNoteButton(track, noteStarts[key][channel], event) ;
						// get ready for new note
						noteStarts[key][channel] = null ;
					}
				} // end of NOTE_OFF block
				else if (command == ShortMessage.PROGRAM_CHANGE){
					int program = shortMsg.getData1();
					Synthesizer synth = null;
					try {
						synth = MidiSystem.getSynthesizer();
					} catch (MidiUnavailableException e1) {
						e1.printStackTrace();
					}
					synth.getChannels()[channel].programChange(program);
				}
			} // end of ShortMessage test
		} // end of MidiEvent loop
	}

	/* ======================================================================
     addNoteButton
	 ====================================================================== */  
	private void addNoteButton (Track t, MidiEvent onEvent, MidiEvent offEvent) {
		ShortMessage shortMsg = (ShortMessage)onEvent.getMessage() ;
		NoteButton newNoteButton = 
			new NoteButton(t, onEvent, offEvent, shortMsg.getChannel(), 
					shortMsg.getData1(), shortMsg.getData2()) ;
		String id = Integer.toString(NoteButton.idNo) ;
		newNoteButton.setActionCommand(id) ;
		newNoteButton.addActionListener(new NoteButtonListener()) ;				  
		noteButtons.put(id,newNoteButton) ;
		add(newNoteButton) ;
		t.add(onEvent);
		t.add(offEvent);
	}

	/* ======================================================================
  NoteButtonListener
	 ====================================================================== */  
	private class NoteButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			notePressed(ev.getActionCommand()) ;
		}
	}

	/* ======================================================================
    handleMouseInput
    A mouse click results in a note being added.
    ====================================================================== */
	private void handleMouseInput() {
		addMouseListener( new MouseListener() {

			public void mouseReleased(MouseEvent e) {
				int x = e.getX() ;
				int y = e.getY() ;
				if(e.getButton() == MouseEvent.BUTTON1){
					int key = (127-y/noteHeight) ;
					long tick = (long)(x*beatScaleFactor) ;
					if(quantisation != 0){
						tick = quantise(tick);
					}
					Track track = sequence.getTracks()[0] ;
					int noteLength = noteLengths.get(theFrame.getNoteLength());
					addNoteButton(track,
							createNoteOnEvent(key,tick),
							createNoteOffEvent(key,tick+noteLength)) ;
					update() ;
				}
			}

			public void mousePressed(MouseEvent e) {}

			public void mouseExited(MouseEvent e) {}

			public void mouseEntered(MouseEvent e) {}

			public void mouseClicked(MouseEvent e) {}

			private long quantise(long tick) {
				tick -= tick%quantisation;
				return tick;
			}});
	}  // end of handleMouseInput()

	/**
	 * Add a program change message to the track
	 * @param instrumentName
	 * @param channel
	 */
	public void createProgramChange(String instrumentName, int channel) {
		Track track = sequence.getTracks()[0];
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(ShortMessage.PROGRAM_CHANGE, channel, programMap.getIndex(instrumentName), 0);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
		MidiEvent event = new MidiEvent(message, 0);
		track.add(event);
	}
	
	/* ======================================================================
    createNoteOnEvent
    ====================================================================== */
	private MidiEvent createNoteOnEvent(int key, long tick) {
		return createNoteEvent(ShortMessage.NOTE_ON, key, theFrame.getVelocity(), tick);
	} // end of createNoteOnEvent

	/* ======================================================================
    createNoteOffEvent
    ====================================================================== */
	private MidiEvent createNoteOffEvent(int key, long tick) {
		return createNoteEvent(ShortMessage.NOTE_OFF, key, 0, tick);
	}

	/* ======================================================================
    createNoteEvent
    ====================================================================== */
	private MidiEvent createNoteEvent(int command, int key, int velocity, long tick) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(command, theFrame.getChannel(), key, velocity);//1 is note channel
		}
		catch (InvalidMidiDataException e) {
			e.printStackTrace();
			System.exit(1);
		}
		MidiEvent event = new MidiEvent(message, tick);
		return event;
	} // end of createNoteEvent

	/* ======================================================================
  notePressed
	 This results in the note's removal.
	 ====================================================================== */
	public void notePressed(String noteId) {
		NoteButton noteButton = noteButtons.remove(noteId) ;
		// remove associated MidiEvents
		noteButton.remove() ;
		// remove the button from the panel
		remove(noteButton) ;
		// update display
		repaint() ;
	}

	/**
	 * Set a filter on a specific channel
	 * @param channel
	 */
	public void setFilter(String channel){
		if(channel == OFF){
			clear();
			//Display all buttons
			for(int buttonC = 1; buttonC <= noteButtons.size(); buttonC ++){
				add(noteButtons.get(String.valueOf(buttonC)));
			}
		}else{
			clear();
			//Display only buttons on a certain channel
			for(int buttonC = 1; buttonC <= noteButtons.size(); buttonC ++){
				if(noteButtons.get(String.valueOf(buttonC)).getChannel() == Integer.valueOf(channel))
					add(noteButtons.get(String.valueOf(buttonC)));
			}
		}
	}

	/**
	 * Set what tick new notes should round to the nearest
	 * @param substring
	 */
	public void setQuantisation(String substring) {
		if(substring.equals(OFF)){
			quantisation=0;
		}else{
			substring = substring.substring(2);
			quantisation = resolution/(Integer.valueOf(substring)/4);
		}
	}

	public int getNoteHeight(){
		return noteHeight;
	}
	
	public String[] getInstruments(){
		return instruments;
	}
	
	public String[] getPossibilites(){
		return possibilities;
	}
	
	public void addBar(){
		DISPLAY_BEATS += 4;
	}
}  // end of GamePanel class
