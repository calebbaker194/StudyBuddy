/*
 * @author Zac Migues
 * 
 * Enables drawing lines on HTML5 cnavas, it's not very pretty,
 * but it does work. 
 */	
var clickX = [], //arrays to hold positions
    clickY = [], 
    clickDrag = [],
    canvas = document.getElementById("canvas"),
	context = canvas.getContext("2d"),
	color = "#ffffff", //default color
	radius = 2, //line radius
	paint = false, //flag for drawing line instead of point
	
	/*
	 * adds a new cursor position to arrays
	 * 
	 * @param x : x position
	 * @param y : y position
	 * @param dragging : boolean, true if cursor/finger is drug while pressed
	 */
	addClick = function(x, y, dragging) {
		clickX.push(x);
		clickY.push(y);
		clickDrag.push(dragging);
	},
	
	/*
	 * does the drawing
	 */
	draw = function() {
		for( i = 0; i <= clickX.length; i++) {
			context.beginPath();
			
			if(clickDrag[i] && i)
				context.moveTo(clickX[i-1], clickY[i-1]); //fills path with color
			else
				context.moveTo(clickX[i] - 1, clickY[i]); //makes a point
			
			context.lineTo(clickX[i], clickY[i]); //makes the line
			
			context.strokeStyle = color; //does the actual drawing
			context.lineCap = "round";
			context.lineJoin = "round";
			context.lineWidth = radius;
			context.stroke();
		}
		context.closePath();
	},
	
	/*
	 * Event handler functions
	 */
	press = function(e) {
		var rect = canvas.getBoundingClientRect(),
		scaleX = canvas.width / rect.width, //scaling mouse position to canvas
		scaleY = canvas.height / rect.height,
		
		//setting x and y vals, checks if mouse movement or touch movement
	    mouseX = ((e.changedTouches ? e.changedTouches[0].clientX : e.clientX) - rect.left) * scaleX, 
	    mouseY = ((e.changedTouches ? e.changedTouches[0].clientY : e.clientY) - rect.top) * scaleY;
		
		addClick(mouseX, mouseY, false);
		sendChannel.send("{\"type\":\"draw\", \"mouseX\":"+mouseX+" ,\"mouseY\":"+mouseY+", \"paint\":false}");
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
			sendChannel.send("{\"type\":\"draw\", \"mouseX\":"+mouseX+" ,\"mouseY\":"+mouseY+", \"paint\":false}");
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
	
	/*
	 * Event handler for choosing a new color
	 * resets the arrays so you can use different colors
	 */
	$(".color").click(function() {
		clickX = [];
		clickY = [];
		clickDrag = [];
		color = $(this).val();
		if(color === "eraser") {
			context.beginPath(); //clears the canvas
			context.clearRect(0, 0, canvas.width, canvas.height);
		}
	});
	
	/*
	 * Event handlers for mouse events
	 */
	$("#canvas").mousedown(press).mousemove(drag).mouseup(release).mouseout(cancel);
	
	/*
	 * Event handlers for touch events
	 */
	canvas.addEventListener("touchstart", press, false);
	canvas.addEventListener("touchmove", drag, false);
	canvas.addEventListener("touchend", release, false);
	canvas.addEventListener("touchcancel", cancel, false);