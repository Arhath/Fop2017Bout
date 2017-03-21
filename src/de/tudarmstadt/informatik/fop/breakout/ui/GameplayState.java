package de.tudarmstadt.informatik.fop.breakout.ui;

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
	
	private float ballSpeedX = 0.5f;
	private float ballSpeedY = 0.5f;
	
	private float playerSpeedX = 0.0f;
	
	private Entity entPlayer;
	private Entity entBall;
	
	private BallFactory ballFac;
	private BorderFactory borderFactory;
	
	LoopEvent move = new LoopEvent();
	
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
		
		LoopEvent l = new LoopEvent();
    	l.addAction(new MoveLeftAction(0.5f));
    	
    	LoopEvent r = new LoopEvent();
    	r.addAction(new MoveRightAction(0.5f));
    	
    	LoopEvent lb = new LoopEvent();
    	lb.addAction(new MoveLeftAction(0.5f));
    	
    	LoopEvent rb = new LoopEvent();
    	rb.addAction(new MoveRightAction(0.5f));
    	
    	LoopEvent pMoveEvent = new LoopEvent();
    	
    	pMoveEvent.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta, Component event) {		
				
				boolean bl = gc.getInput().isKeyDown(Input.KEY_A);
		    	boolean br = gc.getInput().isKeyDown(Input.KEY_D);
	
		    	
		    	entPlayer.removeComponent(l);
		    	entPlayer.removeComponent(r);
		    	
		    	entBall.removeComponent(lb);
		    	entBall.removeComponent(rb);
		    	
		    	playerSpeedX = 0.0f;
		    	
		    	Vector2f pvec = entPlayer.getPosition();
		    	
		    	if (bl)
		    	{
		    		if (pvec.x >= 70)
		    		{
		    			entPlayer.addComponent(l); 
		    			playerSpeedX = -0.5f;
		    		
		    		if (isLocked)
		    			entBall.addComponent(lb);
		    		}
		    	}
		    	
		    	if (br)
		    	{
		    		if (pvec.x <= 730)
		    		{
		    			playerSpeedX = 0.5f;
		    			entPlayer.addComponent(r);
		    		
		    		if (isLocked)
		    			entBall.addComponent(rb);
		    		}
		    	}
			}
			});
    	entPlayer.addComponent(pMoveEvent);
    }
     
    public void UpdateBallSpeed(GameContainer gc, StateBasedGame game, int delta)
    {
    	if (!isLocked)
    		return;
    	
    	ballSpeedX = playerSpeedX;
    		
    	//System.out.println(ballSpeedX);
    }
    
    public void CreateBall() throws SlickException
    {
    	entBall = new Entity(BALL_ID);
    	
    	entBall.addComponent(new ImageRenderComponent(new Image("/images/ball.png"))); // Bildkomponente
    	entBall.setPosition(new Vector2f(400, 490));
    	
    	entBall.setVisible(true);
    	entBall.setPassable(false);
    	entityManager.addEntity(stateID, entBall);
    	
    	entBall.addComponent(move);
    	
    	CollisionEvent c = new CollisionEvent();
    	
    	c.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta,
					Component event) {
				
				Entity target = c.getCollidedEntity();
				System.out.println(target.getID());
				Vector2f pall = entBall.getPosition();
				System.out.println(pall);
				
				if (target.getID() == "background")
				{
					if (pall.y <= 7.0f)
						ballSpeedY = ballSpeedY * -1;
					if (pall.x <= 7.0f)
						ballSpeedX = ballSpeedX * -1;
					if (pall.x >= 800.0f)
						ballSpeedX = ballSpeedX * -1;
					
					if (pall.y >= 600.0f)
					{
						entPlayer.setPosition(new Vector2f(400, 515));
				    	entBall.setPosition(new Vector2f(400, 490));
				    	isLocked = true;
				    	ballSpeedX = 0.0f;
				    	ballSpeedY = 0.0f;
				    	playerSpeedX = 0.0f;
					}
				}
				
				if (target.getID() == "player")
				{
					ballSpeedY = 0.5f;
					if (Math.abs(playerSpeedX) >= Math.abs(ballSpeedX))
						ballSpeedX = playerSpeedX;
				}
			}    		
    		});
    	
    	entBall.addComponent(c);

    	LoopEvent ballMovement = new LoopEvent();
    	ballMovement.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta, Component event) {
				
				
				move.removeAction(actionX);
		    	move.removeAction(actionY);
		    	
		    	UpdateBallSpeed(gc, sb, delta);
		    	
		    	actionX = new MoveRightAction (ballSpeedX);
		    	actionY = new MoveUpAction(ballSpeedY);
		    	
		    	if (isLocked)
		    		return;
		    	

		    	move.addAction(actionX);
		    	move.addAction(actionY);
		    	
		    	
			}
			});
    	entBall.addComponent(ballMovement);
    }
    
    /**
     * Wird vor dem (erstmaligen) Starten dieses States ausgefuehrt
     */
    @Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {

    	ballFac = new BallFactory();
    	borderFactory = new BorderFactory(BorderType.TOP);
    	
    	Entity TopBorder = borderFactory.createEntity();
    	
    	// Hintergrund laden
    	Entity background = new Entity("background");	// Entitaet fuer Hintergrund
    	background.setPosition(new Vector2f(400,300));	// Startposition des Hintergrunds
    	background.addComponent(new ImageRenderComponent(new Image("/images/background.png"))); // Bildkomponente
    	
    	background.setPassable(true);
    	
    	// Hintergrund-Entitaet an StateBasedEntityManager uebergeben
    	entityManager.getInstance().addEntity(stateID, background);
    	
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
			}
    	});
    	space_Listener.addComponent(space_pressed);    	
    	entityManager.addEntity(stateID, space_Listener);
    	
    	// Bei Mausklick soll Wassertropfen erscheinen
    	Entity mouse_Clicked_Listener = new Entity("Mouse_Clicked_Listener");
    	MouseClickedEvent mouse_Clicked = new MouseClickedEvent();
    	
    	mouse_Clicked.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta,
					Component event) {
				// Wassertropfen wird erzeugt
				Entity ball = ballFac.createEntity();
				ball.setPosition(new Vector2f(gc.getInput().getMouseX(),gc.getInput().getMouseY()));
				

				
				// Wassertropfen faellt nach unten
				LoopEvent loop = new LoopEvent();
		    	loop.addAction(new MoveDownAction(0.5f));
		    	ball.addComponent(loop);
		    	
		    	// Wenn der Bildschirm verlassen wird, dann ...
		    	LeavingScreenEvent lse = new LeavingScreenEvent();
		    	
		    	// ... zerstoere den Wassertropfen
		    	lse.addAction(new DestroyEntityAction());
		    	// ... und wechsle ins Hauptmenue
		    	//lse.addAction(new ChangeStateAction(Breakout.MAINMENU_STATE));
		    	
		    	ball.addComponent(lse);
		    	entityManager.addEntity(stateID, ball);
			}    		
    	});
    	mouse_Clicked_Listener.addComponent(mouse_Clicked);
    	
    	entityManager.addEntity(stateID, mouse_Clicked_Listener);
    	


    	
    	CreatePlayer();
    	
    	CreateBall();
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
		entityManager.renderEntities(container, game, g);
	}

	@Override
	public int getID() {
		return stateID;
	}
}
