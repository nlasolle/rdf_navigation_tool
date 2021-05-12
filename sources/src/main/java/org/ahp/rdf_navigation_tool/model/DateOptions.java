package org.ahp.rdf_navigation_tool.model;

/**
 * Object storing date filtering options
 * @author Nicolas Lasolle
 *
 */
public class DateOptions {
	
	private int min;
	private int max;
	private int initialMin;
	private int initialMax;
	private int step;
	
	public DateOptions(int min, int max, int initialMin, int initialMax, int step) {
		this.min = min;
		this.max = max;
		this.initialMin = initialMin;
		this.initialMax = initialMax;
		this.step = step;
	}
	
	/**
	 * @return the min
	 */
	public int getMin() {
		return min;
	}
	/**
	 * @param min the min to set
	 */
	public void setMin(int min) {
		this.min = min;
	}
	/**
	 * @return the max
	 */
	public int getMax() {
		return max;
	}
	/**
	 * @param max the max to set
	 */
	public void setMax(int max) {
		this.max = max;
	}
	/**
	 * @return the initialMin
	 */
	public int getInitialMin() {
		return initialMin;
	}
	/**
	 * @param initialMin the initialMin to set
	 */
	public void setInitialMin(int initialMin) {
		this.initialMin = initialMin;
	}
	/**
	 * @return the initialMax
	 */
	public int getInitialMax() {
		return initialMax;
	}
	/**
	 * @param initialMax the initialMax to set
	 */
	public void setInitialMax(int initialMax) {
		this.initialMax = initialMax;
	}

	/**
	 * @return the step
	 */
	public int getStep() {
		return step;
	}

	/**
	 * @param step the step to set
	 */
	public void setStep(int step) {
		this.step = step;
	}
	
	
}
