package pl.wcja.sound.edit;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;


public interface WaveEditor {

	public abstract void addWaveformPanelListener(WaveEditorPanelListener l);

	public abstract void removeWaveformPanelListener(WaveEditorPanelListener l);

	/**
	 * <p>
	 * Set a wave file contained in this WaveformPanel.
	 * 
	 * @param waveFile
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 */
	public abstract void setWaveFile(File waveFile) throws UnsupportedAudioFileException, IOException;

	/**
	 * Return the wave file contained in this panel
	 * 
	 * @return
	 */
	public abstract File getWaveFile();

	public abstract AudioFileFormat getAudioFileFormat();

	/**
	 * <p>
	 * Translates sample number to time
	 *  
	 * @param sample
	 * @return
	 */
	public abstract double sampleToSecond(double sample);

	/**
	 * <p>
	 * Get byte data from sampleOffset and lenght of sampleLen
	 * @param sampleOffset
	 * @param samplesLen
	 * @return
	 */
	public abstract byte[] getBytes(int sampleOffset, int samplesLen);

	/**
	 * <p>
	 * Get bytes of sample data into buf
	 * @param buf
	 * @param sampleOffset first sample
	 * @param samplesLen number of samples
	 */
	public abstract void getBytesInto(byte[] buf, int sampleOffset, int samplesLen);

	/**
	 * <p>
	 * Get byte data of whole visible area
	 * 
	 * @return
	 */
	public abstract byte[] getBytes();

	/**
	 * <p>
	 * Set marker location on a specified sample
	 * 
	 * @param sample
	 */
	public abstract void setMarkerLocation(double sample);

	/**
	 * <p>
	 * Clear the selection
	 */
	public abstract void clearSelection();

	/**
	 * <p>
	 * Set selection
	 * To clear selection use clearSelection instead.
	 * 
	 * @param selection
	 */
	public abstract void setSelection(Selection selection);

	/**
	 * <p>
	 * View a specified area of sample
	 * 
	 * @param sampleStart
	 * @param sampleEnd
	 */
	public abstract void pan(double sampleStart, double sampleEnd);

	/**
	 * <p>
	 * Returns the total lenght of contained file in seconds
	 * 
	 * @return
	 */
	public abstract double getTotalSecondLength();

	
	
	public void setVisible(boolean b);
	
	public boolean isVisible();
	
	public Dimension getSize();
	
	public void setSize(Dimension size);
	
}