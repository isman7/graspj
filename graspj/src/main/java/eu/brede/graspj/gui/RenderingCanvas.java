package eu.brede.graspj.gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.StackWindow;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;

import javax.swing.Timer;

import eu.brede.common.util.DeepCopy;
import eu.brede.common.util.MathTools;
import eu.brede.graspj.configs.rendering.RenderConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.Option;
import eu.brede.graspj.datatypes.Rendering;
import eu.brede.graspj.pipeline.processors.renderer.SpotRenderer;
import eu.brede.graspj.utils.GroupManager;
import eu.brede.graspj.utils.GroupManager.PushActionHelper;

public class RenderingCanvas extends ImageCanvas {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private AnalysisItem item;
	private SpotRenderer renderer;
	
	private RenderConfig origConfig;
//	private Timer scrollTimer;
	private long scrollTime=0;
	private long requiredTime=200;
//	private boolean scrolling=false;
	private Option pushActions;
	private boolean autoContrast;

//	transient private Timer timer;
	transient private double relativeMagnification;
	transient private long lastRefresh=0;

	private Thread refreshThread;
	
	public RenderingCanvas(final AnalysisItem item, SpotRenderer renderer,
			Option pushActions, Option receiveActions, boolean autoContrast) {
		super(initRendering(item,renderer).getImagePlus(renderer));
//		getImage().setTitle(renderer.getConfig().getMetaData().getName() 
//				+ " - " + getImage().getTitle());
		this.item = item;
		this.renderer = renderer;
		this.pushActions = pushActions;
		this.autoContrast = autoContrast;
//		initRefreshRequestTimer();
		GroupManager.registerReceiver(this,receiveActions);
		
//		renderer.getConfig().put("renderWidth", 512);
//		renderer.getConfig().put("renderHeight", 512);
//		renderer.getConfig().put("pixelSize",
//				renderer.getConfig()
//					.calcPixelSize(item.getAcquisitionConfig()));
		
//		origConfig = new RenderConfig(renderer.getConfig());
		origConfig = DeepCopy.copy(renderer.getConfig());
		
//		new ContrastEnhancer().stretchHistogram(imp.getProcessor(), 0.5);
		
//		java.util.Timer projectTimer = new java.util.Timer();
//		projectTimer.scheduleAtFixedRate(new TimerTask() {
//			private int tempSpotCount = 0;
//			@Override
//			public void run() {
//				if(tempSpotCount != item.getSpots().getSpotCount()) {
//					tempSpotCount = item.getSpots().getSpotCount();
//					RenderingCanvas.this.reRenderImage();
//				}
//			}
//		}, 200, 200);
	}
	
//	public RenderingCanvas(final AnalysisItem item, SpotRenderer renderer, Option pushActions, Option pushActions) {
//		this(item,renderer,pushActions,false);
//	}
	
	public RenderingCanvas(final AnalysisItem item, SpotRenderer renderer) {
		this(item,renderer,new Option(),new Option(),false);
	}
	
	
//	@Override
//	protected void finalize() throws Throwable {
//		// won't work like this as GM holds reference
//		GroupManager.unRegisterReceiver(this);
//		super.finalize();
//	}

