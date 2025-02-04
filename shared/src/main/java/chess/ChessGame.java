package chess;

import java.util.*;

import static chess.ChessPiece.PieceType.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTurn = TeamColor.WHITE;
    private ChessBoard currentBoard = new ChessBoard();
    private ChessMove lastMove = new ChessMove(null, null, null);
    private ChessPiece lastMovePiece = new ChessPiece(null, null);

    public ChessGame() {
        currentBoard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    /**
     * @return The most recent move made in the game
     */
    public ChessMove getLastMove() {
        return lastMove;
    }

    /**
     * Set's the most recent move made in the game
     *
      * @param move the most recent move made in the game
     */
    public void setLastMove(ChessMove move) {
        lastMove = move;
    }

    /**
     * @return The piece that made the most recent move
     */
    public ChessPiece getLastMovePiece() {
        return lastMovePiece;
    }

    /**
     * Set's the piece that made the most recent move
     *
     * @param piece The piece that made the most recent move
     */
    public void setLastMovePiece(ChessPiece piece) {
        lastMovePiece = piece;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTurn == chessGame.currentTurn && Objects.equals(currentBoard, chessGame.currentBoard) && Objects.equals(lastMove, chessGame.lastMove);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTurn, currentBoard, lastMove);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a collection of valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = currentBoard.getPiece(startPosition);
        if (piece == null) {
            return null;
        } else {
            TeamColor pieceColor = piece.getTeamColor();
            Collection<ChessMove> moves = piece.pieceMoves(currentBoard, startPosition);
            if (isEnPassant(startPosition)) {
                ChessPosition endPosition;
                if (piece.getTeamColor() == TeamColor.WHITE) {
                    endPosition = new ChessPosition(6, lastMove.getEndPosition().getColumn());
                } else {
                    endPosition = new ChessPosition(3, lastMove.getEndPosition().getColumn());
                }
                ChessMove newMove = new ChessMove(startPosition, endPosition, null);
                moves.add(newMove);
            }
            Collection<ChessMove> valid = new ArrayList<>();
            currentBoard.addPiece(startPosition,null);
            for (ChessMove move: moves) {
                ChessPiece oldPiece = currentBoard.getPiece(move.getEndPosition());
                currentBoard.addPiece(move.getEndPosition(), piece);
                if (!isInCheck(pieceColor)) {
                    valid.add(move);
                }
                currentBoard.addPiece(move.getEndPosition(), oldPiece);
            }
            currentBoard.addPiece(startPosition, piece);
            return valid;
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPiece piece = currentBoard.getPiece(startPosition);
        if (validMoves(startPosition) == null) {
            throw new InvalidMoveException();
        } else if (!validMoves(startPosition).contains(move)) {
            throw new InvalidMoveException();
        } else if (piece.getTeamColor() != getTeamTurn()) {
            throw new InvalidMoveException();
        } else {
            ChessPosition endPosition = move.getEndPosition();
            ChessPiece newPiece;
            if (move.getPromotionPiece() == null) {
                newPiece = piece;
            } else {
                newPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            }
            if (piece.getPieceType() == PAWN) {
                if (isEnPassant(startPosition)) {
                    if (piece.getTeamColor() == TeamColor.WHITE) {
                        if (endPosition.getRow() == 6 && endPosition.getColumn() == lastMove.getEndPosition().getColumn()) {
                            currentBoard.addPiece(lastMove.getEndPosition(), null);
                        }
                    } else {
                        if (endPosition.getRow() == 3 && endPosition.getColumn() == lastMove.getEndPosition().getColumn()) {
                            currentBoard.addPiece(lastMove.getEndPosition(), null);
                        }
                    }
                }
            }
            currentBoard.addPiece(startPosition, null);
            currentBoard.addPiece(endPosition, newPiece);
            if (piece.getTeamColor() == TeamColor.WHITE) {
                setTeamTurn(TeamColor.BLACK);
            } else {
                setTeamTurn(TeamColor.WHITE);
            }
            setLastMove(move);
            setLastMovePiece(piece);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;
        Set<ChessPosition> testMovePositions = new HashSet<>();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition testPosition = new ChessPosition(i, j);
                ChessPiece testPiece = currentBoard.getPiece(testPosition);
                if (testPiece != null) {
                    if (testPiece.getTeamColor() == teamColor && testPiece.getPieceType() == KING) {
                        kingPosition = testPosition;
                    } else if (testPiece.getTeamColor() != teamColor && testPiece.getPieceType() != null) {
                        Collection<ChessMove> testMoves = testPiece.pieceMoves(currentBoard, testPosition);
                        for (ChessMove testMove : testMoves) {
                            testMovePositions.add(testMove.getEndPosition());
                        }
                    }
                }
            }
        }
        for (ChessPosition testPosition : testMovePositions) {
            if (testPosition.equals(kingPosition)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    ChessPosition testPosition = new ChessPosition(i,j);
                    ChessPiece testPiece = currentBoard.getPiece(testPosition);
                    if (testPiece != null && testPiece.getTeamColor() == teamColor) {
                        Collection<ChessMove> testMoves = validMoves(testPosition);
                        if (!testMoves.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (teamColor == getTeamTurn()) {
            if (!isInCheck(teamColor)) {
                Collection<ChessMove> kingMoves = new ArrayList<>();
                for (int i = 1; i <= 8; i++) {
                    for (int j = 1; j <= 8; j++) {
                        ChessPosition testPosition = new ChessPosition(i, j);
                        ChessPiece testPiece = currentBoard.getPiece(testPosition);
                        if (testPiece != null) {
                            if (testPiece.getTeamColor() == teamColor && testPiece.getPieceType() == KING) {
                                kingMoves = validMoves(testPosition);
                            } else if (testPiece.getTeamColor() == teamColor && testPiece.getPieceType() != KING) {
                                if (!validMoves(testPosition).isEmpty()) {
                                    return false;
                                }
                            }
                        }
                    }
                }
                return kingMoves.isEmpty();
            }
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        currentBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return currentBoard;
    }

    /**
     * Determines if en passant can be played on current move by piece at given starting position
     *
     * @param startPosition the starting position of the piece in question
     * @return True if en passant can be played on current move, otherwise false
     */
    public boolean isEnPassant(ChessPosition startPosition) {
        if (lastMovePiece.getPieceType() == PAWN) {
            ChessPiece piece = currentBoard.getPiece(startPosition);
            if (piece.getPieceType() == PAWN && piece.getTeamColor() == TeamColor.WHITE) {
                if (lastMove.getStartPosition().getRow() == 7 && lastMove.getEndPosition().getRow() == 5) {
                    if (startPosition.getRow() == 5) {
                        return Math.abs(lastMove.getEndPosition().getColumn() - startPosition.getColumn()) == 1;
                    }
                }
            } else if (piece.getPieceType() == PAWN && piece.getTeamColor() == TeamColor.BLACK) {
                if (lastMove.getStartPosition().getRow() == 2 && lastMove.getEndPosition().getRow() == 4) {
                    if (startPosition.getRow() == 4) {
                        return Math.abs(lastMove.getEndPosition().getColumn() - startPosition.getColumn()) == 1;
                    }
                }
            }
        }
        return false;
    }
}

