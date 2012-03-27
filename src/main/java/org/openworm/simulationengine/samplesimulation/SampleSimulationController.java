package org.openworm.simulationengine.samplesimulation;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openworm.simulationengine.core.model.HHModel;
import org.openworm.simulationengine.core.model.IModel;
import org.openworm.simulationengine.core.simulation.ISimulation;
import org.openworm.simulationengine.core.simulation.ISimulationCallbackListener;
import org.openworm.simulationengine.core.simulation.TimeConfiguration;
import org.openworm.simulationengine.core.simulator.ISimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

/**
 * An simple implementation of a simulation controller. This
 * implementation is internal to this module and is not exported to other
 * bundles.
 */
@Controller
public class SampleSimulationController implements ISimulation {
	
	private static Log logger = LogFactory.getLog(SampleSimulationController.class);

	private static final String SESSION_CONTEXT_ID = "SESSION_CTX_ID";
	
	@Autowired
	private ISimulator sampleSimulatorService;

	private SimulationThread _sim=null;
	
	@RequestMapping(value="/setCurrent", method = RequestMethod.GET)
	public void setCurrent(HttpServletRequest request, String variation, OutputStream stream) throws Exception 
	{
		SessionContext sessionContext = (SessionContext)request.getSession(true).getAttribute(SESSION_CONTEXT_ID);
		
		if(sessionContext == null){
	        //Obtain the session object, create a new session if doesn't exist
	        HttpSession session = request.getSession(true);
	        session.setAttribute(SESSION_CONTEXT_ID, new SessionContext());
		}
		
		sessionContext._externalCurrent = Float.valueOf((String)request.getParameter("iext"));
	}
	
	
	class SimulationThread extends Thread implements ISimulationCallbackListener {
		
		private HttpSession _session = null;
		
		public SimulationThread(HttpSession session){
			_session = session;
		}
		
		private SessionContext getSessionContext(){
			return (SessionContext)_session.getAttribute(SESSION_CONTEXT_ID);
		}
		
		/* (non-Javadoc)
		 * @see org.openworm.simulationengine.core.simulation.ISimulationCallbackListener#resultReady(java.util.List)
		 */
		@Override
		public void resultReady(final List<IModel> models) 
		{
			//when the callback is received results are appended
			appendResults(models);
		}

		/**
		 * @param models
		 */
		private void appendResults(List<IModel> models) 
		{			
			String receivedId=models.get(0).getId();
			if(getSessionContext()._models==null)
			{
				getSessionContext()._models=new HashMap<String,List<IModel>>();
			}

			if(getSessionContext()._models.containsKey(receivedId))
			{
				if(getSessionContext()._models.get(receivedId).size()>10000)
				{
					for(int i=0;i<models.size();i++)
					{
						getSessionContext()._models.get(receivedId).remove(i);
					}

				}
				//add all the timesteps for the model
				getSessionContext()._models.get(receivedId).addAll(models);
			}
			else
			{
				getSessionContext()._models.put(receivedId,models);
			}
			
			getSessionContext()._runningCycle = false;
		}
		
        public void run() {
        	getSessionContext()._runSimulation=true;
        	
    		while(getSessionContext()._runSimulation)
    		{
    			if(!getSessionContext()._runningCycle)
    			{
    				getSessionContext()._runningCycle=true;
    				
    				logger.debug("start simulation");
    				int ELEM_COUNT = 30;

    				sampleSimulatorService.initialize(this);
    				sampleSimulatorService.startSimulatorCycle();

    				float START_TIME = -30;
    				float END_TIME = 20;
    				float dt = (float) 0.01;
    				int steps = (int) ((int) (END_TIME - START_TIME) / dt);
    				
    				getSessionContext()._timeConfiguration = new TimeConfiguration(dt, steps, 1);

    				// create the 302 models to be simulated
    				for (int j = 0; j < ELEM_COUNT; j++) {
    					HHModel modelToSimulate;
    					if(getSessionContext()._models==null)
    					{
    						//initial condition
    						modelToSimulate=new HHModel(Integer.toString(j),-10, 0, 0, 1, getSessionContext()._externalCurrent);
    					}
    					else
    					{
    						modelToSimulate=(HHModel)getSessionContext()._models.get(Integer.toString(j)).get(getSessionContext()._models.get(Integer.toString(j)).size()-1);
    						modelToSimulate.setV(getSessionContext()._externalCurrent);
    					}
    					
    					//this is where the simulation hooks up with the solver
    					sampleSimulatorService.simulate(modelToSimulate,getSessionContext()._timeConfiguration);			
    				}

    				sampleSimulatorService.endSimulatorCycle();
    				logger.debug("end simulation");		
    			}
    		}
        }
    }
	
