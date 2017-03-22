package de.tudarmstadt.informatik.fop.breakout.ui;


import java.awt.geom.Arc2D.Double;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import eea.engine.action.Action;
import eea.engine.action.basicactions.ChangeStateAction;
import eea.engine.action.basicactions.DestroyEntityAction;
import eea.engine.action.basicactions.MoveDownAction;
import eea.engine.action.basicactions.MoveUpAction;
import eea.engine.action.basicactions.MoveRightAction;
import eea.engine.action.basicactions.MoveLeftAction;
import eea.engine.component.Component;
import eea.engine.component.render.ImageRenderComponent;
import eea.engine.entity.Entity;
import eea.engine.entity.StateBasedEntityManager;
import eea.engine.event.basicevents.KeyPressedEvent;
import eea.engine.event.basicevents.CollisionEvent;
import eea.engine.event.basicevents.KeyDownEvent;
import eea.engine.event.basicevents.LeavingScreenEvent;
import eea.engine.event.basicevents.LoopEvent;
import eea.engine.event.basicevents.MouseClickedEvent;
import de.tudarmstadt.informatik.fop.breakout.constants.GameParameters;
import de.tudarmstadt.informatik.fop.breakout.constants.GameParameters.BorderType;
import de.tudarmstadt.informatik.fop.breakout.factories.*;


/**
 * @author Timo Bähr
 *
 * Diese Klasse repraesentiert das Spielfenster, indem ein Wassertropfen
 * erscheint und nach unten faellt.
 */
public class GameplayState extends BasicGameState implements GameParameters {

	protected int stateID; 							// Identifier dieses BasicGameState
	protected StateBasedEntityManager entityManager; 	// zugehoeriger entityManager
	
	private boolean bLeft = false;
	private boolean bRight = false;
	private boolean isLocked = true;
	
	private boolean bBlockedRight = false;
	private boolean bBlockedLeft = false;
	
	private boolean bSkipCollision = false;
	
	private boolean isRuning = true;
	
	private int nSkipFrames = 4;
	private int nSkipCount = 0;
	
	private static final int MAX_LIFES = 4;
	private int nLifes = MAX_LIFES;
	
	private Vector2f ballSpeed = new Vector2f(0,0);
	private Vector2f vBallStartPos = new Vector2f(400, 490);
	
	private Vector2f playerSpeed = new Vector2f(0,0);
	
	private Entity entPlayer;
	private Entity entBall;
	
	LoopEvent move = new LoopEvent();
	
	LoopEvent pMove = new LoopEvent();
	
	Action pMoveX = new MoveRightAction(0.0f);
	
	Action actionX = new MoveRightAction(0.5f);
	Action actionY = new MoveUpAction(0.5f);
    
    GameplayState( int sid ) {
       stateID = sid;
       entityManager = StateBasedEntityManager.getInstance();
    }
    
