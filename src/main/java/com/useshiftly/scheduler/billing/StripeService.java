package com.useshiftly.scheduler.billing;

import com.stripe.Stripe;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.Customer;
import com.stripe.param.SubscriptionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Service
public class StripeService {
    public StripeService(@Value("${stripe.secret-key}") String stripeSecretKey) {
        // Set Stripe secret key from application-prod.yml or environment variable
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Create a Stripe subscription for an admin.
     * @param customerId Stripe customer ID
     * @param billableUsers Number of billable users
     * @return Subscription object
     */
    public Subscription createSubscription(String customerId, int billableUsers) throws Exception {
        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
            .setCustomer(customerId)
            .addItem(SubscriptionCreateParams.Item.builder()
                .setPrice("price_id_for_user_monthly") // Replace with your Stripe price ID
                .setQuantity((long) billableUsers)
                .build())
            .build();
        return Subscription.create(params);
    }

    // Add more methods for updating quantity, logging payments, etc.
    /**
     * Create a Stripe customer for an admin.
     * @param email Admin email
     * @param name Admin name
     * @return Customer object
     */
    public Customer createCustomer(String email, String name) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        params.put("name", name);
        return Customer.create(params);
    }

    /**
     * Update the quantity of billable users in a subscription.
     * @param subscriptionId Stripe subscription ID
     * @param newQuantity New quantity of billable users
     * @return Updated Subscription object
     */
    public Subscription updateSubscriptionQuantity(String subscriptionId, int newQuantity) throws Exception {
        Subscription subscription = Subscription.retrieve(subscriptionId);
        Map<String, Object> params = new HashMap<>();
        List<Object> items = new ArrayList<>();
        for (SubscriptionItem item : subscription.getItems().getData()) {
            Map<String, Object> itemParams = new HashMap<>();
            itemParams.put("id", item.getId());
            itemParams.put("quantity", newQuantity);
            items.add(itemParams);
        }
        params.put("items", items);
        return subscription.update(params);
    }

    /**
     * Log a payment event (for compliance/audit).
     * @param subscriptionId Stripe subscription ID
     * @param amount Amount paid
     * @param currency Currency code
     * @param timestamp Payment timestamp
     */
    public void logPayment(String subscriptionId, long amount, String currency, long timestamp) {
        // Implement logic to log payment to BillingLog or audit table
        // Example: billingLogRepository.save(new BillingLog(...));
    }

    /**
     * Retrieve a subscription by ID.
     * @param subscriptionId Stripe subscription ID
     * @return Subscription object
     */
    public Subscription getSubscription(String subscriptionId) throws Exception {
        return Subscription.retrieve(subscriptionId);
    }

    /**
     * Cancel a Stripe subscription.
     * @param subscriptionId Stripe subscription ID
     * @return Cancelled Subscription object
     */
    public Subscription cancelSubscription(String subscriptionId) throws Exception {
        Subscription subscription = Subscription.retrieve(subscriptionId);
        return subscription.cancel();
    }

    // Utility: Set API key from config/env
    public void setApiKey(String apiKey) {
        Stripe.apiKey = apiKey;
    }

    // TODO: Add webhook event handling, invoice retrieval, etc. as needed for full Stripe integration.
}
