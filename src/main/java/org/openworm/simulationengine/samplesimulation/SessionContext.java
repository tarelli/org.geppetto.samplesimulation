package org.openworm.simulationengine.samplesimulation;

import java.util.List;
import java.util.Map;

import org.apache.catalina.websocket.WsOutbound;
import org.openworm.simulationengine.core.model.IModel;
import org.openworm.simulationengine.core.simulation.ITimeConfiguration;

public class SessionContext {
	public ITimeConfiguration _timeConfiguration=null;

	public Map<String, List<IModel>> _models;

	//the external current is only temporary stored here, it should be part of the model
	public Float _externalCurrent;
	
	public int _processedElements = 0;

	public boolean _runningCycle=false;

	public boolean _runSimulation=false;


	public SessionContext() 
	{
        setCurrent(Float.valueOf(0));

	}

	public void setCurrent(Float externalCurrent) 
	{
		_externalCurrent=externalCurrent;
	}
}
