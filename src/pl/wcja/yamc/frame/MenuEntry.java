package pl.wcja.yamc.frame;

import javax.swing.JMenuItem;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public interface MenuEntry {

	public String getMenu();
	
	public String getSubmenu();
	
	public String getEntryName();
	
	public void entrySelected();
	
	public JMenuItem getMenuItem();
	
	public void setMenuItem(JMenuItem jmi);
}
