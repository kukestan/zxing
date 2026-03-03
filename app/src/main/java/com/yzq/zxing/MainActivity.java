package com.yzq.zxing;

import android.content.Context;
import android.content.Intent;
import android.dream.DreamApnInfo;
import android.dream.DreamInterfaceManager;
import android.dream.IDreamInterfaceManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;
import com.yzq.zxinglibrary.encode.CodeCreator;

import java.util.List;


/**
 * @author: yzq
 * @date: 2017/10/26 15:17
 * @declare :
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button scanBtn;
    private TextView result;
    private EditText contentEt;
    private Button encodeBtn;
    private ImageView contentIv;
    private Toolbar toolbar;
    private Button fragScanBtn;
    private int REQUEST_CODE_SCAN = 111;
    /**
     * 生成带logo的二维码
     */
    private Button encodeBtnWithLogo;
    private ImageView contentIvWithLogo;
    private String contentEtString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    private void initView() {
        /*扫描按钮*/
        scanBtn = findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(this);
        /*扫描结果*/
        result = findViewById(R.id.result);

        /*要生成二维码的输入框*/
        contentEt = findViewById(R.id.contentEt);
        /*生成按钮*/
        encodeBtn = findViewById(R.id.encodeBtn);
        encodeBtn.setOnClickListener(this);
        /*生成的图片*/
        contentIv = findViewById(R.id.contentIv);

        fragScanBtn = findViewById(R.id.fragScanBtn);
        fragScanBtn.setOnClickListener(this);

        toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle("扫一扫");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        result = (TextView) findViewById(R.id.result);
        scanBtn = (Button) findViewById(R.id.scanBtn);
        contentEt = (EditText) findViewById(R.id.contentEt);
        encodeBtnWithLogo = (Button) findViewById(R.id.encodeBtnWithLogo);
        encodeBtnWithLogo.setOnClickListener(this);
        contentIvWithLogo = (ImageView) findViewById(R.id.contentIvWithLogo);
        encodeBtn = (Button) findViewById(R.id.encodeBtn);
        contentIv = (ImageView) findViewById(R.id.contentIv);
    }

    @Override
    public void onClick(View v) {


        Bitmap bitmap = null;
        switch (v.getId()) {
            case R.id.scanBtn:
                //zhanghao
                TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                int deviceId = tm.getMtu();
                Log.d("zhanghao", "onClick: deviceId = " + deviceId);

                AndPermission.with(this)
                        .runtime()
                        .permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE)
                        .onGranted(data -> {
                            Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                            /*ZxingConfig是配置类
                             *可以设置是否显示底部布局，闪光灯，相册，
                             * 是否播放提示音  震动
                             * 设置扫描框颜色等
                             * 也可以不传这个参数
                             * */
                            ZxingConfig config = new ZxingConfig();
                            // config.setPlayBeep(false);//是否播放扫描声音 默认为true
                            //  config.setShake(false);//是否震动  默认为true
                            // config.setDecodeBarCode(false);//是否扫描条形码 默认为true
//                                config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为白色
//                                config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
//                                config.setScanLineColor(R.color.colorAccent);//设置扫描线的颜色 默认白色
                            config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
                            intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                            startActivityForResult(intent, REQUEST_CODE_SCAN);
                        })
                        .onDenied(data -> {
                            Uri packageURI = Uri.parse("package:" + getPackageName());
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            startActivity(intent);

                            Toast.makeText(MainActivity.this, "没有权限无法扫描呦", Toast.LENGTH_LONG).show();
                        })
                        .start();

                break;
            case R.id.encodeBtn:
                contentEtString = contentEt.getText().toString().trim();
                if (TextUtils.isEmpty(contentEtString)) {
                    Toast.makeText(this, "请输入要生成二维码图片的字符串", Toast.LENGTH_SHORT).show();
                    return;
                }

                bitmap = CodeCreator.createQRCode(contentEtString, 400, 400, null);
                if (bitmap != null) {
                    contentIv.setImageBitmap(bitmap);
                }

                break;

            case R.id.encodeBtnWithLogo:

                contentEtString = contentEt.getText().toString().trim();
                if (TextUtils.isEmpty(contentEtString)) {
                    Toast.makeText(this, "请输入要生成二维码图片的字符串", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                bitmap = CodeCreator.createQRCode(contentEtString, 400, 400, logo);

                if (bitmap != null) {
                    contentIvWithLogo.setImageBitmap(bitmap);
                }

                break;

            case R.id.fragScanBtn:
                Intent intent = new Intent(this, FragmentActivity.class);
                startActivity(intent);
                break;


            default:
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {

                String content = data.getStringExtra(Constant.CODED_CONTENT);
                //zhanghao
                DreamInterfaceManager dreamInterfaceManager = (DreamInterfaceManager) getSystemService(Context.DREAM_INTERFACE);
                List<DreamApnInfo> ai = ApnQrCodec.unpackQrStringToApnList(content);
                for (DreamApnInfo apnInfo : ai) {
                    Log.d("zhanghao", "zzz query unpack apn " + toString(apnInfo));
                    dreamInterfaceManager.insertApn(getApplicationContext(), apnInfo);
                }
                result.setText("扫描结果为：" + content);
            }
        }
    }
    public String toString(DreamApnInfo apnInfo) {
        String name = apnInfo.name;
        String apn = apnInfo.apn;
        String proxy = apnInfo.proxy;
        String port = apnInfo.port;
        String mmsproxy = apnInfo.mmsproxy;
        String mmsport = apnInfo.mmsport;
        String user = apnInfo.user;
        String server = apnInfo.server;
        String password = apnInfo.password;
        String mmsc = apnInfo.mmsc;
        int author_type = apnInfo.author_type;
        String protocol = apnInfo.protocol;
        String roaming_protocol = apnInfo.roaming_protocol;
        String type = apnInfo.type;
        String mcc = apnInfo.mcc;
        String mnc = apnInfo.mnc;
        String ppp_number = apnInfo.ppp_number;
        int bearer_bitmask = apnInfo.bearer_bitmask;
        int bearer = apnInfo.bearer;
        String mvno_type = apnInfo.mvno_type;
        String mvno_match_data = apnInfo.mvno_match_data;
        return "name: " + name + ", apn: " + apn + ", proxy: " + proxy + ", port: " + port + ", mmsproxy: " + mmsproxy
                + ", mmsport: " + mmsport + ", user: " + user + ", server: " + server + ", password: " + password
                + ", mmsc: " + mmsc + ", author_type: " + author_type + ", protocol: " + protocol
                + ", roaming_protocol: " + roaming_protocol + ", type: " + type + ", mcc: " + mcc
                + ", mnc: " + mnc + ", ppp_number: " + ppp_number + ", bearer_bitmask: " + bearer_bitmask
                + ", bearer: " + bearer + ", mvno_type: " + mvno_type + ", mvno_match_data: "
                + mvno_match_data  ;
    }
}
