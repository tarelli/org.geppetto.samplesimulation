/**
 * @fileoverview sample neuronal simulation client
 *
 * @author matteo@openworm.org (Matteo Cantarelli)
 * @author giovanni@openworm.org (Giovanni Idili)
 */

var plot = null;

var flotOptions = { yaxis: { min: -30, max: 125 }, xaxis: { min: 0, max: 100, show: false }, series: { shadowSize: 0 }, grid: { backgroundColor: { colors: ["#fff", "#eee"] } } };

function refreshChart(data)
{	
	if(data != null){
		var points = [];
		for(var i=0; i< data[0].length; i++){ points.push([data[0][i], data[1][i]]); }
	
		plot.setData([{data: points, label: "V", color: "#4AA02C"}]);
		plot.setupGrid();
        plot.draw();
	}
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
	
	plot = $.plot($("#placeholder"), [{data: [], label: "V", color: "#4AA02C"}], flotOptions);
	
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