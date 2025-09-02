com.my;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity {
    private GitHubGameLoader gameLoader;
    private ProgressBar loadingIndicator;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        gameLoader = (GitHubGameLoader) findViewById(R.id.github_browser);
        loadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);
        
        // Set focusable for key events
        gameLoader.setFocusable(true);
        gameLoader.setFocusableInTouchMode(true);
        gameLoader.requestFocus();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (gameLoader.isGameLoaded() && gameLoader.isPlaying()) {
            if (gameLoader.onKeyDown(keyCode)) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    
    public void onGameLoaded() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingIndicator.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Game loaded from GitHub!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
