package pl.wcja.yamc.event;

import java.util.EventObject;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">pablo</a>, wcja.pl
 *
 */
public class PlaybackEvent extends EventObject {

	private static final long serialVersionUID = 1027083202249426800L;

	public enum State {
		STOP("Stop", 0),
		PLAY("Play", 1),
		PAUSE("Pause", 2),
		FFW("Fast forward", 3),
		REW("Rewind", 4);
		
		private String name = "";
		private int value = 0;
		
		private State(String name, int value) {
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
	
	private State state = State.STOP;
	
	public PlaybackEvent(Object source, State state) {
		super(source);
		this.state = state;
	}

	public State getState() {
		return state;
	}
}
