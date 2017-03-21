package de.tudarmstadt.informatik.fop.breakout.factories;

import java.util.UUID;
import java.util.Vector;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;

import de.tudarmstadt.informatik.fop.breakout.constants.GameParameters;
import eea.engine.component.render.ImageRenderComponent;
import eea.engine.entity.Entity;
import eea.engine.interfaces.IEntityFactory;

/**
 * Factory for creating Borders of the field. Borders are not visible and not
 * passable entities for holding the brick in the field.
 * 
 * @author Tobias Otterbein, Benedikt Wartusch
 * 
 */
public class BrickFactory implements GameParameters {
	
	private Vector<Entity> vBricks;
	
	public int GetNumBricks()
	{
		return vBricks.size();
	}
	
	/**
	 * Factory Constructor
	 * 
	 * @param type
	 *            determines the type of a created border (TOP, LEFT or RIGHT)
	 */
	public BrickFactory() {
	}
	
	public Entity CreateBrick()
	{
		Entity brick;
		
		String uID = UUID.randomUUID().toString();

		brick = new Entity(uID);
				
		brick.setVisible(true);
		brick.setPassable(false);
		
		try {
			// Bild laden und zuweisen
			brick.addComponent(new ImageRenderComponent(new Image("images/brick.png")));
		} catch (SlickException e) {
			System.err.println("Cannot find file images/brick.png!");
			e.printStackTrace();
		}

		return brick;
	}
}
