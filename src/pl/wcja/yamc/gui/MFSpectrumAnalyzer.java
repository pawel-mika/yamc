package pl.wcja.yamc.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import pl.wcja.yamc.event.SpectrumAnalyzerEvent;
import pl.wcja.yamc.event.SpectrumAnalyzerListener;
import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.utils.Decibels;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">pablo</a>, wcja.pl
 *
 */
public class MFSpectrumAnalyzer extends MFPanel implements SpectrumAnalyzerListener {

	private Logger logger = Logger.getLogger(this.getClass());
	private int fftSize = 0;
	private double[][] fftPerChannelBuffer = null;
	private double[] fftSumBuffer = null;
	private double fft0dbValue = 0;
	private double fftDbMargin = 6;	//6db margin?
	private double bandWidth = 0;
	private AudioFormat audioFormat = mf.getMixer().getMixAudioFormat();
	private JPopupMenu popupMenu;
	private double oneSecDraws = 0, avgOneSecFps = 0;
	private long lastFpsCalcTime = 0;

	public enum ViewMode {
		LINEAR("Linear", 0),
		LOGARITHMIC("Logarithmic", 1),
		LOGARITHMIC_2("logarithmic type 2", 2);
		
		private String name = "";
		private int value = 0;
		
		private ViewMode(String name, int value) {
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
	private ViewMode fftViewMode = ViewMode.LINEAR;
	
	public enum DrawType {
		SUM("Sum of channels", 0),
		PER_CHANNEL("Channels separately", 1);
		
		private String name = "";
		private int value = 0;
		
		private DrawType(String name, int value) {
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
	private DrawType drawType = DrawType.SUM;
	
	/**
	 * 
	 * @param mf
	 */
	public MFSpectrumAnalyzer(IMainFrame mf) {
		super(mf);
		initialize();
		setPreferredSize(getMinimumSize());
		this.setToolTipText(String.format("Band width: %sHz",mf.getSpectrumAnalyzer().getBandWidth()));
	}

	private void initialize() {
		setSize(256, 128);
		setBackground(Color.WHITE);
//		setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

		mf.getSpectrumAnalyzer().addSpectrumAnalyzerListener(this);
				
		popupMenu = new JPopupMenu("Menu");
		setComponentPopupMenu(popupMenu);
		popupMenu.add(new AbstractAction("Linear") {
			@Override
			public void actionPerformed(ActionEvent e) {
				fftViewMode = ViewMode.LINEAR;
			}
		});
		popupMenu.add(new AbstractAction("Logarithmic") {
			@Override
			public void actionPerformed(ActionEvent e) {
				fftViewMode = ViewMode.LOGARITHMIC;
			}
		});
		popupMenu.add(new AbstractAction("Logarithmic type 2") {
			@Override
			public void actionPerformed(ActionEvent e) {
				fftViewMode = ViewMode.LOGARITHMIC_2;
			}
		});
//		popupMenu.addPopupMenuListener(this);
	}	
	
	
	@Override
	public void paint(Graphics g) {
//		super.paint(g);
		FontMetrics fm = g.getFontMetrics(); 
		
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if(fftSumBuffer == null) {
			return;
		}
		
		//paint 0db line
		String s0dB = "0dB";
		int line0db = (int)(fft0dbValue  * getHeight());
		g.setColor(Color.gray);
		g.drawLine(0, line0db, getWidth(), line0db);
		g.drawString(s0dB, getWidth() - fm.stringWidth(s0dB), (int)(fm.getStringBounds(s0dB, g).getHeight()));
		
		if(fftViewMode == ViewMode.LINEAR) {
			paintLinear(g);
		} else if(fftViewMode == ViewMode.LOGARITHMIC) {
			paintLog(g);
		} else if(fftViewMode == ViewMode.LOGARITHMIC_2) {
			paintLog2(g);
		}
		
		if(System.currentTimeMillis() - lastFpsCalcTime >= 1000) {
			avgOneSecFps = (oneSecDraws / (System.currentTimeMillis() - lastFpsCalcTime)) * 1000;
			logger.info("Average one second FPS: " + avgOneSecFps);
			oneSecDraws = 0;
			lastFpsCalcTime = System.currentTimeMillis();
		} else {
			oneSecDraws++;
		}
		String sFps = String.format("avg 1 sec fps: %.2f", avgOneSecFps);
		g.setColor(Color.green);
		g.drawString(sFps, 0, getHeight() - fm.getDescent());
	}
	
	private void paintLinear(Graphics g) {
		if(drawType == DrawType.SUM) {
			paintFullBandLinearSum(g);
		} else if(drawType == DrawType.PER_CHANNEL) {
			paintFullBandLinearSum(g);	//test!
		}
	}
	
	private void paintLog(Graphics g) {
		double[] toPaint = new double[fftSize];
		int c = 0;
		for(int i = 0; i < toPaint.length; i += 2) {
			toPaint[c] = (fftSumBuffer[i] * fftSumBuffer[i]) + (fftSumBuffer[i + 1] * fftSumBuffer[i + 1]);
			toPaint[c] = Decibels.linearToDecibels(toPaint[c]) / fftSize;
			toPaint[c] = (toPaint[c] * getHeight()) / fft0dbValue;
			c++;
		}
		
		int barWidth = getWidth() / (fftSize / 2);
		if(barWidth <= 0) {
			barWidth = 1;
		}

		Vector<Double> averaged = new Vector<Double>();
		boolean stop = false;
		double avg = 0, 
				freq = mf.getMixer().getMixAudioFormat().getSampleRate() / 2, 
				divider = 1.1;
		while(!stop) {
			double hiFreq = freq;
			double loFreq = freq / divider;
			freq = loFreq;
			int ih = freqToIndex((int)hiFreq);
			int il = freqToIndex((int)loFreq);
			for(int i = il; i < ih; i++) {
				avg += toPaint[i];
			}
			avg /= (ih - il + 1);
			averaged.add(avg);
			if(loFreq < bandWidth || loFreq < 10) {
				stop = true;
			}
		}
		
		barWidth = getWidth() / (averaged.size() - 1);
		int x = 0, y = 0;
		for(int i = averaged.size() - 1; i >= 0; i --) {
			y  = getHeight() - averaged.get(i).intValue();
			g.setColor(Color.BLUE);
			g.fillRect(x, y, barWidth - 1, getHeight());
			g.setColor(Color.red);
			g.drawLine(x, y, x + barWidth - 2, y);
			x+=barWidth;
		}
	}
	
	
	private void paintLog2(Graphics g) {
		double[] toPaint = new double[fftSize];
		int c = 0;
		for(int i = 0; i < toPaint.length; i += 2) {
			toPaint[c] = (fftSumBuffer[i] * fftSumBuffer[i]) + (fftSumBuffer[i + 1] * fftSumBuffer[i + 1]);
			toPaint[c] = (20 * Math.log10(toPaint[c])) / fftSize;
//			toPaint[c] = (toPaint[c] / (fft0dbValue)) * getHeight();
			toPaint[c] = (toPaint[c] * getHeight()) / fft0dbValue;
			c++;
		}
		
		int barWidth = getWidth() / (fftSize / 2);
		if(barWidth <= 0) {
			barWidth = 1;
		}

		Vector<Double> averaged = new Vector<Double>();
		double avg = 0;
		int j = 1;
		for(int i = 0; i < toPaint.length; i+=j) {
			for(int b = i; b < j + i; b++) {
				if(b >= toPaint.length) {
					break;
				}
				avg += toPaint[b];
			}
			avg /= (j + 1);
			averaged.add(avg);
			j++;
		}

		barWidth = getWidth() / (averaged.size() - 1);
		int x = 0, y = 0;
		for(int i = 0; i < averaged.size(); i ++) {
			y  = getHeight() - averaged.get(i).intValue();
			g.setColor(Color.BLUE);
			g.fillRect(x, y, barWidth - 1, getHeight());
			g.setColor(Color.red);
			g.drawLine(x, y, x + barWidth - 2, y);
			x+=barWidth;
		}
	}
	
	private int freqToIndex(int freq) {
		// special case: freq is lower than the bandwidth of spectrum[0]
		if (freq < bandWidth / 2)
			return 0;
		// special case: freq is within the bandwidth of spectrum[512]
		if (freq > audioFormat.getSampleRate() / 2 - bandWidth / 2)
			return 512;
		// all other cases
		float fraction = freq / audioFormat.getSampleRate();
		int i = Math.round(fftSize * fraction);
		return i;
	}
		
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(fftSize == 0 ? 256 : fftSize, 64);
	}

	@Override
	public void spectrumCalculated(final SpectrumAnalyzerEvent e) {
		fft0dbValue = e.getFft0dbValue();
		fftSize = e.getChannelFFTs()[0].length;
		fftPerChannelBuffer = e.getChannelFFTs();
		bandWidth = e.getBarFrequencyWidth();
		//calculate sum only if we really need it - save some time and CPU power;)
		if(drawType == DrawType.SUM) {	
			double[] sum = new double[e.getChannelFFTs()[0].length];
			int total = e.getChannelFFTs().length;
			for(int i = 0; i < total; i++) {
				for(int j = 0; j < e.getChannelFFTs()[i].length; j++) {
					sum[j] += (e.getChannelFFTs()[i][j] / total);
				}
			}
			fftSumBuffer = sum;
		} else if(drawType == DrawType.PER_CHANNEL) {
			fftSumBuffer = e.getChannelFFTs()[0];
		}
		repaint();		
	}
	
	/**
	 * Draws a transparent image of full spectrum analysis 
	 * on a given graphics.
	 * 
	 * @param g2d
	 */
	private void paintFullBandLinearSum(Graphics g) {
		Image img = new BufferedImage(fftSize / 2, getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D)img.getGraphics();
		
		double[] toPaint = new double[fftSize];
		int c = 0;
		for(int i = 0; i < toPaint.length; i += 2) {
			toPaint[c] = fftSumBuffer[i] == 0 && fftSumBuffer[i + 1] == 0 ? 0 : (fftSumBuffer[i] * fftSumBuffer[i]) + (fftSumBuffer[i + 1] * fftSumBuffer[i + 1]);
			toPaint[c] = Decibels.linearToDecibels(toPaint[c]) / fftSize;
 			toPaint[c] = Double.NEGATIVE_INFINITY == toPaint[c] ? 0 : (toPaint[c] / fft0dbValue) * getHeight();
			c++;
		}
		
		int barWidth = 1;
		int fftIndex = 0, x = 0, y = 0;
		for(int i = 0; i < toPaint.length; i ++) {
			y = getHeight() - (int)toPaint[fftIndex];
			g2d.setColor(Color.gray);
			g2d.drawLine(x, y, x, getHeight());
			g2d.setColor(Color.blue);
			g2d.drawLine(x, y, x, y);
			fftIndex++;
			x+=barWidth;
		}		
		g2d.dispose();
		g.drawImage(img.getScaledInstance(getWidth(), getHeight(), Image.SCALE_FAST), 0, 0, null);
	}
}