	public void alwaysFitCanvasToWindow() {

		imp.getWindow().addComponentListener(new ComponentAdapter() {
			// TODO 100 is hard coded, improve!
			Timer timer = new Timer(100, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					RenderingCanvas.this.fitToWindow();
				}
				
			});
			@Override
			public void componentResized(ComponentEvent e) {
				timer.setRepeats(false);
				timer.restart();
			}
			
		});
	}
	
	
	@Override
	protected void scroll(int sx, int sy) {
		// TODO 100 is hard coded, improve!
		if(System.currentTimeMillis()-scrollTime > requiredTime) {
			scrollTime = System.currentTimeMillis();
			int shiftX = sx-xMouseStart;
			int shiftY = sy-yMouseStart;
			xMouseStart = sx;
			yMouseStart = sy;
			long startTime = System.currentTimeMillis();
			shift(shiftX,shiftY);
			requiredTime = (long) ((System.currentTimeMillis()-startTime)*1.3);
//					scrollTime = System.currentTimeMillis();
		}
	}

	@Override
	public void fitToWindow() {
		
		ImageWindow win = imp.getWindow();
		if (win==null) return;
		Rectangle bounds = win.getBounds();
		Insets insets = win.getInsets();
		int sliderHeight = (win instanceof StackWindow)?20:0;
		int canvasWidth = (bounds.width-10);
		int canvasHeight = (bounds.height-(10+insets.top+sliderHeight));
		
//		rendering.getConfig().put("renderWidth", getSize().width-16);
//		rendering.getConfig().put("renderHeight", getSize().height-8);
		
		int imageWidth = canvasWidth-16;
		int imageHeight = canvasHeight-8;
		renderer.getConfig().put("renderWidth", imageWidth);
		renderer.getConfig().put("renderHeight", imageHeight);
		
//		setDrawingSize(canvasWidth, canvasHeight);
//		getParent().doLayout();
//		win.pack();
		
		srcRect=new Rectangle(0,0,imageWidth, imageHeight);
//		setDrawingSize(canvasWidth, canvasHeight);
		setDrawingSize(imageWidth, imageHeight);
		getParent().doLayout();
		
		requestRefresh();
//		getParent().doLayout();
	}
	
	private static Rendering initRendering(AnalysisItem item, SpotRenderer renderer) {
//		renderer.getConfig().put("renderWidth", 512);
//		renderer.getConfig().put("renderHeight", 512);
		renderer.getConfig().put("pixelSize",
				renderer.getConfig()
			.calcPixelSize(item.getAcquisitionConfig()));
		
		renderer.process(item);
		return renderer.getRendering();
	}
	
	private void shift(final int sx, final int sy) {
		new PushActionHelper<RenderingCanvas>(this,pushActions) {

			@Override
			public void action(RenderingCanvas object) {
				object.silentShift(sx, sy);
			}
		}.push();
		
		silentShift(sx, sy);
	}
	
	private void silentShift(int sx, int sy) {
//		if(pushAction) {
//			for(RenderingCanvas canvas : getRenderingCanvases()) {
//				canvas.shift(sx, sy);
//			}
//		}
		RenderConfig config = renderer.getConfig();
		float offsetX = config.getFloat("offsetX");
		float offsetY = config.getFloat("offsetY");
		float pixelSize = config.getFloat("pixelSize");
		offsetX += sx*pixelSize;
		offsetY += sy*pixelSize;
		config.put("offsetX", offsetX);
		config.put("offsetY", offsetY);
		requestRefresh();
//		imp.getWindow().pack();
//		getParent().doLayout();
	}
	
	private void zoomRelativeAround(double relativeMagnification, int sx, int sy) {
		
		RenderConfig config = renderer.getConfig();

		Point2D.Float newCenter = config.calcPosNM(sx, sy);
		 		
//		Dimension canvasSize = getSize();
//
//		config.put("renderWidth", canvasSize.width);
//		config.put("renderHeight", canvasSize.height);
		
		config.put("pixelSize", 
				(float) (config.getFloat("pixelSize")/relativeMagnification));
		
		config.updateOffset(newCenter);
		

//		double tempMagnification = getMagnification();
//		setMagnification(1.0f);
		requestRefresh(relativeMagnification);
		
//		System.out.println("before: " + getMagnification());
//		setMagnification(tempMagnification*relativeMagnification);
//		System.out.println("after: " + getMagnification());
		
		
//		System.out.println("relative: " + relativeMagnification);
		setMagnification(1.0f);
	}
	
	synchronized private void requestRefresh(double newRelativeMagnification) {
		final double currentRelativeMagnification = newRelativeMagnification*relativeMagnification;
		long now = System.currentTimeMillis();
//		System.out.println("now-latest: " + (now-lastRefresh));
		boolean deny = false;
		if(refreshThread!=null) {
			if(refreshThread.isAlive()) {
				deny = true;
			}
		}
		if(!deny && (now-lastRefresh > 100)) {
//			System.out.println("request accepted");
			relativeMagnification = 1;
			lastRefresh=now;
			refreshThread = new Thread(new Runnable() {
				@Override
				public void run() {
					refreshRendering(currentRelativeMagnification);
				}
			});
//			refreshThread.setPriority(Thread.MAX_PRIORITY);
			refreshThread.start();
		}
		else {
			relativeMagnification = currentRelativeMagnification;
		}
		return;
	}
	
	private void refreshRendering(double relativeMagnification) {
		try {
			
			if(renderer.getRendering()!=null) {
				renderer.getRendering().clean();				
			}
			
			double min = imp.getProcessor().getMin();
			double max = imp.getProcessor().getMax();
			ColorModel oldCM = imp.getProcessor().getCurrentColorModel();
			double areaMultiplier = relativeMagnification*relativeMagnification;
			double renderedFraction = 
				renderer.getConfig().calcRenderedFraction(origConfig);

			// TODO perhaps coerce in SpotRenderer instead?!
			int spotsPerWorker = MathTools.coerce(1.0/renderedFraction, 1, 100)
									.intValue();

//			System.out.println("fraction: " + renderedFraction);						
			renderer.getConfig().put("spotsPerWorker", spotsPerWorker);
			renderer.process(item);
			ImagePlus newImp = renderer.getRendering().getImagePlus(renderer);
			
			if(autoContrast) {				
				IJ.run(newImp, "Enhance Contrast", "saturated=0.35");
//				new ContrastEnhancer().stretchHistogram(imp, 0.5);
//				imp.getProcessor().resetMinAndMax();
//				imp.updateAndDraw();
			}
			else {
				newImp.getProcessor().setMinAndMax(min/areaMultiplier, max/areaMultiplier);
			}
			
			imp.setProcessor(newImp.getProcessor());
			imp.getProcessor().setColorModel(oldCM);
			
			imp.setCalibration(newImp.getCalibration());
//			ScaleBar scaleBar = new ScaleBar();
//			scaleBar.
			
			repaint();
//			setImageUpdated();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	private void initRefreshRequestTimer() {
//		timer = new Timer(100, new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				Global.INSTANCE.getExecutor().submit(new Runnable() {
//					@Override
//					public void run() {
//						refreshRendering();
//					}
//				});
//			}
//			
//		});
//		timer.setRepeats(false);
//	}
	
	
	public void requestRefresh() {
		requestRefresh(1.0);
	}
	
	@Override
	public void zoomIn(final int sx, final int sy) {
		new PushActionHelper<RenderingCanvas>(this,pushActions) {

			@Override
			public void action(RenderingCanvas object) {
				object.zoomInSilent(sx, sy);
			}
		}.push();
		zoomInSilent(sx, sy);
	}
	
	protected void zoomInSilent(int sx, int sy) {
		double oldMagnification = getMagnification();
		double newMagnification = getHigherZoomLevel(oldMagnification);
		double relativeMagnification = newMagnification/oldMagnification;
		
		zoomRelativeAround(relativeMagnification,sx,sy);
		return;
	}

	@Override
	public void zoomOut(final int x, final int y) {
		new PushActionHelper<RenderingCanvas>(this,pushActions) {

			@Override
			public void action(RenderingCanvas object) {
				object.zoomOutSilent(x, y);
			}
		}.push();
		zoomOutSilent(x, y);
	}
	
	protected void zoomOutSilent(int x, int y) {
		double oldMagnification = getMagnification();
		double newMagnification = getLowerZoomLevel(oldMagnification);
		double relativeMagnification = newMagnification/oldMagnification;
		
		zoomRelativeAround(relativeMagnification,x,y);
		return;
	}
	
	@Override
	public void unzoom() {
		new PushActionHelper<RenderingCanvas>(this,pushActions) {

			@Override
			public void action(RenderingCanvas object) {
				object.unzoomSilent();
			}
		}.push();
		unzoomSilent();
		
	}
	
	@Override
	public void zoom100Percent() {
		unzoom();
	}
	
	protected void unzoomSilent() {
		renderer.setConfig(DeepCopy.copy(origConfig));
	}
	
//	private ArrayList<RenderingCanvas> getRenderingCanvases() {
//		ArrayList<RenderingCanvas> canvases = new ArrayList<>();
//		int[] idList = WindowManager.getIDList();
//		if(idList == null) {
//			return canvases;
//		}
//		for(int id : idList) {
//			ImagePlus imp = WindowManager.getImage(id);
//			Canvas canvas = imp.getCanvas();
//			if(canvas instanceof RenderingCanvas) {
//				if(canvas!=this) {
//					canvases.add((RenderingCanvas) canvas);
//				}
//			}
//		}
//		return canvases;
//	}

}
