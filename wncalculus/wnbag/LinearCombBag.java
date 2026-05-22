package wnbag;

import java.util.Map;

import bagexpr.AbstractBag;
import expr.Domain;
import classfunction.ElementaryFunction;

/**
 * Adapter representing a LinearComb as Bag<ElementaryFunction>
 */
public final class LinearCombBag extends AbstractBag<ElementaryFunction> {

    public LinearCombBag(Map<ElementaryFunction, Integer> m) {
        super(m);
    }

    public LinearCombBag(Domain dom, Domain codom) {
        super(dom, codom);
    }

    @Override
    public LinearCombBag build(Domain dom, Domain codom) {
        return new LinearCombBag(dom, codom);
    }

    @Override
    public LinearCombBag build(Map<ElementaryFunction, Integer> m) {
        return new LinearCombBag(m);
    }

    @Override
    public Integer card() {
        int card = 0;
        for (Map.Entry<? extends ElementaryFunction, Integer> x : asMap().entrySet()) {
            Integer k = x.getKey().cardLb();
            if (k == null)
                return null;
            card += k * x.getValue();
        }
        return card;
    }
}