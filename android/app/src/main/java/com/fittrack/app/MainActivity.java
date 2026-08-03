package com.fittrack.app;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(WebDavPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
