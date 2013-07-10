package pl.wcja.yamc.event;

import java.util.EventObject;

/**
 * 
 * @author:		<a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 * @date:		28-10-2011
 *
 */
public class SpectrumAnalyzerEvent extends EventObject {
	
	private static final long serialVersionUID = 2081582814746724690L;
	double[][] channelFFTs = null;
	double barFrequencyWidth = 0;
		
	public SpectrumAnalyzerEvent(Object source, double[][] channelFFTs, double barWidth) {
		super(source);
		this.channelFFTs = channelFFTs;
		this.barFrequencyWidth = barWidth;
	}

	public double[][] getChannelFFTs() {
		return channelFFTs;
	}
	
	public double getBarFrequencyWidth() {
		return barFrequencyWidth;
	}
}
