//******************************************************************************
// Copyright (C) 2016-2019 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Wed Feb 27 17:33:00 2019 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20160225 [weaver]:	Original file.
// 20190227 [weaver]:	Updated to use model and asynchronous event handling.
//
//******************************************************************************
// Notes:
//
//******************************************************************************

package edu.ou.cs.cg.assignment.homework03;

//import java.lang.*;
import java.awt.Component;
import java.awt.event.*;
import java.awt.geom.Point2D;

//******************************************************************************

/**
 * The <CODE>KeyHandler</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @author  Tony Nguyen
 * @version %I%, %G%
 */
public final class KeyHandler extends KeyAdapter
{
	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	@SuppressWarnings("unused")
	private final View view;
	private final Model	model;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public KeyHandler(View view, Model model)
	{
		this.view = view;
		this.model = model;

		Component component = view.getCanvas();

		component.addKeyListener(this);
	}

	//**********************************************************************
	// Override Methods (KeyListener)
	//**********************************************************************

	/**
	 * Keyboard button press and what each button does.
	 */
	public void		keyPressed(KeyEvent e)
	{
		Point2D.Double p = model.getOrigin();

		switch (e.getKeyCode())
		{
			// Moving arrow keys.
			case KeyEvent.VK_LEFT:
				model.moveLeft();
				break;
			case KeyEvent.VK_RIGHT:
				model.moveRight();
				break;
			case KeyEvent.VK_UP:
				model.moveUp();
				break;
			case KeyEvent.VK_DOWN:
				model.moveDown();
				break;
		
			// Both Q and W keys delete stars created by user on screen.
			case KeyEvent.VK_Q:
				model.deleteOldestPoint();
				break;
			case KeyEvent.VK_W:
				model.deleteNewestPoint();
				break;
				
			// Using keys z, x, c, v, b to cycle through sky colors.
			case KeyEvent.VK_Z:
				model.changeSkyColor(1);
				break;
			case KeyEvent.VK_X:
				model.changeSkyColor(2);
				break;
			case KeyEvent.VK_C:
				model.changeSkyColor(3);
				break;
			case KeyEvent.VK_V:
				model.changeSkyColor(4);
				break;
			case KeyEvent.VK_B:
				model.changeSkyColor(5);
				break;
				
			// Using keys 3-9 to adjust number of sides for house feature.
			case KeyEvent.VK_3:
				model.sidesOfStar(3);
				break;
			case KeyEvent.VK_4:
				model.sidesOfStar(4);
				break;
			case KeyEvent.VK_5:
				model.sidesOfStar(5);
				break;
			case KeyEvent.VK_6:
				model.sidesOfStar(6);
				break;
			case KeyEvent.VK_7:
				model.sidesOfStar(7);
				break;
			case KeyEvent.VK_8:
				model.sidesOfStar(8);
				break;
			case KeyEvent.VK_9:
				model.sidesOfStar(9);
				break;
				
			// Clear screen from user sketch.
			case KeyEvent.VK_DELETE:
				model.clearScreen();
				break;
		}

		model.setOriginInSceneCoordinates(p);
	}
}

//******************************************************************************
