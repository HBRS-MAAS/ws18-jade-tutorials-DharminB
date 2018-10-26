package maas.tutorials;
import jade.core.Agent;
import jade.core.behaviours.*;
import java.util.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BookSellerAgent extends Agent {
    private ArrayList books_list;

	protected void setup() {
		// Printout a welcome message
        System.out.println("\tSeller-agent "+getAID().getName()+" is born.");

        // list of books that the seller is offering and their information
        books_list = new ArrayList();

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            for (int i=0; i < args.length; i++) {
                String book_info = (String) args[i];
                String[] book_info_list = book_info.split("_");
                Hashtable book = new Hashtable();
                book.put("title", book_info_list[0]);
                book.put("price", Integer.parseInt(book_info_list[1]));
                book.put("quantity", Integer.parseInt(book_info_list[2]));
                book.put("is_ebook", Boolean.parseBoolean(book_info_list[3]));
                // System.out.println("\tSelling " + book.toString());
                books_list.add(book);
            }
        }
        else {
            // Make the agent terminate immediately
            System.out.println("\tNot selling any book");
            doDelete();
        }
        //System.out.println(books_list.get(0).toString());
		// Add the behaviour serving requests for offer from buyer agents
		addBehaviour(new OfferRequestsServer());
		// Add the behaviour serving purchase orders from buyer agents
		addBehaviour(new PurchaseOrdersServer());
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("\t"+getAID().getLocalName()+" terminating.");
	}

    /*
     * Inner class OfferRequestsServer.
     * This is the behaviour used by Book-seller agents to serve incoming requests
     * for offer from buyer agents.
     * If the requested book is in the local catalogue the seller agent replies
     * with a PROPOSE message specifying the price. Otherwise a REFUSE message is
     * sent back.
     * */
    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            // System.out.println("\tinside OfferRequestsServer action");
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // Message received. Process it
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();
                Integer price = (Integer) this.getPrice(title);
                if (price != -1) {
                    // The requested book is available for sale. Reply with the price
                    // System.out.println("\t" + myAgent.getLocalName() + " making offer for book " + title);
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                }
                else {
                    // The requested book is NOT available for sale.
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
        private int getPrice(String book_name){
            // System.out.println("inside getPrice " + book_name);
            int i;
            for (i = 0; i < books_list.size(); i++) {
                Hashtable book = (Hashtable) books_list.get(i);
                if (book.get("title").equals(book_name)){
                    if ((int) book.get("quantity") > 0) {
                        return (int) book.get("price");
                    }
                    else {
                        return -1;
                    }
                }
            }
            return -1;
        }
    }
    
    /*
     * Inner class PurchaseOrdersServer.
     * This is the behaviour used by Book-seller agents to serve incoming requests
     * for buying a book from buyer agents.
     * If the requested book is in the local catalogue the seller agent replies
     * with a INFORM message thanking the buyer. Otherwise a INFORM message is
     * sent back apologising.
     * */
    private class PurchaseOrdersServer extends CyclicBehaviour {
        public void action() {
            // System.out.println("\tinside PurchaseOrdersServer action");
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // Message received. Process it
                // System.out.println("got accepted");
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();
                // check if the book is still available
                Boolean status = (boolean) this.getAvailability(title);
                if (status) {
                    // The requested book is available for sale. Reply with the INFORM tag
                    // System.out.println("Sold book " + title);
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Thanks for choosing our services.");
                    this.updateInventory(title);
                }
                else {
                    // The requested book is NOT available for sale.
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Sorry, the book is out of stock.");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
        private boolean getAvailability(String book_name){
            // check if book with name "book_name" is available to sell
            // System.out.println("inside getPrice " + book_name);
            int i;
            for (i = 0; i < books_list.size(); i++) {
                Hashtable book = (Hashtable) books_list.get(i);
                if (book.get("title").equals(book_name)){
                    if ((int)book.get("quantity") > 0) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            }
            return false;
        }
        private void updateInventory(String book_name){
            // decrement the quantity of book with title book_name if paperback
            int i;
            for (i = 0; i < books_list.size(); i++) {
                Hashtable book = (Hashtable) books_list.get(i);
                if (book.get("title").equals(book_name) && !(boolean)book.get("is_ebook")){
                    int quantity = (int) book.get("quantity");
                    book.replace("quantity", quantity-1);
                    break;
                }
            }
            // System.out.println(books_list.toString());
        }
    }
}
