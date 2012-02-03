package pl.wcja.yamc.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import pl.wcja.yamc.frame.IMainFrame;

/**
 * 
 * @author:		<a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 * @date:		02-11-2011
 *
 */
public abstract class MFOkCancelDialog extends MFDialog {

	private JButton bOk = null;
	private JButton bCancel = null;
	
	public MFOkCancelDialog(IMainFrame mf) {
		super(mf);
		initialize();
		pack();
	}
	
	private void initialize() {
		bOk = new JButton(new AbstractAction("Ok") {
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				okClicked();
				dispose();
			}
		});
		bCancel = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				cancelCliked();
				dispose();
			}
		});
		setLayout(new BorderLayout());
		JPanel p = new JPanel(new FlowLayout());
		p.add(bOk);
		p.add(bCancel);
		add(p, BorderLayout.SOUTH);
	}
	
	protected abstract void okClicked();
	
	protected abstract void cancelCliked();

}
