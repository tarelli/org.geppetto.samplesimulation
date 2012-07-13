package org.openworm.simulationengine.samplesimulation;

public class SimulationConfig {
	
	private int elemCount;
	private int steps;
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

	public void setSteps(int _steps) {
		this.steps = _steps;
	}

	public float getDt() {
		return dt;
	}

	public void setDt(float _dt) {
		this.dt = _dt;
	}

	public int getSamplingPeriod() {
		return samplingPeriod;
	}

	public void setSamplingPeriod(int _samplingPeriod) {
		this.samplingPeriod = _samplingPeriod;
	}

	public float getV() {
		return v;
	}

	public void setV(float _v) {
		this.v = _v;
	}

	public float getXn() {
		return xn;
	}

	public void setXn(float _xn) {
		this.xn = _xn;
	}

	public float getXm() {
		return xm;
	}

	public void setXm(float _xm) {
		this.xm = _xm;
	}

	public float getXh() {
		return xh;
	}

	public void setXh(float _xh) {
		this.xh = _xh;
	}
}
