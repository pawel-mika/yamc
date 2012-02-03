package pl.wcja.yamc.sound.edit;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.plugin.MenuEntryPlugin;

/**
 * 
 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe³ Mika</a>, Geomar SA
 *
 */
public class TuneEditorGridConfigPlugin extends MenuEntryPlugin {

	public TuneEditorGridConfigPlugin(IMainFrame mf) {
		super(mf);
	}

	@Override
	public void entrySelected() {
		mf.getTuneEditor().configure();
	}

	@Override
	public String getEntryName() {
		return "Editor";
	}

	@Override
	public String getMenu() {
		return "Edit";
	}

	@Override
	public String getSubmenu() {
		return "Preferences";
	}

}
