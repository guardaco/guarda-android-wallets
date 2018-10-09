package com.guarda.ethereum.views.fragments;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.views.activity.MainActivity;
import com.guarda.ethereum.views.fragments.base.BaseFragment;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;

import static com.guarda.ethereum.models.constants.Extras.FIRST_ACTION_MAIN_ACTIVITY;
import static com.guarda.ethereum.models.constants.Extras.GO_TO_SETTINGS;

@AutoInjector(GuardaApp.class)
public class CustomNodeFragment extends BaseFragment {

    @BindView(R.id.et_server)
    EditText et_server;
    @BindView(R.id.et_port)
    EditText et_port;

    @Inject
    SharedManager sharedManager;
    @Inject
    EthereumNetworkManager networkManager;

    @Override
    protected int getLayout() {
        return R.layout.fragment_custom_node;
    }

    @Override
    protected void init() {
        GuardaApp.getAppComponent().inject(this);
        if (!sharedManager.getCustomNode().isEmpty()) {
            URL url = null;
            try {
                url = new URL(sharedManager.getCustomNode());
            } catch (MalformedURLException me) {
                me.printStackTrace();
                Log.d("psd", "CustomNodeFragment - MalformedURLException: " + me.getMessage());
            }
            if (url != null) {
                et_server.setText(String.format("%s:%s", url.getProtocol(), url.getHost()));
                et_port.setText(url.getPort());
            }
        }
    }

    @OnClick({R.id.btn_save_node, R.id.btn_default_node})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.btn_save_node:
                String server = et_server.getText().toString().trim();
                String port = et_port.getText().toString().trim();
                if (!server.isEmpty()) {
                    if (port.isEmpty()) {
                        sharedManager.setCustomNode(server);
                    } else {
                        sharedManager.setCustomNode(String.format("%s:%s", server, port));
                    }
                    //TODO: API for update connection address
//                    networkManager.provideNativeNodeConnection(null);
                }
                goToSettings();
                break;
            case R.id.btn_default_node:
                sharedManager.setCustomNode("");
//                networkManager.provideNativeNodeConnection(null);
                et_server.getText().clear();
                et_port.getText().clear();
                break;
        }
    }

    private void goToSettings() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.putExtra(FIRST_ACTION_MAIN_ACTIVITY, GO_TO_SETTINGS);
        startActivity(intent);
    }
}
