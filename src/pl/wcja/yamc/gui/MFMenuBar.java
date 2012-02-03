package pl.wcja.yamc.gui;

import javax.swing.JMenuBar;

import pl.wcja.yamc.frame.IMainFrame;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class MFMenuBar extends JMenuBar {
	
	private IMainFrame mf = null;
	
	public MFMenuBar(IMainFrame mf) {
		this.mf = mf;
	}

}
