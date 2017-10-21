package com.huantansheng.easyphotos.models.Album;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.huantansheng.easyphotos.R;
import com.huantansheng.easyphotos.constant.Path;
import com.huantansheng.easyphotos.models.Album.entity.Album;
import com.huantansheng.easyphotos.models.Album.entity.ImageItem;
import com.huantansheng.easyphotos.utils.String.StringUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * 专辑模型
 * Created by huan on 2017/10/20.
 */

public class AlbumModel {
    private static final String TAG = "AlbumModel";
    public ArrayList<ImageItem> selectedImages;//选择的图片
    public Album album;
    public int currAlbumIndex;
    private CallBack callBack;

    private final String[] projections = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media._ID};

    public interface CallBack {
        void onAlbumWorkedCallBack();
    }

    /**
     * AlbumModel构造方法
     * @param act 调用专辑的活动实体类
     * @param isShowCamera 是否显示相机按钮
     * @param callBack 初始化全部专辑后的回调
     */
    public AlbumModel(final Activity act, final boolean isShowCamera,AlbumModel.CallBack callBack) {
        selectedImages = new ArrayList<>();
        album = new Album();
        this.callBack = callBack;
        init(act,isShowCamera);
    }

    private void init(final Activity act, final boolean isShowCamera) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initAlbum(act,isShowCamera);
                callBack.onAlbumWorkedCallBack();
            }
        }).start();
    }

    private void initAlbum(Activity act, boolean isShowCamera) {

        Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        ContentResolver contentResolver = act.getContentResolver();
        Cursor cursor = contentResolver.query(contentUri, projections, null, null, sortOrder);
        if (cursor == null) {
            Log.d(TAG, "call: " + "Empty images");
        } else if (cursor.moveToFirst()) {
            String albumItem_all_name = act.getString(R.string.selector_folder_all);
            int pathCol = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            int nameCol = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
            int DateCol = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
            int WidthCol = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH);
            int HeightCol = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT);
            int mimeType = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);
            do {
                String path = cursor.getString(pathCol);
                String name = cursor.getString(nameCol);
                long dateTime = cursor.getLong(DateCol);
                int width = cursor.getInt(WidthCol);
                int height = cursor.getInt(HeightCol);
                String type = cursor.getString(mimeType);

                ImageItem imageItem = new ImageItem(false, name, path, dateTime, width, height, type);

                // 初始化“全部”专辑
                if (album.albumItems.size() == 0) {

                    currAlbumIndex = 0;

                    // 用第一个图片作为专辑的封面
                    album.addAlbumItem(albumItem_all_name, "", path);

                    // 是否显示相机按钮
                    if (isShowCamera) {
                        ImageItem cameraItem = new ImageItem(true, "", Path.CAMERA_ITEM_PATH, 0, 0, 0, "");
                        album.getAlbumItem(albumItem_all_name).addImageItem(cameraItem);
                    }
                }

                // 把图片全部放进“全部”专辑
                album.getAlbumItem(albumItem_all_name).addImageItem(imageItem);

                // 添加当前图片的专辑到专辑模型实体中
                String folderPath = new File(path).getParentFile().getAbsolutePath();
                String albumName = StringUtils.getLastPathSegment(folderPath);
                album.addAlbumItem(albumName, folderPath, path);
                album.getAlbumItem(albumName).addImageItem(imageItem);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

}