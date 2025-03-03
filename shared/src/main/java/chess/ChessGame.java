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

    public TeamColor getTeamTurn() {
        return currentTurn;
    }
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }
    public void setLastMove(ChessMove move) {
        lastMove = move;
    }
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

    public enum TeamColor {WHITE, BLACK}

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

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;
        Set<ChessPosition> testMovePositions = new HashSet<>();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition testPosition = new ChessPosition(i, j);
                ChessPiece testPiece = currentBoard.getPiece(testPosition);
                kingPosition = doesTestPieceCheckKing(teamColor, testPiece, kingPosition, testPosition, testMovePositions);
            }
        }
        for (ChessPosition testPosition : testMovePositions) {
            if (testPosition.equals(kingPosition)) {
                return true;
            }
        }
        return false;
    }

    private ChessPosition doesTestPieceCheckKing(TeamColor teamColor,
                                                 ChessPiece testPiece,
                                                 ChessPosition kingPosition,
                                                 ChessPosition testPosition,
                                                 Set<ChessPosition> testMovePositions) {
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
        return kingPosition;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    ChessPosition testPosition = new ChessPosition(i,j);
                    ChessPiece testPiece = currentBoard.getPiece(testPosition);
                    if (isInCheckmateHelper(teamColor, testPiece, testPosition)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean isInCheckmateHelper(TeamColor teamColor, ChessPiece testPiece, ChessPosition testPosition) {
        if (testPiece != null && testPiece.getTeamColor() == teamColor) {
            Collection<ChessMove> testMoves = validMoves(testPosition);
            return !testMoves.isEmpty();
        }
        return false;
    }

    public boolean isInStalemate(TeamColor teamColor) {
        if (teamColor == getTeamTurn()) {
            if (!isInCheck(teamColor)) {
                Collection<ChessMove> kingMoves = new ArrayList<>();
                for (int i = 1; i <= 8; i++) {
                    for (int j = 1; j <= 8; j++) {
                        ChessPosition testPosition = new ChessPosition(i, j);
                        ChessPiece testPiece = currentBoard.getPiece(testPosition);
                        kingMoves = isInStalemateHelper(testPiece, teamColor, testPosition, kingMoves);
                    }
                }
                return kingMoves.isEmpty();
            }
        }
        return false;
    }
    
    private Collection<ChessMove> isInStalemateHelper(ChessPiece testPiece,
                                                      TeamColor teamColor,
                                                      ChessPosition testPosition,
                                                      Collection<ChessMove> kingMoves) {
        if (testPiece != null) {
            if (testPiece.getTeamColor() == teamColor && testPiece.getPieceType() == KING) {
                kingMoves = validMoves(testPosition);
            } else if (testPiece.getTeamColor() == teamColor && testPiece.getPieceType() != KING) {
                if (!validMoves(testPosition).isEmpty()) {
                    kingMoves = validMoves(testPosition);
                }
            }
        }
        return kingMoves;
    }

    public void setBoard(ChessBoard board) {
        currentBoard = board;
        movePositionCollection.clear();
    }

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
                    castle = isShortCastleValidHelper(TeamColor.WHITE, whiteKing, rook, castle, whiteKingPosition);
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
                     castle = isShortCastleValidHelper(TeamColor.BLACK, blackKing, rook, castle, blackKingPosition);
                }
            }
        }
        return castle;
    }

    private boolean isShortCastleValidHelper(TeamColor teamColor, ChessPiece king, ChessPiece rook, boolean castle, ChessPosition kingPosition) {
        if (teamColor == TeamColor.WHITE) {
            if (king.getPieceType() == KING && whiteKingHasNotMoved()) {
                if (rook.getPieceType() == ROOK && whiteKingSideRookHasNotMoved()) {
                    castle = isShortCastleValidHelper2(teamColor, king, castle, kingPosition);
                }
            }
        } else {
            if (king.getPieceType() == KING && blackKingHasNotMoved()) {
                if (rook.getPieceType() == ROOK && blackKingSideRookHasNotMoved()) {
                    castle = isShortCastleValidHelper2(teamColor, king, castle, kingPosition);
                }
            }
        }
        return castle;
    }

    private boolean isShortCastleValidHelper2(TeamColor teamColor, ChessPiece king, boolean castle, ChessPosition kingPosition) {
        ChessPosition col6;
        ChessPosition col7;
        if (teamColor == TeamColor.WHITE) {
            col6 = new ChessPosition(1, 6);
            col7 = new ChessPosition(1, 7);
        } else {
            col6 = new ChessPosition(8, 6);
            col7 = new ChessPosition(8, 7);
        }
        ChessPiece col6Piece = currentBoard.getPiece(col6);
        ChessPiece col7Piece = currentBoard.getPiece(col7);
        if (col6Piece == null && col7Piece == null) {
            castle = isInCheckDuringCastle(teamColor, castle, kingPosition, king, col6, col7);
        }
        return castle;
    }

    private boolean isInCheckDuringCastle(TeamColor teamColor,
                                          boolean castle,
                                          ChessPosition kingPosition,
                                          ChessPiece king,
                                          ChessPosition movingKingPosition,
                                          ChessPosition finalKingPosition) {
        if (!isInCheck(teamColor)) {
            currentBoard.addPiece(kingPosition, null);
            currentBoard.addPiece(movingKingPosition, king);
            if (!isInCheck(teamColor)) {
                currentBoard.addPiece(movingKingPosition, null);
                currentBoard.addPiece(finalKingPosition, king);
                if (!isInCheck(teamColor)) {
                    castle = true;
                }
            }
            currentBoard.addPiece(movingKingPosition, null);
            currentBoard.addPiece(finalKingPosition, null);
            currentBoard.addPiece(kingPosition, king);
        }
        return castle;
    }

    public boolean isLongCastleValid() {
        boolean castle = false;
        ChessPosition whiteKingPosition = new ChessPosition(1,5);
        ChessPiece whiteKing = currentBoard.getPiece(whiteKingPosition);
        if (whiteKing != null) {
            if (whiteKing.getTeamColor() == TeamColor.WHITE) {
                ChessPosition rookPosition = new ChessPosition(1, 1);
                ChessPiece rook = currentBoard.getPiece(rookPosition);
                if (rook != null) {
                    castle = isLongCastleValidHelper(TeamColor.WHITE, whiteKing, rook, castle, whiteKingPosition);
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
                    castle = isLongCastleValidHelper(TeamColor.BLACK, blackKing, rook, castle, blackKingPosition);
                }
            }
        }
        return castle;
    }

    private boolean isLongCastleValidHelper(TeamColor teamColor, ChessPiece king, ChessPiece rook, boolean castle, ChessPosition kingPosition) {
        if (teamColor == TeamColor.WHITE) {
            if (king.getPieceType() == KING && whiteKingHasNotMoved()) {
                if (rook.getPieceType() == ROOK && whiteQueenSideRookHasNotMoved()) {
                    castle = isLongCastleValidHelper2(teamColor, king, castle, kingPosition);
                }
            }
        } else {
            if (king.getPieceType() == KING && blackKingHasNotMoved()) {
                if (rook.getPieceType() == ROOK && blackQueenSideRookHasNotMoved()) {
                    castle = isLongCastleValidHelper2(teamColor, king, castle, kingPosition);
                }
            } 
        }
        return castle;
    }

    private boolean isLongCastleValidHelper2(TeamColor teamColor, ChessPiece king, boolean castle, ChessPosition kingPosition) {
        ChessPosition col2;
        ChessPosition col3;
        ChessPosition col4;
        if (teamColor == TeamColor.WHITE) {
            col2 = new ChessPosition(1, 2);
            col3 = new ChessPosition(1, 3);
            col4 = new ChessPosition(1, 4);
        } else {
            col2 = new ChessPosition(8, 2);
            col3 = new ChessPosition(8, 3);
            col4 = new ChessPosition(8, 4);
        }
        ChessPiece col2Piece = currentBoard.getPiece(col2);
        ChessPiece col3Piece = currentBoard.getPiece(col3);
        ChessPiece col4Piece = currentBoard.getPiece(col4);
        if (col2Piece == null && col3Piece == null && col4Piece == null) {
            castle = isInCheckDuringCastle(teamColor, castle, kingPosition, king, col4, col3);
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