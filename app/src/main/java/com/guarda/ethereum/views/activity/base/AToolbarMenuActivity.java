package com.guarda.ethereum.views.activity.base;


import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.guarda.ethereum.R;
import com.guarda.ethereum.views.activity.MainActivity;

import static com.guarda.ethereum.models.constants.Extras.FIRST_ACTION_MAIN_ACTIVITY;
import static com.guarda.ethereum.models.constants.Extras.GO_TO_SETTINGS;

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
//            case R.id.toolbar_menu_settings:
//                goToCreateWallet();
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToCreateWallet() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(FIRST_ACTION_MAIN_ACTIVITY, GO_TO_SETTINGS);
        startActivity(intent);
    }
}
