package com.guarda.ethereum.views.activity.base;


import android.view.Menu;
import android.view.MenuItem;

import com.guarda.ethereum.R;

public abstract class AToolbarMenuActivity extends AToolbarActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