	@RequestMapping(value="/startSimulation", method = RequestMethod.GET)
	public void startSimulation(HttpServletRequest request, String variation, OutputStream stream) throws Exception 
	{
		SessionContext sessionContext = (SessionContext)request.getSession().getAttribute(SESSION_CONTEXT_ID);
		
		if(sessionContext == null){
	        //Obtain the session object, create a new session if doesn't exist
	        HttpSession session = request.getSession(true);
	        session.setAttribute(SESSION_CONTEXT_ID, new SessionContext());
		}
        
		_sim = new SimulationThread(request.getSession());
		_sim.start();
	}
	
	@RequestMapping(value="/stopSimulation", method = RequestMethod.GET)
	public void stopSimulation(HttpServletRequest request, String variation, OutputStream stream) throws Exception 
	{
		SessionContext sc = (SessionContext)request.getSession().getAttribute(SESSION_CONTEXT_ID);
		sc._runSimulation=false;
		sc._runningCycle=false;
	}
	
	@RequestMapping(value="/getDataset", method = RequestMethod.GET)
	public void getDataset(HttpServletRequest request, String variation, HttpServletResponse response) throws Exception 
	{
		SessionContext session = (SessionContext)request.getSession().getAttribute(SESSION_CONTEXT_ID);
		if(session._models!=null)
		{
			float START_TIME = -30;
			// some dictionary for plotting
			Hashtable<Float, Float> V_by_t = new Hashtable<Float, Float>();

			for(int j = 0; j < session._models.get("0").size(); j++)
			{
				V_by_t.put(new Float(j*session._timeConfiguration.getTimeStepLength() + START_TIME), ((HHModel)session._models.get("0").get(j)).getV());
			}

			
			List<Float> xDataset=new ArrayList<Float>();
			List<Float> yDataset=new ArrayList<Float>();
			
			for (int t = 0; t <  session._models.get("0").size(); t++) {
				// plot from 0
				if((t*session._timeConfiguration.getTimeStepLength() + START_TIME) >= 0)
				{
					xDataset.add(t*session._timeConfiguration.getTimeStepLength() + START_TIME);
					yDataset.add(V_by_t.get(t*session._timeConfiguration.getTimeStepLength() + START_TIME));
				}
			}
			
			Gson gson = new Gson();


			response.setContentType("application/json"); 
			response.setCharacterEncoding("utf-8"); 
			String bothJson = "["+gson.toJson(xDataset)+","+gson.toJson(yDataset)+"]"; //Put both objects in an array of 2 elements
			response.getWriter().write(bothJson);
		}
	}
	
	@RequestMapping(value="/hh0.png", method = RequestMethod.GET)
	public void renderChart(HttpServletRequest request, String variation, OutputStream stream) throws Exception 
	{
//		SessionContext session = (SessionContext)request.getSession().getAttribute(SESSION_CONTEXT_ID);
//		if(session._models!=null)
//		{
//			float START_TIME = -30;
//			// some dictionary for plotting
//			Hashtable<Float, Float> V_by_t = new Hashtable<Float, Float>();
//
//			for(int j = 0; j < session._models.get("0").size(); j++)
//			{
//				V_by_t.put(new Float(j*session._timeConfiguration.getTimeStepLength() + START_TIME), ((HHModel)session._models.get("0").get(j)).getV());
//			}
//
//			// print some sampled charts to make sure we got fine-looking results.
//			// Plot results
//			XYSeries series = new XYSeries("HH_Graph");
//
//			for (int t = 0; t <  session._models.get("0").size(); t++) {
//				// plot from 0
//				if((t*session._timeConfiguration.getTimeStepLength() + START_TIME) >= 0)
//				{
//					series.add(t*session._timeConfiguration.getTimeStepLength() + START_TIME, V_by_t.get(t*session._timeConfiguration.getTimeStepLength() + START_TIME));
//				}
//			}
//
//			// Add the series to your data set
//			XYSeriesCollection dataset = new XYSeriesCollection();
//			dataset.addSeries(series);
//
//			JFreeChart chart = ChartFactory.createXYLineChart("HH Chart", "time", "Voltage", dataset, PlotOrientation.VERTICAL, true, true, false);
//			ChartUtilities.writeChartAsPNG(stream, chart, 500, 300);
//		}
	}

	/* (non-Javadoc)
	 * @see org.openworm.simulationengine.core.simulation.ISimulation#run()
	 */
	@Override
	public void run() 
	{
		
	}
}


