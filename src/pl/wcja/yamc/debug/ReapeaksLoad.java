package pl.wcja.yamc.debug;

import javax.swing.JFrame;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.plugin.MenuEntryPlugin;
import pl.wcja.yamc.sound.file.Reapeaks;
import pl.wcja.yamc.utils.DialogUtils;

public class ReapeaksLoad extends MenuEntryPlugin {

	public ReapeaksLoad(IMainFrame mf) {
		super(mf);
	}

	@Override
	public String getMenu() {
		return "Debug";
	}

	@Override
	public String getSubmenu() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEntryName() {
		return "Reapeaks load test";
	}

	@Override
	public void entrySelected() {
		try {
			Reapeaks rpk = new Reapeaks(DialogUtils.selectFile((JFrame)mf));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
