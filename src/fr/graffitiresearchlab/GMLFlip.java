package fr.graffitiresearchlab;

import org.apache.log4j.PropertyConfigurator;
import gml4u.drawing.GmlBrushManager;
import gml4u.events.GmlEvent;
import gml4u.events.GmlSavingEvent;
import gml4u.model.Gml;
import gml4u.model.GmlBrush;
import gml4u.model.GmlStroke;
import gml4u.recording.GmlRecorder;
import gml4u.utils.GmlSaver;
import gml4u.utils.Timer;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import sojamo.drop.DropEvent;
import sojamo.drop.SDrop;
import toxi.geom.Vec3D;




public class GMLFlip extends PApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5159485317982749825L;

	private final String APP_NAME = "GMLFLIP";
	private String APP_SUBTITLE = "turns graffiti into flip books";
	private String APP_DRAW_SOMETHING = 	"Use this area to draw (mouse click while moving mouse)" +
												"\r\nPress x to remove the last stroke" +
												"\r\nClick CLEAR to reinit" +
												"\r\nClick EXPORT to create flipbooks (/output folder)" +
												"\r\nClick SAVE to save as GML (/gml folder)" +
												"\r\nDrag&Drop a tag picture to use as guideline" +
												"\r\nUse the + or - key to zoom in/out" +
												"\r\nPress spacebar to remove the picture";
	private String APP_BOTTOM_TITLE = "http://graffitiresearchlab.fr";

	private String MSG_NOTHING_TO_EXPORT = "Nothing to export, draw something first";
	private String MSG_NOTHING_TO_SAVE = "Nothing to save, draw something first";
	private String MSG_EXPORT_PLEASE_WAIT = "Exporting ... please wait ...";
	private String MSG_SAVE_PLEASE_WAIT = "Saving ... please wait ...";
	
	private String MSG_SAVE_ERROR = "An error occured while saving the GML file";
	private String MSG_SAVE_SUCCESS = "GML file saved to the /gml folder";

	private String MSG_EXPORT_ERROR = "An error occured while exporting the Flipbooks";
	private String MSG_EXPORT_SUCCESS = "Flipbooks saved to the /output folder";

	private String MSG_CANNOT_URL = "Cannot load from URL (local image only)";
	private String MSG_NOT_AN_IMAGE = "Unsupported image format";
	private String MSG_LOADING_IMAGE = "Loading image, please wait ...";
	private String MSG_LOADED_IMAGE = "Image loaded";
	
	private String UI_IMAGES_TAG = "PAGES/TAG";
	private String UI_ROWS_PAGE = "ROWS/PAGE";
	private String UI_RECORDING = "RECORDING";
	
	private String UI_CLEAR = "CLEAR";
	private String UI_EXPORT = "EXPORT";
	private String UI_SAVE = "SAVE";
	
	private String img_output_folder = "output";
	private String gml_output_folder = "gml";

	private int textColor = color(185, 25, 123);
	private int bgColor = color(239, 232, 69);

	private ExportThread export;

	// GML stuff
	private GmlSaver saver;
	private GmlRecorder recorder;
	private Vec3D screen;


	private Timer timer = new Timer();
	private float timerStep = .033f;	

	private GmlBrushManager brushManager;

	// UI Components
	private MinusPlus imagesPerTag;
	private MinusPlus rowsPerPage;


	private Button clearButton;
	private Button exportButton;
	private Button saveButton;

	private Label timerLabel;
	private Label infoLabel;


	private int drawingPadX;
	private int drawingPadY;
	private int drawingPadWidth;
	private int drawingPadHeight;
	
	private PImage silhouette;
	private PImage logo;
	private PImage splash;
	
	private PImage bgImage;
	private float bgRatio = 1;
	
	private PFont font;

	private SDrop sdrop;


	public void setup() {
		size(800, 600, OPENGL);
		frameRate(60);
		
		PropertyConfigurator.configure(sketchPath("data")+"/log4j.properties");
		//textSize(12);
		smooth();
		background(bgColor);
		
		drawingPadX = 20;
		drawingPadY = 150;
		drawingPadWidth = width-drawingPadX*2;
		drawingPadHeight = height-drawingPadY-50;

		export = new ExportThread(500, "x", this);
		export.start();

		screen = new Vec3D(drawingPadWidth, drawingPadHeight, 10);
		recorder = new GmlRecorder(screen);
		saver = new GmlSaver(500, "saver", this);
		saver.start();

		brushManager = new GmlBrushManager();
		brushManager.setDefault(brushManager.get(GmlBrushManager.BRUSH_CURVES0000));

		// Init and position UI components

		int x = drawingPadX;
		int y = 85;

		new Label(this, "imgPerTag", x, y, 100, 20, UI_IMAGES_TAG, textColor);
		imagesPerTag = new MinusPlus(this, "imagesPerTag", x, y+25, 70, 20, "pages", 25, 10, 50, textColor, bgColor);

		new Label(this, "rowsPerPage", x+=90, y, 100, 20, UI_ROWS_PAGE, textColor);
		rowsPerPage = new MinusPlus(this, "rowsPerPage", x, y+25, 70, 20, "pages", 8, 4, 10, textColor, bgColor);

		new Label(this, "Recording", x+=130, y, 100, 20, UI_RECORDING, textColor);
		clearButton = new Button(this, "clear", x, y+=25, 80, 20, UI_CLEAR, textColor, bgColor);
		exportButton = new Button(this, "export", x+=90, y, 80, 20, UI_EXPORT, textColor, bgColor);
		saveButton = new Button(this, "save", x+=90, y, 80, 20, UI_SAVE, textColor, bgColor);

		timerLabel = new Label(this, "Timer", drawingPadX + 20, drawingPadY+drawingPadHeight-30, 40, 20, "", textColor);
		infoLabel = new Label(this, "info", 20, height-30, 300, 20, "", textColor);
		
		silhouette = loadImage(sketchPath("data")+"/Silhouette.png");
		logo = loadImage(sketchPath("data")+"/GRLFR_Logo.png");
		splash = loadImage(sketchPath("data")+"/Splash.png");
		
		sdrop = new SDrop(this);
		
		font = loadFont("DINNextRoundedLTPro-Light-48.vlw");
		
				
	}



	public void draw() {

		fill(0);

		timer.tick(timerStep);

		if (timer.started()) {
			timerLabel.setName((""+timer.getTime()).replaceAll("(.*\\...).*", "$1 s"));
		}


		if (clearButton.isClicked()) {
			recorder.clear();
			timer.stop();
			timerLabel.setName("");
			infoLabel.setName("");
			clearButton.isClicked(false);
		}

		if (saveButton.isClicked()) {
			timer.stop();
			timerLabel.setName("");
			saveButton.isClicked(false);
			Gml gml = recorder.getGml();
			if (gml.getStrokes().size() == 0) {
				infoLabel.setName(MSG_NOTHING_TO_SAVE);							
			}
			else {
				// Save
				infoLabel.setName(MSG_SAVE_PLEASE_WAIT);			
				saver.save(gml, sketchPath+"/"+gml_output_folder+"/"+System.currentTimeMillis()+".gml");
			}
		
		}

		if (exportButton.isClicked()) {
			timer.stop();
			exportButton.isClicked(false);
			timerLabel.setName("");
			
			Gml gml = recorder.getGml();
			if (gml.getStrokes().size() == 0) {
				infoLabel.setName(MSG_NOTHING_TO_EXPORT);							
			}
			else {
				// Export
				infoLabel.setName(MSG_EXPORT_PLEASE_WAIT);			
				export.execute(gml, sketchPath+"/"+img_output_folder, rowsPerPage.getValue(), imagesPerTag.getValue());
			}
		}

		drawBackground();

		pushMatrix();
		fill(0);
		translate(drawingPadX, drawingPadY);
		for (GmlStroke stroke : recorder.getStrokes()) {
			brushManager.draw(g, stroke, drawingPadWidth, GmlBrushManager.BRUSH_CURVES0000);
		}
		popMatrix();

	}

	public void dropEvent(DropEvent event) {

		if (event.isURL()) {
			infoLabel.setName(MSG_CANNOT_URL);
		}

		// if the dropped object is an image, then 
		// load the image into our PImage.
		if(event.isImage()) {
			infoLabel.setName(MSG_LOADING_IMAGE);
			bgImage = loadImage(event.filePath());
			float ratio = bgImage.width/bgImage.height;
			if (ratio > 1) { // Landscape
				bgImage.resize(drawingPadWidth, (int) (drawingPadWidth*bgImage.height/bgImage.width));	
			}
			else { // Portrait
				bgImage.resize((int) (drawingPadHeight*bgImage.width/bgImage.height), drawingPadHeight);
			}
			infoLabel.setName(MSG_LOADED_IMAGE);
			
		}
		else {
			infoLabel.setName(MSG_NOT_AN_IMAGE);
		}
	}
	
	public void exportEvent(ExportEvent event) {
		if (event.succeed) {
			infoLabel.setName(MSG_EXPORT_SUCCESS);
		}
		else {
			infoLabel.setName(MSG_EXPORT_ERROR);
		}
	}

	public void gmlEvent(GmlEvent event) {
		if (event instanceof GmlSavingEvent) {
			GmlSavingEvent evt = (GmlSavingEvent) event;
			if (evt.successful) {
				infoLabel.setName(MSG_SAVE_SUCCESS);
			}
			else {
			 	infoLabel.setName(MSG_SAVE_ERROR);
			}
		}
		
	}
	
	
	public void keyPressed() {
		if (key == '-') {
			bgRatio *=.9f;
		}
		else if (key == '+') {
			bgRatio *=1.1f;
		}
		else if (key == ' ') {
			bgImage = null;
		}
		else if (key == 'x' || key == 'X') {
			recorder.removeLastStroke(0);
		}
		infoLabel.setName("");
		
	}

	public void mouseClicked() {
		float x = mouseX-drawingPadX;
		float y = mouseY-drawingPadY;

		if (x >= 0 && x <= drawingPadWidth && y >= 0 && y <= drawingPadHeight) {
			if (!timer.started()) {
				timer.start();
			}
		}
	}
	
	public void mousePressed() {

		float x = mouseX-drawingPadX;
		float y = mouseY-drawingPadY;
		if (x >= 0 && x <= drawingPadWidth && y >= 0 && y <= drawingPadHeight) {
			
			if (!timer.started()) {
				timer.start();
			}
			GmlBrush brush = new GmlBrush();
			brush.set(GmlBrush.UNIQUE_STYLE_ID, GmlBrushManager.BRUSH_CURVES0000);
			recorder.beginStroke(0, 0, brush);
		}
	}


	public void mouseDragged() {
		float x = mouseX-drawingPadX;
		float y = mouseY-drawingPadY;
		if (x >= 0 && x <= drawingPadWidth && y >= 0 && y <= drawingPadHeight) {
			if (timer.started()) {
				x = map(x, 0, drawingPadWidth, 0, 1);
				y = map(y, 0, drawingPadHeight, 0, 1);
				Vec3D v = new Vec3D(x, y, 0);
				recorder.addPoint(0, v, timer.getTime());
			}
		}
	}

	public void mouseReleased() {

		if (timer.started()) {
			recorder.endStroke(0);
		}
	}



	private void drawBackground() {
		background(255);

		if (null != bgImage && 0 != bgImage.width) {
			imageMode(CENTER);
			image(bgImage, drawingPadX + drawingPadWidth/2, drawingPadY + drawingPadHeight/2, bgImage.width*bgRatio, bgImage.height*bgRatio);
		}
		else {
			imageMode(CENTER);
			image(logo, drawingPadX + drawingPadWidth/2, drawingPadY + drawingPadHeight/2);
		}

		// Mask
		pushStyle();
		fill(bgColor);
		//fill(0);
		noStroke();
		rect(0, 0, width, drawingPadY-1);
		rect(0, drawingPadHeight+drawingPadY+1, width, height-drawingPadHeight+drawingPadX);
		rect(0, drawingPadY-1, drawingPadX, drawingPadHeight+drawingPadY);
		rect(drawingPadX+drawingPadWidth, drawingPadY-1, width, drawingPadHeight+drawingPadY);
		popStyle();

		// Banner
		pushStyle();
		noStroke();
		fill(textColor);
		rect(0, 0, width, 75);
		popStyle();

		// Title
		pushStyle();
		textFont(font);
		int x = 30;
		int y = 60;
		textAlign(LEFT);
		fill(255);
		textSize(60);
		text(APP_NAME, x, y);

		// Subtitle
		textAlign(LEFT);
		x += textWidth(APP_NAME);
		textSize(20);
		fill(255);
		text(APP_SUBTITLE.toUpperCase(), x, y);

		textAlign(LEFT);
		noFill();//fill(255, 250);
		stroke(textColor);
		rect(drawingPadX, drawingPadY, drawingPadWidth, drawingPadHeight);

		fill(textColor);
		textAlign(RIGHT);
		textSize(12);
		text(APP_BOTTOM_TITLE, drawingPadX + drawingPadWidth, height-25);
		popStyle();
	
		imageMode(CORNER);
		image(silhouette, width-silhouette.width-10, 10);
	
		imageMode(CORNER);
		image(splash, width-splash.width, height-splash.height);
		
		fill(textColor);
		textAlign(LEFT);
		textSize(14);
		text(APP_DRAW_SOMETHING, drawingPadX+10, drawingPadY + 20);



	}



	public static void main(String _args[]) {
		PApplet.main(new String[] { GMLFlip.class.getName() });
	}

}
