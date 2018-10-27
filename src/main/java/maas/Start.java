package maas;

import java.util.*;
import maas.tutorials.BookBuyerAgent;
import maas.tutorials.BookSellerAgent;

public class Start {
    // list of books in this world
    private String[] books_list = {"bookA", "bookB", "bookC", "bookD", "bookW", "bookX", "bookY", "bookZ" };
    // random number generator
    private Random rand = new Random();

    public static void main(String[] args) {
    	List<String> agents = new Vector<>();
    	// agents.add("buyer1:maas.tutorials.BookBuyerAgent(bookA,bookB,bookX)");
        agents.add("seller1:maas.tutorials.BookSellerAgent(bookA_5_5_false,bookB_7_5_false,bookC_6_5_false,bookD_5_5_false, bookW_7_10000_true,bookX_6_10000_true,bookY_5_10000_true,bookZ_7_10000_true)");
        agents.add("seller2:maas.tutorials.BookSellerAgent(bookA_6_5_false,bookB_5_5_false,bookC_7_5_false,bookD_6_5_false, bookW_5_10000_true,bookX_7_10000_true,bookY_6_10000_true,bookZ_5_10000_true)");
        agents.add("seller3:maas.tutorials.BookSellerAgent(bookA_7_5_false,bookB_6_5_false,bookC_5_5_false,bookD_7_9_false, bookW_6_10000_true,bookX_5_10000_true,bookY_7_10000_true,bookZ_6_10000_true)");

        Start someObjectName = new Start();
        for (int i = 1; i < 21; i++) {
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
        String book4 = this.books_list[this.rand.nextInt(4)+4];
        String buyerString = "buyer" + buyerID + ":maas.tutorials.BookBuyerAgent(" + book1 + "," + book2 + "," + book3 + "," + book4 + ")";
        return buyerString;
    }
}
