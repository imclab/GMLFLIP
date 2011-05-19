package fr.graffitiresearchlab;

import processing.core.PApplet;

public class Label {

	private PApplet p;
	private String id;
	private int x;
	private int y;
	private int width;
	private int height;
	private XAlign xAlign;
	private YAlign yAlign;

	private String name;
	private int textColor;
	private int bgColor;

	private static final int MIN_WIDTH = 20;
	private static final int MIN_HEIGHT = 20;
	
	// TODO limit size
	
	/**
	 * Label constructor
	 * @param parent
	 * @param id
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param name
	 */
	public Label(PApplet parent, String id, int x, int y, int width, int height, XAlign xAlign, YAlign yAlign, String name, int textColor) {
		this.p = parent;
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = (width > MIN_WIDTH ? width : MIN_WIDTH);
		this.height = (height > MIN_HEIGHT ? height : MIN_HEIGHT);
		this.xAlign = xAlign;
		this.yAlign = yAlign;
		this.textColor = textColor;

		this.name = name;
		
		parent.registerDraw(this);
		
	}
	
	/**
	 * Label constructor
	 * @param parent
	 * @param id
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param name
	 */
	public Label(PApplet parent, String id, int x, int y, int width, int height, XAlign xAlign, YAlign yAlign, String name) {
		this(parent, id, x, y, width, height, xAlign, yAlign, name, parent.color(0));
	}

	/**
	 * Label constructor
	 * @param parent
	 * @param id
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param name
	 */
	public Label(PApplet parent, String id, int x, int y, int width, int height, String name, int textColor) {
		this(parent, id, x, y, width, height, XAlign.middle, YAlign.middle, name, textColor);
	}

	
	/**
	 * Label constructor
	 * @param parent
	 * @param id
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param name
	 */
	public Label(PApplet parent, String id, int x, int y, int width, int height, String name) {
		this(parent, id, x, y, width, height, XAlign.middle, YAlign.middle, name);		
	}
	
	/**
	 * Draws the control where if should be drawn
	 * @param p
	 */
	public void draw() {
		p.pushMatrix();
		p.pushStyle();

		p.translate(x, y);
		p.fill(textColor);

		//AlignUtils.positionText(p, width, height, xAlign, yAlign);
		p.textAlign(PApplet.LEFT, PApplet.CENTER);
		
		p.text(name, 0, height/2);
		//p.text(name, 0, 0);

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
	 * Sets the name of the control
	 */
	public void setName(String value) {
		this.name = value;
	}
}
