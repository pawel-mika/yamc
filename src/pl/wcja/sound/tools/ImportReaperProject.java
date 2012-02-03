package pl.wcja.sound.tools;

import pl.wcja.frame.IMainFrame;
import pl.wcja.plugin.MenuEntryPlugin;

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
