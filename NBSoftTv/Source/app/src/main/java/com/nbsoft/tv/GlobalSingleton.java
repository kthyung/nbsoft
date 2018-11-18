package com.nbsoft.tv;

import android.util.Log;

import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.Video;
import com.nbsoft.tv.model.FirebaseDataItem;

import java.util.HashMap;
import java.util.List;

public class GlobalSingleton {
    private static final String TAG = GlobalSingleton.class.getSimpleName();

    private volatile static GlobalSingleton uniqueInstance;

    private HashMap<String, FirebaseDataItem> mHashmapDataList;
    private HashMap<String, Channel> mHashmapChannel;
    private HashMap<String, Video> mHashmapVideo;

    private GlobalSingleton() { }

    public static GlobalSingleton getInstance() {
        if (uniqueInstance == null) {
            synchronized(GlobalSingleton.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new GlobalSingleton();
                }
            }
        }

        return uniqueInstance;
    }

    public void removeGlobalInstances(){
        if(mHashmapDataList!=null){
            mHashmapDataList.clear();
            mHashmapDataList = null;
        }
        if(mHashmapChannel!=null){
            mHashmapChannel.clear();
            mHashmapChannel = null;
        }
        if(mHashmapVideo!=null){
            mHashmapVideo.clear();
            mHashmapVideo = null;
        }
    }

    public void setDataList(HashMap<String, FirebaseDataItem> hashmapDataList){
        Log.d(TAG, "kth setDataList() hashmapDataList : " + (hashmapDataList!=null ? hashmapDataList.size() : ""));
        mHashmapDataList = hashmapDataList;
    }

    public HashMap<String, FirebaseDataItem> getDataList(){
        return mHashmapDataList;
    }

    public void addDataList(FirebaseDataItem item){
        if(mHashmapDataList == null){
            mHashmapDataList = new HashMap<String, FirebaseDataItem>();
        }

        mHashmapDataList.put(item.getCid(), item);
    }

    public void removeDataList(FirebaseDataItem item){
        if(mHashmapDataList == null){
            return;
        }

        mHashmapDataList.remove(item.getCid());
    }

    public FirebaseDataItem getDataListItem(String cid){
        if(mHashmapDataList == null){
            return null;
        }

        return mHashmapDataList.get(cid);
    }

    //--------------------------------------------------------------------------------------------//

    public void setChannelList(HashMap<String, Channel> hashmapChannelList){
        Log.d(TAG, "kth setDataList() hashmapDataList : " + (hashmapChannelList!=null ? hashmapChannelList.size() : ""));
        mHashmapChannel = hashmapChannelList;
    }

    public HashMap<String, Channel> getChannelList(){
        return mHashmapChannel;
    }

    public void addChannelList(Channel item){
        if(mHashmapChannel == null){
            mHashmapChannel = new HashMap<String, Channel>();
        }

        mHashmapChannel.put(item.getId(), item);
    }

    public void removeChannelList(Channel item){
        if(mHashmapChannel == null){
            return;
        }

        mHashmapChannel.remove(item.getId());
    }

    public Channel getChannelListItem(String cid){
        if(mHashmapChannel == null){
            return null;
        }

        return mHashmapChannel.get(cid);
    }

    //--------------------------------------------------------------------------------------------//

    public void setVideoList(HashMap<String, Video> hashmapVideoList){
        Log.d(TAG, "kth setDataList() hashmapDataList : " + (hashmapVideoList!=null ? hashmapVideoList.size() : ""));
        mHashmapVideo = hashmapVideoList;
    }

    public HashMap<String, Video> getVideoList(){
        return mHashmapVideo;
    }

    public void addVideoList(Video item){
        if(mHashmapVideo == null){
            mHashmapVideo = new HashMap<String, Video>();
        }

        mHashmapVideo.put(item.getId(), item);
    }

    public void removeVideoList(Video item){
        if(mHashmapVideo == null){
            return;
        }

        mHashmapVideo.remove(item.getId());
    }

    public Video getVideoListItem(String vid){
        if(mHashmapVideo == null){
            return null;
        }

        return mHashmapVideo.get(vid);
    }
}
