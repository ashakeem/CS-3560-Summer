// Ticket (implements SaleableItem)
public class Ticket implements SaleableItem {
    @Override
    public void sellCopy() {
        System.out.println("Selling a ticket");
    }
}
