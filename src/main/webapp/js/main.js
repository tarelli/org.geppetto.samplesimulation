/**
 * @fileoverview sample neuronal simulation client
 *
 * @author matteo@openworm.org (Matteo Cantarelli)
 * @author giovanni@openworm.org (Giovanni Idili)
 */

var r;

function refreshChart(data)
{
	r.clear();
	// transparent == the third line is invisible
	var lines = r.linechart(10, 10, 300, 220,  [data[0],[0,100]],[data[1],[-25,120]], { nostroke: false, axis: "0 0 1 1", smooth: true,colors: ["#4d7804", "transparent"] });
}
	
function getDataset()
{
	$.ajax({type : 'GET',
			url : '/org.openworm.simulationengine.samplesimulation/getDataset',
			dataType: 'json',
			timeout : 5000,
			success : function(data, textStatus) { refreshChart(data); },
			error : function(xhr, textStatus, errorThrown) { alert("Error getting dataset!"); }
	});
}
	
function start() {
	$.ajax({type : 'GET',
			url : '/org.openworm.simulationengine.samplesimulation/startSimulation',
			timeout : 5000,
			success : function(data, textStatus) { t = setInterval(getDataset, 100); },
			error : function(xhr, textStatus, errorThrown) { alert("Error starting the simulation!"); }
	});		
}

function stop() {
	$.ajax({type : 'GET',
		url : '/org.openworm.simulationengine.samplesimulation/stopSimulation',
		timeout : 5000,
		success : function(data, textStatus) { },
		error : function(xhr, textStatus, errorThrown) { alert("Error stopping the simulation!"); }
	});

	t = clearInterval(t);
}
	
$(document).ready(function(){
	r= Raphael("holder"), txtattr = { font: "12px sans-serif" };
	
	$('#current').change(function(){
	var e = document.getElementById("current");
	var iext = e.options[e.selectedIndex].text;
	
		$.ajax({type : 'GET',
			url : '/org.openworm.simulationengine.samplesimulation/setCurrent?&iext='+iext,
			timeout : 5000,
			success : function(data, textStatus) { },
			error : function(xhr, textStatus, errorThrown) { alert("Error while changing the current: " + errorThrown); }
		});
	});
		
	$('#stop').attr('disabled', 'disabled');
	
	$('#start').click(function(){
		$('#start').attr('disabled', 'disabled');
		$('#stop').removeAttr('disabled');
		$('#imageHH').attr('style', 'visibility:visible');
		start();
	});
	
	$('#stop').click(function(){
		$('#start').removeAttr('disabled');
		$('#stop').attr('disabled', 'disabled');
		stop();
	});	
});