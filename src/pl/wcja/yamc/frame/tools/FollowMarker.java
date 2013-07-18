package pl.wcja.yamc.frame.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.plugin.ToolBarEntryPlugin;
import pl.wcja.yamc.sound.edit.MarkerLocationChangedEvent;
import pl.wcja.yamc.sound.edit.SelectionChangedEvent;
import pl.wcja.yamc.sound.edit.TuneEditorGridListener;
import pl.wcja.yamc.sound.edit.VisibleAreaChangedEvent;

/**
 * Follow marker plugin
 *
 * @author:		<a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 * @date:		16 lip 2013 19:58:28
 *
 */
public class FollowMarker extends ToolBarEntryPlugin implements TuneEditorGridListener {

	private Action action = null;
	private JCheckBox tc = null;
	private JCheckBoxMenuItem jmi = null;
	
	public FollowMarker(IMainFrame mf) {
		super(mf);
		action = new FollowMarkerAction("Follow marker");
		tc = new JCheckBox(action);
		jmi = new JCheckBoxMenuItem(action);
		mf.getTuneEditor().addTuneEditorGridListener(this);
		setMenuItem(jmi);
		//emulate click on menu entry...
		jmi.setSelected(true);
		action.actionPerformed(new ActionEvent(jmi, ActionEvent.ACTION_PERFORMED, ""));
	}

	@Override
	public Component getToolbarComponent() {
		return tc;
	}

	@Override
	public int getOffsetModifier() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMenu() {
		return  "Tools"; 
	}

	@Override
	public String getSubmenu() {
		return  "";
	}

	@Override
	public String getEntryName() {
		return  "";
	}

	@Override
	public void entrySelected() {
	}

	@Override
	public void markerLocationChanged(MarkerLocationChangedEvent e) {
		if(jmi.isSelected() && !mf.getTuneEditor().isMarkerVisible()) {
			mf.getTuneEditor().moveTo(e.getTime(), 0.0);	
		}
	}

	@Override
	public void visibleAreaChanged(VisibleAreaChangedEvent e) {
	}

	@Override
	public void selectionChanged(SelectionChangedEvent e) {
	}
	
	/**
	 * 
	 *
	 * @author:		<a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
	 * @date:		17 lip 2013 14:13:00
	 *
	 */
	private class FollowMarkerAction extends AbstractAction {

		public FollowMarkerAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
		    if(e.getSource() == tc) {
				jmi.setSelected(tc.isSelected());
			} else if(e.getSource() == jmi) {
				tc.setSelected(jmi.isSelected());
			}
		}
		
	}
}
