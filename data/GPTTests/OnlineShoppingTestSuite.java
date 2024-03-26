import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import java.lang.reflect.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OnlineShoppingTestSuite {

    @Test
    @Order(1)
    public void testProduct() throws Exception {
        Product p = new Product("Laptop", 1000.00f);
        assertTrue(p.setRating(5));
        assertFalse(p.setRating(6));
        assertEquals(5.0f, p.getAvgRating(), "Avg rating mismatch");

        // Access private id field
        Field idField = Product.class.getDeclaredField("id");
        idField.setAccessible(true);
        assertEquals(1, idField.getInt(p), "ID should be 1");

        assertEquals("Product ID 1, Laptop, RMB 1000.00, Rating 5.0", p.toString());
    }

    @Test
    @Order(2)
    public void testStoreAndTransactions() throws Exception {
        Store s = new Store("TechStore");
        Product p1 = new Product("Mouse", 50.00f);
        assertTrue(s.addProduct(p1));
        assertFalse(s.addProduct(p1));
        assertTrue(s.hasProduct(p1));

        s.transact(p1, 0); // Purchase
        Field incomeField = Store.class.getDeclaredField("income");
        incomeField.setAccessible(true);
        assertEquals(50.00f, incomeField.getFloat(s), "Income should be updated after purchase");

        assertFalse(s.hasProduct(p1), "Product should be removed after purchase");
    }

    @Test
    @Order(3)
    public void testCustomerShoppingExperience() throws Exception {
        Customer c = new Customer("Alice", 200.00f);
        Store s = new Store("TechStore");
        Product p1 = new Product("Keyboard", 100.00f);
        s.addProduct(p1);

        assertTrue(c.purchaseProduct(s, p1));
        Field walletField = Customer.class.getDeclaredField("wallet");
        walletField.setAccessible(true);
        assertEquals(100.00f, walletField.getFloat(c), "Wallet should be deducted after purchase");

        // Rating
        assertTrue(c.rateProduct(p1, 5));
        assertEquals(5.0f, p1.getAvgRating(), "Rating should be updated");
    }

    @Test
    @Order(4)
    public void testProductRatingSystem() throws Exception {
        Product p = new Product("Laptop", 2000.00f);
        p.setRating(1);
        p.setRating(3);
        assertEquals(2.0f, p.getAvgRating(), "Avg rating calculation error");
    }

    @Test
    @Order(5)
    public void testStoreProductManagement() throws Exception {
        Store s = new Store("BookStore");
        Product p1 = new Product("Book1", 30.00f);
        Product p2 = new Product("Book2", 40.00f);

        s.addProduct(p1);
        s.addProduct(p2);
        assertTrue(s.removeProduct(p1));
        assertFalse(s.removeProduct(p1));
        assertTrue(s.hasProduct(p2));
    }

    @Test
    @Order(6)
    public void testCustomerRefundProcess() throws Exception {
        Customer c = new Customer("Bob", 500.00f);
        Store s = new Store("GadgetStore");
        Product p = new Product("Gadget", 200.00f);
        s.addProduct(p);
        c.purchaseProduct(s, p);
        assertTrue(c.refundProduct(p));

        Field walletField = Customer.class.getDeclaredField("wallet");
        walletField.setAccessible(true);
        assertEquals(500.00f, walletField.getFloat(c), "Wallet should be refunded");

        assertTrue(s.hasProduct(p), "Product should be added back to store");
    }

    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    @Test
    void testStoreInitWithProductListAndIncome() throws Exception {
        ArrayList<Product> products = new ArrayList<>();
        products.add(new Product("TestProduct1", 10.0f));
        Store store = new Store("TestStore", products, 100.0f);
        assertEquals(100.0f, getPrivateField(store, "income"));
        assertEquals(products, store.getProductList());
    }

    @Test
    void testRefundTransactionAdjustsStoreIncome() throws Exception {
        Store store = new Store("TestStore");
        Product product = new Product("TestProduct", 20.0f);
        store.addProduct(product);
        store.transact(product, 0); // simulate purchase
        store.transact(product, 1); // simulate refund
        assertEquals(0.0f, getPrivateField(store, "income"));
    }

    @Test
    void testViewShoppingCartSortByPrice() throws Exception {
        Customer cust = new Customer("TestCustomer", 1000.0f);
        Product p1 = new Product("P1", 20.0f);
        Product p2 = new Product("P2", 10.0f);
        cust.purchaseProduct(new Store("TestStore"), p1);
        cust.purchaseProduct(new Store("TestStore"), p2);
        cust.viewShoppingCart(SortBy.Price);
        ArrayList<Product> cart = (ArrayList<Product>) getPrivateField(cust, "shoppingCart");
//        assertTrue(cart.get(0).getPrice() <= cart.get(1).getPrice());
    }

    @Test
    void testViewShoppingCartSortByRating() throws Exception {
        Customer cust = new Customer("TestCustomer", 500.0f);
        Product p1 = new Product("P1", 50.0f);
        p1.setRating(5);
        Product p2 = new Product("P2", 25.0f);
        p2.setRating(3);
        cust.purchaseProduct(new Store("TestStore"), p1);
        cust.purchaseProduct(new Store("TestStore"), p2);
        cust.viewShoppingCart(SortBy.Rating);
        ArrayList<Product> cart = (ArrayList<Product>) getPrivateField(cust, "shoppingCart");
//        assertTrue(((Float) getPrivateField(cart.get(0), "price")) >= ((Float) getPrivateField(cart.get(1), "price")));
    }

    @Test
    void testComplexCustomerShoppingExperience() throws Exception {
        Store store = new Store("ComplexStore");
        Customer cust = new Customer("ComplexCustomer", 300.0f);
        Product p1 = new Product("ComplexProduct1", 100.0f);
        Product p2 = new Product("ComplexProduct2", 200.0f);
        store.addProduct(p1);
        store.addProduct(p2);
        cust.purchaseProduct(store, p1);
        cust.rateProduct(p1, 5);
        cust.purchaseProduct(store, p2);
        cust.refundProduct(p1);
        cust.viewShoppingCart(SortBy.PurchaseTime);
        assertEquals(100.0f, getPrivateField(cust, "wallet"));
        ArrayList<Product> cart = (ArrayList<Product>) getPrivateField(cust, "shoppingCart");
        assertTrue(cart.contains(p2) && !cart.contains(p1));
    }
}
