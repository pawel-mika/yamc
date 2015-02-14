package pl.wcja.yamc.gui;

import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JMenuBar;

import javazoom.jl.player.advanced.jlap;
import pl.wcja.yamc.event.BufferMixedEvent;
import pl.wcja.yamc.event.MixerListener;
import pl.wcja.yamc.event.SourceMixerChangedEvent;
import pl.wcja.yamc.frame.IMainFrame;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class MFMenuBar extends JMenuBar implements MixerListener {
	
	private IMainFrame mf = null;
	private JLabel jLabelMixerBuffer = new JLabel(".");
	private JLabel jLabelMixerRate = new JLabel(".");
	private JLabel jLabelMixerName = new JLabel(".");
	
	public MFMenuBar(IMainFrame mf) {
		this.mf = mf;
	}
	
	public void initMixerInfo() {
		this.add(Box.createHorizontalGlue());
		this.add(jLabelMixerBuffer);
		this.add(jLabelMixerRate);
		this.add(jLabelMixerName);
		mf.getMixer().addMixerListener(this);
	}
	
	private void updateMixerInfo(Mixer mixer, SourceDataLine sourceDataLine) {
		Info mixerInfo = mixer.getMixerInfo();
		//maybe add here some percentage buffer fill count updated every second?
		jLabelMixerBuffer.setText(String.format("B: %04db/%sb, ", sourceDataLine.available(), sourceDataLine.getBufferSize()));
		jLabelMixerRate.setText(String.format("R:%sHz, ", sourceDataLine.getFormat().getSampleRate()));
		jLabelMixerName.setText(String.format("%s", mixerInfo.getName()));
	}

	@Override
	public void bufferMixed(BufferMixedEvent e) {
		updateMixerInfo(mf.getMixer().getSourceMixer(), mf.getMixer().getSourceDataLine());
	}

	@Override
	public void sourceMixerChanged(SourceMixerChangedEvent e) {
		updateMixerInfo(e.getSourceMixer(), e.getSourceDataLine());
	}
 
}
