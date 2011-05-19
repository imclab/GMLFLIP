package fr.graffitiresearchlab;


import gml4u.drawing.GmlBrushManager;
import gml4u.model.Gml;
import gml4u.model.GmlStroke;
import gml4u.utils.GmlUtils;

import java.lang.reflect.Method;

import processing.core.PApplet;
import processing.core.PGraphics;


public class ExportThread extends Thread {

	//private static final Logger LOGGER = Logger.getLogger(AnalyzerThread.class.getName());

	private boolean running;           // Is the thread running?  Yes or no?
	private int wait;                  // How many milliseconds should we wait in between executions?
	private String threadId;           // Thread name


	private GmlBrushManager brush;

	private boolean requiresExport;
	private PApplet parent;
	private Method callback;

	// Export information
	private Gml gml;
	private String output;
	private int rowsPerPage;
	private int imagesPerTag;
	private String uid;

	private final String APP_EXPORT_MADE1 = "made with";
	private final String APP_EXPORT_MADE2 = "GMLFlip";
	private final String APP_EXPORT_MADE3 = "graffiti -> flipbook";
	private final String APP_EXPORT_MADE4 = "a tool by http://graffitiresearchlab.fr";


	/**
	 * Creates a new GmlParser thread
	 * The parent object must implement a <i>public void gmlEvent(GmlEvent event)</i> method.
	 * @param wait - int (waiting time in ms)
	 * @param id - String (thread id)
	 * @param parent - Object
	 */
	public ExportThread (int wait, String id, PApplet parent){
		try {
			// Looking for a method called "gmlEvent", with one argument of GmlEvent type
			callback = parent.getClass().getMethod("exportEvent", new Class[] { ExportEvent.class });

		}
		catch (Exception e) {
			//LOGGER.warn(parent.getClass()+" shall implement a \"public void gmlEvent(GmlEvent event)\" method to be able to receive GmlEvent");
		}

		this.parent = parent;
		this.wait = wait;
		this.running = false;
		this.threadId = id;

		this.brush = new GmlBrushManager();
	}

	/**
	 * Starts the thread
	 */
	public void start () {
		//LOGGER.debug("Starting thread");
		running = true;
		super.start();
	}

	/**
	 * Run method triggered by start()
	 */
	public void run () {
		while (running){
			try {
				if (requiresExport) {
					//LOGGER.debug("Start export");
					requiresExport = false;

					// Export
					float startTime = System.currentTimeMillis();
					boolean result = export(); 
					float duration = (System.currentTimeMillis() - startTime)/1000;

					try {
						sendStatus(new ExportEvent(result, duration) );
					}
					catch (Exception e) {
						//LOGGER.warn("Couldn't invoke the callback method for some reason. "+e.getMessage());
					}
					// null objects
					nullObjects();
				}					
				sleep((long)(wait));	
			}
			catch (Exception e) {
				//LOGGER.warn(e.getMessage());
			}
		}
		//LOGGER.debug(threadId + " thread is done!");  // The thread is done when we get to the end of run()
		quit();
	}

	/**
	 * Nulls objects after analyzis
	 */
	private void nullObjects() {
		this.gml = null;
		this.output = null;
		this.rowsPerPage = 0;
		this.imagesPerTag = 0;
		this.uid = null;
	}


	/**
	 * Quits the thread
	 */
	public void quit() {
		//LOGGER.debug(threadId + " quitting.");
		running = false;
		interrupt(); // in case the thread is waiting. . .
	}

	/**
	 * Sends status
	 * @param event
	 */
	public void sendStatus(ExportEvent event) {
		if (null != callback) {
			try {
				// Call the method with this object as the argument!
				//LOGGER.debug("Invoking callback");
				callback.invoke(parent, event);
			}
			catch (Exception e) {
				//LOGGER.warn("Couldn't invoke the callback method for some reason. "+e.getMessage());
			}
		}
	}



