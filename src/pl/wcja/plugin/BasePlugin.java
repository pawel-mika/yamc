package pl.wcja.plugin;

import pl.wcja.frame.IMainFrame;

/**
 * 
 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe³ Mika</a>, Geomar SA
 *
 */
public class BasePlugin {
	
	protected IMainFrame mf = null;
	
	public BasePlugin(IMainFrame mf) {
		this.mf = mf;
	}
	
	
}
