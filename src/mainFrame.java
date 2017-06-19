
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author dmcd2356
 */
public class mainFrame extends javax.swing.JFrame {

    private static final String newLine = System.getProperty("line.separator");
    
    enum StateType {
        STATE_INIT,
        STATE_CHOOSE,
        STATE_MOVE,
        STATE_DONE
    };

    private class PegMoveStc {
        int fromPeg;        // initial location
        int overPeg;        // location being jumped
        int toPeg;          // dest location
        
        public PegMoveStc () {
            fromPeg = 0;
            overPeg = 0;
            toPeg   = 0;
        }
        
        public PegMoveStc (PegMoveStc move) {
            fromPeg = move.fromPeg;
            overPeg = move.overPeg;
            toPeg   = move.toPeg;
        }
        
        public PegMoveStc (int from, int over, int to) {
            fromPeg = from;
            overPeg = over;
            toPeg   = to;
        }
    };

    private class HistoryStc {
        int count;          // number of pegs in starting board layout
        int layout_init;    // starting board layout for this peg count
        int layout_end;     // ending   board layout for this peg count
        int moveIx;         // index of move in PossibleMoves list
        int parentIx;       // index of parent in NextMoves list
        int childIx;        // index of 1st child entry in NextMoves list
        int endmask;        // this holds the ending peg count status (bit mask)
        
        public HistoryStc (int start, int end, int move, int parent) {
            count  = countPegs (start);
            layout_init = start;
            layout_end  = end;
            moveIx = move;
            parentIx = parent;
            childIx = -1;
            endmask = 0;
        }
        
        public void setFirstChild (int child) {
            childIx = child;
        }
    };

    private class PegLevels {
        int  index;         // starting index of level
        int  size;          // number of entries in level
        
        public PegLevels () {
            index = 0;
            size = 0;
        }
    }
    
    // total number of starting pegs in game
    private final int NUMBER_OF_STARTING_PEGS = 14;

    private final String Ratings[] = {
        /* 0 left */ "<impossible>",
        /* 1 left */ "Super Smartie-Pants",
        /* 2 left */ "Peg Of My Heart...",
        /* 3 left */ "What? You saw I was giving you hints didn't you?",
        /* 4 left */ "I hope you at least have good looks or money",
        /* 5 left */ "Dumb as a bucket of stupid",
        /* 6 left */ "You're probably not smart enough to read your rating",
        /* 7 left */ "Rode the short bus to school, huh?",
        /* 8 left */ "Impressive! I've never seen a complete falure before",
    };

    StateType  State;           // current peg move state
    int        Layout;          // current board layout in bit field format
    int        ListCount;       // number of total moves in linked list
    int        CurrIx;          // index in NextMoves list for current position
    PegMoveStc CurrMove;        // current move information
    ArrayList<PegMoveStc> PossibleMoves; // array of possible moves
    LinkedList<PegMoveStc> PrevMoves; // queue of prev moves for undo to use

    ArrayList<HistoryStc> NextMoves; // array of possible moves
    PegLevels[] MoveLevels;          // holds the index and size of each level in NextMoves
    
