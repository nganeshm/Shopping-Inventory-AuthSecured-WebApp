package com.app.product.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** No discount at all — the "null object"-style baseline strategy. */
@Component("defaultPricingStrategy")
public class DefaultPricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculateFinalTotal(BigDecimal subtotal, int totalItemCount) {
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }
}