    public void CreatePlayer() throws SlickException
    {
	  //Create Player
		entPlayer = new Entity(PLAYER_ID);
		entPlayer.addComponent(new ImageRenderComponent(new Image("/images/stick.png"))); // Bildkomponente
		entPlayer.setPosition(new Vector2f(400, 515));
		
		entPlayer.setVisible(true);
		entPlayer.setPassable(false);
		entityManager.addEntity(stateID, entPlayer);
		
		entPlayer.addComponent(pMove);
		
    	LoopEvent pUpdate = new LoopEvent();
    	
    	pUpdate.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta, Component event) {
				
				if (!isRuning)
					sb.enterState(MAINMENU_STATE);
				
				boolean bl = gc.getInput().isKeyDown(Input.KEY_A);
		    	boolean br = gc.getInput().isKeyDown(Input.KEY_D);

		    	playerSpeed.x = 0.0f;
		    	
		    	Vector2f pvec = entPlayer.getPosition();
		    	
		    	if (bl)
		    	{
		    		if (pvec.x >= 69)
		    			playerSpeed.x = -0.5f;
		    	}
		    	
		    	if (br)
		    	{
		    		if (pvec.x <= 730)
		    			playerSpeed.x = 0.5f;
		    	}
		    	
		    	pMove.removeAction(pMoveX);
		    	pMoveX = new MoveRightAction(playerSpeed.x);		
		    	pMove.addAction(pMoveX);
			}
			});
    	entPlayer.addComponent(pUpdate);

    	CollisionEvent collision = new CollisionEvent();
    	
    	collision.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta,
					Component event) {
				
				Entity target = collision.getCollidedEntity();
				Vector2f vPlayer = entPlayer.getPosition();
				
				if (target.getID() == "background")
				{
					if (vPlayer.x < 400)
						bBlockedLeft = true;
					else
						bBlockedRight = true;
				}
			}    		
    		});
    	
    	entPlayer.addComponent(collision);
    }
     
    
    public void CreateBall() throws SlickException
    {
    	entBall = new Entity(BALL_ID);
    	entBall.addComponent(new ImageRenderComponent(new Image("/images/ball.png"))); // Bildkomponente
    	entBall.setPosition(vBallStartPos);
    	
    	entBall.setVisible(true);
    	entBall.setPassable(false);
    	entityManager.addEntity(stateID, entBall);
    	
    	entBall.addComponent(move);
    	
    	CollisionEvent collision = new CollisionEvent();
    	
    	collision.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta,
					Component event) {
	    	
		    	//System.out.println(nSkipCount);
				

				Entity target = collision.getCollidedEntity();
				
				Vector2f pall = entBall.getPosition();
				Vector2f vTarget = target.getPosition();
			
				
				double colOri = VecTheta(pall, vTarget);
				
				double dOffX = (90.0d - Math.toDegrees(Math.atan(target.getSize().y / target.getSize().x)));
				double dOffY = (360.0d - dOffX*4)/2;
				
				switch(target.getID())
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
					PlayerLooseLife();
					break;

				case "player":
					if (bSkipCollision)
						return;
				
			
					
					if (FInRange(pall.x - entBall.getSize().x, vTarget.x, target.getSize().x/2) || FInRange(pall.x + entBall.getSize().x, vTarget.x, target.getSize().x/2))
						if (pall.y >= vTarget.y)
							ballSpeed.y = Math.abs(ballSpeed.y) * -1;
						else
						{
							ballSpeed.y = Math.abs(ballSpeed.y);
							if (Math.abs(playerSpeed.x) >= Math.abs(ballSpeed.x))
								ballSpeed.x = playerSpeed.x;
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
					
			    	entityManager.removeEntity(stateID, target);
					//target.setVisible(false);
					//target.setPassable(true);
					break;
				}
				
				bSkipCollision = true;
			}    	
    		});
    	
    	
    	entBall.addComponent(collision);

    	LoopEvent ballUpdate = new LoopEvent();
    	ballUpdate.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta, Component event) {
				
				if(!isRuning)
					return;
				
				move.removeAction(actionX);
		    	move.removeAction(actionY);
		    	
		    	if (isLocked)
		    	{
		    		ballSpeed.x = playerSpeed.x;
		    		ballSpeed.y = 0;
				}
		    	
		    	actionX = new MoveRightAction (ballSpeed.x);
		    	actionY = new MoveUpAction(ballSpeed.y);
		    	

		    	move.addAction(actionX);
		    	move.addAction(actionY);
		    	
		    	if (bSkipCollision)
				{
					nSkipCount += 1;
			    	
			    	if (nSkipCount >= nSkipFrames)
			    	{
			    		nSkipCount = 0;
			    		bSkipCollision = false;
			    	}
			    	//System.out.println(nSkipCount);
				}
		    	
		    	//System.out.println(ballSpeed.x);
			}
			});
    	entBall.addComponent(ballUpdate);
    }
    
    public void GameOver()
    {
    	isRuning = false;
    	System.out.println("game over");
    }
    
    public void PlayerLooseLife()
    {
    	
    	Entity entlife = entityManager.getEntity(stateID, "life" + nLifes);
		if (entlife != null)
			entlife.setVisible(false);
		
		if (nLifes <= 0)
    		GameOver();
    	else
    	{
    		BallReset();
			nLifes -= 1;
    	}
    }
    
    public void BallReset()
    {
    	entBall.setPosition(new Vector2f(entPlayer.getPosition().x, entPlayer.getPosition().y - entBall.getSize().y));
    	isLocked = true;
    	ballSpeed.x = 0.0f;
    	ballSpeed.y = 0.0f;
    	playerSpeed.x = 0.0f;
    }
    
    public double VecTheta(Vector2f v1, Vector2f v2)
    {
    	Vector2f v3 = new Vector2f(0, 0);
		v3.add(v1);
		v3.sub(v2);
		
		
		return v3.getTheta();
    }
    
	public boolean FInRange(float x, float a, float off)
	{
		if (x >= a - off && x <= a + off)
			return true;
		
		return false;
	}
	
	public boolean DInRange(double x, double y, double z)
	{
		double yl = Math.abs(y - z) % 360.0d;
		double yr = Math.abs(y + z) % 360.0d;
		
		if (y == 360.0d)
		{
			System.out.println(yl);
			System.out.println(yr);
		}
		
		if (x >= yl && x <= yr)
			return true;
		
		return false;
	}
	
	public boolean IsAngleBetween(double target, double angle1, double angle2) 
	{
	  // make the angle from angle1 to angle2 to be <= 180 degrees
	  double rAngle = ((angle2 - angle1) % 360 + 360) % 360;
	  if (rAngle >= 180)
	  {
		 double temp = angle1;
		 angle1 = angle2;
		 angle2 = temp;
	  }

	  // check if it passes through zero
	  if (angle1 <= angle2)
	    return target >= angle1 && target <= angle2;
	  else
	    return target >= angle1 || target <= angle2;
	}  
    
    /**
     * Wird vor dem (erstmaligen) Starten dieses States ausgefuehrt
     */
    @Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
    	
    	
    	// Hintergrund laden
