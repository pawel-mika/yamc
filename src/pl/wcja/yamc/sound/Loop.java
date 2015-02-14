package pl.wcja.yamc.sound;

import java.io.Serializable;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class Loop implements Serializable {

	private double loopStart = 0;
	private double loopEnd = 0;
	/**
	 * 
	 */
	private Loop() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param loopStart
	 * @param loopEnd
	 */
	private Loop(double loopStart, double loopEnd) {
		super();
		this.loopStart = loopStart;
		this.loopEnd = loopEnd;
	}
	public double getLoopStart() {
		return loopStart;
	}
	public double getLoopEnd() {
		return loopEnd;
	}
	public void setLoopStart(double loopStart) {
		this.loopStart = loopStart;
	}
	public void setLoopEnd(double loopEnd) {
		this.loopEnd = loopEnd;
	}
		
}
