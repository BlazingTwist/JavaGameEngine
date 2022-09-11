package gamestate.states.perlindemo;

import logging.LogbackLoggerProvider;
import org.slf4j.Logger;

public class ProgressReporter {

	protected static final Logger logger = LogbackLoggerProvider.getLogger(ProgressReporter.class);

	public final int totalSteps;
	private int stepsComplete;
	private int previousPercentile;
	private long startTime;
	private long endTime;

	public ProgressReporter(int totalSteps) {
		this.totalSteps = totalSteps;
	}

	public void start() {
		stepsComplete = 0;
		startTime = System.currentTimeMillis();
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

		if (stepsComplete == totalSteps) {
			endTime = System.currentTimeMillis();
		}
	}

	public long getRuntimeMillis() {
		return endTime - startTime;
	}
}
