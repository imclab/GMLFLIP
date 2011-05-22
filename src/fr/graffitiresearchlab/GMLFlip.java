package fr.graffitiresearchlab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

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
	private final String APP_SUBTITLE = "turns graffiti into flip books";
	private final String APP_DRAW_SOMETHING = 	"Use this area to draw (mouse click while moving mouse)" +
												"\r\nPress x to remove the last stroke" +
												"\r\nClick CLEAR to reinit" +
												"\r\nClick EXPORT to create flipbooks (/output folder)" +
												"\r\nClick SAVE to save as GML (/gml folder)" +
												"\r\nDrag&Drop a tag picture to use as guideline" +
												"\r\nUse the + or - key to zoom in/out" +
												"\r\nPress spacebar to remove the picture";
	private final String APP_BOTTOM_TITLE = "http://graffitiresearchlab.fr";

	private final String MSG_NOTHING_TO_EXPORT = "Nothing to export, draw something first";
	private final String MSG_NOTHING_TO_SAVE = "Nothing to save, draw something first";
	private final String MSG_EXPORT_PLEASE_WAIT = "Exporting ... please wait ...";
	private final String MSG_SAVE_PLEASE_WAIT = "Saving ... please wait ...";
	
	private final String MSG_SAVE_ERROR = "An error occured while saving the GML file";
	private final String MSG_SAVE_SUCCESS = "GML file saved to the /gml folder";

	private final String MSG_EXPORT_ERROR = "An error occured while exporting the Flipbooks";
	private final String MSG_EXPORT_SUCCESS = "Flipbooks saved to the /output folder";

	private final String MSG_CANNOT_URL = "Cannot load from URL (local image only)";
	private final String MSG_NOT_AN_IMAGE = "Unsupported image format";
	private final String MSG_LOADING_IMAGE = "Loading image, please wait ...";
	private final String MSG_LOADED_IMAGE = "Image loaded";
	
	private final String UI_IMAGES_TAG = "PAGES/TAG";
	private final String UI_ROWS_PAGE = "ROWS/PAGE";
	private final String UI_RECORDING = "RECORDING";
	
	private final String UI_CLEAR = "CLEAR";
	private final String UI_EXPORT = "EXPORT";
	private final String UI_SAVE = "SAVE";
	
	private final String IMG_OUTPUT_FOLDER = "output";
	private final String GML_OUTPUT_FOLDER = "gml";

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
	
	private PImage topRightImage;
	private PImage logo;
	private PImage bottomRightImage;
	
	private PImage bgImage;
	private float bgRatio = 1;
	
	private PFont font;

	private SDrop sdrop;

	private Properties loc;
	private Properties gmlflip;

	public void setup() {
		size(800, 600, OPENGL);
		frameRate(60);
		
		// GML4U logs config
		PropertyConfigurator.configure(sketchPath("data")+"/log4j.properties");
		
		// Localization
		loc = new Properties();
		try {
			FileInputStream fis = new FileInputStream(sketchPath("data")+"/loc_EN.properties");
			loc.load(fis);
		} catch (FileNotFoundException e) {
			// TODO log it
			e.printStackTrace();
		} catch (IOException e) {
			// TODO log it
			e.printStackTrace();
		}
		
		// App properties
		gmlflip = new Properties();
		try {
			FileInputStream fis = new FileInputStream(sketchPath("data")+"/gmlflip.properties");
			gmlflip.load(fis);
		} catch (FileNotFoundException e) {
			// TODO log it
			e.printStackTrace();
		} catch (IOException e) {
			// TODO log it
			e.printStackTrace();
		}
		
		// Create output folders
		String path = "";
		path = sketchPath+"/"+gmlflip.getProperty("img_output_folder", IMG_OUTPUT_FOLDER);
		(new File(path)).mkdirs();
		path = sketchPath+"/"+gmlflip.getProperty("gml_output_folder", GML_OUTPUT_FOLDER);
		(new File(path)).mkdirs();
		
		
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

		new Label(this, "imgPerTag", x, y, 100, 20, loc.getProperty("ui_images_tag", UI_IMAGES_TAG), textColor);
		imagesPerTag = new MinusPlus(this, "imagesPerTag", x, y+25, 70, 20, "pages", 25, 10, 50, textColor, bgColor);

		new Label(this, "rowsPerPage", x+=90, y, 100, 20, loc.getProperty("ui_rows_page", UI_ROWS_PAGE), textColor);
		rowsPerPage = new MinusPlus(this, "rowsPerPage", x, y+25, 70, 20, "pages", 8, 4, 10, textColor, bgColor);

		new Label(this, "Recording", x+=130, y, 100, 20, loc.getProperty("ui_recording", UI_RECORDING), textColor);
		clearButton = new Button(this, "clear", x, y+=25, 80, 20, loc.getProperty("ui_clear", UI_CLEAR), textColor, bgColor);
		exportButton = new Button(this, "export", x+=90, y, 80, 20, loc.getProperty("ui_export", UI_EXPORT), textColor, bgColor);
		saveButton = new Button(this, "save", x+=90, y, 80, 20, loc.getProperty("ui_save", UI_SAVE), textColor, bgColor);

		timerLabel = new Label(this, "Timer", drawingPadX + 20, drawingPadY+drawingPadHeight-30, 40, 20, "", textColor);
		infoLabel = new Label(this, "info", 20, height-30, 300, 20, "", textColor);
		
		topRightImage = loadImage(sketchPath("data")+"/"+gmlflip.getProperty("top_right_image","Silhouette.png"));
		logo = loadImage(sketchPath("data")+"/GRLFR_Logo.png");
		bottomRightImage = loadImage(sketchPath("data")+"/"+gmlflip.getProperty("bottom_right_image","Splash.png"));
		
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
				infoLabel.setName(loc.getProperty("msg_nothing_to_save", MSG_NOTHING_TO_SAVE));							
			}
			else {
				// Save
				infoLabel.setName(loc.getProperty("msg_save_please_wait", MSG_SAVE_PLEASE_WAIT));			
				saver.save(gml, sketchPath+"/" + gmlflip.getProperty("gml_output_folder",GML_OUTPUT_FOLDER) +"/"+System.currentTimeMillis()+".gml");
			}
		
		}

		if (exportButton.isClicked()) {
			timer.stop();
			exportButton.isClicked(false);
			timerLabel.setName("");
			
			Gml gml = recorder.getGml();
			if (gml.getStrokes().size() == 0) {
				infoLabel.setName(loc.getProperty("msg_nothing_to_export", MSG_NOTHING_TO_EXPORT));							
			}
			else {
				// Export
				infoLabel.setName(MSG_EXPORT_PLEASE_WAIT);			
				export.execute(gml, sketchPath+"/" + gmlflip.getProperty("img_output_folder", IMG_OUTPUT_FOLDER), rowsPerPage.getValue(), imagesPerTag.getValue());
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
			infoLabel.setName(loc.getProperty("msg_cannot_url", MSG_CANNOT_URL));
		}

		// if the dropped object is an image, then 
		// load the image into our PImage.
		if(event.isImage()) {
			infoLabel.setName(loc.getProperty("msg_loading_image", MSG_LOADING_IMAGE));
			bgImage = loadImage(event.filePath());
			float ratio = bgImage.width/bgImage.height;
			if (ratio > 1) { // Landscape
				bgImage.resize(drawingPadWidth, (int) (drawingPadWidth*bgImage.height/bgImage.width));	
			}
			else { // Portrait
				bgImage.resize((int) (drawingPadHeight*bgImage.width/bgImage.height), drawingPadHeight);
			}
			infoLabel.setName(loc.getProperty("msg_loaded_image", MSG_LOADED_IMAGE));
			
		}
		else {
			infoLabel.setName(loc.getProperty("msg_not_an_image", MSG_NOT_AN_IMAGE));
		}
	}
	
	public void exportEvent(ExportEvent event) {
		if (event.succeed) {
			infoLabel.setName(loc.getProperty("msg_export_success", MSG_EXPORT_SUCCESS));
		}
		else {
			infoLabel.setName(loc.getProperty("msg_export_error", MSG_EXPORT_ERROR));
		}
	}

	public void gmlEvent(GmlEvent event) {
		if (event instanceof GmlSavingEvent) {
			GmlSavingEvent evt = (GmlSavingEvent) event;
			if (evt.successful) {
				infoLabel.setName(loc.getProperty("msg_save_success", MSG_SAVE_SUCCESS));
			}
			else {
			 	infoLabel.setName(loc.getProperty("msg_save_error", MSG_SAVE_ERROR));
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
		text(loc.getProperty("app_name", APP_NAME), x, y);

		// Subtitle
		textAlign(LEFT);
		x += textWidth(loc.getProperty("app_name", APP_NAME));
		textSize(20);
		fill(255);
		text(loc.getProperty("app_subtitle", APP_SUBTITLE).toUpperCase(), x, y);

		textAlign(LEFT);
		noFill();//fill(255, 250);
		stroke(textColor);
		rect(drawingPadX, drawingPadY, drawingPadWidth, drawingPadHeight);

	
		imageMode(CORNER);
		image(topRightImage, width-topRightImage.width-10, 10);
	
		imageMode(CORNER);
		image(bottomRightImage, width-bottomRightImage.width, height-bottomRightImage.height);

		fill(bgColor);
		textAlign(RIGHT);
		textSize(12);
		text(loc.getProperty("app_bottom_title", APP_BOTTOM_TITLE), drawingPadX + drawingPadWidth, height-15);
		popStyle();
		
		fill(textColor);
		textAlign(LEFT);
		textSize(14);
		text(loc.getProperty("app_draw_something", APP_DRAW_SOMETHING), drawingPadX+10, drawingPadY + 20);



	}



	public static void main(String _args[]) {
		PApplet.main(new String[] { GMLFlip.class.getName() });
	}

}
