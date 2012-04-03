package poker.client;

import java.util.List;

import pulpcore.image.CoreFont;
import pulpcore.sprite.Label;
import pulpcore.util.StringUtil;
import client.Scrollable;

public class PokerChatArea extends Scrollable<ColoredString> {

   protected CoreFont font;

   public PokerChatArea(CoreFont font, int x, int y, int w, int h) {
      super(x, y, w, h);

      this.font = font;

      setMaxLines(100);
   }

   public void addLine(ColoredString msg) {
      addItem(msg);
   }

   @Override
   public void createContent(List<ColoredString> objects) {
      int posY = 0;

      for (ColoredString colStr : objects) {
         String[] text = StringUtil.wordWrap(colStr.getString(), font,
               getAvailableSpace());

         if (text.length == 0) {
            text = new String[] { " " };
         }

         for (int j = 0; j < text.length; j++) {
            String line = StringUtil.replace(text[j], "\t", "   ");

            add(new Label(font.tint(colStr.getColor()), line, 0, posY));

            posY += getLineSpacing();
         }
      }
   }

   @Override
   public int getLineSpacing() {
      return font.getHeight() + 2;
   }
}
