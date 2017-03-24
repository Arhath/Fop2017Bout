package de.tudarmstadt.informatik.fop.breakout.factories;

import de.tudarmstadt.informatik.fop.breakout.interfaces.IHitable;

public class Block implements IHitable{
	
	int nLifes = 0;
	String ID;
	
	public Block(String id, int hits)
	{
		ID = id;
		nLifes = hits;
	}
	
	

	@Override
	public void setHitsLeft(int value) {
		// TODO Auto-generated method stub
		nLifes = value;
	}

	@Override
	public int getHitsLeft() {
		// TODO Auto-generated method stub
		return nLifes;
	}

	@Override
	public void addHitsLeft(int value) {
		// TODO Auto-generated method stub
		nLifes += value;
	}

	@Override
	public boolean hasHitsLeft() {
		// TODO Auto-generated method stub
		return nLifes > 0;
	}
	
	public String getID()
	{
		return ID;
	}

}
