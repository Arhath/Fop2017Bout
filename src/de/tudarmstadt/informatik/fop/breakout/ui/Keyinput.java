package de.tudarmstadt.informatik.fop.breakout.ui;

import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;

public class Keyinput implements KeyListener {
	
	GameplayState gs;

    public Keyinput(GameplayState game) {
    	gs = game;
    }

    @Override
    public void inputEnded() {
        // TODO Auto-generated method stub

    }

    @Override
    public void inputStarted() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isAcceptingInput() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void setInput(Input arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(int arg0, char arg1) {
    	//gs.InputChar(arg1);
    }

    @Override
    public void keyReleased(int arg0, char arg1) {
        // TODO Auto-generated method stub

    }
}