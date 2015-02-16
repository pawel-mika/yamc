package pl.wcja.yamc.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import pl.wcja.yamc.event.SpectrumAnalyzerEvent;
import pl.wcja.yamc.event.SpectrumAnalyzerListener;
import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.utils.Decibels;

public class ChannelPowerMeter extends MFPanel implements SpectrumAnalyzerListener {

	private double fft0dbValue;
	private int fftSize;
	private double[] fftChannelPower;
	private double bandWidth;
	private AnalyzerDialog analyzerDialog = null;
	
	public enum Direction {
		VERTICAL("Vertical", 0),
		HORIZONTAL("Horizontal", 1);
		
		private String name = "";
		private int value = 0;
		
		private Direction(String name, int value) {
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
	
	private Direction direction = Direction.HORIZONTAL;

	public ChannelPowerMeter(IMainFrame mf) {
		super(mf);
		initialize();
	}

	private void initialize() {
		mf.getSpectrumAnalyzer().addSpectrumAnalyzerListener(this);
		
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					if(analyzerDialog != null && analyzerDialog.isDisplayable()) {
						analyzerDialog.setVisible(true);
					} else {
						analyzerDialog = new AnalyzerDialog(mf);
						analyzerDialog.setVisible(true);
					}
				} else if(e.getButton() == MouseEvent.BUTTON3) {
					switch(direction) {
						case VERTICAL:
							direction = Direction.HORIZONTAL;
							break;
						case HORIZONTAL:
							direction = Direction.VERTICAL;
							break;
					}
				}
			}
		});
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		if(fftChannelPower == null) {
			return;
		}
		double scaley = getHeight() / mf.getSpectrumAnalyzer().getDbMaxValue();
		double scalex = getWidth() / mf.getSpectrumAnalyzer().getDbMaxValue();
		if(direction == Direction.VERTICAL) {
			int barWidth = getWidth() / fftChannelPower.length;
			int x = 0, y = 0;
			for(int i = 0; i < fftChannelPower.length; i++) {
				y  = (int)(getHeight() - (fftChannelPower[i] * scaley));
				g.setColor(Color.gray);
				g.fillRect(x, y, barWidth - 1, getHeight());
				g.setColor(Color.red);
				g.drawLine(x, y, x + barWidth - 2, y);
				x+=barWidth;
			}
		} else if(direction == Direction.HORIZONTAL) {
			int barHeight = getHeight() / fftChannelPower.length;
			int barWidth = 0, y = 0;
			int zerodB = getWidth() - (int)(getWidth() * fft0dbValue);
			for(int i = 0; i < fftChannelPower.length; i++) {
				barWidth = (int)((fftChannelPower[i] * scalex));
				g.setColor(Color.gray);
				g.fillRect(0, y, barWidth, barHeight - 1);
				g.setColor(Color.blue);
				g.drawLine(zerodB, y, zerodB, y + barHeight - 2);
				g.setColor(Color.red);
				g.drawLine(barWidth, y, barWidth, y + barHeight - 2);
				y += barHeight;
			}
		}
	}
	
	@Override
	public void spectrumCalculated(SpectrumAnalyzerEvent e) {
		synchronized (e.getChannelFFTs()) {
			fftSize = e.getChannelFFTs()[0].length;
			fftChannelPower = new double[e.getChannelFFTs().length];
			double dNumber = 0;
			int total = e.getChannelFFTs().length;
			double scale = (fftSize / 2) / Math.log10(fftSize / 2);

			//try no.2
			for(int i = 0; i < total; i++) {
				for(int j = 0; j < e.getChannelFFTs()[i].length; j+=2) {
					double weight = (scale * Math.log10(j + 1)) * 2;
					dNumber = Math.pow(e.getChannelFFTs()[i][j], 2) + Math.pow(e.getChannelFFTs()[i][j + 1], 2);
					fftChannelPower[i] += dNumber / (weight == 0 ? 1 : weight);
				}
			}
			for(int i = 0; i < fftChannelPower.length; i++) {
				fftChannelPower[i] = Decibels.linearToDecibels(Math.sqrt(fftChannelPower[i])) / fftSize;
			}
		}
		repaint();
	}

}
