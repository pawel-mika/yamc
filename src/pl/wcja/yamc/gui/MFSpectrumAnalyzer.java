package pl.wcja.yamc.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

import pl.wcja.yamc.event.SpectrumAnalyzerEvent;
import pl.wcja.yamc.event.SpectrumAnalyzerListener;
import pl.wcja.yamc.frame.IMainFrame;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">pablo</a>, wcja.pl
 *
 */
public class MFSpectrumAnalyzer extends MFPanel implements SpectrumAnalyzerListener {

	private int fftSize = 0;
	private double[] fftBuffer = null;
	private double fft0dbValue = 0;
	private double fftDbMargin = 6;	//6db margin?
	private double bandWidth = 0;
	private AudioFormat audioFormat = mf.getMixer().getMixAudioFormat();
	
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
	private JPopupMenu popupMenu;
	
	/**
	 * 
	 * @param mf
	 */
	public MFSpectrumAnalyzer(IMainFrame mf) {
		super(mf);
		initialize();
		setPreferredSize(getMinimumSize());
	}

	private void initialize() {
		setSize(128, 64);
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
		
		if(fftBuffer == null) {
			return;
		}
		
		//paint 0db line
		String s0dB = "0dB";
		int line0db = (int)(fft0dbValue  * getHeight()) / getHeight();
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
	}
	
	private void paintLinear(Graphics g) {
		double[] toPaint = new double[fftSize];
		int c = 0;
		for(int i = 0; i < toPaint.length; i += 2) {
			toPaint[c] = (fftBuffer[i] * fftBuffer[i]) + (fftBuffer[i + 1] * fftBuffer[i + 1]);
 			toPaint[c] = (20 * Math.log10(toPaint[c])) / fftSize;
			toPaint[c] = (toPaint[c] / fft0dbValue) * getHeight();
			c++;
		}
		
		int barWidth = getWidth() / (fftSize / 2);
		if(barWidth <= 0) {
			barWidth = 1;
		}
				
		int fftIndex = 0, x = 0, y = 0;
		for(int i = 0; i < toPaint.length; i ++) {
			y  = getHeight() - (int)toPaint[fftIndex];
			g.setColor(Color.BLUE);
			g.fillRect(x, y, barWidth - 1, getHeight());
			g.setColor(Color.red);
			g.drawLine(x, y, x + barWidth - 2, y);
			fftIndex++;
			x+=barWidth;
		}	
	}
	
	private void paintLog(Graphics g) {
		double[] toPaint = new double[fftSize];
		int c = 0;
		for(int i = 0; i < toPaint.length; i += 2) {
			toPaint[c] = (fftBuffer[i] * fftBuffer[i]) + (fftBuffer[i + 1] * fftBuffer[i + 1]);
			toPaint[c] = (20 * Math.log10(toPaint[c])) / fftSize;
			toPaint[c] = (toPaint[c] / (fft0dbValue)) * getHeight();
			
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
			toPaint[c] = (fftBuffer[i] * fftBuffer[i]) + (fftBuffer[i + 1] * fftBuffer[i + 1]);
			toPaint[c] = (20 * Math.log10(toPaint[c])) / fftSize;
			toPaint[c] = (toPaint[c] / (fft0dbValue)) * getHeight();
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
		return new Dimension(fftSize, 32);
	}

	@Override
	public void spectrumCalculated(final SpectrumAnalyzerEvent e) {
		fft0dbValue = e.getFft0dbValue();
		fftSize = e.getChannelFFTs()[0].length;
		double[] sum = new double[e.getChannelFFTs()[0].length];
		int total = e.getChannelFFTs().length;
		for(int i = 0; i < total; i++) {
			for(int j = 0; j < e.getChannelFFTs()[i].length; j++) {
				sum[j] += (e.getChannelFFTs()[i][j] / total);
			}
		}
		fftBuffer = sum;
		bandWidth = e.getBarFrequencyWidth();
		repaint();		
	}
}