    /**
     * Creates new form mainFrame
     */
    public mainFrame() {
        initComponents();
        
        CurrIx = 0; // this will reflect the 1st entry in table, which is the initial layout
        PrevMoves = new LinkedList<>();
        CurrMove = new PegMoveStc();
        NextMoves = new ArrayList<>();
        MoveLevels = new PegLevels[NUMBER_OF_STARTING_PEGS];
        for (int ix = 0; ix < NUMBER_OF_STARTING_PEGS; ix++)
            MoveLevels[ix] = new PegLevels();
        
        // the peg numbering:
        //         01
        //       02  03
        //     04  05  06
        //   07  08  09  10
        // 11  12  13  14  15
        //
        // setup the list of all possible moves so we can specify a move
        // by just the index into this list
        PossibleMoves = new ArrayList<>();                  // moveIx
        PossibleMoves.add( new PegMoveStc( 1,  2,  4) );    // 0
        PossibleMoves.add( new PegMoveStc( 1,  3,  6) );    // 1
        PossibleMoves.add( new PegMoveStc( 2,  4,  7) );    // 2
        PossibleMoves.add( new PegMoveStc( 2,  5,  9) );    // 3
        PossibleMoves.add( new PegMoveStc( 3,  5,  8) );    // 4
        PossibleMoves.add( new PegMoveStc( 3,  6, 10) );    // 5
        PossibleMoves.add( new PegMoveStc( 4,  2,  1) );    // 6
        PossibleMoves.add( new PegMoveStc( 4,  5,  6) );    // 7
        PossibleMoves.add( new PegMoveStc( 4,  7, 11) );    // 8
        PossibleMoves.add( new PegMoveStc( 4,  8, 13) );    // 9
        PossibleMoves.add( new PegMoveStc( 5,  8, 12) );    // 10
        PossibleMoves.add( new PegMoveStc( 5,  9, 14) );    // 11
        PossibleMoves.add( new PegMoveStc( 6,  3,  1) );    // 12
        PossibleMoves.add( new PegMoveStc( 6,  5,  4) );    // 13
        PossibleMoves.add( new PegMoveStc( 6,  9, 13) );    // 14
        PossibleMoves.add( new PegMoveStc( 6, 10, 15) );    // 15
        PossibleMoves.add( new PegMoveStc( 7,  4,  2) );    // 16
        PossibleMoves.add( new PegMoveStc( 7,  8,  9) );    // 17
        PossibleMoves.add( new PegMoveStc( 8,  5,  3) );    // 18
        PossibleMoves.add( new PegMoveStc( 8,  9, 10) );    // 19
        PossibleMoves.add( new PegMoveStc( 9,  5,  2) );    // 20
        PossibleMoves.add( new PegMoveStc( 9,  8,  7) );    // 21
        PossibleMoves.add( new PegMoveStc(10,  6,  3) );    // 22
        PossibleMoves.add( new PegMoveStc(10,  9,  8) );    // 23
        PossibleMoves.add( new PegMoveStc(11,  7,  4) );    // 24
        PossibleMoves.add( new PegMoveStc(11, 12, 13) );    // 25
        PossibleMoves.add( new PegMoveStc(12,  8,  5) );    // 26
        PossibleMoves.add( new PegMoveStc(12, 13, 14) );    // 27
        PossibleMoves.add( new PegMoveStc(13,  8,  4) );    // 28
        PossibleMoves.add( new PegMoveStc(13,  9,  6) );    // 29
        PossibleMoves.add( new PegMoveStc(13, 12, 11) );    // 30
        PossibleMoves.add( new PegMoveStc(13, 14, 15) );    // 31
        PossibleMoves.add( new PegMoveStc(14,  9,  5) );    // 32
        PossibleMoves.add( new PegMoveStc(14, 13, 12) );    // 33
        PossibleMoves.add( new PegMoveStc(15, 10,  6) );    // 34
        PossibleMoves.add( new PegMoveStc(15, 14, 13) );    // 35
        
        // init the pegs
        initPegs();
    }

    private void initPegs () {
        State = StateType.STATE_INIT;
        ListCount = 0;
        PrevMoves.clear();

        // init user interface
        ratingTextField.setText( "" );
        instructionsTextField.setText("choose a starting peg to remain open");
        movesTextPane.setText("");
        countTextArea.setText("Total: 0" + newLine);
        countTextArea.setText("");
        for( int pegs = 1; pegs <=8; pegs++ ) {
            countTextArea.setText(countTextArea.getText() + pegs + " left: 0" + newLine);
        }

        // init pegs
        for( int ix = 1; ix <= 15; ix++ ) {
            pegMark(ix, false);
            pegSet (ix, true);
        }
    }

    private void logMove (PegMoveStc move, boolean undo) {
        if (undo) {
            movesTextPane.setText(movesTextPane.getText() + "UNDO: " + move.fromPeg + " to " + move.toPeg + ", removed " + move.overPeg + "\n");
        }
        else {
            movesTextPane.setText(movesTextPane.getText() + move.fromPeg + " to " + move.toPeg + ", removed " + move.overPeg + "\n");
        }
    }
    
    /**
     * undo the last move.
     * This removes the last move from the history list and restores the
     * statistics presented to the user to the previous move.
     */
    private void undoLastMove () {
        if( PrevMoves.size() > 0 ) {
            // undo the last move
            PegMoveStc lastMove = PrevMoves.removeLast();
            pegSet(lastMove.fromPeg, true);
            pegSet(lastMove.overPeg, true);
            pegSet(lastMove.toPeg  , false);

            // re-generate board layout
            Layout = pegGetLayout();

            // re-calculate future move statistics
            int moveIx = NextMoves.get(CurrIx).parentIx;
            if (moveIx >= 0) {
                CurrIx = moveIx;
                int endmask = NextMoves.get(CurrIx).endmask;
                updateFutureMoves(endmask);
            }

            // make sure we reset to pick a peg state (in case game ended)
            State = StateType.STATE_CHOOSE;
            instructionsTextField.setText("select next peg to move");
            ratingTextField.setText( "" );

            // remove last entry in moves list
            logMove (lastMove, true);
        }
        else
        {
            ratingTextField.setText("nothing to undo");
        }
    }
    
