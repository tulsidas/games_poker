package poker.server.model;

import java.util.Collections;
import java.util.Comparator;

import poker.common.model.Card;
import poker.common.model.Deck;
import poker.common.model.Hand;

import com.google.common.collect.Lists;

public class HandEvaluator {

    /**
     * Given a hand, return a string naming the hand ('Ace High Flush', etc..)
     */
    public static String nameHand(Hand h) {
        return name_hand(rankHand(h));
    }

    /**
     * Compares two hands against each other.
     * 
     * @param h1
     *            The first hand
     * @param h2
     *            The second hand
     * @return 1 = first hand is best, 2 = second hand is best, 0 = tie
     */
    public static int compareHands(Hand h1, Hand h2) {
        int r1 = rankHand(h1);
        int r2 = rankHand(h2);

        if (r1 > r2) {
            return 1;
        }
        if (r1 < r2) {
            return 2;
        }
        return 0;
    }

    @SuppressWarnings("all")
    public static void main(String[] args) {
        Hand h1 = new Hand();
        Hand h2 = new Hand();

        h1.addCard(new Card("2S"));
        h1.addCard(new Card("3D"));
        h1.addCard(new Card("4H"));
        h1.addCard(new Card("5S"));
        h1.addCard(new Card("6C"));

        h1.addCard(new Card("2H"));
        h1.addCard(new Card("3C"));

        h2.addCard(new Card("2S"));
        h2.addCard(new Card("3D"));
        h2.addCard(new Card("4H"));
        h2.addCard(new Card("5S"));
        h2.addCard(new Card("6C"));

        h2.addCard(new Card("KD"));
        h2.addCard(new Card("QH"));

        // int ret = HandEvaluator.compareHands(h1, h2);
        Comparator<Hand> handComparator = new Comparator<Hand>() {
            public int compare(Hand h1, Hand h2) {
                return HandEvaluator.rankHand(h1) - HandEvaluator.rankHand(h2);
            }
        };

        Hand ganadora = Collections.max(Lists.newArrayList(h1, h2),
                handComparator);

        int i1 = HandEvaluator.rankHand(h1);
        int i2 = HandEvaluator.rankHand(h2);

        Hand besth1 = HandEvaluator.getBest5CardHand(h1);
        Hand besth2 = HandEvaluator.getBest5CardHand(h2);

        int b1 = HandEvaluator.rankHand(besth1);
        int b2 = HandEvaluator.rankHand(besth2);

        h2.toString();
    }

    /**
     * Compares two 5-7 card hands against each other.
     * 
     * @param rank1
     *            The rank of the first hand
     * @param h2
     *            The second hand
     * @return 1 = first hand is best, 2 = second hand is best, 0 = tie
     */
    public int compareHands(int rank1, Hand h2) {
        int r1 = rank1;
        int r2 = rankHand(h2);

        if (r1 > r2) {
            return 1;
        }
        if (r1 < r2) {
            return 2;
        }
        return 0;
    }

    /**
     * Given a board, cache all possible two card combinations of hand ranks, so
     * that lightenting fast hand comparisons may be done later.
     */
    public int[][] getRanks(Hand board) {
        Hand myhand = new Hand(board);
        int[][] rc = new int[52][52];
        int i, j, n1, n2;
        Deck d = new Deck();
        d.extractHand(board);

        // tabulate ranks
        for (i = d.getTopCardIndex(); i < Deck.NUM_CARDS; i++) {
            myhand.addCard(d.getCard(i));
            n1 = d.getCard(i).getIndex();
            for (j = i + 1; j < Deck.NUM_CARDS; j++) {
                myhand.addCard(d.getCard(j));
                n2 = d.getCard(j).getIndex();
                rc[n1][n2] = rc[n2][n1] = rankHand(myhand);
                myhand.removeCard();
            }
            myhand.removeCard();
        }
        return rc;
    }

    /** ******************************************************************* */
    // MORE HAND COMPARISON STUFF (Adapted from C code by Darse Billings)
    /** ******************************************************************* */

    /**
     * Get the best 5 card poker hand from a 7 card hand
     * 
     * @param h
     *            Any 7 card poker hand
     * @return A Hand containing the highest ranked 5 card hand possible from
     *         the input.
     */
    public static Hand getBest5CardHand(Hand h) {
        int[] ch = h.getCardArray();
        int[] bh = new int[6];
        /* int j = */Find_Hand(ch, bh);
        Hand nh = new Hand();
        for (int i = 0; i < 5; i++) {
            nh.addCard(bh[i + 1]);
        }
        return nh;
    }

    private final static int unknown = -1;

    private final static int strflush = 9;

    private final static int quads = 8;

