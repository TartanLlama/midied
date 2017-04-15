package MIDI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;

import javax.swing.JPanel;


/**
 * @author 090006772
 * A class to display a visualization of a piano
 */
public class PianoImagePanel extends JPanel {
	private MIDIEd theFrame;
	private static final int NUMBER_OF_WHITE_KEYS = 75;

	public PianoImagePanel(MIDIEd frame) throws IOException{
		super(new BorderLayout());
		theFrame = frame;
		
		setPreferredSize(new Dimension(58, 1125));
		repaint();
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int blackNoteCounter = 0;
		int noteHeight = theFrame.getRollPanel().getNoteHeight();
		int alteredNoteHeight = noteHeight + noteHeight/2;
		boolean increaseSize = false;
		int y = 0;
		
		//Draws the piano
		for(int whiteNotesC = 0; whiteNotesC < NUMBER_OF_WHITE_KEYS; whiteNotesC ++){
			//Makes sure the white key is extended if necessary
			if(!increaseSize){
				g.drawRect(0, y, 58, alteredNoteHeight);
				y += alteredNoteHeight;
			}else{
				g.drawRect(0, y, 58, alteredNoteHeight + noteHeight/2);
				y += alteredNoteHeight + noteHeight/2;
			}
			
			//Draws black keys in the correct places
			if(blackNoteCounter == 0 || blackNoteCounter == 3 || blackNoteCounter == 4){
				increaseSize = true;
				g.setColor(Color.black);
				g.fillRect(0, y - noteHeight/2, 30, noteHeight);
			}else if(blackNoteCounter == 1 || blackNoteCounter == 5){
				increaseSize = true;
				g.setColor(Color.black);
				g.fillRect(0, y - noteHeight/2, 30, noteHeight);
			}
			
			blackNoteCounter ++;
			//Resets the back counter if needed
			if(blackNoteCounter == 7){
				blackNoteCounter = 0;
			}
		}
	}
}
