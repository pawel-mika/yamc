package pl.wcja.yamc.plugin;

import pl.wcja.yamc.frame.IMainFrame;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">user</a>, wcja.pl
 *
 */
public class BasePlugin {
	
	protected IMainFrame mf = null;
	
	public BasePlugin(IMainFrame mf) {
		this.mf = mf;
	}
	
}
