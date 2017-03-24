package de.tudarmstadt.informatik.fop.breakout.test.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.StateBasedGame;

import de.tudarmstadt.informatik.fop.breakout.constants.GameParameters;
import de.tudarmstadt.informatik.fop.breakout.interfaces.IHitable;
import de.tudarmstadt.informatik.fop.breakout.ui.Breakout;
import de.tudarmstadt.informatik.fop.breakout.ui.GameplayState;
import eea.engine.action.Action;
import eea.engine.action.basicactions.MoveRightAction;
import eea.engine.action.basicactions.MoveUpAction;
import eea.engine.component.Component;
import eea.engine.component.render.ImageRenderComponent;
import eea.engine.entity.Entity;
import eea.engine.entity.StateBasedEntityManager;
import eea.engine.event.basicevents.CollisionEvent;
import eea.engine.event.basicevents.LoopEvent;
import eea.engine.test.TestAppGameContainer;

import de.tudarmstadt.informatik.fop.breakout.factories.Block;
import de.tudarmstadt.informatik.fop.breakout.factories.BrickFactory;

public class Adapter implements GameParameters {

  /*
   * the instance of our game, extends StateBasedGame
   */
	Breakout breakout;
	
	/**
	 * The TestAppGameContainer for running the tests
	 */
	TestAppGameContainer app;

  //TODO you should declare the additional attributes you may require here.
	Entity entBall = null;
	GameplayState gameplay = new GameplayState(0);
	StateBasedEntityManager entityManager = StateBasedEntityManager.getInstance();
	
	Vector2f ballSpeed = new Vector2f(0,0);
	
	LoopEvent move = new LoopEvent();
	Action actionX = new MoveRightAction(0.0f);
	Action actionY = new MoveUpAction(0.0f);
	
	Vector2f stickpos = new Vector2f(0, 200);
	
	private List<Block> vbricks = new ArrayList<Block>();
	
	static int nLifes = 0;
	
	/**
	 * Use this constructor to initialize everything you need.
	 */
	public Adapter() {
		breakout = null;
	}

	/* ***************************************************
	 * ********* initialize, run, stop the game **********
	 * ***************************************************
	 * 
	 * You can normally leave this code as it is.
	 */

	public StateBasedGame getStateBasedGame() {
		return breakout;
	}

