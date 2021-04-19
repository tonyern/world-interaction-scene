//******************************************************************************
// Copyright (C) 2016-2019 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Fri Feb  1 18:21:52 2019 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20160209 [weaver]:	Original file.
// 20190129 [weaver]:	Updated to JOGL 2.3.2 and cleaned up.
// 20190203 [weaver]:	Additional cleanup and more extensive comments.
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

package edu.ou.cs.cg.assignment.solution01;

//import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

//******************************************************************************

/**
 * The <CODE>Application</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class Application
	implements GLEventListener, Runnable
{
	//**********************************************************************
	// Public Class Members
	//**********************************************************************

	public static final GLU	GLU = new GLU();
	public static final GLUT	GLUT = new GLUT();
	public static final Random	RANDOM = new Random();

	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private int				w;				// Canvas width
	private int				h;				// Canvas height
	private int				k = 0;			// Animation counter
	private TextRenderer		renderer;

	private int				m = 1;			// Number of points to draw
	private boolean			p = false;		// Draw lines?

	//**********************************************************************
	// Main
	//**********************************************************************

	public static void	main(String[] args)
	{
		SwingUtilities.invokeLater(new Application(args));
	}

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public Application(String[] args)
	{
	}

	//**********************************************************************
	// Override Methods (Runnable)
	//**********************************************************************

	public void	run()
	{
		GLProfile		profile = GLProfile.getDefault();
		GLCapabilities	capabilities = new GLCapabilities(profile);
		GLCanvas		canvas = new GLCanvas(capabilities);	// Single-buffer
		//GLJPanel		canvas = new GLJPanel(capabilities);	// Double-buffer
		JFrame			frame = new JFrame("Application");

		// Specify the starting width and height of the canvas itself
		canvas.setPreferredSize(new Dimension(1280, 720));

		// Populate and show the frame
		frame.setBounds(50, 50, 200, 200);
		frame.getContentPane().add(canvas);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Exit when the user clicks the frame's close button
		frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});

		// Register this class to update whenever OpenGL needs it
		canvas.addGLEventListener(this);

		// Have OpenGL call display() to update the canvas 60 times per second
		FPSAnimator	animator = new FPSAnimator(canvas, 60);

		animator.start();
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
		k++;									// Advance animation counter

		if (m > 100000)						// Check point cap
		{
			m = 1;								// Reset point count
			p = !p;							// Switch point/line mode
		}
		else
		{
			m++;								// Faster increase at low counts
		}

		m = (int)Math.floor(m * 1.02) + 1;		// Increase point count
	}

	// Render the scene model and display the current animation frame.
	private void	render(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT);	// Clear the buffer

		//setProjection(gl);					// Use a coordinate system

		// Draw the scene
		drawTinkerbellMap(gl);					// Draw the Tinkerbell map
		drawText(drawable);					// Draw some text

		gl.glFlush();							// Finish and display
	}

	//**********************************************************************
	// Private Methods (Pipeline)
	//**********************************************************************

	// Position and orient the default camera to view in 2-D, centered above.
	private void	setProjection(GL2 gl)
	{
		GLU	glu = new GLU();

		gl.glMatrixMode(GL2.GL_PROJECTION);		// Prepare for matrix xform
		gl.glLoadIdentity();						// Set to identity matrix
		glu.gluOrtho2D(-1.0f, 1.0f, -1.0f, 1.0f);	// 2D translate and scale
	}

	//**********************************************************************
	// Private Methods (Scene)
	//**********************************************************************

	// This page is helpful:
	// en.wikipedia.org/wiki/Tinkerbell_map
	private void	drawTinkerbellMap(GL2 gl)
	{
		if (p)									// Line or point mode?
			gl.glBegin(GL.GL_LINE_STRIP);		// Specify line segment sequence
		else
			gl.glBegin(GL.GL_POINTS);			// Specify point set

		gl.glColor3f(1.0f, 1.0f, 1.0f);		// Draw in white

		double		a = 0.9;					// Constant for updating x
		double		b = -0.6013;				// Constant for updating x
		double		c = 2.0;					// Constant for updating y
		double		d = 0.50;					// Constant for updating y
		double		lx = -0.72;				// Initial x coordinate
		double		ly = -0.64;				// Initial y coordinate

		for (int i=0; i<m; i++)
		{
			double	llx = lx * lx - ly * ly + a * lx + b * ly;
			double	lly = 2 * lx * ly + c * lx + d * ly;

			lx = llx;
			ly = lly;
			//System.out.println(" " + lx + " " + ly);

			// Make the map fit into the scene by translating the x coordinate,
			// and translating and scaling the y coordinate. (Changing the x
			// and y ranges in setProjection() above might be a better way.)
			gl.glVertex2d(lx + 0.4, (ly + 0.5) / 1.1);
		}

		gl.glEnd();
	}

	// Warning! Text is drawn in unprojected canvas/viewport coordinates.
	// For more on text rendering, the example on this page is long but helpful:
	// jogamp.org/jogl-demos/src/demos/j2d/FlyingText.java
	private void	drawText(GLAutoDrawable drawable)
	{
		renderer.beginRendering(w, h);
		renderer.setColor(0.75f, 0.75f, 0.75f, 1.0f);
		renderer.draw("Tinkerbell map (Points: " + m + ")", 2, h - 14);
		renderer.endRendering();
	}
}

//******************************************************************************
