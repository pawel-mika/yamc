package pl.wcja.yamc.event;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public interface MixerListener {
	
	public void bufferMixed(BufferMixedEvent e);
	
	public void sourceMixerChanged(SourceMixerChangedEvent e);
	
}
