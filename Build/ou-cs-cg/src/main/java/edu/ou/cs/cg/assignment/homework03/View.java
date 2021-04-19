//******************************************************************************
// Copyright (C) 2016-2019 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Sun Feb  2 17:56:41 2020 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20160209 [weaver]:	Original file.
// 20190129 [weaver]:	Updated to JOGL 2.3.2 and cleaned up.
// 20190203 [weaver]:	Additional cleanup and more extensive comments.
// 20190206 [weaver]:	Heavily reduced version of old Homework 02 solution.
// 20200121 [weaver]:	Modified to set up OpenGL and UI on the Swing thread.
//
//******************************************************************************
// Notes:
//
// Warning! This code uses deprecated features of OpenGL, including immediate
// mode vertex attribute specification, for sake of easier classroom learning.
// See www.khronos.org/opengl/wiki/Legacy_OpenGL
//
//******************************************************************************

package edu.ou.cs.cg.assignment.homework03;

//import java.lang.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
//import java.awt.event.*;
//import java.awt.geom.*;
import java.util.*;
//import javax.swing.*;
import com.jogamp.opengl.*;
//import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

import edu.ou.cs.cg.assignment.homework03.KeyHandler;
import edu.ou.cs.cg.assignment.homework03.Model;
import edu.ou.cs.cg.assignment.homework03.MouseHandler;
import edu.ou.cs.cg.utilities.Utilities;

//******************************************************************************

/**
 * The <CODE>Application</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @author  Tony Nguyen
 * @version %I%, %G%
 */
