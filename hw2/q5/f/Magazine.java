// Magazine (implements SaleableItem)
public class Magazine implements SaleableItem {
    @Override
    public void sellCopy() {
        System.out.println("Selling a magazine");
    }
}
