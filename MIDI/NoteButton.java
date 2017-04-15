package MIDI;
// NoteButton.java

import javax.swing.*;
import java.awt.*;
import javax.sound.midi.*;

public final class NoteButton extends JButton {
	private Track track ;
	private MidiEvent onEvent ;
	private MidiEvent offEvent ;
	private int channel ;
	private int key ;
	private int velocity ;

	// Display colour for the 16 channels
	// Palette from: http://web.media.mit.edu/~wad/color/palette.html
	// Except last, from: http://www.tayloredmktg.com/rgb/
	private static final Color[] channelColours = {
		Color.black,
		new Color(87,87,87),   // Dark grey
		new Color(173,35,35),  // a red
		new Color(42,75,215),  // a blue
		new Color(29,105,20),  // a green
		new Color(129,74,25),  // a brown
		new Color(129,38,192), // a purple
		new Color(160,160,160), // a light gray
		new Color(129,197,122), // a light green
		new Color(157,175,255), // a light blue
		new Color(41,208,208),  // a cyan
		new Color(255,146,51),  // an orange
		new Color(255,238,51),  // a yellow
		new Color(233,222,187), // a tan
		new Color(255,205,243), // a pink
		new Color(49,79,79),    //dark slate gray
	} ;

	// a unique identifier for every NoteButton constructed.
	public static int idNo = 0 ;

	// To remove the margin
	private static Insets noMargin = new Insets(0,0,0,0) ;	

	/* ======================================================================
     Constructor
	 ====================================================================== */
	public NoteButton(Track t, MidiEvent on, MidiEvent off, int c, int k, int v) {
		super(Integer.toString(v)) ;
		idNo++ ;
		track = t ;
		onEvent = on ;
		offEvent = off ;
		channel = c ;
		key = k ;
		velocity = v ;
		setBackground(Color.white) ;
		setForeground(channelColours[c]);	
	}  

	/* ======================================================================
     toString
	 ====================================================================== */
	public String toString() {
		StringBuffer result = new StringBuffer() ;
		result.append(" channel: "+channel) ;
		result.append(" key: "+key) ;
		result.append(" vel: "+velocity) ;
		return result.toString() ;
	}

	/* ======================================================================
     setPositionAndSize
	 ====================================================================== */
	public void setPositionAndSize(float beatScaleFactor, int noteHeight, float horizontalZoom) {
		long onTick = onEvent.getTick() ;
		long offTick = offEvent.getTick() ;
		setBounds((int)(onTick/beatScaleFactor),
				(127-key)*noteHeight,
				(int)((offTick-onTick)/beatScaleFactor),
				noteHeight) ;
	}

	/* ======================================================================
     remove
	 ====================================================================== */
	public void remove() {
		track.remove(onEvent) ;
		track.remove(offEvent) ;
	}
	
	public int getChannel(){
		return channel;
	}
}