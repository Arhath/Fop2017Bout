package de.tudarmstadt.informatik.fop.breakout.factories;

import java.util.UUID;
import java.util.Vector;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.StateBasedGame;

import de.tudarmstadt.informatik.fop.breakout.constants.GameParameters;
import eea.engine.action.Action;
import eea.engine.component.Component;
import eea.engine.component.render.ImageRenderComponent;
import eea.engine.entity.Entity;
import eea.engine.event.Event;
import eea.engine.event.basicevents.CollisionEvent;
import eea.engine.interfaces.IEntityFactory;

/**
 * Factory for creating Borders of the field. Borders are not visible and not
 * passable entities for holding the brick in the field.
 * 
 * @author Tobias Otterbein, Benedikt Wartusch
 * 
 */
public class BrickFactory implements IEntityFactory, GameParameters {
	
	private Vector<Entity> vBricks;
	
	private int ID;
	private int HP;
	
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
	
	
	public BrickFactory(int hp, int id) {
		ID = id;
		HP = hp;
	}
	
	@Override
	public Entity createEntity()
	{
		Entity brick;

		brick = new Entity(BLOCK_ID + ID);
				
		brick.setPosition(new Vector2f((int)(Math.random()*800), (int)(Math.random()*400)));
		brick.setVisible(true);
		brick.setPassable(false);
		brick.setScale(1.0f);
		
		try {
			// Bild laden und zuweisen
			brick.addComponent(new ImageRenderComponent(new Image("images/block_" + HP + ".png")));
		} catch (SlickException e) {
			System.err.println("Cannot find file images/brick.png!");
			e.printStackTrace();
		}

		return brick;
	}
}
