package pl.wcja.yamc.event;

import java.util.EventObject;

import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class SourceMixerChangedEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7280182079417732009L;
	private Mixer sourceMixer = null;
	private SourceDataLine sourceDataLine = null;
	
	public SourceMixerChangedEvent(Object source, Mixer sourceMixer, SourceDataLine sourceDataLine) {
		super(source);
		this.sourceMixer = sourceMixer;
		this.sourceDataLine = sourceDataLine;
	}
	
	public Mixer getSourceMixer() {
		return sourceMixer;
	}

	public SourceDataLine getSourceDataLine() {
		return sourceDataLine;
	}
	
}
