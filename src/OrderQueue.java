import java.util.ArrayList;
import java.util.List;

public class OrderQueue {
    protected SamePriceOrders head;

    public OrderQueue() {
        head = null;
    }

    public int peekPrice() {
        if (head == null) {
            return 0;
        }
        return head.getPrice();
    }

    public int peekSize() {
        if (head == null)
            return 0;
        return head.getSize();
    }

    public void addOrder(int timestamp, int order_id, int price, int size) {}

    public List<Order> matchOrder(int price, int size) {
        return new ArrayList<Order>();
    }

    public boolean removeOrderId(int id) {
        if (head == null)
            return false;
        SamePriceOrders ptr = head;
        while (ptr != null) {
            if (ptr.removeOrderId(id) >= 0) {
                if (ptr.getSize() == 0) {       // Remove pointer if price block empty
                    removePtr(ptr);
                }
                return true;
            }
            ptr = ptr.next;
        }
        return false;
    }

    protected void removePtr(SamePriceOrders target) {
        if (head == target) {
            head = head.next;
        } else {
            SamePriceOrders ptr = head;
            SamePriceOrders prev = null;
            while (ptr != null) {
                if (ptr == target) {
                    prev.next = ptr.next;
                    return;
                }
                prev = ptr;
                ptr = ptr.next;
            }
        }
    }

    public void printOrders() {
        if (head == null)
            System.out.println("Empty queue");
        else {
            SamePriceOrders ptr = head;
            while (ptr != null) {
                System.out.println("-- price " + ptr.getPrice() + " -------------------------------------------------------------------");
                ptr.printOrders();
                ptr = ptr.next;
            }
        }

    }


}

class BidQueue extends OrderQueue {
    BidQueue() {
        super();
    }

    public List<Order> matchOrder(int price, int size) {
        SamePriceOrders ptr = super.head;
        List<Order> tradedOrders = new ArrayList<>();
        while (ptr != null && size != 0) {
            if (ptr.getPrice() >= price) {      // If prices match make trade
                List<Order> current = ptr.tradeShares(size);
                size = current.get(0).getSize();
                current.remove(0);
                // Save all successful orders
                for (Order o: current) {
                    tradedOrders.add(o);
                }

                if (ptr.getSize() == 0) {
                    SamePriceOrders removingPrice = ptr;
                    ptr = ptr.next;         // Set next before removing current pointer
                    removePtr(removingPrice);
                }
            }
            ptr = ptr.next;
        }
        Order tradeSize = new Order(-1, -1, size);  // Add amount of shares traded
        tradedOrders.add(0, tradeSize);
        return tradedOrders;
    }

    public void addOrder(int timestamp, int order_id, int price, int size) {
        Order newOrder = new Order(timestamp, order_id, size);
        if (head == null) {                     // If empty list add to head
            SamePriceOrders newOrderPrice = new SamePriceOrders(price);
            newOrderPrice.addOrder(newOrder);
            head = newOrderPrice;
        } else if (price > head.getPrice()) {   // If price greater than head
            SamePriceOrders newOrderPrice = new SamePriceOrders(price);
            newOrderPrice.addOrder(newOrder);
            newOrderPrice.next = head;
            head = newOrderPrice;

        } else {
            SamePriceOrders ptr = head;
            SamePriceOrders prev = null;
            while (ptr != null) {
                if (price == ptr.getPrice()) {          // Add to specific price point
                    ptr.addOrder(newOrder);
                    return;
                } else if (price > ptr.getPrice()) {
                    SamePriceOrders newOrderPrice = new SamePriceOrders(price);
                    newOrderPrice.addOrder(newOrder);
                    prev.next = newOrderPrice;
                    newOrderPrice.next = ptr;
                    return;
                }
                prev = ptr;
                ptr = ptr.next;
            }
            // Didn't find matching price, create new price point
            SamePriceOrders newOrderPrice = new SamePriceOrders(price);
            newOrderPrice.addOrder(newOrder);
            prev.next = newOrderPrice;
        }
    }
}


