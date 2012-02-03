package pl.wcja.config;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import pl.wcja.frame.IMainFrame;
import pl.wcja.frame.MenuEntry;
import pl.wcja.frame.ToolBarEntry;
import pl.wcja.frame.tools.QuitApp;
import pl.wcja.frame.tools.ShowWaveEditor;
import pl.wcja.gui.MFMixer;
import pl.wcja.sound.edit.TuneEditorGridConfigPlugin;
import pl.wcja.sound.tools.AddTrack;
import pl.wcja.sound.tools.ImportReaperProject;
import pl.wcja.sound.tools.NewProject;
import pl.wcja.sound.tools.OpenProject;
import pl.wcja.sound.tools.ProjectProperities;
import pl.wcja.sound.tools.SaveProject;

public class BaseMenuBuilder implements MenuBuilder {

	private IMainFrame mf = null;
	
	public BaseMenuBuilder(IMainFrame mf) {
		this.mf = mf;
	}

	@Override
	public void build(JMenuBar menuBar) {
		//narazie na sztywno...
		addEntry(menuBar, new NewProject(mf));
		addEntry(menuBar, new OpenProject(mf));
		addEntry(menuBar, new ImportReaperProject(mf));
		addEntry(menuBar, new SaveProject(mf));
		addEntry(menuBar, new ProjectProperities(mf));
		addEntry(menuBar, new QuitApp(mf));
		if(mf.getMixer() != null) {
			addEntry(menuBar, mf.getMixer());
		}
		addEntry(menuBar, new TuneEditorGridConfigPlugin(mf));
		addEntry(menuBar, new AddTrack(mf));
		addEntry(menuBar, new ShowWaveEditor(mf));
		
		//TODO zautomatyzowac budowanie na podstawie konfiga i pluginów
	}
	
	private void addEntry(JMenuBar mb, MenuEntry me) {
		for(int i = 0; i < mb.getMenuCount(); i++) {
			JMenu menu = mb.getMenu(i);
			if(me.getMenu().equalsIgnoreCase(menu.getText())) {
				if(me.getSubmenu() != null && !me.getSubmenu().isEmpty()) {
					for(Component c : menu.getMenuComponents()) {
						if(c instanceof JMenu) {
							JMenu submenu = ((JMenu)c);
							if(submenu.getText().equalsIgnoreCase(me.getSubmenu())) {
								submenu.add(getJMenuItem(me));
								return;
							}
						}
					}
					JMenu submenu = new JMenu(me.getSubmenu());
					submenu.add(getJMenuItem(me));
					menu.add(submenu);
					addToolbarComponent(me);
					return;
				} else if(me.getSubmenu() == null || me.getSubmenu().isEmpty()) {
					menu.add(getJMenuItem(me));
					addToolbarComponent(me);
					return;
				}
			}
		}
		JMenu jm = new JMenu(me.getMenu());
		if(me.getSubmenu() != null && !me.getSubmenu().isEmpty()) {
			JMenu jms = new JMenu(me.getSubmenu());
			jms.add(getJMenuItem(me));
			jm.add(jms);
		} else {
			jm.add(getJMenuItem(me));
		}
		mb.add(jm);
		addToolbarComponent(me);
	}
	
	private void addToolbarComponent(MenuEntry me) {
		if(me instanceof ToolBarEntry) {
			Component tc = ((ToolBarEntry)me).getToolbarComponent();
			mf.getToolbar().add(tc);//, mf.getToolbar().getComponents().length + ((ToolBarEntry)me).getOffsetModifier());
		}
	}
	
	private JMenuItem getJMenuItem(final MenuEntry me) {
		JMenuItem jmi = new JMenuItem(me.getEntryName());
		jmi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				me.entrySelected();
			}
		});
		me.setMenuItem(jmi);
		return jmi;
	}
}