    private final static int fullhouse = 7;

    private final static int flush = 6;

    private final static int straight = 5;

    private final static int trips = 4;

    private final static int twopair = 3;

    private final static int pair = 2;

    private final static int nopair = 1;

    // private final static int highcard = 1;

    /**
     * Get a string from a hand type.
     * 
     * @param handtype
     *            number coding a hand type
     * @return name of hand type
     */
    // private String drb_Name_Hand(int handtype) {
    // switch (handtype) {
    // case -1:
    // return ("Hidden Hand");
    // case 1:
    // return ("High Card");
    // case 2:
    // return ("Pair");
    // case 3:
    // return ("Two Pair");
    // case 4:
    // return ("Three of a Kind");
    // case 5:
    // return ("Straight");
    // case 6:
    // return ("Flush");
    // case 7:
    // return ("Full House");
    // case 8:
    // return ("Four of a Kind");
    // case 9:
    // return ("Straight Flush");
    // default:
    // return ("Very Weird hand indeed");
    // }
    // }
    /* drbcont: want to Find_ the _best_ flush and _best_ strflush ( >9 cards) */

    private static boolean Check_StrFlush(int[] hand, int[] dist, int[] best) {
        int i, j, suit, strght, strtop;
        boolean returnvalue;
        int[] suitvector = new int[14];
        /*
         * _23456789TJQKA boolean vector 01234567890123 indexing
         */

        returnvalue = false; /* default */

        /* do flat distribution of whole suits (cdhs are 0123 respectively) */

        for (suit = 0; suit <= 3; suit++) {

            /* explicitly initialize suitvector */
            suitvector[0] = 13;
            for (i = 1; i <= suitvector[0]; i++) {
                suitvector[i] = 0;
            }
            for (i = 1; i <= hand[0]; i++) {
                if ((hand[i] != unknown) && ((hand[i] / 13) == suit)) {
                    suitvector[(hand[i] % 13) + 1] = 1;
                }
            }

            /* now look for straights */
            if (suitvector[13] >= 1) {
                // Ace low straight
                strght = 1;
            }
            else {
                strght = 0;
            }
            strtop = 0;

            for (i = 1; i <= 13; i++) {
                if (suitvector[i] >= 1) {
                    strght++;
                    if (strght >= 5) {
                        strtop = i - 1;
                    }
                }
                else
                    strght = 0;
            }

            /* determine if there was a straight flush and copy it to best[] */

            if (strtop > 0) { /* no 2-high straight flushes */
                for (j = 1; j <= 5; j++) {
                    best[j] = ((13 * suit) + strtop + 1 - j);
                }
                /* Adjust for case of Ace low (five high) straight flush */
                if (strtop == 3) {
                    best[5] = best[5] + 13;
                }
                returnvalue = true;
            }
        }
        return (returnvalue);
    }

    private static void Find_Quads(int[] hand, int[] dist, int[] best) {
        int i, j, quadrank = 0, kicker;

        /* find rank of largest quads */
        for (i = 1; i <= 13; i++) {
            if (dist[i] >= 4) {
                quadrank = i - 1;
            }
        }

        /* copy those quads */
        i = 1; /* position in hand[] */
        j = 1; /* position in best[] */
        while (j <= 4) { /* assume all four will be found before i > hand[0] */
            if ((hand[i] != unknown) && ((hand[i] % 13) == quadrank)) {
                best[j] = hand[i];
                j++;
            }
            i++;
        }

        /* find best kicker */
        kicker = unknown; /* default is unknown kicker */
        for (i = 1; i <= 13; i++) { /* find rank of largest kicker */
            if ((dist[i] >= 1) && ((i - 1) != quadrank)) {
                kicker = i - 1;
            }
        }

        /* copy kicker */
        if (kicker != unknown) {
            i = 1; /* position in hand[] */
            while (j <= 5) { /* assume kicker will be found before i > hand[0] */
                if ((hand[i] != unknown) && ((hand[i] % 13) == kicker)) {
                    best[j] = hand[i];
                    j++;
                }
                i++;
            }
        }
        else {
            best[j] = unknown;
            j++;
        }
    }