public final class View
	implements GLEventListener
{
	//**********************************************************************
	// Public Class Members
	//**********************************************************************

	public static final GLU	GLU = new GLU();
	public static final GLUT	GLUT = new GLUT();
	public static final Random	RANDOM = new Random();
	private static final DecimalFormat	FORMAT = new DecimalFormat("0.000");

	//**********************************************************************
	// Private Members (State internal variables)
	//**********************************************************************
	
	private final GLJPanel canvas;
	private final FPSAnimator animator;
	private final Model	model;
	
	private final KeyHandler keyHandler;
	private final MouseHandler mouseHandler;
	
	private int				w;				// Canvas width
	private int				h;				// Canvas height
	private int				k;			    // Animation counter
	
	private TextRenderer	renderer;
	private float			thickline;		// Line thickness
	
	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public View(GLJPanel canvas)
	{
		k = 0;
		
		// Register this class to update whenever OpenGL needs it
		this.canvas = canvas;
		
		// Have OpenGL call display() to update the canvas 60 times per second
		canvas.addGLEventListener(this);
		
		// Initialize model (scene data and parameter manager)
		model = new Model(this);

		// Initialize controller (interaction handlers)
		keyHandler = new KeyHandler(this, model);
		mouseHandler = new MouseHandler(this, model);
			
		// Initialize animation
		animator = new FPSAnimator(canvas, 60);
		animator.start();
	}
	
	//**********************************************************************
	// Getters and Setters
	//**********************************************************************

	public GLJPanel	getCanvas()
	{
		return canvas;
	}

	public int	getWidth()
	{
		return w;
	}

	public int	getHeight()
	{
		return h;
	}
	
	public KeyHandler getKeyHandler()
	{
		return keyHandler;
	}
	
	public MouseHandler getMouseHandler()
	{
		return mouseHandler;
	}

	//**********************************************************************
	// Override Methods (GLEventListener)
	//**********************************************************************

	// Called immediately after the GLContext of the GLCanvas is initialized.
	public void	init(GLAutoDrawable drawable)
	{
		w = drawable.getSurfaceWidth();
		h = drawable.getSurfaceHeight();

		renderer = new TextRenderer(new Font("Serif", Font.PLAIN, 18),
									true, true);

		initPipeline(drawable);
	}

	// Notification to release resources for the GLContext.
	public void	dispose(GLAutoDrawable drawable)
	{
		renderer = null;
	}

	// Called to initiate rendering of each frame into the GLCanvas.
	public void	display(GLAutoDrawable drawable)
	{
		update(drawable);
		render(drawable);
	}

	// Called during the first repaint after a resize of the GLCanvas.
	public void	reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		this.w = w;
		this.h = h;
	}

	//**********************************************************************
	// Private Methods (Rendering)
	//**********************************************************************

	// Update the scene model for the current animation frame.
	private void	update(GLAutoDrawable drawable)
	{
		k++; // Advance animation counter
	}

	// Render the scene model and display the current animation frame.
	private void	render(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);		    // Clear the buffer.
		
		// Background sky.
		drawSky(gl, model.getRedValue(), model.getGreenValue(), model.getBlueValue());
		
		drawMain(gl);
		
		drawMoon(gl);
		
		// Background galaxy.
		setProjectionGalaxy(gl);
		drawLorenzAttractor(gl);
		
		setProjection(gl);							// Use screen coordinates.

		// Two houses.
		drawHouse1(gl);
		drawHouse2(gl);
		
		// Draw fences
		drawFences(gl);
		
		// Create the grass.
		drawGrass(gl);
		
		drawMode(drawable);
		
		// Draws a flag pole with a flag and ropes.
		drawFlag(gl, 600, 469, 185, 110, 255, 255, 255);
	}

	//**********************************************************************
	// Private Methods (Pipeline)
	//**********************************************************************

	// www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glBlendFunc.xml
	private void	initPipeline(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();
		
		// Make the sky gradient easier by enabling alpha blending.

		// See com.jogamp.opengl.GL
		gl.glEnable(GL2.GL_POINT_SMOOTH);	// Turn on point anti-aliasing
		gl.glEnable(GL2.GL_LINE_SMOOTH);	// Turn on line anti-aliasing

		gl.glEnable(GL.GL_BLEND);			// Turn on color channel blending
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
	}

	// Position and orient the default camera to view in 2-D, in pixel coords.
	private void	setProjection(GL2 gl)
	{
		// Main part of the scene.
		GLU	glu = new GLU();

		gl.glMatrixMode(GL2.GL_PROJECTION);		     // Prepare for matrix xform
		gl.glLoadIdentity();						 // Set to identity matrix
		glu.gluOrtho2D(0.0f, 1280.0f, 0.0f, 720.0f); // 2D translate and scale
	}
	
	// Position and orient the default camera to view in 2-D, in pixel coords.
	private void	setProjectionGalaxy(GL2 gl)
	{		
		GLU galaxy = new GLU();
			
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		galaxy.gluOrtho2D(-1.0f, 1.0f, -1.45f, 1.0f);
	}

	//**********************************************************************
	// Private Methods (Scene)
	//**********************************************************************
	
	// These pages are helpful:
	// en.wikipedia.org/wiki/Lorenz_system
	// www.algosome.com/articles/lorenz-attractor-programming-code.html
	private void	drawLorenzAttractor(GL2 gl)
	{
		gl.glPointSize(5.0f);					// Set point size (in pixels)
		gl.glBegin(GL.GL_POINTS);				// Start specifying points

		double		dt = 0.01;					// Integration step size
		double		sigma = 10.0;				// Constant for updating x
		double		rho = 28.0;				// Constant for updating y
		double		beta = 8.0 / 3.0;			// Constant for updating z
		double		lx = 0.1;					// Initial x coordinate
		double		ly = 0.0;					// Initial y coordinate
		double		lz = 0.0;					// Initial z coordinate

		for (int i=0; i<10000; i++)
		{
			double	llx = lx + dt * sigma * (ly - lx);
			double	lly = ly + dt * (lx * (rho - lz) - ly);
			double	llz = lz + dt * (lx * ly - beta * lz);

			lx = llx;
			ly = lly;
			lz = llz;
			//System.out.println(" " + lx + " " + ly + " " + lz);

			float	cc = (float)((lz + 30.0) / 60.0);
			int	n = k % 10000 - i;
			int	cw = (n + 20) * 6 + 15;

			// Lower background star opacity to experiment with animation.
			if (Math.abs(n) <= 20)							// Animation window
				setColor(gl, 255, 255-cw, cw, 255-cw);		// Dots cycling
			else
				gl.glColor4f(cc, cc, cc, 0.05f);			// Background stars

			double	dy = 0.00005 * k * (1.5 - 0.05 * lx);	// Galaxy rise

			gl.glVertex2d(-lx / 30.0, ly / 30.0 + dy);
		}

		gl.glEnd();
		gl.glPointSize(1.0f);					// Reset point size (in pixels)
	}
	
	/**
	 * Draws the gradients for the night sky.
	 * 
	 * @param gl OpenGL object.
	 * @param redValue Red color scheme
	 * @param greenValue Green color scheme
	 * @param blueValue Blue color scheme
	 */
	private void	drawSky(GL2 gl, int redValue, int greenValue, int blueValue)
	{
		gl.glBegin(GL2.GL_QUADS);

		// Opaque gold on horizon
		setColor(gl, redValue, greenValue, blueValue);
		gl.glVertex2i(0, 131);
		gl.glVertex2i(1280, 131);
		setColor(gl, 32, 48, 96, 64);		// Translucent dark blue at top
		gl.glVertex2i(1280, 720);
		gl.glVertex2i(0, 720);

		gl.glEnd();
	}
	
	private static final int		SIDES_MOON = 18;
	private static final double	ANGLE_MOON = 2.0 * Math.PI / SIDES_MOON;

	/**
	 * Draws the moon.
	 * 
	 * @param gl OpenGL object.
	 */
	private void	drawMoon(GL2 gl)
	{
		double		theta = 0.20 * ANGLE_MOON;
		int		cx = model.getMoonLightX();
		int		cy = model.getMoonLightY();
		int		r = 59;

		// Fill the whole moon in white
		gl.glBegin(GL.GL_TRIANGLE_FAN);

		setColor(gl, 255, 255, 255);			// White
		gl.glVertex2d(cx, cy);

		for (int i=0; i<SIDES_MOON+1; i++)		// 18 sides
		{
			gl.glVertex2d(cx + r * Math.cos(theta), cy + r * Math.sin(theta));
			theta += ANGLE_MOON;
		}

		gl.glEnd();

		// Fill the outside shadow in dark bluish gray
		theta = -1.80 * ANGLE_MOON;

		gl.glBegin(GL.GL_TRIANGLE_FAN);

		setColor(gl, 64, 64, 80);
		gl.glVertex2d(cx, cy);

		for (int i=0; i<8; i++)				// 7 sides
		{
			gl.glVertex2d(cx + r * Math.cos(theta), cy + r * Math.sin(theta));
			theta += ANGLE_MOON;
		}

		gl.glEnd();

		// Fill the inside shadow in dark bluish gray
		theta = 1.50 * ANGLE_MOON;
		cx = model.getMoonDarkX();
		cy = model.getMoonDarkY();
		theta = 7.2 * ANGLE_MOON;

		gl.glBegin(GL.GL_TRIANGLE_FAN);

		setColor(gl, 64, 64, 80);
		gl.glVertex2d(cx, cy);

		for (int i=0; i<8; i++)				// 7 sides
		{
			gl.glVertex2d(cx + r * Math.cos(theta), cy + r * Math.sin(theta));
			theta += ANGLE_MOON;
		}

		gl.glEnd();
	}

	// Draw house1 which is the on the left side of the scene.
	private void	drawHouse1(GL2 gl)
	{
		int	dx = 108;
		int	dy = 132;

		// Chimney is on the left side of the house.
		drawChimney(gl, dx + 34, dy, 128, 0, 0);
		// Frame of house.
		drawFrame(gl, dx, dy, 128, 64, 0);
		
		// Window to the left of the door.
		drawWindow(gl, dx + 141, dy + 127, 20, 20, 255, 255, 128);
		
		// Window to the right of the door with shades.
		drawWindow(gl, dx + 34, dy + 127, 20, 20, 255, 255, 128);
		
		// Window shades to the left of the door.
		drawShades(gl, dx + 14, dy + 107, 20, 40, 118, 219, 219, 0);
		drawShades(gl, dx + 33, dy + 107, 20, 40, 118, 219, 219, 1);
		
		// Window shades to the right of the door.
		drawShades(gl, dx + 121, dy + 107, 20, 40, 118, 219, 219, 0);
		drawShades(gl, dx + 140, dy + 107, 20, 40, 118, 219, 219, 1);
		
		// Door is centered of the house.
		drawDoor(gl, dx + 65, dy, 192, 128, 0);
		// Window on the door.
		drawWindow(gl, dx + 85, dy + 65, 15, 15, 255, 255, 128);
		// Door knob on the door.
		drawDoorKnob(gl, dx + 95, dy + 35, 4, 255, 255, 128);
		
		drawHouseStar(gl, dx + 87, dy + 200);
	}

	// Draw the parts of a slightly different house.
	private void	drawHouse2(GL2 gl)
	{
		int	dx = 1048;
		int	dy = 132;

		// Chimney is on the right side of the house.
		drawChimney(gl,	dx + 120, dy, 40, 100, 90);
		// Frame of the house.
		drawFrame(gl, dx, dy, 128, 64, 0);
		// House has a divided roof.
		
		// Windows on the house.
		drawWindow(gl, dx + 98, dy + 64, 20, 20, 225, 225, 128);
		drawWindow(gl, dx + 144, dy + 64, 20, 20, 225, 255, 128);
		
		// Shades on the left window.
		drawShades(gl, dx + 78, dy + 44, 20, 40, 118, 219, 219, 0);
		drawShades(gl, dx + 97, dy + 44, 20, 40, 118, 219, 219, 1);
		
		// Shades on the right window.
		drawShades(gl, dx + 123, dy + 44, 20, 40, 118, 219, 219, 0);
		drawShades(gl, dx + 143, dy + 44, 20, 40, 118, 219, 219, 1);
		
		// Door on the house with a door knob.
		drawDoor(gl, dx + 7, dy, 192, 128, 0);
		drawDoorKnob(gl, dx + 15, dy + 35, 4, 255, 255, 128);
		// Window on the door.
		drawDoorKnob(gl, dx + 25, dy + 70, 15, 255, 255, 128);
		
		drawHouseStar(gl, dx + 87, dy + 200);
	}
	
	/**
	 * Creates the grass in a scene when called.
	 * 
	 * @param gl OpenGL object
	 */
	private void	drawGrass(GL2 gl)
	{
		// Set color and fill the grass.
		setColor(gl, 20, 45, 0);
		fillRect(gl, 0, 0, 1280, 131);		
	}
	
	/**
	 * This method draws a filled circle with an outer edge.
	 * 
	 * @param gl OpenGL object
	 * @param dx X-coordinate
	 * @param dy Y-coordinate
	 * @param r Radius
	 * @param redValue Red color scheme
	 * @param greenValue Green color scheme
	 * @param blueValue Blue color scheme
	 */
	private void	drawDoorKnob(GL2 gl, int dx, int dy, int r, 
			int redValue, int greenValue, int blueValue)
	{
		// Set color and fill the circle.
		setColor(gl, redValue, greenValue, blueValue);
		fillCircle(gl, dx, dy, r);
		
		// Set color and fill the edge of the circle.
		setColor(gl, 0, 0, 0);
		edgeCircle(gl, dx, dy, r);
	}
	
	/**
	 * Draws a flag with a pole and rope.
	 * 
	 * @param gl OpenGL object
	 * @param dx X-coordinate
	 * @param dy Y-coordinate
	 * @param ww Width
	 * @param hh Height
	 * @param redValue Red color scheme
	 * @param greenValue Green color scheme
	 * @param blueValue Blue color scheme
	 */
	private void	drawFlag(GL2 gl, int dx, int dy, int ww, int hh,
			int redValue, int greenValue, int blueValue)
	{	
		// TODO Start drawing the flag.
		setColor(gl, redValue, greenValue, blueValue);
		fillRect(gl, dx, dy, ww, hh);
		setColor(gl, 0, 0, 0);
		edgeRect(gl, dx, dy, ww, hh);
		drawDoorKnob(gl, dx + 95, dy + 55, 35, 169, 0, 0);

		// Draw the flag pole.
		drawFlagPole(gl, dx, dy);
	}
	
	private void	drawFlagPole(GL2 gl, int dx, int dy)
	{
		// Draw the flag pole.
		setColor(gl, 117, 117, 117);
		fillRect(gl, 595, 100, 10, 500);
		drawDoorKnob(gl, dx, dy + 140, 15, 178, 166, 0);
				
		// Draw the rope.
		setColor(gl, 203, 149, 0);
		drawRope(gl, 605, 578, 610, 540);
				
		setColor(gl, 203, 149, 0);
		drawRope(gl, 610, 540, 610, 200);
				
		setColor(gl, 203, 149, 0);
		drawRope(gl, 610, 200, 605, 190);
	}
	
	/**
	 * Draws a sequence of connect lines.
	 * 
	 * @param gl OpenGL object
	 * @param x1 Starting x position
	 * @param y1 Starting y position
	 * @param x2 Ending x position
	 * @param y2 Ending y position
	 */
	private void	drawRope(GL2 gl, int x1, int y1, int x2, int y2)
	{
		gl.glLineWidth(2.0f);
		gl.glBegin(GL.GL_LINES);
		
		// Draw lines given points.
		gl.glVertex2d(x1, y1);
		gl.glVertex2d(x2, y2);
		
		gl.glEnd();
	}
	
	/**
	 * Draw shades which are just two triangles.
	 * 
	 * @param gl OpenGL object
	 * @param dx X-coordinate
	 * @param dy Y-coordinate
	 * @param ww Width
	 * @param hh Height
	 * @param redValue Red color scheme
	 * @param greenValue Green color scheme
	 * @param blueValue Blue color scheme
	 * @param rightOrLeft 0 or 1. 0 denotes left while 1 denotes right
	 */
	private void	drawShades(GL2 gl, int dx, int dy, int ww, int hh,
			int redValue, int greenValue, int blueValue, int rightOrLeft)
	{
		// Set color and fill the shades.
		setColor(gl, redValue, greenValue, blueValue);
		fillTriangle(gl, dx, dy, ww, hh, rightOrLeft);
		
		// Black color edge of the shades.
		setColor(gl, 0, 0, 0);
		edgeTriangle(gl, dx, dy, ww, hh, rightOrLeft); 
	}

	/**
	 * Draws a chimney on the scene when called.
	 * 
	 * @param gl OpenGL object
	 * @param dx X-coordinate
	 * @param dy Y-coordinate
	 * @param redValue Red color scheme
	 * @param greenValue Green color scheme
	 * @param blueValue Blue color scheme
	 */
	private void	drawChimney(GL2 gl, int dx, int dy,
			int redValue, int greenValue, int blueValue)
	{
		// Set color and fill the chimney.
		setColor(gl, redValue, greenValue, blueValue);
		fillRect(gl, dx, dy, 30, 250);

		// Black color edge of the chimney.
		setColor(gl, 0, 0, 0);
		edgeRect(gl, dx, dy, 30, 250);
	}
	
	/**
	 * Draws star on house.
	 * 
	 * @param gl OpenGL object.
	 * @param cx X-Coordinate.
	 * @param cy Y-Coordinate.
	 */
	private void	drawHouseStar(GL2 gl, int cx, int cy)
	{
		double	theta = 0.5 * Math.PI;

		setColor(gl, 255, 255, 0);
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex2d(cx, cy);
		doStarVertices(gl, cx, cy, model.getNumberOfSides(), 20.0, 8.0);
		gl.glVertex2d(cx + 20 * Math.cos(theta), cy + 20 * Math.sin(theta));
		gl.glEnd();

		setColor(gl, 0, 0, 0);
		gl.glBegin(GL.GL_LINE_STRIP);
		doStarVertices(gl, cx, cy, model.getNumberOfSides(), 20.0, 8.0);
		gl.glVertex2d(cx + 20 * Math.cos(theta), cy + 20 * Math.sin(theta));
		gl.glEnd();
	}

	// Define five corners of a house frame that is shorter on the left side.
	private static final Point[]	OUTLINE_FRAME = new Point[]
	{
		new Point(  0,   0),		// base, left corner
		new Point(176,   0),		// base, right corner
		new Point(176, 162),		// roof, right corner
		new Point( 88, 250),		// roof, apex
		new Point(  0, 162),		// roof, left corner
	};

	/**
	 * Draw a house frame, given its lower left corner.
	 * 
	 * @param gl OpenGL object
	 * @param dx X-coordinate
	 * @param dy Y-coordinate
	 * @param redValue Red color scheme
	 * @param greenValue Green color scheme
	 * @param blueValue Blue color scheme
	 */
	private void	drawFrame(GL2 gl, int dx, int dy,
			int redValue, int greenValue, int blueValue)
	{
		// Set color and fill the house frame.
		setColor(gl, redValue, greenValue, blueValue);
		fillPoly(gl, dx, dy, OUTLINE_FRAME);

		// Black color edge of the house frame.
		setColor(gl, 0, 0, 0);
		edgePoly(gl, dx, dy, OUTLINE_FRAME);
	}

	/**
	 * Draws a door on the scene when called.
	 * 
	 * @param gl OpenGL object
	 * @param dx X-coordinate
	 * @param dy Y-coordinate
	 * @param redValue Red color scheme
	 * @param greenValue Green color scheme
	 * @param blueValue Blue color scheme
	 */
	private void	drawDoor(GL2 gl, int dx, int dy,
			int redValue, int greenValue, int blueValue)
	{	
		// Set color and fill the door.
		setColor(gl, redValue, greenValue, blueValue);
		fillRect(gl, dx, dy, 40, 92);

		// Black color edge of the door.
		setColor(gl, 0, 0, 0);
		edgeRect(gl, dx, dy, 40, 92);
	}

	/**
	 * Draw a window, given its center.
	 * 
	 * @param gl OpenGL object
	 * @param dx X-coordinate
	 * @param dy Y-coordinate
	 * @param ww Width
	 * @param hh Height
	 * @param redValue Red color scheme
	 * @param greenValue Green color scheme
	 * @param blueValue Blue color scheme
	 */
	private void	drawWindow(GL2 gl, int dx, int dy, int ww, int hh,
			int redValue, int greenValue, int blueValue)
	{
		// Set window and fill the window.
		setColor(gl, redValue, greenValue, blueValue);
		fillRect(gl, dx - ww, dy - hh, 2 * ww, 2 * hh);

		// Black color edge.drawShades(gl, dx + 14, dy + 107, 20, 40, 118, 219, 219, 0);
		setColor(gl, 0, 0, 0);
		edgeRect(gl, dx - ww, dy - hh, 2 * ww, 2 * hh);
	}

	//**********************************************************************
	// Private Methods (Scene, Fence)
	//**********************************************************************
	
	private void	drawFences(GL2 gl)
	{
		drawZigZagFences(gl);
		drawJaggedFences(gl);
	}
	
	private void	drawZigZagFences(GL2 gl)
	{
		fillFenceStrip(gl, 283, 132, 8);
		edgeFenceStrip(gl, 283, 132, 8);

		fillFenceStrip(gl, 12, 132, 4);
		edgeFenceStrip(gl, 12, 132, 4);
		
		gl.glEnd();
	}
	
	private void	drawJaggedFences(GL2 gl)
	{
		// Draw a rightward-increasing jagged fence
		// TODO: False would be right jag, true would be left.
		fillFenceBoard(gl, false,  1024, 132);
		edgeFenceBoard(gl, false,  1024, 132);
		model.addJaggedFences(new Point2D.Double(1024, 132));
		
		fillFenceBoard(gl, false,  1001, 132);
		edgeFenceBoard(gl, false,  1001, 132);
		model.addJaggedFences(new Point2D.Double(1001, 132));
		
		fillFenceBoard(gl, false,  978, 132);
		edgeFenceBoard(gl, false,  978, 132);
		model.addJaggedFences(new Point2D.Double(978, 132));
		
		fillFenceBoard(gl, false,  955, 132);
		edgeFenceBoard(gl, false,  955, 132);
		model.addJaggedFences(new Point2D.Double(955, 132));
		
		fillFenceBoard(gl, false,  932, 132);
		edgeFenceBoard(gl, false,  932, 132);
		model.addJaggedFences(new Point2D.Double(932, 132));
		
		fillFenceBoard(gl, false,  909, 132);
		edgeFenceBoard(gl, false,  909, 132);
		model.addJaggedFences(new Point2D.Double(909, 132));
		
		fillFenceBoard(gl, false,  886, 132);
		edgeFenceBoard(gl, false,  886, 132);
		model.addJaggedFences(new Point2D.Double(886, 132));
		
		fillFenceBoard(gl, false,  863, 132);
		edgeFenceBoard(gl, false,  863, 132);
		model.addJaggedFences(new Point2D.Double(863, 132));
		
		fillFenceBoard(gl, false,  1224, 132);
		edgeFenceBoard(gl, false,  1224, 132);
		model.addJaggedFences(new Point2D.Double(1224, 132));
		
		fillFenceBoard(gl, false,  1247, 132);
		edgeFenceBoard(gl, false,  1247, 132);
		model.addJaggedFences(new Point2D.Double(1247, 132));
		
		gl.glEnd();
	}

	// Fills a left-to-right sequence of fence boards using a QUAD_STRIP.
	private void	fillFenceStrip(GL2 gl, int dx, int dy, int boards)
	{
		setColor(gl, 192, 192, 128);			// Tan

		gl.glBegin(GL2.GL_QUAD_STRIP);

		gl.glVertex2i(dx + 0, dy + 0);		// base, leftmost slat
		gl.glVertex2i(dx + 0, dy + 102);	// peak, leftmost slat

		for (int i=1; i<=boards; i++)
		{
			int	x = i * 24;
			int	y = ((i % 2 == 1) ? 112 : 102);

			gl.glVertex2i(dx + x, dy + 0);	// base, next slat
			gl.glVertex2i(dx + x, dy + y);	// peak, next slat
		}

		gl.glEnd();
	}

	// Edges a left-to-right sequence of fence boards using LINE_LOOPs.
	private void	edgeFenceStrip(GL2 gl, int dx, int dy, int boards)
	{
		setColor(gl, 0, 0, 0);					// Black

		gl.glLineWidth(thickline);

		for (int i=0; i<boards; i++)
		{
			int	xl = i * 24;
			int	xr = xl + 24;
			int	yl = ((i % 2 == 0) ? 102 : 112);
			int	yr = ((i % 2 == 0) ? 112 : 102);

			gl.glBegin(GL2.GL_LINE_LOOP);

			gl.glVertex2i(dx + xl, dy + 0);	// base, left
			gl.glVertex2i(dx + xr, dy + 0);	// base, right
			gl.glVertex2i(dx + xr, dy + yr);	// peak, right
			gl.glVertex2i(dx + xl, dy + yl);	// peak, left

			gl.glEnd();
		}

		gl.glLineWidth(1.0f);
	}

	// Define four corners of a fence board that is shorter on the left side.
	private static final Point[]	OUTLINE_BOARD_L = new Point[]
	{
		new Point(  0,   0),		// base, left
		new Point( 24,   0),		// base, right
		new Point( 24, 112),		// peak, right
		new Point(  0, 102),		// peak, left
	};

	// Define four corners of a fence board that is shorter on the right side.
	private static final Point[]	OUTLINE_BOARD_R = new Point[]
	{
		new Point(  0,   0),		// base, left
		new Point( 24,   0),		// base, right
		new Point( 24, 102),		// peak, right
		new Point(  0, 112),		// peak, left
	};

	// Fills a single fence slat with bottom left corner at dx, dy.
	// If flip is true, the slat is higher on the left, else on the right.
	private void	fillFenceBoard(GL2 gl, boolean flip, int dx, int dy)
	{
		setColor(gl, 192, 192, 128);			// Tan
		fillPoly(gl, dx, dy, (flip ? OUTLINE_BOARD_R : OUTLINE_BOARD_L));
	}

	// Edges a single fence slat with bottom left corner at dx, dy.
	// If flip is true, the slat is higher on the left, else on the right.
	private void	edgeFenceBoard(GL2 gl, boolean flip, int dx, int dy)
	{
		setColor(gl, 0, 0, 0);					// Black
		edgePoly(gl, dx, dy, (flip ? OUTLINE_BOARD_R : OUTLINE_BOARD_L));
	}

	//**********************************************************************
	// Private Methods (Mouse handler functionality)
	//**********************************************************************
	
	/**
	 * Data on cursor position.
	 * 
	 * @param drawable
	 */
	private void	drawMode(GLAutoDrawable drawable)
	{
		GL2		gl = drawable.getGL().getGL2();
		double[]	p = Utilities.mapViewToScene(gl, 0.5 * w, 0.5 * h, 0.0);
		double[]	q = Utilities.mapSceneToView(gl, 0.0, 0.0, 0.0);
		String		svc = ("View center in scene: [" + FORMAT.format(p[0]) +
						   " , " + FORMAT.format(p[1]) + "]");
		String		sso = ("Scene origin in view: [" + FORMAT.format(q[0]) +
						   " , " + FORMAT.format(q[1]) + "]");

		renderer.beginRendering(w, h);

		// Draw all text in yellow
		renderer.setColor(1.0f, 1.0f, 0.0f, 1.0f);

		Point2D.Double	cursor = model.getCursor();

		if (cursor != null)
		{
			String		sx = FORMAT.format(new Double(cursor.x));
			String		sy = FORMAT.format(new Double(cursor.y));
			String		s = "Pointer at (" + sx + "," + sy + ")";

			renderer.draw(s, 2, 2);
		}
		else
		{
			renderer.draw("No Pointer", 2, 2);
		}

		renderer.draw(svc, 2, 16);
		renderer.draw(sso, 2, 30);

		renderer.endRendering();
	}

	/**
	 * Main method for user drawing sketches.
	 * 
	 * @param gl OpenGL Object.
	 */
	private void	drawMain(GL2 gl)
	{
		drawSkyStars(gl);
		drawCursor(gl);
	}
	
	/**
	 * Draws a star in the sky when mouse is clicked or dragged.
	 * 
	 * @param gl OpenGL object.
	 */
	private void	drawSkyStars(GL2 gl)
	{
		java.util.List<Point2D.Double>	points = model.getPolyline();
		
		for (Point2D.Double p : points)
		{
			drawHouseStar(gl, (int) p.x, (int) p.y);
		}

		gl.glEnd();
	}
	
	private void	drawCursor(GL2 gl)
	{
		Point2D.Double	cursor = model.getCursor();

		if (cursor == null)
			return;

		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glColor3f(1.0f, 1.0f, 1.0f);

		for (int i=0; i<32; i++)
		{
			double	theta = (2.0 * Math.PI) * (i / 32.0);

			gl.glVertex2d(cursor.x + 25 * Math.cos(theta),
						  cursor.y + 25 * Math.sin(theta));
		}

		gl.glEnd();
	}
	
	//**********************************************************************
	// Private Methods (Utility Functions)
	//**********************************************************************
	
	private void	setColor(GL2 gl, int r, int g, int b, int a)
	{
		gl.glColor4f(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
	}

	// Sets fully opaque color, normalizing r, g, b values from max 255 to 1.0.
	private void	setColor(GL2 gl, int r, int g, int b)
	{
		setColor(gl, r, g, b, 255);
	}
	
	/**
	 * Fills a rectangle having lower left corner at (x,y) and dimensions (w,h).
	 * 
	 * @param gl OpenGL object
	 * @param x X-coordinate starting
	 * @param y Y-coordinate starting
	 * @param w Width
	 * @param h Height
	 */
	private void	fillRect(GL2 gl, int x, int y, int w, int h)
	{
		gl.glBegin(GL2.GL_POLYGON);

		gl.glVertex2i(x+0, y+0);
		gl.glVertex2i(x+0, y+h);
		gl.glVertex2i(x+w, y+h);
		gl.glVertex2i(x+w, y+0);

		gl.glEnd();
	}
	
	/**
	 * Edges a rectangle having lower left corner at (x,y) and dimensions (w,h).
	 * 
	 * @param gl OpenGL object
	 * @param x X-coordinate starting
	 * @param y Y-coordinate starting
	 * @param w Width
	 * @param h Height
	 */
	private void	edgeRect(GL2 gl, int x, int y, int w, int h)
	{
		gl.glLineWidth(thickline);

		gl.glBegin(GL.GL_LINE_LOOP);

		gl.glVertex2i(x+0, y+0);
		gl.glVertex2i(x+0, y+h);
		gl.glVertex2i(x+w, y+h);
		gl.glVertex2i(x+w, y+0);

		gl.glEnd();

		gl.glLineWidth(1.0f);
	}
	
	/**
	 * Fills in a triangle given (x,y) coordinates given base and height.
	 * Creates an upside down triangle.
	 * 
	 * @param gl OpenGL object
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @param base Base of the triangle
	 * @param height Height of the triangle
	 * @param rightOrLeft 0 or 1. 0 denotes left while 1 denotes right
	 */
	private void	fillTriangle(GL2 gl, int x, int y, int base, int height,
			int rightOrLeft)
	{
		gl.glBegin(GL2.GL_TRIANGLES);
		
		if (rightOrLeft == 0)
		{
			gl.glVertex2f(x+base, y+height);
			gl.glVertex2f(x, y+height);
			gl.glVertex2f(x+0, y+0);
		}
		else if (rightOrLeft == 1)
		{
			gl.glVertex2f(x+0, y+height);
			gl.glVertex2f(x+base, y+height);
			gl.glVertex2f(x+base, y+0);
		}
		
		gl.glEnd();
	}
	
	/**
	 * Fills in a triangle given (x,y) coordinates given base and height.
	 * 
	 * @param gl OpenGL object
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @param base Base of the triangle
	 * @param height Height of the triangle
	 * @param rightOrLeft 0 or 1. 0 denotes left while 1 denotes right
	 */
	private void	edgeTriangle(GL2 gl, int x, int y, int base, int height,
			int rightOrLeft)
	{
		gl.glLineWidth(thickline);

		gl.glBegin(GL.GL_LINE_LOOP);

		if (rightOrLeft == 0)
		{
			gl.glVertex2f(x+base, y+height);
			gl.glVertex2f(x, y+height);
			gl.glVertex2f(x+0, y+0);
		}
		else if (rightOrLeft == 1)
		{
			gl.glVertex2f(x+0, y+height);
			gl.glVertex2f(x+base, y+height);
			gl.glVertex2f(x+base, y+0);
		}
		
		gl.glEnd();

		gl.glLineWidth(1.0f);
	}
	
	/**
	 * Fills in a circle having a center a (x,y) and a radius r.
	 * Code came from a YouTube video https://www.youtube.com/watch?v=NnutNkde5TE
	 * 
	 * @param gl OpenGL object
	 * @param x X-coordinate starting
	 * @param y Y-coordinate starting
	 * @param r Radius of the circle
	 */
	private void	fillCircle(GL2 gl, int x, int y, int r)
	{
		gl.glBegin(GL2.GL_POLYGON);
		
		// Calculation to get the circle.
		float theta;
		
		for (int i = 0; i < 360; i++)
		{
			theta = (float) (i * 3.142/180);
			
			gl.glVertex2f((float) (x + r * Math.cos(theta)), 
					(float) (y + r * Math.sin(theta)));
		}
		
		gl.glEnd();
	}
	
	/**
	 * Edges of the circle with coordinates (x,y) and a radius r.
	 * 
	 * @param gl OpenGL object
	 * @param x X-coordinate starting
	 * @param y Y-coordinate starting
	 * @param r Radius of the circle
	 */
	private void	edgeCircle(GL2 gl, int x, int y, int r)
	{
		gl.glLineWidth(thickline);

		gl.glBegin(GL.GL_LINE_LOOP);

		// Calculation to get the circle.
		float theta;
		
		for (int i = 0; i < 360; i++)
		{
			theta = (float) (i * 3.142/180);
			
			gl.glVertex2f((float) (x + r * Math.cos(theta)), 
					(float) (y + r * Math.sin(theta)));
		}
		
		gl.glEnd();

		gl.glLineWidth(1.0f);
	}

	/**
	 * Fills a polygon defined by a starting point and a sequence of offsets.
	 * 
	 * @param gl OpenGL object
	 * @param startx X-coordinate starting
	 * @param starty Y-coordinate starting
	 * @param offsets How much to offset
	 */
	private void	fillPoly(GL2 gl, int startx, int starty, Point[] offsets)
	{
		gl.glBegin(GL2.GL_POLYGON);

		for (int i=0; i<offsets.length; i++)
			gl.glVertex2i(startx + offsets[i].x, starty + offsets[i].y);

		gl.glEnd();
	}

	/**
	 * Edges a polygon defined by a starting point and a sequence of offsets.
	 * 
	 * @param gl OpenGL object
	 * @param startx X-coordinate starting
	 * @param starty Y-coordinate starting
	 * @param offsets How much to offset
	 */
	private void	edgePoly(GL2 gl, int startx, int starty, Point[] offsets)
	{
		gl.glLineWidth(thickline);

		gl.glBegin(GL2.GL_LINE_LOOP);

		for (int i=0; i<offsets.length; i++)
			gl.glVertex2i(startx + offsets[i].x, starty + offsets[i].y);

		gl.glEnd();

		gl.glLineWidth(1.0f);
	}
	
	/**
	 * Draws points on the star.
	 * 
	 * @param gl OpenGL object.
	 * @param cx X-Coordinate.
	 * @param cy Y-Coordinate.
	 * @param sides Number of sides.
	 * @param r1
	 * @param r2
	 */
	private void	doStarVertices(GL2 gl, int cx, int cy, int sides, double r1, double r2)
	{
		double	delta = Math.PI / sides;
		double	theta = 0.5 * Math.PI;
		
		for (int i=0; i<sides; i++)
		{
			gl.glVertex2d(cx + r1 * Math.cos(theta), cy + r1 * Math.sin(theta));
			theta += delta;
			
			gl.glVertex2d(cx + r2 * Math.cos(theta), cy + r2 * Math.sin(theta));
			theta += delta;
		}
	}
}

//******************************************************************************