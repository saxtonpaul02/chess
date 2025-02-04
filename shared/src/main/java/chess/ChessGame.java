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
        ChessPiece piece = currentBoard.getPiece(startPosition);
        if (piece == null) {
            return null;
        } else {
            TeamColor pieceColor = piece.getTeamColor();
            Collection<ChessMove> moves = piece.pieceMoves(currentBoard, startPosition);
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
        }
        else {
            ChessPosition endPosition = move.getEndPosition();
            ChessPiece newPiece;
            if (move.getPromotionPiece() == null) {
                newPiece = piece;
            } else {
                newPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            }
            currentBoard.addPiece(startPosition, null);
            currentBoard.addPiece(endPosition, newPiece);
            if (piece.getTeamColor() == TeamColor.WHITE) {
                setTeamTurn(TeamColor.BLACK);
            } else {
                setTeamTurn(TeamColor.WHITE);
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
            ChessPosition kingPosition = null;
            Collection<ChessMove> kingMoves = new ArrayList<>();
            Map<ChessPosition, ChessPiece> checkingPieces = new HashMap<>();
            Set<ChessPosition> teamMovePositions = new HashSet<>();
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    ChessPosition testPosition = new ChessPosition(i, j);
                    ChessPiece testPiece = currentBoard.getPiece(testPosition);
                    if (testPiece != null) {
                        if (testPiece.getTeamColor() == teamColor && testPiece.getPieceType() == KING) {
                            kingPosition = testPosition;
                            kingMoves = validMoves(testPosition);
                        } else if (testPiece.getTeamColor() == teamColor && testPiece.getPieceType() != KING) {
                            Collection<ChessMove> teamMoves = validMoves(testPosition);
                            for (ChessMove teamMove : teamMoves) {
                                teamMovePositions.add(teamMove.getEndPosition());
                            }
                        } else if (testPiece.getTeamColor() != teamColor && testPiece.getPieceType() != null) {
                            Collection<ChessMove> testMoves = validMoves(testPosition);
                            for (ChessMove testMove : testMoves) {
                                if (testMove.getEndPosition().equals(kingPosition)) {
                                    checkingPieces.put(testPosition, testPiece);
                                }
                            }
                        }
                    }
                }
            }
            if (!kingMoves.isEmpty()) {
                return false;
            } else if (checkingPieces.size() > 1) {
                return true;
            } else {
                for (ChessPosition checkingPiecesPosition : checkingPieces.keySet()) {
                    return !piecesCanBlockCheck(teamMovePositions, checkingPiecesPosition, checkingPieces.get(checkingPiecesPosition), kingPosition);
                }
            }
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
            Collection<ChessMove> kingMoves = new ArrayList<>();
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    ChessPosition testPosition = new ChessPosition(i, j);
                    ChessPiece testPiece = currentBoard.getPiece(testPosition);
                    if (testPiece != null) {
                        if (testPiece.getTeamColor() == teamColor && testPiece.getPieceType() == KING) {
                            kingMoves = validMoves(testPosition);
                        }
                    }
                }
            }
            return kingMoves.isEmpty();
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
     * Determines if the king's team members can get the king out of check by taking or blocking the
     * checking piece, performed after having determined the king cannot get himself out of check
     * and that there is only one checking piece
     *
     * @param teamMovePositions the set of all possible positions for the king's team members to move to
     * @param checkingPiecePosition the position of the piece checking the king
     * @param checkingPiece the type of piece checking the king
     * @param kingPosition the position of the king
     * @return True if the piece checking the king can be legally taken or if check can be blocked by one
     * of the king's team members, otherwise false
     */

    private boolean piecesCanBlockCheck(Set<ChessPosition> teamMovePositions, ChessPosition checkingPiecePosition, ChessPiece checkingPiece, ChessPosition kingPosition) {
        if (teamMovePositions.contains(checkingPiecePosition)) return true;
        else if (checkingPiece.getPieceType() == PAWN || checkingPiece.getPieceType() == KNIGHT) return false;
        else {
            int rowDiff = kingPosition.getRow() - checkingPiecePosition.getRow();
            int colDiff = kingPosition.getColumn() - checkingPiecePosition.getColumn();
            if (Math.abs(rowDiff) == Math.abs(colDiff)) {
                for (ChessPosition teamPosition : teamMovePositions) {
                    int teamRowDiff = kingPosition.getRow() - teamPosition.getRow();
                    int teamColDiff = kingPosition.getColumn() - teamPosition.getColumn();
                    if (Math.abs(teamRowDiff) == Math.abs(teamColDiff) && Math.abs(teamRowDiff) < Math.abs(rowDiff)) {
                        if (rowDiff > 0 && colDiff > 0 && teamRowDiff > 0 && teamColDiff > 0) return true;
                        else if (rowDiff > 0 && colDiff < 0 && teamRowDiff > 0 && teamColDiff < 0) return true;
                        else if (rowDiff < 0 && colDiff > 0 && teamRowDiff < 0 && teamColDiff > 0) return true;
                        else if (rowDiff < 0 && colDiff < 0 && teamRowDiff < 0 && teamColDiff < 0) return true;
                        else continue;
                    } else continue;
                }
            } else if (rowDiff == 0) {
                for (ChessPosition teamPosition : teamMovePositions) {
                    int teamRow = teamPosition.getRow();
                    int kingRow = kingPosition.getRow();
                    if (teamRow == kingRow) {
                        int teamCol = teamPosition.getColumn();
                        int kingCol = kingPosition.getColumn();
                        int opponentCol = checkingPiecePosition.getColumn();
                        if (kingCol < teamCol && teamCol < opponentCol) return true;
                        else if (kingCol > teamCol && teamCol > opponentCol) return true;
                        else continue;
                    } else continue;
                }
            } else {
                for (ChessPosition teamPosition : teamMovePositions) {
                    int teamCol = teamPosition.getColumn();
                    int kingCol = kingPosition.getColumn();
                    if (teamCol == kingCol) {
                        int teamRow = teamPosition.getRow();
                        int kingRow = kingPosition.getRow();
                        int opponentRow = checkingPiecePosition.getRow();
                        if (kingRow < teamRow && teamRow < opponentRow) return true;
                        else if (kingRow > teamRow && teamRow > opponentRow) return true;
                        else continue;
                    } else continue;
                }
            }
        }
        return false;
    }
}

