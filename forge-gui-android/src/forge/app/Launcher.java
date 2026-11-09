package forge.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import forge.SharedDeck;
import forge.gui.GuiBase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Launcher extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent main = new Intent(Launcher.this, Main.class);
        main.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(main);

        Intent intent = getIntent();
        sendIntent(intent, intent.getAction(), intent.getType());

        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void sendIntent(Intent intent, String action, String type) {
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> receive(intent, type), 1500);
        }
    }

    //a Forge .dck shared by another app (Scry Magic's "Send to Forge") is saved as a deck; other text goes to the clipboard
    private void receive(Intent intent, String type) {
        if (!"text/plain".equals(type)) { return; }
        String text = null;
        Uri textUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        try {
            if (textUri != null) {
                InputStream in = getContentResolver().openInputStream(textUri);
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                for (String line; (line = r.readLine()) != null; ) {
                    total.append(line).append('
');
                }
                text = total.toString();
            } else {
                text = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (text == null) { return; }
        if (SharedDeck.looksLikeDeck(text)) {
            SharedDeck.receive(text);
            return;
        }
        try {
            GuiBase.getInterface().copyToClipboard(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        sendIntent(intent, intent.getAction(), intent.getType());
    }
}
