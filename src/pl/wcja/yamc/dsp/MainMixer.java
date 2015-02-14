package pl.wcja.yamc.dsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import pl.wcja.yamc.debug.DebugConfig;
import pl.wcja.yamc.event.BufferMixedEvent;
import pl.wcja.yamc.event.MixerListener;
import pl.wcja.yamc.event.PlaybackEvent;
import pl.wcja.yamc.event.SourceMixerChangedEvent;
import pl.wcja.yamc.event.PlaybackEvent.State;
import pl.wcja.yamc.event.PlaybackStatusListener;
import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.gui.MFMixerPanel;
import pl.wcja.yamc.sound.Track;
import pl.wcja.yamc.sound.TrackItem;
import pl.wcja.yamc.sound.Tune;
import pl.wcja.yamc.sound.edit.TrackItemPanel;
import pl.wcja.yamc.utils.SoundUtils;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class MainMixer {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	protected IMainFrame mf = null;
	
	private long fetchTime = 0, completeFetchTime = 0, bufferMixTime = 0;
	
	private Thread playerThread = null;
	private PlayerRunnable playerRunnable = null;

	protected PlaybackEvent.State state = State.STOP;
	protected Vector<Mixer> sourceMixers = new Vector<Mixer>();
	protected Vector<Mixer> targetMixers = new Vector<Mixer>(); 
	private Mixer sourceMixer = null;
	protected Mixer targetMixer = null;
	protected int mixerMaxLines = 0;
	protected SourceDataLine defaultDataLine = null;
	protected int mixBufferSampleLen = 512;
	protected int outSamplerate = 44100;
	protected int outBitrate = 16;
	protected int outChannels = 2; 
	protected AudioFormat mixAudioFormat = new AudioFormat(outSamplerate, outBitrate, outChannels, true, false);
	
	private List<PlaybackStatusListener> playbackStatusListeners = new LinkedList<PlaybackStatusListener>();
	private List<MixerListener> mixerListeners = new LinkedList<MixerListener>();
	
	private TrackItem emptyTrackItem = new TrackItem(new Track("Empty track"), 0, 0);
	
	/**
	 * 
	 */
	public MainMixer(IMainFrame mf) {
		this.mf = mf;
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

		setSourceMixer(sourceMixers.get(0));
		targetMixer = targetMixers.get(targetMixers.size() - 1);
		
//		sourceMixer = AudioSystem.getMixer(null);
	}
		
	public void play() {
		if(!defaultDataLine.isOpen()) {
			playerRunnable = new PlayerRunnable();
			playerThread = new Thread(playerRunnable);
			playerThread.setPriority(Thread.MAX_PRIORITY);
			playerThread.start();
		}
		firePlaybackEvent(new PlaybackEvent(this, State.PLAY));
		fireSourceMixerChangedEvent(new SourceMixerChangedEvent(this, sourceMixer, defaultDataLine));
	}
	
	public void pause() {
		if(playerThread != null && playerRunnable != null && !playerRunnable.isInterrupted()) {
			playerRunnable.interrupt();
			playerThread.interrupt();
			defaultDataLine.drain();
			defaultDataLine.stop();
			defaultDataLine.close();
		}
		firePlaybackEvent(new PlaybackEvent(this, State.PAUSE));
		fireSourceMixerChangedEvent(new SourceMixerChangedEvent(this, sourceMixer, defaultDataLine));
	}
	
	public void stop() {
		if(playerThread != null && playerRunnable != null && !playerRunnable.isInterrupted()) {
			playerRunnable.interrupt();
			playerThread.interrupt();
			defaultDataLine.drain();
			defaultDataLine.stop();
			defaultDataLine.close();
//			for(SourceDataLine sdl : trackItemLines.values()) {
//				sdl.drain();
//				sdl.stop();
//			}
		} else if(playerThread != null && playerRunnable.isInterrupted()){
			//przy stopie drugi klik spowoduje reset pozycji markera...albo i nie:):P
			mf.getTuneEditor().setMarkerLocation(0);
		}
		firePlaybackEvent(new PlaybackEvent(this, State.STOP));
		fireSourceMixerChangedEvent(new SourceMixerChangedEvent(this, sourceMixer, defaultDataLine));
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
	
	public Mixer getSourceMixer() {
		return sourceMixer;
	}
	
	public Vector<Mixer> getSourceMixers() {
		return sourceMixers;
	}
	
	public SourceDataLine getSourceDataLine() {
		return defaultDataLine;
	}
	
	public void setSourceMixer(Mixer sm) {
		this.sourceMixer = sm;
		try {
			pause();
			if(defaultDataLine != null && defaultDataLine.isOpen()) {
				defaultDataLine.close();
			}
			if(sourceMixer != null && sourceMixer.isOpen()) {
				sourceMixer.close();
			}
			sourceMixer.open();
			defaultDataLine = (SourceDataLine) sourceMixer.getLine(sourceMixer.getSourceLineInfo()[0]);
			mixerMaxLines = sourceMixer.getMaxLines(defaultDataLine.getLineInfo());
			fireSourceMixerChangedEvent(new SourceMixerChangedEvent(this, this.sourceMixer, defaultDataLine));
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
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
	
	/**
	 * 
	 * @param position in seconds
	 * @param frames - samples to get (to get size in bytes have to be multiplied by byte per frame) 
	 * @return
	 */
	private Collection<FetchedSampleData> fetchTune(double position, int frames) {
		completeFetchTime = System.nanoTime();
		Tune tune = mf.getTuneEditor().getTune();
		if(tune != null) {
			synchronized(tune) {
				List<FetchedSampleData> out = new ArrayList<MFMixerPanel.FetchedSampleData>();
				for(Track t : tune.getTracks()) {
					for(TrackItem ti : t.getItems()) {
						TrackItemPanel tip = mf.getTuneEditor().getTrackItemPanel(ti);
						if(includeTrackItemPanelInMix(position, frames, tip)) {
							if(DebugConfig.getInstance().isDebugPerTrackFetch()) {
								fetchTime = System.nanoTime();
								logger.info(String.format("Fetching %s for %s samples from %s", tip.getWaveFile().getName(), frames, position));
							}
							byte[] buf = new byte[frames * mixAudioFormat.getFrameSize()];
							fetchTrackItemPanel(buf, tip, position, frames, mixAudioFormat.getFrameSize());
							out.add(new FetchedSampleData(ti, tip.getAudioFileFormat().getFormat().getChannels(), buf));
							if(DebugConfig.getInstance().isDebugPerTrackFetch()) {
								long l = System.nanoTime() - fetchTime;
								logger.info(String.format(" - done in: %sns (%s\u00B5s, %ss)", l, l / 1000, (double)l / 1000000));
							}
						} else {
							//get empty silent buffer...
//							byte[] buf = new byte[frames * mixAudioFormat.getFrameSize()];
//							out.add(new FetchedSampleData(ti, mixAudioFormat.getChannels(), buf));
						}
					}
				}
				if(DebugConfig.getInstance().isDebugCompleteFetch()) {
					long l = System.nanoTime() - completeFetchTime;
					logger.info(String.format("Complete fetching done in: %sns (%s\u00B5s, %sms, %ss)", l, l / 1000, (double)l / 1000000, (double)l / 1000000000));
				}
				//if no fetched data - add empty track to keep datalines busy;)
				if(out.isEmpty()) {
					out.add(new FetchedSampleData(emptyTrackItem, 2, new byte[frames * mixAudioFormat.getFrameSize()]));
				}
				return out;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param tip
	 * @param startTime
	 * @param frames
	 * @return
	 */
	private void fetchTrackItemPanel(byte[] fetchBuffer, TrackItemPanel tip, double startTime, int frames, int frameSize) {
		//moze przeliczyc wszystko na ramki zamiast czasu?
		double endTime = startTime + samplesToTime(frames);
		double sampleStart = tip.getTrackItem().getTimeFrom();
		double sampleEnd = tip.getTrackItem().getTimeTo();
		if(startTime < sampleStart && endTime >= sampleStart) {
			//dodajemy cisze z przodu bufora
			int tmpFrames = frames - (int)timeToSamples(sampleStart - startTime);
			byte[] tmpBuff = tip.getBytes(0, tmpFrames);
			System.arraycopy(tmpBuff, 0, fetchBuffer, (frames - tmpFrames) * frameSize, tmpFrames * frameSize);
//			printBuffer(fetchBuffer);
		} else if(startTime >= sampleStart && endTime <= sampleEnd) {
			//bierzemy calosc...
			double s = trackToItemTime(startTime, tip);
			tip.getBytesInto(fetchBuffer, (int)timeToSamples(s), frames);
		} else if(startTime <= sampleEnd && endTime > sampleEnd){
			//dodajemy cisze na koniec probki TODO - sprawdzic czemu tutaj nie wlazi? zly warunek?
			int tmpFrames = frames - (int)timeToSamples(sampleEnd - endTime);
			byte[] tmpBuff = tip.getBytes(0, tmpFrames);
			System.arraycopy(tmpBuff, 0, fetchBuffer, 0, tmpFrames);			
		}
	}
	
	/**
	 * 
	 * @param trackTime
	 * @param tip
	 * @return
	 */
	private double trackToItemTime(double trackTime, TrackItemPanel tip) {
		return trackTime - tip.getTrackItem().getTimeFrom();
	}
	
	/**
	 * do we need to include specified TrackItemPanel in current mixBuffer 
	 * @param position
	 * @param frames
	 * @param tpi
	 * @return
	 */
	private boolean includeTrackItemPanelInMix(double position, int frames, TrackItemPanel tpi) {
		if(tpi.getWaveFile() == null) {
			return false;
		}
		double positionEnd = position + tpi.sampleToSecond(frames);
		if(((position - tpi.getTrackItem().getTimeFrom()) >= 0 || (positionEnd - tpi.getTrackItem().getTimeFrom() >=0 )) && 
			((position - tpi.getTrackItem().getTimeTo() <= 0) || (positionEnd - tpi.getTrackItem().getTimeTo() <= 0))) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @author <a href="mailto:ketonal80@gmail.com">
	 *
	 */
	protected class FetchedSampleData {
		private TrackItem trackItem = null;
		int channelNo = 2;
		byte[] sampleData = null;
		public FetchedSampleData(TrackItem trackItem, int channelNo, byte[] sampleData) {
			super();
			this.trackItem = trackItem;
			this.channelNo = channelNo;
			this.sampleData = sampleData;
		}
		public int getChannelNo() {
			return channelNo;
		}
		public byte[] getSampleData() {
			return sampleData;
		}
		public TrackItem getTrackItem() {
			return trackItem;
		}
	}
	
	/**
	 * <p>
	 * Translate littleendian frame to double[]
	 * @param frame
	 * @param bytesPerChannel
	 * @param channels
	 * @return
	 */
	private double[] convertFrame(byte[] frame, int bytesPerChannel, int channels) {
		double[] sample = new double[channels];
		int idx = 0;
		for(int c = 0; c < channels; c++) {
			double value = 0;
			for(int b = 0; b < bytesPerChannel; b++) {
				value += (int)frame[idx] << (b * 8);
				idx++;
			}
			sample[c] = value;
		}
		return sample;
	}
	

	/**
	 * 
	 * @param buffer
	 * @param frameNo
	 * @param frameSize
	 * @return
	 */
	private byte[] getFrame(byte[] buffer, int frameNo, int frameSize) {
		byte[] frame = new byte[frameSize];
		System.arraycopy(buffer, frameNo * frameSize, frame, 0, frameSize);
		return frame;
	}

	private byte[] mixFetchedData(Collection<FetchedSampleData> fetched) {
		byte[][] test = new byte[fetched.size()][];
		int i = 0; 
		for(FetchedSampleData fsd : fetched) {
			test[i++] = fsd.sampleData;
		}
		return SoundUtils.mixdownBuffers(mixAudioFormat, test);
	}
	/**
	 * TODO poprawic to bo w pytkie nie tak jak powinno by� jest...
	 * 
	 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
	 *
	 */
	protected class PlayerRunnable implements Runnable {
		private boolean interrupted = false;
		@Override
		public void run() {
			try {
				defaultDataLine.open(new AudioFormat(outSamplerate, outBitrate, outChannels, true, false), mixBufferSampleLen * 8);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}

			while(true && !interrupted && defaultDataLine.isOpen()) {
				double d = mf.getTuneEditor().getMarkerPosition();
				double t = samplesToTime(mixBufferSampleLen);						
				mf.getTuneEditor().setMarkerLocation(d + t);

				Collection<FetchedSampleData> fetched = fetchTune(d - t, mixBufferSampleLen);
				if(DebugConfig.getInstance().isDebugBufferMix()) {
					bufferMixTime = System.nanoTime();
				}
				byte[] test = mixFetchedData(fetched);	//test new mixing using floating point mixer method
				if(DebugConfig.getInstance().isDebugBufferMix()) {
					long l = System.nanoTime() - bufferMixTime;
					logger.info(String.format("Fetched data (%s streams) mix done in: %sns (%s\u00B5s, %sms)", 
							fetched.size(), l, l / 1000, (double)l / 1000000));
				}
				fireMixerEvent(new BufferMixedEvent(this, mixAudioFormat, test));
				//
				defaultDataLine.write(test, 0, test.length);
				defaultDataLine.start();
				logger.info(String.format("----> bufferSize: %s, available to write: %s", defaultDataLine.getBufferSize(), defaultDataLine.available()));
				if(defaultDataLine.getBufferSize() == defaultDataLine.available()) {
					logger.error(" -- buffer underrun!!");
				} else if(defaultDataLine.available() == 0) {
					logger.error(" -- buffer overrunn!!");
				}
			}
		}
		
		public void interrupt() {
			this.interrupted = true;
		}
		
		public boolean isInterrupted() {
			return interrupted;
		}
	}
	
	public void addPlaybackStatusListener(PlaybackStatusListener l) {
		synchronized (playbackStatusListeners) {
			playbackStatusListeners.add(l);
		}
	}
	
	public void removePlaybackStatusListener(PlaybackStatusListener l) {
		synchronized (playbackStatusListeners) {
			playbackStatusListeners.remove(l);
		}
	}
	
	private void firePlaybackEvent(PlaybackEvent e) {
		synchronized (playbackStatusListeners) {
			for(PlaybackStatusListener l : playbackStatusListeners) {
				l.playbackStatusChanged(e);
			}
			state = e.getState();
		}
	}
	
	public void addMixerListener(MixerListener l) {
		synchronized (mixerListeners) {
			mixerListeners.add(l);
		}
	}
	
	public void removeMixerListener(MixerListener l) {
		synchronized (mixerListeners) {
			mixerListeners.remove(l);
		}
	}
	
	private void fireMixerEvent(BufferMixedEvent e) {
		synchronized (mixerListeners) {
			for(MixerListener l : mixerListeners) {
				l.bufferMixed(e);
			}
		}
	}
	
	private void fireSourceMixerChangedEvent(SourceMixerChangedEvent e) {
		synchronized (mixerListeners) {
			for(MixerListener l : mixerListeners) {
				l.sourceMixerChanged(e);
			}
		}
	}

}