    private static void Find_FullHouse(int[] hand, int[] dist, int[] best) {
        int i, j, tripsrank = 0, pairrank = 0;

        /* find rank of largest trips */
        for (i = 1; i <= 13; i++) {
            if (dist[i] >= 3) {
                tripsrank = i - 1;
            }
        }

        /* copy those trips */
        i = 1; /* position in hand[] */
        j = 1; /* position in best[] */
        while (j <= 3) { /* assume all three will be found before i > hand[0] */
            if ((hand[i] != unknown) && ((hand[i] % 13) == tripsrank)) {
                best[j] = hand[i];
                j++;
            }
            i++;
        }

        /* find best pair */
        i = 13;
        pairrank = -1;
        while (pairrank < 0) { /* assume kicker will be found before i = 0 */
            if ((dist[i] >= 2) && ((i - 1) != tripsrank)) {
                pairrank = i - 1;
            }
            else {
                i--;
            }
        }

        /* copy best pair */
        i = 1; /* position in hand[] */
        while (j <= 5) { /* assume pair will be found before i > hand[0] */
            if ((hand[i] != unknown) && ((hand[i] % 13) == pairrank)) {
                best[j] = hand[i];
                j++;
            }
            i++;
        }
    }

    private static void Find_Flush(int[] hand, int[] dist, int[] best) { // /
        // finds only the best flush in highest suit
        int i, j, flushsuit = 0;
        int[] suitvector = new int[14];
        /*
         * _23456789TJQKA boolean vector 01234567890123 indexing
         */
        /* find flushsuit */
        for (i = 14; i <= 17; i++) {
            if (dist[i] >= 5) {
                flushsuit = i - 14;
            }
        }

        /* explicitly initialize suitvector */
        suitvector[0] = 13;
        for (i = 1; i <= suitvector[0]; i++) {
            suitvector[i] = 0;
        }

        /* do flat distribution of whole flushsuit */
        for (i = 1; i <= hand[0]; i++) {
            if ((hand[i] != unknown) && ((hand[i] / 13) == flushsuit)) {
                suitvector[(hand[i] % 13) + 1] = 1;
            }
        }

        /* determine best five cards in flushsuit */
        i = 13;
        j = 1;
        while (j <= 5) { /*
                          * assume all five flushcards will be found before i <
                          * 1
                          */
            if (suitvector[i] >= 1) {
                best[j] = (13 * flushsuit) + i - 1;
                j++;
            }
            i--;
        }
    }

    private static void Find_Straight(int[] hand, int[] dist, int[] best) {
        int i, j, strght, strtop;

        /* look for highest straight */
        if (dist[13] >= 1) {
            // Ace low straight
            strght = 1;
        }
        else {
            strght = 0;
        }
        strtop = 0;

        for (i = 1; i <= 13; i++) {
            if (dist[i] >= 1) {
                strght++;
                if (strght >= 5) {
                    strtop = i - 1;
                }
            }
            else
                strght = 0;
        }

        /* copy the highest straight */
        if (strtop > 3) { /* note: different extraction from others */
            for (j = 1; j <= 5; j++) {
                for (i = 1; i <= hand[0]; i++) {
                    if ((hand[i] != unknown)
                            && (hand[i] % 13 == (strtop + 1 - j))) {
                        best[j] = hand[i];
                    }
                }
            }
        }
        else if (strtop == 3) {
            for (j = 1; j <= 4; j++) {
                for (i = 1; i <= hand[0]; i++) {
                    if ((hand[i] != unknown)
                            && (hand[i] % 13 == (strtop + 1 - j))) {
                        best[j] = hand[i];
                    }
                }
            }
            for (i = 1; i <= hand[0]; i++) { /* the Ace in a low straight */
                if ((hand[i] != unknown) && (hand[i] % 13 == 12)) {
                    best[5] = hand[i];
                }
            }
        }
    }

    private static void Find_Trips(int[] hand, int[] dist, int[] best) {
        int i, j, tripsrank = 0, kicker1, kicker2;

        /* find rank of largest trips */
        for (i = 1; i <= 13; i++) {
            if (dist[i] >= 3) {
                tripsrank = i - 1;
            }
        }

        /* copy those trips */
        i = 1; /* position in hand[] */
        j = 1; /* position in best[] */
        while (j <= 3) { /* assume all three will be found before i > hand[0] */
            if ((hand[i] != unknown) && ((hand[i] % 13) == tripsrank)) {
                best[j] = hand[i];
                j++;
            }
            i++;
        }

        /* find best kickers */
        kicker1 = unknown; /* default is unknown kicker */
        for (i = 1; i <= 13; i++) { /* find rank of largest kicker */
            if ((dist[i] >= 1) && ((i - 1) != tripsrank)) {
                kicker1 = i - 1;
            }
        }

        kicker2 = unknown;
        for (i = 1; i <= kicker1; i++) { /* find rank of second kicker */
            if ((dist[i] >= 1) && ((i - 1) != tripsrank)) {
                kicker2 = i - 1;
            }
        }

        /* copy kickers */
        if (kicker1 != unknown) {
            i = 1; /* position in hand[] */
            while (j <= 4) { /* assume kicker1 will be found before i > hand[0] */
                if ((hand[i] != unknown) && ((hand[i] % 13) == kicker1)) {
                    best[j] = hand[i];
                    j++;
                }
                i++;
            }
        }
        else {
            best[j] = unknown;
            j++;
        }

        if (kicker2 != unknown) {
            i = 1; /* position in hand[] */
            while (j <= 5) { /* assume kicker2 will be found before i > hand[0] */
                if ((hand[i] != unknown) && ((hand[i] % 13) == kicker2)) {
                    best[j] = hand[i];
                    j++;
                }
                i++;
            }
        }
        else {
            best[j] = unknown;
            j++;
        }
    }

