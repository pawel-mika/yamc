package pl.wcja.gui;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import pl.wcja.frame.IMainFrame;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class MFPanel extends JPanel {

	protected IMainFrame mf = null;
	
	public MFPanel(IMainFrame mf) {
		this.mf = mf;
	}

	public MFPanel(IMainFrame mf, LayoutManager layout) {
		super(layout);
		this.mf = mf;
	}

	public MFPanel(IMainFrame mf, boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		this.mf = mf;
	}

	public MFPanel(IMainFrame mf, LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		this.mf = mf;
	}
}