//    	Entity background = new Entity("background");	// Entitaet fuer Hintergrund
//    	background.setPosition(new Vector2f(400,300));	// Startposition des Hintergrunds
//    	background.addComponent(new ImageRenderComponent(new Image("/images/background.png"))); // Bildkomponente
//    	background.setScale(1.1f);
//    	
//    	background.setPassable(true);
//    	
//    	// Hintergrund-Entitaet an StateBasedEntityManager uebergeben
//    	entityManager.getInstance().addEntity(stateID, background);
    	
    	
    	
    	//Create Borders
    	
    	entityManager.addEntity(stateID, new BorderFactory(BorderType.TOP).createEntity());
    	entityManager.addEntity(stateID, new BorderFactory(BorderType.LEFT).createEntity());
    	entityManager.addEntity(stateID, new BorderFactory(BorderType.RIGHT).createEntity());
    	entityManager.addEntity(stateID, new BorderFactory(BorderType.DOWN).createEntity());
    	
    	for(int i = 0; i <= 40; i++)
    		entityManager.addEntity(stateID, new BrickFactory().createEntity());
    	
    	for(int i = 0; i <= MAX_LIFES; i++)
    	{
    		Entity entlife = new Entity("life" + i);
    		entlife.setPassable(false);
    		entlife.setVisible(true);
    		entlife.setPosition(new Vector2f(i*50 + 30, 577));
    		entlife.addComponent(new ImageRenderComponent(new Image("/images/ball.png"))); // Bildkomponente
    		
    		entityManager.addEntity(stateID, entlife);
    	}
    	
    	// Bei Drücken der ESC-Taste zurueck ins Hauptmenue wechseln
    	Entity esc_Listener = new Entity("ESC_Listener");
    	KeyPressedEvent esc_pressed = new KeyPressedEvent(Input.KEY_ESCAPE);
    	esc_pressed.addAction(new ChangeStateAction(Breakout.MAINMENU_STATE));
    	esc_Listener.addComponent(esc_pressed);    	
    	entityManager.addEntity(stateID, esc_Listener);
    	
    	
    	Entity space_Listener = new Entity("SPACE_Listener");
    	KeyPressedEvent space_pressed = new KeyPressedEvent(Input.KEY_SPACE);
    	space_pressed.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta, Component event) {
				isLocked = false;
				ballSpeed.y = 0.5f;
			}
    	});
    	space_Listener.addComponent(space_pressed);    	
    	entityManager.addEntity(stateID, space_Listener);
    	
    	// Bei Mausklick soll Wassertropfen erscheinen
//    	Entity mouse_Clicked_Listener = new Entity("Mouse_Clicked_Listener");
//    	MouseClickedEvent mouse_Clicked = new MouseClickedEvent();
//    	
//    	mouse_Clicked.addAction(new Action() {
//			@Override
//			public void update(GameContainer gc, StateBasedGame sb, int delta,
//					Component event) {
//				// Wassertropfen wird erzeugt
//				Entity ball = new BallFactory().createEntity();
//				ball.setPosition(new Vector2f(gc.getInput().getMouseX(),gc.getInput().getMouseY()));
//				
//
//				
//				// Wassertropfen faellt nach unten
//				LoopEvent loop = new LoopEvent();
//		    	loop.addAction(new MoveDownAction(0.5f));
//		    	ball.addComponent(loop);
//		    	
//		    	// Wenn der Bildschirm verlassen wird, dann ...
//		    	LeavingScreenEvent lse = new LeavingScreenEvent();
//		    	
//		    	// ... zerstoere den Wassertropfen
//		    	lse.addAction(new DestroyEntityAction());
//		    	// ... und wechsle ins Hauptmenue
//		    	//lse.addAction(new ChangeStateAction(Breakout.MAINMENU_STATE));
//		    	
//		    	ball.addComponent(lse);
//		    	entityManager.addEntity(stateID, ball);
//			}    		
//    	});
//    	mouse_Clicked_Listener.addComponent(mouse_Clicked);
//    	
//    	entityManager.addEntity(stateID, mouse_Clicked_Listener);
    	

    	CreatePlayer();
    	
    	CreateBall();
    }
    
    public void SkipCollision()
    {
    	bSkipCollision = true;
    	nSkipCount = 0;
    }

    /**
     * Wird vor dem Frame ausgefuehrt
     */
    @Override
	public void update(GameContainer gc, StateBasedGame game, int delta) throws SlickException {
		// StatedBasedEntityManager soll alle Entities aktualisieren
    	
    	bLeft = gc.getInput().isKeyDown(Input.KEY_A);
    	bRight = gc.getInput().isKeyDown(Input.KEY_D);

    	entityManager.updateEntities(gc, game, delta);
	}
    
    /**
     * Wird mit dem Frame ausgefuehrt 	
     */
	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		// StatedBasedEntityManager soll alle Entities rendern
		g.drawImage(new Image("/images/background.png"), 0, 0);	
		entityManager.renderEntities(container, game, g);
		
		g.drawString("GAME", 200, 200);
	}

	@Override
	public int getID() {
		return stateID;
	}
}
