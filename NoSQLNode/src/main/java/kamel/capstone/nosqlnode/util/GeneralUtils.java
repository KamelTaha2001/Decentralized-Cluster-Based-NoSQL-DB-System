package kamel.capstone.nosqlnode.util;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

public class GeneralUtils {
    public static <T> List<T> intersection(List<List<T>> lists) {
        if (lists.size() > 0) {
            return lists.get(0).stream().filter(item -> {
                boolean contains = true;
                for (int i = 1; i < lists.size(); i++)
                    if (!lists.get(i).contains(item)) {
                        contains = false;
                        break;
                    }
                return contains;
            }).toList();
        } else return Collections.emptyList();
    }

    public static byte[] longToByteArray(Long number) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(number);
        return buffer.array();
    }
}
