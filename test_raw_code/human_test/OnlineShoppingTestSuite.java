import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

public class OnlineShoppingTestSuite{
    private Customer customer;
    private Store store;
    private Product laptop, mouse;

    @BeforeEach
    public void setUp() {
        // Initialize a store, customer, and products for testing
        store = new Store("Gadget World");
        customer = new Customer("Bob", 1500.0f);

        laptop = new Product("Laptop", 1000.0f);
        mouse = new Product("Mouse", 50.0f);

        store.addProduct(laptop);
        store.addProduct(mouse);
    }

    @Test
    public void testProductRatings() {
        // Test adding valid and invalid ratings
        assertTrue(laptop.setRating(4), "Valid rating should be accepted.");
        assertFalse(laptop.setRating(6), "Rating outside 1-5 should be rejected.");

        // Assuming getAvgRating rounds to one decimal place as per the assignment requirement
        assertEquals(4.0f, laptop.getAvgRating(), "Average rating should match the only rating given.");
    }

    @Test
    public void testStoreProductManagement() {
        // Verify initial product listing
        assertTrue(store.hasProduct(laptop), "Store should contain the laptop initially.");

        // Test product removal
        assertTrue(store.removeProduct(laptop), "Should successfully remove existing product.");
        assertFalse(store.hasProduct(laptop), "Laptop should no longer exist in the store after removal.");

        // Test adding back the product and checking uniqueness
        assertTrue(store.addProduct(laptop), "Should be able to add back the laptop.");
        assertFalse(store.addProduct(laptop), "Should not add the same product twice.");
    }

    @Test
    public void testCustomerPurchaseAndProductRemoval() {
        // Test purchase
        assertTrue(customer.purchaseProduct(store, laptop), "Customer should be able to purchase the laptop.");
        assertFalse(store.hasProduct(laptop), "Laptop should be removed from the store after purchase.");

        // Attempt to purchase an already purchased product
        assertFalse(customer.purchaseProduct(store, laptop), "Should not be able to purchase removed product.");

        // Test product re-addition and re-purchase
        store.addProduct(laptop);
        assertTrue(customer.purchaseProduct(store, laptop), "Should be able to purchase the re-added laptop.");
    }

    @Test
    public void testMultipleProductPurchaseAndRatings() {
        // Test purchasing multiple products
        customer.purchaseProduct(store, laptop);
        customer.purchaseProduct(store, mouse);

        // Rate products
        customer.rateProduct(laptop, 5);
        customer.rateProduct(mouse, 3);

        // Check ratings directly since we can't verify via customer due to method constraints
        assertEquals(5.0f, laptop.getAvgRating(), "Laptop rating should be correct.");
        assertEquals(3.0f, mouse.getAvgRating(), "Mouse rating should be correct.");
    }

    @Test
    public void testInvalidRating() {
        // Attempt to set invalid ratings
        assertFalse(laptop.setRating(0), "Should reject rating below valid range.");
        assertFalse(laptop.setRating(6), "Should reject rating above valid range.");

        // Verify no rating was added
        assertEquals(0.0f, laptop.getAvgRating(), "Average rating should remain unaffected by invalid ratings.");
    }

}
