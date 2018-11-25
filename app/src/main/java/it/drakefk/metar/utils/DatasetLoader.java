package it.drakefk.metar.utils;

import android.util.Log;

import com.vuforia.DataSet;
import com.vuforia.ImageTarget;
import com.vuforia.MultiTarget;
import com.vuforia.ObjectTarget;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.Trackable;
import com.vuforia.TrackerManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import it.drakefk.metar.MetARMain;

public class DatasetLoader {

    private static final String TAG = "DatasetLoader";

    /** Flag used to select a Dataset from the storage with FileChooser */
    public static final int REQUESTCODE_PICK_DATASET = 1;

    private MetARMain mainActivity;

    /** List of the datasets loaded, a group of images that the application may track */
    private ArrayList<DataSet> mDatasetLoaded = new ArrayList<DataSet>();

    public DatasetLoader(MetARMain mainActivity) {
        this.mainActivity = mainActivity;
        this.mDatasetLoaded = this.mainActivity.getDatasetLoaded();
    }

    /**
     * Load a dataset from the given path, create a new
     * Dataset object and activate it
     * @param path the path where I find the .xml file
     * @param storageType STORAGE_TYPE.STORAGE_ABSOLUTE, STORAGE_TYPE.STORAGE_APPRESOURCE, STORAGE_TYPE.STORAGE_APP
     */
    public void loadAndActivateDataset(String path, int storageType){
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return;

        DataSet loadedDataset = objectTracker.createDataSet();

        if (loadedDataset == null)
            return;

        if (!loadedDataset.load(path,
                storageType))
            return;

        objectTracker.activateDataSet(loadedDataset);

        this.mDatasetLoaded.add(loadedDataset);
        this.addMarkers(loadedDataset);
    }

    /**
     * Load a dataset from the given path, create a new
     * Dataset object and activate it.
     * This is used when you use the .zip, the zip will be
     * extracted in the same folder and the extracted files
     * will be loaded.
     * @param path the path where I find the .xml file
     * @param storageType STORAGE_TYPE.STORAGE_ABSOLUTE, STORAGE_TYPE.STORAGE_APPRESOURCE, STORAGE_TYPE.STORAGE_APP
     */
    public void loadAndActivateDatasetFromZip(String path, int storageType){
        // First I check the extension
        if( path.endsWith(".xml")){
            this.loadAndActivateDataset(path, STORAGE_TYPE.STORAGE_ABSOLUTE);
            return;
        }
        else if( path.endsWith(".dat")){
            this.loadAndActivateDataset(path.substring(0, path.length()-4)+".xml", STORAGE_TYPE.STORAGE_ABSOLUTE);
            return;
        }
        else if( !path.endsWith(".zip")){
            return;
        }

        try {
            unzip(path, path.substring(0, path.length()-4));
            Log.d(TAG, "Load Dataset: unzipped "+path);
            String[] split = path.split("/");
            this.loadAndActivateDataset(path.substring(0, path.length()-4)
                            +"/"+split[split.length-1].substring(0, split[split.length-1].length()-4)+".xml"
                    , storageType);
        } catch (IOException e) {
            e.printStackTrace();
//            showToast(getString(R.string.failed_to_load)+" "+path);
        }
    }

    public void addMarkers( DataSet d){
        if( d == null)return;

//        Trackable marker;
//        for( int i = 0; i < d.getNumTrackables(); i++){
//            marker = d.getTrackable(i);
//            if( marker instanceof ImageTarget){
//                ImageTarget it = (ImageTarget) marker;
//            }
//            else if( marker instanceof MultiTarget){
//                MultiTarget mt = (MultiTarget) marker;
//                float[] dim = ((ImageTarget)mt.getPart(mt.getName()+".Front")).getSize().getData();
//                dim[2] = ((ImageTarget)mt.getPart(mt.getName()+".Right")).getSize().getData()[0];
//            }
//            else if( marker instanceof ObjectTarget){
//                ObjectTarget ot = (ObjectTarget) marker;
//            }
//        }
    }

    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipFilePath path of the file .zip
     * @param destDirectory path where to extract the file .zip
     * @throws IOException thrown by ZipInputStream
     */
    public static void unzip(String zipFilePath, String destDirectory) throws IOException {

        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));//new BufferedInputStream(new URL(zipFilePath).openStream()));//new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);

            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        int BUFFER_SIZE = 4096;
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

}
