package com.guagusi.temple;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

/**
 * 头像选取代码片段
 * Created by maozi on 2014/3/20.
 */
public class AvatarActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = AvatarActivity.class.getSimpleName();

    private static final int CODE_SELECT_PHOTO = 0x0001;
    private static final int CODE_TAKE_PHOTO = 0x0002;
    private static final int CODE_CROP_PHOTO = 0x0003;

    private File mTempFile;
    private Button mBtnSelectPhoto, mBtnTakePhoto;
    private ImageView mImgVAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar);

        initViews();

        // 需要检查SDCard是否挂载，是否足够存储空间，否则在内部存储创建
        mTempFile = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + "-avatar");
    }

    private void initViews() {
        mBtnSelectPhoto = (Button) findViewById(R.id.btn_selectPhoto);
        mBtnTakePhoto = (Button) findViewById(R.id.btn_takePhoto);
        mImgVAvatar = (ImageView) findViewById(R.id.imgV_avatar);

        mBtnSelectPhoto.setOnClickListener(this);
        mBtnTakePhoto.setOnClickListener(this);
    }

    /**
     * 调用裁剪图片
     * @param inUri
     * @param aspectX
     * @param aspectY
     * @param outputX
     * @param outputY
     * @param outUri
     * @param requestCode
     */
    private void cropImage(Uri inUri, int aspectX, int aspectY, int outputX,
                           int outputY, Uri outUri, int requestCode) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(inUri, "image/*");
        cropIntent.putExtra("crop", "false");
        cropIntent.putExtra("aspectX", aspectX);
        cropIntent.putExtra("aspectY", aspectY);
        cropIntent.putExtra("outputX", outputX);
        cropIntent.putExtra("outputY", outputY);
        cropIntent.putExtra("outputFormat", "JPEG");
        cropIntent.putExtra("noFaceDetection", false);
        cropIntent.putExtra("return-data", false);
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
        startActivityForResult(cropIntent, requestCode);
    }

    @Override
    public void onClick(View view) {
        if(view == mBtnSelectPhoto) {
            Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT);
            // data 和 type 需要一起设置。setType会把data 赋值null,setData 会把type 赋值 null
            albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(albumIntent, CODE_SELECT_PHOTO);
        } else if(view == mBtnTakePhoto) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // 设置角度，单位度，只允许0 ，90， 180， 270
            cameraIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
            Uri uri = Uri.fromFile(mTempFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(cameraIntent, CODE_TAKE_PHOTO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case CODE_SELECT_PHOTO:
                case CODE_TAKE_PHOTO:
                    Uri uri = null;
                    if(data != null) {
                        uri = data.getData();
                        Uri outUri = Uri.fromFile(mTempFile);
                        cropImage(uri, 1, 1, 144, 144, outUri, CODE_CROP_PHOTO);
                    } else {
                        uri = Uri.fromFile(mTempFile);
                        cropImage(uri, 1, 1, 144, 144, uri, CODE_CROP_PHOTO);
                    }
                    break;
                case CODE_CROP_PHOTO:
                    Bitmap avatar = BitmapFactory.decodeFile(mTempFile.getAbsolutePath());
                    // 需要对avatar 采样
                    mImgVAvatar.setImageBitmap(avatar);
                    break;
                default:
                    break;
            }
        }
    }
}
