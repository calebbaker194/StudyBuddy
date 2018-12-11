	var clickX = [], 
    clickY = [], 
    clickDrag = [],
    canvas = document.getElementById("canvas"),
	context = canvas.getContext("2d"),
	color = "#ffffff", 
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
			
			context.strokeStyle = color;
			context.lineCap = "round";
			context.lineJoin = "round";
			context.lineWidth = radius;
			context.stroke();
		}
		context.closePath();
	},
	
	press = function(e) {
		var rect = canvas.getBoundingClientRect(),
		scaleX = canvas.width / rect.width,
		scaleY = canvas.height / rect.height,
		
	    mouseX = ((e.changedTouches ? e.changedTouches[0].clientX : e.clientX) - rect.left) * scaleX, 
	    mouseY = ((e.changedTouches ? e.changedTouches[0].clientY : e.clientY) - rect.top) * scaleY;
		
		addClick(mouseX, mouseY, false);
		paint = true;
		draw();
	},
	
	drag = function(e) {
		var rect = canvas.getBoundingClientRect(),
		scaleX = canvas.width / rect.width,
		scaleY = canvas.height / rect.height,
		
		mouseX = ((e.changedTouches ? e.changedTouches[0].clientX : e.clientX) - rect.left) * scaleX, 
	    mouseY = ((e.changedTouches ? e.changedTouches[0].clientY : e.clientY) - rect.top) * scaleY;
		
		if(paint) {
			addClick(mouseX, mouseY, true);
			draw();
		}
		
		e.preventDefault();
	},
	
	release = function(e) {
		paint = false;
		draw();
	},
	
	cancel = function(e) {
		paint = false;
	};
	
	$(".color").click(function() {
		clickX = [];
		clickY = [];
		clickDrag = [];
		color = $(this).val();
		if(color === "eraser") {
			context.beginPath();
			context.clearRect(0, 0, canvas.width, canvas.height);
		}
	});
	
	$("#canvas").mousedown(press).mousemove(drag).mouseup(release).mouseout(cancel);
	
	canvas.addEventListener("touchstart", press, false);
	canvas.addEventListener("touchmove", drag, false);
	canvas.addEventListener("touchend", release, false);
	canvas.addEventListener("touchcancel", cancel, false);