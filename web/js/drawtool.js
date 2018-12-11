	var red = "#ff3300", 
    clickX = [], 
    clickY = [], 
    clickDrag = [],
    canvas = document.getElementById("canvas"),
	context = canvas.getContext("2d"),
	color = red, 
	radius = 2,
	paint = false,
	
	addClick = function(x, y, dragging) {
		clickX.push(x);
		clickY.push(y);
		clickDrag.push(dragging);
	},
	
	draw = function() {
		
		for( i = 0; i <= clickX.length; i++) {
			context.beginPath();
			
			if(clickDrag[i] && i)
				context.moveTo(clickX[i-1], clickY[i-1]);
			else
				context.moveTo(clickX[i] - 1, clickY[i]);
			
			context.lineTo(clickX[i], clickY[i]);
			context.strokeStyle = red;
			context.lineCap = "round";
			context.lineJoin = "round";
			context.lineWidth = radius;
			context.stroke();
		}
		//context.closePath();
	},
		
	press = function(e) {
		var mouseX = e.clientX, mouseY = e.clientY;
		addClick(mouseX, mouseY, false);
		paint = true;
		draw();
	},
		
	drag = function(e) {
		var mouseX = e.clientX, mouseY = e.clientY;
		if(paint) {
			addClick(mouseX, mouseY, true);
			draw();
		}
	},
		
	release = function() {
		paint = false;
		draw();
	},
			
	cancel = function() {
		paint = false;
	};
	
	canvas.addEventListener("mousedown", press, true);
	canvas.addEventListener("mousemove", drag, true);
	canvas.addEventListener("mouseup", release, true);
	canvas.addEventListener("mouseout", cancel, true);