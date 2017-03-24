package de.tudarmstadt.informatik.fop.breakout.ui;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import eea.engine.action.Action;
import eea.engine.action.basicactions.ChangeStateAction;
import eea.engine.action.basicactions.MoveUpAction;
import eea.engine.action.basicactions.MoveRightAction;
import eea.engine.component.Component;
import eea.engine.component.render.ImageRenderComponent;
import eea.engine.entity.Entity;
import eea.engine.entity.StateBasedEntityManager;
import eea.engine.event.basicevents.KeyPressedEvent;
import eea.engine.event.basicevents.CollisionEvent;
import eea.engine.event.basicevents.LoopEvent;
import de.tudarmstadt.informatik.fop.breakout.constants.GameParameters;
import de.tudarmstadt.informatik.fop.breakout.factories.*;

/**
 * @author Timo BÃ¤hr
 *
 * Diese Klasse repraesentiert das Spielfenster, indem ein Wassertropfen
 * erscheint und nach unten faellt.
 */
public class GameplayState extends BasicGameState implements GameParameters {

	protected int stateID; 							// Identifier dieses BasicGameState
	protected StateBasedEntityManager entityManager; 	// zugehoeriger entityManager
	
	
	//anfangsstatus des balls am brett
	private boolean isLocked = true;
	
	//do once at game finish
	private boolean bDoOnce = true;
	
	//skip collision for a number of frames to prevent bad behavior
	private boolean bSkipCollision = false;
	private int nSkipFrames = 4;
	private int nSkipCount = 0;
	
	//game is runing?
	private boolean isRuning = true;
	
	//did we achieve a highscore
	private boolean isHighscore = false;
	private int nScorePosition = 0;
	
	//already stored?
	private boolean scoreStored = false;

	
	private float fTotalHighscore = 1000000.0f;
	private float fTimeMultiplier = 20.0f;
	
	private float fPlayerScore = 0;
	
	private int nTotalBlocks = 0;
	
	//gamestate parsed via gameentity 
	private int GAMESTATE = 1; // 1 = game 2 = lost 3 = won
	
	// playername for scoring
	private String playerName = "aaa";
	
	//timer for level
	private float fTime = 0;
	private float fTimeForLevel = 400.0f;
	
	//player constants
	private static final float MAX_PLAYER_SPEED = 0.6f;
	private static final float MAX_BALL_SPEED = 1.0f;
	private static final float BALL_START_SPEED = 0.3f;
	
	private static final int MAX_LIFES = 4;
	private int nLifes = MAX_LIFES;
	
	//do we require a name input?
	private boolean bName = false;
	
	//ball and player varriables
	private Vector2f ballSpeed = new Vector2f(0,0);
	private Vector2f vBallStartPos = new Vector2f(400, 490);
	
	private Vector2f playerSpeed = new Vector2f(0,0);
	
	private Entity entPlayer;
	private Entity entBall;
	
	private List<Entity> vBricks = new ArrayList<Entity>();
	private List<Integer> vBrickHp = new ArrayList<Integer>();
	
	LoopEvent move = new LoopEvent();
	
	LoopEvent pMove = new LoopEvent();
	
	Action pMoveX = new MoveRightAction(0.0f);
	
	Action actionX = new MoveRightAction(0.0f);
	Action actionY = new MoveUpAction(0.0f);
    
