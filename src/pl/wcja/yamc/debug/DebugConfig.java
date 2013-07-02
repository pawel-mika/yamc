package pl.wcja.yamc.debug;

public final class DebugConfig {

	private final static DebugConfig instance = new DebugConfig();

	private boolean debugPerTrackFetch = false;
	private boolean debugCompleteFetch = true;
	private boolean debugBufferMix = true;
	private boolean debugTuneEditorGridDrawing = true;
	
	public boolean isDebugTuneEditorGridDrawing() {
		return debugTuneEditorGridDrawing;
	}

	public boolean isDebugPerTrackFetch() {
		return debugPerTrackFetch;
	}

	public boolean isDebugCompleteFetch() {
		return debugCompleteFetch;
	}

	public boolean isDebugBufferMix() {
		return debugBufferMix;
	}

	public void setDebugTuneEditorGridDrawing(boolean debugTuneEditorGridDrawing) {
		this.debugTuneEditorGridDrawing = debugTuneEditorGridDrawing;
	}

	public void setDebugPerTrackFetch(boolean debugPerTrackFetch) {
		this.debugPerTrackFetch = debugPerTrackFetch;
	}

	public void setDebugCompleteFetch(boolean debugCompleteFetch) {
		this.debugCompleteFetch = debugCompleteFetch;
	}

	public void setDebugBufferMix(boolean debugBufferMix) {
		this.debugBufferMix = debugBufferMix;
	}

	public static DebugConfig getInstance() {
		return instance;
	}
	
	private DebugConfig() {
		
	}
	
}
