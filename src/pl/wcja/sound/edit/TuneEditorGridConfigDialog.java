package pl.wcja.sound.edit;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import pl.wcja.sound.edit.TuneEditorGrid.GridDisplay;

/**
 * 
 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe³ Mika</a>, Geomar SA
 *
 */
public class TuneEditorGridConfigDialog extends JDialog {

	private TuneEditor editor = null;
	
	public TuneEditorGridConfigDialog(TuneEditor editor) {
		super((JFrame)editor.getMainFrame());
		this.editor = editor;
		setLayout(new GridBagLayout());
		
		initialize();
		pack();
	}
	
	private void initialize() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		
		JLabel jl = new JLabel("Grid view: ");
		add(jl, gbc);
		gbc.gridx++;
		
		JComboBox jcb = new JComboBox();
		for(GridDisplay gd : TuneEditorGrid.GridDisplay.values()) {
			jcb.addItem(gd);
		}
		jcb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editor.setGridDisplay((GridDisplay)((JComboBox)e.getSource()).getSelectedItem());
			}
		});
		add(jcb, gbc);
		gbc.gridx++;
	}
	
}