    private static void Find_TwoPair(int[] hand, int[] dist, int[] best) {
        int i, j, pairrank1 = 0, pairrank2 = 0, kicker;

        /* find rank of largest pair */
        for (i = 1; i <= 13; i++) {
            if (dist[i] >= 2) {
                pairrank1 = i - 1;
            }
        }
        /* find rank of second largest pair */
        for (i = 1; i <= 13; i++) {
            if ((dist[i] >= 2) && ((i - 1) != pairrank1)) {
                pairrank2 = i - 1;
            }
        }

        /* copy those pairs */
        i = 1; /* position in hand[] */
        j = 1; /* position in best[] */
        while (j <= 2) { /* assume both will be found before i > hand[0] */
            if ((hand[i] != unknown) && ((hand[i] % 13) == pairrank1)) {
                best[j] = hand[i];
                j++;
            }
            i++;
        }
        i = 1; /* position in hand[] */
        while (j <= 4) { /* assume both will be found before i > hand[0] */
            if ((hand[i] != unknown) && ((hand[i] % 13) == pairrank2)) {
                best[j] = hand[i];
                j++;
            }
            i++;
        }

        /* find best kicker */
        kicker = unknown; /* default is unknown kicker */
        for (i = 1; i <= 13; i++) { /* find rank of largest kicker */
            if ((dist[i] >= 1) && ((i - 1) != pairrank1)
                    && ((i - 1) != pairrank2)) {
                kicker = i - 1;
            }
        }

        /* copy kicker */
        if (kicker != unknown) {
            i = 1; /* position in hand[] */
            while (j <= 5) { /* assume kicker will be found before i > hand[0] */
                if ((hand[i] != unknown) && ((hand[i] % 13) == kicker)) {
                    best[j] = hand[i];
                    j++;
                }
                i++;
            }
        }
        else {
            best[j] = unknown;
            j++;
        }
    }

    private static void Find_Pair(int[] hand, int[] dist, int[] best) {
        int i, j, pairrank = 0, kicker1, kicker2, kicker3;

        /* find rank of largest pair */
        for (i = 1; i <= 13; i++) {
            if (dist[i] >= 2) {
                pairrank = i - 1;
            }
        }

        /* copy that pair */
        i = 1; /* position in hand[] */
        j = 1; /* position in best[] */
        while (j <= 2) { /* assume both will be found before i > hand[0] */
            if ((hand[i] != unknown) && ((hand[i] % 13) == pairrank)) {
                best[j] = hand[i];
                j++;
            }
            i++;
        }

        /* find best kickers */
        kicker1 = unknown; /* default is unknown kicker */
        for (i = 1; i <= 13; i++) { /* find rank of largest kicker */
            if ((dist[i] >= 1) && ((i - 1) != pairrank)) {
                kicker1 = i - 1;
            }
        }
        kicker2 = unknown;
        for (i = 1; i <= kicker1; i++) { /* find rank of second kicker */
            if ((dist[i] >= 1) && ((i - 1) != pairrank)) {
                kicker2 = i - 1;
            }
        }
        kicker3 = unknown;
        for (i = 1; i <= kicker2; i++) { /* find rank of third kicker */
            if ((dist[i] >= 1) && ((i - 1) != pairrank)) {
                kicker3 = i - 1;
            }
        }

        /* copy kickers */
        if (kicker1 != unknown) {
            i = 1; /* position in hand[] */
            while (j <= 3) { /* assume kicker1 will be found before i > hand[0] */
                if ((hand[i] != unknown) && ((hand[i] % 13) == kicker1)) {
                    best[j] = hand[i];
                    j++;
                }
                i++;
            }
        }
        else {
            best[j] = unknown;
            j++;
        }

        if (kicker2 != unknown) {
            i = 1; /* position in hand[] */
            while (j <= 4) { /* assume kicker2 will be found before i > hand[0] */
                if ((hand[i] != unknown) && ((hand[i] % 13) == kicker2)) {
                    best[j] = hand[i];
                    j++;
                }
                i++;
            }
        }
        else {
            best[j] = unknown;
            j++;
        }

        if (kicker3 != unknown) {
            i = 1; /* position in hand[] */
            while (j <= 5) { /* assume kicker3 will be found before i > hand[0] */
                if ((hand[i] != unknown) && ((hand[i] % 13) == kicker3)) {
                    best[j] = hand[i];
                    j++;
                }
                i++;
            }
        }
        else {
            best[j] = unknown;
            j++;
        }
    }

