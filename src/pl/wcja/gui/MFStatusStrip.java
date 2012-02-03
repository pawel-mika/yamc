package pl.wcja.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import pl.wcja.frame.IMainFrame;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 * 
 */
public class MFStatusStrip extends MFPanel {
	private JLabel jlStatus;
	private JLabel jlPosition;
	private JLabel jlSelection;

	public MFStatusStrip(IMainFrame mf) {
		super(mf);
		init();
	}

	private void init() {
		setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));
		setLayout(new GridBagLayout());
		jlStatus = new JLabel("Status");
		jlPosition = new JLabel("Position");
		jlSelection = new JLabel("Selection");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.6D;
		gbc.weighty = 0.0D;
		add(jlStatus, gbc);
		gbc.gridx++;
		gbc.weightx = 0.2D;
		add(jlPosition, gbc);
		gbc.gridx++;
		gbc.weightx = 0.2D;
		add(jlSelection, gbc);
//		jlStatus.setBorder(BorderFactory.createMatteBorder(0,0,0,1,Color.DARK_GRAY));
//		jlCommand.setBorder(BorderFactory.createMatteBorder(0,0,0,1,Color.DARK_GRAY));
//		jlRequest.setBorder(BorderFactory.createLineBorder(Color.blue, 1));
		validate();
	}
	
	public void setPosition(String command) {
		setLabelStatus(jlPosition, command);
	}
	
	public void setSelection(String request) {
		setLabelStatus(jlSelection, request);
	}
	
	public void setStatus(String status) {
		setLabelStatus(jlStatus, status);
	}
	
	private void setLabelStatus(final JLabel label, final String msg) {
		if(SwingUtilities.isEventDispatchThread()) {
			label.setText(msg);
			validate();
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setLabelStatus(label, msg);
				}
			});
		}		
	}
}