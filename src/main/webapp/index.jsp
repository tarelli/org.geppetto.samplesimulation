<%-- <%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.openworm.simulationengine.samplesimulation.SampleSimulationController"%> --%>
<%-- <%
 
 ApplicationContext beanFactory =  WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
SampleSimulationController simulation =(SampleSimulationController)beanFactory.getBean("sampleSimulationController", SampleSimulationController.class);
 
String clickedButton = request.getParameter("command"); 
if (clickedButton != null) 
{ 
	simulation.run(); 
} 

%> --%>


<html>
<head>
<link rel="stylesheet" href="./js/style.css" media="screen" charset="utf-8">
<script src="./js/jquery-1.6.3.min.js"></script>
<script src="./js/raphael-min.js"></script>
<script src="./js/g.raphael-min.js"></script>
<script src="./js/g.line-min.js"></script>
<script type="text/javascript">
	var r ;
	function refreshChart(data)
	{
		 r.clear();
		 var lines = r.linechart(10, 10, 300, 220,  [data[0],[0,100]],[data[1],[-25,120]], { nostroke: false, axis: "0 0 1 1", smooth: true,colors: [
		                                                                                                                                            "#4d7804",       
		                                                                                                                                            "transparent"    // the third line is invisible
		                                                                                                                                          ] });
	}
	
	function getDataset()
	{
		$.ajax({type : 'GET',
                                   url : '/org.openworm.simulationengine.samplesimulation/getDataset',
									dataType: 'json',
                                   timeout : 5000,
                                   success : function(data, textStatus) {
										//alert("Dataset received!");
										refreshChart(data);
                                   },
                                   error : function(xhr, textStatus, errorThrown) {
                                       alert("Error getting dataset!");
                                   }
                               });
	}
	
	function start() {
		$.ajax({type : 'GET',
                                   url : '/org.openworm.simulationengine.samplesimulation/startSimulation',
                                   timeout : 5000,
                                   success : function(data, textStatus) {
										//alert("Simulation started!");
										t = setInterval(getDataset, 100);
                                   },
                                   error : function(xhr, textStatus, errorThrown) {
                                       alert("Error starting the simulation!");
                                   }
                               });
		
	}

	function stop() {
		$.ajax({type : 'GET',
                                   url : '/org.openworm.simulationengine.samplesimulation/stopSimulation',
                                   timeout : 5000,
                                   success : function(data, textStatus) {
										//alert("Simulation stopped!");
                                   },
                                   error : function(xhr, textStatus, errorThrown) {
                                       alert("Error stopping the simulation!");
                                   }
                               });
		t = clearInterval(t);
	}
	
	
	$(document).ready(function(){
		
        
		 r=Raphael("holder"),
		    txtattr = { font: "12px sans-serif" };
		
         
		$('#current').change(function(){
			var e = document.getElementById("current");
			var iext = e.options[e.selectedIndex].text;
			
			$.ajax({type : 'GET',
                                   url : '/org.openworm.simulationengine.samplesimulation/setCurrent?&iext='+iext,
                                   timeout : 5000,
                                   success : function(data, textStatus) {
										//alert("New current set: "+iext+"mA");
                                   },
                                   error : function(xhr, textStatus, errorThrown) {
                                       alert("Error while changing the current: " + errorThrown);
                                   }
                               });
		});
		
			$('#stop').attr('disabled', 'disabled');
	
			$('#start').click(function() 	
				{
				$('#start').attr('disabled', 'disabled');
				$('#stop').removeAttr('disabled');
				$('#imageHH').attr('style', 'visibility:visible');
				start();
				});
			
			$('#stop').click(function() 	
				{
				$('#start').removeAttr('disabled');
				$('#stop').attr('disabled', 'disabled');
				stop();
				});
		
	});
	
</script>
</head>
<body>
	<center>
		<img src="http://www.openworm.org/imgs/OpenWormLogo.png">

		<p>
			<b>OpenWorm Simulation Engine Prototype</b>

		</p>

		<p>OSGi/Spring powered platform</p>
		<p>Using Alpha Kernel to simulate 302 Hodgkin-Hukley Neurons (just
			one displayed)</p>
		<br />
		<form>
			<input id="start" type="button" value="Start Simulation" /> <input
				id="stop" type="button" value="Stop Simulation" /> I(mA): <select
				id="current">
				<option value="1">-10</option>
				<option value="2">-5</option>
				<option value="3" selected="selected">0</option>
				<option value="4">5</option>
				<option value="5">10</option>
				<option value="6">20</option>
			</select>
		</form>
		
		<div id="holder"></div>
	</center>

</body>
</html>