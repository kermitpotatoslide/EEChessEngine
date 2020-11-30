package com.alonsoruibal.chess.swing;

import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.book.FileBook;
import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.pgn.PgnImportExport;
import com.alonsoruibal.chess.search.SearchEngineThreaded;
import com.alonsoruibal.chess.search.SearchObserver;
import com.alonsoruibal.chess.search.SearchParameters;
import com.alonsoruibal.chess.search.SearchStatusInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 
 * @author Alberto Alonso Ruibal
 */
public class ChessApp extends Panel implements SearchObserver, ActionListener {
	private static final Logger logger = Logger.getLogger("ChessApplet");

	private static final long serialVersionUID = 5653881094129134036L;
	
	private boolean userToMove;
	
	private SearchEngineThreaded engine;
	private BoardJPanel boardPanel;
	private SearchParameters searchParameters;
	private JComboBox comboOpponent;
	private JComboBox comboTime;
	private JComboBox comboElo;
	private JComboBox comboPieces;
	private JComboBox comboBoards;
	private JTextField fenField;
	private final String[] opponentString = {"Computer Whites", "Computer Blacks", "Human vs Human", "Computer vs Computer"};
	private final String[] timeString = {"1 second", "2 seconds", "5 seconds", "15 seconds", "30 seconds", "60 seconds"};
	private final int[] timeValues = {1000, 2000, 5000, 15000, 30000, 60000};
	private final String[] eloString = {"ELO 1000", "ELO 1100", "ELO 1200", "ELO 1300", "ELO 1400", "ELO 1500", "ELO 1600", "ELO 1700", "ELO 1800", "ELO 1900", "ELO 2000", "ELO 2100"};
	private final int[] eloValues = {1000, 1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2100};
	private final String[] piecesString = {"Berlin", "Jumbo", "Leipzip", "Merida", "Staunton"};
	private final String[] piecesValues = {"/berlin.png", "/jumbo.png", "/leipzig.png", "/merida.png", "/staunton.png"};
	private final String[] boardsString = {"Blue", "Brown", "Gray", "Marble", "Wood"};
	private final String[] boardsValues = {"/blue.png", "/brown.png", "/gray.png", "/marble.png", "/wood.png"};
	private JLabel message;
	private PgnDialog pgnDialog;

	private boolean flip = false;