    /**
     * Reads the peg locations from the gui and generates a bit-mask value
     * that represents each peg remaining.
     * 
     * @return bit-mask of the remaining pegs
     */
    private int pegGetLayout () {
        int  BoardLayout = 0;
    
        if(pegRadioButton1.isSelected())  BoardLayout |= 0x0001;
        if(pegRadioButton2.isSelected())  BoardLayout |= 0x0002;
        if(pegRadioButton3.isSelected())  BoardLayout |= 0x0004;
        if(pegRadioButton4.isSelected())  BoardLayout |= 0x0008;
        if(pegRadioButton5.isSelected())  BoardLayout |= 0x0010;
        if(pegRadioButton6.isSelected())  BoardLayout |= 0x0020;
        if(pegRadioButton7.isSelected())  BoardLayout |= 0x0040;
        if(pegRadioButton8.isSelected())  BoardLayout |= 0x0080;
        if(pegRadioButton9.isSelected())  BoardLayout |= 0x0100;
        if(pegRadioButton10.isSelected()) BoardLayout |= 0x0200;
        if(pegRadioButton11.isSelected()) BoardLayout |= 0x0400;
        if(pegRadioButton12.isSelected()) BoardLayout |= 0x0800;
        if(pegRadioButton13.isSelected()) BoardLayout |= 0x1000;
        if(pegRadioButton14.isSelected()) BoardLayout |= 0x2000;
        if(pegRadioButton15.isSelected()) BoardLayout |= 0x4000;
    
        return BoardLayout;
    }
    
    /**
     * determines if the specified peg slot is occupied.
     * 
     * @param peg - the peg selection (1 - 15)
     * @return true if specified slot is occupied
     */
    private boolean pegGetStatus (int peg) {
        boolean pegVal = false;
        
        switch(peg) {
        case  1 : pegVal = pegRadioButton1.isSelected();  break;
        case  2 : pegVal = pegRadioButton2.isSelected();  break;
        case  3 : pegVal = pegRadioButton3.isSelected();  break;
        case  4 : pegVal = pegRadioButton4.isSelected();  break;
        case  5 : pegVal = pegRadioButton5.isSelected();  break;
        case  6 : pegVal = pegRadioButton6.isSelected();  break;
        case  7 : pegVal = pegRadioButton7.isSelected();  break;
        case  8 : pegVal = pegRadioButton8.isSelected();  break;
        case  9 : pegVal = pegRadioButton9.isSelected();  break;
        case 10 : pegVal = pegRadioButton10.isSelected(); break;
        case 11 : pegVal = pegRadioButton11.isSelected(); break;
        case 12 : pegVal = pegRadioButton12.isSelected(); break;
        case 13 : pegVal = pegRadioButton13.isSelected(); break;
        case 14 : pegVal = pegRadioButton14.isSelected(); break;
        case 15 : pegVal = pegRadioButton15.isSelected(); break;
        default :
            break;
        }
        
        return pegVal;
    }
    
    /**
     * modifies gui to add or remove a peg in the specified slot.
     * 
     * @param peg - the peg selection (1 - 15)
     * @param enable - true to add a peg, false to remove it
     */
    private void pegSet (int peg, boolean enable) {
        switch(peg) {
        case  1 : pegRadioButton1.setSelected(enable);  break;
        case  2 : pegRadioButton2.setSelected(enable);  break;
        case  3 : pegRadioButton3.setSelected(enable);  break;
        case  4 : pegRadioButton4.setSelected(enable);  break;
        case  5 : pegRadioButton5.setSelected(enable);  break;
        case  6 : pegRadioButton6.setSelected(enable);  break;
        case  7 : pegRadioButton7.setSelected(enable);  break;
        case  8 : pegRadioButton8.setSelected(enable);  break;
        case  9 : pegRadioButton9.setSelected(enable);  break;
        case 10 : pegRadioButton10.setSelected(enable); break;
        case 11 : pegRadioButton11.setSelected(enable); break;
        case 12 : pegRadioButton12.setSelected(enable); break;
        case 13 : pegRadioButton13.setSelected(enable); break;
        case 14 : pegRadioButton14.setSelected(enable); break;
        case 15 : pegRadioButton15.setSelected(enable); break;
        default :
            break;
        }
    }
    
    /**
     * modifies gui to mark the selected peg to be moved, or unmark it
     * after it is moved.
     * 
     * @param peg - the peg selection (1 - 15)
     * @param enable - true to mark a peg, false to unmark it
     */
    private void pegMark (int peg, boolean enable) {
        enable = !enable;
        switch( peg ) {
        case  1 : pegRadioButton1.setEnabled(enable);  break;
        case  2 : pegRadioButton2.setEnabled(enable);  break;
        case  3 : pegRadioButton3.setEnabled(enable);  break;
        case  4 : pegRadioButton4.setEnabled(enable);  break;
        case  5 : pegRadioButton5.setEnabled(enable);  break;
        case  6 : pegRadioButton6.setEnabled(enable);  break;
        case  7 : pegRadioButton7.setEnabled(enable);  break;
        case  8 : pegRadioButton8.setEnabled(enable);  break;
        case  9 : pegRadioButton9.setEnabled(enable);  break;
        case 10 : pegRadioButton10.setEnabled(enable); break;
        case 11 : pegRadioButton11.setEnabled(enable); break;
        case 12 : pegRadioButton12.setEnabled(enable); break;
        case 13 : pegRadioButton13.setEnabled(enable); break;
        case 14 : pegRadioButton14.setEnabled(enable); break;
        case 15 : pegRadioButton15.setEnabled(enable); break;
        default :
            break;
        }
    }
    
