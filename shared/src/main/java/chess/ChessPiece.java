package chess;

import java.util.ArrayList;
import java.util.Collection;

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
        if (piece == PieceType.BISHOP) {
            int row1 = myPosition.getRow();
            int column1 = myPosition.getColumn();
            while (row1 < 8 && column1 < 8) {
                row1++;
                column1++;
                ChessPosition newPosition = new ChessPosition(row1, column1);
                if (board.getPiece(newPosition) == null) {
                    ChessMove move = new ChessMove(myPosition,newPosition,null);
                    moves.add(move);
                } else {
                    if (board.getPiece(newPosition).getTeamColor() != pieceColor) {
                        ChessMove move = new ChessMove(myPosition,newPosition,null);
                        moves.add(move);
                    }
                    break;
                }
            }
            int row2 = myPosition.getRow();
            int column2 = myPosition.getColumn();
            while (row2 > 1 && column2 < 8) {
                row2--;
                column2++;
                ChessPosition newPosition = new ChessPosition(row2, column2);
                if (board.getPiece(newPosition) == null) {
                    ChessMove move = new ChessMove(myPosition,newPosition,null);
                    moves.add(move);
                } else {
                    if (board.getPiece(newPosition).getTeamColor() != pieceColor) {
                        ChessMove move = new ChessMove(myPosition,newPosition,null);
                        moves.add(move);
                    }
                    break;
                }
            }
            int row3 = myPosition.getRow();
            int column3 = myPosition.getColumn();
            while (row3 > 1 && column3 > 1) {
                row3--;
                column3--;
                ChessPosition newPosition = new ChessPosition(row3, column3);
                if (board.getPiece(newPosition) == null) {
                    ChessMove move = new ChessMove(myPosition,newPosition,null);
                    moves.add(move);
                } else {
                    if (board.getPiece(newPosition).getTeamColor() != pieceColor) {
                        ChessMove move = new ChessMove(myPosition,newPosition,null);
                        moves.add(move);
                    }
                    break;
                }
            }
            int row4 = myPosition.getRow();
            int column4 = myPosition.getColumn();
            while (row4 < 8 && column4 > 1) {
                row4++;
                column4--;
                ChessPosition newPosition = new ChessPosition(row4, column4);
                if (board.getPiece(newPosition) == null) {
                    ChessMove move = new ChessMove(myPosition,newPosition,null);
                    moves.add(move);
                } else {
                    if (board.getPiece(newPosition).getTeamColor() != pieceColor) {
                        ChessMove move = new ChessMove(myPosition,newPosition,null);
                        moves.add(move);
                    }
                    break;
                }
            }
        }
        return moves;
    }
}
