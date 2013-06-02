/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2011, 2013 OpenWorm.
 * http://openworm.org
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 *
 * Contributors:
 *     	OpenWorm - http://openworm.org/people.html
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/

package org.geppetto.samplesimulation;

import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.model.HHModel;
import org.geppetto.core.model.IModel;
import org.geppetto.core.simulation.ISimulation;
import org.geppetto.core.simulation.ISimulationCallbackListener;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulation.TimeConfiguration;
import org.geppetto.core.simulator.ISimulator;

class SimulationThread extends Thread implements ISimulation, ISimulatorCallbackListener
{

	private static Log logger = LogFactory.getLog(SimulationThread.class);

	private SessionContext sessionContext = null;

	public ISimulator sampleSimulatorService;

	public SimulationConfig config;

	public SimulationThread(SessionContext context, SimulationConfig config, ISimulator sampleSimulatorService)
	{
		this.sessionContext = context;
		this.config = config;
		this.sampleSimulatorService = sampleSimulatorService;
	}

	private SessionContext getSessionContext()
	{
		return sessionContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.geppetto.core.simulation.ISimulationCallbackListener
	 * #resultReady(java.util.List)
	 */
	@Override
	public void resultReady(final List<IModel> models)
	{
		// when the callback is received results are appended
		appendResults(models);
	}

	/**
	 * @param models
	 */
	private void appendResults(List<IModel> models)
	{
		String receivedId = models.get(0).getId();
		if (getSessionContext()._models == null)
		{
			getSessionContext()._models = new HashMap<String, List<IModel>>();
		}

		if (getSessionContext()._models.containsKey(receivedId))
		{
			// check if we have more steps than can be displayed
			if (getSessionContext()._models.get(receivedId).size() >= (config.getViewport() / config.getDt()) / config.getSamplingPeriod())
			{
				// if we have more steps that can be displayed - remove the
				// difference
				for (int i = 0; i < models.size(); i++)
				{
					// always remove the first - when removing everything gets shifted
					getSessionContext()._models.get(receivedId).remove(0);
				}
			}

			// add all the timesteps for the model
			getSessionContext()._models.get(receivedId).addAll(models);
		}
		else
		{
			getSessionContext()._models.put(receivedId, models);
		}

		getSessionContext()._processedElements = getSessionContext()._processedElements + 1;

		// NOTE: this needs to be set only when all the elements have been processed
		if (getSessionContext()._processedElements == config.getElemCount())
		{
			getSessionContext()._runningCycle = false;
		}
	}

	public void run()
	{
		while (getSessionContext()._runSimulation)
		{
			if (!getSessionContext()._runningCycle)
			{
				getSessionContext()._runningCycle = true;
				getSessionContext()._processedElements = 0;

				logger.debug("start simulation");
				int ELEM_COUNT = config.getElemCount();

				sampleSimulatorService.initialize(this);
				sampleSimulatorService.startSimulatorCycle();

				getSessionContext()._timeConfiguration = new TimeConfiguration((float) config.getDt(), config.getSteps(), config.getSamplingPeriod());

				// create the models to be simulated
				for (int j = 0; j < ELEM_COUNT; j++)
				{
					HHModel modelToSimulate;
					if (getSessionContext()._models == null)
					{
						// initial condition
						modelToSimulate = new HHModel(Integer.toString(j), config.getV(), config.getXn(), config.getXm(), config.getXh(), getSessionContext()._externalCurrent);
					}
					else
					{
						modelToSimulate = (HHModel) getSessionContext()._models.get(Integer.toString(j)).get(getSessionContext()._models.get(Integer.toString(j)).size() - 1);
						modelToSimulate.setI(getSessionContext()._externalCurrent);
					}

					// this is where the simulation hooks up with the solver
					sampleSimulatorService.simulate(modelToSimulate, getSessionContext()._timeConfiguration);
				}

				sampleSimulatorService.endSimulatorCycle();
				logger.debug("end simulation");
			}
		}
	}

	@Override
	public void init(URL simConfigURL, ISimulationCallbackListener simulationListener)
	{
		// NOT USED HERE
	}

	@Override
	public void reset()
	{
		// NOT USED HERE
	}
}