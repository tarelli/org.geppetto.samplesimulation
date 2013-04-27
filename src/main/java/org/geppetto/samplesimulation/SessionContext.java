package org.geppetto.samplesimulation;

import java.util.List;
import java.util.Map;

import org.apache.catalina.websocket.WsOutbound;
import org.geppetto.core.model.IModel;
import org.geppetto.core.simulation.ITimeConfiguration;

public class SessionContext
{
	/*
	 * Simulation parameters
	 */
	public ITimeConfiguration _timeConfiguration = null;
	// the external current is only temporary stored here, it should be part of the model
	public Float _externalCurrent;
	
	/*
	 * Properties with stored simulation results
	 */
	public Map<String, List<IModel>> _models;
	public int _processedElements = 0;

	/*
	 * Simulation flags
	 */
	public boolean _runningCycle = false;
	public boolean _runSimulation = false;

	public SessionContext()
	{
		setCurrent(Float.valueOf(0));
	}

	public void setCurrent(Float externalCurrent)
	{
		_externalCurrent = externalCurrent;
	}
}
