package org.tylproject.vaadin.addon.mongo;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.tylproject.data.mongo.Customer;
import org.tylproject.vaadin.addon.BufferedMongoContainer;

/**
 * Created by evacchi on 12/11/14.
 */
public class BufferedMongoContainerTest extends BaseTest {

    @Test
    public void testRemoveFirstItem() {
        final BufferedMongoContainer<Customer> mc =
                builder().buildBuffered();

        int initSize = mc.size();

        Object itemId = mc.firstItemId();
        mc.removeItem(itemId);
        Object nextItemId = mc.firstItemId();
        assertNotNull(nextItemId);
        assertNotEquals(itemId, nextItemId);
        mc.removeItem(nextItemId);

        assertEquals(-1, mc.indexOfId(itemId));
        assertFalse(mc.getItemIds(0, 1).contains(nextItemId));
        assertNotEquals(null, mc.getItemIds(0, 1).get(0));
        assertNotEquals(initSize, mc.size());
    }

}
