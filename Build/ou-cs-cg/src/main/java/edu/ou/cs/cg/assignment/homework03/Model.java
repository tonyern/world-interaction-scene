//******************************************************************************
// Copyright (C) 2019 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Wed Feb 27 17:27:48 2019 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20190227 [weaver]:	Original file.
//
//******************************************************************************
//
// The model manages all of the user-adjustable variables utilized in the scene.
// (You can store non-user-adjustable scene data here too, if you want.)
//
// For each variable that you want to make interactive:
//
//   1. Add a member of the right type
//   2. Initialize it to a reasonable default value in the constructor.
//   3. Add a method to access a copy of the variable's current value.
//   4. Add a method to modify the variable.
//
// Concurrency management is important because the JOGL and the Java AWT run on
// different threads. The modify methods use the GLAutoDrawable.invoke() method
// so that all changes to variables take place on the JOGL thread. Because this
// happens at the END of GLEventListener.display(), all changes will be visible
// to the View.update() and render() methods in the next animation cycle.
//
//******************************************************************************

package edu.ou.cs.cg.assignment.homework03;

//import java.lang.*;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.*;
import com.jogamp.opengl.*;
import edu.ou.cs.cg.utilities.Utilities;

//******************************************************************************

/**
 * The <CODE>Model</CODE> class.
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class Model
{
	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final View					view;

	// Model variables
	private Point2D.Double				origin;	// Current origin coordinates
	private Point2D.Double				cursor;	// Current cursor coordinates
	private ArrayList<Point2D.Double>	points;	// Drawn star points
	private ArrayList<Point2D.Double>   zigZagFences; // Interactive fences
	private ArrayList<Point2D.Double>   jagFences; // Interactive fences
	
	// Model variables for adjusting color
	private int redValue;
	private int blueValue;
	private int greenValue;
	
	// Model variables for adjusting number of sides
	private int numberOfSides;
	
	// Model variables for moving the moon around the scene.
	// moonLight is the bright side of the moon.
	private int moonLightX;
	private int moonLightY;
	// moonDark is the dark side of the moon.
	private int moonDarkX;
	private int moonDarkY;
	
	// Model variables for moving the flag up or down the pole.
	private boolean flagUp;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public Model(View view)
	{
		this.view = view;

		// Initialize user-adjustable variables (with reasonable default values)
		origin = new Point2D.Double(0.0, 0.0);
		cursor = null;
		points = new ArrayList<Point2D.Double>();
		zigZagFences = new ArrayList<Point2D.Double>();
		jagFences = new ArrayList<Point2D.Double>();
		
		// Default color for the sky.
		redValue = 128;
		blueValue = 112;
		greenValue = 80;
		
		// 5 side star is the default.
		numberOfSides = 5;
		
		// Default X's and Y's for the moon.
		moonLightX = 94;
		moonLightY = 720 - 92;
		moonDarkX = 128;
		moonDarkY = 650;
		
		// Flag is at the top of the pole as default.
		flagUp = true;
	}

	//**********************************************************************
	// Public Methods (Access Variables)
	//**********************************************************************

	public Point2D.Double	getOrigin()
	{
		return new Point2D.Double(origin.x, origin.y);
	}

	public Point2D.Double	getCursor()
	{
		if (cursor == null)
		{
			return null;
		}
		else
		{
			return new Point2D.Double(cursor.x, cursor.y);
		}
	}

	public List<Point2D.Double>	getPolyline()
	{
		return Collections.unmodifiableList(points);
	}
	
	public List<Point2D.Double> getZigZagFences()
	{
		return Collections.unmodifiableList(zigZagFences);
	}
	
	public List<Point2D.Double> getJagFences()
	{
		return Collections.unmodifiableList(jagFences);
	}
	
	public int getRedValue()
	{
		return redValue;
	}
	
	public int getGreenValue()
	{
		return greenValue;
	}
	
	public int getBlueValue()
	{
		return blueValue;
	}
	
	public int getNumberOfSides()
	{
		return numberOfSides;
	}
	
	public int getMoonLightX()
	{
		return moonLightX;
	}
	
	public int getMoonLightY()
	{
		return moonLightY;
	}
	
	public int getMoonDarkX()
	{
		return moonDarkX;
	}
	
	public int getMoonDarkY()
	{
		return moonDarkY;
	}
	
	public boolean getFlagPosition()
	{
		return flagUp;
	}

	//**********************************************************************
	// Public Methods (Modify Variables)
	//**********************************************************************
	
	public void flipFence(Point q)
	{
		System.out.println("Fence at: " + q);
	}
	
	public void moveLeft()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				moonLightX -= 5;
				moonDarkX -= 5;
			}
		});;
	}
	
	public void moveRight()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				moonLightX += 5;
				moonDarkX += 5;
			}
		});;
	}
	
	public void moveUp()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				moonLightY += 5;
				moonDarkY += 5;
			}
		});;
	}
	
	public void moveDown()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				moonLightY -= 5;
				moonDarkY -= 5;
			}
		});;
	}
	
	public void sidesOfStar(int numberOfSides)
	{
		this.numberOfSides = numberOfSides;
	}
	
	public void changeSkyColor(int mode)
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl)
			{
				if (mode == 1)
				{
					redValue = 247;
					greenValue = 129;
					blueValue = 129;	
				}
				else if (mode == 2)
				{
					redValue = 180;
					greenValue = 141;
					blueValue = 238;
				}
				else if (mode == 3)
				{
					redValue = 93;
					greenValue = 249;
					blueValue = 249;
				}
				else if (mode == 4)
				{
					redValue = 198;
					greenValue = 238;
					blueValue = 206;
				}
				else if (mode == 5)
				{
					redValue = 253;
					greenValue = 255;
					blueValue = 179;
				}
			}
		});;
	}
	
	public void deleteOldestPoint()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				if (points.size() != 0)
				{
					points.remove(0);
				}
			}
		});;
	}
	
	public void deleteNewestPoint()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				if (points.size() != 0)
				{
					points.remove(points.size() - 1);
				}
			}
		});;
	}
	
	public void removeSpecificStar(Point2D.Double cursorPosition)
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				if (points.size() != 0)
				{
					int i = 0;
					for (Point2D.Double p : points)
					{
						if (cursorPosition.x - 25 <= p.x
								&& cursorPosition.x + 25 >= p.x)
						{
							points.remove(i);
							break;
						}
						i++;
					}
				}
			}
		});;
	}
	
	public void	setOriginInSceneCoordinates(Point2D.Double q)
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				origin = new Point2D.Double(q.x, q.y);
			}
		});;
	}

	public void	setOriginInViewCoordinates(Point q)
	{
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			public void	update(double[] p) {
				origin = new Point2D.Double(p[0], p[1]);
			}
		});;
	}

	public void	setCursorInViewCoordinates(Point q)
	{
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			public void	update(double[] p) {
				cursor = new Point2D.Double(p[0], p[1]);
			}
		});;
	}

	public void	turnCursorOff()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				cursor = null;
			}
		});;
	}
	
	public void addZigZagFences(Double points)
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				zigZagFences.add(points);
			}
		});;
	}
	
	public void addJaggedFences(Double points)
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				jagFences.add(points);
			}
		});;
	}
	
	public void deleteJaggedFences(Double points)
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				jagFences.remove(points);
			}
		});;
	}

	public void	addPolylinePointInViewCoordinates(Point q)
	{
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			public void	update(double[] p) {
				points.add(new Point2D.Double(p[0], p[1]));
				//System.out.println("Adding star at: (" + p[0] + ", " + p[1] + ")");
			}
		});;
	}

	public void	clearScreen()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				points.clear();
			}
		});;
	}

	//**********************************************************************
	// Inner Classes
	//**********************************************************************

	// Convenience class to simplify the implementation of most updaters.
	private abstract class BasicUpdater implements GLRunnable
	{
		public final boolean	run(GLAutoDrawable drawable)
		{
			GL2	gl = drawable.getGL().getGL2();

			update(gl);

			return true;	// Let animator take care of updating the display
		}

		public abstract void	update(GL2 gl);
	}

	// Convenience class to simplify updates in cases in which the input is a
	// single point in view coordinates (integers/pixels).
	private abstract class ViewPointUpdater extends BasicUpdater
	{
		private final Point	q;
		
		public ViewPointUpdater(Point q)
		{
			this.q = q;
		}

		public final void	update(GL2 gl)
		{
			int		h = view.getHeight();
			double[]	p = Utilities.mapViewToScene(gl, q.x, h - q.y, 0.0);

			update(p);
		}

		public abstract void	update(double[] p);
	}
}

//******************************************************************************