	public void execute(Gml gml, String outputFolder, int imagesPerPage, int imagesPerTag) {
		this.gml = gml;
		this.output = outputFolder;
		this.rowsPerPage = imagesPerPage;
		this.imagesPerTag = imagesPerTag; 
		this.uid = ""+System.currentTimeMillis();

		// TODO Check arguments if needed

		requiresExport = true;
	}


	/**
	 * Exports the Gml to images
	 */
	private boolean export() {

		try {
			// Clean the Gml (starts at 0)
			GmlUtils.timeBox(gml, 10, true);


			float step = gml.getDuration()/imagesPerTag;
			float time = step;

			float width=2000;
			float height = width*1.4142f;

			// Calculate image height
			// Images' dimensions
			float border = 25;
			float thumbnailHeight = height/rowsPerPage;

			float gmlHeight = thumbnailHeight-(border*2);
			float gmlWidth = gmlHeight*1.9f;


			int totalPages =  imagesPerTag+1;


			for (int p=1; p <= totalPages; p++) {

				PGraphics pg = parent.createGraphics((int) width, (int) height, PGraphics.P3D);
				pg.beginDraw();

				pg.background(255);

				pg.pushMatrix();
				for(int j=0; j<rowsPerPage; j++) {

					// Rectangle
					pg.pushStyle();

					pg.stroke(125);
					pg.rect(0, 0, width, thumbnailHeight);
					pg.popStyle();


					if (p < totalPages) {
						// Draw Gml (left)
						pg.pushMatrix();
						pg.translate(border, border);
						pg.textSize(50);
						pg.fill(0);
						pg.textAlign(PApplet.LEFT);
						pg.text("<gml>", gmlWidth+border*2+40, 40);
						pg.text("</gml>", gmlWidth+border*2+40, thumbnailHeight-60);
						pg.noFill();
						for (GmlStroke stroke : gml.getStrokes()) {
							brush.draw(pg, stroke, gmlWidth, time, GmlBrushManager.BRUSH_CURVES0000);
						}
						pg.popMatrix();

						// Draw Gml (left)
						pg.pushMatrix();
						pg.translate(width-(gmlWidth+border), border);
						pg.textSize(50);
						pg.fill(0);
						pg.textAlign(PApplet.RIGHT);
						pg.text("<gml>", -40, 40);
						pg.text("</gml>", -40, thumbnailHeight-60);
						pg.noFill();
						for (GmlStroke stroke : gml.getStrokes()) {
							brush.draw(pg, stroke, gmlWidth, time, GmlBrushManager.BRUSH_CURVES0000);
						}
						pg.popMatrix();
					}

					// Last page
					else {
						// Draw info (right)
						pg.pushMatrix();
						pg.translate(border, border);
						pg.pushStyle();
						pg.fill(10);
						pg.textSize(40);
						pg.text(APP_EXPORT_MADE1, border, 30);
						pg.textSize(80);
						pg.text(APP_EXPORT_MADE2, border, 125);
						pg.textSize(30);
						pg.text(APP_EXPORT_MADE3, border, 175);
						pg.text(APP_EXPORT_MADE4, border, 220);
						pg.popStyle();

						pg.popMatrix();

						// Draw info (left)
						pg.pushMatrix();
						pg.translate(width/2+100, border);
						pg.pushStyle();
						pg.fill(10);
						pg.textSize(40);
						pg.text(APP_EXPORT_MADE1, border, 30);
						pg.textSize(80);
						pg.text(APP_EXPORT_MADE2, border, 125);
						pg.textSize(30);
						pg.text(APP_EXPORT_MADE3, border, 175);
						pg.text(APP_EXPORT_MADE4, border, 220);
						pg.popStyle();

						pg.popMatrix();
					}


					pg.translate(0, thumbnailHeight);

				}
				pg.popMatrix();
				pg.pushStyle();
				pg.stroke(0);
				pg.fill(255);
				pg.rect(width/2-75, 0, 150, height);
				pg.line(width/2, 0, width/2, height);
				pg.popStyle();

				pg.endDraw();


				pg.save(output+"/"+uid+"_"+p+".png");
				time+=step;
			}
		}
		// In case something bad happens
		catch(Exception e) {
			return false;
		}
		return true;
	}

}