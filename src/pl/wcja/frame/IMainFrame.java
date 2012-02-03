package pl.wcja.frame;

import javax.swing.JSplitPane;

import pl.wcja.gui.MFMenuBar;
import pl.wcja.gui.MFMixer;
import pl.wcja.gui.MFStatusStrip;
import pl.wcja.gui.MFToolBar;
import pl.wcja.sound.SpectrumAnalyzer;
import pl.wcja.sound.edit.TuneEditorGrid;
import pl.wcja.sound.edit.WaveEditor;

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
	
	public MFMixer getMixer();
		
	public MFStatusStrip getStatusStrip();
	
	public TuneEditorGrid getTuneEditor();
	
	public void setTuneTitle(String title);
	
	public WaveEditor getWaveEditor();
	
	public JSplitPane getMainSplitPane();

	public abstract SpectrumAnalyzer getSpectrumAnalyzer();
}