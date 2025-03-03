package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        PieceType piece = getPieceType();
        Collection<ChessMove> moves = new ArrayList<>();

        if (piece == PieceType.PAWN) {
            if (board.getPiece(myPosition).getTeamColor() == WHITE) {
                getWhitePawnMove(board, myPosition, moves);
            } else {
                getBlackPawnMove(board, myPosition, moves);
            }
        }

        else if (piece == PieceType.QUEEN) {
            getHorizontalAndVerticalPieceMoves(board, myPosition, moves);
            getDiagonalPieceMoves(board, myPosition, moves);
        }

        else if (piece == PieceType.KING) {
            getKingMoves(board, myPosition, moves);
        }

        else if (piece == PieceType.BISHOP) {
            getDiagonalPieceMoves(board, myPosition, moves);
        }

        else if (piece == PieceType.KNIGHT) {
            getKnightMoves(board, myPosition, moves);
        }
        
        else if (piece == PieceType.ROOK) {
            getHorizontalAndVerticalPieceMoves(board, myPosition, moves);
        }
        return moves;
    }

    private void getKnightMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        row++;
        row++;
        column++;
        if (row <= 8 && column <= 8) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
        row--;
        column++;
        if (row <= 8 && column <= 8) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
        row--;
        row--;
        if (row >= 1 && column <= 8) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
        row--;
        column--;
        if (row >= 1 && column <= 8) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
        column--;
        column--;
        if (row >= 1 && column >= 1) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
        row++;
        column--;
        if (row >= 1 && column >= 1) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
        row++;
        row++;
        if (row <= 8 && column >= 1) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
        row++;
        column++;
        if (row <= 8 && column >= 1) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
    }

    private void getKingMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        row++;
        if (row <= 8) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
        column++;
        if (row <= 8 && column <= 8) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
        row--;
        if (column <= 8) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
        row--;
        if (row >= 1 && column <= 8) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
        column--;
        if (row >= 1) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
        column--;
        if (row >= 1 && column >= 1) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
        row++;
        if (column >= 1) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
        row++;
        if (row <= 8 && column >= 1) {
            kingAndKnightOccupiedSpace(board, myPosition, moves, row, column);
        }
    }

    private void getBlackPawnMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        row--;
        ChessPosition newPosition = new ChessPosition(row, column);
        if (board.getPiece(newPosition) == null) {
            blackPawnMove(myPosition, moves, row, newPosition);
            if (row + 1 == 7) {
                ChessPosition newPositionForward = new ChessPosition(row-1, column);
                if (board.getPiece(newPositionForward) == null) {
                    ChessMove move = new ChessMove(myPosition, newPositionForward, null);
                    moves.add(move);
                }
            }
        }
        if (column < 8) {
            ChessPosition newPositionLeft = new ChessPosition(row, column + 1);
            if (board.getPiece(newPositionLeft) != null) {
                if (board.getPiece(newPositionLeft).getTeamColor() == WHITE) {
                    blackPawnMove(myPosition, moves, row, newPositionLeft);
                }
            }
        }
        if (column > 1) {
            ChessPosition newPositionRight = new ChessPosition(row, column-1);
            if (board.getPiece(newPositionRight) != null) {
                if (board.getPiece(newPositionRight).getTeamColor() == WHITE) {
                    blackPawnMove(myPosition, moves, row, newPositionRight);
                }
            }
        }
    }

    private void getWhitePawnMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        row++;
        ChessPosition newPosition = new ChessPosition(row, column);
        if (board.getPiece(newPosition) == null) {
            whitePawnMove(myPosition, moves, row, newPosition);
            if (row - 1 == 2) {
                ChessPosition newPositionForward = new ChessPosition(row+1, column);
                if (board.getPiece(newPositionForward) == null) {
                    ChessMove move = new ChessMove(myPosition, newPositionForward, null);
                    moves.add(move);
                }
            }
        }
        if (column > 1) {
            ChessPosition newPositionLeft = new ChessPosition(row, column - 1);
            if (board.getPiece(newPositionLeft) != null) {
                if (board.getPiece(newPositionLeft).getTeamColor() == BLACK) {
                    whitePawnMove(myPosition, moves, row, newPositionLeft);
                }
            }
        }
        if (column < 8) {
            ChessPosition newPositionRight = new ChessPosition(row, column + 1);
            if (board.getPiece(newPositionRight) != null) {
                if (board.getPiece(newPositionRight).getTeamColor() == BLACK) {
                    whitePawnMove(myPosition, moves, row, newPositionRight);
                }
            }
        }
    }

    private boolean positionEmptyOrOccupiedByOpponentProcedure(ChessBoard board, 
                                                               ChessPosition newPosition, 
                                                               ChessPosition myPosition, 
                                                               Collection<ChessMove> moves) {
        if (board.getPiece(newPosition) != null) {
            if (board.getPiece(newPosition).getTeamColor() != pieceColor) {
                ChessMove move = new ChessMove(myPosition,newPosition,null);
                moves.add(move);
            }
            return true;
        } else {
            ChessMove move = new ChessMove(myPosition,newPosition,null);
            moves.add(move);
        }
        return false;
    }

    private void getHorizontalAndVerticalPieceMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        int counter1 = 0;
        int path1 = myPosition.getRow();
        ChessPosition newPosition;
        while (counter1 < 2) {
            while (path1 < 8) {
                path1++;
                if (counter1 == 0) {
                    newPosition = new ChessPosition(path1, column);
                } else {
                    newPosition = new ChessPosition(row, path1);
                }
                if (positionEmptyOrOccupiedByOpponentProcedure(board, newPosition, myPosition, moves)) {
                    break;
                }
            }
            counter1++;
            path1 = myPosition.getColumn();
        }
        int counter2 = 0;
        int path2 = myPosition.getRow();
        while (counter2 < 2) {
            while (path2 > 1) {
                path2--;
                if (counter2 == 0) {
                    newPosition = new ChessPosition(path2, column);
                } else {
                    newPosition = new ChessPosition(row, path2);
                }
                if (positionEmptyOrOccupiedByOpponentProcedure(board, newPosition, myPosition, moves)) {
                    break;
                }
            }
            counter2++;
            path2 = myPosition.getColumn();
        }
    }

    private void getDiagonalPieceMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int row1 = myPosition.getRow();
        int column1 = myPosition.getColumn();
        while (row1 < 8 && column1 < 8) {
            row1++;
            column1++;
            ChessPosition newPosition = new ChessPosition(row1, column1);
            if (positionEmptyOrOccupiedByOpponentProcedure(board, newPosition, myPosition, moves)) {
                break;
            }
        }
        int row2 = myPosition.getRow();
        int column2 = myPosition.getColumn();
        while (row2 > 1 && column2 < 8) {
            row2--;
            column2++;
            ChessPosition newPosition = new ChessPosition(row2, column2);
            if (positionEmptyOrOccupiedByOpponentProcedure(board, newPosition, myPosition, moves)) {
                break;
            }
        }
        int row3 = myPosition.getRow();
        int column3 = myPosition.getColumn();
        while (row3 > 1 && column3 > 1) {
            row3--;
            column3--;
            ChessPosition newPosition = new ChessPosition(row3, column3);
            if (positionEmptyOrOccupiedByOpponentProcedure(board, newPosition, myPosition, moves)) {
                break;
            }
        }
        int row4 = myPosition.getRow();
        int column4 = myPosition.getColumn();
        while (row4 < 8 && column4 > 1) {
            row4++;
            column4--;
            ChessPosition newPosition = new ChessPosition(row4, column4);
            if (positionEmptyOrOccupiedByOpponentProcedure(board, newPosition, myPosition, moves)) {
                break;
            }
        }
    }

    private void whitePawnMove(ChessPosition myPosition, Collection<ChessMove> moves, int row, ChessPosition newPosition) {
        if (row == 8) {
            pawnPromotionMoves(myPosition, moves, newPosition);
        } else {
            ChessMove move = new ChessMove(myPosition, newPosition, null);
            moves.add(move);
        }
    }

    private void blackPawnMove(ChessPosition myPosition, Collection<ChessMove> moves, int row, ChessPosition newPosition) {
        if (row == 1) {
            pawnPromotionMoves(myPosition, moves, newPosition);
        } else {
            ChessMove move = new ChessMove(myPosition, newPosition, null);
            moves.add(move);
        }
    }

    private void pawnPromotionMoves(ChessPosition myPosition, Collection<ChessMove> moves, ChessPosition newPosition) {
        ChessMove promotion1 = new ChessMove(myPosition, newPosition, PieceType.QUEEN);
        moves.add(promotion1);
        ChessMove promotion2 = new ChessMove(myPosition, newPosition, PieceType.BISHOP);
        moves.add(promotion2);
        ChessMove promotion3 = new ChessMove(myPosition, newPosition, PieceType.KNIGHT);
        moves.add(promotion3);
        ChessMove promotion4 = new ChessMove(myPosition, newPosition, PieceType.ROOK);
        moves.add(promotion4);
    }

    private void kingAndKnightOccupiedSpace(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves, int row, int column) {
        ChessPosition newPosition = new ChessPosition(row, column);
        if (board.getPiece(newPosition) == null) {
            ChessMove move = new ChessMove(myPosition, newPosition, null);
            moves.add(move);
        } else {
            if (board.getPiece(newPosition).getTeamColor() != pieceColor) {
                ChessMove move = new ChessMove(myPosition, newPosition, null);
                moves.add(move);
            }
        }
    }
}
