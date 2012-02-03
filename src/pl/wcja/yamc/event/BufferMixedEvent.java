package pl.wcja.yamc.event;

import java.util.EventObject;

import javax.sound.sampled.AudioFormat;

/**
 * 
 * @author:		<a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 * @date:		24-10-2011
 *
 */
public class BufferMixedEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5602605699819101821L;
	private AudioFormat mixFormat = null;
	private byte[] buffer = null;
	
	public BufferMixedEvent(Object source, AudioFormat mixFormat, byte[] buffer) {
		super(source);
		this.mixFormat = mixFormat;
		this.buffer = buffer;
	}
	
	public AudioFormat getMixFormat() {
		return mixFormat;
	}
	
	public byte[] getBuffer() { 
		return buffer;
	}

}
