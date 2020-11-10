import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Runner {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter your csv file here: ");
        String inputFile = scan.next();
        BufferedReader csvReader = null;
        BufferedWriter tradeWriter = null;
        BufferedWriter bboWriter = null;

        try {
            // Creating output files
            tradeWriter = new BufferedWriter(new FileWriter("trades.csv"));
            bboWriter = new BufferedWriter(new FileWriter("bbo.csv"));
            // Create columns
            tradeWriter.write("trade_price,trade_size,buy_order_id,sell_order_id\n");
            bboWriter.write("bid_price,bid_size,ask_price,ask_size\n");


            // Reading from input file
            csvReader = new BufferedReader(new FileReader(inputFile));
            OrderQueue bidList = new BidQueue();
            OrderQueue askList = new AskQueue();
            csvReader.readLine();   // Read the column row first
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                String action = data[1];
                String side = "";
                int timestamp = 0;
                int order_id = Integer.parseInt(data[2]);
                int price = 0;
                int size = 0;

                if (action.equals("insert")) {         // Adding order
                    // Get row info
                    side = data[3];
                    timestamp = Integer.parseInt(data[0]);
                    price = Integer.parseInt(data[4]);
                    size = Integer.parseInt(data[5]);

                    // Check for potential trades when inserting new order
                    List<Order> tradedOrders = new ArrayList<>();
                    if (side.equals("buy")) {                           // check if trade can be made with new order
                        tradedOrders = askList.matchOrder(price, size);     // Make any trades that are available
                    } else if (side.equals("sell")) {
                        tradedOrders = bidList.matchOrder(price, size);     // Make any trades that are available
                    }
                    int sizeLeft = tradedOrders.get(0).getSize();
                    tradedOrders.remove(0);

                    // System.out.println("Traded orders:");
                    for (Order o: tradedOrders) {
                        // o.printOrder();
                        String tradeEntry = price + "," + o.getSize() + ",";
                        if (side.equals("buy")) {
                            tradeEntry += order_id +"," + o.getOrder_id() + "\n";
                        } else {
                            tradeEntry += o.getOrder_id() + "," +  order_id + "\n";
                        }
                        System.out.println("tradeEntry");
                        System.out.println(tradeEntry);
                        tradeWriter.write(tradeEntry);
                    }
                    if (sizeLeft > 0) {     // If order is incomplete
                        if (side.equals("buy")) {           // Add to bid list
                            bidList.addOrder(timestamp, order_id, price, sizeLeft);
                        } else if (side.equals("sell")) {   // Add to ask list
                            askList.addOrder(timestamp, order_id, price, sizeLeft);
                        }

                    }
                } else if (action.equals("cancel")) {
                    if (!bidList.removeOrderId(order_id))   // Search bid list then ask list
                        askList.removeOrderId(order_id);
                }

                System.out.println("Ask List***");
                askList.printOrders();
                System.out.println("Bid List***");
                bidList.printOrders();

                int bestBidPrice = bidList.peekPrice();
                int bidSize = bidList.peekSize();
                int bestAskPrice = askList.peekPrice();
                int askSize = askList.peekSize();
                String bboEntry = bestBidPrice + "," + bidSize + "," + bestAskPrice + "," + askSize + "\n";
                System.out.println("bboEntry");
                System.out.println(bboEntry);
                bboWriter.write(bboEntry);
            }
        } catch (Exception e) {
            System.out.println("Error in reading/writing csv!");
            System.out.println(e);
            e.printStackTrace();
        } finally {
            csvReader.close();
            tradeWriter.close();
            bboWriter.close();
        }
    }
}