    /**
     * Determines if the specified peg is valid to be moved for the given layout.
     * 
     * @param layout - bitmask value of the specified peg layout
     * @param peg - the peg selection (1 - 15)
     * @return true if peg has at least 1 valid move
     */
    private boolean isMovable (int layout, int peg) {
        int from_peg, over_peg, to_peg;
        
        // search all possible moves to make
        for( int ix = 0; ix < PossibleMoves.size(); ix++ ) {
            // check if this peg is in the possible moves list
            PegMoveStc validMove = PossibleMoves.get(ix);
            if( peg == validMove.fromPeg ) {
                // get bit mask values for the peg move
                from_peg = 1 << (validMove.fromPeg - 1);
                over_peg = 1 << (validMove.overPeg - 1);
                to_peg   = 1 << (validMove.toPeg   - 1);
                
                // check if the peg is in the starting pos, the dest pos is available,
                // and the peg to jump exists
                if( (layout & from_peg) != 0 && (layout & over_peg) != 0 && (layout & to_peg) == 0) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * determines if any pegs can be moved for the specified layout.
     * 
     * @param layout - bitmask value of the specified peg layout
     * @return true if at least 1 peg has at least 1 valid move
     */
    private boolean anyValidMoves (int layout) {
        int from_peg, over_peg, to_peg;
        
        // search all possible moves to make
        for( int ix = 0; ix < PossibleMoves.size(); ix++ ) {
            // get bit mask values for the peg move
            PegMoveStc validMove = PossibleMoves.get(ix);
            from_peg = 1 << (validMove.fromPeg - 1);
            over_peg = 1 << (validMove.overPeg - 1);
            to_peg   = 1 << (validMove.toPeg   - 1);
    
            // check if the peg is in the starting pos, the dest pos is available,
            // and the peg to jump exists
            if( (layout & from_peg) != 0 && (layout & over_peg) != 0 && (layout & to_peg) == 0 ) {
                return true;
            }
        }
    
        return false;
    }
    
    /**
     * determines if the specified move is valid for the specified peg layout.
     * 
     * @param layout - bitmask value of the specified peg layout
     * @param move - the move to test
     * @return the new board layout if valid, 0 if not
     */
    private int isValidMove (int layout, PegMoveStc move) {
        int from_peg, over_peg, to_peg;
        int new_layout;
        
        // get bit mask values for the peg move
        from_peg = 1 << (move.fromPeg - 1);
        over_peg = 1 << (move.overPeg - 1);
        to_peg   = 1 << (move.toPeg   - 1);
                
        // check if the peg is in the starting pos, the dest pos is available,
        // and the peg to jump exists
        if( (layout & from_peg) != 0 && (layout & over_peg) != 0 && (layout & to_peg) == 0) {
            // determine new layout
            new_layout = layout;
            new_layout &= ~(from_peg | over_peg); // remove these pegs
            new_layout |= to_peg;  // set this peg
            return new_layout;
        }
    
        return 0;
    }
    
    /**
     * determines if the specified move is valid for the specified peg layout.
     * 
     * @param from - the peg to move
     * @param to - the ending position of the move
     * @return the move index in the PossibleMoves list, -1 if invalid move
     */
    private int getPegMoveIx (int from, int to) {
        // search all possible moves to make
        for( int ix = 0; ix < PossibleMoves.size(); ix++ ) {
            // check if this move is in the possible moves list
            PegMoveStc validMove = PossibleMoves.get(ix);
            if( (from == validMove.fromPeg) && (to == validMove.toPeg)) {
                return ix;
            }
        }
        return -1;
    }
    
    /**
     * counts the number of pegs in a layout.
     * 
     * @param layout - the spercified peg layout
     * @return the number of pegs on the board
     */
    private int countPegs (int layout) {
        int count = 0;
        int pegmask = 0x01;
        
        for( int ix = 0; ix < 15; ix++ ) {
            if((layout & pegmask) != 0) {
                count++;
            }
    
            pegmask <<= 1;
        }
    
        return count;
    }

    /**
     * returns the index in NextMoves for the move selected
     * 
     * @param moveIx - the index in PossibleMoves for the selected move
     * @return index in NextMoves for the selection made (-1 if invalid inputs)
     */
    private int getNext (int moveIx) {
        if (CurrIx < 0 || CurrIx >= NextMoves.size()) {
            System.out.println("Invalid value for CurrIx: " + CurrIx);
            return -1;
        }
        if (CurrIx == 0) {
            System.out.println("No more previous moves");
            return 0;
        }
        int layout = NextMoves.get(CurrIx).layout_init;
        int childIx = NextMoves.get(CurrIx).childIx;
        
        // make sure the node has a child
        if (childIx < 0 || childIx >= NextMoves.size())
            return -1;

        // search all the children of the current node
        for (int ix = childIx; NextMoves.get(ix).parentIx == layout; ix++) {
            // exit with the child index
            if (NextMoves.get(ix).moveIx == moveIx) {
                CurrIx = ix;
                return ix;
            }
        }
        
        System.out.println("ERROR: child not found for CurrIx: " + CurrIx);
        return -1;
    }
    
    /**
     * For a given peg layout find all the next valid moves.
     * Adds the entries to the history buffer if valid.
     * 
     * @param currIx   - index in NextMoves of current layout
     * @param parentIx - index in NextMoves of parent layout
     * @param layout   - bitmask value of the specified peg layout
     * @return the number of entries added for this layout
     */
    private int saveNextMoves (int parentIx, int layout) {
        int count = 0;
        for (int moveIx = 0; moveIx < PossibleMoves.size(); moveIx++) {
            int newLayout = isValidMove (layout, PossibleMoves.get(moveIx));
            if (newLayout != 0) {
                // create an entry indicating the start and end layouts and
                // the move that got us there and the index in this list of
                // its parent and add it to the history list.
                ++count;
                HistoryStc entry = new HistoryStc(layout, newLayout, moveIx, parentIx);
                int currIx = NextMoves.indexOf(entry);
                if (currIx < 0) {
                    currIx = NextMoves.size(); // the size before we add the entry will be its index
                    NextMoves.add(entry);
                }

                // save index of 1st child in parent layout for forward reference
                if (parentIx >= 0 && NextMoves.get(parentIx).childIx == -1)
                    NextMoves.get(parentIx).setFirstChild(currIx);
            }
        }
        return count;
    }
    
    private int extractMoves (int layout) {
        // run all of the layouts at this level of peg count
        int pcount = countPegs (layout);
        int totalsize = 0;

        // clear List of future moves
        NextMoves.clear();

        // create the entries of moves for the initial layout
        saveNextMoves(-1, layout);
        
        // start with the initial layout as the 1st entry
        // (note that the begin and end layouts are the same)
//        HistoryStc entry = new HistoryStc(layout, layout, -1, pcount, parentIx);
//        NextMoves.add(entry);

        // now run through each entry in the table to get the next moves for each
        // sibling at the current level
        for(int parentIx = 0; parentIx < NextMoves.size(); parentIx++) {

            // add all the possible move entries for the next terminating layout
            // (this adds entries to NextMoves if there are any)
            layout = NextMoves.get(parentIx).layout_end;
            saveNextMoves(parentIx, layout);

            // check if we have exhaused the current peg count moves and
            // keep an array of where each new peg count starts in the list
            // and how many entries in it
            int newpcount = countPegs (layout);
            if (pcount != newpcount) {
                int newsize = NextMoves.size();
                pcount = newpcount;
                System.out.println("level " + pcount + ": " + (newsize - totalsize) + " entries");
                if (pcount <= NUMBER_OF_STARTING_PEGS && pcount > 0) {
                    MoveLevels[pcount-1].index = totalsize;
                    MoveLevels[pcount-1].size  = newsize - totalsize;
                }
                totalsize = newsize;
            }
        }

        // now we need to find the stats for each move - start from the lowest level
        int currmask = 0x01; // LSbit indicates path goes to single peg
        for (int level = 0; level < NUMBER_OF_STARTING_PEGS; level++) {
            int startIx = MoveLevels[level].index;
            int length  = MoveLevels[level].size;
            for (int ix = startIx; ix < startIx + length; ix++) {
                // set the bit indicating we reached the current level
                int childIx = NextMoves.get(ix).childIx;
                if (childIx < 0)
                    NextMoves.get(ix).endmask = currmask;
                else {
                    // this one has a child, so sum up all of the chilkdren end condition bits
                    for (int nextix = childIx; nextix < NextMoves.size() && ix == NextMoves.get(nextix).parentIx; nextix++)
                        NextMoves.get(ix).endmask |= NextMoves.get(nextix).endmask;
                }
            }
            
            // update the lower order masks and the current bit mask
            currmask <<= 1;
            System.out.println("level " + (level+1) + ": completed");
        }

        return pcount;
    }

    /**
     * determines the statistics for future moves given the layout.
     * Results are displayed in the gui.
     * 
     * @param endmask - bitmask indicating possible peg count endings
     */
    private void updateFutureMoves (int endmask) {
        countTextArea.setText("");
        for( int pegs = 1; pegs <=8; pegs++ ) {
            int maskbit = 1 << pegs;
            String value = (endmask & maskbit) == 0 ? "0":"1";
            countTextArea.setText(countTextArea.getText() + pegs + " left: " + value + newLine);
        }
    }
    
    /**
     * the state machine for choosing the starting and ending location
     * to move a peg.
     * 
     * @param peg - the selected peg (1 - 15)
     */
    private void pegMove (int peg) {
        int new_layout;
        int pegs_left;
        int endmask;
        
        // clear out this field initially
        ratingTextField.setText("");
        
        // undo the toggle action that is automatically done to the peg state
        // because we are not using them as such and want to set them ourselves.
        if( pegGetStatus(peg) )
            pegSet(peg, false);
        else
            pegSet(peg, true);
        
        switch( State )
        {
        case STATE_INIT :
            // disable the selected peg
            pegSet(peg, false);
    
            // save initial layout
            Layout = pegGetLayout();
    
            // determine all future moves
            int count = extractMoves (Layout);
            
            // calculate future move statistics from the initial setup
            endmask = NextMoves.get(0).endmask;
            updateFutureMoves(endmask);
    
            instructionsTextField.setText("select peg to move");
            State = StateType.STATE_CHOOSE;
            break;
    
        case STATE_CHOOSE :
            // check if valid move
            if(!pegGetStatus(peg))
                instructionsTextField.setText("peg not found: select a peg");
            else if(!isMovable(Layout,peg))
                instructionsTextField.setText("not a valid move: select a peg");
            else {
                // highlight chosen peg
                pegMark(peg, true);
                CurrMove.fromPeg = peg;
            
                instructionsTextField.setText("select location to move to");
                State = StateType.STATE_MOVE;
            }
            break;
    
        case STATE_MOVE :
            CurrMove.toPeg = peg;
            
            // check if valid move
            int moveIx = getPegMoveIx(CurrMove.fromPeg, CurrMove.toPeg);
            if (moveIx < 0) {
                instructionsTextField.setText("not a valid move: select a location");
                break;
            }
        
            // it's a possible move, get corresponding jumped peg
            CurrMove.overPeg = PossibleMoves.get(moveIx).overPeg;

            // now check if move is valid for layout
            new_layout = isValidMove(Layout, CurrMove);
            if(new_layout == 0)
                instructionsTextField.setText("not a valid move: select a location");
            else {
                // save board change
                Layout = new_layout;
    
                // unhighlight chosen peg and uncheck its current position
                pegMark(CurrMove.fromPeg, false);
                pegSet (CurrMove.fromPeg, false);
    
                // uncheck the jumped peg
                pegSet(CurrMove.overPeg, false);
    
                // check new location
                pegSet(CurrMove.toPeg, true);
                
                // save move in buffer
                PrevMoves.add(new PegMoveStc(CurrMove));
    
                logMove(CurrMove, false);

                // get the move status following the specified move
                int nextIx = getNext(moveIx);
                endmask = NextMoves.get(nextIx).endmask;
                
                // update future move stats
                updateFutureMoves(endmask);

                instructionsTextField.setText("select next peg to move");
                State = StateType.STATE_CHOOSE;
    
                // check if any valid moves left
                if(anyValidMoves(Layout) == false) {
                    pegs_left = NUMBER_OF_STARTING_PEGS - PrevMoves.size();
                    instructionsTextField.setEnabled(true);
                    instructionsTextField.setText("GAME OVER: you left " + pegs_left + " pegs");
                    if( pegs_left > 8 )
                        pegs_left = 8;
                    ratingTextField.setText( Ratings[pegs_left] );
                    State = StateType.STATE_DONE;
                }
            }
            break;
    
        case STATE_DONE :
        default :
            break;
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainTabbedPane = new javax.swing.JTabbedPane();
        pegBoardPanel = new javax.swing.JPanel();
        undoButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        pegsPanel = new javax.swing.JPanel();
        pegRadioButton12 = new javax.swing.JRadioButton();
        pegRadioButton11 = new javax.swing.JRadioButton();
        pegRadioButton4 = new javax.swing.JRadioButton();
        pegRadioButton13 = new javax.swing.JRadioButton();
        pegRadioButton6 = new javax.swing.JRadioButton();
        pegRadioButton5 = new javax.swing.JRadioButton();
        pegRadioButton2 = new javax.swing.JRadioButton();
        pegRadioButton7 = new javax.swing.JRadioButton();
        pegRadioButton10 = new javax.swing.JRadioButton();
        pegRadioButton3 = new javax.swing.JRadioButton();
        pegRadioButton8 = new javax.swing.JRadioButton();
        pegRadioButton1 = new javax.swing.JRadioButton();
        pegRadioButton15 = new javax.swing.JRadioButton();
        pegRadioButton9 = new javax.swing.JRadioButton();
        pegRadioButton14 = new javax.swing.JRadioButton();
        instructionsTextField = new javax.swing.JTextField();
        ratingTextField = new javax.swing.JTextField();
        countScrollPane = new javax.swing.JScrollPane();
        countTextArea = new javax.swing.JTextArea();
        movesPanel = new javax.swing.JPanel();
        movesScrollPane = new javax.swing.JScrollPane();
        movesTextPane = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("PegGame");

        undoButton.setText("Undo");
        undoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoButtonActionPerformed(evt);
            }
        });

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        pegRadioButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pegRadioButton12ActionPerformed(evt);
            }
        });

        pegRadioButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pegRadioButton11ActionPerformed(evt);
            }
        });

        pegRadioButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pegRadioButton4ActionPerformed(evt);
            }
        });

        pegRadioButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pegRadioButton13ActionPerformed(evt);
            }
        });

        pegRadioButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pegRadioButton6ActionPerformed(evt);
            }
        });

        pegRadioButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pegRadioButton5ActionPerformed(evt);
            }
        });

        pegRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pegRadioButton2ActionPerformed(evt);
            }
        });

        pegRadioButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pegRadioButton7ActionPerformed(evt);
            }
        });

        pegRadioButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pegRadioButton10ActionPerformed(evt);
            }
        });

        pegRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pegRadioButton3ActionPerformed(evt);
            }
        });

        pegRadioButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pegRadioButton8ActionPerformed(evt);
            }
        });

        pegRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pegRadioButton1ActionPerformed(evt);
            }
        });

        pegRadioButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pegRadioButton15ActionPerformed(evt);
            }
        });

        pegRadioButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pegRadioButton9ActionPerformed(evt);
            }
        });

        pegRadioButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pegRadioButton14ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pegsPanelLayout = new javax.swing.GroupLayout(pegsPanel);
        pegsPanel.setLayout(pegsPanelLayout);
        pegsPanelLayout.setHorizontalGroup(
            pegsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pegsPanelLayout.createSequentialGroup()
                .addGroup(pegsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pegsPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(pegRadioButton7)
                        .addGap(0, 0, 0)
                        .addGroup(pegsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(pegRadioButton2)
                            .addComponent(pegRadioButton8))
                        .addGroup(pegsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pegsPanelLayout.createSequentialGroup()
                                .addComponent(pegRadioButton9)
                                .addGap(0, 0, 0)
                                .addComponent(pegRadioButton10))
                            .addComponent(pegRadioButton3)))
                    .addGroup(pegsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pegsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pegsPanelLayout.createSequentialGroup()
                                .addComponent(pegRadioButton11)
                                .addGap(0, 0, 0)
                                .addComponent(pegRadioButton12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(pegsPanelLayout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(pegRadioButton4)))
                        .addGroup(pegsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(pegRadioButton1)
                            .addComponent(pegRadioButton5)
                            .addComponent(pegRadioButton13))
                        .addGroup(pegsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pegsPanelLayout.createSequentialGroup()
                                .addComponent(pegRadioButton14)
                                .addGap(0, 0, 0)
                                .addComponent(pegRadioButton15))
                            .addComponent(pegRadioButton6))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pegsPanelLayout.setVerticalGroup(
            pegsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pegsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pegRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pegsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pegsPanelLayout.createSequentialGroup()
                        .addGroup(pegsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pegRadioButton2)
                            .addComponent(pegRadioButton3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pegsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pegRadioButton5)
                            .addComponent(pegRadioButton4)))
                    .addComponent(pegRadioButton6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pegsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pegRadioButton8)
                    .addComponent(pegRadioButton7)
                    .addComponent(pegRadioButton9)
                    .addComponent(pegRadioButton10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pegsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pegRadioButton14)
                    .addComponent(pegRadioButton15, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pegRadioButton11, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pegRadioButton12, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pegRadioButton13, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        countScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        countScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        countTextArea.setColumns(20);
        countTextArea.setRows(5);
        countScrollPane.setViewportView(countTextArea);

        javax.swing.GroupLayout pegBoardPanelLayout = new javax.swing.GroupLayout(pegBoardPanel);
        pegBoardPanel.setLayout(pegBoardPanelLayout);
        pegBoardPanelLayout.setHorizontalGroup(
            pegBoardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pegBoardPanelLayout.createSequentialGroup()
                .addGap(69, 69, 69)
                .addComponent(undoButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(resetButton)
                .addGap(50, 50, 50))
            .addGroup(pegBoardPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pegBoardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(instructionsTextField)
                    .addGroup(pegBoardPanelLayout.createSequentialGroup()
                        .addComponent(countScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(pegsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 65, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(pegBoardPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ratingTextField)
                .addContainerGap())
        );
        pegBoardPanelLayout.setVerticalGroup(
            pegBoardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pegBoardPanelLayout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addGroup(pegBoardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(countScrollPane)
                    .addGroup(pegBoardPanelLayout.createSequentialGroup()
                        .addComponent(pegsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 98, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(instructionsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ratingTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pegBoardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(undoButton)
                    .addComponent(resetButton))
                .addContainerGap())
        );

        mainTabbedPane.addTab("Play", pegBoardPanel);

        movesScrollPane.setViewportView(movesTextPane);

        javax.swing.GroupLayout movesPanelLayout = new javax.swing.GroupLayout(movesPanel);
        movesPanel.setLayout(movesPanelLayout);
        movesPanelLayout.setHorizontalGroup(
            movesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(movesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
        );
        movesPanelLayout.setVerticalGroup(
            movesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(movesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
        );

        mainTabbedPane.addTab("Moves", movesPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainTabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainTabbedPane)
        );

        mainTabbedPane.getAccessibleContext().setAccessibleName("tabs");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pegRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pegRadioButton1ActionPerformed
        pegMove (1);
    }//GEN-LAST:event_pegRadioButton1ActionPerformed

    private void pegRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pegRadioButton2ActionPerformed
        pegMove (2);
    }//GEN-LAST:event_pegRadioButton2ActionPerformed

    private void pegRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pegRadioButton3ActionPerformed
        pegMove (3);
    }//GEN-LAST:event_pegRadioButton3ActionPerformed

    private void pegRadioButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pegRadioButton5ActionPerformed
        pegMove (5);
    }//GEN-LAST:event_pegRadioButton5ActionPerformed

    private void pegRadioButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pegRadioButton4ActionPerformed
        pegMove (4);
    }//GEN-LAST:event_pegRadioButton4ActionPerformed

    private void pegRadioButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pegRadioButton14ActionPerformed
        pegMove (14);
    }//GEN-LAST:event_pegRadioButton14ActionPerformed

    private void pegRadioButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pegRadioButton7ActionPerformed
        pegMove (7);
    }//GEN-LAST:event_pegRadioButton7ActionPerformed

    private void pegRadioButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pegRadioButton6ActionPerformed
        pegMove (6);
    }//GEN-LAST:event_pegRadioButton6ActionPerformed

    private void pegRadioButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pegRadioButton9ActionPerformed
        pegMove (9);
    }//GEN-LAST:event_pegRadioButton9ActionPerformed

    private void pegRadioButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pegRadioButton8ActionPerformed
        pegMove (8);
    }//GEN-LAST:event_pegRadioButton8ActionPerformed

    private void pegRadioButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pegRadioButton10ActionPerformed
        pegMove (10);
    }//GEN-LAST:event_pegRadioButton10ActionPerformed

    private void pegRadioButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pegRadioButton11ActionPerformed
        pegMove (11);
    }//GEN-LAST:event_pegRadioButton11ActionPerformed

    private void pegRadioButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pegRadioButton12ActionPerformed
        pegMove (12);
    }//GEN-LAST:event_pegRadioButton12ActionPerformed

    private void pegRadioButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pegRadioButton13ActionPerformed
        pegMove (13);
    }//GEN-LAST:event_pegRadioButton13ActionPerformed

    private void pegRadioButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pegRadioButton15ActionPerformed
        pegMove (15);
    }//GEN-LAST:event_pegRadioButton15ActionPerformed

    private void undoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoButtonActionPerformed
        undoLastMove();
    }//GEN-LAST:event_undoButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        initPegs();
    }//GEN-LAST:event_resetButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(mainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(mainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(mainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(mainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new mainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane countScrollPane;
    private javax.swing.JTextArea countTextArea;
    private javax.swing.JTextField instructionsTextField;
    private javax.swing.JTabbedPane mainTabbedPane;
    private javax.swing.JPanel movesPanel;
    private javax.swing.JScrollPane movesScrollPane;
    private javax.swing.JTextPane movesTextPane;
    private javax.swing.JPanel pegBoardPanel;
    private javax.swing.JRadioButton pegRadioButton1;
    private javax.swing.JRadioButton pegRadioButton10;
    private javax.swing.JRadioButton pegRadioButton11;
    private javax.swing.JRadioButton pegRadioButton12;
    private javax.swing.JRadioButton pegRadioButton13;
    private javax.swing.JRadioButton pegRadioButton14;
    private javax.swing.JRadioButton pegRadioButton15;
    private javax.swing.JRadioButton pegRadioButton2;
    private javax.swing.JRadioButton pegRadioButton3;
    private javax.swing.JRadioButton pegRadioButton4;
    private javax.swing.JRadioButton pegRadioButton5;
    private javax.swing.JRadioButton pegRadioButton6;
    private javax.swing.JRadioButton pegRadioButton7;
    private javax.swing.JRadioButton pegRadioButton8;
    private javax.swing.JRadioButton pegRadioButton9;
    private javax.swing.JPanel pegsPanel;
    private javax.swing.JTextField ratingTextField;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton undoButton;
    // End of variables declaration//GEN-END:variables
}
