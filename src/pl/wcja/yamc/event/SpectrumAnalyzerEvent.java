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
	double fft0dbValue = 0;
	double barFrequencyWidth = 0;
	
	public SpectrumAnalyzerEvent(Object source, double[][] channelFFTs, double fft0dbValue, double barWidth) {
		super(source);
		this.channelFFTs = channelFFTs;
		this.fft0dbValue = fft0dbValue;
		this.barFrequencyWidth = barWidth;
	}

	public double[][] getChannelFFTs() {
		return channelFFTs;
	}
	
	public double getBarFrequencyWidth() {
		return barFrequencyWidth;
	}
	
	public double getFft0dbValue() {
		return fft0dbValue;
	}
}
