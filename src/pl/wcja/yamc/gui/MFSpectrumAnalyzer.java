package pl.wcja.yamc.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
//	private double fft0dbValue = 0;
	private double dbUpperMargin = 6;		//6db margin?
	private double dbUpperMarginHeight = 0;	
	private int dbLabelMargin = 0;
	private double bandWidth = 0;
	private AudioFormat audioFormat = mf.getMixer().getMixAudioFormat();
	private JPopupMenu popupMenu;
	private double oneSecDraws = 0, avgOneSecFps = 0, targetOneSecFps = 25;
	private long lastFpsCalcTime = 0;
	private Image spectrumImage = null;
	
	private Color C_GRID = new Color(224,224,224);
	private Color C_GRID_LEGEND = Color.LIGHT_GRAY;

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
		//this.setToolTipText(String.format("Band width: %sHz",mf.getSpectrumAnalyzer().getBandWidth()));
	}

	/**
	 * 
	 */
	private void initialize() {
		setSize(256, 128);
		setBackground(Color.WHITE);
		setFont(new Font("Tahoma", Font.PLAIN, 10));
		
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
		g.setFont(getFont());
		FontMetrics fm = g.getFontMetrics(); 
		
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if(fftSumBuffer == null) {
			return;
		}

		paintGrid(g);
		
		if(fftViewMode == ViewMode.LINEAR) {
			paintLinear(g);
		} else if(fftViewMode == ViewMode.LOGARITHMIC) {
			paintSpectrumLog(g);
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

		g.setColor(Color.green);
		String sInfo = String.format("Avg 1 sec fps: %.2f", avgOneSecFps);
		int sy = getHeight() - fm.getDescent();
		g.drawString(sInfo, 1, sy);
		sInfo = String.format("Band width: %.4f Hz", mf.getSpectrumAnalyzer().getBandWidth());
		sy -= fm.getHeight();
		g.drawString(sInfo, 1, sy);
	}
	
	/**
	 * 
	 * @param g
	 */
	private void paintLinear(Graphics g) {
		if(drawType == DrawType.SUM) {
			paintFullBandLinearSum(g);
		} else if(drawType == DrawType.PER_CHANNEL) {
			paintFullBandLinearSum(g);	//test!
		}
	}
	
	/**
	 * paint the grid
	 * @param g
	 */
	private void paintGrid(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		//paint horizontal dB lines
		double scaley = getHeight() / mf.getSpectrumAnalyzer().getSNR(); 
		int y = 0;
		for(int i = 0; i <= mf.getSpectrumAnalyzer().getSNR(); i+= 10) {
			String sdB = String.format("%s dB", i > 0 ? -i : i);
			int linedB = (int)(i  * scaley);
			y = linedB;
			int stringWidth = fm.stringWidth(sdB);
			g.setColor(C_GRID_LEGEND);
			g.drawString(sdB, getWidth() - stringWidth, (int)(y - fm.getLineMetrics(sdB, g).getStrikethroughOffset()));
			g.setColor(C_GRID);
			g.drawLine(0, y, getWidth() - stringWidth - 2, y);
			dbLabelMargin = dbLabelMargin < stringWidth ? stringWidth : dbLabelMargin;
		}
		//paint Hz lines...
//		for(int x = 0; x <= getWidth() - dbLabelMargin; x += 10) {
//			
//		}
	}
	
	/**
	 * Draws a transparent image of full spectrum analysis 
	 * on a given graphics.
	 * 
	 * @param g2d
	 */
	private void paintFullBandLinearSum(Graphics g) {
		spectrumImage = new BufferedImage(fftSize / 2, getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D)spectrumImage.getGraphics();
		
		double scaley = getHeight() / mf.getSpectrumAnalyzer().getDbMaxValue();
		double[] toPaint = new double[fftSize];
		int c = 0;
		for(int i = 0; i < toPaint.length; i += 2) {
			toPaint[c] = fftSumBuffer[i] == 0 && fftSumBuffer[i + 1] == 0 ? 0 : (fftSumBuffer[i] * fftSumBuffer[i]) + (fftSumBuffer[i + 1] * fftSumBuffer[i + 1]);
			toPaint[c] = Decibels.linearToDecibels(Math.sqrt(toPaint[c])) / fftSize;
			toPaint[c] = Double.NEGATIVE_INFINITY == toPaint[c] ? 0 : toPaint[c] * scaley;
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
		g.drawImage(spectrumImage.getScaledInstance(getWidth() - dbLabelMargin, getHeight(), Image.SCALE_FAST), 0, 0, null);
	}

	/**
	 * 
	 * @param g
	 */
	private void paintSpectrumLog(Graphics g) {
		spectrumImage = new BufferedImage(fftSize, getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D)spectrumImage.getGraphics();
		
		double scaley = getHeight() / mf.getSpectrumAnalyzer().getDbMaxValue();
		double[] toPaint = new double[fftSize];
		int c = 0;
		for(int i = 0; i < toPaint.length; i += 2) {
			toPaint[c] = fftSumBuffer[i] == 0 && fftSumBuffer[i + 1] == 0 ? 0 : (fftSumBuffer[i] * fftSumBuffer[i]) + (fftSumBuffer[i + 1] * fftSumBuffer[i + 1]);
			toPaint[c] = Decibels.linearToDecibels(Math.sqrt(toPaint[c])) / fftSize;
 			toPaint[c] = Double.NEGATIVE_INFINITY == toPaint[c] ? 0 : toPaint[c] * scaley;
			c++;
		}
		
		int fftIndex = 0, x = 0, y = 0;
		int nextx = 0;
		double scale = (toPaint.length / 2) / Math.log10(toPaint.length / 2);
		for(int i = 0; i < toPaint.length; i++) {
			y = getHeight() - (int)toPaint[fftIndex];
			fftIndex++;
			x = (int)(scale * Math.log10(i + 1)) * 2;
			nextx = (int)(scale * Math.log10(i + 2)) * 2;
			g2d.setColor(Color.gray);
			g2d.fillRect(x, y, nextx - x + 1, getHeight());
			g2d.setColor(Color.red);
			g2d.drawLine(x, y, nextx, y);
		}		
		g2d.dispose();
		g.drawImage(spectrumImage.getScaledInstance(getWidth() - dbLabelMargin, getHeight(), Image.SCALE_FAST), 0, 0, null);
	}
	
	/**
	 * 
	 * @param g
	 */
	private void paintLog2(Graphics g) {
		double[] toPaint = new double[fftSize];
		int c = 0;
		double scaley = getHeight() / mf.getSpectrumAnalyzer().getDbMaxValue();
		for(int i = 0; i < toPaint.length; i += 2) {
			toPaint[c] = (fftSumBuffer[i] * fftSumBuffer[i]) + (fftSumBuffer[i + 1] * fftSumBuffer[i + 1]);
			toPaint[c] = (20 * Math.log10(Math.sqrt(toPaint[c]))) / fftSize;
			toPaint[c] = (toPaint[c] * scaley);
			c++;
		}
		
//		int barWidth = getWidth() / (fftSize / 2);
//		if(barWidth <= 0) {
//			barWidth = 1;
//		}
//
//		Vector<Double> averaged = new Vector<Double>();
//		double avg = 0;
//		int j = 1;
//		for(int i = 0; i < toPaint.length; i+=j) {
//			for(int b = i; b < j + i; b++) {
//				if(b >= toPaint.length) {
//					break;
//				}
//				avg += toPaint[b];
//			}
//			avg /= (j + 1);
//			averaged.add(avg);
//			j++;
//		}
//
//		barWidth = getWidth() / (averaged.size() - 1);
//		int x = 0, y = 0;
//		for(int i = 0; i < averaged.size(); i ++) {
//			y  = getHeight() - averaged.get(i).intValue();
//			g.setColor(Color.BLUE);
//			g.fillRect(x, y, barWidth - 1, getHeight());
//			g.setColor(Color.red);
//			g.drawLine(x, y, x + barWidth - 2, y);
//			x+=barWidth;
//		}
		Vector<Double> averaged = new Vector<>();
		int fftIndex = 0, x = 0, y = 0, ai = 0;
		int nextx = 0;
		double scale = (toPaint.length / 2) / Math.log10(toPaint.length / 2);
		for(int i = 0; i < toPaint.length; i++) {
			y = getHeight() - (int)toPaint[fftIndex];
			fftIndex++;
			x = (int)(scale * Math.log10(i + 1)) * 2;
			nextx = (int)(scale * Math.log10(i + 2)) * 2;
			
			
			
//			g2d.setColor(Color.gray);
//			g2d.fillRect(x, y, nextx - x + 1, getHeight());
//			g2d.setColor(Color.red);
//			g2d.drawLine(x, y, nextx, y);
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
}