	/**
	 * Diese Methode initialisiert das Spiel im Debug-Modus, d.h. es wird ein
	 * AppGameContainer gestartet, der keine Fenster erzeugt und aktualisiert.
	 * 
	 * Sie müssen diese Methode erweitern
	 */
	public void initializeGame() {

    // Set the library path depending on the operating system
    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
      System.setProperty("org.lwjgl.librarypath",
          System.getProperty("user.dir") + "/native/windows");
    } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
      System.setProperty("org.lwjgl.librarypath",
          System.getProperty("user.dir") + "/native/macosx");
    } else {
      System.setProperty("org.lwjgl.librarypath",
          System.getProperty("user.dir") + "/native/"
              + System.getProperty("os.name").toLowerCase());
    }

    // Initialize the game in debug mode (no GUI output)
		breakout = new Breakout(true);

		try {
			app = new TestAppGameContainer(breakout);
			app.start(0);
		} catch (SlickException e) {
			e.printStackTrace();
		}
		
		createBallInstance("ball");
		
		try {
			File file = new File(System.getProperty("user.dir") + "/maps/level1.map");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			
			int lineCount = 0, brickCount = 0;
			int idBrick = 0;
			int nTotal = 0;
			
			Block block = null;
			
			while ((line = bufferedReader.readLine()) != null) {
				
				String[] parts = line.split(",");
				System.out.println(parts[0]);
				
				for (int i = 0; i <= parts.length-1; i++)
				{
					int hp = Integer.parseInt(parts[i]);
					System.out.println(hp);
					
					if (hp > 0)
					{
						String blockID = "block" + brickCount + "_" + lineCount;
						
						block = new Block(blockID, hp);
						
						vbricks.add(block);
						
						nTotal += 1;
					}
					
					brickCount += 1;
					idBrick += 1;
				}
				
				System.out.println(" newline" + lineCount);
				lineCount += 1;
				brickCount = 0;
				
				stringBuffer.append(line);
				stringBuffer.append("\n");
			}
			
			fileReader.close();
			//System.out.println("Contents of file:");
			//System.out.println(stringBuffer.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stop a running game
	 */
	public void stopGame() {
		if (app != null) {
			app.exit();
			app.destroy();
		}
		StateBasedEntityManager.getInstance().clearAllStates();
		breakout = null;
	}

	public void changeToGameplayState() {
		this.getStateBasedGame().enterState(GAMEPLAY_STATE);
		try {
			app.updateGame(1);
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}

	public void changeToHighScoreState() {
		this.getStateBasedGame().enterState(HIGHSCORE_STATE);
		try {
			app.updateGame(1);
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}

	/* ***************************************************
	 * ********************** Ball **********************
	 * ***************************************************
	 */
	
	/**
	 * Returns a new Entity that represents a ball with ID ballID.
	 * It was added for tests, as we do not know what class/package will represent
	 * your "ball" entity.
	 * 
	 * @param ballID the ID for the new ball instance
	 * @return an entity representing a ball with the ID passed in as ballID
	 */
	public Entity createBallInstance(String ballID) {
	  //TODO write code that returns a ball instance with ID 'ballID'
		
		entBall = new Entity(ballID);
    	entBall.setPosition(new Vector2f(-100,-100));
    	
    	entBall.setVisible(true);
    	entBall.setPassable(false);
    	entityManager.addEntity(1, entBall);
    	
    	entBall.addComponent(move);
    	
    	CollisionEvent collision = new CollisionEvent();
    	
    	collision.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta,
					Component event) {
	    	
		    	//System.out.println(nSkipCount);
				

				Entity target = collision.getCollidedEntity();
				
				System.out.println(target.getID());
				
				Vector2f pall = entBall.getPosition();
				Vector2f vTarget = target.getPosition();
			
				String tID = (String)target.getID();
				String id = "";
				
				if (tID.contains("block"))
				{
					id = tID.substring(5);
					tID = "block";
				}
				
				switch(tID)
				{
				case "leftBorder":
					ballSpeed.x = Math.abs(ballSpeed.x);
					break;
					
				case "rightBorder":
					ballSpeed.x = Math.abs(ballSpeed.x) * -1.0f;
					break;
					
				case "topBorder":
					ballSpeed.y = Math.abs(ballSpeed.y) * -1.0f;
					break;
					
				case "downBorder":
						//PlayerLooseLife();
					break;

				case "player":
					//if (bSkipCollision)
						//return;
				
					
					if (FInRange(pall.x - entBall.getSize().x, vTarget.x, target.getSize().x/2) || FInRange(pall.x + entBall.getSize().x, vTarget.x, target.getSize().x/2))
						if (pall.y >= vTarget.y)
							ballSpeed.y = Math.abs(ballSpeed.y) * -1;
						else
						{
							ballSpeed.y = Math.abs(ballSpeed.y);
						}
					
					if (pall.y >= vTarget.y - target.getSize().y / 2 && pall.y <= vTarget.y + target.getSize().y / 2 )
						if (pall.x >= vTarget.x)
						{
							ballSpeed.y = Math.abs(ballSpeed.y);
							ballSpeed.x = Math.abs(ballSpeed.x) * 1.2f;
						}
						else
						{
							ballSpeed.y = Math.abs(ballSpeed.y);
							ballSpeed.x = Math.abs(ballSpeed.x) * -1.2f;
						}

					
					break;
					
				case "block":
					
					System.out.println(id);
					
					if (target.isPassable())
						return;

					//System.out.println("c: " + colOri); 
					//System.out.println(dOffX);
					//System.out.println(dOffY);
					
					if (FInRange(pall.x - entBall.getSize().x, vTarget.x, target.getSize().x/2) || FInRange(pall.x + entBall.getSize().x, vTarget.x, target.getSize().x/2))
						if (pall.y >= vTarget.y)
							ballSpeed.y = Math.abs(ballSpeed.y) * -1;
						else
							ballSpeed.y = Math.abs(ballSpeed.y);
					
					if (pall.y >= vTarget.y - target.getSize().y / 2 && pall.y <= vTarget.y + target.getSize().y / 2 )
						if (pall.x >= vTarget.x)
							ballSpeed.x = Math.abs(ballSpeed.x);
						else
							ballSpeed.x = Math.abs(ballSpeed.x) * -1;
					
//					if (IsAngleBetween(colOri, 270.0d - dOffX, 270.0d + dOffX))
//					{
//						ballSpeed.y = Math.abs(ballSpeed.y);
//					}
//					else
//					if (IsAngleBetween(colOri, 90.0d - dOffX, 90.0d + dOffX))
//					{
//						ballSpeed.y = Math.abs(ballSpeed.y) * -1;
//					}
//					else
//					if (IsAngleBetween(colOri, 180.0d - dOffY, 180.0d + dOffY))
//					{
//						ballSpeed.x = Math.abs(ballSpeed.x) * -1;
//					}
//					else
//					if (IsAngleBetween(colOri, 360.0d - dOffY, 360.0d + dOffY))
//					{
//						ballSpeed.x = Math.abs(ballSpeed.x);
//					}
					
					//BlockHit(target, id, 1);

					//target.setVisible(false);
					//target.setPassable(true);
					break;
				}
			}    	
    		});
    	
    	
    	entBall.addComponent(collision);

    	LoopEvent ballUpdate = new LoopEvent();
    	ballUpdate.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta, Component event) {
				
				move.removeAction(actionX);
		    	move.removeAction(actionY);
		    	
				//System.out.println(ballSpeed.x);
		    	
		    	actionX = new MoveRightAction (ballSpeed.x);
		    	actionY = new MoveUpAction(ballSpeed.y);
		    	

		    	move.addAction(actionX);
		    	move.addAction(actionY);
		    	
		    	//System.out.println(ballSpeed.x);
			}
			});
    	entBall.addComponent(ballUpdate);
    	
    	return entBall;
	}

	/**
	 * Returns an instance of the IHitable interface that represents a block
	 * with the ID as passed in and the requested number of hits left (1 = next
	 * hit causes the block to vanish, 2 = it takes two hits, ...)
	 * 
	 * @param blockID the ID the returns block entity should have
	 * @param hitsUntilDestroyed the number of hits (> 0) the block should have left
	 * before it vanishes (1 = vanishes with next touch by ball)
	 * @return an entity representing a block with the given ID and hits left
	 */
	public IHitable createBlockInstance(String blockID, int hitsUntilDestroyed) {
	  //TODO write code that returns a block instance with ID 'blockID'
	  // and that requires hitsUntilDestroyed "hits" until it vanishes
		
		Block block = new Block(blockID, hitsUntilDestroyed);
		
	  return block;
	}

	/**
	 * sets the ball's orientation (angle). 
	 * Note: the name of the method is somewhat unfortunate, but is taken from EEA's entity.
	 * 
	 * @param i the new orientation angle for the ball (0...360)
	 */
	public void setRotation(int i) {
	  //TODO write code sets the ball rotation to the value passed in
		entBall.setRotation(i);
	}

  /**
   * returns the ball's orientation (angle). 
   * Note: the name of the method is somewhat unfortunate, but is taken from EEA's entity.
   * 
   * @return the orientation angle for the ball (0...360)
   */
	public float getRotation() {
    //TODO write code retrieves the ball's rotation
		return entBall.getRotation();
	}

	/**
	 * Sets the ball's position to the coordinate provide
	 * 
	 * @param vector2f the target position for the ball
	 */
	public void setPosition(Vector2f vector2f) {
		entBall.setPosition(vector2f);
	  //TODO provide code that sets the position of the ball to the coordinates passed in
	}

  /**
   * returns a definition of the ball's size. Typically, the size of the ball will
   * be constant, but programmers may introduce bonus items that shrink or enlarge the ball.
   * 
   * @return the size of the ball
   */
	public Vector2f getSize() {
		return entBall.getSize();
	}

	/**
	 * returns the current speed of the ball's movement
	 * 
	 * @return the ball's speed
	 */
	public float getSpeed() {
    //TODO write code to retrieve the ball speed
	  return ballSpeed.x;
	}

	/**
	 * sets the current speed of the ball to the given value
	 * 
	 * @param speed the new speed of the ball
	 */
	public void setSpeed(float speed) {
		ballSpeed.x = speed;
		ballSpeed.y = speed;
    //TODO write code to set the ball speed
	}

	/**
	 * provide a proper code mapping to a check if your ball entity collides with
	 * 'otherEntity'. You will have to access your ball instance for this purpose.
	 * 
	 * @param otherEntity another entity that the ball may (or may not) collide with
	 * 
	 * @return true if the two entities have collided. Note: your ball should by default
	 * not collide with itself (or other balls, if there are any), null, the background,
	 * or "passable" entities (e.g. other image you have placed on the screen). It should only
	 * collide with the stick if the orientation is correct (>90 but <270 degrees, else it would
	 * "collide with the underside of the stick") but should be "gone" then already).
	 * It should also collide with the borders if the orientation is correct for this, e.g.,
	 * only collide with the top border if the orientation is fitting).
	 */
	public boolean collides(Entity otherEntity) {
	  //TODO write code to test if the ball collides with 'otherEntity'
		if (otherEntity == null)
			return false;
		
		if(otherEntity.isPassable() || otherEntity.getID().substring(0, 4).equals("ball"))
			return false;
		
		Vector2f vb = entBall.getPosition();
		Vector2f vt = otherEntity.getPosition();
		System.out.println(entBall.getSize());
		
		if (FInRange(vb.x, vt.x, otherEntity.getSize().x/2) && FInRange(vb.y, vt.y, otherEntity.getSize().y/2))
			return true;
		
		return false;	  
	}

	/* ***************************************************
	 * ********************** Player *********************
	 * ***************************************************
	 */
	
	/**
	 * ensures that the player has "value" additional lives (=additional balls left).
	 * 
	 * @param value the number of additional balls/lives to be added.
   */
	public void addLives(int value) {
	  //TODO write code to add the given number to the player's lives
		nLifes += value;
	}

	/**
	 * ensures that the player has exactly "playerLives" balls/lives left.
	 * 
	 * @param playerLives the number of lives/balls the player shall have left
	 */
	public void setLives(int playerLives) {
	  //TODO write code to set the number of player's lives to playerLives
		nLifes = playerLives;
	}

	/**
	 * queries your classes for the number of lives/balls the player has left
	 * 
	 * @return the number of lives/balls left
	 */
	public int getLivesLeft() {
	  //TODO write code to retrieve the number of lives left
		
	  return nLifes;
	}

	/** 
	 * checks if the player still has at least one live/ball left
	 * 
	 * @return true if the player still has at least one live/ball left, else false.
	 */
	public boolean hasLivesLeft() {
	  //TODO write code to test if the player has at least one live left
		return nLifes > 0;
	}

	/* ***************************************************
	 * ********************** Block **********************
	 * ***************************************************
	 */

	/**
	 * Sets a number of necessary hits for degrading this block
	 * 
	 * @param hitsLeft
	 *            number of necessary hits
	 * @param blockID
	 *            blockID ID of the chosen block
	 */
	public void setHitsLeft(int hitsLeft, String blockID) {
	  //TODO write code to set the number of required hits for 'blockID' to hitsLeft
		for(int i = 0; i < vbricks.size(); i++)
		{
			Block l = vbricks.get(i);
			if (l.getID().compareTo(blockID) == 0)
			{
				l.setHitsLeft(hitsLeft);
				return;
			}
		}
	}

	/**
	 * Returns the number of necessary hits for degrading this block
	 * 
	 * @param blockID
	 *            ID of the chosen block
	 * @return number of hits
	 */
	public int getHitsLeft(String blockID) {
	  //TODO write code to return how many hits 'blockID' needs to vanish
		
		for(int i = 0; i < vbricks.size(); i++)
		{
			Block l = vbricks.get(i);
			if (l.getID().compareTo(blockID) == 0)
			{
				return l.getHitsLeft();
			}
		}
		
		throw new NullPointerException("null pointer exception");
		
		//return 0;
	}

	/**
	 * Adds a number of necessary hits for degrading this block
	 * 
	 * @param hitsLeft
	 *            number of hits added
	 * @param blockID
	 *            blockID ID of the chosen block
	 */
	public void addHitsLeft(int hitsLeft, String blockID) {
    //TODO write code to add the given number to the block's "hit capacity"
		for(int i = 0; i < vbricks.size(); i++)
		{
			Block l = vbricks.get(i);
			if (l.getID().compareTo(blockID) == 0)
			{
				l.addHitsLeft(hitsLeft);
				return;
			}
		}
	}

	/**
	 * Returns whether the block has hits left
	 * 
	 * @param blockID
	 *            blockID ID of the chosen block
	 * @return true, if block has hits left, else false
	 */
	public boolean hasHitsLeft(String blockID) {
    //TODO write code to return if the given block still has hits left
		for(int i = 0; i < vbricks.size(); i++)
		{
			Block l = vbricks.get(i);
			if (l.getID().compareTo(blockID) == 0)
			{
				return l.hasHitsLeft();
			}
		}
		
		return false;
	}

	/* ***************************************************
	 * ********************** Stick **********************
	 * ***************************************************
	 */
	
	/**
	 * returns the current position of the stick
	 * 
	 * @return the current position of the stick
	 */
	public Vector2f getStickPosition() {
	  //TODO write code to return the position of the stick
	  return stickpos; // these are arbitrary values(!)
	}

	/* ***************************************************
	 * ********************** Input **********************
	 * ***************************************************
	 */

	/**
	 * This Method should emulate the key down event.
	 * 
	 * @param updatetime
	 *            : Zeitdauer bis update-Aufruf
	 * @param input
	 *            : z.B. Input.KEY_K, Input.KEY_L
	 */
	public void handleKeyDown(int updatetime, Integer input) {
	  //TODO write code that handles a "key pressed" event
	  // note: do not forget to call app.updateGame(updatetime);
	}

	/**
	 * This Method should emulate the pressing of the right arrow key.
	 */
	public void handleKeyDownRightArrow() {
	  //TODO write code for handling a "right arrow" key press
	  // hint: you may use the above method.
		stickpos = new Vector2f(700, 0);
	}

	/**
	 * This Method should emulate the pressing of the left arrow key.
	 */
	public void handleKeyDownLeftArrow() {
    //TODO write code for handling a "left arrow" key press
    // hint: you may use the above method.
		stickpos = new Vector2f(100, 0);
	}
	
	public boolean FInRange(float x, float a, float off)
	{
		if (x >= a - off && x <= a + off)
			return true;
		
		return false;
	}
}
