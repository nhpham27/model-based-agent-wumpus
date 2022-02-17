/*
 * Class that defines the agent function.
 * 
 * Written by James P. Biagioni (jbiagi1@uic.edu)
 * for CS511 Artificial Intelligence II
 * at The University of Illinois at Chicago
 * 
 * Last modified 2/19/07 
 * 
 * DISCLAIMER:
 * Elements of this application were borrowed from
 * the client-server implementation of the Wumpus
 * World Simulator written by Kruti Mehta at
 * The University of Texas at Arlington.
 * 
 */
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class AgentFunction {
	
	// string to store the agent's name
	// do not remove this variable
	private String agentName = "Agent Smith";
	
	// all of these variables are created and used
	// for illustration purposes; you may delete them
	// when implementing your own intelligent agent
	private int[] actionTable;
	private boolean bump;
	private boolean glitter;
	private boolean breeze;
	private boolean stench;
	private boolean scream;
	private Random rand;
	
	// 
	private int lastAction = -1;
	private boolean alreadyShot;
	private int worldStateSize = 6;
	private Square[][] state;
	static boolean debugMode = false;
	static int trial = 10000;
	public AgentFunction()
	{
		// for illustration purposes; you may delete all code
		// inside this constructor when implementing your 
		// own intelligent agent

		// this integer array will store the agent actions
		actionTable = new int[6];
				  
		actionTable[0] = Action.NO_OP;
		actionTable[1] = Action.GO_FORWARD;
		actionTable[2] = Action.TURN_RIGHT;
		actionTable[3] = Action.TURN_LEFT;
		actionTable[4] = Action.GRAB;
		actionTable[5] = Action.SHOOT;
		
		// new random number generator, for
		// randomly picking actions to execute
		rand = new Random();
		
		// initialize the state of the world
		state = new Square[this.worldStateSize][this.worldStateSize];
		for(int i = 0;i < this.worldStateSize; i++) {
			for(int j = 0;j < this.worldStateSize; j++) {
				state[i][j] = new Square();
				state[i][j].x = i;
				state[i][j].y = j;
			}
		}
		// the agent is initially at 1,1 facing East
		this.state[1][1].agentDirection = '>';
		this.alreadyShot = false;
		if(debugMode == true)
			printState();
	}
	
	public int process(TransferPercept tp)
	{
		// To build your own intelligent agent, replace
		// all code below this comment block. You have
		// access to all percepts through the object
		// 'tp' as illustrated here:
		
		// read in the current percepts
		bump = tp.getBump();
		glitter = tp.getGlitter();
		breeze = tp.getBreeze();
		stench = tp.getStench();
		scream = tp.getScream();
		
		if(glitter == true) {
			return Action.GRAB;
		}
		
		// update the state based on current percept
		// and the most recent action
		this.updateState(lastAction, tp);
		if(debugMode == true)
			this.printState();
		// return action to be performed
		//this.lastAction = actionTable[rand.nextInt(4)];
		lastAction = this.actionRules();
		return lastAction;
	}
	
	// public method to return the agent's name
	// do not remove this method
	public String getAgentName() {
		return agentName;
	}
	
	// print the state of the world
	private void printState() {
		for(int i = this.worldStateSize - 1;i >= 0; i--) {
			for(int j = 0;j < this.worldStateSize; j++) {
				Square temp = state[j][i];
				if(temp.isPit > 0)
					System.out.print("[P]");
				else if(temp.isWall)
						System.out.print("[X]");
				else if(temp.isWumpus > 0)
					System.out.print("[W]");
				else if(temp.agentDirection == ' ')
					System.out.print("[" + temp.noGold + "]");
				else
					System.out.print("[" + temp.agentDirection + "]");
			}
			System.out.println("");
		}
		System.out.println("");
	}
	
	// get agent's location, aka, the square the agent is in
	private Square getAgentLocation() {
		Square agentLoc = null;
		for(int i = 0;i < this.worldStateSize; i++) {
			for(int j = 0;j < this.worldStateSize; j++) {
				if(state[i][j].agentDirection != ' ') {
					agentLoc = state[i][j];
				}
			}
		}
		return agentLoc;
	}
	
	// get the squares around the agent
	private HashMap<String, Square> getAroundSquares(){
		HashMap<String, Square> squares = new HashMap<>();
		// get the current location
		Square curLoc = this.getAgentLocation();
		
		// get the 4 squares around the agent
		// set their positions based on the orientation of the agent
		// Ex: if the agent is facing East, the square on the right is
		// in front, and the square on the left is in the back of the agent
		int x = curLoc.x;
		int y = curLoc.y;
		// get the squares aroung the agent
		Square topSquare = state[x][y+1];
		Square leftSquare = state[x-1][y];
		Square bottomSquare = state[x][y-1];
		Square rightSquare = state[x+1][y];
		// named all the squares based on the agent's orientation
		if(curLoc.agentDirection == 'A') {
			squares.put("front", topSquare);
			squares.put("left", leftSquare);
			squares.put("back", bottomSquare);
			squares.put("right", rightSquare);
		}
		else if(curLoc.agentDirection == '<') {
			squares.put("front", leftSquare);
			squares.put("left", bottomSquare);
			squares.put("back", rightSquare);
			squares.put("right", topSquare);
		}
		else if(curLoc.agentDirection == 'v') {
			squares.put("front", bottomSquare);
			squares.put("left", rightSquare);
			squares.put("back", topSquare);
			squares.put("right", leftSquare);
		}
		else if(curLoc.agentDirection == '>') {
			squares.put("front", rightSquare);
			squares.put("left", topSquare);
			squares.put("back", leftSquare);
			squares.put("right", bottomSquare);
		}
		
		return squares;
	}
	
	// get the current orientation of the agent based on
	// previous orientation and action taken
	public char getDirection(char prevDirection, int action) {
		String orientations = "<A>v";
		// find the index of previous orientation in the list
		// of orientations
		int index = orientations.indexOf(prevDirection);
		int offset = 0;
		// get the offset to add to the index to get
		// new orientation of the agent
		if(action == Action.TURN_LEFT)
			offset = -1;
		else if(action == Action.TURN_RIGHT)
			offset = 1;
		index = index + offset;
		
		// if the index exceed the boundary,
		// set it to the value in the  other 
		// end of the list
		if(index < 0)
			index = orientations.length() - 1;
		else if(index > orientations.length() - 1)
			index = 0;
		
		return orientations.charAt(index);
	}

	class Square {
		int isWumpus;
		int isPit;
		int noGold;
		boolean isSafe;
		boolean isWall;
		char agentDirection;
		boolean hasStench;
		boolean hasBreeze;
		int x,y;
		
		Square(){
			this.isWumpus = 0;
			this.isPit = 0;
			this.noGold = 0;
			this.isWall = false;
			this.isSafe = false;
			this.hasStench = false;
			this.hasBreeze = false;
			this.agentDirection = ' ';
			x = y = -1;
		}
		
		// return max value in all the probabilities
		public int getMaxProb() {
			int[] arr = {this.isPit, this.isWumpus};
			int max = this.isPit;
			for(int val : arr) {
				if(max < val)
					max = val;
			}
			return max;
		}
	}
	
//	// update the world state based on the current percept and 
//	// most recent action
	private void updateState(int action, TransferPercept tp) {
		// get the percept
		bump = tp.getBump();
		glitter = tp.getGlitter();
		breeze = tp.getBreeze();
		stench = tp.getStench();
		scream = tp.getScream();
		// get the square the agent is currently in
		Square agentLoc = this.getAgentLocation();
		// update the location and orientation of the agent
		// if the agent does not sense a bump
		HashMap<String, Square> squares = this.getAroundSquares();
		if(bump == false && action != -1) {
			if(action == Action.GO_FORWARD) {
				// if the agent goes forward, update the location
				squares.get("front").agentDirection = agentLoc.agentDirection;
				squares.get("front").isSafe = true;
				agentLoc.isSafe = true;
				agentLoc.agentDirection = ' ';
			}
			else {
				// if the agent turns, update the orientation
				agentLoc.agentDirection = this.getDirection(agentLoc.agentDirection, action);
			}
			
			// now, the location and orientation of the agent might be changed
			// so, get the new location and the squares around it
			agentLoc = this.getAgentLocation();
			squares = this.getAroundSquares();
		}
		
		if(agentLoc.noGold < 9)
			agentLoc.noGold += 1;
		// update the percepts in current square
		
		if(stench == true) {
			agentLoc.hasStench = true;
		}
		if(breeze == true) {
			agentLoc.hasBreeze = true;
		}
		
		// there is no stench or breeze, mark
		// all around squares as safe
		if(stench == false && breeze == false) {
			squares.get("front").isSafe = true;
			squares.get("left").isSafe = true;
			squares.get("right").isSafe = true;
			squares.get("back").isSafe = true;
		}
		
		// check all the squares and update the probabilities
		this.updateSquares();
		// update the squares around the agent based on the percept
		// if the agent senses bump, mark the square in front as Wall
		if(bump == true) {
			squares.get("front").isWall = true;
			squares.get("front").noGold = 10;
		}
		
		// if there is a scream, mark all squares labeled as Wumpus as safe
		if(scream == true) {
			for(int i = this.worldStateSize - 1;i >= 0; i--) {
				for(int j = 0;j < this.worldStateSize; j++) {
					if(state[i][j].isWumpus > 0) {
						state[i][j].isWumpus = 0;
					}
					state[i][j].hasStench = false;
				}
			}
		}
		else {
			// there is no scream and the last action was SHOOT
			// there is no Wumpus infront of the agent, so clear
			// the square in front of the agent
			if(action == Action.SHOOT && !alreadyShot) {
				this.alreadyShot = true;
				squares.get("front").isWumpus = 0;
				if(squares.get("front").isPit == 0) {
					squares.get("front").isSafe = true;
				}
			}
		}
	}
	
	private void updateSquares() {
		// loop through all squares
		Square square = null;
		for(int i = 1;i < this.worldStateSize - 1; i++) {
			for(int j = 1;j < this.worldStateSize - 1; j++) {
				square = this.state[i][j];
				// get the squares around it
				int x = square.x;
				int y = square.y;
				// get the squares aroung the agent
				// top, left, bottom, right
				Square[] aroundSquares = {state[x][y+1], state[x-1][y], 
											state[x][y-1], state[x+1][y]};
				
				if(square.isSafe == false) {
					square.isWumpus = 0;
					Square temp = null;
					for(Square s : aroundSquares) {
						// visited squares will have noGold > 0
						if(s.noGold > 0) {
							if(s.hasStench) {
								square.isWumpus += 3;
								// record the square that has 
								// probability of having wumpus
								// greater than 3
								if(s.isWumpus > 3) {
									temp = s;
								}
							}
							else {
								square.isWumpus = 0;
								break;
							}
						}
					}
					
					if(temp != null) {
						for(int a = this.worldStateSize - 1;a >= 0; a--) {
							for(int b = 0;b < this.worldStateSize; b++) {
								if(state[a][b].isWumpus > 0 && state[a][b].isPit == 0) {
									state[a][b].isWumpus = 0;
									state[a][b].noGold = 0;
									state[a][b].isSafe = true;
								}
							}
						}
						temp.isWumpus = 10;
						temp.noGold = 10;
						temp.isSafe = false;
					}
					
					square.isPit = 0;
					for(Square s : aroundSquares) {
						// visited squares will have noGold > 0
						if(s.noGold > 0) {
							if(s.hasBreeze) {
								square.isPit += 3;
							}
							else {
								square.isPit = 0;
								break;
							}
						}
					}
				}
				else {
					square.isPit = 0;
					square.isWumpus = 0;
				}
			}
		}
	}
	
	private int actionRules() {
		// get the squares around the agent
		HashMap<String, Square> squares = this.getAroundSquares();
		Square frontSquare = squares.get("front");
		Square leftSquare = squares.get("left");
		Square rightSquare = squares.get("right");
		Square backSquare = squares.get("back");
		Square agentLoc = this.getAgentLocation();
//		System.out.println("front pit: " + frontSquare.isPit);
//		System.out.println("front wumpus: " + frontSquare.isWumpus);
		
		// if there's no gold, stop exploring the environment
		if(this.checkGold() == false) {
			return Action.NO_OP;
		}
		
		if(frontSquare.isWall) {
			return rand.nextBoolean() ? Action.TURN_LEFT : Action.TURN_RIGHT;
		}

		if(frontSquare.isWumpus > 0 && !this.alreadyShot) {
			return Action.SHOOT;
		}
		
		if(frontSquare.isWumpus > 0 || frontSquare.isPit > 0) {
			int blockAgent = 10;
			
			if((leftSquare.isPit > 0 || leftSquare.isWumpus > 0 || leftSquare.isWall)
					&& (rightSquare.isPit > 0 || rightSquare.isWumpus > 0 || rightSquare.isWall)
					&& (backSquare.isPit > 0 || backSquare.isWumpus > 0 || backSquare.isWall)) {
				return Action.NO_OP;
			}
			else {
				int count = 0;
				if(leftSquare.isPit > 0) {
					count++;
				}
				if(rightSquare.isPit > 0) {
					count++;
				}
				if(backSquare.isPit > 0) {
					count++;
				}
				if(frontSquare.isPit > 0) {
					count++;
				}
				if(count >= 2
					&& agentLoc.noGold > 8) {
					blockAgent = 0;
				}
			}
			
			if(frontSquare.isPit == 3
				&& (leftSquare.isPit > 3 || rightSquare.isPit > 3) 
				&& blockAgent == 0) {
				return Action.GO_FORWARD;
			}
			else {
				return rand.nextBoolean() ? Action.TURN_LEFT : Action.TURN_RIGHT;
			}
		}
		else {
			if((leftSquare.isPit > 0 || leftSquare.isWumpus > 0 || leftSquare.isWall)
					&& (rightSquare.isPit > 0 || rightSquare.isWumpus > 0 || rightSquare.isWall)
					&& (backSquare.isPit > 0 || backSquare.isWumpus > 0 || backSquare.isWall)) {
				return Action.GO_FORWARD;
			}
//			
			if(frontSquare.noGold <= rightSquare.noGold
				&& frontSquare.noGold <= leftSquare.noGold) {
				return Action.GO_FORWARD;
			}
			if(leftSquare.noGold < frontSquare.noGold 
				&& leftSquare.noGold < rightSquare.noGold
				&& leftSquare.noGold < frontSquare.noGold) {
				return Action.TURN_LEFT;
			}
			if(rightSquare.noGold < leftSquare.noGold 
				&& rightSquare.noGold < frontSquare.noGold) {
				return Action.TURN_RIGHT;
			}
			// if the front square is safe and
			// the square is unvisited, proceed
			// if it is visited, there is 20% to
			// go forward
			else {
				if(rand.nextInt(10) < frontSquare.noGold)
					return rand.nextBoolean() ? Action.TURN_LEFT : Action.TURN_RIGHT;
				else
					return Action.GO_FORWARD;
			}
		}
	}
	
	// check if there exists gold in the empty squares
	private boolean checkGold() {
		boolean hasGold = false, hasPath = true;
		for(int i = 1;i < this.worldStateSize - 1; i++) {
			for(int j = 1;j < this.worldStateSize - 1; j++) {
				if(state[i][j].noGold == 0)
					hasGold = true;
			}
		}
		
//		for(int i = 1;i < this.worldStateSize - 1; i++) {
//			for(int j = 1;j < this.worldStateSize - 1; j++) {
//				if(state[i][j].noGold == 0)
//					hasGold = true;
//			}
//		}
		
		return (hasGold && hasPath);
	}
}