package pl.wcja.plugin;

import javax.swing.JMenuItem;

import pl.wcja.frame.IMainFrame;
import pl.wcja.frame.ToolBarEntry;

/**
 * 
 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe³ Mika</a>, Geomar SA
 *
 */
public abstract class ToolBarEntryPlugin extends BasePlugin implements ToolBarEntry {

	private JMenuItem jMenuItem = null;

	public ToolBarEntryPlugin(IMainFrame mf) {
		super(mf);
	}
	
	public ToolBarEntryPlugin(IMainFrame mf, JMenuItem menuItem) {
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