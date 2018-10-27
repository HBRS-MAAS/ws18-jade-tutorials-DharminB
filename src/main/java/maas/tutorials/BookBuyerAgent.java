package maas.tutorials;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


@SuppressWarnings("serial")
public class BookBuyerAgent extends Agent {
    // total number of books bought
    private int numberOfBooksBought = 0;
    // number of books needed to be bought for the buyer agent to die
    private int mandatoryNumberOfBooksNeeded = 3;
    // The list of known seller agents
    private AID[] sellerAgents;
    // private AID[] sellerAgents = {  new AID("seller1", AID.ISLOCALNAME),
    //                                 new AID("seller2", AID.ISLOCALNAME),
    //                                 new AID("seller3", AID.ISLOCALNAME)};
    protected void setup() {
		// Printout a welcome message
        System.out.println("\tBuyer-agent "+getAID().getLocalName()+" is born.");

        this.publishBuyerAID();
        // wait for sellers to be born
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // get the agent id of all the available sellers
        this.getSellerAID();
        // Get the title of the book to buy as a start-up argument
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String book_title = (String) args[i];
                // System.out.println("\tTrying to buy " + book_title);
                addBehaviour(new  RequestPerformer(book_title));
            }
        }
        else {
            // Make the agent terminate immediately
            System.out.println("\tNo book title specified");
            doDelete();
        }
        // addBehaviour(new shutdown());
    }

    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }

    protected void publishBuyerAID(){
        // Register the book-buying service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-buying");
        sd.setName("JADE-book-trading");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    protected void getSellerAID() {
        // Update the list of seller agents
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-selling");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            sellerAgents = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                sellerAgents[i] = result[i].getName();
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

	/*
	Inner class RequestPerformer.
	This is the behaviour used by Book-buyer agents to request seller
	agents the target book.
	*/
	private class RequestPerformer extends Behaviour {
		private AID bestSeller; // The agent who provides the best offer
		private int bestPrice; // The best offered price
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;
        private String book_title;

        RequestPerformer (String book_title) {
            this.book_title = book_title;
            // System.out.println("inside constructor of RequestPerformer " + this.book_title);
        }

		public void action() {
			switch (step) {
			case 0:
				// Send the cfp to all sellers
                // System.out.println("\t" + myAgent.getLocalName() + " inside step 0 " + this.book_title);
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < sellerAgents.length; ++i) {
					cfp.addReceiver(sellerAgents[i]);
				}
				cfp.setContent(this.book_title);
				cfp.setConversationId("book-trade");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
				MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				// Receive all proposals/refusals from seller agents
                // System.out.println("\t" + myAgent.getLocalName() + " inside step 1 " + this.book_title);
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
                    // System.out.println(reply.getContent());
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						// This is an offer
						int price = Integer.parseInt(reply.getContent());
						if (bestSeller == null || price < bestPrice) {
							// This is the best offer at present
							bestPrice = price;
							bestSeller = reply.getSender();
						}
					}
					repliesCnt++;
					if (repliesCnt >= sellerAgents.length) {
						// We received all replies
						step = 2;
					}
				}
				else {
					block();
				}
				break;
			case 2:
				// Send the purchase order to the seller that provided the best offer
                // System.out.println("\t" + myAgent.getLocalName() + " inside step 2 " + this.book_title);
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(bestSeller);
				order.setContent(this.book_title);
				order.setConversationId("book-trade");
				order.setReplyWith("order"+System.currentTimeMillis());
				myAgent.send(order);
				// Prepare the template to get the purchase order reply
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
				MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
			case 3:
				// Receive the purchase order reply
                // System.out.println("\t" + myAgent.getLocalName() + " inside step 3 " + this.book_title);
				reply = myAgent.receive(mt);
				if (reply != null) {
					// Purchase order reply received
                    // System.out.println(reply.getContent());
					if (reply.getPerformative() == ACLMessage.INFORM 
                            && reply.getContent().equals("Thanks for choosing our services.")) {
						// Purchase successful. We can terminate
						System.out.println("\t" + myAgent.getLocalName() + " purchased "
                                + this.book_title + " from " + reply.getSender().getLocalName());
						// System.out.println("Price = "+bestPrice);
					}
					step = 4;
				}
				else {
					block();
				}
				break;
			}
		}
		public boolean done() {
            if (step == 4) {
                numberOfBooksBought ++;
                if (numberOfBooksBought >= mandatoryNumberOfBooksNeeded) {
                    myAgent.doDelete();
                }
                return true;
            }
            else {
                return false;
            }
		}
	} // End of inner class RequestPerformer


}
