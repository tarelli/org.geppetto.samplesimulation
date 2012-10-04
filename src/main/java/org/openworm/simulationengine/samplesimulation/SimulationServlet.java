package org.openworm.simulationengine.samplesimulation;

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
import org.openworm.simulationengine.core.model.HHModel;
import org.openworm.simulationengine.core.simulator.ISimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.google.gson.Gson;


public class SimulationServlet extends WebSocketServlet {

	private static final long serialVersionUID = 1L;

	private static final long UPDATE_CYCLE = 100;



	private final Timer _simTimer = new Timer(
			SimulationServlet.class.getSimpleName() + " Timer");

	private final AtomicInteger _connectionIds = new AtomicInteger(0);

	private final ConcurrentHashMap<Integer, SessionContext> _simulations = new ConcurrentHashMap<Integer, SessionContext>();

	private final ConcurrentHashMap<Integer, SimDataInbound> _connections = new ConcurrentHashMap<Integer, SimDataInbound>();

	@Override
	protected StreamInbound createWebSocketInbound(String subProtocol,
			HttpServletRequest request) {
		return new SimDataInbound(_connectionIds.incrementAndGet());
	}

	@Override
	public void init() throws ServletException {
		super.init();
		super.init();
		_simTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					update();
				} catch (RuntimeException e) {
					// log.error("Caught to prevent timer from shutting down",
					// e);
				}
			}
		}, UPDATE_CYCLE, UPDATE_CYCLE);

	}

	private void update() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<SessionContext> iterator = getSimulations().iterator(); iterator
				.hasNext();) {
			SessionContext simulation = iterator.next();

			if (simulation._models != null) {
				List<Float> xDataset = new ArrayList<Float>();
				List<Float> yDataset = new ArrayList<Float>();

				for (int t = 0; t < simulation._models.get("0").size(); t++) {
					// plot from 0
					if ((t * simulation._timeConfiguration.getTimeStepLength()) >= 0) {
						xDataset.add(t
								* SampleSimulationController.config.getSamplingPeriod()
								* simulation._timeConfiguration
										.getTimeStepLength());
						yDataset.add(((HHModel) simulation._models.get("0")
								.get(t)).getV());
					}
				}

				Gson gson = new Gson();
				sb.append("[" + gson.toJson(xDataset) + ","
						+ gson.toJson(yDataset) + "]"); // Put both objects in n
														// array of 2 elements

			}

			if (iterator.hasNext()) {
				sb.append(',');
			}
		}
		// sendUpdate(String.format("{'type': 'update', 'data' : [%s]}",
		sendUpdate(sb.toString());

	}

	private void sendUpdate(String message) {
		for (SimDataInbound connection : getConnections()) {
			try {
				CharBuffer buffer = CharBuffer.wrap(message);
				connection.getWsOutbound().writeTextMessage(buffer);
			} catch (IOException ignore) {
				// Ignore
			}
		}
	}

	private Collection<SimDataInbound> getConnections() {
		return Collections.unmodifiableCollection(_connections.values());
	}

	private Collection<SessionContext> getSimulations() {
		return Collections.unmodifiableCollection(_simulations.values());
	}

	private final class SimDataInbound extends MessageInbound {

		private final int id;
		private SessionContext _sessionContext;

		private SimDataInbound(int id) {
			super();
			this.id = id;
		}

		@Override
		protected void onOpen(WsOutbound outbound) {
			_sessionContext = new SessionContext();
			_simulations.put(Integer.valueOf(id), _sessionContext);
			_connections.put(Integer.valueOf(id), this);
			// tentative support for multiple connections but we'll have to see
			// how the osgi services can be instantiated

		}

		@Override
		protected void onClose(int status) {
			_simulations.remove(Integer.valueOf(id));
			_connections.remove(Integer.valueOf(id));
		}

		@Override
		protected void onBinaryMessage(ByteBuffer message) throws IOException {
			throw new UnsupportedOperationException(
					"Binary message not supported.");
		}

		@Override
		protected void onTextMessage(CharBuffer message) throws IOException {
			String msg = message.toString();
			if (msg.equals("start")) {
				new SimulationThread(_sessionContext).start();
			} else if (msg.equals("stop")) {
				_sessionContext._runSimulation = false;
				_sessionContext._runningCycle = false;
			} else {
				_sessionContext.setCurrent(Float.valueOf(msg));
			}
		}

	}
}
