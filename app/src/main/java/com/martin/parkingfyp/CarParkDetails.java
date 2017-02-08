package com.martin.parkingfyp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by Martin on 01/02/2017.
 */

public class CarParkDetails  extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_park_details);
        setToolbarText("Details");
        TextView title = (TextView)findViewById(R.id.details_title);
        TextView spaces = (TextView)findViewById(R.id.details_spaces);
        TextView status = (TextView)findViewById(R.id.details_status);

        title.setText(getIntent().getExtras().getString("Name"));
        spaces.setText(getIntent().getExtras().getString("Spaces"));
        status.setText(getIntent().getExtras().getString("Status"));
    }

    @Override
    protected boolean useDrawerToggle() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