    private static void Find_NoPair(int[] hand, int[] dist, int[] best) {
        int i, j, kicker1, kicker2, kicker3, kicker4, kicker5;

        /* find best kickers */
        kicker1 = unknown; /* default is unknown kicker */
        for (i = 1; i <= 13; i++) { /* find rank of largest kicker */
            if (dist[i] >= 1) {
                kicker1 = i - 1;
            }
        }
        kicker2 = unknown;
        for (i = 1; i <= kicker1; i++) { /* find rank of second kicker */
            if (dist[i] >= 1) {
                kicker2 = i - 1;
            }
        }
        kicker3 = unknown;
        for (i = 1; i <= kicker2; i++) { /* find rank of third kicker */
            if (dist[i] >= 1) {
                kicker3 = i - 1;
            }
        }
        kicker4 = unknown;
        for (i = 1; i <= kicker3; i++) { /* find rank of fourth kicker */
            if (dist[i] >= 1) {
                kicker4 = i - 1;
            }
        }
        kicker5 = unknown;
        for (i = 1; i <= kicker4; i++) { /* find rank of fifth kicker */
            if (dist[i] >= 1) {
                kicker5 = i - 1;
            }
        }

        /* copy kickers */
        j = 1; /* position in best[] */

        if (kicker1 != unknown) {
            i = 1; /* position in hand[] */
            while (j <= 1) { /* assume kicker1 will be found before i > hand[0] */
                if ((hand[i] != unknown) && ((hand[i] % 13) == kicker1)) {
                    best[j] = hand[i];
                    j++;
                }
                i++;
            }
        }
        else {
            best[j] = unknown;
            j++;
        }

        if (kicker2 != unknown) {
            i = 1; /* position in hand[] */
            while (j <= 2) { /* assume kicker2 will be found before i > hand[0] */
                if ((hand[i] != unknown) && ((hand[i] % 13) == kicker2)) {
                    best[j] = hand[i];
                    j++;
                }
                i++;
            }
        }
        else {
            best[j] = unknown;
            j++;
        }

        if (kicker3 != unknown) {
            i = 1; /* position in hand[] */
            while (j <= 3) { /* assume kicker3 will be found before i > hand[0] */
                if ((hand[i] != unknown) && ((hand[i] % 13) == kicker3)) {
                    best[j] = hand[i];
                    j++;
                }
                i++;
            }
        }
        else {
            best[j] = unknown;
            j++;
        }

        if (kicker4 != unknown) {
            i = 1; /* position in hand[] */
            while (j <= 4) { /* assume kicker4 will be found before i > hand[0] */
                if ((hand[i] != unknown) && ((hand[i] % 13) == kicker4)) {
                    best[j] = hand[i];
                    j++;
                }
                i++;
            }
        }
        else {
            best[j] = unknown;
            j++;
        }

        if (kicker5 != unknown) {
            i = 1; /* position in hand[] */
            while (j <= 5) { /* assume kicker5 will be found before i > hand[0] */
                if ((hand[i] != unknown) && ((hand[i] % 13) == kicker5)) {
                    best[j] = hand[i];
                    j++;
                }
                i++;
            }
        }
        else {
            best[j] = unknown;
            j++;
        }
    }

