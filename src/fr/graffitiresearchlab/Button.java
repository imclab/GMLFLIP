package fr.graffitiresearchlab;

import java.awt.event.MouseEvent;

import processing.core.PApplet;

public class Button {

	private PApplet p;
	private String id;
	private int x;
	private int y;
	private int width;
	private int height;
	private String name;
	private boolean clicked;
	private int textColor;
	private int bgColor;

	private static final int MIN_WIDTH = 20;
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
	 * @param clicked
	 * @param textColor
	 * @param bgColor
	 */
	public Button(PApplet parent, String id, int x, int y, int width, int height, String name, int textColor, int bgColor) {
		this.p = parent;
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width > MIN_WIDTH ? width : MIN_WIDTH;;
		this.height = height > MIN_HEIGHT ? height : MIN_HEIGHT;
		this.name = name;
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
	 * @param clicked
	 */
	public Button(PApplet parent, String id, int x, int y, int width, int height, String name) {
		this(parent, id, x, y, width, height, name, parent.color(0), parent.color(255, 0));
	}
	
	/**
	 * Draws the control where if should be drawn
	 * @param p
	 */
	public void draw() {
		p.pushMatrix();
		p.pushStyle();

		p.translate(x, y);
		p.noFill();
		p.textAlign(PApplet.CENTER, PApplet.CENTER);
		p.fill(bgColor);
		p.stroke(textColor);
		p.rect(0, 0, width, height);
		
		if (clicked) {
			p.fill(125, 125);
			p.rect(1, 1, width-2, height-2);
		}
		p.fill(textColor);
		p.text(name, width/2, height/2);

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
	public boolean isClicked() {
		return clicked;
	}
	
	/**
	 * Sets the value
	 * @param value
	 */
	public void isClicked(boolean clicked) {
		this.clicked = clicked;
	}
	
	
	/**
	 * Checks if the click occured in one of the control area and adjusts value accordingly
	 * @param mouseX
	 * @param mouseY
	 */
	private void check(int mouseX, int mouseY) {
		int absX = mouseX-x;
		int absY = mouseY-y;
		if (absX >= 0 && absX <= width && absY >=0 && absY < height) {
			clicked = true;
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
