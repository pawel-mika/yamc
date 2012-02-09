package pl.wcja.yamc.dsp;

import java.util.LinkedList;

import javax.sound.sampled.AudioFormat;

import pl.wcja.yamc.event.BufferMixedEvent;
import pl.wcja.yamc.event.MixerListener;
import pl.wcja.yamc.event.PlaybackEvent;
import pl.wcja.yamc.event.PlaybackEvent.State;
import pl.wcja.yamc.event.PlaybackStatusListener;
import pl.wcja.yamc.event.SpectrumAnalyzerEvent;
import pl.wcja.yamc.event.SpectrumAnalyzerListener;
import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.utils.Decibels;
import pl.wcja.yamc.utils.SoundUtils;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * Collects mixed sound data until mixedBuffer lenght == fftSize and then
 * performs fft function on it, fires events, and finally clears the mixedbuffer
 * so it can collect new data.
 * 
 * @author:		<a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 * @date:		28-10-2011
 *
 */
public class SpectrumAnalyzer implements PlaybackStatusListener, MixerListener {

	protected IMainFrame mf = null;
	private int fftSize = 0;
	private int byteBufSize = 0;
	private byte[] mixedBuffer = new byte[0];
	private double barFrequencyWidth = 0;
	private double fft0dbValue = 0;
	private DoubleFFT_1D fft = null;
	private Thread analyzerThread = null;
	private AnalyzerRunnable analyzerRunnable = null;
	private AudioFormat audioFormat = null;
	
	private LinkedList<SpectrumAnalyzerListener> listeners = new LinkedList<SpectrumAnalyzerListener>();
	
	public enum WindowFunction {
		HAMMING("Hamming", 0),
		BLACKMANNHARRIS("Blackmann-Harris", 1);
		
		private String name = "";
		private int value = 0;
		
		private WindowFunction(String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;  
		}
		
		public int getValue() {
			return value;
		}
	}
	
	private WindowFunction windowFunction = WindowFunction.BLACKMANNHARRIS;
	
	public SpectrumAnalyzer(IMainFrame mf) {
		this.mf = mf;
		audioFormat = mf.getMixer().getMixAudioFormat();
		byteBufSize = 1024 * audioFormat.getFrameSize();
		fftSize = byteBufSize / audioFormat.getFrameSize();
		initialize();
	}

	private void initialize() {
		mf.getMixer().addPlaybackStatusListener(this);
		mf.getMixer().addMixerListener(this);
	}
	
	public void addSpectrumAnalyzerListener(SpectrumAnalyzerListener l) {
		synchronized (listeners) {
			listeners.add(l);
		}
	}
	
	public void removeSpectrumAnalyzerListener(SpectrumAnalyzerListener l) {
		synchronized (listeners) {
			listeners.remove(l);
		}
	}
	
	private void fireSpectrumEvent(SpectrumAnalyzerEvent e) {
		synchronized (listeners) {
			for(SpectrumAnalyzerListener l : listeners) {
				l.spectrumCalculated(e);
			}
		}
	}

	@Override
	public void playbackStatusChanged(PlaybackEvent e) {
		if(e.getState() == State.PLAY) {
			startAnalyzer();
		} else if(e.getState() == State.STOP) {
			stopAnalyzer();
		}
	}
	
	private void setupAnalyzer() {
		fft = new DoubleFFT_1D(fftSize);
		//calculate max
		double[] tmp = new double[fftSize];
		for(int i = 0; i < tmp.length; i++) {
			tmp[i] = Math.pow(2, audioFormat.getSampleSizeInBits() - 1) * (i % 2 == 0 ? 1 : -1);
		}
		fft.realForward(tmp);
		fft0dbValue = (tmp[0] * tmp[0] + tmp[1] * tmp[1]);
//		fft0dbValue = (10 * Math.log10(fft0dbValue))  / tmp.length;
		fft0dbValue = Decibels.linearToDecibels(fft0dbValue) / tmp.length;
	}
	
