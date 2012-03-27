package org.openworm.simulationengine.samplesimulation3;

import org.junit.Test;
import org.openworm.simulationengine.samplesimulation.SampleSimulationController;


/**
 * JUnit test for the WelcomeController.
 */
public class SimulationControllTests {
	
	private SampleSimulationController controller = new SampleSimulationController();
	
	@Test
	public void testRun() {
		controller.run();
	}
}
