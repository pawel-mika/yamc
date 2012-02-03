package pl.wcja.yamc.gui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.sound.Tune;

public class ProjectProperitiesDialog extends MFOkCancelDialog {

	public ProjectProperitiesDialog(IMainFrame mf) {
		super(mf);
		Tune t = mf.getTuneEditor().getTune();
		SpringLayout layout = new SpringLayout();
		JPanel p = new JPanel(layout);
		JLabel l = (JLabel) p.add(new JLabel("Name:"));
		JTextField tf = (JTextField) p.add(new JTextField(t.getTuneName(), 64));
		layout.putConstraint(SpringLayout.WEST, l, 4, SpringLayout.WEST, p);
		
		add(p, BorderLayout.CENTER);
		pack();
	}

	@Override
	protected void okClicked() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void cancelCliked() {
		// TODO Auto-generated method stub

	}

}
