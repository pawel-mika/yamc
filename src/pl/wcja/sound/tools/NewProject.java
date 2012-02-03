package pl.wcja.sound.tools;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

import pl.wcja.frame.IMainFrame;
import pl.wcja.plugin.MenuEntryPlugin;
import pl.wcja.sound.Track;
import pl.wcja.sound.Tune;

public class NewProject extends MenuEntryPlugin {

	public NewProject(IMainFrame mf) {
		super(mf);
	}

	@Override
	public void entrySelected() {
//		NewProjectDialog d = new NewProjectDialog(mf);
		createNewEmptyProject();
	}

	@Override
	public String getEntryName() {
		return "New...";
	}

	@Override
	public String getMenu() {
		return "File";
	}

	@Override
	public String getSubmenu() {
		return null;
	}
	
	private void createNewEmptyProject() {
		Tune t = new Tune(90, "Empty project");
		t.setViewFrom(0);
		t.setViewTo(16);
		t.addTrack(new Track("New track"));
		mf.getTuneEditor().setTune(t);
	}
	
	private class NewProjectDialog extends JDialog {
		
		private JButton jbOk = new JButton("Ok");
		private JButton jbCancel = new JButton("Cancel");
		
		public NewProjectDialog(IMainFrame mf) {
			super((JFrame)mf);
			
		}
		
	}

}
