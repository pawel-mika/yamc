package pl.wcja.plugin;

import javax.swing.JMenuItem;

import pl.wcja.frame.IMainFrame;
import pl.wcja.frame.MenuEntry;

/**
 * 
 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe³ Mika</a>, Geomar SA
 *
 */
public abstract class MenuEntryPlugin extends BasePlugin implements MenuEntry {

	private JMenuItem jMenuItem = null;
	
	public MenuEntryPlugin(IMainFrame mf) {
		super(mf);
	}
	
	public MenuEntryPlugin(IMainFrame mf, JMenuItem menuItem) {
		super(mf);
		this.jMenuItem = menuItem;
	}

	public JMenuItem getMenuItem() {
		return jMenuItem;
	}
	
	public void setMenuItem(JMenuItem jmi) {
		this.jMenuItem = jmi;
	}
}