	private void init() {
		Config config = new Config();
		config.setTranspositionTableSize(8); // Due to memory limits, TT is set to 8 MB
		engine = new SearchEngineThreaded(config);
		engine.getConfig().setBook(new FileBook("/book_small.bin"));
		searchParameters = new SearchParameters();
		int timeDefaultIndex = 0;
		searchParameters.setMoveTime(timeValues[timeDefaultIndex]);
		engine.setObserver(this);
		
		pgnDialog = new PgnDialog(null);
		
		JButton button;
		JPanel control = new JPanel();
		control.setLayout(new GridLayout(17,1));
		
		JLabel labelGame = new JLabel("Game");
		control.add(labelGame);
		
		button = new JButton("New Game");
		button.setActionCommand("restart");
		button.addActionListener(this);
		control.add(button);
		
		button = new JButton("Undo Move");
		button.setActionCommand("back");
		button.addActionListener(this);
		control.add(button);

		button = new JButton("Go");
		button.setActionCommand("go");
		button.addActionListener(this);
		control.add(button);
		
		JLabel labelEngine = new JLabel("Engine");
		control.add(labelEngine);

		comboOpponent = new JComboBox<>(opponentString);
		comboOpponent.setActionCommand("opponent");
		comboOpponent.addActionListener(this);
		control.add(comboOpponent);
		
		comboTime = new JComboBox<>(timeString);
		comboTime.setActionCommand("time");
		comboTime.addActionListener(this);
		control.add(comboTime);

		comboElo = new JComboBox<>(eloString);
		comboElo.setActionCommand("elo");
		comboElo.addActionListener(this);
		control.add(comboElo);
		
		JLabel labelAppearance = new JLabel("Appearance");
		control.add(labelAppearance);

		comboPieces = new JComboBox<>(piecesString);
		comboPieces.setActionCommand("pieces");
		comboPieces.addActionListener(this);
		control.add(comboPieces);

		comboBoards = new JComboBox<>(boardsString);
		comboBoards.setActionCommand("boards");
		comboBoards.addActionListener(this);
		control.add(comboBoards);

		button = new JButton("Flip Board");
		button.setActionCommand("flip");
		button.addActionListener(this);
		control.add(button);
		
		JLabel labelFen = new JLabel("FEN/PGN");
		control.add(labelFen);
		
		fenField = new JTextField();
		fenField.setColumns(15);
		control.add(fenField);
		button = new JButton("Set FEN");
		button.setActionCommand("fen");
		button.addActionListener(this);
		control.add(button);
		
		button = new JButton("View PGN");
		button.setActionCommand("pgn");
		button.addActionListener(this);
		control.add(button);
		
		message = new JLabel();
		control.add(message);
				
		boardPanel = new BoardJPanel(this);
		JPanel global = new JPanel();
		global.setBackground(Color.LIGHT_GRAY);
		global.setLayout(new BorderLayout());
		
		JPanel control2 = new JPanel();
		control2.setLayout(new FlowLayout());
		control2.add(control);
		global.add("East", control2);
		global.add("Center", boardPanel);

		add(global);

		int opponentDefaultIndex = 1;
		comboOpponent.setSelectedIndex(opponentDefaultIndex);
		comboTime.setSelectedIndex(timeDefaultIndex);
		int eloDefaultIndex = 11;
		comboElo.setSelectedIndex(eloDefaultIndex);
		int piecesDefaultIndex = 3;
		comboPieces.setSelectedIndex(piecesDefaultIndex);
		int boardsDefaultIndex = 0;
		comboBoards.setSelectedIndex(boardsDefaultIndex);
	}
	
	public void start() {
		userToMove=true;
	}

	public void stop() {
		logger.debug("Stop!");
		engine.stop();
		userToMove=false;
	}
	
	public void destroy() {
		logger.debug("Destroy!");
		if (engine!= null) {
			engine.destroy();
			engine = null;
		}
		System.gc();
	}

	public void userMove(int fromIndex, int toIndex) {
		if (!userToMove) return;
		boardPanel.unhighlight();
		int move = Move.getFromString(engine.getBoard(), BitboardUtils.index2Algebraic(fromIndex) + BitboardUtils.index2Algebraic(toIndex), true);
		// Verify legality and play
		if (engine.getBoard().getLegalMove(move) != Move.NONE) {
			engine.getBoard().doMove(move);
			update(true);
			checkUserToMove();
		} else {
			logger.debug("move not legal");
			update(false);
		}
    }
	
	private void checkUserToMove() {
		userToMove = false;
		
		switch(comboOpponent.getSelectedIndex()) {
		case 0:
			if (!engine.getBoard().getTurn()) userToMove = true;
			break;
		case 1:
			if (engine.getBoard().getTurn()) userToMove = true;
			break;
		case 2:
			userToMove = true;
			break;
		case 3:
			break;
		}
		boardPanel.setAcceptInput(userToMove);
		update(!userToMove);

		if (!userToMove && (engine.getBoard().isEndGame() == 0)) engine.go(searchParameters);
		System.out.println("checkUserToMove... userToMove="+userToMove);

	}

	public void bestMove(int bestMove, int ponder) {
		System.out.println("bestMove... userToMove="+userToMove);
		if (userToMove) return;
		boardPanel.unhighlight();
		boardPanel.highlight(Move.getFromIndex(bestMove), Move.getToIndex(bestMove));
		engine.getBoard().doMove(bestMove);
		checkUserToMove();
	}
	
