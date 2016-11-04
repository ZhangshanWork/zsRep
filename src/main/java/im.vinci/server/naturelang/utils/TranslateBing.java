package im.vinci.server.naturelang.utils;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

public class TranslateBing {
    public TranslateBing() {
    }

    public static String doTranslate(String lang) throws Exception {
        Translate.setClientId("inspero_vinci");
        Translate.setClientSecret("wfxbmZ1rRHN9CWhLyRjqmUKwwmf0WLHVRRb3hO4qFQ4=");
        String translatedText = "";

        try {
            translatedText = Translate.execute(lang, Language.CHINESE_SIMPLIFIED, Language.ENGLISH);
            return translatedText;
        } catch (Exception var3) {
            throw var3;
        }
    }
}