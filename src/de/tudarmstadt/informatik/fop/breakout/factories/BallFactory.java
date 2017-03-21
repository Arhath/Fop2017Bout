package de.tudarmstadt.informatik.fop.breakout.factories;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;

import de.tudarmstadt.informatik.fop.breakout.constants.GameParameters;
import eea.engine.component.render.ImageRenderComponent;
import eea.engine.entity.Entity;
import eea.engine.interfaces.IEntityFactory;

/**
 * Factory for creating Borders of the field. Borders are not visible and not
 * passable entities for holding the ball in the field.
 * 
 * @author Tobias Otterbein, Benedikt Wartusch
 * 
 */
public class BallFactory implements IEntityFactory, GameParameters {

	/**
	 * Factory Constructor
	 * 
	 * @param type
	 *            determines the type of a created border (TOP, LEFT or RIGHT)
	 */
	public BallFactory() {
	}

	@Override
	public Entity createEntity() {

		Entity ball;

		ball = new Entity(BALL_ID);
				
		ball.setVisible(true);
		ball.setPassable(false);
		
		try {
			// Bild laden und zuweisen
			ball.addComponent(new ImageRenderComponent(new Image("images/ball.png")));
		} catch (SlickException e) {
			System.err.println("Cannot find file images/ball.png!");
			e.printStackTrace();
		}

		return ball;
	}

}
