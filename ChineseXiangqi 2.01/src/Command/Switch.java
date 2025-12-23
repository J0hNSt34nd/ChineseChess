package Command;

import Pieces.Piece;
import main.Board;
import main.Game;

public class Switch {

    private Board _board;

    public Switch(Board board) {
        _board = board;

    }

    public void proceed() {
        System.out.println(_board.getTurn());
//        if (_board.isRedFirst()) {
        if (!Game.isNetworkMode()) {
            if (_board.pieceMoving && _board.getTurn() % 2 == 0 && !_board.isRedFirst()) {
                for (Piece piece : _board.getPieces()) {
                    if (piece.getTeam() == 0)
                        piece.chosable = true;
                    else piece.chosable = false;
                }
//            _board.nextTurn();
            } else if (_board.pieceMoving && _board.getTurn() % 2 == 1 && _board.isRedFirst()) {
                for (Piece piece : _board.getPieces()) {
                    if (piece.getTeam() == 1)
                        piece.chosable = true;

                    else
                        piece.chosable = false;
                }
//            _board.nextTurn();
            }
        } else {
            if (_board.isRedFirst()) {
                if (_board.pieceMoving && _board.getTurn() % 2 == 1) {
                    for (Piece piece : _board.getPieces()) {
                        if (piece.getTeam() == 1)
                            piece.chosable = true;
                        else piece.chosable = false;
                    }
                }
                else if (_board.pieceMoving && _board.getTurn() % 2 == 0) {
                    for (Piece piece : _board.getPieces()) {
                        piece.chosable = false;
                    }
                }
            } else if (!_board.isRedFirst()) {
                if (_board.pieceMoving && _board.getTurn() % 2 == 0) {
                    for (Piece piece : _board.getPieces()) {
                        if (piece.getTeam() == 0)
                            piece.chosable = true;
                        else piece.chosable = false;
                    }
                } else if (_board.pieceMoving && _board.getTurn() % 2 == 1) {
                    for (Piece piece : _board.getPieces()) {
                        piece.chosable = false;
                    }
                }
            }
        }


//        } else if (!_board.isRedFirst()) {
//            if (_board.pieceMoving && _board.getTurn() % 2 == 1) {
//                for (Piece piece : _board.getPieces()) {
//                    if (piece.getTeam() == 0)
//                        piece.chosable = true;
//                    else piece.chosable = false;
//                }
////            _board.nextTurn();
//            } else if (_board.pieceMoving && _board.getTurn() % 2 == 0) {
//                for (Piece piece : _board.getPieces()) {
//                    if (piece.getTeam() == 1)
//                        piece.chosable = true;
//
//                    else
//                        piece.chosable = false;
//                }
////            _board.nextTurn();
//            }
//        }

    }

    public void forceproceed() {
        System.out.println(_board.getTurn());
//        if (_board.isRedFirst()) {
        if (!Game.isNetworkMode()) {
            if (_board.getTurn() % 2 == 0 && !_board.isRedFirst()) {
                for (Piece piece : _board.getPieces()) {
                    if (piece.getTeam() == 0)
                        piece.chosable = true;
                    else piece.chosable = false;
                }
//            _board.nextTurn();
            } else if (_board.getTurn() % 2 == 1 && _board.isRedFirst()) {
                for (Piece piece : _board.getPieces()) {
                    if (piece.getTeam() == 1)
                        piece.chosable = true;

                    else
                        piece.chosable = false;
                }
//            _board.nextTurn();
            }
        } else {
            if (_board.isRedFirst()) {
                if (_board.getTurn() % 2 == 1) {
                    for (Piece piece : _board.getPieces()) {
                        if (piece.getTeam() == 1)
                            piece.chosable = true;
                        else piece.chosable = false;
                    }
                }
                else if (_board.getTurn() % 2 == 0) {
                    for (Piece piece : _board.getPieces()) {
                        piece.chosable = false;
                    }
                }
            } else if (!_board.isRedFirst()) {
                if (_board.getTurn() % 2 == 0) {
                    for (Piece piece : _board.getPieces()) {
                        if (piece.getTeam() == 0)
                            piece.chosable = true;
                        else piece.chosable = false;
                    }
                } else if (_board.getTurn() % 2 == 1) {
                    for (Piece piece : _board.getPieces()) {
                        piece.chosable = false;
                    }
                }
            }
        }

    }
}
