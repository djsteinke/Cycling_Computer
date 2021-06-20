package rnfive.djs.ant.antplus.pcc.defines;

import java.util.EnumSet;
import java.util.Iterator;

public enum EventFlag {
    UNRECOGNIZED_FLAG_PRESENT(1L),
    WAS_BUFFERED(2L);

    private final long longValue;

    EventFlag(long longValue) {
        this.longValue = longValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public static EnumSet<EventFlag> getEventFlagsFromLong(long longValue) {
        EnumSet<EventFlag> eventFlags = EnumSet.noneOf(EventFlag.class);
        EventFlag[] var3 = values();

        for (EventFlag flag : var3) {
            long flagValue = flag.getLongValue();
            if ((flagValue & longValue) == flagValue) {
                eventFlags.add(flag);
                longValue -= flagValue;
            }
        }

        if (0L != longValue) {
            eventFlags.add(UNRECOGNIZED_FLAG_PRESENT);
        }

        return eventFlags;
    }

    public static long getLongFromEventFlags(EnumSet<EventFlag> eventFlags) {
        long longValue = 0L;

        EventFlag flag;
        for(Iterator<EventFlag> var3 = eventFlags.iterator(); var3.hasNext(); longValue |= flag.getLongValue()) {
            flag = var3.next();
        }

        return longValue;
    }
}
