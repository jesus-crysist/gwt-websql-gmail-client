package edu.raf.jovica.diplomski.util;

import edu.raf.jovica.diplomski.client.data.Message;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * Created with IntelliJ IDEA.
 * User: jovica
 * Date: 12/7/13
 * Time: 10:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class MessageComparator implements Comparator<Message> {
    @Override
    public int compare(Message m1, Message m2) {
        return (int) (m2.getReceivedDate().getTime() - m1.getReceivedDate().getTime());
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public Comparator<Message> reversed() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Comparator<Message> thenComparing(Comparator<? super Message> other) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <U extends Comparable<? super U>> Comparator<Message> thenComparing(Function<? super Message, ? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <U extends Comparable<? super U>> Comparator<Message> thenComparing(Function<? super Message, ? extends U> keyExtractor) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Comparator<Message> thenComparingInt(ToIntFunction<? super Message> keyExtractor) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Comparator<Message> thenComparingLong(ToLongFunction<? super Message> keyExtractor) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Comparator<Message> thenComparingDouble(ToDoubleFunction<? super Message> keyExtractor) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
