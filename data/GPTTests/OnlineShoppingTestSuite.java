import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

public class OnlineShoppingTestSuite {
    private Customer customer;
    private Store store1, store2;
    private Product productLaptop, productMouse, productPhone;

    @BeforeEach
    public void setUp() {
        // Initialize some stores and products for use in multiple tests
        store1 = new Store("TechStore");
        store2 = new Store("GadgetStore");

        productLaptop = new Product("Laptop", 1000.00f);
        productMouse = new Product("Mouse", 25.00f);
        productPhone = new Product("Phone", 500.00f);

        // Adding products to stores
        store1.addProduct(productLaptop);
        store2.addProduct(productMouse);
        store2.addProduct(productPhone);

        // Setting up a customer with a wallet balance
        customer = new Customer("Alice", 2000.00f);
    }

    @Test
    public void testProductRating() {
        // Testing the rating functionality for a product
        assertTrue(productLaptop.setRating(5), "Setting a valid rating should return true.");
        assertTrue(productLaptop.setRating(1), "Setting another valid rating should return true.");
        assertFalse(productLaptop.setRating(6), "Setting an invalid rating should return false.");
        assertEquals(3.0, productLaptop.getAvgRating(), 0.1, "The average rating should be correctly calculated.");
    }

    @Test
    public void testStoreProductManagement() {
        // Testing product addition, removal, and existence check in a store
        assertTrue(store1.hasProduct(productLaptop), "Store should have the product.");
        assertFalse(store1.hasProduct(productMouse), "Store should not have the product it didn't add.");

        assertTrue(store1.removeProduct(productLaptop), "Removing an existing product should return true.");
        assertFalse(store1.hasProduct(productLaptop), "Store should not have the product after it's removed.");
    }

    @Test
    public void testCustomerPurchaseAndRefund() {
        // Testing the purchase functionality
        assertTrue(customer.purchaseProduct(store1, productLaptop), "Customer should be able to purchase the laptop.");
//        assertEquals(1000.00f, customer.getWallet(), 0.01, "Customer wallet should be updated after purchase.");

        // Testing the refund functionality (assuming it's implemented)
        assertTrue(customer.refundProduct(productLaptop), "Customer should be able to refund the laptop.");
//        assertEquals(2000.00f, customer.getWallet(), 0.01, "Customer wallet should be updated after refund.");
    }

    @Test
    public void testCustomerRatingProduct() {
        // Customer purchases a product and rates it
        customer.purchaseProduct(store2, productPhone);
        assertTrue(customer.rateProduct(productPhone, 4), "Customer should be able to rate purchased product.");
        assertEquals(4.0, productPhone.getAvgRating(), 0.1, "Product rating should be updated correctly.");
    }

//    @Test
//    public void testSortingShoppingCart() {
//        // Testing sorting functionality in the shopping cart
//        customer.purchaseProduct(store1, productLaptop); // Price: 1000, Rating: Unrated
//        customer.purchaseProduct(store2, productMouse);  // Price: 25, Rating: Unrated
//        customer.purchaseProduct(store2, productPhone);  // Price: 500, Rating: Unrated
//
//        // Assuming the implementation of a method to get sorted shopping cart
//        customer.viewShoppingCart(SortBy.Price);
//        assertEquals(productMouse, customer.viewShoppingCart().get(0), "Mouse should be the cheapest product.");
//        assertEquals(productPhone, sortedByPrice.get(1), "Phone should be the second cheapest product.");
//        assertEquals(productLaptop, sortedByPrice.get(2), "Laptop should be the most expensive product.");
//    }
}
