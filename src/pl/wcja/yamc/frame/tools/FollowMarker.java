package pl.wcja.yamc.frame.tools;

import java.awt.Component;

import javax.swing.JCheckBox;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.plugin.ToolBarEntryPlugin;
import pl.wcja.yamc.sound.edit.MarkerLocationChangedEvent;
import pl.wcja.yamc.sound.edit.SelectionChangedEvent;
import pl.wcja.yamc.sound.edit.TuneEditorGridListener;
import pl.wcja.yamc.sound.edit.VisibleAreaChangedEvent;

/**
 * 
 *
 * @author:		<a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 * @date:		16 lip 2013 19:58:28
 *
 */
public class FollowMarker extends ToolBarEntryPlugin implements TuneEditorGridListener {

	private JCheckBox tc = null;
	
	public FollowMarker(IMainFrame mf) {
		super(mf);
		tc = new JCheckBox("Follow marker");
		mf.getTuneEditor().addTuneEditorGridListener(this);
		tc.setSelected(true);
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
		return  ""; 
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
		if(tc.isSelected() && !mf.getTuneEditor().isMarkerVisible()) {
			mf.getTuneEditor().moveTo(e.getTime(), 0.0);	
		}
	}

	@Override
	public void visibleAreaChanged(VisibleAreaChangedEvent e) {
	}

	@Override
	public void selectionChanged(SelectionChangedEvent e) {
	}

}
