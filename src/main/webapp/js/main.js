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

/**
 * @fileoverview sample neuronal simulation client
 * 
 * @author matteo@openworm.org (Matteo Cantarelli)
 * @author giovanni@openworm.org (Giovanni Idili)
 */

// CONSTANTS
var DEFAULT_CURRENT_VALUE = '3';

// WEBSOCKET
var Simulation = {};

Simulation.initialize = function() {
	Simulation.connect('ws://' + window.location.host + '/org.geppetto.samplesimulation/SimulationServlet');
};

Simulation.setCurrent = function(current) {
	Simulation.current = current;
	Simulation.socket.send(current);
	Console.log('Sent: Current ' + current);
};

Simulation.stop = function() {
	Simulation.socket.send("stop");
	Console.log('Sent: Stop simulation');
};

Simulation.start = function() {
	Simulation.socket.send("start");
	Console.log('Sent: Start simulation');
};

Simulation.reset = function() {
	Simulation.socket.send("reset");
	Console.log('Sent: Reset simulation');
};

Simulation.connect = (function(host) {
	if ('WebSocket' in window) {
		Simulation.socket = new WebSocket(host);
	} else if ('MozWebSocket' in window) {
		Simulation.socket = new MozWebSocket(host);
	} else {
		Console.log('Error: WebSocket is not supported by this browser.');
		return;
	}

	Simulation.socket.onopen = function() {
		Console.log('Info: WebSocket connection opened.');
	};

	Simulation.socket.onclose = function() {
		Console.log('Info: WebSocket closed.');
		Simulation.stop();
	};

	Simulation.socket.onmessage = function(message) {
		refreshChart(eval(message.data));  //this is gonna parse the string that we receive back. an array is expected.
	};
});

var Console = {};

Console.log = (function(message) {
	var console = document.getElementById('console');
	var p = document.createElement('p');
	p.style.wordWrap = 'break-word';
	p.innerHTML = message;
	console.appendChild(p);
	while (console.childNodes.length > 25) {
		console.removeChild(console.firstChild);
	}
	console.scrollTop = console.scrollHeight;
});

// APPLICATION LOGIC
var plot = null;
var flotOptions = {
	yaxis : {
		min : -30,
		max : 125
	},
	xaxis : {
		show : false,
		min : 0,
		max : 100
	},
	series : {
		shadowSize : 0
	},
	grid : {
		backgroundColor : {
			colors : [ "#fff", "#eee" ]
		}
	}
};

function refreshChart(data) {
	if (data != null) {
		var points = [];
		for ( var i = 0; i < data[1].length; i++) {
			// HACK: altering 1 value in the sequence by a small amount to work
			// around a bug in chrome
			// NOTE: curious? go read this: http://goo.gl/3BGXi
			(i == 0) ? points.push([ data[0][i], data[1][i] + 0.0001 ])
					: points.push([ data[0][i], data[1][i] ]);
		}

		plot.setData([ {
			data : points,
			label : "mV",
			color : "#4AA02C"
		} ]);
		// no need to call plot.setupGrid() because the xaxis viewport is fixed
		plot.draw();
	}
}

$(document).ready(function() {

	plot = $.plot($("#placeholder"), [ {
		data : [],
		label : "mV",
		color : "#4AA02C"
	} ], flotOptions);

	Simulation.initialize();

	$('#current').change(function() {
		var e = document.getElementById("current");
		var iext = e.options[e.selectedIndex].text;
		Simulation.setCurrent(iext);
	});

	$('#stop').attr('disabled', 'disabled');
	$('#reset').attr('disabled', 'disabled');

	$('#start').click(function() {
		$('#start').attr('disabled', 'disabled');
		$('#reset').attr('disabled', 'disabled');
		$('#stop').removeAttr('disabled');
		$('#imageHH').attr('style', 'visibility:visible');
		Simulation.start();
	});

	$('#stop').click(function() {
		$('#start').removeAttr('disabled');
		$('#reset').removeAttr('disabled');
		$('#stop').attr('disabled', 'disabled');
		Simulation.stop();
	});
	
	$('#reset').click(function() {
		$('#start').removeAttr('disabled');
		$('#reset').attr('disabled', 'disabled');
		$('#stop').attr('disabled', 'disabled');
		
		// reset current to default value
		$("#current").val(DEFAULT_CURRENT_VALUE);
		
		Simulation.reset();
	});

});
