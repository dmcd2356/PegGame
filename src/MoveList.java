/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dmcd2356
 */
public class MoveList {
    
    private int    moveIx;     // the index in the move table corresponding to move
    private int    layout;     // the board layout following the move
    private int    childCnt;   // number of children owned by this node
    private int    level;      // level of this entry (0=init setup, 1 = 1st move, etc)
    private int    count;      // index for parent (identifies which child of parent)
    private MoveList pThis;      // ptr to alternate move to make on this round
    private MoveList pNext;      // ptr to first entry in child list
    private MoveList pLast;      // ptr to last entry in child list
    private MoveList pPrev;      // ptr to prev entry in pThis list
    private MoveList pParent;    // ptr to parent of this move (prev move)

    public MoveList (MoveList apParent, int aLevel, int aCount, int aMoveIx, int aLayout) {
        // set values for this move entry
        moveIx   = aMoveIx;
        layout   = aLayout;
        childCnt = 0;
        level    = aLevel;
        count    = aCount;
        pThis    = null;
        pPrev    = null;
        pNext    = null;
        pLast    = null;
        pParent  = apParent;
    }

    int getLayout () {
        return layout;
    }

    int getMoveIx () {
        return moveIx;
    }

    int getLevel () {
        return level;
    }

    int getChildCnt () {
        return childCnt;
    }

    int getCount () {
        return count;
    }

    void incrChildCnt () {
        childCnt++;
    }

    void setThis (MoveList pMove) {
        pThis = pMove;
    }

    void setNext (MoveList pMove) {
        pNext = pMove;
    }

    void setLast (MoveList pMove) {
        pLast = pMove;
    }

    void setPrev (MoveList pMove) {
        pPrev = pMove;
    }

    MoveList getThis () {
        return pThis;
    }

    MoveList getNext () {
        return pNext;
    }

    MoveList getLast () {
        return pLast;
    }

    MoveList getPrev ()
    {
        return pPrev;
    }

    MoveList getParent () {
        return pParent;
    }
}
