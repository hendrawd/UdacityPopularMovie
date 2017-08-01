package ganteng.hendrawd.popularmovies.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

/**
 * A helper class for easily call TextToSpeech from various android versions
 *
 * @author hendrawd on 11/22/16
 */

public class TextToSpeechHelper {

    public interface SpeakCallback {
        void onSpeak();
    }

    /**
     * Started at android 2.3 we will use "in_ID" as the android standard
     * although we can use "in" for java 5 and 6, and "id" as java >7
     * https://web.archive.org/web/20120814113314/http://colincooper.net/blog/2011/02/17/android-supported-language-and-locales/
     */
    public final String ID = "in_ID";
    public final String US = "en_US";

    private TextToSpeech textToSpeech;
    private SpeakCallback speakCallback;

    public void speak(Context context, final String stringToSpeak, final Locale locale) {
        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (locale != null) {
                        textToSpeech.setLanguage(locale);
                    } else {
                        textToSpeech.setLanguage(new Locale(US));
                    }
                    speakCompat(stringToSpeak);
                    if (speakCallback != null) {
                        speakCallback.onSpeak();
                    }
                }
            });
        } else {
            //negate speak command if it is currently speaking
            if (!textToSpeech.isSpeaking()) {
                speakCompat(stringToSpeak);
            }
            if (speakCallback != null) {
                speakCallback.onSpeak();
            }
        }
    }

    public void speak(Context context, final String stringToSpeak) {
        speak(context, stringToSpeak, null);
    }

    private void speakCompat(String stringToSpeak) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeechGreater21(stringToSpeak);
        } else {
            textToSpeechUnder20(stringToSpeak);
        }
    }

    public void stop() {
        textToSpeech.stop();
    }

    @SuppressWarnings("deprecation")
    private void textToSpeechUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void textToSpeechGreater21(String text) {
        String utteranceId = this.hashCode() + "";
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    public void setCallback(SpeakCallback speakCallback) {
        this.speakCallback = speakCallback;
    }
}
