package com.app.product.strategy;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 10% off the subtotal once an order contains 10 or more total units.
 * @Primary makes this the one actually injected where a single
 * PricingStrategy is @Autowired/constructor-injected, without deleting the
 * simpler DefaultPricingStrategy (useful for tests, or swapping back via a
 * @Qualifier).
 */
@Component
@Primary
public class BulkOrderDiscountStrategy implements PricingStrategy {

    private static final int BULK_THRESHOLD = 10;
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");

    @Override
    public BigDecimal calculateFinalTotal(BigDecimal subtotal, int totalItemCount) {
        if (totalItemCount >= BULK_THRESHOLD) {
            BigDecimal discount = subtotal.multiply(DISCOUNT_RATE);
            return subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }
}
