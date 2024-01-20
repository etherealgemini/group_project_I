
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class TestOnlineStoreLocal {
    // Define the necessary class instances and constructors
    private static Product product1;
    private static Product product2;
    private static Product product3;
    private static Product product4;
    private static Product product5;
    private static Constructor<Product> productConstructor;
    private static Store store1;
    private static Store store2;
    private static Constructor<Store> storeConstructor1;
    private static Constructor<Store> storeConstructor2;
    private static Customer customer;
    private static Constructor<Customer> customerConstructor;

    // Add test cases for Product class
    // ...

    // Add test cases for Store class
    // ...

    // Add test cases for Customer class
    // ...

    // Add additional test cases for the refundProduct method in the Customer class
    @Test
    public void test25_refundProduct() {
        try {
            // Create a new customer and add products to the shopping cart
            Customer customer1 = customerConstructor.newInstance("C4", 100);
            Method purchaseProduct = Customer.class.getDeclaredMethod("purchaseProduct", Store.class, Product.class);
            purchaseProduct.invoke(customer1, store2, product2);
            purchaseProduct.invoke(customer1, store2, product3);
            purchaseProduct.invoke(customer1, store1, product5);

            // Refund products and check the shopping cart, wallet, and store product list
            Method refundProduct = Customer.class.getDeclaredMethod("refundProduct", Product.class);
            refundProduct.setAccessible(true);
            Field shoppingCart = Customer.class.getDeclaredField("shoppingCart");
            shoppingCart.setAccessible(true);
            Field wallet = Customer.class.getDeclaredField("wallet");
            wallet.setAccessible(true);
            Field productList = Store.class.getDeclaredField("productList");
            productList.setAccessible(true);
            Field income = Store.class.getDeclaredField("income");
            income.setAccessible(true);
            Field price = Product.class.getDeclaredField("price");
            price.setAccessible(true);

            // Refund product2
            assertTrue((Boolean) refundProduct.invoke(customer1, product2));
            assertFalse((Boolean) refundProduct.invoke(customer1, product2));
            assertFalse((Boolean) refundProduct.invoke(customer1, product1));
            assertFalse((Boolean) refundProduct.invoke(customer1, product4));

            // Check the updated shopping cart, wallet, and store product list
            ArrayList<Product> answerProductList = new ArrayList<>(Arrays.asList(product3, product5));
            ArrayList<Product> answerShoppingCart = new ArrayList<>(Arrays.asList(product3, product5));
            assertEquals(answerShoppingCart, shoppingCart.get(customer1));
            assertEquals(102f, wallet.get(customer1));
            assertEquals(answerProductList, productList.get(store2));
            assertEquals(1000f, income.get(store2));

            // Refund product3
            assertTrue((Boolean) refundProduct.invoke(customer1, product3));

            // Check the updated shopping cart, wallet, and store product list
            answerProductList = new ArrayList<>(Arrays.asList(product5));
            answerShoppingCart = new ArrayList<>(Arrays.asList(product5));
            assertEquals(answerShoppingCart, shoppingCart.get(customer1));
            assertEquals(105.5f, wallet.get(customer1));
            assertEquals(answerProductList, productList.get(store2));
            assertEquals(1005.5f, income.get(store2));

            // Refund product5
            assertTrue((Boolean) refundProduct.invoke(customer1, product5));

            // Check the updated shopping cart, wallet, and store product list
            answerProductList = new ArrayList<>(Arrays.asList(product5));
            answerShoppingCart = new ArrayList<>();
            assertEquals(answerShoppingCart, shoppingCart.get(customer1));
            assertEquals(106.5f, wallet.get(customer1));
            assertEquals(answerProductList, productList.get(store1));
            assertEquals(1f, income.get(store1));

        } catch (Exception e) {
            e.printStackTrace();
            fail("refundProduct error!");
        }
    }
}
