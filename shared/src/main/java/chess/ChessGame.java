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
    private final Set<ChessPosition> movePositionCollection = new HashSet<>();

    public ChessGame() {
        currentBoard.resetBoard();
        movePositionCollection.clear();
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
     * Set's the most recent move made in the game
     *
      * @param move the most recent move made in the game
     */
    public void setLastMove(ChessMove move) {
        lastMove = move;
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
        return currentTurn == chessGame.currentTurn
                && Objects.equals(currentBoard, chessGame.currentBoard)
                && Objects.equals(lastMove, chessGame.lastMove)
                && Objects.equals(lastMovePiece, chessGame.lastMovePiece)
                && Objects.equals(movePositionCollection, chessGame.movePositionCollection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTurn, currentBoard, lastMove, lastMovePiece, movePositionCollection);
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
            if (isEnPassantValid(startPosition)) {
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
            if (piece.getPieceType() == KING) {
                if (isShortCastleValid()) {
                    ChessPosition endPosition;
                    if (piece.getTeamColor() == TeamColor.WHITE) {
                        endPosition = new ChessPosition(1, 7);
                    } else {
                        endPosition = new ChessPosition(8,7);
                    }
                    ChessMove newMove = new ChessMove(startPosition, endPosition, null);
                    valid.add(newMove);
                }
                if (isLongCastleValid()) {
                    ChessPosition endPosition;
                    if (piece.getTeamColor() == TeamColor.WHITE) {
                        endPosition = new ChessPosition(1, 3);
                    } else {
                        endPosition = new ChessPosition(8,3);
                    }
                    ChessMove newMove = new ChessMove(startPosition, endPosition, null);
                    valid.add(newMove);
                }
            }
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
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece piece = currentBoard.getPiece(startPosition);
        Collection<ChessMove> moves = validMoves(startPosition);
        if (moves == null || !moves.contains(move) || piece.getTeamColor() != currentTurn) {
            throw new InvalidMoveException();
        } else if (piece.getPieceType() == KING) {
            makeKingMove(startPosition, endPosition, piece);
        } else {
            ChessPiece newPiece;
            if (move.getPromotionPiece() == null) {
                newPiece = piece;
            } else {
                newPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            }
            if (piece.getPieceType() == PAWN) {
                makePawnMove(startPosition, piece, endPosition);
            }
            currentBoard.addPiece(startPosition, null);
            currentBoard.addPiece(endPosition, newPiece);
        }
        if (piece.getTeamColor() == TeamColor.WHITE) {
            setTeamTurn(TeamColor.BLACK);
        } else {
            setTeamTurn(TeamColor.WHITE);
        }
        setLastMove(move);
        setLastMovePiece(piece);
        movePositionCollection.add(startPosition);
    }

    private void makePawnMove(ChessPosition startPosition, ChessPiece piece, ChessPosition endPosition) {
        if (isEnPassantValid(startPosition)) {
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

    private void makeKingMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece piece) {
        if (currentTurn == TeamColor.WHITE) {
            if (startPosition.equals(new ChessPosition(1,5))) {
                if (endPosition.equals(new ChessPosition(1,7))) {
                    ChessPiece rook = currentBoard.getPiece(new ChessPosition(1, 8));
                    currentBoard.addPiece(new ChessPosition(1, 8), null);
                    currentBoard.addPiece(new ChessPosition(1, 7), piece);
                    currentBoard.addPiece(new ChessPosition(1, 6), rook);
                    currentBoard.addPiece(new ChessPosition(1, 5), null);
                } else if (endPosition.equals(new ChessPosition(1,3))) {
                    ChessPiece rook = currentBoard.getPiece(new ChessPosition(1,1));
                    currentBoard.addPiece(new ChessPosition(1, 1), null);
                    currentBoard.addPiece(new ChessPosition(1, 3), piece);
                    currentBoard.addPiece(new ChessPosition(1, 4), rook);
                    currentBoard.addPiece(new ChessPosition(1, 5), null);
                } else {
                    currentBoard.addPiece(startPosition,null);
                    currentBoard.addPiece(endPosition, piece);
                }
            } else {
                currentBoard.addPiece(startPosition,null);
                currentBoard.addPiece(endPosition, piece);
            }
        } else {
            if (startPosition.equals(new ChessPosition(8,5))) {
                if (endPosition.equals(new ChessPosition(8, 7))) {
                    ChessPiece rook = currentBoard.getPiece(new ChessPosition(8, 8));
                    currentBoard.addPiece(new ChessPosition(8, 8), null);
                    currentBoard.addPiece(new ChessPosition(8, 7), piece);
                    currentBoard.addPiece(new ChessPosition(8, 6), rook);
                    currentBoard.addPiece(new ChessPosition(8, 5), null);
                } else if (endPosition.equals(new ChessPosition(8, 3))) {
                    ChessPiece rook = currentBoard.getPiece(new ChessPosition(8, 1));
                    currentBoard.addPiece(new ChessPosition(8, 1), null);
                    currentBoard.addPiece(new ChessPosition(8, 3), piece);
                    currentBoard.addPiece(new ChessPosition(8, 4), rook);
                    currentBoard.addPiece(new ChessPosition(8, 5), null);
                } else {
                    currentBoard.addPiece(startPosition,null);
                    currentBoard.addPiece(endPosition, piece);
                }
            } else {
                currentBoard.addPiece(startPosition,null);
                currentBoard.addPiece(endPosition, piece);
            }
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
        movePositionCollection.clear();
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return currentBoard;
    }

    public boolean isEnPassantValid(ChessPosition startPosition) {
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

    public boolean isShortCastleValid() {
        boolean castle = false;
        ChessPosition whiteKingPosition = new ChessPosition(1,5);
        ChessPiece whiteKing = currentBoard.getPiece(whiteKingPosition);
        if (whiteKing != null) {
            if (whiteKing.getTeamColor() == TeamColor.WHITE) {
                ChessPosition rookPosition = new ChessPosition(1, 8);
                ChessPiece rook = currentBoard.getPiece(rookPosition);
                if (rook != null) {
                    if (whiteKing.getPieceType() == KING && whiteKingHasNotMoved()) {
                        if (rook.getPieceType() == ROOK && whiteKingSideRookHasNotMoved()) {
                            ChessPosition a6 = new ChessPosition(1, 6);
                            ChessPosition a7 = new ChessPosition(1, 7);
                            ChessPiece a6Piece = currentBoard.getPiece(a6);
                            ChessPiece a7Piece = currentBoard.getPiece(a7);
                            if (a6Piece == null && a7Piece == null) {
                                if (!isInCheck(TeamColor.WHITE)) {
                                    currentBoard.addPiece(whiteKingPosition, null);
                                    currentBoard.addPiece(a6, whiteKing);
                                    if (!isInCheck(TeamColor.WHITE)) {
                                        currentBoard.addPiece(a6, null);
                                        currentBoard.addPiece(a7, whiteKing);
                                        if (!isInCheck(TeamColor.WHITE)) {
                                            castle = true;
                                        }
                                    }
                                    currentBoard.addPiece(a6, null);
                                    currentBoard.addPiece(a7, null);
                                    currentBoard.addPiece(whiteKingPosition, whiteKing);
                                }
                            }
                        }
                    }
                }
            }
        }
        ChessPosition blackKingPosition = new ChessPosition(8, 5);
        ChessPiece blackKing = currentBoard.getPiece(blackKingPosition);
        if (blackKing != null) {
             if (blackKing.getTeamColor() == TeamColor.BLACK) {
                 ChessPosition rookPosition = new ChessPosition(8, 8);
                 ChessPiece rook = currentBoard.getPiece(rookPosition);
                 if (rook != null) {
                    if (blackKing.getPieceType() == KING && blackKingHasNotMoved()) {
                        if (rook.getPieceType() == ROOK && blackKingSideRookHasNotMoved()) {
                            ChessPosition h6 = new ChessPosition(8, 6);
                            ChessPosition h7 = new ChessPosition(8, 7);
                            ChessPiece h6Piece = currentBoard.getPiece(h6);
                            ChessPiece h7Piece = currentBoard.getPiece(h7);
                            if (h6Piece == null && h7Piece == null) {
                                if (!isInCheck(TeamColor.BLACK)) {
                                    currentBoard.addPiece(blackKingPosition, null);
                                    currentBoard.addPiece(h6, blackKing);
                                    if (!isInCheck(TeamColor.BLACK)) {
                                        currentBoard.addPiece(h6, null);
                                        currentBoard.addPiece(h7, blackKing);
                                        if (!isInCheck(TeamColor.BLACK)) {
                                            castle = true;
                                        }
                                    }
                                    currentBoard.addPiece(h6, null);
                                    currentBoard.addPiece(h7, null);
                                    currentBoard.addPiece(blackKingPosition, blackKing);
                                }
                            }
                        }
                    }
                }
            }
        }
        return castle;
    }

    public boolean isLongCastleValid() {
        boolean castle = false;
        ChessPosition whiteKingPosition = new ChessPosition(1,5);
        ChessPiece whiteKing = currentBoard.getPiece(whiteKingPosition);
        if (whiteKing != null) {
            if (currentTurn == TeamColor.WHITE) {
                ChessPosition rookPosition = new ChessPosition(1, 1);
                ChessPiece rook = currentBoard.getPiece(rookPosition);
                if (rook != null) {
                    if (whiteKing.getPieceType() == KING && whiteKingHasNotMoved()) {
                        if (rook.getPieceType() == ROOK && whiteQueenSideRookHasNotMoved()) {
                            ChessPosition a2 = new ChessPosition(1, 2);
                            ChessPosition a3 = new ChessPosition(1, 3);
                            ChessPosition a4 = new ChessPosition(1, 4);
                            ChessPiece a2Piece = currentBoard.getPiece(a2);
                            ChessPiece a3Piece = currentBoard.getPiece(a3);
                            ChessPiece a4Piece = currentBoard.getPiece(a4);
                            if (a2Piece == null && a3Piece == null && a4Piece == null) {
                                if (!isInCheck(TeamColor.WHITE)) {
                                    currentBoard.addPiece(whiteKingPosition, null);
                                    currentBoard.addPiece(a4, whiteKing);
                                    if (!isInCheck(TeamColor.WHITE)) {
                                        currentBoard.addPiece(a4, null);
                                        currentBoard.addPiece(a3, whiteKing);
                                        if (!isInCheck(TeamColor.WHITE)) {
                                            castle = true;
                                        }
                                    }
                                    currentBoard.addPiece(a4, null);
                                    currentBoard.addPiece(a3, null);
                                    currentBoard.addPiece(whiteKingPosition, whiteKing);
                                }
                            }
                        }
                    }
                }
            }
        }
        ChessPosition blackKingPosition = new ChessPosition(8,5);
        ChessPiece blackKing = currentBoard.getPiece(blackKingPosition);
        if (blackKing != null) {
            if (blackKing.getTeamColor() == TeamColor.BLACK) {
                ChessPosition rookPosition = new ChessPosition(8, 8);
                ChessPiece rook = currentBoard.getPiece(rookPosition);
                if (rook != null) {
                    if (blackKing.getPieceType() == KING && blackKingHasNotMoved()) {
                        if (rook.getPieceType() == ROOK && blackQueenSideRookHasNotMoved()) {
                            ChessPosition h2 = new ChessPosition(8, 2);
                            ChessPosition h3 = new ChessPosition(8, 3);
                            ChessPosition h4 = new ChessPosition(8, 4);
                            ChessPiece h2Piece = currentBoard.getPiece(h2);
                            ChessPiece h3Piece = currentBoard.getPiece(h3);
                            ChessPiece h4Piece = currentBoard.getPiece(h4);
                            if (h2Piece == null && h3Piece == null && h4Piece == null) {
                                if (!isInCheck(TeamColor.BLACK)) {
                                    currentBoard.addPiece(whiteKingPosition, null);
                                    currentBoard.addPiece(h4, blackKing);
                                    if (!isInCheck(TeamColor.BLACK)) {
                                        currentBoard.addPiece(h4, null);
                                        currentBoard.addPiece(h3, blackKing);
                                        if (!isInCheck(TeamColor.BLACK)) {
                                            castle = true;
                                        }
                                    }
                                    currentBoard.addPiece(h4, null);
                                    currentBoard.addPiece(h3, null);
                                    currentBoard.addPiece(blackKingPosition, blackKing);
                                }
                            }
                        }
                    }
                }
            }
        }
        return castle;
    }

    public boolean whiteKingHasNotMoved() {
        return !movePositionCollection.contains(new ChessPosition(1,5));
    }

    public boolean blackKingHasNotMoved() {
        return !movePositionCollection.contains(new ChessPosition(8,5));
    }

    public boolean whiteKingSideRookHasNotMoved() {
        return !movePositionCollection.contains(new ChessPosition(1,8));
    }

    public boolean whiteQueenSideRookHasNotMoved() {
        return !movePositionCollection.contains(new ChessPosition(1,1));
    }

    public boolean blackKingSideRookHasNotMoved() {
        return !movePositionCollection.contains(new ChessPosition(8,8));
    }

    public boolean blackQueenSideRookHasNotMoved() {
        return !movePositionCollection.contains(new ChessPosition(8,1));
    }
}