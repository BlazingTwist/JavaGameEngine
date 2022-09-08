package gamestate.states.perlindemo;

import logging.LogbackLoggerProvider;
import org.slf4j.Logger;

public class ProgressReporter {

	protected static final Logger logger = LogbackLoggerProvider.getLogger(ProgressReporter.class);

	public final int totalSteps;
	private int stepsComplete;
	private int previousPercentile;

	public ProgressReporter(int totalSteps) {
		this.totalSteps = totalSteps;
	}

	public void reset() {
		stepsComplete = 0;
	}

	public int getStepsComplete() {
		return stepsComplete;
	}

	public synchronized void stepComplete() {
		stepsComplete++;
		float progress = (float) stepsComplete / totalSteps;
		int percentile = (int) (progress * 100);
		if (percentile != previousPercentile) {
			logger.info("{}% complete! ({} / {})", percentile, stepsComplete, totalSteps);
		}
		previousPercentile = percentile;
	}
}
