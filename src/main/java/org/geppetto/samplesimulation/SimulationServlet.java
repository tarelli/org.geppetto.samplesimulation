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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;
import org.geppetto.core.model.HHModel;
import org.geppetto.core.simulator.ISimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.google.gson.Gson;

public class SimulationServlet extends WebSocketServlet
{

	@Autowired
	public SimulationConfig config;

	@Autowired
	public ISimulator sampleSimulatorService;

	private static final long serialVersionUID = 1L;

	private static final long UPDATE_CYCLE = 100;

	private final AtomicInteger _connectionIds = new AtomicInteger(0);

	private final ConcurrentHashMap<Integer, SessionContext> _simulations = new ConcurrentHashMap<Integer, SessionContext>();

	private final ConcurrentHashMap<Integer, SimDataInbound> _connections = new ConcurrentHashMap<Integer, SimDataInbound>();

	private Timer _simTimer;

	@Override
	protected StreamInbound createWebSocketInbound(String subProtocol, HttpServletRequest request)
	{
		return new SimDataInbound(_connectionIds.incrementAndGet());
	}

	@Override
	public void init() throws ServletException
	{
		super.init();
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	private void startClientUpdateTimer()
	{
		_simTimer = new Timer(SimulationServlet.class.getSimpleName() + " - Timer - " + new java.util.Date().getTime());
		_simTimer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					update();
				}
				catch (RuntimeException e)
				{
					// log.error("Caught to prevent timer from shutting down", e);
				}
			}
		}, UPDATE_CYCLE, UPDATE_CYCLE);
	}

	private void update()
	{
		StringBuilder sb = new StringBuilder();
		for (Iterator<SessionContext> iterator = getSimulations().iterator(); iterator.hasNext();)
		{
			SessionContext simulation = iterator.next();

			if (simulation._models != null)
			{
				List<Float> xDataset = new ArrayList<Float>();
				List<Float> yDataset = new ArrayList<Float>();

				for (int t = 0; t < simulation._models.get("0").size(); t++)
				{
					// plot from 0
					if ((t * simulation._timeConfiguration.getTimeStepLength()) >= 0)
					{
						xDataset.add(t * config.getSamplingPeriod() * simulation._timeConfiguration.getTimeStepLength());
						yDataset.add(((HHModel) simulation._models.get("0").get(t)).getV());
					}
				}

				Gson gson = new Gson();
				// Put both objects in n array of 2 elements
				sb.append("[" + gson.toJson(xDataset) + "," + gson.toJson(yDataset) + "]");
			}
		}

		sendUpdate(sb.toString());
	}

	private void sendUpdate(String message)
	{
		for (SimDataInbound connection : getConnections())
		{
			try
			{
				CharBuffer buffer = CharBuffer.wrap(message);
				connection.getWsOutbound().writeTextMessage(buffer);
			}
			catch (IOException ignore)
			{
				// Ignore
			}
		}
	}

	private Collection<SimDataInbound> getConnections()
	{
		return Collections.unmodifiableCollection(_connections.values());
	}

	private Collection<SessionContext> getSimulations()
	{
		return Collections.unmodifiableCollection(_simulations.values());
	}

	private final class SimDataInbound extends MessageInbound
	{

		private final int id;
		private SessionContext _sessionContext;

		private SimDataInbound(int id)
		{
			super();
			this.id = id;
		}

		@Override
		protected void onOpen(WsOutbound outbound)
		{
			_sessionContext = new SessionContext();
			_simulations.put(Integer.valueOf(id), _sessionContext);
			_connections.put(Integer.valueOf(id), this);
			// tentative support for multiple connections but we'll have to see
			// how the osgi services can be instantiated
		}

		@Override
		protected void onClose(int status)
		{
			_simulations.remove(Integer.valueOf(id));
			_connections.remove(Integer.valueOf(id));
		}

		@Override
		protected void onBinaryMessage(ByteBuffer message) throws IOException
		{
			throw new UnsupportedOperationException("Binary message not supported.");
		}

		@Override
		protected void onTextMessage(CharBuffer message) throws IOException
		{
			String msg = message.toString();
			if (msg.equals("start"))
			{
				_sessionContext._runSimulation = true;
				new SimulationThread(_sessionContext, config, sampleSimulatorService).start();
				startClientUpdateTimer();
			}
			else if (msg.equals("stop"))
			{
				_sessionContext._runSimulation = false;
				_simTimer.cancel();
			}
			else if (msg.equals("reset"))
			{
				// stop simulation in case it's running
				if (_sessionContext._runSimulation)
				{
					_sessionContext._runSimulation = false;
					_simTimer.cancel();
				}

				// reset simulation parameters
				resetSimulationParams();
			}
			else
			{
				_sessionContext.setCurrent(Float.valueOf(msg));
			}
		}

		private void resetSimulationParams()
		{
			_sessionContext._models = null;
			_sessionContext._processedElements = 0;
			_sessionContext._externalCurrent = 0.0f;
		}

	}
}