    public GameplayState( int sid ) {
       stateID = sid;
       entityManager = StateBasedEntityManager.getInstance();
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
    	
    	Entity entState = new Entity("gamestate");
    	entState.setScale(1.0f);
    	entState.setPosition(new Vector2f(-100, -100));
    	entState.setVisible(false);
    	entityManager.addEntity(stateID, entState);
    	
    	
    	//create lives
    	for(int i = 0; i <= MAX_LIFES - 1; i++)
    	{
    		Entity entlife = new Entity("life" + i);
    		entlife.setPassable(false);
    		entlife.setVisible(true);
    		entlife.setPosition(new Vector2f(i*50 + 30, 577));
    		if(!DEBUG)
    		entlife.addComponent(new ImageRenderComponent(new Image("/images/ball.png"))); // Bildkomponente
    		
    		entityManager.addEntity(stateID, entlife);
    	}
    	
    	// Bei Drücken der ESC-Taste zurueck ins Hauptmenue wechseln
    	Entity esc_Listener = new Entity("ESC_Listener");
    	KeyPressedEvent esc_pressed = new KeyPressedEvent(Input.KEY_ESCAPE);
    	esc_pressed.addAction(new ChangeStateAction(Breakout.MAINMENU_STATE));
    	esc_Listener.addComponent(esc_pressed);    	
    	entityManager.addEntity(stateID, esc_Listener);
    	
    	//space frees the ball
    	Entity space_Listener = new Entity("SPACE_Listener");
    	KeyPressedEvent space_pressed = new KeyPressedEvent(Input.KEY_SPACE);
    	space_pressed.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta, Component event) {
				if (isRuning)
				{
					
					if (isLocked)
					{
						ballSpeed.y = BALL_START_SPEED;
						if (playerSpeed.x != 0)
							SetBallSpeedX(Math.signum(playerSpeed.x) * BALL_START_SPEED);
						else
							SetBallSpeedX((float)(BALL_START_SPEED * Math.signum(Math.random() - 0.5f)));
						
						isLocked = false;
					}
				}
			}
    	});
    	space_Listener.addComponent(space_pressed);
    	entityManager.addEntity(stateID, space_Listener);
    	
    	// p pauses the game
    	Entity pause_Listener = new Entity("PAUSE_Listener");
    	KeyPressedEvent pause_pressed = new KeyPressedEvent(Input.KEY_P);
    	pause_pressed.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta, Component event) {
				gc.setPaused(!gc.isPaused());
			}
    	});
    	pause_Listener.addComponent(pause_pressed);    	
    	entityManager.addEntity(stateID, pause_Listener);
    	
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
    	
    	
    	
    	//call creation methods
    	ReadLevel();

    	CreatePlayer();
    	
    	entBall = CreateBall(BALL_ID);
    	
    	BallReset();
    }
    
    //level parser
    
    public void ReadLevel()
    {
    	if(DEBUG)
    		return;
    	
    	try {
			File file = new File(System.getProperty("user.dir") + "/maps/level1.map");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			
			int lineCount = 1, brickCount = 0; 
			int idBrick = 0;
			int nTotal = 0;
			
			Entity entBrick = null;
			
			while ((line = bufferedReader.readLine()) != null) {
				
				String[] parts = line.split(",");
				System.out.println(parts[0]);
				
				for (int i = 0; i <= parts.length-1; i++)
				{
					int hp = Integer.parseInt(parts[i]);
					System.out.println(hp);
					
					if (hp > 0)
					{
						entBrick = new BrickFactory(hp, Integer.toString(idBrick)).createEntity();
						Vector2f size = entBrick.getSize();
						Vector2f pos = new Vector2f( size.x * (brickCount + 0.5f),  size.y * (lineCount + 0.5f));
						entBrick.setPosition(pos);
						
						nTotal += 1;
						
						entityManager.addEntity(stateID, entBrick);
					}
						
					vBricks.add(entBrick);
					vBrickHp.add(hp);
					
					brickCount += 1;
					idBrick += 1;
				}
				
				System.out.println(" newline" + lineCount);
				lineCount += 1;
				brickCount = 0;
				
				stringBuffer.append(line);
				stringBuffer.append("\n");
			}
			
			nTotalBlocks = nTotal;
			
			fileReader.close();
			//System.out.println("Contents of file:");
			//System.out.println(stringBuffer.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
    //create the player stick
    public void CreatePlayer() throws SlickException
    {
	  //Create Player
		entPlayer = new Entity(PLAYER_ID);
		if(!DEBUG)
		entPlayer.addComponent(new ImageRenderComponent(new Image("/images/stick.png"))); // Bildkomponente
		entPlayer.setPosition(new Vector2f(400, 515));
		
		entPlayer.setVisible(true);
		entPlayer.setPassable(false);
		entityManager.addEntity(stateID, entPlayer);
		
		entPlayer.addComponent(pMove);
		
    	LoopEvent pUpdate = new LoopEvent();
    	
    	//player update function
    	
    	pUpdate.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta, Component event) {
				
				pMove.removeAction(pMoveX);
				
				if (!isRuning)
					return;
				
				boolean bl = gc.getInput().isKeyDown(Input.KEY_A) || gc.getInput().isKeyDown(Input.KEY_LEFT);
		    	boolean br = gc.getInput().isKeyDown(Input.KEY_D) || gc.getInput().isKeyDown(Input.KEY_RIGHT);
		    	
		    	Vector2f pvec = entPlayer.getPosition();
		    	
		    	if (!bl && !br)
		    		playerSpeed.x = 0.0f;
		    	
		    	if (bl)
		    	{
		    		if (pvec.x >= entPlayer.getSize().x/2)
		    			playerSpeed.x = Math.max(-1 * MAX_PLAYER_SPEED, Math.abs(playerSpeed.x - 0.1f) * -1.1f);
		    		else
		    			playerSpeed.x = 0.0f;
		    	}
		    	
		    	if (br)
		    	{
		    		if (pvec.x <= WINDOW_WIDTH - entPlayer.getSize().x/2)
		    			playerSpeed.x = Math.min(MAX_PLAYER_SPEED, Math.abs(playerSpeed.x + 0.1f) * 1.1f);
		    		else
		    			playerSpeed.x = 0.0f;
		    	}
		    	
		    	pMoveX = new MoveRightAction(playerSpeed.x);		
		    	pMove.addAction(pMoveX);
		    	
		    	//System.out.println(CountBlocks());
			}
			});
    	entPlayer.addComponent(pUpdate);
    	
    	
    	
    }
     
    //create the ball entity
    
    public Entity CreateBall(String id) throws SlickException
    {
    	Entity entBall = new Entity(id);
    	if(!DEBUG)
    	entBall.addComponent(new ImageRenderComponent(new Image("/images/ball.png"))); // Bildkomponente
    	entBall.setPosition(vBallStartPos);
    	
    	entBall.setVisible(true);
    	entBall.setPassable(false);
    	entityManager.addEntity(stateID, entBall);
    	
    	entBall.addComponent(move);
    	
    	//ball collision handler
    	
    	CollisionEvent collision = new CollisionEvent();
    	
    	collision.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta,
					Component event) {
	    	
		    	//System.out.println(nSkipCount);
				

				Entity target = collision.getCollidedEntity();
				
				//System.out.println(target.getID());
				
				Vector2f pall = entBall.getPosition();
				Vector2f vTarget = target.getPosition();
			
				
				double colOri = VecTheta(pall, vTarget);
				
				double dOffX = (90.0d - Math.toDegrees(Math.atan(target.getSize().y / target.getSize().x)));
				double dOffY = (360.0d - dOffX*4)/2;
				
				String tID = (String)target.getID();
				String id = "";
				
				if (tID.contains("block"))
				{
					id = tID.substring(5);
					tID = "block";
				}
				
				//with what did we collide?
				
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
						PlayerLooseLife();
					break;

				case "player":
					if (bSkipCollision)
						return;
				
					try {
		    			PlaySound("hitStick.wav");
		    		} catch (SlickException e) {
		    			// TODO Auto-generated catch block
		    			e.printStackTrace();
		    		}
					
					// where did we collide? so we can reflect
					
					if (FInRange(pall.x - entBall.getSize().x, vTarget.x, target.getSize().x/2) || FInRange(pall.x + entBall.getSize().x, vTarget.x, target.getSize().x/2))
						if (pall.y >= vTarget.y)
							ballSpeed.y = Math.abs(ballSpeed.y) * -1;
						else
						{
							ballSpeed.y = Math.abs(ballSpeed.y);
							if (playerSpeed.x != 0)
								ballSpeed.x = Math.signum(playerSpeed.x) * Math.abs(ballSpeed.x);
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
					
					// we hit a block
					
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
					
					BlockHit(target, id, 1);

					//target.setVisible(false);
					//target.setPassable(true);
					break;
				}
				
				//skip 4 frames to not spamm collision in certain cases
				
				SkipCollision(4);
			}    	
    		});
    	
    	
    	entBall.addComponent(collision);

    	
    	// ball update function
    	
    	LoopEvent ballUpdate = new LoopEvent();
    	ballUpdate.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta, Component event) {
				
				//removing movement calculating and adding movement
				
				move.removeAction(actionX);
		    	move.removeAction(actionY);
		    	
				if(!isRuning)
					return;
		    	
				//System.out.println(ballSpeed.x);
				
				SetBallSpeedX(ballSpeed.x * (1 + SPEEDUP_VALUE));
				
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
    	
    	return entBall;
    }
    
    //total block hp 0 = win
    
    public int CountBlockHp()
    {
    	int HP = 0;
    	
    	for(int i = 0; i < vBricks.size(); i++)
    		HP += vBrickHp.get(i);
    	
    	return HP;
    }
    
    //total blocks 0 = win
    
    public int CountBlocks()
    {
    	int c = 0;
    	
    	for(int i = 0; i < vBricks.size(); i++)
    		if (vBricks.get(i) != null && vBrickHp.get(i) > 0)
    			c += 1;
    	
    	return c;
    }
    
    //hit that block with some damage
    
    public void BlockHit(Entity t, String id, int dmg)
    {
    	int ID = Integer.parseInt(id);
    	int hp = vBrickHp.get(ID);
    	hp -= dmg;
    	vBrickHp.set(ID, Math.max(hp, 0));
    	
    	if (hp <= 0)
    	{
    		try {
				PlaySound("Omnomnom.wav");
			} catch (SlickException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		vBricks.set(ID, null);
    		entityManager.removeEntity(stateID, t);
    	}
    	else
    	{	
    		
    		try {
    			PlaySound("hitBlock.wav");
    		} catch (SlickException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
	    	try {
				// Bild laden und zuweisen
				t.addComponent(new ImageRenderComponent(new Image("images/block_" + hp + ".png")));
			} catch (SlickException e) {
				System.err.println("Cannot find file images/brick.png!");
				e.printStackTrace();
			}
    	}
    	
    	int hpTotal = CountBlockHp();
    	
    	//System.out.println("total hp " + hpTotal);
    	if (hpTotal <= 0)
    		GameWon();
    }
    
    //game won
    
    public void GameWon()
    {
    	isRuning = false;
    	//System.out.println("YOU WIN");
    	Entity ent = entityManager.getEntity(stateID, "gamestate");
    	ent.setScale(3.0f);
    }
    
    //game over

    public void GameOver()
    {
    	
    	isRuning = false;
    	//System.out.println("game over");
    	Entity ent = entityManager.getEntity(stateID, "gamestate");
    	ent.setScale(2.0f);
    }
    
    //called on game end to store the score and player name
    
    public void StoreHighscore(int score, String name)
    {
    	String filepath = System.getProperty("user.dir") + "/maps/highscores.txt";
    	
    	try {
    		
    		//open file
			File file = new File(filepath);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			String temp = "";
			
			int nline = 1;
			List<String> lines = new ArrayList<String>();
			
			//get all lines into list
			
			while ((line = bufferedReader.readLine()) != null){
				lines.add(line);
			}
			
			//iterate list and check if score is highscore
			
			for(int i = 0; i < lines.size(); i++)
			{
				String[] parts = lines.get(i).split(",");
				//System.out.println(parts[0]);
				
				if(score > Integer.parseInt(parts[0].trim()))
				{
					parts[0] = Integer.toString(score);
					parts[1] = name.trim();
					
					lines.add(parts[0] + "," + parts[1]);
					
					//compare scores leave out names then sort by highest score
					
					Collections.sort(lines, new Comparator<String>() {
				        @Override
				        public int compare(String s1, String s2)
				        {
				        	String[] p1 = s1.split(",");
				        	String[] p2 = s2.split(",");
				        	
				        	if (Integer.parseInt(p1[0]) == Integer.parseInt(p2[0]))
				        		return 0;
				        	
				        	if (Integer.parseInt(p1[0]) > Integer.parseInt(p2[0]))
				        		return 1;
				        	
				        	if (Integer.parseInt(p1[0]) < Integer.parseInt(p2[0]))
				        		return -1;
				        	
				        	return 0;
				        }
				    });
					
					//cut out last part because we now have 11 entries instead of 10
					lines.remove(0);
					
					java.util.Collections.reverse(lines);
					
					isHighscore = true;
					nScorePosition = nline;
					break;
				}
				
				nline += 1;

			}

    		fileReader.close();
    		
    		//write file with new scores
    		
    		PrintWriter writer = new PrintWriter(filepath, "UTF-8");
    		for(int i = 0; i < lines.size(); i++)
			{
    			writer.print(lines.get(i) + "\n");
			}
    	    writer.close();
    		
			//System.out.println("Contents of file:");
			//System.out.println(stringBuffer.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    //check if we have a highscore same as above
    
    public boolean CheckHighscore(int score)
    {
    	String filepath = System.getProperty("user.dir") + "/maps/highscores.txt";
    	
    	try {
			File file = new File(filepath);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			String temp = "";
			
			int nline = 1;
			List<String> lines = new ArrayList<String>();
			
			while ((line = bufferedReader.readLine()) != null){
				lines.add(line);
			}
			
			for(int i = 0; i < lines.size(); i++)
			{
				String[] parts = lines.get(i).split(",");
				//System.out.println(parts[0]);
				
				if(score > Integer.parseInt(parts[0].trim()))
				{
					nScorePosition = nline;
					return true;
				}
				
				nline += 1;

			}

    		fileReader.close();
    		
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return false;
    }
    
    //player looses a life
    
    public void PlayerLooseLife()
    {
    	
    	//we hide the live entity at the bottom
    	
    	Entity entlife = entityManager.getEntity(stateID, "life" + (nLifes - 1));
    	nLifes -= 1;
		if (entlife != null)
			entlife.setVisible(false);
		
		if (nLifes <= 0)
    		GameOver();
    	else
    	{
    		try {
    			PlaySound("Autsch.wav");
    		} catch (SlickException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
    		BallReset();
    	}
    }
 
    
    public void SetBallSpeedX(float sp)
    {
    	if (sp == 0)
    		ballSpeed.x = sp;
    	
    	if(sp > 0)
    		ballSpeed.x = Math.min(MAX_BALL_SPEED, sp);
    	
    	if(sp < 0)
    		ballSpeed.x = Math.max(-1 * MAX_BALL_SPEED, sp);
    }
    
    public void SetBallSpeedY(float sp)
    {
    	if (sp == 0)
    		ballSpeed.y = sp;
    	
    	if(sp > 0)
    		ballSpeed.y = Math.min(MAX_BALL_SPEED, sp);
    	
    	if(sp < 0)
    		ballSpeed.y = Math.max(-1 * MAX_BALL_SPEED, sp);
    }
    
    //reset the ball when it respawns
    
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
    
    //is the float value x in intervall [a+off; a-off] ? we use this for collision detection
    
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
    
    public void SkipCollision(int nFrames)
    {
    	nSkipFrames = nFrames;
    	bSkipCollision = true;
    	nSkipCount = 0;
    }

    /**
     * Wird vor dem Frame ausgefuehrt
     */
    @Override
	public void update(GameContainer gc, StateBasedGame game, int delta) throws SlickException {
		// StatedBasedEntityManager soll alle Entities aktualisieren

    	entityManager.updateEntities(gc, game, delta);
    	
    	//get gamestate
    	Entity entState = entityManager.getEntity(stateID, "gamestate");
		int gameState = (int)entState.getScale();
    	
		//timer while not paused
    	if (!gc.isPaused() && gameState <= 1)
    		fTime += delta;

		GAMESTATE = gameState;
	}
    
    /**
     * Wird mit dem Frame ausgefuehrt 	
     */
	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		// StatedBasedEntityManager soll alle Entities rendern
		
		//skip on debug
		if (DEBUG)
			return;
		
		//background
		g.setColor(Color.black);
		
		g.drawImage(new Image("/images/background.png"), 0, 0);	
		entityManager.renderEntities(container, game, g);
		
		//pause screen
		if (container.isPaused())
		{
			g.drawImage(new Image("/images/pause.png"), 300, 360);
		}
		
			//g.drawImage(new Image("/images/pause.png"), 0, 0);
			//g.drawString("GAME", 200, 200);
		
		//calculate highscore
		int nBricks = 0;
		
		for(int i = 0; i < vBricks.size(); i++)
		{
			Entity ent = entityManager.getEntity(stateID, "block" + i);
			
			if (ent != null)
				nBricks += 1;
		}
		
		float timeLeft = fTimeForLevel - fTime / 1000;
		
		float highscore =  (1 - (float)nBricks / nTotalBlocks) * fTotalHighscore;
		
		if (timeLeft <= 0)
			GameOver();
		
		g.drawString("|    " + nBricks + " / " + nTotalBlocks + "  |  " + (int)(timeLeft) + "s" + "  |  " + (int)highscore, 130, 10);
		
		//System.out.println(highscore + "ents: " + nTotalBlocks + " / " + nBricks + " time: " + fTime / 1000);

		//System.out.println(gameState);
		
		fPlayerScore = highscore;
		
		//check if game is over
		
		if(GAMESTATE == 3)
			fPlayerScore = highscore * fTimeMultiplier * timeLeft / fTimeForLevel;

		if (GAMESTATE == 2 || GAMESTATE == 3)
			OnGameEnd();
		
		switch (GAMESTATE)
		{
		case 1:
			
			break;
			
			//loose
		case 2:
			
			g.drawImage(new Image("/images/Lose.png"), 300, 360);
			HighScoreScreen(container, game, g, highscore);
			break;
			//win
		case 3:
			g.drawImage(new Image("/images/win.png"), 300, 360);
			
			
			HighScoreScreen(container, game, g, highscore);
			break;
		}
		
	}
	
	//do once on game end
	
	public void OnGameEnd()
	{
		if (!bDoOnce)
			return;
		
		bDoOnce = false;
		isHighscore = CheckHighscore((int)fPlayerScore);
		
		if (isHighscore)
		{
			try {
				PlaySound("Brett vergrößern.wav");
			} catch (SlickException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			bName = false;
		}
		else
		{
			try {
				PlaySound("Brett verkleinern.wav");
			} catch (SlickException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			bName = true;
		}
			
	}
	
	//keyboard events for entering the name
	
	public void keyPressed(int key, char c) {
		
		if (key == Input.KEY_ENTER) {
        	bName = true;
        }
		
		if (key == Input.KEY_BACK && !bName) {
			if (playerName.trim().length() > 0)
				playerName = playerName.trim().substring(0, playerName.trim().length()-1);
			
			System.out.println(playerName.length());
        }

		
        if(isHighscore && !bName)
        	playerName = playerName.trim() + Character.toString(c);

            super.keyPressed(key, c);
	}

	public void keyReleased(int key, char c) {

        if (key == Input.KEY_0) {
        }

            super.keyReleased(key, c);
	}
	
	//this draws the highscore screen and waits for name input if neccesary
	
	public void HighScoreScreen(GameContainer gc, StateBasedGame game, Graphics g, float score)
	{
		
		if (!scoreStored && bName)
		{
			StoreHighscore((int)score, playerName);
			scoreStored = true;

			if (isHighscore)
			{
				try {
					PlaySound("Hovern.wav");
				} catch (SlickException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				game.enterState(HIGHSCORE_STATE);
			}
		}
		
		if (isHighscore)
		{
			g.drawString("New Highscore: " + (int)score + " !!", 500, 400);
			g.drawString(nScorePosition  + ". " + "ENTER NAME: " + playerName, 500, 450);
		}
		else
			g.drawString("Score: " + (int)score, 500, 400);
	}
	
	public float getBallSpeed()
	{
		return ballSpeed.x;
	}
	
	public void setBallSpeed(float speed)
	{
		ballSpeed.x = speed;
		ballSpeed.y = speed;
	}
	
	public Entity getBall()
	{
		return entBall;
	}
	
	public void playerAddLifes(int lifes)
	{
		nLifes += lifes;
	}
	
	public void playerSetLifes(int lifes)
	{
		nLifes = lifes;
	}
	
	public int playerGetLifes()
	{
		return nLifes;
	}
	
	// sound player
	public void PlaySound(String s) throws SlickException
	{
		if (DEBUG)
			return;
		
		Music Sound_1 = new Music(System.getProperty("user.dir") + "/sounds/" + s);
    	Sound_1.play(1.0f, 0.4f);
	}

	@Override
	public int getID() {
		return stateID;
	}
}