	private void startAnalyzer() {
		setupAnalyzer();
//		analyzerRunnable = new AnalyzerRunnable();
//		analyzerThread = new Thread(analyzerRunnable);
//		analyzerThread.setPriority(Thread.NORM_PRIORITY);
//		analyzerThread.start();
	}
	
	private void stopAnalyzer() {
		if(analyzerRunnable != null) {
			analyzerRunnable.interrupt();
		}
		if(analyzerThread != null) {
			analyzerThread.interrupt();	
		}
	}
	
	private void pauseAnalyzer() {
		
	}

	public double getBandWidth() {
//		return (audioFormat.getSampleRate() / 2) / fftSize;
		return audioFormat.getSampleRate() / fftSize;
	}
	
	public int freqToIndex(int freq) {
		// special case: freq is lower than the bandwidth of spectrum[0]
		if (freq < getBandWidth() / 2)
			return 0;
		// special case: freq is within the bandwidth of spectrum[512]
		if (freq > audioFormat.getSampleRate() / 2 - getBandWidth() / 2)
			return 512;
		// all other cases
		float fraction = freq / audioFormat.getSampleRate();
		int i = Math.round(fftSize * fraction);
		return i;
	}
	
	protected class AnalyzerRunnable implements Runnable {
		private boolean paused = false;
		private boolean interrupted = false;
		private double buffer[] = null; 
		private double fftBuffers[][] = null;
		
		@Override
		public void run() {
			//allocate buffers for per-channel analysis
			fftBuffers = new double[audioFormat.getChannels()][];
			//calculate bar frequency 
			barFrequencyWidth = getBandWidth();
			//start loop
			while(true && !paused) {
				if(interrupted) {
					return;
				}
//				if(mixedBuffer.length >= byteBufSize) {
//					System.out.println("**** mixed buf len: " + mixedBuffer.length);
//					buffer = SoundUtils.byteArrayToDoubleArray(mixedBuffer, 2, audioFormat.isBigEndian());
//					fftBuffers = SoundUtils.splitChannels(buffer, audioFormat.getChannels());
//					for(int c = 0; c < fftBuffers.length; c++) {
//						fftBuffers[c] = SoundUtils.applyBlackmannHarrisWindow(fftBuffers[c]);
//						fft.realForward(fftBuffers[c]);	
//					}
//					fireSpectrumEvent(new SpectrumAnalyzerEvent(this, fftBuffers, fft0dbValue, barFrequencyWidth));
//					mixedBuffer = new byte[0];
//				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void interrupt() {
			interrupted = true;
		}
		
		public void pause() {
			paused = !paused;
		}
		
		public boolean isInterrupted() {
			return interrupted;
		}
		
		public boolean isPaused() {
			return paused;
		}
	}

	@Override
	public void bufferMixed(BufferMixedEvent e) {
		audioFormat = e.getMixFormat();
		byte[] tmp = new byte[mixedBuffer.length + e.getBuffer().length];
		System.arraycopy(mixedBuffer, 0, tmp, 0, mixedBuffer.length);
		System.arraycopy(e.getBuffer(), 0, tmp, mixedBuffer.length, e.getBuffer().length);
		mixedBuffer = tmp;
				
		if(mixedBuffer.length >= byteBufSize) {
			double buffer[] = null; 
			double fftBuffers[][] = null;
			
			System.out.println("**** mixed buf len: " + mixedBuffer.length + ", performing FFT analysis...");
			buffer = SoundUtils.byteArrayToDoubleArray(mixedBuffer, 2, audioFormat.isBigEndian());
			fftBuffers = SoundUtils.splitChannels(buffer, audioFormat.getChannels());
			for(int c = 0; c < fftBuffers.length; c++) {
				fftBuffers[c] = SoundUtils.applyBlackmannHarrisWindow(fftBuffers[c]);
				fft.realForward(fftBuffers[c]);	
			}
			fireSpectrumEvent(new SpectrumAnalyzerEvent(this, fftBuffers, fft0dbValue, barFrequencyWidth));
			mixedBuffer = new byte[0];
		}
	}
}
