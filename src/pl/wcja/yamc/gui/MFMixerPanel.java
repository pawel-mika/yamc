package pl.wcja.yamc.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.sampled.Mixer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EtchedBorder;

import pl.wcja.yamc.dsp.MainMixer;
import pl.wcja.yamc.event.PlaybackEvent;
import pl.wcja.yamc.event.PlaybackEvent.State;
import pl.wcja.yamc.event.PlaybackStatusListener;
import pl.wcja.yamc.frame.Configurable;
import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.frame.ToolBarEntry;
import pl.wcja.yamc.utils.DialogUtils;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class MFMixerPanel extends MainMixer implements ToolBarEntry, Configurable, PlaybackStatusListener {
//	private Map<TrackItem, SourceDataLine> trackItemLines = new HashMap<TrackItem, SourceDataLine>();
	
	private JPanel jpMixer = new JPanel(new FlowLayout(FlowLayout.LEFT));
	private JButton jbPlay = new JButton("Play");
	private JButton jbStop = new JButton("Stop");
	private JToggleButton jtbLoop = new JToggleButton("Loop");
	
	public MFMixerPanel(IMainFrame mf) {
		super(mf);
		jpMixer.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		jbPlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(state == State.STOP || state == State.PAUSE) {
					play();	
				} else {
					pause();
				}
			}
		});
		jbStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();	
			}
		});
		jpMixer.add(jbPlay);
		jpMixer.add(jbStop);
		jpMixer.add(jtbLoop);
		addPlaybackStatusListener(this);
	}

	@Override
	public Component getToolbarComponent() {
		return jpMixer; 
	}

	@Override
	public int getOffsetModifier() {
		return 0;
	}

	@Override
	public String getMenu() {
		return "Edit";
	}

	@Override
	public String getSubmenu() {
		return "Preferences";
	}

	@Override
	public String getEntryName() {
		return "Mixer";
	}

	@Override
	public void entrySelected() {
		configure();
	}
	
	private JMenuItem jmi = null;
	
	@Override
	public JMenuItem getMenuItem() {
		return jmi;
	}

	@Override
	public void setMenuItem(JMenuItem jmi) {
		this.jmi = jmi;
	}
	
	@Override
	public void configure() {
		JDialog jd = new JDialog();
		jd.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.NORTHEAST;
		
		JLabel jl = new JLabel("Output device: ");
		jd.add(jl, gbc);
		gbc.gridx+=1;
		
		JComboBox jcbSource = new JComboBox(sourceMixers);
		
//		JComboBox
		jcbSource.setSelectedItem(getSourceMixer());
		jd.add(jcbSource, gbc);
		gbc.gridy+=1;
		
		gbc.gridx = 0;
		jl = new JLabel("Capture device: ");
		jd.add(jl, gbc);
		gbc.gridx+=1;
		
		JComboBox jcbTarget = new JComboBox(targetMixers);
		jcbTarget.setSelectedItem(targetMixer);
		jd.add(jcbTarget, gbc);
		gbc.gridy+=1;
		
		jcbSource.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				return super.getListCellRendererComponent(list, ((Mixer)value).getMixerInfo(), index, isSelected, cellHasFocus);
			}
		});
		
		jcbTarget.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				return super.getListCellRendererComponent(list, ((Mixer)value).getMixerInfo(), index, isSelected, cellHasFocus);
			}
		});
		
		jcbSource.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Mixer m = (Mixer)((JComboBox)e.getSource()).getSelectedItem();
				setSourceMixer(m);
			}
		});
		
		jcbTarget.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				targetMixer = (Mixer)((JComboBox)e.getSource()).getSelectedItem();
			}
		});
		
		gbc.gridx = 0;
		gbc.gridy = 10;
		gbc.gridwidth = 2;
		JPanel jp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton jbOk = new JButton("Ok");
		JButton jbCancel = new JButton("Cancel");
		jp.add(jbOk);
		jp.add(jbCancel);
		jd.add(jp, gbc);		
		
		jd.pack();
		DialogUtils.centerScreenDialog(jd);
		jd.setVisible(true);
	}

	@Override
	public void playbackStatusChanged(PlaybackEvent e) {
		switch(e.getState()) {
			case PLAY:
				jbPlay.setText("Pause");
				break;
			case PAUSE:
				jbPlay.setText("Play");
				break;
			default:
				jbPlay.setText("Play");
				break;
		}
	}
	
}
