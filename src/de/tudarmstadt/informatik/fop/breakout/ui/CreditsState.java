package de.tudarmstadt.informatik.fop.breakout.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import de.tudarmstadt.informatik.fop.breakout.constants.GameParameters;
import eea.engine.action.Action;
import eea.engine.action.basicactions.ChangeStateAction;
import eea.engine.action.basicactions.ChangeStateInitAction;
import eea.engine.action.basicactions.QuitAction;
import eea.engine.component.render.ImageRenderComponent;
import eea.engine.entity.Entity;
import eea.engine.entity.StateBasedEntityManager;
import eea.engine.event.ANDEvent;
import eea.engine.event.basicevents.KeyPressedEvent;
import eea.engine.event.basicevents.MouseClickedEvent;
import eea.engine.event.basicevents.MouseEnteredEvent;

/**
 * @author Timo Bähr
 *
 * Diese Klasse repraesentiert das Menuefenster, indem ein neues
 * Spiel gestartet werden kann und das gesamte Spiel beendet 
 * werden kann.
 */
public class CreditsState extends BasicGameState implements GameParameters {

	private int stateID; 							// Identifier von diesem BasicGameState
	private StateBasedEntityManager entityManager; 	// zugehoeriger entityManager
	
	private final int distance = 100;
    private final int start_Position = 180;
    
    CreditsState( int sid ) {
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
    	if(!DEBUG)
    	background.addComponent(new ImageRenderComponent(new Image("/images/creditsScreen.png"))); // Bildkomponente
    	
    	Entity esc_Listener = new Entity("ESC_Listener");
    	KeyPressedEvent esc_pressed = new KeyPressedEvent(Input.KEY_ESCAPE);
    	esc_pressed.addAction(new ChangeStateAction(Breakout.MAINMENU_STATE));
    	esc_Listener.addComponent(esc_pressed);    	
    	entityManager.addEntity(stateID, esc_Listener);
    	    	
    	// Hintergrund-Entitaet an StateBasedEntityManager uebergeben
    	entityManager.addEntity(stateID, background);
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
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		if (DEBUG)
			return;
		
		entityManager.renderEntities(container, game, g);
	}

	@Override
	public int getID() {
		return stateID;
	}
	
}
