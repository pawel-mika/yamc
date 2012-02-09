package pl.wcja.yamc.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

import pl.wcja.yamc.config.BaseMenuBuilder;
import pl.wcja.yamc.db.Database;
import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.frame.MFUndoManager;
import pl.wcja.yamc.sound.SpectrumAnalyzer;
import pl.wcja.yamc.sound.Track;
import pl.wcja.yamc.sound.TrackItem;
import pl.wcja.yamc.sound.Tune;
import pl.wcja.yamc.sound.edit.MarkerLocationChangedEvent;
import pl.wcja.yamc.sound.edit.Selection;
import pl.wcja.yamc.sound.edit.SelectionChangedEvent;
import pl.wcja.yamc.sound.edit.TrackItemPanel;
import pl.wcja.yamc.sound.edit.TuneEditorGrid;
import pl.wcja.yamc.sound.edit.TuneEditorGridListener;
import pl.wcja.yamc.sound.edit.TuneEditorSelection;
import pl.wcja.yamc.sound.edit.VisibleAreaChangedEvent;
import pl.wcja.yamc.sound.edit.WaveEditorPanel;
import pl.wcja.yamc.sound.edit.WaveEditorPanelListener;
import pl.wcja.yamc.utils.DialogUtils;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class MainFrame extends JFrame implements IMainFrame, WindowListener, WaveEditorPanelListener, TuneEditorGridListener {

	private MFUndoManager mfUndoManager = null;
	private MFStatusStrip mfStatusStrip = null;
	private MFToolBar mfToolBar = null;
	private MFMenuBar mfMenuBar = null;
	private Database database = null;
	private WaveEditorPanel waveEditor = null;
	private TuneEditorGrid tuneEditor = null;
	private MFMixer mfMixer = null;
	private JSplitPane mainSplitPane = null;
	private SpectrumAnalyzer spectrumAnalyzer = null;
	
	public MainFrame() {
		super();
		initMainFrame();
		initTestToolbarButtons(); //REMOVE LATER...
		addWindowListener(this);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	}

	private void initMainFrame() {
		setSize(1024, 640);
		
		DialogUtils.centerScreenDialog(this);

		mfMenuBar = new MFMenuBar(this);
		mfMenuBar.add(new JMenu("File"));
		mfMenuBar.add(new JMenu("Edit"));
		mfMenuBar.add(new JMenu("View"));
		mfMenuBar.add(new JMenu("Help"));
		setJMenuBar(mfMenuBar);		
		
		mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		add(mainSplitPane, BorderLayout.CENTER);
		
		tuneEditor = new TuneEditorGrid(this);
		tuneEditor.setMinimumSize(new Dimension(320,200));
		tuneEditor.addTuneEditorGridListener(this);
		initTestTune();
		mainSplitPane.setTopComponent(tuneEditor);
		
		waveEditor = new WaveEditorPanel();
		mainSplitPane.setBottomComponent(waveEditor);
		waveEditor.addWaveformPanelListener(this);
		mainSplitPane.setDividerLocation(0.5d);	
		
		mfStatusStrip = new MFStatusStrip(this);
		add(mfStatusStrip, BorderLayout.SOUTH);
		
		mfToolBar = new MFToolBar(this, "Main toolbar");
		mfToolBar.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(mfToolBar, BorderLayout.NORTH);
		
		mfMixer = new MFMixer(this);
		mfToolBar.add(mfMixer);
		
		new BaseMenuBuilder(this).build(mfMenuBar);
		
		spectrumAnalyzer = new SpectrumAnalyzer(this);
		
		ChannelPowerMeter cp = new ChannelPowerMeter(this);
		cp.setPreferredSize(new Dimension(256,32));
		cp.setBackground(Color.white);
		mfToolBar.add(cp);
		
		mfUndoManager = new MFUndoManager(this);		
	}
	
	private void initTestTune() {
		Tune tt = new Tune(90, "default test tune...");
		tt.setViewFrom(-5);
		tt.setViewTo(65);
		for(int i = 0; i <= 3; i++) {
			Track t = new Track(String.format("Track %s", i));
			t.addItem(new TrackItem(t, 0, 10 + i * 2));
			tt.addTrack(t);
		}
		tuneEditor.setTune(tt);
	}
	
	/**
	 * DO WYWALENIA POZNIEJ....
	 */
	private void initTestToolbarButtons() {
		JButton bLoad = new JButton("Load file");
		mfToolBar.add(bLoad);
		bLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					File file = DialogUtils.selectFile(MainFrame.this);
					if(file != null) {
						waveEditor.setWaveFile(file);
						mfStatusStrip.setStatus(file.getAbsolutePath());
						for(Component c : tuneEditor.getComponents()) {
							if(c instanceof TrackItemPanel) {
								TrackItemPanel tip = (TrackItemPanel)c;
								tip.setWaveFile(file);
								tip.getTrackItem().setLenght(tip.getTotalSecondLength());
							}
						}
						tuneEditor.rearrangeTracks();
						tuneEditor.repaint();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		JButton bClearSelection = new JButton("Clear selection");
		mfToolBar.add(bClearSelection);
		bClearSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				waveEditor.clearSelection();
			}
		});
	}
	
	public void quitApplication() {
		if(database != null && database.isConnected()) {
			database.disconnect();
		}
		dispose();
		System.exit(0);
	}
	
	public MFUndoManager getUndoManager() {
		return mfUndoManager;
	}
	
	public MFMenuBar getMFMenuBar() {
		return mfMenuBar;
	}
	
	public MFToolBar getToolbar() {
		return mfToolBar;
	}
	
	public MFMixer getMixer() {
		return mfMixer;
	}
	
	public MFStatusStrip getStatusStrip() {
		return mfStatusStrip;
	}
	
	public TuneEditorGrid getTuneEditor() {
		return tuneEditor;
	}
	
	public void setTuneTitle(String tuneTitle) {
		setTitle("YAMC v 0.0.1a (c) Pablo - " + tuneTitle);
	}
	
	public WaveEditorPanel getWaveEditor() {
		return waveEditor;
	}
	
	@Override
	public SpectrumAnalyzer getSpectrumAnalyzer() {
		return spectrumAnalyzer;
	}
	
	public JSplitPane getMainSplitPane() {
		return mainSplitPane;
	}
	
	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		boolean b = DialogUtils.showYesNoDialog(this, "Do you wish to close application?", "R U SIUR?");
		if(b) {
			quitApplication();
		} else {
			//iconify albo do traya...
//			quitApplication();
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void markerLocationChanged(MarkerLocationChangedEvent e) {
		mfStatusStrip.setSelection(String.format("Marker location: %2.3f (%s)", e.getTime(), e.getUnit()));
		
	}

	@Override
	public void visibleAreaChanged(VisibleAreaChangedEvent e) {
		mfStatusStrip.setPosition(String.format("Viewing: %2.3f - %2.3f (%s)", e.getVisibleStart(), e.getVisibleEnd(), e.getUnit()));
	}

	@Override
	public void selectionChanged(SelectionChangedEvent e) {
		Selection s = e.getSelection();
		if(s != null && s instanceof TuneEditorSelection) {
			mfStatusStrip.setSelection(String.format("Selection: %s - %sb / %s - %ss", 
					getTuneEditor().secondToBeatAbsolute(s.getSelectionStart()), getTuneEditor().secondToBeatAbsolute(s.getSelectionEnd()),
					s.getSelectionStart(), s.getSelectionEnd()));
		} else {
			
		}
	}
}
