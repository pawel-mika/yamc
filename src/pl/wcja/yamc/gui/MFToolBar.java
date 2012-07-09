package pl.wcja.yamc.gui;

import java.awt.Component;

import javax.swing.JToolBar;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.frame.ToolBarEntry;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class MFToolBar extends JToolBar {

	protected IMainFrame mf = null; 
	
	public MFToolBar(IMainFrame mf) {
		this.mf = mf;
	}

	public MFToolBar(IMainFrame mf, int orientation) {
		super(orientation);
		this.mf = mf;
	}

	public MFToolBar(IMainFrame mf, String name) {
		super(name);
		this.mf = mf;
	}

	public MFToolBar(IMainFrame mf, String name, int orientation) {
		super(name, orientation);
		this.mf = mf;
	}
	
	public Component add(ToolBarEntry e) {
		return super.add(e.getToolbarComponent());
	}

}
