package pl.wcja.yamc.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.sound.Tune;

public class ProjectProperitiesDialog extends MFOkCancelDialog {

	private JTextField jtfTuneName;

	public ProjectProperitiesDialog(IMainFrame mf) {
		super(mf);
		Tune t = mf.getTuneEditor().getTune();
//		SpringLayout layout = new SpringLayout();
//		JPanel p = new JPanel(layout);
//		JLabel l = (JLabel) p.add(new JLabel("Name:"));
//		JTextField tf = (JTextField) p.add(new JTextField(t.getTuneName(), 64));
//		layout.putConstraint(SpringLayout.WEST, l, 4, SpringLayout.WEST, p);
//		layout.putConstraint(SpringLayout.NORTH, l, 4, SpringLayout.WEST, p);
//		layout.putConstraint(SpringLayout.WEST, tf, 4, SpringLayout.EAST, l);
//		layout.putConstraint(SpringLayout.NORTH, tf, 4, SpringLayout.WEST, p);
//		layout.putConstraint(SpringLayout.EAST, p, 4, SpringLayout.EAST, tf);
//		layout.putConstraint(SpringLayout.SOUTH, p, 4, SpringLayout.SOUTH, tf);
		JPanel p = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(2,2,2,2), 1, 1);
		p.setLayout(gbl);
		p.add(new JLabel("Name:"), gbc);
		gbc.gridy = 1;
		p.add(new JLabel("Track count:"), gbc);
		gbc.gridy = 2;
		p.add(new JLabel("Length:"), gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		jtfTuneName = new JTextField(t.getTuneName(), 32);
		p.add(jtfTuneName, gbc);
		gbc.gridy = 1;
		p.add(new JLabel("" + t.getTracks().size()), gbc);
		gbc.gridy = 2;
		p.add(new JLabel("" + t.getLength()), gbc);
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
