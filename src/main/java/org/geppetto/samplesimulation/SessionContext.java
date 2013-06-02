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
