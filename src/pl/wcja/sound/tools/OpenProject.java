package pl.wcja.sound.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import pl.wcja.frame.IMainFrame;
import pl.wcja.plugin.ToolBarEntryPlugin;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class OpenProject extends ToolBarEntryPlugin {

	private JButton jbOpen = null;
	
	public OpenProject(IMainFrame mf) {
		super(mf);
		jbOpen = new JButton("Open");
		jbOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadProject();
			}
		});
	}

	@Override
	public Component getToolbarComponent() {
		return jbOpen;
	}

	@Override
	public int getOffsetModifier() {
		return 0;
	}

	@Override
	public String getMenu() {
		return "File";
	}

	@Override
	public String getSubmenu() {
		return null;
	}

	@Override
	public String getEntryName() {
		return "Open project...";
	}

	@Override
	public void entrySelected() {
		loadProject();
	}
	
	private void loadProject() {
		
	}

}
