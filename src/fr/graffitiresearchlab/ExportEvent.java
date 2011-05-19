package fr.graffitiresearchlab;

public class ExportEvent {
	
	public boolean succeed;
	public float duration;
	
	/**
	 * ResultEvent contructor
	 * @param succeed
	 * @param duration
	 */
	public ExportEvent(boolean succeed, float duration) {
		this.succeed = succeed;
		this.duration = duration;
	}
}
