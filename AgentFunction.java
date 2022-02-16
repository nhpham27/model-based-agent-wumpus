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
	private boolean debugMode = false;
	public AgentFunction()
	{
		// for illustration purposes; you may delete all code
		// inside this constructor when implementing your 
		// own intelligent agent

		// this integer array will store the agent actions
//		actionTable = new int[6];
//				  
//		actionTable[0] = Action.NO_OP;
//		actionTable[1] = Action.GO_FORWARD;
//		actionTable[2] = Action.TURN_RIGHT;
//		actionTable[3] = Action.TURN_LEFT;
//		actionTable[4] = Action.GRAB;
//		actionTable[5] = Action.SHOOT;
		
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
		this.state[1][1].hasAgent = '>';
		this.alreadyShot = false;
		if(debugMode == true)
			printState();
	}
	
	// update the world state based on the current percept and 
	// most recent action
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
				squares.get("front").hasAgent = agentLoc.hasAgent;
				squares.get("front").isSafe = true;
				agentLoc.isSafe = true;
				agentLoc.hasAgent = ' ';
			}
			else {
				// if the agent turns, update the orientation
				agentLoc.hasAgent = this.getOrientation(agentLoc.hasAgent, action);
			}
			
			// now, the location and orientation of the agent might be changed
			// so, get the new location and the squares around it
			agentLoc = this.getAgentLocation();
			squares = this.getAroundSquares();
		}
		
		// check the squares around, if any square which is marked
		// as Wumpus or Pit but there is no respective sense, set
		// that square to safe
		for(Map.Entry<String, Square> item: squares.entrySet()) {
			Square square = item.getValue();
			if(square.isPit && breeze == false) {
				square.isPit = false;
			}
			if(square.isWumpus && stench == false) {
				square.isWumpus = false;
			}
		}
		
		
		// update the squares around the agent based on the percept
		// if the agent senses bump, mark the square in front as Wall
		if(bump == true) {
			squares.get("front").isWall = true;
		}
		else if(stench == true && breeze == false) {
			// if the agent senses only stench
			// mark all the squares around the agent that
			// are not safe as Wumpus
			for(Map.Entry<String, Square> item: squares.entrySet()) {
				Square square = item.getValue();
				if(!square.isSafe) {
					square.isWumpus = true;
				}
			}
		}
		else if(breeze == true && stench == false) {
			// if the agent senses only breeze
			// mark all the squares around the agent that
			// are not safe as Pit
			for(Map.Entry<String, Square> item: squares.entrySet()) {
				Square square = item.getValue();
				if(!square.isSafe) {
					square.isPit = true;
				}
			}
		}
		else if(breeze == true && stench == true) {
			// if the agent senses both breeze and stench
			// check the squares around to see if there exists
			// both Pit and Wumpus, if not, mark the squares
			for(Map.Entry<String, Square> item: squares.entrySet()) {
				Square square = item.getValue();
				if(!square.isSafe && !square.isWumpus && !square.isPit && !square.isWall) {
//					if(rand.nextBoolean()) {
//						square.isPit = true;
//					}
//					else {
//						square.isWumpus = true;
//					}
					square.isPit = true;
				}
			}
		}
		// if there is a scream, mark all squares labeled as Wumpus as safe
		if(scream == true) {
			for(int i = this.worldStateSize - 1;i >= 0; i--) {
				for(int j = 0;j < this.worldStateSize; j++) {
					if(state[i][j].isWumpus == true) {
						state[i][j].isWumpus = false;
						state[i][j].isSafe = true;
					}
				}
			}
		}
		else {
			// there is no scream and the last action was SHOOT
			// there is no Wumpus infront of the agent, so clear
			// the square in front of the agent
			if(action == Action.SHOOT && !alreadyShot) {
				this.alreadyShot = true;
				squares.get("front").isSafe = true;
				squares.get("front").isWumpus = false;
			}
		}
	}
	
	// get agent's location, aka, the square the agent is in
	private Square getAgentLocation() {
		Square agentLoc = null;
		for(int i = 0;i < this.worldStateSize; i++) {
			for(int j = 0;j < this.worldStateSize; j++) {
				if(state[i][j].hasAgent != ' ') {
					agentLoc = state[i][j];
				}
			}
		}
		return agentLoc;
	}
	
	// get the square in front of the agent
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
		if(curLoc.hasAgent == 'A') {
			squares.put("front", topSquare);
			squares.put("left", leftSquare);
			squares.put("back", bottomSquare);
			squares.put("right", rightSquare);
		}
		else if(curLoc.hasAgent == '<') {
			squares.put("front", leftSquare);
			squares.put("left", bottomSquare);
			squares.put("back", rightSquare);
			squares.put("right", topSquare);
		}
		else if(curLoc.hasAgent == 'v') {
			squares.put("front", bottomSquare);
			squares.put("left", rightSquare);
			squares.put("back", topSquare);
			squares.put("right", leftSquare);
		}
		else if(curLoc.hasAgent == '>') {
			squares.put("front", rightSquare);
			squares.put("left", topSquare);
			squares.put("back", leftSquare);
			squares.put("right", bottomSquare);
		}
		
		return squares;
	}
	
	// get the current orientation of the agent based on
	// previous orientation and action taken
	public char getOrientation(char prevOrientation, int action) {
		String orientations = "<A>v";
		// find the index of previous orientation in the list
		// of orientations
		int index = orientations.indexOf(prevOrientation);
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
	
	
	// print the state of the world
	private void printState() {
		for(int i = this.worldStateSize - 1;i >= 0; i--) {
			for(int j = 0;j < this.worldStateSize; j++) {
				Square temp = state[j][i];
				if(temp.isPit)
					System.out.print("[P]");
				else if(temp.isWall)
						System.out.print("[X]");
				else if(temp.isWumpus)
					System.out.print("[W]");
				else
					System.out.print("[" + temp.hasAgent + "]");
			}
			System.out.println("");
		}
		System.out.println("");
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
		lastAction = this.actionRules();
		return lastAction;
	}
	
	// return an action based on the state of the world
	private int actionRules() {
		int action = Action.NO_OP;
		
		// get the squares around the agent
		HashMap<String, Square> squares = this.getAroundSquares();
		Square frontSquare = squares.get("front");
		Square leftSquare = squares.get("left");
		Square rightSquare = squares.get("right");
		Square backSquare = squares.get("back");
		
		if(!frontSquare.isWall && !frontSquare.isPit
			&& !frontSquare.isWumpus) {
			// there is no obstacle in front and the square 
			// is not marked as safe(not visited)
			if(!frontSquare.isSafe) {
				action = Action.GO_FORWARD;
			}
			else if((leftSquare.isWall || leftSquare.isPit || leftSquare.isWumpus)
					&& (rightSquare.isWall || rightSquare.isPit || rightSquare.isWumpus)
					&& (backSquare.isWall || backSquare.isPit || backSquare.isWumpus)){
				// if there are obstacles in left, right and back, go forward
				action = Action.GO_FORWARD;
			}
			// if the front square is marked as safe
			// 40% go forward, 30% turn left, 30% turn right
			else {
				if(rand.nextInt(10) < 4) {
					action = Action.GO_FORWARD;
				}
				else {
					action = rand.nextBoolean() ? Action.TURN_LEFT : Action.TURN_RIGHT;
				}
			}
		}
		else {
			// if there is a square in front that is marked as Wumpus
			// and the agent has not shot, the agent will shoot
			if(frontSquare.isWumpus && this.alreadyShot == false) {
				return action = Action.SHOOT;
			}
			
			if(!leftSquare.isWall && !leftSquare.isPit
					&& !leftSquare.isWumpus) {
				// there is an obstacle in front
				// there is no obstacle on the left, so turn left
				action = Action.TURN_LEFT;
			}
			else if(!rightSquare.isWall && !rightSquare.isPit
					&& !rightSquare.isWumpus) {
				// there are obstacles in front and left
				// there is no obstacle on the right, so turn right
				action = Action.TURN_RIGHT;
			}
			else if(backSquare.isWall || backSquare.isPit
					|| backSquare.isWumpus){
				// there are obstacles in front, left, right, back
				// do nothing
				action = Action.NO_OP;
			}
			else {
				// turn left or right otherwise
				action = rand.nextBoolean() ? Action.TURN_LEFT : Action.TURN_RIGHT;
			}
		}
		
		return action;
	}
	
	// public method to return the agent's name
	// do not remove this method
	public String getAgentName() {
		return agentName;
	}
	
	// Square class represents one square in Wumpus environment
	class Square {
		boolean isPit;
		boolean isWumpus;
		boolean isWall;
		boolean isSafe;
		char hasAgent; // save the orientation of the agent('<','^,'>','v')
		int x, y; // coordinates of the square
		
		Square(){
			this.isPit = false;
			this.isWumpus = false;
			this.isWall = false;
			this.isSafe = false;
			this.hasAgent = ' ';
			x = y = -1;
		}
	}
}