	private void update(boolean thinking) {
		boardPanel.setFen(engine.getBoard().getFen(), flip, false);
		fenField.setText(engine.getBoard().getFen());
//		List<Move> moves = legalMoveGenerator.generateMoves(engine.getBoard());
//		if (moves.size() == 0) {
//			System.out.println("End Game");
//		}
		switch (engine.getBoard().isEndGame()) {
		case 1 :
			message.setText("White win");
			break;
		case -1:
			message.setText("Black win");
			break;
		case 99:
			message.setText("Draw");
			break;
		default:
			if (engine.getBoard().getMoveNumber() == 0) message.setText("http://www.mobialia.com");
			else if (engine.getBoard().getTurn()) message.setText("White move" + (thinking ? " - Thinking..." : ""));
			else message.setText("Black move" + (thinking ? " - Thinking..." : ""));
		}
		invalidate();
		validate();
		repaint();
	}

	public void info(SearchStatusInfo info) {
		
	}

	public void actionPerformed(ActionEvent oAE) {
		if ("restart".equals(oAE.getActionCommand())) {
			logger.debug("restart!!!!");
			boardPanel.unhighlight();
			userToMove = true; 
			engine.stop();
			engine.getBoard().startPosition();
			checkUserToMove();
		} else if ("back".equals(oAE.getActionCommand())) {
			boardPanel.unhighlight();
			userToMove = true; 
			engine.stop();
			logger.debug("undoing move");
			engine.getBoard().undoMove();
			update(false);
		} else if ("pgn".equals(oAE.getActionCommand())) {
			if (!engine.isSearching()) {
				int o = comboOpponent.getSelectedIndex();
				String whiteName = (o == 0 || o == 3 ? "Computer" : "Player");
				String blackName = (o == 1 || o == 3 ? "Computer" : "Player");
				pgnDialog.setText(PgnImportExport.getPgn(engine.getBoard(), whiteName, blackName));
				pgnDialog.setVisible(true);
			}
		} else if ("fen".equals(oAE.getActionCommand())) {
			boardPanel.unhighlight();
			userToMove = true; 
			engine.stop();
			engine.getBoard().setFen(fenField.getText());
			update(false);
		} else if ("go".equals(oAE.getActionCommand())) {
			if (!engine.isSearching()) checkUserToMove();
		} else if ("opponent".equals(oAE.getActionCommand())) {
			if (!engine.isSearching()) checkUserToMove();
		} else if ("time".equals(oAE.getActionCommand())) {
			searchParameters.setMoveTime(timeValues[comboTime.getSelectedIndex()]);
		} else if ("elo".equals(oAE.getActionCommand())) {
			int engineElo = eloValues[comboElo.getSelectedIndex()];
			logger.debug("Setting elo " + engineElo);
			engine.getConfig().setLimitStrength(true);
			engine.getConfig().setElo(engineElo);
		} else if ("flip".equals(oAE.getActionCommand())) {
			flip = !flip;
			boardPanel.unhighlight();
			boardPanel.setFen(boardPanel.getLastFen(), flip, true);
		} else if ("pieces".equals(oAE.getActionCommand())) {
			PieceJLabel.style = piecesValues[comboPieces.getSelectedIndex()];
			if (boardPanel != null) {
				boardPanel.setFen(boardPanel.getLastFen(), flip, true);
			}
		} else if ("boards".equals(oAE.getActionCommand())) {
			SquareJPanel.loadImages(this.getClass().getResource(boardsValues[comboBoards.getSelectedIndex()]));
			if (boardPanel != null) {
				boardPanel.setFen(boardPanel.getLastFen(), flip, true);
			}
		}
	}

	public static void main(String[] args) {
		Frame frame = new Frame("Carballo Chess Engine");
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			}
		});

		ChessApp chessApp = new ChessApp();
		chessApp.setSize(800,610);
		frame.add(chessApp);
		frame.pack();
		chessApp.init();
		frame.setSize(800,610 + 50);
		frame.setVisible(true);
	}
}