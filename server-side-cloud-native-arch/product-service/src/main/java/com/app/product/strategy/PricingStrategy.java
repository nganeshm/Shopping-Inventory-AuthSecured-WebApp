package com.app.product.strategy;

import java.math.BigDecimal;

/**
 * Open/Closed Principle in action: OrderServiceImpl depends on this
 * abstraction to compute an order's final total. Adding a new pricing rule
 * (seasonal discount, loyalty-tier discount, coupon codes...) means adding a
 * NEW class that implements this interface and wiring it in — the order
 * service's own code never needs to change or be re-tested for that.
 */
public interface PricingStrategy {
    BigDecimal calculateFinalTotal(BigDecimal subtotal, int totalItemCount);
}
