package poker.common.model;

import java.util.Arrays;

/**
 * Stores a Hand of Cards (up to a maximum of 7)
 * 
 * @author Aaron Davidson
 */

public class Hand {
   public final static int MAX_CARDS = 7;

   private int[] cards;

   public Hand() {
      cards = new int[MAX_CARDS + 1];
      cards[0] = 0;
   }

   public Hand(Card... cartas) {
      this();
      for (Card c : cartas) {
         addCard(c);
      }
   }

   /**
    * Duplicate an existing hand.
    * 
    * @param h
    *           the hand to clone.
    */
   public Hand(Hand h) {
      cards = new int[MAX_CARDS + 1];
      cards[0] = h.size();
      for (int i = 1; i <= cards[0]; i++)
         cards[i] = h.cards[i];
   }

   /**
    * Get the size of the hand.
    * 
    * @return the number of cards in the hand
    */
   public int size() {
      return cards[0];
   }

   /**
    * Remove the last card in the hand.
    */
   public void removeCard() {
      if (cards[0] > 0) {
         cards[0]--;
      }
   }

   /**
    * Remove the all cards from the hand.
    */
   public void makeEmpty() {
      cards[0] = 0;
   }

   /**
    * Add a card to the hand. (if there is room)
    * 
    * @param c
    *           the card to add
    * @return true if the card was added, false otherwise
    */
   public boolean addCard(Card c) {
      if (c == null) {
         return false;
      }
      if (cards[0] == MAX_CARDS) {
         return false;
      }
      cards[0]++;
      cards[cards[0]] = c.getIndex();
      return true;
   }

   /**
    * Add a card to the hand. (if there is room)
    * 
    * @param i
    *           the index value of the card to add
    * @return true if the card was added, false otherwise
    */
   public boolean addCard(int i) {
      if (cards[0] == MAX_CARDS) {
         return false;
      }
      cards[0]++;
      cards[cards[0]] = i;
      return true;
   }

   /**
    * Get the a specified card in the hand
    * 
    * @param pos
    *           the position (1..n) of the card in the hand
    * @return the card at position pos
    */
   public Card getCard(int pos) {
      if (pos < 1 || pos > cards[0]) {
         return null;
      }
      return new Card(cards[pos]);
   }

   /**
    * Add a card to the hand. (if there is room)
    * 
    * @param c
    *           the card to add
    */
   public void setCard(int pos, Card c) {
      if (cards[0] < pos) {
         return;
      }
      cards[pos] = c.getIndex();
   }

   /**
    * Obtain the array of card indexes for this hand. First element contains the
    * size of the hand.
    * 
    * @return array of card indexs (size = MAX_CARDS+1)
    */
   public int[] getCardArray() {
      return cards;
   }

   public boolean hasCard(Card c) {
      for (int i = 0; i < cards.length; i++) {
         int cIndex = cards[i];
         if (cIndex == c.getIndex()) {
            return true;
         }
      }

      return false;
   }

   /**
    * Bubble Sort the hand to have cards in descending order, but card index.
    * Used for database indexing.
    */
   public void sort() {
      boolean flag = true;
      while (flag) {
         flag = false;
         for (int i = 1; i < cards[0]; i++) {
            if (cards[i] < cards[i + 1]) {
               flag = true;
               int t = cards[i];
               cards[i] = cards[i + 1];
               cards[i + 1] = t;
            }
         }
      }
   }

   /**
    * Get a string representation of this Hand.
    */
   public String toString() {
      String s = "";
      for (int i = 1; i <= cards[0]; i++) {
         s += " " + getCard(i).toString();
      }
      return s;
   }

   /**
    * Get the specified card id
    * 
    * @param pos
    *           the position (1..n) of the card in the hand
    * @return the card at position pos
    */
   public int getCardIndex(int pos) {
      return cards[pos];
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(cards);
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      final Hand other = (Hand) obj;
      if (!Arrays.equals(cards, other.cards))
         return false;
      return true;
   }
}