package corn.cardreader.tech_card.util;

import corn.cardreader.utilities.ReaderDelegate;
import java.util.Map;

public interface TechCardReaderDelegate extends ReaderDelegate {
    void onFinish(Map<String, String> data);
}
