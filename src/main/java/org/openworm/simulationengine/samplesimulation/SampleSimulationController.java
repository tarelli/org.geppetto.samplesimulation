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
	private SimulationConfig config;
	
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
			String receivedId = models.get(0).getId();
			if(getSessionContext()._models==null)
			{
				getSessionContext()._models=new HashMap<String,List<IModel>>();
			}

			if(getSessionContext()._models.containsKey(receivedId))
			{
				// check if we have more steps than can be displayed
				if(getSessionContext()._models.get(receivedId).size() >= (config.getViewport() / config.getDt())/config.getSamplingPeriod())
				{
					// if we have more steps that can be displayed - remove the difference
					for(int i=0;i<models.size();i++)
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
				getSessionContext()._models.put(receivedId,models);
			}
			
			getSessionContext()._processedElements = getSessionContext()._processedElements + 1;
			
			// NOTE: this needs to be set only when all the elements have been processed
			if(getSessionContext()._processedElements == config.getElemCount() - 1){
				getSessionContext()._runningCycle = false;
			}
		}
		
        public void run() {
        	getSessionContext()._runSimulation=true;
        	
    		while(getSessionContext()._runSimulation)
    		{
    			if(!getSessionContext()._runningCycle)
    			{
    				getSessionContext()._runningCycle = true;
    				getSessionContext()._processedElements = 0;
    				
    				logger.debug("start simulation");
    				int ELEM_COUNT = config.getElemCount();

    				sampleSimulatorService.initialize(this);
    				sampleSimulatorService.startSimulatorCycle();
    				
    				getSessionContext()._timeConfiguration = new TimeConfiguration((float) config.getDt(), config.getSteps(), config.getSamplingPeriod());

    				// create the models to be simulated
    				for (int j = 0; j < ELEM_COUNT; j++) {
    					HHModel modelToSimulate;
    					if(getSessionContext()._models==null)
    					{
    						//initial condition
    						modelToSimulate=new HHModel(Integer.toString(j), config.getV(), config.getXn(), config.getXm(), config.getXh(), getSessionContext()._externalCurrent);
    					}
    					else
    					{
    						modelToSimulate=(HHModel)getSessionContext()._models.get(Integer.toString(j)).get(getSessionContext()._models.get(Integer.toString(j)).size()-1);
    						modelToSimulate.setI(getSessionContext()._externalCurrent);
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
			List<Float> xDataset=new ArrayList<Float>();
			List<Float> yDataset=new ArrayList<Float>();
			
			for (int t = 0; t <  session._models.get("0").size(); t++) 
			{
				// plot from 0
				if((t*session._timeConfiguration.getTimeStepLength()) >= 0)
				{
					xDataset.add(t*config.getSamplingPeriod()*session._timeConfiguration.getTimeStepLength());
					yDataset.add(((HHModel)session._models.get("0").get(t)).getV());
				}
			}
			
			Gson gson = new Gson();

			response.setContentType("application/json"); 
			response.setCharacterEncoding("utf-8"); 
			String bothJson = "[" + gson.toJson(xDataset) + "," + gson.toJson(yDataset)+ "]"; //Put both objects in an array of 2 elements
			response.getWriter().write(bothJson);
		}
	}

	/* (non-Javadoc)
	 * @see org.openworm.simulationengine.core.simulation.ISimulation#run()
	 */
	@Override
	public void run() 
	{
		
	}
}


