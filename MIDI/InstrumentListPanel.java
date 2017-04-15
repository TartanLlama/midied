package MIDI;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;


/**
 * @author 090006772
 * A panel allowing the user to add program change events to the MIDI tracks
 */
public class InstrumentListPanel extends JPanel{
	private MIDIEd theFrame;
	private JTable table;
	private JComboBox combo;
	
	public InstrumentListPanel(MIDIEd frame){
		setLayout(new BorderLayout());
		theFrame = frame;
		
		String[][] data = new String[16][2];
		String[] columnNames = {"Channel", "Instrument"};
		
		//Populate the data array
		for(int dataC = 0; dataC < data.length; dataC ++){
			data[dataC][0] = String.valueOf(dataC);
			data[dataC][1] = theFrame.getRollPanel().getInstruments()[dataC];
		}
		
		//Set up the table
		table = new JTable(data, columnNames); 
		table.getColumnModel().getColumn(0).setPreferredWidth(20);
		table.getColumnModel().getColumn(1).setPreferredWidth(200);
		add(table, BorderLayout.WEST);
		
		//Set up the instrument selector
		combo = new JComboBox(theFrame.getRollPanel().getPossibilites());
		add(combo, BorderLayout.SOUTH);
		
		//Set up the change button
		JButton change = new JButton("Change Instrument");
		change.addActionListener(new ActionListener() {
			
			//Add the specified program change event
			public void actionPerformed(ActionEvent e) {
				if(table.getSelectedRow() >= 0){
					theFrame.getRollPanel().createProgramChange(String.valueOf(combo.getSelectedItem()), table.getSelectedRow());
					table.getModel().setValueAt(combo.getSelectedItem(), table.getSelectedRow(), 1);
				}
			}
		});
		
		add(change, BorderLayout.EAST);
	}
}

