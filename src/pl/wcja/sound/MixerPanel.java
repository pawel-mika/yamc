package pl.wcja.sound;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import pl.wcja.frame.Configurable;
import pl.wcja.utils.DialogUtils;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class MixerPanel extends JPanel implements Configurable {

	protected Vector<Mixer> sourceMixers = new Vector<Mixer>();
	protected Vector<Mixer> targetMixers = new Vector<Mixer>(); 
	protected Mixer sourceMixer = null;
	protected Mixer targetMixer = null;
	protected int mixerMaxLines = 0;
	protected SourceDataLine defaultDataLine = null;
	protected int mixBufferSampleLen = 512;
	protected int outSamplerate = 44100;
	protected int outBitrate = 16;
	protected int outChannels = 2; 
	protected AudioFormat mixAudioFormat = new AudioFormat(outSamplerate, outBitrate, outChannels, true, false);
	
	/**
	 * 
	 */
	public MixerPanel() {
		super(new FlowLayout(FlowLayout.LEADING, 0, 0));
		initialize();
	}
	
	private void initialize() {
		//find source/targetMixers lists
		for(Info mi : AudioSystem.getMixerInfo()) {
			try {
				Mixer m = AudioSystem.getMixer(mi);
				m.open();
				Line.Info[] lines = m.getSourceLineInfo();
				if(lines.length > 0) {
					sourceMixers.add(m);
				}
				lines = m.getTargetLineInfo();
				if(lines.length > 0) {
					targetMixers.add(m);
				}
			} catch (LineUnavailableException e1) {
				e1.printStackTrace();
			}
		}

		sourceMixer = sourceMixers.get(0);
		targetMixer = targetMixers.get(targetMixers.size() - 1);
		
//		sourceMixer = AudioSystem.getMixer(null);
		try {
			sourceMixer.open();
			defaultDataLine = (SourceDataLine) sourceMixer.getLine(sourceMixer.getSourceLineInfo()[0]);
			mixerMaxLines = sourceMixer.getMaxLines(defaultDataLine.getLineInfo());
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public void play() {
		
	}
	
	public void stop() {
		
	}
	
	public double samplesToTime(double samples) {
		return samples / outSamplerate;
	}
	
	public double timeToSamples(double time) {
		return time * outSamplerate;
	}
	
	public int getMixBufferSampleLength() {
		return mixBufferSampleLen;
	}
	
	public AudioFormat getMixAudioFormat() {
		return mixAudioFormat;
	}
	
	public Mixer getSource() {
		return sourceMixer;
	}
	
	public Vector<Mixer> getSourceMixers() {
		return sourceMixers;
	}
	
	public void setSourceMixer(Mixer sm) {
		this.sourceMixer = sm;
	}
	
	public Mixer getTarget() {
		return targetMixer;
	}
	
	public Vector<Mixer> getTargetMixers() {
		return targetMixers;
	}
	
	public void setTargetMixer(Mixer tm) {
		this.targetMixer = tm;
	}
	
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
		
		JComboBox<Mixer> jcbSource = new JComboBox<Mixer>(sourceMixers);
		jcbSource.setSelectedItem(sourceMixer);
		jd.add(jcbSource, gbc);
		gbc.gridy+=1;
		
		gbc.gridx = 0;
		jl = new JLabel("Capture device: ");
		jd.add(jl, gbc);
		gbc.gridx+=1;
		
		JComboBox<Mixer> jcbTarget = new JComboBox<Mixer>(targetMixers);
		jcbTarget.setSelectedItem(targetMixer);
		jd.add(jcbTarget, gbc);
		gbc.gridy+=1;
		
		jcbSource.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				return super.getListCellRendererComponent(list, ((Mixer)value).getMixerInfo(), index, isSelected, cellHasFocus);
			}
		});
		
		jcbTarget.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				return super.getListCellRendererComponent(list, ((Mixer)value).getMixerInfo(), index, isSelected, cellHasFocus);
			}
		});
		
		jcbSource.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sourceMixer = (Mixer)((JComboBox)e.getSource()).getSelectedItem();
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
}