    private static int Find_Hand(int[] hand, int[] best) {
        // -1 means unknown card
        int i, card, rank, suit, hand_type, rankmax1, rankmax2, flushmax, strght, strmax;
        int[] dist = new int[18];
        /*
         * _23456789TJQKAcdhs distribution vector 012345678901234567 indexing
         */

        /* explicitly initialize distribution vector */
        dist[0] = 17;
        for (i = 1; i <= dist[0]; i++) {
            dist[i] = 0;
        }

        for (i = 1; i <= hand[0]; i++) {
            if (hand[i] != unknown) {
                card = hand[i];
                rank = card % 13;
                suit = card / 13;

                if (!((rank < 0) || (rank > 12))) {
                    dist[rank + 1]++;
                }

                if (!((suit < 0) || (suit > 3))) {
                    dist[suit + 14]++;
                }
            }
        }

        /* scan the distribution array for maximums */
        rankmax1 = 0;
        rankmax2 = 0;
        flushmax = 0;
        strmax = 0;

        if (dist[13] >= 1) {
            strght = 1;
        }
        else
            strght = 0; /* Ace low straight */

        for (i = 1; i <= 13; i++) {
            if (dist[i] > rankmax1) {
                rankmax2 = rankmax1;
                rankmax1 = dist[i];
            }
            else if (dist[i] > rankmax2) {
                rankmax2 = dist[i];
            }

            if (dist[i] >= 1) {
                strght++;
                if (strght > strmax) {
                    strmax = strght;
                }
            }
            else
                strght = 0;
        }

        for (i = 14; i <= 17; i++) {
            if (dist[i] > flushmax) {
                flushmax = dist[i];
            }
        }

        hand_type = unknown;

        if ((flushmax >= 5) && (strmax >= 5)) {
            if (Check_StrFlush(hand, dist, best)) {
                hand_type = strflush;
            }
            else {
                hand_type = flush;
                Find_Flush(hand, dist, best);
            }
        }
        else if (rankmax1 >= 4) {
            hand_type = quads;
            Find_Quads(hand, dist, best);
        }
        else if ((rankmax1 >= 3) && (rankmax2 >= 2)) {
            hand_type = fullhouse;
            Find_FullHouse(hand, dist, best);
        }
        else if (flushmax >= 5) {
            hand_type = flush;
            Find_Flush(hand, dist, best);
        }
        else if (strmax >= 5) {
            hand_type = straight;
            Find_Straight(hand, dist, best);
        }
        else if (rankmax1 >= 3) {
            hand_type = trips;
            Find_Trips(hand, dist, best);
        }
        else if ((rankmax1 >= 2) && (rankmax2 >= 2)) {
            hand_type = twopair;
            Find_TwoPair(hand, dist, best);
        }
        else if (rankmax1 >= 2) {
            hand_type = pair;
            Find_Pair(hand, dist, best);
        }
        else {
            hand_type = nopair;
            Find_NoPair(hand, dist, best);
        }

        return (hand_type);
    }

    /** ******************************************************************* */
    // DENIS PAPP'S HAND RANK IDENTIFIER CODE:
    /** ******************************************************************* */

    private static final int POKER_HAND = 5;

    public static final int HIGH = 0;

    public static final int PAIR = 1;

    public static final int TWOPAIR = 2;

    public static final int THREEKIND = 3;

    public static final int STRAIGHT = 4;

    public static final int FLUSH = 5;

    public static final int FULLHOUSE = 6;

    public static final int FOURKIND = 7;

    public static final int STRAIGHTFLUSH = 8;

    public static final int FIVEKIND = 9;

    public static final int NUM_HANDS = 10;

    private static final int NUM_RANKS = 13;

    private static final int ID_GROUP_SIZE = (Card.NUM_RANKS * Card.NUM_RANKS
            * Card.NUM_RANKS * Card.NUM_RANKS * Card.NUM_RANKS);

    private final static byte ID_ExistsStraightFlush(Hand h, byte major_suit) {
        boolean[] present = new boolean[Card.NUM_RANKS];
        // for (i=0;i<Card.NUM_RANKS;i++) present[i]=false;

        for (int i = 0; i < h.size(); i++) {
            int cind = h.getCardIndex(i + 1);
            if (Card.getSuit(cind) == major_suit) {
                present[Card.getRank(cind)] = true;
            }
        }
        int straight = present[Card.ACE] ? 1 : 0;
        byte high = 0;
        for (int i = 0; i < Card.NUM_RANKS; i++) {
            if (present[i]) {
                if ((++straight) >= POKER_HAND) {
                    high = (byte) i;
                }
            }
            else {
                straight = 0;
            }
        }
        return high;
    }

    // suit: Card.NUM_SUITS means any
    // not_allowed: Card.NUM_RANKS means any
    // returns ident value
    private final static int ID_KickerValue(byte[] paired, int kickers,
            byte[] not_allowed) {
        int i = Card.ACE;
        int value = 0;
        while (kickers != 0) {
            while (paired[i] == 0 || i == not_allowed[0] || i == not_allowed[1]) {
                i--;
            }
            kickers--;
            value += pow(Card.NUM_RANKS, kickers) * i;
            i--;
        }
        return value;
    }

    private final static int ID_KickerValueSuited(Hand h, int kickers, byte suit) {
        int i;
        int value = 0;

        boolean[] present = new boolean[Card.NUM_RANKS];
        // for (i=0;i<Card.NUM_RANKS;i++) present[i] = false;

        for (i = 0; i < h.size(); i++)
            if (h.getCard(i + 1).getSuit() == suit)
                present[h.getCard(i + 1).getRank()] = true;

        i = Card.ACE;
        while (kickers != 0) {
            while (present[i] == false)
                i--;
            kickers--;
            value += pow(Card.NUM_RANKS, kickers) * i;
            i--;
        }
        return value;
    }

