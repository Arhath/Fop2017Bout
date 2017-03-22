package de.tudarmstadt.informatik.fop.breakout.ui;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import eea.engine.action.Action;
import eea.engine.action.basicactions.ChangeStateInitAction;
import eea.engine.action.basicactions.QuitAction;
import eea.engine.component.render.ImageRenderComponent;
import eea.engine.entity.Entity;
import eea.engine.entity.StateBasedEntityManager;
import eea.engine.event.ANDEvent;
import eea.engine.event.basicevents.MouseClickedEvent;
import eea.engine.event.basicevents.MouseEnteredEvent;

/**
 * @author Timo Bähr
 *
 * Diese Klasse repraesentiert das Menuefenster, indem ein neues
 * Spiel gestartet werden kann und das gesamte Spiel beendet 
 * werden kann.
 */
public class MainMenuState extends BasicGameState {

	private int stateID; 							// Identifier von diesem BasicGameState
	private StateBasedEntityManager entityManager; 	// zugehoeriger entityManager
	
	private final int distance = 100;
    private final int start_Position = 180;
    
    MainMenuState( int sid ) {
       stateID = sid;
       entityManager = StateBasedEntityManager.getInstance();
    }
    
    /**
     * Wird vor dem (erstmaligen) Starten dieses State's ausgefuehrt
     */
    @Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
    	// Hintergrund laden
    	Entity background = new Entity("menu");	// Entitaet fuer Hintergrund
    	background.setPosition(new Vector2f(400,300));	// Startposition des Hintergrunds
    	background.addComponent(new ImageRenderComponent(new Image("/images/menu.png"))); // Bildkomponente
    	    	
    	// Hintergrund-Entitaet an StateBasedEntityManager uebergeben
    	entityManager.addEntity(stateID, background);
    	
    	/* Neues Spiel starten-Entitaet */
    	String new_Game = "Neues Spiel starten";
    	Entity new_Game_Entity = new Entity(new_Game);
    	
    	// Setze Position und Bildkomponente
    	new_Game_Entity.setPosition(new Vector2f(200, 190));
    	new_Game_Entity.addComponent(new ImageRenderComponent(new Image("images/start.png")));
    	new_Game_Entity.setScale(1.0f);
    	
    	// Erstelle das Ausloese-Event und die zugehoerige Action
    	ANDEvent mainEvents = new ANDEvent(new MouseEnteredEvent(), new MouseClickedEvent());
    	Action new_Game_Action = new ChangeStateInitAction(Breakout.GAMEPLAY_STATE);
    	mainEvents.addAction(new_Game_Action);
    	new_Game_Entity.addComponent(mainEvents);
    	
    	// Fuege die Entity zum StateBasedEntityManager hinzu
    	entityManager.addEntity(this.stateID, new_Game_Entity);
    	
    	/* Beenden-Entitaet */
    	Entity quit_Entity = new Entity("Beenden");
    	
    	// Setze Position und Bildkomponente
    	quit_Entity.setPosition(new Vector2f(200, 320));
    	quit_Entity.addComponent(new ImageRenderComponent(new Image("images/exit.png")));
    	quit_Entity.setScale(1.0f);
    	
    	// Erstelle das Ausloese-Event und die zugehoerige Action
    	ANDEvent mainEvents_q = new ANDEvent(new MouseEnteredEvent(), new MouseClickedEvent());
    	Action quit_Action = new QuitAction();
    	mainEvents_q.addAction(quit_Action);
    	quit_Entity.addComponent(mainEvents_q);
    	
    	// Fuege die Entity zum StateBasedEntityManager hinzu
    	entityManager.addEntity(this.stateID, quit_Entity);
    	
    }

    /**
     * Wird vor dem Frame ausgefuehrt
     */
    @Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		entityManager.updateEntities(container, game, delta);
	}
    
    /**
     * Wird mit dem Frame ausgefuehrt
     */
	@Override
	public void render(GameContainer container, StateBasedGame game, 
												Graphics g) throws SlickException {
		entityManager.renderEntities(container, game, g);
	}

	@Override
	public int getID() {
		return stateID;
	}
	
}
