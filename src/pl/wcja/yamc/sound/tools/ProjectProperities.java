package pl.wcja.yamc.sound.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.gui.ProjectProperitiesDialog;
import pl.wcja.yamc.plugin.ToolBarEntryPlugin;
import pl.wcja.yamc.utils.DialogUtils;

public class ProjectProperities extends ToolBarEntryPlugin {

	public ProjectProperities(IMainFrame mf) {
		super(mf);
	}

	private JButton jbProp = null;
	
	@Override
	public Component getToolbarComponent() {
		jbProp = new JButton("Properities");
		jbProp.setAction(new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				entrySelected();
			}
		});
		return jbProp;
	}

	@Override
	public int getOffsetModifier() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMenu() {
		return "File";
	}

	@Override
	public String getSubmenu() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEntryName() {
		return "Properities";
	}

	@Override
	public void entrySelected() {
		ProjectProperitiesDialog d = new ProjectProperitiesDialog(mf);
		DialogUtils.centerDialog((Component)mf, d);
		d.setVisible(true);
	}

}
