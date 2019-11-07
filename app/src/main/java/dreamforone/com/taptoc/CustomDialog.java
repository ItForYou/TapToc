package dreamforone.com.taptoc;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class CustomDialog extends Dialog {
    View.OnClickListener gelleryOnClickListener,photoOnClickListener;
    private LinearLayout galleryLayout,photoLayout;


    public CustomDialog(@NonNull Context context,View.OnClickListener gelleryOnClickListener,View.OnClickListener photoOnClickListener) {
        super(context);
        this.gelleryOnClickListener=gelleryOnClickListener;
        this.photoOnClickListener=photoOnClickListener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        setContentView(R.layout.custom_dialog);

        galleryLayout=(LinearLayout)findViewById(R.id.galleryLayout);
        photoLayout=(LinearLayout)findViewById(R.id.photoLayout);
        galleryLayout.setOnClickListener(gelleryOnClickListener);
        photoLayout.setOnClickListener(photoOnClickListener);
    }

}
