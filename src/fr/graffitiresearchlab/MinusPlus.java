package fr.graffitiresearchlab;

import java.awt.event.MouseEvent;

import processing.core.PApplet;

public class MinusPlus {

	private PApplet p;
	private String id;
	private int x;
	private int y;
	private int width;
	private int height;
	private String name;
	private int value;
	private int min;
	private int max;
	private int textColor;
	private int bgColor;

	private static final int MIN_WIDTH = 65;
	private static final int MIN_HEIGHT = 20;

	
	/**
	 * MinusPlusCommand constructor
	 * @param parent
	 * @param id
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param name
	 * @param value
	 * @param min
	 * @param max
	 * @param textColor
	 * @param bgColor
	 */
	public MinusPlus(PApplet parent, String id, int x, int y, int width, int height, String name, int value, int min, int max, int textColor, int bgColor) {
		this.p = parent;
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width > MIN_WIDTH ? width : MIN_WIDTH;;
		this.height = height > MIN_HEIGHT ? height : MIN_HEIGHT;
		this.name = name;
		this.value = value;
		this.min = min;
		this.max = max;
		this.textColor = textColor;
		this.bgColor = bgColor;
		
		parent.registerMouseEvent(this);
		parent.registerDraw(this);
	
	}
	
	/**
	 * MinusPlusCommand constructor
	 * @param parent
	 * @param id
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param name
	 * @param value
	 * @param min
	 * @param max
	 */
	public MinusPlus(PApplet parent, String id, int x, int y, int width, int height, String name, int value, int min, int max) {
		this(parent, id, x, y, width, height, name, value, min, max, parent.color(255), parent.color(0,0));
	}
	
	
	/**
	 * Draws the control where if should be drawn
	 * @param p
	 */
	public void draw() {
		p.pushMatrix();
		p.pushStyle();

		p.translate(x, y);
		p.fill(bgColor);
		p.stroke(textColor);
		p.textAlign(PApplet.CENTER, PApplet.CENTER);
		
		p.rect(0, 0, width, height);
		p.rect(0, 0, 20, height);
		p.rect(width-20, 0, 20, height);
		
		p.fill(textColor);
		// Minus
		p.text("-", 10, height/2);

		// Value
		p.text(value, width/2, height/2);
		
		// Plus
		p.text("+", width-10, height/2);

		p.popStyle();
		p.popMatrix();		
	}
	
	/**
	 * Sets the position of the control
	 * @param x
	 * @param y
	 */
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Gets the id o the control
	 * @return
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Gets the name of the control
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the current value of the control
	 * @return
	 */
	public int getValue() {
		return value;
	}
	
	/**
	 * Sets the value if between the min/max limit
	 * Otherwise sets the value to either min or max if lower or greater than min/max
	 * @param value
	 */
	public void setValue(int value) {
		if (value >= min && value <= max) {
			this.value = value;
		}
		else if (value < min) {
			value = min;
		}
		else if (value > max) {
			value = max;
		}
	}
	
	/**
	 * Sets the minimum value
	 * @param min
	 */
	public void setMin(int min) {
		this.min = min;
		if (this.min > this.max) {
			this.max = this.min;
		}
	}
	
	/**
	 * Sets the maximum value
	 * @param min
	 */
	public void setMax(int max) {
		this.max = max;
		if (this.max < this.min) {
			this.min = this.max;
		}
	}
	
	/**
	 * Checks if the click occured in one of the control area and adjusts value accordingly
	 * @param mouseX
	 * @param mouseY
	 */
	private void check(int mouseX, int mouseY) {
		int absX = mouseX-x;
		int absY = mouseY-y;
		if (absX >= 0 && absX <= width/2 && absY >=0 && absY < height) {
			if (value > min) value--;
		}
		if (absX >= width/2 && absX <= width && absY >=0 && absY < height) {
			if (value < max) value++;
		}
	}
	
	/**
	 * Callback function to receive mouse events sent by the parent PApplet
	 * @param event
	 */
	public void mouseEvent(MouseEvent event) {
		int x = event.getX();
		int y = event.getY();
		
		switch(event.getID()) {
	 		
		case MouseEvent.MOUSE_CLICKED :
			check(x, y);
			break;	
		}	 	
	}
}
