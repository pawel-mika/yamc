package pl.wcja.yamc.frame.tools;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JToggleButton;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.plugin.ToolBarEntryPlugin;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class ShowWaveEditor extends ToolBarEntryPlugin {

	private JToggleButton jtbWaveEditor = null;
	private Dimension lastSize = null;
	private int lastDividerLocation = 0;
	
	public ShowWaveEditor(IMainFrame mf) {
		super(mf);
		jtbWaveEditor = new JToggleButton("WaveEditor");
		jtbWaveEditor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switchWaveEditor();
			}
		});
	}

	@Override
	public String getMenu() {
		return "View";
	}

	@Override
	public String getSubmenu() {
		return null;
	}

	@Override
	public String getEntryName() {
		return "Wave editor";
	}

	@Override
	public void entrySelected() {
		switchWaveEditor();
	}

	@Override
	public Component getToolbarComponent() {
		return jtbWaveEditor;
	}

	@Override
	public int getOffsetModifier() {
		return 0;
	}
	
	private void switchWaveEditor() {
		if(mf.getWaveEditor().isVisible()) {
			lastSize = mf.getWaveEditor().getSize();
			lastDividerLocation = mf.getMainSplitPane().getDividerLocation();
			mf.getWaveEditor().setVisible(false);
			getMenuItem().setSelected(false);
		} else {
			mf.getWaveEditor().setVisible(true);
			mf.getWaveEditor().setSize(lastSize);
			mf.getMainSplitPane().setDividerLocation(lastDividerLocation);
			getMenuItem().setSelected(true);
		}
		jtbWaveEditor.setSelected(mf.getWaveEditor().isVisible());
	}
}