    /**
     * Get a numerical ranking of this hand. Uses java based code, so may be
     * slower than using the native methods, but is more compatible this way.
     * 
     * Based on Denis Papp's Loki Hand ID code (id.cpp) Given a 1-9 card hand,
     * will return a unique rank such that any two hands will be ranked with the
     * better hand having a higher rank.
     * 
     * @param h
     *            a 1-9 card hand
     * @return a unique number representing the hand strength of the best 5-card
     *         poker hand in the given 7 cards. The higher the number, the
     *         better the hand is.
     */
    public final static int rankHand(Hand h) {
        boolean straight = false;
        boolean flush = false;
        byte maxHand = (byte) (h.size() >= POKER_HAND ? POKER_HAND : h.size());
        byte rank, suit;

        // pair data

        // array to track the groups or cards in your hand
        byte[] groupSize = new byte[POKER_HAND + 1];

        // array to track paired cards
        byte[] paired = new byte[Card.NUM_RANKS];

        // array to track the rank of our pairs
        byte[][] pairRank = new byte[POKER_HAND + 1][2];

        // straight
        byte straightHigh = 0; // track the high card (rank) of our straight
        byte straightSize;

        // flush
        byte[] suitSize = new byte[Card.NUM_SUITS];
        byte majorSuit = 0;

        // determine pairs, dereference order data, check flush
        // for (r=0;r<Card.NUM_RANKS;r++) paired[r] = 0;
        // for (r=0;r<Card.NUM_SUITS;r++) suit_size[r] = 0;
        // for (r=0;r<=POKER_HAND;r++) group_size[r] = 0;
        for (int r = 0; r < h.size(); r++) {
            int cind = h.getCardIndex(r + 1);

            rank = (byte) Card.getRank(cind);
            suit = (byte) Card.getSuit(cind);

            // Add rank of card to paired array to track the pairs we have.
            paired[rank]++;

            // keep track of the groups in our hand
            // (1-pair, 2-pair, 1-trips, 1-trips 1-pair)
            groupSize[paired[rank]]++;

            // To prevent looking at group_size[-1], which would be bad.
            if (paired[rank] != 0) {
                // Decrease the previous group by one. group_size[0] should end
                // up
                // at -5.
                groupSize[paired[rank] - 1]--;
            }

            if ((++suitSize[suit]) >= POKER_HAND) {
                // Add suit to suit array, then check for a flush.
                flush = true;
                majorSuit = suit;
            }
        }
        // Card.ACE low? Add to straight_size if so.
        straightSize = (byte) (paired[Card.ACE] != 0 ? 1 : 0);

        for (int i = 0; i < (POKER_HAND + 1); i++) {
            pairRank[i][0] = (byte) Card.NUM_RANKS;
            pairRank[i][1] = (byte) Card.NUM_RANKS;
        }

        // check for straight and pair data
        // Start at the Deuce. straight_size = 1 if we have an ace.
        for (int r = 0; r < Card.NUM_RANKS; r++) {
            // check straight
            if (paired[r] != 0) {
                if ((++straightSize) >= POKER_HAND) { // Do we have five cards
                                                      // in a
                    // row (a straight!)
                    straight = true; // We sure do.
                    straightHigh = (byte) r; // Keep track of that high card
                }
            }
            else { // Missing a card for our straight. start the count over.
                straightSize = 0;
            }
            // get pair ranks, keep two highest of each
            int cardTimes = paired[r];
            if (cardTimes != 0) {
                pairRank[cardTimes][1] = pairRank[cardTimes][0];
                pairRank[cardTimes][0] = (byte) r;
            }
        }

        // now id type
        int ident;

        if (groupSize[POKER_HAND] != 0) { // we have five cards of the same rank
            // in our hand.
            ident = FIVEKIND * ID_GROUP_SIZE; // must have five of a kind !!
            ident += pairRank[POKER_HAND][0];
            return ident;
        }

        if (straight && flush) {
            byte hi = ID_ExistsStraightFlush(h, majorSuit);
            if (hi > 0) {
                ident = STRAIGHTFLUSH * ID_GROUP_SIZE;
                ident += hi;
                return ident;
            }
        }

        if (groupSize[4] != 0) {
            ident = FOURKIND * ID_GROUP_SIZE;
            ident += pairRank[4][0] * Card.NUM_RANKS;
            pairRank[4][1] = (byte) Card.NUM_RANKS; // just in case 2 sets quads
            ident += ID_KickerValue(paired, 1, pairRank[4]);
        }
        else if (groupSize[3] >= 2) {
            ident = FULLHOUSE * ID_GROUP_SIZE;
            ident += pairRank[3][0] * Card.NUM_RANKS;
            ident += pairRank[3][1];
        }
        else if (groupSize[3] == 1 && groupSize[2] != 0) {
            ident = FULLHOUSE * ID_GROUP_SIZE;
            ident += pairRank[3][0] * Card.NUM_RANKS;
            ident += pairRank[2][0];
        }
        else if (flush) {
            ident = FLUSH * ID_GROUP_SIZE;
            ident += ID_KickerValueSuited(h, 5, majorSuit);
        }
        else if (straight) {
            ident = STRAIGHT * ID_GROUP_SIZE;
            ident += straightHigh;
        }
        else if (groupSize[3] == 1) {
            ident = THREEKIND * ID_GROUP_SIZE;
            ident += pairRank[3][0] * Card.NUM_RANKS * Card.NUM_RANKS;
            ident += ID_KickerValue(paired, maxHand - 3, pairRank[3]);
        }
        else if (groupSize[2] >= 2) { // TWO PAIR
            ident = TWOPAIR * ID_GROUP_SIZE;
            ident += pairRank[2][0] * Card.NUM_RANKS * Card.NUM_RANKS;
            ident += pairRank[2][1] * Card.NUM_RANKS;
            ident += ID_KickerValue(paired, maxHand - 4, pairRank[2]);
        }
        else if (groupSize[2] == 1) { // A PAIR
            ident = PAIR * ID_GROUP_SIZE;
            ident += pairRank[2][0] * Card.NUM_RANKS * Card.NUM_RANKS
                    * Card.NUM_RANKS;
            ident += ID_KickerValue(paired, maxHand - 2, pairRank[2]);
        }
        else { // A Low
            ident = HIGH * ID_GROUP_SIZE;
            ident += ID_KickerValue(paired, maxHand, pairRank[2]);
        }
        return ident;
    }

