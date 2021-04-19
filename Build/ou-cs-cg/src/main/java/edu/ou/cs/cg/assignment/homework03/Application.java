//******************************************************************************
// Copyright (C) 2016-2020 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Sun Feb 16 16:43:45 2020 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20160225 [weaver]:	Original file.
// 20190203 [weaver]:	Updated to JOGL 2.3.2 and cleaned up.
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
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

//******************************************************************************

/**
 * The <CODE>Application</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @author  Tony Nguyen
 * @version %I%, %G%
 */
public final class Application
	implements Runnable
{
	//**********************************************************************
	// Public Class Members
	//**********************************************************************

	public static final String		DEFAULT_NAME = "World Scene Interaction";
	public static final Dimension	DEFAULT_SIZE = new Dimension(780, 320);

	//**********************************************************************
	// Main
	//**********************************************************************

	public static void	main(String[] args)
	{
		SwingUtilities.invokeLater(new Application(args));
	}

	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private View		view;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public Application(String[] args)
	{
		// Does nothing.
	}

	//**********************************************************************
	// Override Methods (Runnable)
	//**********************************************************************

	public void	run()
	{
		GLProfile		profile = GLProfile.getDefault();

		GLCapabilities	capabilities = new GLCapabilities(profile);
		
		// Double-buffer canvas
		GLJPanel		canvas = new GLJPanel(capabilities);
		JFrame			frame = new JFrame(DEFAULT_NAME);

		// Specify the starting width and height of the canvas itself
		canvas.setPreferredSize(DEFAULT_SIZE);

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

		// Create a view to manage the canvas
		view = new View(canvas);
	}

	public View getView() 
	{
		return view;
	}

	public void setView(View view) 
	{
		this.view = view;
	}
}

//******************************************************************************
