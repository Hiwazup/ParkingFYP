package com.martin.parkingfyp;

import android.os.Bundle;
import android.view.MenuItem;

public class About extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setToolbarText(getString(R.string.about_toolbar_text));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_about:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
