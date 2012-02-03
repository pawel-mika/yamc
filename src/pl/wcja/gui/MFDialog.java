package pl.wcja.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;

import javax.swing.JDialog;

import pl.wcja.frame.IMainFrame;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public abstract class MFDialog extends JDialog {

	protected IMainFrame mf = null;
	
	public MFDialog(IMainFrame mf) {
		this.mf = mf;
	}

	public MFDialog(IMainFrame mf, Frame owner) {
		super(owner);
		this.mf = mf;
	}

	public MFDialog(IMainFrame mf, Dialog owner) {
		super(owner);
		this.mf = mf;
	}

	public MFDialog(IMainFrame mf, Window owner) {
		super(owner);
		this.mf = mf;
	}

	public MFDialog(IMainFrame mf, Frame owner, boolean modal) {
		super(owner, modal);
		this.mf = mf;
	}

	public MFDialog(IMainFrame mf, Frame owner, String title) {
		super(owner, title);
		this.mf = mf;
	}

	public MFDialog(IMainFrame mf, Dialog owner, boolean modal) {
		super(owner, modal);
		this.mf = mf;
	}

	public MFDialog(IMainFrame mf, Dialog owner, String title) {
		super(owner, title);
		this.mf = mf;
	}

	public MFDialog(IMainFrame mf, Window owner, ModalityType modalityType) {
		super(owner, modalityType);
		this.mf = mf;
	}

	public MFDialog(IMainFrame mf, Window owner, String title) {
		super(owner, title);
		this.mf = mf;
	}

	public MFDialog(IMainFrame mf, Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		this.mf = mf;
	}

	public MFDialog(IMainFrame mf, Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
		this.mf = mf;
	}

	public MFDialog(IMainFrame mf, Window owner, String title, ModalityType modalityType) {
		super(owner, title, modalityType);
		this.mf = mf;
	}

	public MFDialog(IMainFrame mf, Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		this.mf = mf;
	}

	public MFDialog(IMainFrame mf, Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		this.mf = mf;
	}

	public MFDialog(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
		super(owner, title, modalityType, gc);
		this.mf = mf;
	}
}