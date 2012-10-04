package org.openworm.simulationengine.samplesimulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openworm.simulationengine.core.simulation.ISimulation;
import org.openworm.simulationengine.core.simulator.ISimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * An simple implementation of a simulation controller. This
 * implementation is internal to this module and is not exported to other
 * bundles.
 */
@Controller
public class SampleSimulationController implements ISimulation {
	
	private static Log logger = LogFactory.getLog(SampleSimulationController.class);

	@Autowired
	public static SimulationConfig config;

	@Autowired
	public static ISimulator sampleSimulatorService;
	
	/* (non-Javadoc)
	 * @see org.openworm.simulationengine.core.simulation.ISimulation#run()
	 */
	@Override
	public void run() 
	{
		
	}
}


