package pl.wcja.sound.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import pl.wcja.frame.IMainFrame;
import pl.wcja.plugin.ToolBarEntryPlugin;
import pl.wcja.sound.Track;
import pl.wcja.sound.Tune;

public class AddTrack extends ToolBarEntryPlugin {

	private JButton jbAddTrack = null;
	
	public AddTrack(IMainFrame mf) {
		super(mf);
		jbAddTrack = new JButton("Add track");
		jbAddTrack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addTrack();
			}
		});
	}

	@Override
	public int getOffsetModifier() {
		return 0;
	}

	@Override
	public Component getToolbarComponent() {
		return jbAddTrack;
	}

	@Override
	public void entrySelected() {
		addTrack();
	}

	@Override
	public String getEntryName() {
		return "Add track";
	}

	@Override
	public String getMenu() {
		return "Edit";
	}

	@Override
	public String getSubmenu() {
		return null;
	}
	
	private void addTrack() {
		Tune tune = mf.getTuneEditor().getTune();
		tune.addTrack(new Track(String.format("New track %s", tune.getTracks().size())));
		mf.getTuneEditor().rearrangeTracks();
		mf.getTuneEditor().repaint();
	}

}
