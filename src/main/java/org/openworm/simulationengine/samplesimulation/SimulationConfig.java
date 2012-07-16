package org.openworm.simulationengine.samplesimulation;

public class SimulationConfig {
	
	private int elemCount;
	private int steps;
	private int viewport;
	private float dt;
	private int samplingPeriod;

	private float v;
	private float xm;
	private float xn;
	private float xh;
	
	public int getElemCount() {
		return elemCount;
	}
	
	public void setElemCount(int elemCount) {
		this.elemCount = elemCount;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}
	
	public int getViewport() {
		return viewport;
	}

	public void setViewport(int viewport) {
		this.viewport = viewport;
	}

	public float getDt() {
		return dt;
	}

	public void setDt(float dt) {
		this.dt = dt;
	}

	public int getSamplingPeriod() {
		return samplingPeriod;
	}

	public void setSamplingPeriod(int samplingPeriod) {
		this.samplingPeriod = samplingPeriod;
	}

	public float getV() {
		return v;
	}

	public void setV(float v) {
		this.v = v;
	}

	public float getXn() {
		return xn;
	}

	public void setXn(float xn) {
		this.xn = xn;
	}

	public float getXm() {
		return xm;
	}

	public void setXm(float xm) {
		this.xm = xm;
	}

	public float getXh() {
		return xh;
	}

	public void setXh(float xh) {
		this.xh = xh;
	}
}
