package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static chess.ChessPiece.PieceType.KING;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    public ChessGame() {

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        } else {
            TeamColor pieceColor = piece.getTeamColor();
            Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
            Iterator<ChessMove> iterator = moves.iterator();
            while (iterator.hasNext()) {
                ChessMove move = iterator.next();
                ChessBoard newBoard = getBoard();
                newBoard.addPiece(move.getStartPosition(), null);
                newBoard.addPiece(move.getEndPosition(), piece);
                if (isInCheck(pieceColor)) {
                    iterator.remove();
                }
            }
            return moves;
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessBoard board = getBoard();
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece piece = board.getPiece(startPosition);
        if (validMoves(startPosition).contains(move)) {
            board.addPiece(startPosition, null);
            board.addPiece(endPosition, piece);
        } else {
            throw new InvalidMoveException();
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {

    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        throw new RuntimeException("Not implemented");
    }
}
