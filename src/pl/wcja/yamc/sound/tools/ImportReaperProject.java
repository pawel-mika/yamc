package pl.wcja.yamc.sound.tools;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.plugin.MenuEntryPlugin;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class ImportReaperProject extends MenuEntryPlugin {

	public ImportReaperProject(IMainFrame mf) {
		super(mf);
	}

	@Override
	public String getMenu() {
		return "File";
	}

	@Override
	public String getSubmenu() {
		return "Import";
	}

	@Override
	public String getEntryName() {
		return "Reaper";
	}

	@Override
	public void entrySelected() {
		importReaper();
	}
	
	private void importReaper() {
		
	}

}
