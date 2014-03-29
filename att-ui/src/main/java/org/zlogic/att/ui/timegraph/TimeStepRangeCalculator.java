/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.timegraph;

import java.text.MessageFormat;
import java.util.NavigableMap;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Class for calculating the best time scale range
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class TimeStepRangeCalculator {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(TimeStepRangeCalculator.class.getName());
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	/**
	 * Minimum tick step
	 */
	private static final long minimumStep = 1000;//1 second

	/**
	 * List of TimeStep for specific time intervals
	 */
	NavigableMap<Long, TimeStep> nextStep = new TreeMap<>();

	/**
	 * Varying steps (e.g. milliseconds-seconds-minutes-hours) to use proper
	 * intervals since time is non-metric
	 */
	private class TimeStep {

		public TimeStep(long[] stepMultipliers, long nextStepMultiplier) {
			this.stepMultipliers = stepMultipliers;
			this.nextStepMultiplier = nextStepMultiplier;
		}
		private long stepMultipliers[];
		private long nextStepMultiplier;

		public long getNextStepMultiplier() {
			return nextStepMultiplier;
		}

		public long[] getStepMulipliers() {
			return stepMultipliers;
		}

		public long getStepMultiplier(int index) {
			return (index >= 0 && index < stepMultipliers.length) ? stepMultipliers[index] : -1;
		}
	};

	/**
	 * Constructs the TimeStepRangeCalculator
	 */
	public TimeStepRangeCalculator() {
		nextStep.put(0L, new TimeStep(new long[]{1, 2, 5}, 10));
		nextStep.put(1000L, new TimeStep(new long[]{1, 2, 5, 15, 20, 30}, 60));
		nextStep.put(1000 * 60 * 60L, new TimeStep(new long[]{1, 2, 3, 6, 9, 12}, 24));
		nextStep.put(1000 * 60 * 60L, new TimeStep(new long[]{1, 2, 5}, 10));
	}

	/**
	 * Returns the current TimeStep class
	 *
	 * @param step the current time step
	 * @return the TimeStep class matching a specific step
	 */
	private TimeStep getCurrentStep(long step) {
		return nextStep.floorEntry(step) != null ? nextStep.floorEntry(step).getValue() : nextStep.firstEntry().getValue();
	}

	/**
	 * Returns the optimal time step, larger than minStep
	 *
	 * @param minStep the minumum acceptable time step
	 * @return the optimal time step, larger than minStep
	 */
	public long getTicksStep(long minStep) {
		long step = minimumStep;
		int stepMultiplier = 0;
		//Iterate through possible scales
		for (;;) {
			TimeStep currentStep = getCurrentStep(step);
			if ((step * currentStep.getStepMultiplier(stepMultiplier)) > minStep || step >= Long.MAX_VALUE || step < 0) {
				step = step * currentStep.getStepMultiplier(stepMultiplier);
				break;
			}
			stepMultiplier++;
			if (currentStep.getStepMultiplier(stepMultiplier) < 0) {
				stepMultiplier = 0;
				step *= currentStep.getNextStepMultiplier();
			}
		}
		if (step < Long.MAX_VALUE && step > 0) {
			return step;
		} else {
			log.severe(MessageFormat.format(messages.getString("STEP_IS_OUT_OF_RANGE"), new Object[]{step}));
			return 0;
		}
	}
}
