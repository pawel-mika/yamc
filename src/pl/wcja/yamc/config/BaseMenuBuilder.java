package pl.wcja.yamc.config;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import pl.wcja.yamc.debug.DebugConfigPlugin;
import pl.wcja.yamc.debug.ReapeaksLoad;
import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.frame.MenuEntry;
import pl.wcja.yamc.frame.ToolBarEntry;
import pl.wcja.yamc.frame.tools.FollowMarker;
import pl.wcja.yamc.frame.tools.QuitApp;
import pl.wcja.yamc.frame.tools.ShowWaveEditor;
import pl.wcja.yamc.sound.edit.TuneEditorGridConfigPlugin;
import pl.wcja.yamc.sound.tools.AddTrack;
import pl.wcja.yamc.sound.tools.ImportReaperProject;
import pl.wcja.yamc.sound.tools.NewProject;
import pl.wcja.yamc.sound.tools.OpenProject;
import pl.wcja.yamc.sound.tools.ProjectProperities;
import pl.wcja.yamc.sound.tools.SaveProject;

/**
 * 
 * @author:		<a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 * @date:		09-02-2012
 *
 */
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
		addEntry(menuBar, new ReapeaksLoad(mf));
		
		addEntry(menuBar, new DebugConfigPlugin(mf));
		addEntry(menuBar, new FollowMarker(mf));
		
		//TODO Automate menu building using config files/plugins
	}
	
	private void addEntry(JMenuBar mb, MenuEntry me) {
		for(int i = 0; i < mb.getMenuCount(); i++) {
			JMenu menu = mb.getMenu(i);
			if(menu != null && me.getMenu().equalsIgnoreCase(menu.getText())) {
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
			mf.getToolbar().invalidate();
		}
	}
	
	private JMenuItem getJMenuItem(final MenuEntry me) {
		JMenuItem jmi = me.getMenuItem() == null ? 
				new JMenuItem(me.getEntryName()):
				me.getMenuItem();
		
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