    private static int pow(int n, int p) {
        int res = 1;
        while (p-- > 0) {
            res *= n;
        }
        return res;
    }

    private static final String[] hand_name = { "HIGH", "PAIR", "TWO PAIR",
            "THREE KIND", "STRAIGHT", "FLUSH", "FULL HOUSE", "FOUR KIND",
            "STRAIGHT FLUSH", "FIVE KIND" };

    private static final String[] rank_sing = { "dos", "tres", "cuatro",
            "cinco", "seis", "siete", "ocho", "nueve", "diez", "J", "Q", "K",
            "as" };

    private static final String[] rank_plural = { "dos", "tres", "cuatros",
            "cincos", "seis", "sietes", "ochos", "nueves", "dieces", "J", "Q",
            "K", "ases" };

    /**
     * Return a string naming the hand
     * 
     * @param rank
     *            calculated by rankHand_java()
     */
    private static String name_hand(int rank) {

      int type = (int) (rank / ID_GROUP_SIZE);
      int ident = (int) (rank % ID_GROUP_SIZE), ident2;

      String t = "";

      switch (type) {
      case HIGH:
         ident /= NUM_RANKS * NUM_RANKS * NUM_RANKS * NUM_RANKS;
         t = rank_sing[ident];
         break;
      case FLUSH:
         ident /= NUM_RANKS * NUM_RANKS * NUM_RANKS * NUM_RANKS;
         t = "Color al " + rank_sing[ident];
         break;
      case PAIR:
         ident /= NUM_RANKS * NUM_RANKS * NUM_RANKS;
         t = "Par de " + rank_plural[ident];
         break;
      case TWOPAIR:
         ident2 = ident / (NUM_RANKS * NUM_RANKS);
         ident = (ident % (NUM_RANKS * NUM_RANKS)) / NUM_RANKS;
         t = "Par doble de " + rank_plural[ident2] + " y " + rank_plural[ident];
         break;
      case THREEKIND:
         t = "Pierna de " + rank_plural[ident / (NUM_RANKS * NUM_RANKS)];
         break;
      case FULLHOUSE:
         t = "Full de  " + rank_plural[ident / NUM_RANKS] + " y "
               + rank_plural[ident % NUM_RANKS];
         break;
      case FOURKIND:
         t = "Poker de " + rank_plural[ident / NUM_RANKS];
         break;
      case STRAIGHT:
         t = "Escalera al " + rank_sing[ident];
         break;
      case STRAIGHTFLUSH:
          if (rank_sing[ident].equals("as")) {
              t = "Escalera REAL";
          }
          else {
              t = "Escalera color al " + rank_sing[ident];
          }
         break;
      case FIVEKIND:
         t = "Flor slivestre!";
         break;
      default:
         t = hand_name[type];
      }

      return t;
   }
}