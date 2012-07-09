package pl.wcja.yamc.frame;

import javax.swing.JSplitPane;

import pl.wcja.yamc.dsp.SpectrumAnalyzer;
import pl.wcja.yamc.gui.MFMenuBar;
import pl.wcja.yamc.gui.MFMixerPanel;
import pl.wcja.yamc.gui.MFStatusStrip;
import pl.wcja.yamc.gui.MFToolBar;
import pl.wcja.yamc.sound.edit.TuneEditorGrid;
import pl.wcja.yamc.sound.edit.WaveEditor;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public interface IMainFrame {
	
	public void quitApplication();
	
	public MFUndoManager getUndoManager();
	
	public MFMenuBar getMFMenuBar();
	
	public MFToolBar getToolbar();
	
	public MFMixerPanel getMixer();
		
	public MFStatusStrip getStatusStrip();
	
	public TuneEditorGrid getTuneEditor();
	
	public void setTuneTitle(String title);
	
	public WaveEditor getWaveEditor();
	
	public JSplitPane getMainSplitPane();

	public abstract SpectrumAnalyzer getSpectrumAnalyzer();
}