package pl.wcja.frame.tools;

import pl.wcja.frame.IMainFrame;
import pl.wcja.plugin.MenuEntryPlugin;

/**
 * 
 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe³ Mika</a>, Geomar SA
 *
 */
public class QuitApp extends MenuEntryPlugin {

	public QuitApp(IMainFrame mf) {
		super(mf);
	}

	@Override
	public void entrySelected() {
		mf.quitApplication();
	}

	@Override
	public String getEntryName() {
		return "Quit application";
	}

	@Override
	public String getMenu() {
		return "File";
	}

	@Override
	public String getSubmenu() {
		return null;
	}
}