class AskQueue extends OrderQueue {
    AskQueue() {
        super();
    }
    public List<Order> matchOrder(int price, int size) {
        SamePriceOrders ptr = super.head;
        List<Order> tradedOrders = new ArrayList<>();
        while (ptr != null && size != 0) {
            if (ptr.getPrice() <= price) {      // If prices match make trade
                List<Order> current = ptr.tradeShares(size);
                size = current.get(0).getSize();
                current.remove(0);
                // Save all successful orders
                for (Order o: current) {
                    tradedOrders.add(o);
                }
            }
            ptr = ptr.next;
        }
        Order tradeSize = new Order(-1, -1, size);  // Add amount of shares traded
        tradedOrders.add(0, tradeSize);
        return tradedOrders;
    }

    public void addOrder(int timestamp, int order_id, int price, int size) {
        Order newOrder = new Order(timestamp, order_id, size);
        if (head == null) {                     // If empty list add to head
            SamePriceOrders newOrderPrice = new SamePriceOrders(price);
            newOrderPrice.addOrder(newOrder);
            head = newOrderPrice;
        } else if (price < head.getPrice()) {   // If price greater than head
            SamePriceOrders newOrderPrice = new SamePriceOrders(price);
            newOrderPrice.addOrder(newOrder);
            newOrderPrice.next = head;
            head = newOrderPrice;

        } else {
            SamePriceOrders ptr = head;
            SamePriceOrders prev = null;
            while (ptr != null) {
                if (price == ptr.getPrice()) {          // Add to specific price point
                    ptr.addOrder(newOrder);
                    return;
                } else if (price < ptr.getPrice()) {
                    SamePriceOrders newOrderPrice = new SamePriceOrders(price);
                    newOrderPrice.addOrder(newOrder);
                    prev.next = newOrderPrice;
                    newOrderPrice.next = ptr;
                    return;
                }
                prev = ptr;
                ptr = ptr.next;
            }
            // Didn't find matching price, create new price point
            SamePriceOrders newOrderPrice = new SamePriceOrders(price);
            newOrderPrice.addOrder(newOrder);
            prev.next = newOrderPrice;
        }
    }

}

class SamePriceOrders {
    private int price;
    private int totalSizeOrders;
    private List<Order> orders;
    public SamePriceOrders next;

    public SamePriceOrders(int price) {
        this.price = price;
        totalSizeOrders = 0;
        orders = new ArrayList<>();
        next = null;
    }

    public void addOrder(Order newOrder) {
        orders.add(newOrder);
        totalSizeOrders += newOrder.getSize();
    }

    public List<Order> tradeShares(int size) {
        List<Order> tradedOrders = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            Order current = orders.get(i);
            int currentSize = current.getSize();
            if (size >= currentSize) {                  // If size > current remove this order
                tradedOrders.add(current);
                size -= currentSize;
                totalSizeOrders -= currentSize;
            } else if (size != 0){                                    // else compute difference on current order and set size to 0
                current.setSize(currentSize - size);
                totalSizeOrders -= size;
                Order partialTrade = new Order(current.getTimestamp(), current.getOrder_id(), size);
                tradedOrders.add(partialTrade);
                size = 0;
            }
        }
        orders.removeAll(tradedOrders);             // Removed completed orders
        Order tradeSize = new Order(-1, -1, size);  // Add amount of shares traded
        tradedOrders.add(0, tradeSize);
        return tradedOrders;
    }

    int removeOrderId(int id) {
        for (int i = 0; i < orders.size(); i++) {
            Order current = orders.get(i);
            if (current.getOrder_id() == id) {
                totalSizeOrders -= current.getSize();
                current.printOrder();
                orders.remove(i);
                return i;
            }
        }
        return -1;
    }

    int getSize() { return totalSizeOrders; }
    int getPrice() { return price; }

    void printOrders() {
        for(Order order: orders) {
            order.printOrder();
        }
    }
}


class Order {
    private int timestamp;
    private int order_id;
    private int size;

    Order(int timestamp, int order_id, int size) {
        this.timestamp = timestamp;
        this.order_id = order_id;
        this.size = size;
    }
    public int getTimestamp() { return timestamp; }
    public int getOrder_id() { return order_id; }
    public int getSize() { return size; }

    void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
    void setOrder_id(int order_id) {
        this.order_id = order_id;
    }
    void setSize(int size) {
        this.size = size;
    }

    void printOrder() {
        System.out.println("time: " + timestamp + "\t\torder_id: " + order_id + "\tsize: " + size + "\n");
    }
}
