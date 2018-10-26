package maas;

import java.util.*;
import maas.tutorials.BookBuyerAgent;
import maas.tutorials.BookSellerAgent;

public class Start {
    // list of books in this world
    private String[] books_list = {"bookA", "bookB", "bookC", "bookD", "bookE", "bookF", "bookW", "bookX", "bookY", "bookZ" };
    // random number generator
    private Random rand = new Random();

    public static void main(String[] args) {
    	List<String> agents = new Vector<>();
    	agents.add("buyer1:maas.tutorials.BookBuyerAgent(bookA,bookB,bookX)");
        agents.add("seller1:maas.tutorials.BookSellerAgent(bookA_7_9_false,bookB_10_5_false,bookC_6_3_false,bookD_3_3_false, bookW_4_10000_true,bookX_6_10000_true,bookY_8_10000_true,bookZ_10_10000_true)");
        agents.add("seller2:maas.tutorials.BookSellerAgent(bookB_9_5_false,bookC_9_5_false,bookD_8_5_false,bookE_7_5_false, bookW_4_10000_true,bookX_6_10000_true,bookY_8_10000_true,bookZ_10_10000_true)");
        agents.add("seller3:maas.tutorials.BookSellerAgent(bookC_5_3_false,bookD_4_3_false,bookE_10_5_false,bookF_9_9_false, bookW_4_10000_true,bookX_6_10000_true,bookY_8_10000_true,bookZ_10_10000_true)");

        Start someObjectName = new Start();
        for (int i = 2; i < 21; i++) {
            agents.add(someObjectName.getBuyerAgentInitializationString(i));
        }

    	List<String> cmd = new Vector<>();
    	cmd.add("-agents");
    	StringBuilder sb = new StringBuilder();
    	for (String a : agents) {
    		sb.append(a);
    		sb.append(";");
    	}
    	cmd.add(sb.toString());
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));
    }
    private String getBuyerAgentInitializationString(int buyerID){
        // System.out.println(this.rand.nextInt(this.books_list.length));
        String book1 = this.books_list[this.rand.nextInt(this.books_list.length)];
        String book2 = this.books_list[this.rand.nextInt(this.books_list.length)];
        String book3 = this.books_list[this.rand.nextInt(this.books_list.length)];
        String book4 = this.books_list[this.rand.nextInt(4)+6];
        String buyerString = "buyer" + buyerID + ":maas.tutorials.BookBuyerAgent(" + book1 + "," + book2 + "," + book3 + "," + book4 + ")";
        return buyerString;
    }
}
