package model;

import model.Board;

/**
 * 
 * Thread of the board
 * 
 * @author Thomas Muehlematter
 * 
 */
public class BoardThread extends Thread {

	Board board;

	public BoardThread(Board b) {
		this.board = b;
	}

	public void run() {
		board.setGo(true);
		board.simulate();
	}
}
