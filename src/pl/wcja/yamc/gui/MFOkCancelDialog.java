package pl.wcja.yamc.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
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
	
	/**
	 * @param mf
	 * @param owner
	 * @param modal
	 */
	public MFOkCancelDialog(IMainFrame mf, Dialog owner, boolean modal) {
		super(mf, owner, modal);
		initialize();
		pack();
	}

	/**
	 * @param mf
	 * @param owner
	 * @param title
	 * @param modal
	 * @param gc
	 */
	public MFOkCancelDialog(IMainFrame mf, Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(mf, owner, title, modal, gc);
		initialize();
		pack();
	}

	/**
	 * @param mf
	 * @param owner
	 * @param title
	 * @param modal
	 */
	public MFOkCancelDialog(IMainFrame mf, Dialog owner, String title, boolean modal) {
		super(mf, owner, title, modal);
		initialize();
		pack();
	}

	/**
	 * @param mf
	 * @param owner
	 * @param title
	 */
	public MFOkCancelDialog(IMainFrame mf, Dialog owner, String title) {
		super(mf, owner, title);
		initialize();
		pack();
	}

	/**
	 * @param mf
	 * @param owner
	 */
	public MFOkCancelDialog(IMainFrame mf, Dialog owner) {
		super(mf, owner);
		initialize();
		pack();
	}

	/**
	 * @param mf
	 * @param owner
	 * @param modal
	 */
	public MFOkCancelDialog(IMainFrame mf, Frame owner, boolean modal) {
		super(mf, owner, modal);
		initialize();
		pack();
	}

	/**
	 * @param mf
	 * @param owner
	 * @param title
	 * @param modal
	 * @param gc
	 */
	public MFOkCancelDialog(IMainFrame mf, Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(mf, owner, title, modal, gc);
		initialize();
		pack();
	}

	/**
	 * @param mf
	 * @param owner
	 * @param title
	 * @param modal
	 */
	private MFOkCancelDialog(IMainFrame mf, Frame owner, String title, boolean modal) {
		super(mf, owner, title, modal);
		initialize();
		pack();
	}

	/**
	 * @param mf
	 * @param owner
	 * @param title
	 */
	private MFOkCancelDialog(IMainFrame mf, Frame owner, String title) {
		super(mf, owner, title);
		initialize();
		pack();
	}

	/**
	 * @param mf
	 * @param owner
	 */
	private MFOkCancelDialog(IMainFrame mf, Frame owner) {
		super(mf, owner);
		initialize();
		pack();
	}

	/**
	 * @param mf
	 * @param owner
	 * @param modalityType
	 */
	private MFOkCancelDialog(IMainFrame mf, Window owner, ModalityType modalityType) {
		super(mf, owner, modalityType);
		initialize();
		pack();
	}

	/**
	 * @param mf
	 * @param owner
	 * @param title
	 * @param modalityType
	 */
	private MFOkCancelDialog(IMainFrame mf, Window owner, String title, ModalityType modalityType) {
		super(mf, owner, title, modalityType);
		initialize();
		pack();
	}

	/**
	 * @param mf
	 * @param owner
	 * @param title
	 */
	private MFOkCancelDialog(IMainFrame mf, Window owner, String title) {
		super(mf, owner, title);
		initialize();
		pack();
	}

	/**
	 * @param mf
	 * @param owner
	 */
	public MFOkCancelDialog(IMainFrame mf, Window owner) {
		super(mf, owner);
		initialize();
		pack();
	}

	/**
	 * @param owner
	 * @param title
	 * @param modalityType
	 * @param gc
	 */
	public MFOkCancelDialog(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
		super(owner, title, modalityType, gc);
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
