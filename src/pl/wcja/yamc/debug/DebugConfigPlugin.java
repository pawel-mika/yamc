package pl.wcja.yamc.debug;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.plugin.MenuEntryPlugin;

public class DebugConfigPlugin extends MenuEntryPlugin {

	public DebugConfigPlugin(IMainFrame mf) {
		super(mf);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getMenu() {
		return "Debug";
	}

	@Override
	public String getSubmenu() {
		return null;
	}

	@Override
	public String getEntryName() {
		return "Debug configuration...";
	}

	@Override
	public void entrySelected() {
		// TODO Auto-generated method stub

	}

}
